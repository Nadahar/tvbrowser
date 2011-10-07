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
package i18nplugin;

import java.util.Properties;

import util.settings.PropertyBasedSettings;

/**
 * @author bananeweizen
 *
 */
public final class I18NSettings extends PropertyBasedSettings {

  public I18NSettings(final Properties properties) {
    super(properties);
  }

  public void setDivider(int divider) {
    set("DialogDevider.Location", divider);
  }

  public int getDivider() {
    return get("DialogDevider.Location", 0);
  }

}
