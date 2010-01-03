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
public class StringProperty extends Property {

  private static final String NULL_VALUE = "(null)";

  private String mDefaultValue;
  private String mCachedValue;

  public StringProperty(final PropertyManager manager, final String key,
      final String defaultValue) {
    super(manager, key);

    mDefaultValue = defaultValue;
    mCachedValue = null;
  }

  public String getDefault() {
    return mDefaultValue;
  }

  public String getString() {
    if (mCachedValue == null) {
      String asString = getProperty();
      if (asString == null) {
        if (mDefaultValue == null) {
          mCachedValue = NULL_VALUE;
        } else {
          mCachedValue = getDefault();
        }
      } else {
        mCachedValue = asString;
      }
    }

    if (mCachedValue.equals(NULL_VALUE)) {
      return null;
    } else {
      return mCachedValue;
    }
  }

  public void setString(String value) {
    if (value == null) {
      value = NULL_VALUE;
    }

    if ((mDefaultValue == null) ? (value.equals(NULL_VALUE)) : value
        .equals(mDefaultValue)) {
      // The default value was set
      setProperty(null);
    } else {
      setProperty(value);
    }

    mCachedValue = value;
  }

  protected void clearCache() {
    mCachedValue = null;
  }

  /**
   * Set the Default-Value of this Class
   * 
   * @param defaultValue
   *          Default Value
   * @since 2.5
   */
  public void setDefault(String defaultValue) {
    mDefaultValue = defaultValue;
  }

  /**
   * reset this option to the default value
   * 
   * @since 3.0
   */
  public void resetToDefault() {
    setString(getDefault());
  }

  public String toString() {
    return getString();
  }
}
