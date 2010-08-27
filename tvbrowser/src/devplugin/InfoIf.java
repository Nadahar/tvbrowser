/*
* TV-Browser
* Copyright (C) 2003-2010 TV-Browser-Team (dev@tvbrowser.org)
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
package devplugin;
/**
 * An interface for all classes that supports the getId()
 * and getInfo() methods.
 * <p>
 * @author René Mach
 * @since 3.0
 */
public interface InfoIf {
  /**
   * Gets the PluginInfo.
   * <p>
   * @return The plugin info.
   */
  public PluginInfo getInfo();
  
  /**
   * Gets the Id.
   * <p>
   * @return The Id.
   */
  public String getId();
}
