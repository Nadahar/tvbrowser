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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.settings;

/**
 * A property for byte values.
 * 
 * @author René Mach
 */
public class ByteProperty extends Property {
  
  private byte mDefaultValue;
  private boolean mIsCacheFilled;
  private byte mCachedValue;
  
  /**
   * Creates an instance of this propety.
   * <p>
   * @param manager The property manager to use.
   * @param key The key of this property.
   * @param defaultValue The default value for this property.
   */
  public ByteProperty(PropertyManager manager, String key,
    byte defaultValue)
  {
    super(manager, key);
    
    mDefaultValue = defaultValue;
    mIsCacheFilled = false;
  }

  /**
   * Gets the default value of this property.
   * <p>
   * @return The default value of this property.
   */
  public byte getDefault() {
    return mDefaultValue;
  }
  
  /**
   * Gets the current value of this property.
   * <p>
   * @return The current value of this property.
   */
  public byte getByte() {
    if (! mIsCacheFilled) {
      String asString = getProperty();
      if (asString == null) {
        mCachedValue = mDefaultValue;
      } else {
        try {
          mCachedValue = Byte.parseByte(asString);
        }
        catch (Exception exc) {
          mCachedValue = mDefaultValue;
        }
      }

      mIsCacheFilled = true;
    }

    return mCachedValue;
  }
  
  /**
   * Sets the value of this property.
   * <p>
   * @param value The new value for this property.
   */
  public void setByte(byte value) {
    if (value == mDefaultValue) {
      setProperty(null);
    } else {
      setProperty(Byte.toString(value));
    }
    
    mCachedValue = value;
  }
  
  
  protected void clearCache() {
    mIsCacheFilled = false;
  }
}
