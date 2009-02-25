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
package mediathekplugin;

import java.util.Properties;

public class MediathekSettings {
  private Properties mProperties;

  public MediathekSettings(final Properties settings) {
    if (settings == null) {
      this.mProperties = new Properties();
    } else {
      this.mProperties = settings;
    }
  }

  private static final String KEY_READ_EPISODES_ON_START = "readProgramsOnStart";

  public boolean isReadEpisodesOnStart() {
    return mProperties.getProperty(KEY_READ_EPISODES_ON_START, "false").equals("true");
  }

  public Properties storeSettings() {
    return mProperties;
  }

  public void setReadEpisodesOnStart(final boolean selected) {
    mProperties.setProperty(KEY_READ_EPISODES_ON_START, selected ? "true" : "false");
  }
}
