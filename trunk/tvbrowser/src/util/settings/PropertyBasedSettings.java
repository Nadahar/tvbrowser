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
package util.settings;

import java.util.Properties;

/**
 * @author bananeweizen
 * 
 */
public class PropertyBasedSettings {

  private Properties mProperties;

  public PropertyBasedSettings(final Properties properties) {
    if (properties == null) {
      mProperties = new Properties();
    } else {
      mProperties = properties;
    }
  }

  public Properties storeSettings() {
    return mProperties;
  }

  protected String get(final String key) {
    return get(key, null);
  }

  protected void set(final String key, final String value) {
    mProperties.setProperty(key, value);
  }

  protected void remove(final String key) {
    mProperties.remove(key);
  }

  protected String get(final String key, final String defaultValue) {
    return mProperties.getProperty(key, defaultValue);
  }

  protected boolean get(final String key, final boolean defaultValue) {
    return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
  }

  protected void set(final String key, final boolean value) {
    set(key, String.valueOf(value));
  }

  protected int get(final String key, final int defaultValue) {
    return Integer.valueOf(get(key, String.valueOf(defaultValue)));
  }

  protected void set(final String key, final int defaultValue) {
    set(key, String.valueOf(defaultValue));
  }
}
