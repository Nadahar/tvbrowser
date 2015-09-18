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

import java.util.HashMap;
import java.util.Properties;

import util.settings.PropertyBasedSettings;

/**
 * @author bananeweizen
 * 
 */
public class ProgramListSettings extends PropertyBasedSettings {
  private static final String KEY_INDEX = "index";
  private static final String KEY_FILTER = "filter";
  public static final String KEY_SHOW_DESCRIPTION = "showDescription";
  public static final String KEY_PROVIDE_TAB = "provideTab";
  public static final String KEY_SHOW_DATE_SEPARATOR = "showDateSeparator";
  public static final String KEY_TAB_REACT_ON_FILTER_CHANGE = "reactOnFilterChange";
  public static final String KEY_TAB_TIME_SCROLL_AROUND = "tabTimeScrollAround";
  public static final String KEY_TAB_REACT_ON_TIME = "tabReactOnTime";
  public static final String KEY_TAB_REACT_ON_CHANNEL = "tabReactOnChannel";
  public static final String KEY_TAB_REACT_ON_DATE = "tabReactOnDate";
  
  private static final HashMap<String, Boolean> mDefaultMap;
  
  static {
    mDefaultMap = new HashMap<String, Boolean>();
    
    mDefaultMap.put(KEY_SHOW_DESCRIPTION, true);
    mDefaultMap.put(KEY_PROVIDE_TAB, true);
    mDefaultMap.put(KEY_SHOW_DATE_SEPARATOR, true);
    mDefaultMap.put(KEY_TAB_REACT_ON_FILTER_CHANGE, true);
    mDefaultMap.put(KEY_TAB_TIME_SCROLL_AROUND, false);
    mDefaultMap.put(KEY_TAB_REACT_ON_TIME, true);
    mDefaultMap.put(KEY_TAB_REACT_ON_CHANNEL, true);
    mDefaultMap.put(KEY_TAB_REACT_ON_DATE, true);
  }

  public ProgramListSettings(final Properties properties) {
    super(properties);
  }

  public int getIndex() {
    return get(KEY_INDEX, 0);
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
  
  public void setBooleanValue(final String key, final boolean value) {
    set(key, value);
  }
  
  public boolean getBooleanValue(final String key) {
    Boolean defaultValue = mDefaultMap.get(key);
    
    if(defaultValue == null) {
      defaultValue = new Boolean(false);
    }
    
    return get(key, defaultValue.booleanValue());
  }
}
