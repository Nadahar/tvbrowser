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
package devplugin;

/**
 * This Class represents an Icon in the Icon-Theme.
 * 
 * You can use this Class to identify a specific Icon.
 * 
 * To load the Icon, please use Plugin.getPluginManager().getIconFromTheme(plugin, themeIcon, size)
 *
 * @since 2.2
 */
public class ThemeIcon {
  /** Category of the Icon */
  private String mCategory;
  /** Name of the Icon */
  private String mName;
  /** Size of the Icon */
  private int mSize;
  
  /**
   * Create a ThemeIcon
   * @param category Category of the Icon (action, apps, devices etc)
   * @param name Name of the Icon without Extension (go-down, mail-message-new etc)
   * @param size Size of the Icon
   */
  public ThemeIcon(String category, String name, int size) {
    mCategory = category;
    mName = name;
    mSize = size;
  }
  
  /**
   * @return Category of the Icon 
   */
  public String getCategory() {
    return mCategory;
  }

  /**
   * @return Name of the Icon
   */
  public String getName() {
    return mName;
  }

  /**
   * @return Size of the Icon
   */
  public int getSize() {
    return mSize;
  }

  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((mCategory == null) ? 0 : mCategory.hashCode());
    result = PRIME * result + ((mName == null) ? 0 : mName.hashCode());
    result = PRIME * result + mSize;
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ThemeIcon other = (ThemeIcon) obj;
    if (mCategory == null) {
      if (other.mCategory != null)
        return false;
    } else if (!mCategory.equals(other.mCategory))
      return false;
    if (mName == null) {
      if (other.mName != null)
        return false;
    } else if (!mName.equals(other.mName))
      return false;
    if (mSize != other.mSize)
      return false;
    return true;
  }
     
}