/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $util.settingss $
 * $Revision$
 */
package util.settings;

import java.awt.Font;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class FontProperty extends Property {

  private Font mDefaultValue;
  private Font mCachedValue;
    
    
    
  public FontProperty(PropertyManager manager, String key,
    Font defaultValue)
  {
    super(manager, key);
  
    mDefaultValue = defaultValue;
    mCachedValue = null;
  }


  public Font getDefault() {
    return mDefaultValue;
  }


  public Font getFont() {
    if (mCachedValue == null) {
      String asString = getProperty();
  
      if (asString != null) {
        String[] splits = asString.split(",");
        if (splits.length == 3) {
          try {
            String name = splits[0];
            int style = Integer.parseInt(splits[1]);
            int size = Integer.parseInt(splits[2]);
            
            mCachedValue = new Font(name, style, size);
          }
          catch(NumberFormatException exc) {
            // We use the default value
          }
        }
      }
  
      if (mCachedValue == null) {
        mCachedValue = getDefault();
      }
    }
  
    return mCachedValue;
  }
    
    
  public void setFont(Font value) {
    if (value == null) {
      throw new IllegalArgumentException("You can't set a null value");
    }

    if (value.equals(getDefault())) {
      setProperty(null);
    } else {
      String asString = value.getName() + "," + value.getStyle() + ","
        + value.getSize();
      setProperty(asString);
    }
    
    mCachedValue = value;
  }
    
    
  protected void clearCache() {
    mCachedValue = null;
  }

}
