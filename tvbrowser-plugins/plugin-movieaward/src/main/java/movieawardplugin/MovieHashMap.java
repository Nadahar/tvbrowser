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

import devplugin.Program;

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

  public void addMovie(Movie movie) {
    // hash titles
    final HashMap<String, String> titles = movie.getTitles();
    for (String title : titles.values()) {
      addMovieTitle(movie, title);
    }
    // hash alternative titles
    HashMap<String, ArrayList<String>> altTitles = movie.getAlternativeTitles();
    for (ArrayList<String> langTitles : altTitles.values()) {
      for (String title : langTitles) {
        addMovieTitle(movie, title);
      }
    }
  }

  private void addMovieTitle(Movie movie, String title) {
    int hash = title.toLowerCase().hashCode();
    ArrayList<Movie> list = mMovieLookup.get(hash);
    if (list == null) {
      list = new ArrayList<Movie>(2);
      mMovieLookup.put(hash, list);
    }
    list.add(movie);
  }

  public ArrayList<Movie> getMovies(Program program) {
    return mMovieLookup.get(program.getTitle().toLowerCase().hashCode());
  }

}
