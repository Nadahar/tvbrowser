/*
 * mZap2it-Plugin
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


package zap2itimporter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JPanel;

import tvdataservice.MutableProgram;
import util.exc.TvBrowserException;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.Date;


/**
 *
 * @author  pumpkin
 */
public class zap2itimporter implements tvdataservice.TvDataService{
  
  protected static final String USER = "user";
  protected static final String PASS = "pass";
  protected static final String CHANNEL_NAME = "channelname";
  protected static final String CHANNEL_ID = "channelid";
  protected static final String CHANNEL_NUMBER = "numberOfChannels";
  
  protected String mUsername;
  protected String mPassword;
  private Channel[] mChannels = new Channel[0];
  
  /** Creates a new instance of zap2itImporter */
  public zap2itimporter() {
  }
  
  public Channel[] checkForAvailableChannels(devplugin.ProgressMonitor monitor) throws TvBrowserException {
    if (mUsername!=null){
      try {
        SaxHandler sh = new SaxHandler(this,new Date(),0,monitor);
        Hashtable<Channel, Vector<MutableProgram>> result = sh.doWork();
        Enumeration<Channel> en = result.keys();
        Vector<Channel> v = new Vector<Channel>();
        while (en.hasMoreElements()){
          v.add(en.nextElement());
        }
        mChannels =  v.toArray(new Channel[v.size()]);
        return mChannels;
      } catch (Exception e){
        e.printStackTrace();
        throw new TvBrowserException(this.getClass(),"error",e.toString());
      }
    } else {
      throw new TvBrowserException(this.getClass(),"no username found","no username found");
    }
  }
  
  /** Gets the Version of the implemented API
   * Since TV-Browser 0.9.7 getAPIVersion must return 1.0
   */
  public devplugin.Version getAPIVersion() {
    return new devplugin.Version(1,0,false);
  }
  
  
  /** Gets the list of the mChannels that are available by this data service.
   */
  public Channel[] getAvailableChannels() {
    return mChannels;
  }
  
  /** Gets information about this TvDataService
   */
  public devplugin.PluginInfo getInfo() {
    devplugin.PluginInfo info = new devplugin.PluginInfo("Zap2It-Importer","Imports Zap2It-Data", "Gilson Laurent", new devplugin.Version (0,2,false));
    return info;
  }
  
  public tvdataservice.SettingsPanel getSettingsPanel() {
    MySettingsPanel panel = new MySettingsPanel(this);
    return panel;
  }
  
  public boolean hasSettingsPanel() {
    return true;
  }
  
  /** Called by the host-application during start-up. Implements this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(java.util.Properties settings) {
    mUsername = settings.getProperty(USER);
    mPassword = settings.getProperty(PASS);
    String number = settings.getProperty(CHANNEL_NUMBER);
    ChannelGroup group = new Zap2itChannelGroup();
    try {
      int length = Integer.parseInt(number);
      mChannels = new Channel[length];
      for (int i =0;i<length;i++){
        String name = settings.getProperty(CHANNEL_NAME+i);
        String id = settings.getProperty(CHANNEL_ID+i);
        if ((id!=null) && (name!=null)){
          mChannels[i] = new Channel(this, name, id, java.util.TimeZone.getDefault(), "us", "(c) mZap2it-labs","http://www.zap2it.com",group);
        } else {
          System.out.println("fehlende Daten");;
        }
      }
    } catch (Exception E){

    }
  }
  
  public void setWorkingDirectory(java.io.File dataDir) {
  }
  
  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public java.util.Properties storeSettings() {
    java.util.Properties settings = new java.util.Properties();
    try {
      settings.setProperty(USER,mUsername);
      settings.setProperty(PASS,mPassword);
      settings.setProperty(CHANNEL_NUMBER,Integer.toString(mChannels.length));
      for (int i =0;i<mChannels.length;i++){
        settings.setProperty(CHANNEL_NAME+i,mChannels[i].getName());
        settings.setProperty(CHANNEL_ID+i,mChannels[i].getId());
      }
    } catch (Exception E){
      E.printStackTrace();
    }
    return settings;
  }
  
  public boolean supportsDynamicChannelList() {
    return true;
  }
  
  /** Updates the TV data provided by this data service.
   *
   * @throws TvBrowserException
   */
  public void updateTvData(tvdataservice.TvDataUpdateManager updateManager, Channel[] channelArr, Date startDate, int dateCount, devplugin.ProgressMonitor monitor) throws TvBrowserException {
    try {
      SaxHandler sh = new SaxHandler(this,new Date(startDate),dateCount,monitor);
      Hashtable<Channel, Vector<MutableProgram>> result = sh.doWork();
      monitor.setMessage ("transfering data to tvbrowser DB");
      Enumeration<Channel> en = result.keys();
      Vector<Channel> v = new Vector<Channel>();
      while (en.hasMoreElements()){
        v.add(en.nextElement());
      }
      mChannels = v.toArray(new Channel[v.size()]);
      for (int i=0;i<mChannels.length;i++){
        Vector<MutableProgram> progs = result.get(mChannels[i]);
        if (progs!=null){
          tvdataservice.MutableProgram[] progsToImport = progs.toArray(new tvdataservice.MutableProgram[progs.size()]);
          for (int j=0;j<dateCount;j++){
            Date date = new Date(startDate);
            date = date.addDays(j);
            System.out.println("importing data for "+date.getDateString()+" and "+mChannels[i].getName());
            tvdataservice.MutableChannelDayProgram mdcp = new tvdataservice.MutableChannelDayProgram(date,mChannels[i]);
            for (int k=0;k<progsToImport.length;k++){
              Date toTest = progsToImport[k].getDate();
              if ((toTest.getMonth() == date.getMonth()) && (toTest.getDayOfMonth() == date.getDayOfMonth())){
                System.out.print(" "+toTest.getDayOfMonth());
                if (progsToImport[k].getTitle() != null){
                  if (progsToImport[k].getTitle().length() !=0){
                    mdcp.addProgram(progsToImport[k]);
                  } else {
                    System.out.println("zero-length-title");
                  }
                } else {
                  System.out.println("null-title");
                }
              }
            }
            updateManager.updateDayProgram(mdcp);
          }
        }
      }
    } catch (Exception e){
      e.printStackTrace();
      throw new TvBrowserException(this.getClass(),"error",e.toString());
    }
  }


  class Zap2itChannelGroup implements ChannelGroup {

    public Zap2itChannelGroup() {

    }

    public String getName() {
      return "Zap2it";
    }

    public String getId() {
      return "zap2it";
    }

    public String getDescription() {
      return "www.zap2it.com";
    }

    public String getProviderName() {
      return "Zap2it";
    }
  }

}




class MySettingsPanel extends tvdataservice.SettingsPanel implements java.awt.event.ActionListener{
  
  zap2itimporter mZap2it;
  javax.swing.JTextField mUsername;
  javax.swing.JTextField mPassword;
  
  public MySettingsPanel(zap2itimporter zap){
    mZap2it = zap;
    
    this.setLayout (new GridLayout (1,1));
    
    JPanel panel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 1.0;
    javax.swing.JLabel userNameLabel = new javax.swing.JLabel("username");
    gridbag.setConstraints(userNameLabel, c);
    panel.add(userNameLabel);
    
    mUsername = new javax.swing.JTextField(mZap2it.mUsername);
    c.anchor = GridBagConstraints.EAST;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gridbag.setConstraints(mUsername, c);
    panel.add(mUsername);
    
    c.gridwidth = GridBagConstraints.RELATIVE;
    c.anchor = GridBagConstraints.WEST;
    javax.swing.JLabel passwordLabel = new javax.swing.JLabel("password");
    gridbag.setConstraints(passwordLabel, c);
    panel.add(passwordLabel);
    
    mPassword = new javax.swing.JTextField(mZap2it.mPassword);
    c.anchor = GridBagConstraints.EAST;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gridbag.setConstraints(mPassword, c);
    panel.add(mPassword);
    
    javax.swing.JButton update = new javax.swing.JButton("update channellist");
    c.anchor = GridBagConstraints.EAST;
    gridbag.setConstraints(update, c);
    panel.add(update);
    
    add (panel);
    update.addActionListener(this);
  }
  
  public void ok() {
    mZap2it.mUsername = mUsername.getText();
    mZap2it.mPassword = mPassword.getText();
  }
  
  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    ok();
    try {
      mZap2it.checkForAvailableChannels(null);
    } catch (Exception e){
      e.printStackTrace();
      javax.swing.JOptionPane.showMessageDialog(this,"update failed","error",javax.swing.JOptionPane.ERROR_MESSAGE);
    }
  }


}
