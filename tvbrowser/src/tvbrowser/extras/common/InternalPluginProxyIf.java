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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.common;

import javax.swing.Icon;

import devplugin.SettingsTab;


/**
 * Have to be implemented by all internal
 * plugins to support common methods.
 * 
 * @author René Mach
 * @since 2.6
 */
public interface InternalPluginProxyIf {
  /** This is the key for the keyboard accelerator for
   * internal plugin button actions.
   * @since 2.7
   */
  public static final String KEYBOARD_ACCELERATOR = "####KEYBOARD_ACCELERATOR####";
  
  /** Gets the icon of this internal plugin.
   * @return The icon for this internal plugin. */
  public Icon getIcon();
  
  /** Gets the name of this internal plugin.
   * @return The name of this internal plugin. */
  public String getName();
  
  /** Gets the description of this internal plugin.
   * @return The description of this internal plugin. */
  public String getButtonActionDescription();
  
  /** Gets the id of this internal plugin.
   * @return The id of this internal plugin. */
  public String getId();
  
  /** Gets the settings tab of this internal plugin.
   * @return The settings tab of this internal plugin. */
  public SettingsTab getSettingsTab();
  
  /** Gets the settings id of this internal plugin.
   * @return The settings id of this internal plugin. */
  public String getSettingsId();
  
  /**
   * comparator for internal plugin proxies
   * @author bananeweizen
   * @since 2.7
   */
  public static class Comparator implements java.util.Comparator<InternalPluginProxyIf> {
    public int compare(InternalPluginProxyIf o1, InternalPluginProxyIf o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }

  /**
   * @since 3.0
   */
  public void handleTvDataUpdateFinished();
}