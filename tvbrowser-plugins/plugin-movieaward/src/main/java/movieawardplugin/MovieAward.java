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
 * CVS information:
 *     $Date: 2007-10-02 10:19:08 +0200 (Di, 02 Okt 2007) $
 *   $Author: Bananeweizen $
 * $Revision: 3966 $
 */
package movieawardplugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import devplugin.Program;

public class MovieAward {
  private static Logger mLog = Logger.getLogger(MovieAward.class.getName());

  private HashMap<String, String> mNames = new HashMap<String, String>();
  private HashMap<String, MovieAwardCategory> mCategorie = new HashMap<String, MovieAwardCategory>();
  private ArrayList<Movie> mMovies = new ArrayList<Movie>();
  private HashMap<String, Award> mAwards = new HashMap<String, Award>();
  private String mUrl;

  public MovieAward() {
  }

  /**
   * Add a name for the award
   * @param language Language (e.g. en, de, de-at)
   * @param name Name of the Award
   */
  public void addName(String language, String name) {
    mLog.info("Added movie award " + language + "_---" + name);
    mNames.put(language, name);
  }

  /**
   * Add a category for the award
   * @param category category
   */
  public void addCategorie(MovieAwardCategory category) {
    mCategorie.put(category.getId(), category);
  }

  public void addMovie(Movie movie) {
    mMovies.add(movie);
  }

  public void addAward(Award award) {
    mAwards.put(award.getMovieId(), award);
  }

  public void setUrl(String url) {
    mUrl = url;
  }

  public String getUrl() {
    return mUrl;
  }

  public boolean containsAwardFor(final Program program) {
    for (Movie movie:mMovies) {
      if (movie.matchesProgram(program) && mAwards.containsKey(movie.getId())) {
        return true;
      }
    }
    return false;
  }

}
