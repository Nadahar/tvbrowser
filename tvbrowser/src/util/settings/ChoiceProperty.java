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

import java.util.HashSet;
import java.util.logging.Logger;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ChoiceProperty extends Property {

  private static final Logger mLog
    = Logger.getLogger(ChoiceProperty.class.getName());

  private HashSet<String> mAllowedValueSet;
  private String mDefaultValue;
  private String mCachedValue;
  
  
  
  public ChoiceProperty(PropertyManager manager, String key,
    String defaultValue, String[] allowedValueArr)
  {
    super(manager, key);

    mDefaultValue = defaultValue;
    mCachedValue = null;
    
    mAllowedValueSet = new HashSet<String>(allowedValueArr.length);
    for (int i = 0; i < allowedValueArr.length; i++) {
      mAllowedValueSet.add(allowedValueArr[i]);
    }
  }
  
  
  public String getDefault() {
    return mDefaultValue;
  }
  
  
  public String getString() {
    if (mCachedValue == null) {
      String asString = getProperty();
      if (asString == null) {
        mCachedValue = mDefaultValue;
      } else {
        // Check whether the value is allowed
        if (isAllowed(asString)) {
          mCachedValue = asString;
        } else {
          // The value is not allowed -> Use the default
          mLog.info("The setting '" + getKey() + "' is set to an illegal value ("
            + asString + "). Using the default instead (" + mDefaultValue + ")...");
          mCachedValue = mDefaultValue;
        }
      }
    }

    return mCachedValue;
  }
  
  
  public void setString(String value) {
    if (value == null) {
      throw new IllegalArgumentException("You can't set a null value");
    }
    
    // Check whether the value is allowed
    if (! isAllowed(value)) {
      throw new IllegalArgumentException("The value '" + value + "' is not allowed");
    }
    
    if (value.equals(mDefaultValue)) {
      setProperty(null);
    } else {
      setProperty(value);
    }
    
    mCachedValue = value;
  }
  
  
  protected void clearCache() {
    mCachedValue = null;
  }


  public boolean isAllowed(String value) {
    return mAllowedValueSet.contains(value);
  }

}
