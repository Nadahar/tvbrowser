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
import java.io.File;

import util.ui.*;

import tvdataloader.TVDataServiceInterface;
import tvbrowser.core.*;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class DataServiceSettingsTab extends devplugin.SettingsTab implements ActionListener {
  
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(DataServiceSettingsTab.class);
 
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
    mLocalizer.msg("autoDownload.30", "Every 30 minutes"),
    mLocalizer.msg("autoDownload.60", "Every 60 minutes")
  };
  
  private String[] MODE_MSG_ARR = new String[] {
    mLocalizer.msg("onlineMode", "Online mode"),
    mLocalizer.msg("offlineMode", "Offline mode")
  };
  
  
  private JComboBox mServiceCB;
  private JButton mConfigBt;
  private JButton mChangeDataDirBt;
  private JTextField mTvDataTF;
  
  
  
  public DataServiceSettingsTab() {
    setLayout(new FlowLayout(FlowLayout.LEADING));
    
    String msg;
    JPanel p1;
    
    JPanel main = new JPanel(new TabLayout(1));
    add(main);
    
    // tv data
    JPanel tvDataPn = new JPanel(new TabLayout(2, true));
    msg = mLocalizer.msg("tvData", "TV data");
    tvDataPn.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(tvDataPn);
    
    msg = mLocalizer.msg("deleteTvData", "Detele TV data");
    tvDataPn.add(new JLabel(msg));
    tvDataPn.add(new JComboBox(DELETE_MSG_ARR));

    msg = mLocalizer.msg("tvDataFolder", "TV data folder");
    tvDataPn.add(new JLabel(msg));
    p1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    tvDataPn.add(p1);
    mTvDataTF = new JTextField(Settings.getTVDataDirectory(), 30);
    p1.add(mTvDataTF);
    p1.add(new JLabel(" "));
    mChangeDataDirBt = new JButton("...");
    mChangeDataDirBt.addActionListener(this);
    p1.add(mChangeDataDirBt);
    
    // browser mode
    JPanel browseModePn = new JPanel(new TabLayout(2, true));
    msg = mLocalizer.msg("tvData", "TV data");
    browseModePn.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(browseModePn);

    msg = mLocalizer.msg("autoDownload", "Download automatically");
    browseModePn.add(new JLabel(msg));
    browseModePn.add(new JComboBox(AUTO_DOWNLOAD_MSG_ARR));

    msg = mLocalizer.msg("startIn", "Start in");
    browseModePn.add(new JLabel(msg));
    browseModePn.add(new JComboBox(MODE_MSG_ARR));
    
    // TV data loader
    JPanel dataLoaderPn = new JPanel(new TabLayout(2, true));
    msg = mLocalizer.msg("tvDataLoader", "TV data loader");
    dataLoaderPn.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(dataLoaderPn);
    
    msg = mLocalizer.msg("configureTvDataLoaders", "Configure tv data loaders");
    dataLoaderPn.add(new JLabel(msg));
    p1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    dataLoaderPn.add(p1);
    mServiceCB = new JComboBox(DataLoaderManager.getInstance().getDataLoaders());
    mServiceCB.setRenderer(new DataLoaderRenderer());
    mServiceCB.addActionListener(this);
    p1.add(mServiceCB);
    p1.add(new JLabel(" "));
    mConfigBt = new JButton(mLocalizer.msg("configure", "Configure..."));
    mConfigBt.addActionListener(this);
    p1.add(mConfigBt);
    
    /*
    
    
    
    serviceConfigPanel.add(mServiceCB,BorderLayout.CENTER);
    mConfigBt=new JButton("configure...");
    mConfigBt.addActionListener(this);
    final String curSelectedService;
    mServiceCB.addActionListener(this);

    
    
    
    JPanel content=new JPanel();
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
    
    JPanel tvDataPanel=new JPanel();
    tvDataPanel.setLayout(new BoxLayout(tvDataPanel,BoxLayout.Y_AXIS));
    
    JPanel browserModePanel=new JPanel();
    browserModePanel.setLayout(new BoxLayout(browserModePanel,BoxLayout.Y_AXIS));
    
    JPanel dataServicePanel=new JPanel();
    dataServicePanel.setLayout(new BoxLayout(dataServicePanel,BoxLayout.Y_AXIS));
    
    browserModePanel.setBorder(BorderFactory.createTitledBorder("Browser mode"));
    dataServicePanel.setBorder(BorderFactory.createTitledBorder("TV data loader"));
    
    content.add(tvDataPanel);
    content.add(browserModePanel);
    content.add(dataServicePanel);
    
    JPanel delPanel=new JPanel(new GridLayout(0,2,20,0));
    delPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    delPanel.add(new JLabel("Delete tv data",JLabel.RIGHT));
    delPanel.add(new JComboBox(dataDeleteComboBoxEntries));
    
    JPanel dataDirPanel=new JPanel(new GridLayout(0,2,20,20));
    dataDirPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    dataDirPanel.add(new JLabel("tv data folder",JLabel.RIGHT));
    JPanel panel1=new JPanel(new BorderLayout());
    mTvDataTF=new JTextField(Settings.getTVDataDirectory());
    panel1.add(mTvDataTF,BorderLayout.CENTER);
    mChangeDataDirBt=new JButton("...");
    mChangeDataDirBt.addActionListener(this);
    panel1.add(mChangeDataDirBt,BorderLayout.EAST);
    
    
    dataDirPanel.add(panel1);
    
    
    JPanel autoPanel=new JPanel(new GridLayout(0,2,20,0));
    autoPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    autoPanel.add(new JLabel("Download automatically",JLabel.RIGHT));
    autoPanel.add(new JComboBox(autoDownloadComboBoxEntries));
    
    JPanel modePanel=new JPanel(new GridLayout(0,2,20,0));
    modePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    modePanel.add(new JLabel("Start in",JLabel.RIGHT));
    modePanel.add(new JComboBox(browserModeComboboxEntries));
    
    JPanel servicePanel=new JPanel(new GridLayout(0,2,20,0));
    servicePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    servicePanel.add(new JLabel("Configure tv data loaders:",JLabel.RIGHT));
    
    JPanel serviceConfigPanel=new JPanel(new BorderLayout());
    
    mServiceCB=new JComboBox(DataLoaderManager.getInstance().getDataLoaders());
    serviceConfigPanel.add(mServiceCB,BorderLayout.CENTER);
    mConfigBt=new JButton("configure...");
    mConfigBt.addActionListener(this);
    final String curSelectedService;
    mServiceCB.addActionListener(this);
    
    
    serviceConfigPanel.add(mConfigBt,BorderLayout.EAST);
    
    servicePanel.add(serviceConfigPanel);
    
    tvDataPanel.add(delPanel);
    tvDataPanel.add(dataDirPanel);
    browserModePanel.add(autoPanel);
    browserModePanel.add(modePanel);
    dataServicePanel.add(servicePanel);
    
    JPanel panel2=new JPanel();
    panel2.add(content);
    add(panel2,BorderLayout.NORTH);
    mConfigBt.addActionListener(this);
    final Frame parent=(Frame)getParent();
    mConfigBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        String item=(String)mServiceCB.getSelectedItem();
        JDialog dlg=new DataServiceConfigDlg(parent,item);
        dlg.pack();
        util.ui.UiUtilities.centerAndShow(dlg);
        dlg.dispose();
        
      }
    }
    );
    */
    
  }
  
  
  
  public void ok() {
    System.out.println("OK");
    Settings.setTVDataDirectory(mTvDataTF.getText());
  }
  
  
  
  public String getName() {
    return mLocalizer.msg("tvData", "TV data");
  }
  
  
  
  public void actionPerformed(ActionEvent event) {
    Object source=event.getSource();
    if (source == mServiceCB) {
      String item=(String)mServiceCB.getSelectedItem();
      tvdataloader.TVDataServiceInterface curSelectedService
        = DataLoaderManager.getInstance().getDataLoader(item);
      boolean enabled = (item != null) && (curSelectedService != null)
        && curSelectedService.hasSettingsPanel();
      mConfigBt.setEnabled(enabled);
    }
    else if (source == mChangeDataDirBt) {
      JFileChooser fc =new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setApproveButtonText(mLocalizer.msg("ok", "OK"));
      fc.setCurrentDirectory(new File(mTvDataTF.getText()));
      int retVal=fc.showOpenDialog(getParent());
      if (retVal==JFileChooser.APPROVE_OPTION) {
        File f=fc.getSelectedFile();
        mTvDataTF.setText(f.getAbsolutePath());
      }
    }
    else if (source == mConfigBt) {
      String item=(String)mServiceCB.getSelectedItem();
      DataServiceConfigDlg dlg = new DataServiceConfigDlg(this, item);
      dlg.centerAndShow();
    }
  }
  
  
  // inner class DataLoaderRenderer
  
  
  class DataLoaderRenderer extends DefaultListCellRenderer {
    
    public Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus)
    {
      if (value instanceof TVDataServiceInterface) {
        TVDataServiceInterface dataLoader = (TVDataServiceInterface) value;
        value = dataLoader.getName();
      }
      
      return super.getListCellRendererComponent(list, value, index, isSelected,
        cellHasFocus);
    }
  
  }
  
}
