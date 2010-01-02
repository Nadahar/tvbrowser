/*
 * DVBPluginSettingsTab.java
 * Copyright (C) 2006 Probum
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
 *     $Date: $
 *   $Author: $
 * $Revision: $
 */

package dvbplugin;

import javax.swing.Icon;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.Sizes;

import util.ui.ImageUtilities;

/**
 * Represents a page in the settings dialog.
 *
 * @author Probum
 */
public class DVBPluginSettingsTab implements devplugin.SettingsTab {

  private boolean isWindows;
  private SettingsPanel set;


  /**
   * Creates a new instance of DVBPluginSettingsTab
   *
   * @param isWindows are we running on windows?
   */
  public DVBPluginSettingsTab(boolean isWindows) {
    this.isWindows = isWindows;
  }


  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    set = new SettingsPanel(isWindows);
    set.setBorder(Borders.createEmptyBorder(Sizes.DLUY5,Sizes.DLUX5,Sizes.DLUY5,Sizes.DLUX5));
    return set;
  }


  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    String iconName = DVBPlugin.DVBPLUGIN_SMALLICON;
    return ImageUtilities.createImageIconFromJar(iconName, getClass());
  }


  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return "DVBViewerPlugin";
  }


  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
  // mSettings = set.getSet();
  }
}
