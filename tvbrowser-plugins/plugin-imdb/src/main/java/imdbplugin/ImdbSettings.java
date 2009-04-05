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

public class ImdbSettings {

  private static final String KEY_DONT_ASK_CREATE_DATABASE = "dontAskCreateDatabase";
  private static final String KEY_DATABASE_VERSION = "databaseVersion";
  /**
   * database version number
   * <p>
   * Whenever the format of the database changes, this version number must be
   * incremented to reflect that a new import of the data is necessary.
   * </p>
   */
  private static final int CURRENT_DATABASE_VERSION = 2;
  
  private Properties mProperties;

  public ImdbSettings(final Properties properties) {
    if (properties != null) {
      mProperties = properties;
    } else {
      mProperties = new Properties();
    }
  }

  public Properties storeSettings() {
    return mProperties;
  }

  public void askCreateDatabase(final boolean ask) {
    mProperties.setProperty(KEY_DONT_ASK_CREATE_DATABASE, Boolean.toString(ask));
  }

  /**
   * shall the plugin ask to create a new database if none is available?
   * 
   * @return
   */
  public boolean askCreateDatabase() {
    return mProperties.getProperty(KEY_DONT_ASK_CREATE_DATABASE, "false").equals(
        "false");
  }

  /**
   * check if the local database has the structure and format expected by this
   * version of the plugin
   * 
   * @return
   */
  public boolean isDatabaseCurrentVersion() {
    return Integer
        .parseInt(mProperties.getProperty(KEY_DATABASE_VERSION, "-1")) == CURRENT_DATABASE_VERSION;
  }

  /**
   * mark the newly imported local database as being current, so it fulfills the
   * requirements of the current plugin version
   */
  public void setCurrentDatabaseVersion() {
    mProperties.setProperty(KEY_DATABASE_VERSION, Integer
        .toString(CURRENT_DATABASE_VERSION));
  }
}
