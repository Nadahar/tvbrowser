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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

import devplugin.Program;

public class MovieAward implements Comparable<MovieAward> {
  private static Logger mLog = Logger.getLogger(MovieAward.class.getName());

  private HashMap<String, String> mNames = new HashMap<String, String>();
  private HashMap<String, MovieAwardCategory> mCategory = new HashMap<String, MovieAwardCategory>();
  private HashMap<String, ArrayList<Award>> mAwards = new HashMap<String, ArrayList<Award>>();
  private String mUrl;
  private String mProviderName;
  private String mProviderUrl;

  private MovieDatabase mMovieDatabase;
  
  private MovieHashMap mMovies;

  public MovieAward(final MovieDatabase database) {
    mMovieDatabase = database;
    mMovies = new MovieHashMap();
  }

  /**
   * Add a name for the award
   * @param language Language (e.g. en, de, de-at)
   * @param name Name of the Award
   */
  public void addName(final String language, final String name) {
    mLog.info("Added movie award " + name + " (" + language + ')');
    mNames.put(language.toLowerCase(), name);
  }

  /**
   * Add a category for the award
   * @param category category
   */
  public void addCategorie(final MovieAwardCategory category) {
    mCategory.put(category.getId(), category);
  }

  public void addMovie(final Movie movie) {
    mMovies.addMovie(movie);
  }

  public void addAward(final Award award) {
    ArrayList<Award> awardList = mAwards.get(award.getMovieId());

    if (awardList == null) {
      awardList = new ArrayList<Award>();
      mAwards.put(award.getMovieId(), awardList);
    }
    awardList.add(award);
  }

  public void setUrl(final String url) {
    mUrl = url;
  }

  public String getUrl() {
    return mUrl;
  }

  public boolean containsAwardFor(final Program program) {
    // At first try to find movies in the award-data
    final ArrayList<Movie> movies = mMovies.getMovies(program);
    if (movies != null) {
      for (Movie movie : movies) {
        if (movie.matchesProgram(program) && mAwards.containsKey(movie.getId())) {
          return true;
        }
      }
    }

    // No movie found, try to find a movie-id in the global movie database
    final Movie movie = mMovieDatabase.getMovieFor(program);
    return (movie != null) && (mAwards.containsKey(movie.getId()));
  }

  public Award[] getAwardsFor(final Program program) {
    final ArrayList<Award> list = new ArrayList<Award>();

    final ArrayList<Movie> movies = mMovies.getMovies(program);
    if (movies != null) {
      for (Movie movie : movies) {
        if (movie.matchesProgram(program) && mAwards.containsKey(movie.getId())) {
          for (Award award : mAwards.get(movie.getId())) {
            list.add(award);
          }
        }
      }
    }

    // If no Movie was found, try to get the id from the MovieDatabase
    final Movie movie = mMovieDatabase.getMovieFor(program);

    if (movie != null) {
      final ArrayList<Award> awards = mAwards.get(movie.getId());
      if (awards != null) {
        for (Award award:mAwards.get(movie.getId())) {
          list.add(award);
        }
      }
    }

    return list.toArray(new Award[list.size()]);
  }

  public String getName() {
    String name = mNames.get(Locale.getDefault().getLanguage());
    if (name== null) {
      name = mNames.get("en");
    }
    return name;
  }

  public String getCategoryName(final String category) {
    final MovieAwardCategory cat = mCategory.get(category);

    if (cat == null) {
      return category;
    }
    
    String name = cat.getName(Locale.getDefault().getLanguage());

    if (name == null) {
      name = cat.getName("en");
    }

    return name;
  }

  public void setProviderName(final String providerName) {
    mProviderName = providerName;
  }

  public void setProviderUrl(final String providerUrl) {
    mProviderUrl = providerUrl;
  }

  public String getProviderUrl() {
    return mProviderUrl;
  }

  public String getProviderName() {
    return mProviderName;
  }

  public int compareTo(final MovieAward other) {
    return getName().compareTo(other.getName());
  }
}
