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

import devplugin.Version;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class VersionProperty extends Property {

  private Version mDefaultValue;
  private Version mCachedValue;
    
    
    
  public VersionProperty(PropertyManager manager, String key,
    Version defaultValue)
  {
    super(manager, key);
  
    mDefaultValue = defaultValue;
    mCachedValue = null;
  }
  
  
  public Version getDefault() {
    return mDefaultValue;
  }


  public Version getVersion() {
    if (mCachedValue == null) {
      String asString = getProperty();
  
      if (asString != null) {
        try {
          int asInt = Integer.parseInt(asString);
          int major = asInt / 10000;
          int minor = asInt % 10000 / 100;
          int subMinor = asInt % 100;
          
          if(asString.length() == 3) {
            major = asInt / 100;
            minor = asInt % 100;            
            subMinor = 0;
          }
     
          mCachedValue = new devplugin.Version(major,minor,subMinor);
        }
        catch(NumberFormatException exc) {
          // We use the default value
        }
      }
  
      if (mCachedValue == null) {
        mCachedValue = mDefaultValue;
      }
    }
  
    return mCachedValue;
  }
    
    
  public void setVersion(Version value) {
    if (value == null) {
      throw new IllegalArgumentException("You can't set a null value");
    }

    if (value.equals(mDefaultValue)) {
      setProperty(null);
    } else {
      int asInt = value.getMajor() * 10000 + value.getMinor() * 100 + value.getSubMinor();
      setProperty(Integer.toString(asInt));
    }
    
    mCachedValue = value;
  }
    
    
  protected void clearCache() {
    mCachedValue = null;
  }

}
