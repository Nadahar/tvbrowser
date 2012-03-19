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
public class IntProperty extends Property {
  
  private int mDefaultValue;
  protected boolean mIsCacheFilled;
  private int mCachedValue;
  
  
  
  public IntProperty(PropertyManager manager, String key,
    int defaultValue)
  {
    super(manager, key);
    
    mDefaultValue = defaultValue;
    mIsCacheFilled = false;
  }


  public int getDefault() {
    return mDefaultValue;
  }


  public int getInt() {
    if (! mIsCacheFilled) {
      String asString = getProperty();
      if (asString == null) {
        mCachedValue = getDefault();
      } else {
        try {
          mCachedValue = Integer.parseInt(asString);
        }
        catch (Exception exc) {
          mCachedValue = getDefault();
        }
      }

      mIsCacheFilled = true;
    }

    return mCachedValue;
  }
  
  
  public void setInt(int value) {
    if (value == mDefaultValue) {
      setProperty(null);
    } else {
      setProperty(Integer.toString(value));
    }
    
    mCachedValue = value;
  }
  
  
  protected void clearCache() {
    mIsCacheFilled = false;
  }

}
