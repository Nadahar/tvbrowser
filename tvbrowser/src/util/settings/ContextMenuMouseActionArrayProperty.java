/*
 * TV-Browser
 * Copyright (C) 2013 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.settings;

import org.apache.commons.lang3.StringUtils;

/**
 * An array property that can store ContextMenuMouseActionSetting.
 * 
 * @author René Mach
 * @since 3.3.1
 */
public class ContextMenuMouseActionArrayProperty extends Property {
  private ContextMenuMouseActionSetting[] mDefaultValue;
  private ContextMenuMouseActionSetting[] mCachedValue;
  
  public ContextMenuMouseActionArrayProperty(PropertyManager manager, String key, ContextMenuMouseActionSetting[] defaultSetting) {
    super(manager, key);
    
    mDefaultValue = defaultSetting;
    mCachedValue = null;
  }

  public ContextMenuMouseActionSetting[] getDefault() {
    return mDefaultValue;
  }
  
  public ContextMenuMouseActionSetting[] getContextMenuMouseActionArray() {
    if (mCachedValue == null) {
      String asString = getProperty();
      if ((asString != null) && (asString.length() > 0)) {
        String[] splits = asString.split(",");
        try {
          ContextMenuMouseActionSetting[] menuSettingsArr = new ContextMenuMouseActionSetting[splits.length];
          for (int i = 0; i < menuSettingsArr.length; i++) {
            menuSettingsArr[i] = new ContextMenuMouseActionSetting(splits[i]);
          }

          // Reading succeed -> Set the mCachedValue
          mCachedValue = menuSettingsArr;
        }
        catch(NumberFormatException exc) {
          // We use the default value
          exc.printStackTrace();
        }
      } else if ((asString != null) && (StringUtils.isEmpty(asString))){
        mCachedValue = new ContextMenuMouseActionSetting[0];
      }

      if (mCachedValue == null) {
        mCachedValue = mDefaultValue;
      }
    }
    
    return mCachedValue;
  }  
  
  public void setContextMenuMouseActionArray(ContextMenuMouseActionSetting[] array) {
    if (array == null) {
      throw new IllegalArgumentException("You can't set a null value");
    }
    
    boolean equalsDefault = false;
    if ((mDefaultValue != null) && (array.length == mDefaultValue.length)) {
      equalsDefault = true;
      for (int i = 0; i < array.length; i++) {
        if (array[i] != mDefaultValue[i]) {
          equalsDefault = false;
          break;
        }
      }
    }

    if (equalsDefault) {
      setProperty(null);
    } else {
      StringBuilder buffer = new StringBuilder();

      for (int i = 0; i < array.length; i++) {
        if (i != 0) {
          buffer.append(',');
        }
        buffer.append(array[i].toString());
      }

      setProperty(buffer.toString());
    }

    mCachedValue = array;
  }
  
  @Override
  protected void clearCache() {
    mCachedValue = null;
  }
}
