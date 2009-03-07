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
package widgetplugin;

import java.util.Properties;

public class WidgetSettings {

  private static final String SETTING_PORT_NUMBER = "portNumber";
  private static final String SETTING_PORT_NUMBER_DEFAULT = "34567";
  private static final String SETTING_REFRESH = "refresh";
  
  private Properties mSettings;

  public WidgetSettings(final Properties settings) {
    mSettings = settings;
  }

  public int getPortNumber() {
    return Integer.valueOf(mSettings.getProperty(SETTING_PORT_NUMBER,
        SETTING_PORT_NUMBER_DEFAULT));
  }

  public boolean getRefresh() {
    return Boolean.valueOf(mSettings.getProperty(SETTING_REFRESH, "true"));
  }

  public void setPortNumber(final int port) {
    mSettings.setProperty(SETTING_PORT_NUMBER, String.valueOf(port));
  }

  public void setRefresh(final boolean selected) {
    mSettings.setProperty(SETTING_REFRESH, selected ? "true" : "false");
  }

  public Properties storeSettings() {
    return mSettings;
  }

}
