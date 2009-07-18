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

public class ZattooSettings {

  private static final String KEY_PLAYER = "PLAYER";
  private static final String KEY_COUNTRY = "COUNTRY";
  
  private static final int PLAYER_LOCAL = 0;
  private static final int PLAYER_WEB = 1;
  private static final int PLAYER_PRISM = 2;

  private Properties mProperties;

  public ZattooSettings(final Properties properties) {
    mProperties = properties;
  }

  public Properties storeSettings() {
    return mProperties;
  }
  
  private int getPlayer() {
    return getProperty(KEY_PLAYER, ZattooPlugin.canUseLocalPlayer() ? PLAYER_LOCAL : PLAYER_WEB);
  }

  private int getProperty(String key, int defaultValue) {
    return Integer.valueOf(mProperties.getProperty(key, String.valueOf(defaultValue)));
  }

  public boolean getUseWebPlayer() {
    return PLAYER_WEB == getPlayer();
  }

  public String getCountry() {
    return mProperties.getProperty(KEY_COUNTRY, "de");
  }

  public void setCountry(final String country) {
    mProperties.setProperty(KEY_COUNTRY, country);
  }

  public void setWebPlayer() {
    setPlayer(PLAYER_WEB);
  }

  private void setPlayer(int player) {
    setProperty(KEY_PLAYER, player);
  }

  private void setProperty(String key, int value) {
    mProperties.setProperty(key, String.valueOf(value));
  }

  public void setLocalPlayer() {
    setPlayer(PLAYER_LOCAL);
  }

  public void setPrismPlayer() {
    setPlayer(PLAYER_PRISM);
  }

  public boolean getPrismPlayer() {
    return getPlayer() == PLAYER_PRISM;
  }

}
