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
import java.awt.event.*;


import tvdataservice.TvDataService;
import tvbrowser.core.*;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class TVDataSettingsTab implements devplugin.SettingsTab, ActionListener {
  
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(TVDataSettingsTab.class);
  
  private static final String[] DELETE_MSG_ARR = new String[] {
    mLocalizer.msg("delete.2", "After 2 days"),
    mLocalizer.msg("delete.3", "After 3 days"),
    mLocalizer.msg("delete.7", "After 1 week"),
    mLocalizer.msg("delete.14", "After 2 weeks"),
    mLocalizer.msg("delete.-1", "Manually")
  };
  
  private static final String[] AUTO_DOWNLOAD_MSG_ARR = new String[] {
    mLocalizer.msg("autoDownload.never", "Never"),
    mLocalizer.msg("autoDownload.startUp", "When TV-Browser starts up"),
  };
  
  private String[] MODE_MSG_ARR = new String[] {
    mLocalizer.msg("onlineMode", "Online mode"),
    mLocalizer.msg("offlineMode", "Offline mode")
  };
  
  private JPanel mSettingsPn;
  
  private JComboBox /*mServiceCB, */mTVDataLifespanCB, mBrowseModeCB, mAutoDownloadCB;
  //private JButton mConfigBt;
  private JButton mChangeDataDirBt;
  private JButton mDeleteTVDataBt;
  private JTextField mTvDataTF;
  
  
  
  public TVDataSettingsTab() {
  }
  
  
  /*
  public void updateConfigButton() {
    TvDataService curSelectedService=(TvDataService)mServiceCB.getSelectedItem();
    boolean enabled = (curSelectedService != null)
    && curSelectedService.hasSettingsPanel();
    mConfigBt.setEnabled(enabled);
  }
  
  */
  
  public void actionPerformed(ActionEvent event) {
    Object source=event.getSource();
  /*  if (source == mServiceCB) {
      updateConfigButton();
    }*/
  /*  else if (source == mChangeDataDirBt) {
      JFileChooser fc =new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setApproveButtonText(mLocalizer.msg("ok", "OK"));
      fc.setCurrentDirectory(new File(mTvDataTF.getText()));
      int retVal=fc.showOpenDialog(mSettingsPn);
      if (retVal==JFileChooser.APPROVE_OPTION) {
        File f=fc.getSelectedFile();
        mTvDataTF.setText(f.getAbsolutePath());
      }
    }*/
   /* else if (source == mConfigBt) {
      TvDataService item = (TvDataService)mServiceCB.getSelectedItem();
      DataServiceConfigDlg dlg = new DataServiceConfigDlg(mSettingsPn, item);
      dlg.centerAndShow();
    }*/
    if (source==mDeleteTVDataBt) {
      DeleteTVDataDlg dlg = new DeleteTVDataDlg(mSettingsPn);
      dlg.centerAndShow();
    }
  }
  
  
  
  private void makeSelectionInTVDataLifespanCB(int days) {
    if (days==2) {
      mTVDataLifespanCB.setSelectedIndex(0);
    }
    else if  (days==3) {
      mTVDataLifespanCB.setSelectedIndex(1);
    }
    else if (days==7) {
      mTVDataLifespanCB.setSelectedIndex(2);
    }
    
    else if (days==14) {
      mTVDataLifespanCB.setSelectedIndex(3);
    }
    
    else {
      mTVDataLifespanCB.setSelectedIndex(4);
    }
  }
  
  
  
  private int getDaysFromTVDataLifespanCB() {
    int inx=mTVDataLifespanCB.getSelectedIndex();
    if (inx==0) {
      return 2;
    }
    else if (inx==1) {
      return 3;
    }
    else if (inx==2) {
      return 7;
    }
    else if (inx==3) {
      return 14;
    }
    return -1;
  }
  
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    String msg;
    JPanel p1;

    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    //JPanel main = new JPanel(new TabLayout(1));
    //mSettingsPn.add(main, BorderLayout.NORTH);
    
    // tv data
    
    JPanel tvDataPn=new JPanel(new GridLayout(0,2,0,7));
    
    tvDataPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("tvData", "TV data")));
    
    mSettingsPn.add(tvDataPn,BorderLayout.NORTH);
    
    msg = mLocalizer.msg("deleteTvData", "Delete TV data");    
    tvDataPn.add(new JLabel(msg));
    JPanel panel1=new JPanel(new BorderLayout());
    
    mTVDataLifespanCB=new JComboBox(DELETE_MSG_ARR);
    
    makeSelectionInTVDataLifespanCB(Settings.getTVDataLifespan());
    
    panel1.add(mTVDataLifespanCB,BorderLayout.WEST);
    tvDataPn.add(panel1);
    panel1=new JPanel(new BorderLayout());
    tvDataPn.add(new JPanel());
    msg=mLocalizer.msg("delete.now","Delete now...");
    mDeleteTVDataBt=new JButton(msg);
    mDeleteTVDataBt.addActionListener(this);
    panel1.add(mDeleteTVDataBt,BorderLayout.WEST);
    tvDataPn.add(panel1);
    
    //tvDataPn.add(panel1);
    msg = mLocalizer.msg("autoDownload", "Download automatically");
    tvDataPn.add(new JLabel(msg));
  
    mAutoDownloadCB=new JComboBox(AUTO_DOWNLOAD_MSG_ARR);
    if (Settings.getAutomaticDownload()==Settings.ONSTARTUP) {
      mAutoDownloadCB.setSelectedIndex(1);
    }
    tvDataPn.add(mAutoDownloadCB);
    
    msg = mLocalizer.msg("startIn", "Start in");
    tvDataPn.add(new JLabel(msg));
    
    mBrowseModeCB=new JComboBox(MODE_MSG_ARR);
    tvDataPn.add(mBrowseModeCB);
    
    if (Settings.getStartupInOnlineMode()) {
      mBrowseModeCB.setSelectedIndex(0);
    }else{
      mBrowseModeCB.setSelectedIndex(1);
    }
    
    
    /*
    JPanel tvDataPn = new JPanel(new TabLayout(2, true));
    main.add(tvDataPn);
    
    
    
    msg = mLocalizer.msg("deleteTvData", "Delete TV data");
    tvDataPn.add(new JLabel(msg));
    
    JPanel panel1=new JPanel(new FlowLayout());
    
    mTVDataLifespanCB=new JComboBox(DELETE_MSG_ARR);
    
    makeSelectionInTVDataLifespanCB(Settings.getTVDataLifespan());
    
    panel1.add(mTVDataLifespanCB);
    mDeleteTVDataBt=new JButton("delete...");
    mDeleteTVDataBt.addActionListener(this);
    panel1.add(mDeleteTVDataBt);
    
    tvDataPn.add(panel1);
    
   
    msg = mLocalizer.msg("autoDownload", "Download automatically");
    tvDataPn.add(new JLabel(msg));
  
    mAutoDownloadCB=new JComboBox(AUTO_DOWNLOAD_MSG_ARR);
    if (Settings.getAutomaticDownload()==Settings.ONSTARTUP) {
      mAutoDownloadCB.setSelectedIndex(1);
    }
    tvDataPn.add(mAutoDownloadCB);
    
    msg = mLocalizer.msg("startIn", "Start in");
    tvDataPn.add(new JLabel(msg));
    
    mBrowseModeCB=new JComboBox(MODE_MSG_ARR);
    tvDataPn.add(mBrowseModeCB);
    
    if (Settings.getStartupInOnlineMode()) {
      mBrowseModeCB.setSelectedIndex(0);
    }else{
      mBrowseModeCB.setSelectedIndex(1);
    }
    
    */
    
    
    // TV data service
   /* JPanel dataServicePn = new JPanel(new FlowLayout(FlowLayout.LEADING));
    msg = mLocalizer.msg("configureTvDataServices", "Configure tv data services");
    dataServicePn.setBorder(BorderFactory.createTitledBorder(msg));
    mSettingsPn.add(dataServicePn,BorderLayout.SOUTH);
    
    mServiceCB = new JComboBox(TvDataServiceManager.getInstance().getDataServices());
    mServiceCB.setRenderer(new DataServiceRenderer());
    mServiceCB.addActionListener(this);
    dataServicePn.add(mServiceCB);
    dataServicePn.add(new JLabel(" "));
    mConfigBt = new JButton(mLocalizer.msg("configure", "Configure..."));
    mConfigBt.addActionListener(this);
    dataServicePn.add(mConfigBt);
    
    updateConfigButton();
    */
    return mSettingsPn;
  }
  
  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
 //   Settings.setTVDataDirectory(mTvDataTF.getText());
    
    Settings.setTVDataLifespan(getDaysFromTVDataLifespanCB());
    
    Settings.setStartupInOnlineMode(mBrowseModeCB.getSelectedIndex()==0);
    
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
  
  
  // inner class DataServiceRenderer
  
  
  class DataServiceRenderer extends DefaultListCellRenderer {
    
    public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus) {
      if (value instanceof TvDataService) {
        TvDataService dataService = (TvDataService) value;
        value = dataService.getName();
      }
      
      return super.getListCellRendererComponent(list, value, index, isSelected,
      cellHasFocus);
    }
    
  }
  
}
