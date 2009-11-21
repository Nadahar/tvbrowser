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

public class ZattooChannelProperties extends ChannelProperties {

  private ZattooSettings mSettings;

  public ZattooChannelProperties(final String fileName, ZattooSettings zattooSettings) {
    super(fileName);
    mSettings = zattooSettings;
  }

  @Override
  protected void checkProperties() {
    //
  }

  @Override
  protected boolean isValidProperty(final String id) {
    if (id== null) {
      return false;
    }
    if (id.length() == 0) {
      return false;
    }
    if (id.startsWith("=")) {
      return false;
    }
    int comma = id.indexOf(',');
    if (!mSettings.getUseLocalPlayer() && comma >= 0) {
      return id.substring(comma + 1).trim().length() > 0;
    } else if (!mSettings.getUseLocalPlayer() && comma == -1) {
      return false;
    } else {
      return true;
    }
  }

}
