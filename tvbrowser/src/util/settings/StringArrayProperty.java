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
public class StringArrayProperty  extends Property {

  private String[] mDefaultValue;
  private String[] mCachedValue;
  
  
  
  public StringArrayProperty(PropertyManager manager, String key,
    String[] defaultValue)
  {
    super(manager, key);

    mDefaultValue = defaultValue;
    mCachedValue = null;
  }


  public String[] getDefault() {
    return mDefaultValue;
  }


  public boolean containsItem(String str) {
    return indexOfItem(str) != -1;
  }
  
  
  public void removeItem(String str) {
    String[] arr = getStringArray();
    if (arr != null) {
      int index = indexOfItem(str);
      if (index != -1) {
        String[] newArr = new String[arr.length - 1];
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(arr, index + 1, newArr, index, arr.length - index - 1);
        
        setStringArray(newArr);
      }
    }
  }
  
  
  public void addItem(String str) {
    String[] arr = getStringArray();

    String[] newArr;
    if (arr == null) {
      newArr = new String[] { str };
    } else {
      newArr = new String[arr.length + 1];
      System.arraycopy(arr, 0, newArr, 0, arr.length);
      newArr[newArr.length - 1] = str;
      
      setStringArray(newArr);
    }
  }
  
  
  private int indexOfItem(String str) {
    String[] arr = getStringArray();
    if (arr != null) {
      for (int i = 0; i < arr.length; i++) {
        if ((arr[i] == null) ? (arr[i] == str) : arr[i].equals(str)) {
          return i;
        }
      }
    }
    
    return -1;
  }
  
  
  public String[] getStringArray() {
    if (mCachedValue == null) {
      String asString = getProperty();
      if (asString == null) {
        mCachedValue = mDefaultValue;
      } else {
        mCachedValue = asString.split(",");
      }
    }

    return mCachedValue;
  }
  
  
  public void setStringArray(String[] value) {
    if (value == null) {
      throw new IllegalArgumentException("You can't set a null value");
    }
    
    boolean equalsDefault = false;
    if ((mDefaultValue != null) && (value.length == mDefaultValue.length)) {
      equalsDefault = true;
      for (int i = 0; i < value.length; i++) {
        if (! value[i].equals(mDefaultValue[i])) {
          equalsDefault = false;
          break;
        }
      }
    }
    
    if (equalsDefault) {
      setProperty(null);
    } else {
      StringBuffer buffer = new StringBuffer();
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
