/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mrz 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package tvbrowser.core.plugin;

import devplugin.ActionMenu;


/**
 * Is an interface for showing support of button
 * action for toolbar of internal plugin.
 * 
 * @author René Mach
 * @since 2.7
 */
public interface ButtonActionIf {
  
  /**
   * Gets the action menu with the action supported for toolbar actions.
   * @return The action menu with the supported toolbar actions
   */
  public ActionMenu getButtonAction();
  
  /**
   * Gets the id of this ButtonActionIf.
   * @return The id of this ButtonActionIf.
   */
  public String getId();
  
  /**
   * Gets the description for this ButtonActionIf.
   * @return The description for this ButtonActionIf.
   */
  public String getButtonActionDescription();
}
