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

import javax.swing.*;
import java.awt.*;

import tvbrowser.core.*;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class TVDataSettingsTab implements devplugin.SettingsTab {
  
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(TVDataSettingsTab.class);
  
  private static final String[] AUTO_DOWNLOAD_MSG_ARR = new String[] {
    mLocalizer.msg("autoDownload.never", "Never"),
    mLocalizer.msg("autoDownload.startUp", "When TV-Browser starts up"),
  };
  
  private JPanel mSettingsPn;
  
  private JComboBox mAutoDownloadCB;
  private JButton mChangeDataDirBt;
  private JButton mDeleteTVDataBt;
  private JTextField mTvDataTF;
  
  
  
  public TVDataSettingsTab() {
  }
  
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    String msg;
    JPanel p1;

    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
       
    
    JPanel tvDataPn=new JPanel(new GridLayout(0,2,0,7));
    
    tvDataPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("tvData", "TV data")));
    
    mSettingsPn.add(tvDataPn,BorderLayout.NORTH);
    

    //tvDataPn.add(panel1);
    msg = mLocalizer.msg("autoDownload", "Download automatically");
    tvDataPn.add(new JLabel(msg));
  
    mAutoDownloadCB=new JComboBox(AUTO_DOWNLOAD_MSG_ARR);
    if (Settings.getAutomaticDownload()==Settings.ONSTARTUP) {
      mAutoDownloadCB.setSelectedIndex(1);
    }
    tvDataPn.add(mAutoDownloadCB);
    
    return mSettingsPn;
  }
  
  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
      
    int inx = mAutoDownloadCB.getSelectedIndex();
    if (inx == 0) {
      Settings.setAutomaticDownload("never");
    } else {
      Settings.setAutomaticDownload("startup");
    }
  }
  
  
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }
  
  
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("tvData", "TV data");
  }
  
}
