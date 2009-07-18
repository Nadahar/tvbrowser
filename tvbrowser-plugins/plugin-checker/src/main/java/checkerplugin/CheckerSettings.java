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
package checkerplugin;

import java.util.Properties;

/**
 * @author bananeweizen
 *
 */
public final class CheckerSettings {

  private static final String KEY_AUTOSTART = "autostart";
  private Properties mProperties;

  public CheckerSettings(Properties properties) {
    if (properties == null) {
      mProperties = new Properties();
    }
    else {
      mProperties = properties;
    }
  }

  public boolean getAutostart() {
    return getProperty(KEY_AUTOSTART, false);
  }

  private boolean getProperty(final String key, boolean defaultValue) {
    return Boolean.valueOf(getProperty(key, String.valueOf(defaultValue)));
  }

  private String getProperty(final String key, final String defaultValue) {
    return mProperties.getProperty(key, defaultValue);
  }

  public void setAutostart(final boolean autoStart) {
    setProperty(KEY_AUTOSTART, autoStart);
  }

  private void setProperty(final String key, final boolean value) {
    setProperty(key, String.valueOf(value));
  }

  private void setProperty(final String key, final String value) {
    mProperties.setProperty(key, value);
  }

  public Properties storeSettings() {
    return mProperties;
  }

}
