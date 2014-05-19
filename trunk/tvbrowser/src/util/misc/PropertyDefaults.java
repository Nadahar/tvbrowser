/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * SVN information:
 *     $Date: 2010-03-07 07:27:49 +0100 (So, 07 Mrz 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6542 $
 */
package util.misc;

import java.util.HashMap;
import java.util.Properties;

/**
 * A class that stores a HashMap with default values for Properties
 * 
 * @author René Mach
 * @since 3.3.4
 */
public class PropertyDefaults {
  private HashMap<String, String> mDefaultValueMap;
  
  public PropertyDefaults(HashMap<String, String> defaultValueMap) {
    mDefaultValueMap = defaultValueMap;
  }
  
  /**
   * Gets the value for the given key from the given properties
   * or the default value for the key of this property defaults.
   * 
   * @param key The key of the property value to get.
   * @param properties The properties to get the value from
   * 
   * @return A value or <code>null</code> if no value exists for the given key.
   */
  public String getValueFromProperties(String key, Properties properties) {
    if(key != null && properties != null) {
      return properties.getProperty(key, mDefaultValueMap.get(key));
    }
    else if(key != null) {
      return mDefaultValueMap.get(key);
    }
    
    return null;
  }
  
  /**
   * Gets the default value of this property defaults for the given key.
   * 
   * @param key The key of the property value default to get.
   * 
   * @return The default value or <code>null</code> if no default value exists for the given key.
   */
  public String getDefaultValueForKey(String key) {
    if(key != null) {
      return mDefaultValueMap.get(key);
    }
    
    return null;
  }
}
