/*
 * PDA-Plugin
 * Copyright (C) 2004 gilson laurent pumpkin@gmx.de
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
 */
package microtvbrowserplugin;

import java.awt.BorderLayout;
import java.util.Properties;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;


import util.ui.progress.ProgressWindow;



public class MicroTvBrowserPlugin extends devplugin.Plugin{
  
  public static StringBuffer exportLog = new StringBuffer("");
  
  private JFileChooser chooser;
  
  
  private String[] channelList = new String[0];
  private int daysToExport = 1;
  private String lastUsedDir;
  
  private boolean useNanoEdition = false;
  private int exportLevel = 0;
  
  private boolean useIconsInProgList = false;
  
  private static final String PROP_USE_ICONS_IN_PROG_LIST = "iconsInProgList";
  private static final String PROP_EXPORT_LEVEL = "exportlevel";
  private static final String PROP_USE_NANO = "useNano";
  private static final String PROP_DAYS_TO_EXPORT = "days";
  private static final String PROP_CHANNELS_TO_EXPORT = "channels";
  private static final String PROP_CHANNEL = "channel";
  
  private static final String PROP_CHANNEL_NAME_IN_NOW_LIST = "channelInNow";
  private static final String PROP_STANDART_DIR = "saveTo";
  
  static util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MicroTvBrowserPlugin.class);
  
  public void loadSettings(Properties settings) {
    try {
      int length = Integer.parseInt(settings.getProperty(PROP_CHANNELS_TO_EXPORT));
      channelList = new String[length];
      for (int i=0;i<length;i++){
        channelList[i] = settings.getProperty(PROP_CHANNEL+i);
      }
      daysToExport = Integer.parseInt(settings.getProperty(PROP_DAYS_TO_EXPORT));
      useNanoEdition = Boolean.getBoolean(settings.getProperty(PROP_USE_NANO));
      exportLevel = Integer.parseInt(settings.getProperty(PROP_EXPORT_LEVEL));
      useIconsInProgList = new Boolean(settings.getProperty(PROP_USE_ICONS_IN_PROG_LIST)).booleanValue();
      channelNameInNowList = new Boolean(settings.getProperty(PROP_CHANNEL_NAME_IN_NOW_LIST)).booleanValue();
      lastUsedDir = settings.getProperty(PROP_STANDART_DIR,null);
    } catch (Exception E){
    }
  }
  
  public Properties storeSettings() {
    Properties P = new Properties();
    P.setProperty(PROP_CHANNEL_NAME_IN_NOW_LIST, Boolean.toString(channelNameInNowList));
    P.setProperty(PROP_USE_ICONS_IN_PROG_LIST,Boolean.toString(useIconsInProgList));
    P.setProperty(PROP_EXPORT_LEVEL,Integer.toString(exportLevel));
    P.setProperty(PROP_USE_NANO,Boolean.toString(useNanoEdition));
    P.setProperty(PROP_DAYS_TO_EXPORT,Integer.toString(daysToExport));
    P.setProperty(PROP_CHANNELS_TO_EXPORT,Integer.toString(channelList.length));
    for (int i=0;i<channelList.length;i++){
      P.setProperty(PROP_CHANNEL+i,channelList[i]);
    }
    P.setProperty(PROP_STANDART_DIR,lastUsedDir);
    return P;
  }
  
  public String[] getChannelList(){
    return channelList;
  }
  
  public void setChannelList(String[] s){
    channelList = s;
  }
  
  public int getDaysToExport(){
    return daysToExport;
  }
  
  public void setDaysToExport(int s){
    daysToExport= s;
  }
  
  public devplugin.PluginInfo getInfo() {
    String name = "MicroTvBrowser";
    String desc = mLocalizer.msg( "Creates MIDlets" ,"Creates MIDlets" );
    String author = "Gilson Laurent";
    return new devplugin.PluginInfo(name, desc, author, new devplugin.Version(0,84));
  }
  
  /**
   * This method is called by the host-application to show the plugin in the
   * menu or in the toolbar.
   */
  public String getButtonText() {
    return "MicroTvBrowser";//
  }
  
  public devplugin.SettingsTab getSettingsTab() {
    return new Settings(this);
  }
  
  public java.util.jar.JarFile getJarFilePublic(){
    return getJarFile();
  }
  
  public java.awt.Frame getParentFramePublic(){
    return getParentFrame();
  }
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the menu or the toolbar.
   */
  public void execute() {
    while (channelList.length==0){
      if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(getParentFrame()
      ,mLocalizer.msg( "No Channels selected." ,"No Channels selected. Please use settings to select some.\n\n would you like to see the dialog now ?")
      ,mLocalizer.msg( "nothing to export","nothing to export"), JOptionPane.YES_NO_OPTION)){
        return;
      } else {
        showSettings();
      }
    }
    exportLog = new StringBuffer("");
    
    java.io.File dir = null;
    chooser = new JFileChooser();
    if (lastUsedDir!= null){
      chooser.setCurrentDirectory(new java.io.File(lastUsedDir));
    }
    chooser.setDialogTitle(mLocalizer.msg("choose directory","choose directory"));
    
    JPanel jp = new JPanel();
    JButton jb = new JButton(mLocalizer.msg("Settings","Settings"));
    jp.add(jb);
    
    jb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showSettings();
      }
    });
    chooser.setAccessory(jp);
    
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int returnVal = chooser.showSaveDialog(getParentFrame());
    if(returnVal == JFileChooser.APPROVE_OPTION) {
      dir = chooser.getSelectedFile();
      lastUsedDir = dir.toString();
    } else {
      return;
    }
    ProgressWindow progress = new ProgressWindow(getParentFrame(),mLocalizer.msg("Please wait","Please wait"));
    
    if (this.useNanoEdition){
      NanoMIDletCreator nano = new NanoMIDletCreator(this,dir,progress);
      progress.run(nano);
    } else {
      MIDletCreator MC = new MIDletCreator(this, dir, progress);
      progress.run(MC);
    }
  }
  
  /**
   * Returns the name of the file, containing your mark icon (in the jar-File).
   * <p>
   * This icon is used for marking programs in the program table.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   *
   * @see #getMarkIcon()
   */
  public String getMarkIconName(){
    return "/microtvbrowserplugin/M16.png";
  }
  
  
  /**
   * Returns the name of the file, containing your button icon (in the jar-File).
   * <p>
   * This icon is used for the toolbar and the menu.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   *
   * @see #getButtonIcon()
   */
  public String getButtonIconName(){
    return "/microtvbrowserplugin/M16.png";
  }
  
  protected void dismissSettings(boolean save){
    if (save){
      configDialogSettings.saveSettings();
    }
    configDialog.hide();
    configDialog.dispose();
  }
  
  JDialog configDialog;
  devplugin.SettingsTab configDialogSettings;
  
  private boolean channelNameInNowList = false;
  
  public void showSettings(){
    configDialog = new JDialog(getParentFrame(),true);
    configDialog.getContentPane().setLayout(new BorderLayout());
    configDialogSettings = getSettingsTab();
    configDialog.getContentPane().add(configDialogSettings.createSettingsPanel(),BorderLayout.CENTER);
    
    JPanel buttons = new JPanel();
    
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dismissSettings(true);
      }
    });
    
    JButton cancelButton = new JButton(mLocalizer.msg("Cancel","Cancel"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dismissSettings(false);
      }
    });
    
    buttons.add(okButton);
    buttons.add(cancelButton);
    
    configDialog.getContentPane().add(buttons,BorderLayout.SOUTH);
    
    configDialog.setSize(400,600);
    configDialog.setLocationRelativeTo(null);
    configDialog.show();
  }
  
  /** Getter for property useNanoEdition.
   * @return Value of property useNanoEdition.
   */
  public boolean isUseNanoEdition() {
    return useNanoEdition;
  }
  
  /** Setter for property useNanoEdition.
   * @param useNanoEdition New value of property useNanoEdition.
   */
  public void setUseNanoEdition(boolean useNanoEdition) {
    this.useNanoEdition = useNanoEdition;
  }
  
  /** Getter for property exportLevel.
   * @return Value of property exportLevel.
   */
  public int getExportLevel() {
    return exportLevel;
  }
  
  /** Setter for property exportLevel.
   * @param exportLevel New value of property exportLevel.
   */
  public void setExportLevel(int exportLevel) {
    this.exportLevel = exportLevel;
  }
  
  /** Getter for property useIconsInProgList.
   * @return Value of property useIconsInProgList.
   */
  public boolean isUseIconsInProgList() {
    return useIconsInProgList;
  }
  
  /** Setter for property useIconsInProgList.
   * @param useIconsInProgList New value of property useIconsInProgList.
   */
  public void setUseIconsInProgList(boolean useIcons) {
    useIconsInProgList = useIcons;
  }
  
  /** Getter for property channelNameInNowList.
   * @return Value of property channelNameInNowList.
   */
  public boolean isChannelNameInNowList() {
    return channelNameInNowList;
  }
  
  /** Setter for property channelNameInNowList.
   * @param channelNameInNowList New value of property channelNameInNowList.
   */
  public void setChannelNameInNowList(boolean channelNameInNowList) {
    this.channelNameInNowList = channelNameInNowList;
  }
  
}
