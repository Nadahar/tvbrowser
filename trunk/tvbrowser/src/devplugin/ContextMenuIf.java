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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package devplugin;

/**
 * A interface for a object that supports return an ActionMenu
 * for a program.
 * 
 * @author René Mach
 *
 */
public interface ContextMenuIf {
  
  /**
   * Gets the actions for the context menu of a program.
   * 
   * @param program The program the context menu will be shown for.
   * @return the actions this plugin provides for the given program or
   *         <code>null</code> if the plugin does not provide this feature.
   */
  public ActionMenu getContextMenuActions(Program program);
  
  /**
   * Gets the ID of this ContextMenuIf.
   * 
   * @return The ID of this ContextMenuIf.
   */
  public String getId();
  
  /**
   * @since 3.0
   */
  public static String ACTIONKEY_KEYBOARD_EVENT = "ACTIONKEY_KEY_EVENT";
  
}
