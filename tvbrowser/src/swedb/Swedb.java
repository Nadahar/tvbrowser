/*
 * Swedb.java
 *
 * Created on March 7, 2005, 10:28 AM
 */

package swedb;

import java.awt.Frame;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;

import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import devplugin.Channel;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;

/**
 *
 * @author  pumpkin
 */
public class Swedb implements tvdataservice.TvDataService{
  
  private long lastChannelUpdate = 0;
  
  private Channel[] channel = new Channel[0];
  private ChannelContainer[] internalChannel = new ChannelContainer[0];
  
  private boolean firstContact = true;
  
  /** Creates a new instance of Swedb */
  public Swedb() {
  }
  
  public Channel[] checkForAvailableChannels(ProgressMonitor monitor) throws TvBrowserException {
    try {
      if (firstContact){
        JOptionPane.showMessageDialog(new Frame(),"Please support the soucre and register at http://tv.swedb.se/","Copyright notice",JOptionPane.INFORMATION_MESSAGE);
        firstContact = false;
      }
      URL url = new URL("http://tv.swedb.se/xmltv/channels.xml.gz");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setIfModifiedSince(lastChannelUpdate);
      if (con.getResponseCode() == 200){
        internalChannel = ChannelParser.parse(new GZIPInputStream(con.getInputStream()));
        System.out.println("done, found "+internalChannel.length+" channels");
        lastChannelUpdate = con.getLastModified();
        con.disconnect();
        convert();
        System.out.println("convert done");
      } else {
        System.out.println("no new data");
      }
    } catch (Exception E){
      throw new TvBrowserException(Swedb.class, "somethings wrong","somethings wrong");
    }
    return null;
  }
  
  private void convert(){
    channel = new Channel[internalChannel.length];
    for (int i=0;i<internalChannel.length;i++){
      channel[i] = new Channel(this, internalChannel[i].getName(), internalChannel[i].getId(), TimeZone.getDefault(), "sw", "(c) swedb", "http://tv.swedb.se");
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
  public Channel[] getAvailableChannels(){
    return channel;
  }
  
  /** Gets information about this TvDataService
   */
  public PluginInfo getInfo() {
    return new PluginInfo("swedb Importer", "imports data from swedb", "Gilson Laurent", new devplugin.Version(0,2,false));
  }
  
  
  public SettingsPanel getSettingsPanel() {
    return null;
  }
  
  public boolean hasSettingsPanel() {
    return false;
  }
  
  /** Called by the host-application during start-up. Implements this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(Properties settings) {
    try {
      lastChannelUpdate = Long.parseLong(settings.getProperty("CHANNEL_TIME"));
      System.out.println ("last channel update: "+lastChannelUpdate);
      firstContact = new Boolean (settings.getProperty("FIRST_CONTACT","true")).booleanValue();
      int channels = Integer.parseInt(settings.getProperty("NUMBER_OF_CHANNELS"));
      internalChannel = new ChannelContainer[channels];
      for (int i=0;i<channels;i++){
        internalChannel[i] = new ChannelContainer(
        settings.getProperty("CHANNELID"+i),
        settings.getProperty("CHANNELNAME"+i),
        settings.getProperty("CHANNELURL"+i),
        settings.getProperty("CHANNELTIME"+i));
      }
      convert();
    } catch (Exception E){
      lastChannelUpdate = 0;
      channel = new Channel[0];
      internalChannel = new ChannelContainer[0];
      firstContact = true;
    }
  }
  
  public void setWorkingDirectory(java.io.File dataDir) {
  }
  
  
  /** Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public Properties storeSettings() {
    Properties settings = new Properties();
    settings.setProperty("CHANNEL_TIME", Long.toString(lastChannelUpdate));
    settings.setProperty("NUMBER_OF_CHANNELS", Integer.toString(internalChannel.length));
    settings.setProperty("FIRST_CONTACT", Boolean.toString(firstContact));
    for (int i=0;i<internalChannel.length;i++){
      settings.setProperty("CHANNELID"+i, internalChannel[i].getId());
      settings.setProperty("CHANNELNAME"+i, internalChannel[i].getName());
      settings.setProperty("CHANNELURL"+i, internalChannel[i].getBaseUrl());
      settings.setProperty("CHANNELTIME"+i, internalChannel[i].getLastUpdateString());
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
  public void updateTvData(TvDataUpdateManager updateManager, Channel[] channelArr, devplugin.Date startDate, int dateCount, ProgressMonitor monitor) throws TvBrowserException {
    checkForAvailableChannels(monitor);
    System.out.println ("swedb asked for "+channelArr.length+" channels");
    int counter = 0;
    monitor.setMaximum(channelArr.length*dateCount);
    devplugin.Date start = new devplugin.Date(startDate);
    long[] updateTime = new long[internalChannel.length];
    
    for (int time =0;time<dateCount;time++){
      devplugin.Date day = start.addDays(time);
      for (int i =0;i<channelArr.length;i++){
        monitor.setValue(counter++);
        for (int j=0;j<channel.length;j++){
          if (channel[j].equals(channelArr[i])){
            //den nehmen wir.
            String datum = Integer.toString(day.getYear());
            datum = datum + "-";
            if (day.getMonth()< 10){
              datum = datum + "0";
            }
            datum = datum + Integer.toString(day.getMonth());
            datum = datum + "-";
            
            if (day.getDayOfMonth()< 10){
              datum = datum + "0";
            }
            datum = datum + Integer.toString(day.getDayOfMonth());
            try {
              System.out.println("getting: "+internalChannel[j].getBaseUrl()+internalChannel[j].getId()+"_"+datum+".xml.gz");
              URL url = new URL(internalChannel[j].getBaseUrl()+internalChannel[j].getId()+"_"+datum+".xml.gz");
              HttpURLConnection con = (HttpURLConnection) url.openConnection();
              con.setIfModifiedSince(internalChannel[j].getLastUpdate(day));
              System.out.println("lastupdate was: "+internalChannel[j].getLastUpdate(day)+" returncode: "+con.getResponseCode());
              if (con.getResponseCode() == 200){
                updateManager.updateDayProgram(DayParser.parse(new GZIPInputStream(con.getInputStream()),channelArr[i],day));
                internalChannel[j].setLastUpdate(day,con.getLastModified());
              }
            } catch (Exception E){
              E.printStackTrace();
            }
          }
        }
      }
    }
  }
  
}
