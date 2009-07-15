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
package zattooplugin;

import java.util.Properties;

import util.misc.OperatingSystem;

public class ZattooSettings {

  private static final String KEY_USE_WEBPLAYER = "usewebplayer";
  private static final String KEY_COUNTRY = "COUNTRY";

  private Properties mProperties;

  public ZattooSettings(final Properties properties) {
    mProperties = properties;
  }

  public Properties storeSettings() {
    return mProperties;
  }

  public boolean getUseWebPlayer() {
    return getProperty(KEY_USE_WEBPLAYER, !OperatingSystem.isWindows());
  }

  private boolean getProperty(final String key, final boolean defaultValue) {
    return Boolean.valueOf(mProperties.getProperty(key, String.valueOf(defaultValue)));
  }

  public String getCountry() {
    return mProperties.getProperty(KEY_COUNTRY, "de");
  }

  public void setCountry(final String country) {
    mProperties.setProperty(KEY_COUNTRY, country);
  }

  public void setUseWebPlayer(boolean useWeb) {
    setProperty(KEY_USE_WEBPLAYER, useWeb);
  }

  private void setProperty(final String key, final boolean value) {
    mProperties.setProperty(key, String.valueOf(value));
  }

}
