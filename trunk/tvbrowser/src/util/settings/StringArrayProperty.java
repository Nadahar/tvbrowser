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

import java.util.ArrayList;

/**
 * A String-Array
 * 
 * @author Til Schneider, www.murfman.de
 */
public class StringArrayProperty  extends Property {

  /** The Default-Value */
  private String[] mDefaultValue;
  /** The Cached-Value */
  private String[] mCachedValue;
  
  
  /**
   * Creates the StringArray
   * @param manager Manager
   * @param key Key for this Property
   * @param defaultValue the Default-Value
   */
  public StringArrayProperty(PropertyManager manager, String key,
    String[] defaultValue)
  {
    super(manager, key);

    mDefaultValue = defaultValue;
    mCachedValue = null;
  }

  /**
   * The Default-Value
   * @return default-Value
   */
  public String[] getDefault() {
    return mDefaultValue;
  }

  /**
   * Tests if this Array contains a specific String
   * @param str String to test
   * @return true if String is in this Array
   */
  public boolean containsItem(String str) {
    return indexOfItem(str) != -1;
  }
  
  /**
   * Remove a String
   * @param str String to remove
   */
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
  
  /**
   * Add a String to the Array
   * @param str String to add
   */
  public void addItem(String str) {
    String[] arr = getStringArray();

    String[] newArr;
    if (arr == null) {
      newArr = new String[] { str };
      setStringArray(newArr);
    } else {
      newArr = new String[arr.length + 1];
      System.arraycopy(arr, 0, newArr, 0, arr.length);
      newArr[newArr.length - 1] = str;
      
      setStringArray(newArr);
    }
  }
  
  /**
   * The Index of a Specific String
   * @param str Get the Index for this String
   * @return Index of the String
   */
  private int indexOfItem(String str) {
    String[] arr = getStringArray();
    if (arr != null) {
      for (int i = 0; i < arr.length; i++) {
        if ((arr[i] == null) ? (str == null) : arr[i].equals(str)) {
          return i;
        }
      }
    }
    
    return -1;
  }
  
  /**
   * Get the StringArray
   * @return StringArray
   */
  public String[] getStringArray() {
    if (mCachedValue == null) {
      String asString = getProperty();
      if (asString == null) {
        mCachedValue = mDefaultValue;
      } else {
        mCachedValue = splitStrings(asString);
      }
    }

    return mCachedValue;
  }
  
  /**
   * Set the StringArray
   * @param value new StringArray
   */
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
      StringBuilder buffer = new StringBuilder();
      for (int i = 0; i < value.length; i++) {
        if (i != 0) {
          buffer.append(',');
        }
        buffer.append(addSlashes(value[i]));
      }
      setProperty(buffer.toString());
    }
    
    mCachedValue = value;
  }
  
  /**
   * Clear the Cache
   */
  protected void clearCache() {
    mCachedValue = null;
  }

  /**
   * Splits the String into pieces
   * @param string String to split
   * @return String-Array
   */
  private String[] splitStrings(String string) {
    String[] splitted = string.split(","); 
    
    ArrayList<String> list = new ArrayList<String>();
    
    StringBuilder current = new StringBuilder();
    
    for (int i= 0;i<splitted.length;i++) {
      if (splitted[i].endsWith("\\") && (countEndSlashes(splitted[i]) % 2 == 1)) {
        current.append(splitted[i].substring(0, splitted[i].length()-1));
        current.append(',');
      } else {
        current.append(splitted[i]);
        list.add(current.toString().replaceAll("\\\\\\\\", "\\\\"));
        current = new StringBuilder();
      }
    }
    
    return list.toArray(new String[list.size()]);
  }
  
  /**
   * Counts the amount of Slashes at the End of a String
   * @param str String
   * @return Number of Slashes
   */
  private int countEndSlashes(String str) {
    int pos = str.length()-1;
    int count = 0;
    while ((pos > 0) && (str.charAt(pos) == '\\')) {
      count++;
      pos--;
    }
    
    return count;
  }
  
  /**
   * Returns the String
   */
  public String toString() {
   return getProperty();
  }
  
  /**
   * Adds Slashes to \ and ,
   * @param string 
   * @return String with Slashes
   */
  private String addSlashes(String string) {
    string = string.replaceAll("\\\\", "\\\\\\\\");
    string = string.replaceAll(",", "\\\\,");
    return string;
  }
}
