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
package tvbrowser.ui.settings;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

public class DirectoriesSettingsTab implements SettingsTab {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(DirectoriesSettingsTab.class);

  private util.ui.DirectoryChooserPanel mTVDataFolderPanel;

  public DirectoriesSettingsTab() {
  }

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    String msg;

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setLayout(new FormLayout("fill:150dlu:grow", "pref, 5dlu, pref"));
    mainPanel.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    mainPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("UserDefinedFolders", "User defined folders")), cc.xy(1,1));
    
    msg = mLocalizer.msg("tvdatadir", "tv data folder");
    String tvDataDir = Settings.propTVDataDirectory.getString();
    mTVDataFolderPanel = new util.ui.DirectoryChooserPanel(msg, tvDataDir);

    mainPanel.add(mTVDataFolderPanel, cc.xy(1,3));
    
    return mainPanel;
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.propTVDataDirectory.setString(mTVDataFolderPanel.getText());
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("status", "folder-open", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {

    return mLocalizer.msg("directories", "Directories");
  }

}
