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
import javax.swing.*;
import tvbrowser.core.Settings;
import devplugin.SettingsTab;

public class DirectoriesSettingsTab implements SettingsTab {
  
  /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(DirectoriesSettingsTab.class);
 

  private util.ui.DirectoryChooserPanel mTVDataFolderPanel; 
    
  public DirectoriesSettingsTab() {
    
  }
    /**
     * Creates the settings panel for this tab.
     */
  public JPanel createSettingsPanel() {
    String msg;
    
    JPanel mainPanel=new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    JPanel content=new JPanel();
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
    
    JPanel checkBoxPanel=new JPanel(new BorderLayout());
    checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(0,0,3,0));

    content.add(checkBoxPanel);
    
    final tvbrowser.ui.settings.DirectoryChooser directoriesPanel=new tvbrowser.ui.settings.DirectoryChooser();

    msg = mLocalizer.msg("tvdatadir", "tv data folder");
    String tvDataDir = Settings.propTVDataDirectory.getString();    
    mTVDataFolderPanel=new util.ui.DirectoryChooserPanel(msg, tvDataDir);
    directoriesPanel.addDirectoryChooserPanel(mTVDataFolderPanel);

    content.add(directoriesPanel);
    mainPanel.add(content,BorderLayout.NORTH);
    
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
    return new ImageIcon("imgs/Open16.gif");
  }
  
  
    /**
     * Returns the title of the tab-sheet.
     */
  public String getTitle() {

  return mLocalizer.msg("directories", "Directories");
 }

  




}
  
