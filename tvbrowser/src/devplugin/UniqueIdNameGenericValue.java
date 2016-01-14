/*
* TV-Browser
* Copyright (C) 2016 TV-Browser team (dev@tvbrowser.org)
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
package devplugin;

/**
 * A class with unique ID a name and a generic value to store.
 * <p>
 * @author René Mach
 * @since 3.4.4
 *
 * @param <T> The type of the stored data value.
 */
public class UniqueIdNameGenericValue<T> {
  private String mUniqueId;
  private String mName;
  private T mValue;
  
  public UniqueIdNameGenericValue(String uniqueId, String name, T value) throws NullPointerException {
    if(uniqueId == null || value == null) {
      throw new NullPointerException("uniqueId and value must not be null");
    }
    
    mUniqueId = uniqueId;
    mName = name;
    mValue = value;
  }
  
  public String getUniqueId() {
    return mUniqueId;
  }
  
  public String getName() {
    return mName;
  }
  
  public T getValue() {
    return mValue;
  }
  
  public void setValue(T value) {
    mValue = value;
  }
  
  @Override
  public String toString() {
    return mUniqueId+(mName != null ? ":"+mName : "");
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof UniqueIdNameGenericValue) {
      return mUniqueId.equals(((UniqueIdNameGenericValue<?>)obj).mUniqueId) && mValue.getClass().equals(((UniqueIdNameGenericValue<?>)obj).getValue().getClass());
    }
    return super.equals(obj);
  }
}
