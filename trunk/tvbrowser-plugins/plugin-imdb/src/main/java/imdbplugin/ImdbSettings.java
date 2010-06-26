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
package imdbplugin;

import java.util.Properties;

import devplugin.Date;

final class ImdbSettings {

  private static final String KEY_DONT_ASK_CREATE_DATABASE = "dontAskCreateDatabase";
  private static final String KEY_DATABASE_VERSION = "databaseVersion";
  private static final String KEY_LAST_UPDATE = "LAST_UPDATE";
  private static final String KEY_NUMBER_OF_MOVIES = "NUMBER OF MOVIES";
  /**
   * database version number
   * <p>
   * Whenever the format of the database changes, this version number must be
   * incremented to reflect that a new import of the data is necessary.
   * </p>
   */
  private static final int CURRENT_DATABASE_VERSION = 6;
  private static final String KEY_MINIMUM_RATING = "minimum rating";
  private static final String KEY_HISTOGRAM = "histogram";

  private Properties mProperties;

  ImdbSettings(final Properties properties) {
    if (properties != null) {
      mProperties = properties;
    } else {
      mProperties = new Properties();
    }
  }

  Properties storeSettings() {
    return mProperties;
  }

  void askCreateDatabase(final boolean ask) {
    mProperties.setProperty(KEY_DONT_ASK_CREATE_DATABASE, Boolean.toString(ask));
  }

  /**
   * shall the plugin ask to create a new database if none is available?
   *
   * @return
   */
  boolean askCreateDatabase() {
    return !Boolean.parseBoolean(mProperties.getProperty(KEY_DONT_ASK_CREATE_DATABASE, "false"));
  }

  /**
   * check if the local database has the structure and format expected by this
   * version of the plugin
   *
   * @return
   */
  boolean isDatabaseCurrentVersion() {
    return Integer
        .parseInt(mProperties.getProperty(KEY_DATABASE_VERSION, "-1")) == CURRENT_DATABASE_VERSION;
  }

  /**
   * mark the newly imported local database as being current, so it fulfills the
   * requirements of the current plugin version
   */
  void setCurrentDatabaseVersion() {
    mProperties.setProperty(KEY_DATABASE_VERSION, Integer
        .toString(CURRENT_DATABASE_VERSION));
  }

  String getUpdateDate() {
    return mProperties.getProperty(KEY_LAST_UPDATE,"-");
  }

  String getNumberOfMovies() {
    return mProperties.getProperty(KEY_NUMBER_OF_MOVIES,"0");
  }

  void setNumberOfMovies(final int ratingCount) {
    mProperties.setProperty(KEY_NUMBER_OF_MOVIES, String.valueOf(ratingCount));
  }

  void setUpdateDate(final Date currentDate) {
    mProperties.setProperty(KEY_LAST_UPDATE, currentDate.toString());
  }

  int getMinimumRating() {
    return Integer.valueOf(mProperties.getProperty(KEY_MINIMUM_RATING, "50"));
  }

  void setMinimumRating(final int rating) {
    mProperties.setProperty(KEY_MINIMUM_RATING, String.valueOf(rating));
  }

  void setHistogram(final ImdbHistogram histogram) {
    int[] data = histogram.getData();
    for (int i = 0; i < data.length; i++) {
      mProperties.setProperty(KEY_HISTOGRAM + String.valueOf(i), String.valueOf(data[i]));
    }
  }

  int[] getHistogram() {
    int[] histogram = new int[ImdbRating.MAX_RATING_NORMALIZATION + 1];
    for (int i = 0; i < histogram.length; i++) {
      histogram[i] = Integer.valueOf(mProperties.getProperty(KEY_HISTOGRAM + String.valueOf(i), "0"));
    }
    return histogram;
  }
}
