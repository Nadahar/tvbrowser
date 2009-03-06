/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package movieawardplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * Hash map for fast movie lookup. For all titles of the movie it creates an
 * entry in the internal lookup table by using the hashCode() of the lower case
 * title as key.
 * 
 * @author Bananeweizen
 * 
 */
public class MovieHashMap {
  private HashMap<Integer, ArrayList<Movie>> mMovieLookup = new HashMap<Integer, ArrayList<Movie>>();

  public void addMovie(final Movie movie) {
    final HashSet<String> singularTitles = new HashSet<String>();
    // hash titles
    singularTitles.addAll(movie.getTitles().values());
    // hash alternative titles
    final HashMap<String, ArrayList<String>> altTitles = movie
        .getAlternativeTitles();
    for (ArrayList<String> langTitles : altTitles.values()) {
      singularTitles.addAll(langTitles);
    }
    // add each title only once
    for (String title : singularTitles) {
      addMovieTitle(movie, title);
    }
  }

  private void addMovieTitle(final Movie movie, final String title) {
    final int hash = title.toLowerCase().hashCode();
    ArrayList<Movie> list = mMovieLookup.get(hash);
    if (list == null) {
      list = new ArrayList<Movie>(2);
      mMovieLookup.put(hash, list);
    }
    list.add(movie);
  }

  public ArrayList<Movie> getMovies(final Program program) {
    final int titleHash = program.getTitle().toLowerCase().hashCode();
    final String original = program
        .getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
    if (original == null || original.length() == 0) {
      return mMovieLookup.get(titleHash);
    } else {
      final ArrayList<Movie> movies = mMovieLookup.get(titleHash);
      final int originalHash = original.toLowerCase().hashCode();
      if (originalHash != titleHash) {
        final ArrayList<Movie> originalMovies = mMovieLookup.get(originalHash);
        if (originalMovies != null) {
          if (movies != null) {
            // filter duplicates
            final HashSet<Movie> result = new HashSet<Movie>(movies);
            result.addAll(originalMovies);
            return new ArrayList<Movie>(result);
          } else {
            return originalMovies;
          }
        }
      }
      return movies;
    }
  }

}
