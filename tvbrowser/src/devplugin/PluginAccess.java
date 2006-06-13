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

import javax.swing.Icon;


/**
 * An access to another plugin.
 * <p>
 * Contains all operations that may be called on a plugin from another plugin.
 *
 * @author Til Schneider, www.murfman.de
 */
public interface PluginAccess extends Marker,ProgramReceiveIf,ContextMenuIf {
  
  /**
   * Gets the ID of this plugin.
   * 
   * @return The ID of this plugin.
   */
  public String getId();
  
  /**
   * Gets the meta information about the plugin.
   * 
   * @return The meta information about the plugin.
   */
  public PluginInfo getInfo();

  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @return The description text for the program table icons or
   *         <code>null</code> if the plugin does not provide this feature.
   *
   * @see #getProgramTableIcons(Program)
   */
  public String getProgramTableIconText();

  /**
   * Gets the icons this Plugin provides for the given program. These icons will
   * be shown in the program table under the start time.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @param program The programs to get the icons for.
   * @return The icons for the given program or <code>null</code>.
   *
   * @see #getProgramTableIconText()
   */
  public Icon[] getProgramTableIcons(Program program);


  /**
   * Gets the icon to use for marking programs in the program table. Should be
   * 16x16.
   * 
   * @return the icon to use for marking programs in the program table.
   */
  public Icon getMarkIcon();

  /**
   * Gets the icons to use for marking programs in the program table. Should be
   * 16x16.
   * 
   * @return the icons to use for marking programs in the program table.
   * @since 2.3
   */
  public Icon[] getMarkIcons(Program p);
  
  public boolean canUseProgramTree();

  public PluginTreeNode getRootNode();
}
