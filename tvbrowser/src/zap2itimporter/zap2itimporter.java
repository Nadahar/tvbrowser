/*
 * zap2it-Plugin
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

import devplugin.Date;
import devplugin.Channel;
import java.util.Vector;
import util.exc.TvBrowserException;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;

import javax.swing.JPanel;


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
  
  protected String username;
  protected String password;
  private Channel[] channels = new Channel[0];
  
  /** Creates a new instance of zap2itImporter */
  public zap2itimporter() {
  }
  
  public Channel[] checkForAvailableChannels(devplugin.ProgressMonitor monitor) throws TvBrowserException {
    if (username!=null){
      try {
        saxHandler sh = new saxHandler(this,new Date(),0,monitor);
        java.util.Hashtable result = sh.doWork();
        java.util.Enumeration en = result.keys();
        Vector v = new Vector();
        while (en.hasMoreElements()){
          v.add(en.nextElement());
        }
        channels =  (Channel[])v.toArray(new Channel[v.size()]);
        return channels;
      } catch (Exception E){
        E.printStackTrace();
        throw new TvBrowserException(this.getClass(),"error",E.toString());
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
  
  
  /** Gets the list of the channels that are available by this data service.
   */
  public Channel[] getAvailableChannels() {
    return channels;
  }
  
  /** Gets information about this TvDataService
   */
  public devplugin.PluginInfo getInfo() {
    devplugin.PluginInfo info = new devplugin.PluginInfo("Zap2It-Importer","Imports Zap2It-Data", "Gilson Laurent");
    return info;
  }
  
  public tvdataservice.SettingsPanel getSettingsPanel() {
    mySettingsPanel panel = new mySettingsPanel(this);
    return panel;
  }
  
  public boolean hasSettingsPanel() {
    return true;
  }
  
  /** Called by the host-application during start-up. Implements this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(java.util.Properties settings) {
    username = settings.getProperty(USER);
    password = settings.getProperty(PASS);
    String number = settings.getProperty(CHANNEL_NUMBER);
    try {
      int length = Integer.parseInt(number);
      channels = new Channel[length];
      for (int i =0;i<length;i++){
        String name = settings.getProperty(CHANNEL_NAME+i);
        String id = settings.getProperty(CHANNEL_ID+i);
        if ((id!=null) && (name!=null)){
          channels[i] = new Channel(this, name, id, java.util.TimeZone.getDefault(), "us", "(c) zap2it-labs");
        } else {
          System.out.println("fehlende Daten");;
        }
      }
    } catch (Exception E){
      Channel[] channels = new Channel[0];
    }
  }
  
  public void setWorkingDirectory(java.io.File dataDir) {
    System.out.println ("********************************** "+dataDir.toString()+" *******************");
  }
  
  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public java.util.Properties storeSettings() {
    java.util.Properties settings = new java.util.Properties();
    try {
      settings.setProperty(USER,username);
      settings.setProperty(PASS,password);
      settings.setProperty(this.CHANNEL_NUMBER,Integer.toString(channels.length));
      for (int i =0;i<channels.length;i++){
        settings.setProperty(this.CHANNEL_NAME+i,channels[i].getName());
        settings.setProperty(this.CHANNEL_ID+i,channels[i].getId());
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
      saxHandler sh = new saxHandler(this,new Date(startDate),dateCount,monitor);
      java.util.Hashtable result = sh.doWork();
      monitor.setMessage ("transfering data to tvbrowser DB");
      java.util.Enumeration en = result.keys();
      Vector v = new Vector();
      while (en.hasMoreElements()){
        v.add(en.nextElement());
      }
      channels = (Channel[])v.toArray(new Channel[v.size()]);
      for (int i=0;i<channels.length;i++){
        int month = startDate.getMonth();
        int day = startDate.getDayOfMonth();
        Vector progs = (Vector) result.get(channels[i]);
        if (progs!=null){
          tvdataservice.MutableProgram[] progsToImport = (tvdataservice.MutableProgram[])progs.toArray(new tvdataservice.MutableProgram[progs.size()]);
          for (int j=0;j<dateCount;j++){
            Date date = new Date(startDate);
            date = date.addDays(j);
            System.out.println("importing data for "+date.getDateString()+" and "+channels[i].getName());
            tvdataservice.MutableChannelDayProgram mdcp = new tvdataservice.MutableChannelDayProgram(date,channels[i]);
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
    } catch (Exception E){
      E.printStackTrace();
      throw new TvBrowserException(this.getClass(),"error",E.toString());
    }
  }
  
}


class mySettingsPanel extends tvdataservice.SettingsPanel implements java.awt.event.ActionListener{
  
  zap2itimporter zap2it;
  javax.swing.JTextField username;
  javax.swing.JTextField password;
  
  public mySettingsPanel(zap2itimporter zap){
    zap2it = zap;
    
    this.setLayout (new GridLayout (1,1));
    
    JPanel panel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.fill = c.HORIZONTAL;
    c.anchor = c.WEST;
    c.weightx = 1.0;
    javax.swing.JLabel userNameLabel = new javax.swing.JLabel("username");
    gridbag.setConstraints(userNameLabel, c);
    panel.add(userNameLabel);
    
    username = new javax.swing.JTextField(zap2it.username);
    c.anchor = c.EAST;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gridbag.setConstraints(username, c);
    panel.add(username);
    
    c.gridwidth = GridBagConstraints.RELATIVE;
    c.anchor = c.WEST;
    javax.swing.JLabel passwordLabel = new javax.swing.JLabel("password");
    gridbag.setConstraints(passwordLabel, c);
    panel.add(passwordLabel);
    
    password = new javax.swing.JTextField(zap2it.password);
    c.anchor = c.EAST;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gridbag.setConstraints(password, c);
    panel.add(password);
    
    javax.swing.JButton update = new javax.swing.JButton("update channellist");
    c.anchor = c.EAST;
    gridbag.setConstraints(update, c);
    panel.add(update);
    
    add (panel);
    update.addActionListener(this);
  }
  
  public void ok() {
    zap2it.username = username.getText();
    zap2it.password = password.getText();
  }
  
  public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
    ok();
    try {
      zap2it.checkForAvailableChannels(null);
    } catch (Exception E){
      E.printStackTrace();
      javax.swing.JOptionPane.showMessageDialog(this,"update failed","error",javax.swing.JOptionPane.ERROR_MESSAGE);
    }
  }
}