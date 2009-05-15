/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * VCS information:
 *     $Date: 2007-10-02 10:19:08 +0200 (Di, 02 Okt 2007) $
 *   $Author: Bananeweizen $
 * $Revision: 3966 $
 */
package movieawardplugin;

import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class MovieAwardHandler extends DefaultHandler {
  private static final String CATEGORIES = "categories";

  private static final String AWARDS = "awards";

  private static final String MOVIES = "movies";

  private static final String AWARD_DATA = "awarddata";

  private static final String PROVIDED_BY = "provided_by";

  private static final String ALTERNATIVE_TITLE = "alternativetitle";

  private static final String ORIGINAL = "original";

  private static final String NAME = "name";

  private static final String TITLE = "title";

  private static final String LANGUAGE = "lang";

  private static final String URL = "url";

  private static final String AWARD = "award";

  private static final String RECIPIENT = "recipient";

  private static final String MOVIE = "movie";

  private static final String STATUS = "status";

  private static final String YEAR = "year";

  private static final String DIRECTOR = "director";

  private static final String ATTRIBUTE_ID = "id";

  private static final String CATEGORY = "category";
  
//Bolle Edit: Additional Information for Best Song and Animated Short Subject Category 
  private static final String ADD_INFO = "add_info";
//  
  private static Logger mLog = Logger.getLogger(MovieAwardHandler.class.getName());

  /**
   * Holds the text of the current tag.
   */
  private StringBuilder mText;
  private Properties mAttributes;

  /**
   * Name of the Parent-Node
   */
  private ArrayList<String> mNodeNames = new ArrayList<String>();
  /**
   * The current movie award to fill with data
   */
  private MovieAward mAward;
  private MovieAwardCategory mCategorie;
  private Movie mMovie;

  public MovieAwardHandler(final MovieAward movieAward) {
    mAward = movieAward;
    mText = new StringBuilder();
  }

  /**
   * Handles the occurrence of tag text.
   */
  @Override
  public void characters(final char ch[], final int start, final int length)
      throws SAXException {
    // There is some text -> Add it to the text buffer
    mText.append(ch, start, length);
  }

  /**
   * Handles the occurrence of a start tag.
   */
  @Override
  public void startElement(final String uri, final String localName,
      final String qName, final Attributes attributes)
      throws SAXException {

    mNodeNames.add(qName);
    // A new tag begins -> Clear the text buffer
    clear(mText);

    String parent = null;
    if (mNodeNames.size() > 1) {
      parent = mNodeNames.get(mNodeNames.size() - 2);
    }

    mAttributes = new Properties();

    for (int i = 0; i < attributes.getLength(); i++) {
      mAttributes.setProperty(attributes.getQName(i), attributes.getValue(i));
    }

    if (CATEGORY.equals(qName)) {
      mCategorie = new MovieAwardCategory(attributes.getValue(ATTRIBUTE_ID));
    } else if (MOVIE.equals(qName)) {
      mMovie = new Movie(attributes.getValue(ATTRIBUTE_ID));

      int year = -1;
      final String yearString = attributes.getValue(YEAR);
      if (yearString != null) {
        try {
          year = Integer.parseInt(yearString);
        } catch (NumberFormatException ex) {
          year = -1;
        }
      }

      mMovie.setProductionYear(year);
      mMovie.setDirector(attributes.getValue(DIRECTOR));
    } else if (AWARD.equals(qName)) {
      int year = -1;
      final String yearString = attributes.getValue(YEAR);
      if (yearString != null) {
        try {
          year = Integer.parseInt(yearString);
        } catch (NumberFormatException ex) {
          year = -1;
        }
      }

      mAward.addAward(new Award(attributes.getValue(CATEGORY),
                                attributes.getValue(STATUS),
                                attributes.getValue(MOVIE),
                                year,
                                attributes.getValue(RECIPIENT),
                                //Bolle Edit: ADD_INFO added
                                attributes.getValue(ADD_INFO)
          ));
    } else if (PROVIDED_BY.equals(qName)) {
      if (attributes.getValue(URL) != null) {
        mAward.setProviderUrl(attributes.getValue(URL));
      }
    } else if (AWARD_DATA.equals(qName) || NAME.equals(qName)
        || MOVIES.equals(qName) || AWARDS.equals(qName)
        || ALTERNATIVE_TITLE.equals(qName) || CATEGORIES.equals(qName)
        || CATEGORY.equals(qName) || TITLE.equals(qName)
        || URL.equals(qName)) {
      // Do nothing
    } else {
      mLog.log(Level.INFO, "Unknown Element : " + qName);
    }

  }

  /**
   * Handles the occurrence of an end tag.
   */
  @Override
  public void endElement(final String uri, final String localName,
      final String qName)
      throws SAXException {
    String parent = null;
    if (mNodeNames.size() > 1) {
      parent = mNodeNames.get(mNodeNames.size() - 2);
    }

    if (NAME.equals(qName)) {
      if (AWARD_DATA.equals(parent)) {
        mAward.addName(mAttributes.getProperty(LANGUAGE), mText.toString());
      } else if (CATEGORY.equals(parent)) {
        mCategorie.addName(mAttributes.getProperty(LANGUAGE), mText.toString());
      }
    } else if (CATEGORY.equals(qName)) {
      mAward.addCategorie(mCategorie);
    } else if (TITLE.equals(qName) && MOVIE.equals(parent)) {
      final boolean original = "yes".equalsIgnoreCase(mAttributes
          .getProperty(ORIGINAL));
      mMovie.addTitle(mAttributes.getProperty(LANGUAGE), mText.toString(), original);
    } else if (MOVIE.equals(qName)) {
      mAward.addMovie(mMovie);
    } else if (URL.equals(qName) && (AWARD_DATA.equals(parent))) {
      mAward.setUrl(mText.toString());
    } else if (PROVIDED_BY.equals(qName)) {
      mAward.setProviderName(mText.toString());
    } else if (ALTERNATIVE_TITLE.equals(qName) && MOVIE.equals(parent)) {
      mMovie.addAlternativeTitle(mAttributes.getProperty(LANGUAGE), mText.toString());
    }

    mNodeNames.remove(mNodeNames.size() -1);
  }


  /**
   * Clears a StringBuffer
   *
   * @param buffer The StringBuffer to clear.
   */
  private void clear(final StringBuilder buffer) {
    buffer.delete(0, buffer.length());
  }
}
