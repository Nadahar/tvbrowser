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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class MovieDatabaseHandler extends DefaultHandler {
  private static final String ELEMENT_ALTERNATIVETITLE = "alternativetitle";

  private static final String ATTRIBUTE_LANG = "lang";

  private static final String ATTRIBUTE_ID = "id";

  private static final String ELEMENT_TITLE = "title";

  private static final String ATTRIBUTE_YEAR = "year";

  private static final String ATTRIBUTE_DIRECTOR = "director";

  private static final String ELEMENT_MOVIES = "movies";

  private static final String ELEMENT_MOVIE = "movie";

  private static final Logger mLog = Logger.getLogger(MovieDatabaseHandler.class.getName());

  /**
   * Holds the text of the current tag.
   */
  private StringBuilder mText;
  private Attributes mAttributes;

  /**
   * Name of the Parent-Node
   */
  private ArrayList<String> mNodeNames = new ArrayList<String>();
  /**
   * The current movie award to fill with data
   */
  private MovieDatabase mMovieDatabase;
  private Movie mMovie;

  public MovieDatabaseHandler(final MovieDatabase movieAward) {
    mMovieDatabase = movieAward;
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

    mAttributes = attributes;

    if (ELEMENT_MOVIE.equals(qName)) {
      mMovie = new Movie(attributes.getValue(ATTRIBUTE_ID));

      int year;
      try {
        year = Integer.parseInt(attributes.getValue(ATTRIBUTE_YEAR));
      } catch (NumberFormatException ex) {
        year = -1;
      }

      mMovie.setProductionYear(year);
      mMovie.setDirector(attributes.getValue(ATTRIBUTE_DIRECTOR));
    } else {
      if (!ELEMENT_MOVIES.equals(qName) && !ELEMENT_TITLE.equals(qName) && !ELEMENT_ALTERNATIVETITLE.equals(qName)) {
        mLog.log(Level.INFO, "Unknown Element : " + qName);
      }
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

    if (ELEMENT_TITLE.equals(qName) && ELEMENT_MOVIE.equals(parent)) {
      final boolean original = "yes".equalsIgnoreCase(mAttributes
          .getValue("original"));
      mMovie.addTitle(mAttributes.getValue(ATTRIBUTE_LANG), mText.toString(), original);
    } else if (ELEMENT_MOVIE.equals(qName)) {
      mMovieDatabase.addMovie(mMovie);
    } if (ELEMENT_ALTERNATIVETITLE.equals(qName) && ELEMENT_MOVIE.equals(parent)) {
      mMovie.addAlternativeTitle(mAttributes.getValue(ATTRIBUTE_LANG), mText.toString());
    }

    mNodeNames.remove(mNodeNames.size() -1);
  }


  /**
   * Clears a StringBuffer
   *
   * @param buffer The StringBuffer to clear.
   */
  private void clear(final StringBuilder buffer) {
    buffer.setLength(0);
  }
}