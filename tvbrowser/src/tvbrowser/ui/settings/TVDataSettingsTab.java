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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import tvbrowser.core.*;
import tvbrowser.ui.mainframe.UpdateDlg;

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
  //  mLocalizer.msg("autoDownload.never", "Never"),
  //  mLocalizer.msg("autoDownload.startUp", "When TV-Browser starts up"),
    mLocalizer.msg("autoDownload.daily", "Once a day"),
    mLocalizer.msg("autoDownload.every3days","Every three days"),
    mLocalizer.msg("autoDownload.weekly","Weekly")
    
  };
  
  private JPanel mSettingsPn;
  
  private JComboBox mAutoDownloadCB;
  private JButton mChangeDataDirBt;
  private JButton mDeleteTVDataBt;
  private JTextField mTvDataTF;
  private JCheckBox mAutoDownloadCb;
  private JComboBox mAutoDownloadPeriodCB;
  private JRadioButton mDonotAskBeforeDownloadRB;
  private JRadioButton mAskBeforeDownloadRB;
  
  
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
       
    JPanel onStartupPn=new JPanel();
    onStartupPn.setLayout(new BoxLayout(onStartupPn,BoxLayout.Y_AXIS));   
    onStartupPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("autoDownload", "Download automatically")));
    
    JPanel autoDownloadPn=new JPanel(new GridLayout(0,2,0,7));
    
    
    onStartupPn.add(autoDownloadPn);
    
    mSettingsPn.add(onStartupPn,BorderLayout.NORTH);
    
    mAutoDownloadCb = new JCheckBox(mLocalizer.msg("onStartUp", "On startup"));
    autoDownloadPn.add(mAutoDownloadCb);
  
    mAutoDownloadCB=new JComboBox(AUTO_DOWNLOAD_MSG_ARR);
    if (Settings.getAutomaticDownload()==Settings.DAILY) {
      mAutoDownloadCB.setSelectedIndex(0);
    }
    else if (Settings.getAutomaticDownload()==Settings.EVERY3DAYS) {
      mAutoDownloadCB.setSelectedIndex(1);
    }
    else if (Settings.getAutomaticDownload()==Settings.WEEKLY) {
      mAutoDownloadCB.setSelectedIndex(2);
    }
          
    mAutoDownloadCb.setSelected(Settings.getAutomaticDownload()!=Settings.NEVER);      
    
   
    autoDownloadPn.add(mAutoDownloadCB);
    
    JPanel askBeforeDLPanel=new JPanel();
    askBeforeDLPanel.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
    

    askBeforeDLPanel.setLayout(new BoxLayout(askBeforeDLPanel,BoxLayout.Y_AXIS));
        
    mAskBeforeDownloadRB=new JRadioButton(mLocalizer.msg("autoDownload.ask","Ask before downloading"));
    mDonotAskBeforeDownloadRB=new JRadioButton(mLocalizer.msg("autoDownload.dontask","Don't ask, download for"));
    
    ButtonGroup buttonGroup=new ButtonGroup();
    buttonGroup.add(mAskBeforeDownloadRB);
    buttonGroup.add(mDonotAskBeforeDownloadRB);
    
    if (Settings.getAskForAutoDownload()) {
      mAskBeforeDownloadRB.setSelected(true);
    }
    else {
      mDonotAskBeforeDownloadRB.setSelected(true);
    }
    
    JPanel pn1=new JPanel(new BorderLayout());
    JPanel pn2=new JPanel(new BorderLayout());
    JPanel pn3=new JPanel(new BorderLayout());
    
    pn1.add(mAskBeforeDownloadRB,BorderLayout.WEST);
    
    pn2.add(mDonotAskBeforeDownloadRB,BorderLayout.WEST);
    pn2.add(pn3,BorderLayout.CENTER);
    mAutoDownloadPeriodCB=new JComboBox(UpdateDlg.PERIOD_MSG_ARR);
    pn3.add(mAutoDownloadPeriodCB,BorderLayout.WEST);
    
    int autoDLPeriod=Settings.getAutoDownloadPeriod();
    if (autoDLPeriod==UpdateDlg.GETALL) {
      mAutoDownloadPeriodCB.setSelectedIndex(mAutoDownloadPeriodCB.getItemCount()-1);
    }
    else {
      mAutoDownloadPeriodCB.setSelectedIndex(autoDLPeriod);
    }
    
    askBeforeDLPanel.add(pn1);
    askBeforeDLPanel.add(pn2);
    
    onStartupPn.add(askBeforeDLPanel);
    
    
    mAutoDownloadCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        setAutoDownloadEnabled(mAutoDownloadCb.isSelected());
      }
    });
    
    setAutoDownloadEnabled(mAutoDownloadCb.isSelected());
    
    return mSettingsPn;
  }
  
  
  public void setAutoDownloadEnabled(boolean enabled) {
    mAskBeforeDownloadRB.setEnabled(enabled);
    mDonotAskBeforeDownloadRB.setEnabled(enabled);
    mAutoDownloadCB.setEnabled(enabled);
    mAutoDownloadPeriodCB.setEnabled(enabled);
  }
  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
      
    int inx = mAutoDownloadCB.getSelectedIndex();
    
    if (!mAutoDownloadCb.isEnabled()) {
      Settings.setAutomaticDownload(Settings.NEVER);
    }
    else if (inx == 0) {
      Settings.setAutomaticDownload(Settings.DAILY);
    }
    else if (inx == 1) {
      Settings.setAutomaticDownload(Settings.EVERY3DAYS);
    }
    else if (inx == 2) {
      Settings.setAutomaticDownload(Settings.WEEKLY);
    }
    
    Settings.setAskForAutoDownload(mAskBeforeDownloadRB.isSelected());
    
    inx=mAutoDownloadPeriodCB.getSelectedIndex();
    if (inx==mAutoDownloadPeriodCB.getItemCount()-1) {
      Settings.setDownloadPeriod(UpdateDlg.GETALL);
    }
    else {
      Settings.setDownloadPeriod(inx);
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
