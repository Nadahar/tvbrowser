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
package genreplugin;

import java.util.ArrayList;
import java.util.Properties;

import util.settings.PropertyBasedSettings;

/**
 * @author bananeweizen
 *
 */
class GenreSettings extends PropertyBasedSettings {

  private final static String SETTINGS_DAYS = "days";
  private final static String FILTERED_GENRE = "filteredGenre";
  private final static String FILTERED_GENRES_COUNT = "filteredGenresCount";
  private static final String UNIFY_BRACE_GENRES = "unifyBraces";

  GenreSettings(final Properties properties) {
    super(properties);
  }

  public int getDays() {
    return get(SETTINGS_DAYS, 7);
  }

  public void setDays(int days) {
    set(SETTINGS_DAYS, days);
  }
  
  public void setHiddenGenres(final Object[] hidden) {
    set(FILTERED_GENRES_COUNT, hidden.length);
    for (int i = 0; i < hidden.length; i++) {
      set(FILTERED_GENRE + i, (String) hidden[i]);
    }
  }

  public String[] getHiddenGenres() {
    final int filterCount = get(FILTERED_GENRES_COUNT, 0);
    ArrayList<String> genres = new ArrayList<String>(filterCount);
    for (int i = 0; i<filterCount; i++) {
      String genre = get(FILTERED_GENRE + i, "");
      if (!genre.isEmpty()) {
        genres.add(genre);
      }
    }
    return genres.toArray(new String[genres.size()]);
  }

  public boolean getUnifyBraceGenres() {
    return get(UNIFY_BRACE_GENRES, true);
  }

  public void setUnifyBraceGenres(boolean unify) {
    set(UNIFY_BRACE_GENRES, unify);
  }
}
