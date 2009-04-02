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
 *   $Author$
 * $Revision$
 */
package util.settings;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class IntArrayProperty extends Property {

  private int[] mDefaultValue;
  private int[] mCachedValue;
  
  
  
  public IntArrayProperty(PropertyManager manager, String key,
    int[] defaultValue)
  {
    super(manager, key);

    mDefaultValue = defaultValue;
    mCachedValue = null;
  }


  public int[] getDefault() {
    return mDefaultValue;
  }


  public int[] getIntArray() {
    if (mCachedValue == null) {
      String asString = getProperty();
      if ((asString != null) && (asString.length() > 0)) {
        String[] splits = asString.split(",");
        try {
          int[] intArr = new int[splits.length];
          for (int i = 0; i < intArr.length; i++) {
            intArr[i] = Integer.parseInt(splits[i]);
          }
            
          // Reading succeed -> Set the mCachedValue
          mCachedValue = intArr;      
        }
        catch(NumberFormatException exc) {
          // We use the default value
          exc.printStackTrace();
        }
      } else if ((asString != null) && (asString.length() == 0)){
        mCachedValue = new int[0];
      }
  
      if (mCachedValue == null) {
        mCachedValue = mDefaultValue;
      }
    }

    return mCachedValue;
  }
  
  
  public void setIntArray(int[] value) {
    if (value == null) {
      throw new IllegalArgumentException("You can't set a null value");
    }
    
    boolean equalsDefault = false;
    if ((mDefaultValue != null) && (value.length == mDefaultValue.length)) {
      equalsDefault = true;
      for (int i = 0; i < value.length; i++) {
        if (value[i] != mDefaultValue[i]) {
          equalsDefault = false;
          break;
        }
      }
    }
    
    if (equalsDefault) {
      setProperty(null);
    } else {
      StringBuilder buffer = new StringBuilder();
    
      for (int i = 0; i < value.length; i++) {
        if (i != 0) {
          buffer.append(',');
        }
        buffer.append(value[i]);
      }
      
      setProperty(buffer.toString());
    }
    
    mCachedValue = value;
  }
  
  
  protected void clearCache() {
    mCachedValue = null;
  }

}
