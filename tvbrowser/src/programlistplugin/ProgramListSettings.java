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
package programlistplugin;

import java.util.Properties;

import util.settings.PropertyBasedSettings;

/**
 * @author bananeweizen
 * 
 */
public class ProgramListSettings extends PropertyBasedSettings {
  private static final String KEY_INDEX = "index";
  private static final String KEY_FILTER = "filter";
  private static final String KEY_SHOW_DESCRIPTION = "showDescription";

  public ProgramListSettings(final Properties properties) {
    super(properties);
  }

  public int getIndex() {
    return get(KEY_INDEX, 0);
  }

  public boolean getShowDescription() {
    return get(KEY_SHOW_DESCRIPTION, true);
  }

  public void setShowDescription(final boolean show) {
    set(KEY_SHOW_DESCRIPTION, show);
  }

  public void setIndex(int index) {
    set(KEY_INDEX, index);
  }

  public String getFilterName() {
    return get(KEY_FILTER, "");
  }

  public void setFilterName(final String name) {
    set(KEY_FILTER, name);
  }

}
