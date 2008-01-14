/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date: 2006-06-03 00:23:19 +0200 (Sa, 03 Jun 2006) $
 *   $Author: ds10 $
 * $Revision: 2452 $
 */
package bbcbackstagedataservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.compress.tar.TarEntry;
import org.apache.commons.compress.tar.TarInputStream;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.ui.Localizer;
import bbc.rd.tvanytime.TVAnytimeException;
import bbc.rd.tvanytime.serviceInformation.ServiceInformation;
import bbc.rd.tvanytime.xml.NonFatalXMLException;
import bbc.rd.tvanytime.xml.SAXXMLParser;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;
import devplugin.Version;

/**
 * This Dataservice collects Data from http://backstage.bbc.co.uk/feeds/tvradio/
 *
 * @author bodum
 */
public class BbcBackstageDataService extends AbstractTvDataService {
  /** Base-URL */
  private static final String BASEURL = "http://backstage.bbc.co.uk/feeds/tvradio/";
  /** Translator */
  private static final Localizer mLocalizer = Localizer
          .getLocalizerFor(BbcBackstageDataService.class);
  /**
   * Logger
   */
  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(BbcBackstageDataService.class
      .getName());

  /**
   * Channelgroup
   */
  private ChannelGroup mBbcChannelGroup = new BbcChannelGroup("BBC Backstage", "bbcbackstage",
      mLocalizer.msg("desc", "Data from BBC Backstage"), "BBC Backstage");

  /**
   * List of Channels
   */
  private ArrayList<Channel> mChannels = new ArrayList<Channel>();

  /**
   * Working-Directory
   */
  private File mWorkingDir;

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#checkForAvailableChannelGroups(devplugin.ProgressMonitor)
   */
  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor) throws TvBrowserException {
    return new ChannelGroup[] { mBbcChannelGroup };
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#checkForAvailableChannels(devplugin.ChannelGroup, devplugin.ProgressMonitor)
   */
  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
    try {
      ArrayList<Channel> channels = new ArrayList<Channel>();

      monitor.setMessage(mLocalizer.msg("loading", "Loading BBC data"));
      
      loadBBCData();
      
      monitor.setMessage(mLocalizer.msg("parsing", "Parsing BBC Data"));
      // Create parser
      SAXXMLParser parser = new SAXXMLParser();
      // Configure the parser to parse the standard profile (ie. everything).
      (parser).setParseProfile(SAXXMLParser.STANDARD);

      String info = "";
      try {
        // Do the parsing...
        File file = new File(mWorkingDir, "ServiceInformation.xml");
        parser.parse(file);
        
        FileInputStream stream = new FileInputStream(file);
        int bytes= stream.available();
        byte buffer[]= new byte[bytes];
        stream.read(buffer);
        info = new String(buffer).toLowerCase();
      } catch (NonFatalXMLException nfxe) {
        // Handle non-fatal XML exceptions
        // Contain any invalid TVAnytime data values from XML source.
        // These are all collated by the parser and thrown at the end to avoid
        // having to abort the parsing.
        nfxe.printStackTrace();
      }

      monitor.setMessage(mLocalizer.msg("store", "Storing BBC Data"));
      
      int categories = Channel.CATEGORY_NONE;
      int max = parser.getServiceInformationTable().getNumServiceInformations();
      for (int i = 0; i < max; i++) {
        ServiceInformation serviceInfo = parser.getServiceInformationTable().getServiceInformation(i);

        // find out, whether this is radio or TV
        int pos = info.indexOf(("<ServiceInformation serviceId='" + serviceInfo.getServiceID() +"'>").toLowerCase());
        if (pos >= 0) {
          pos = info.indexOf("<![CDATA[".toLowerCase(), pos);
          if (pos >= 0) {
            pos += 9;
            if (info.substring(pos).startsWith("Audio and video".toLowerCase())) {
              categories = Channel.CATEGORY_TV;
            }
            else if (info.substring(pos).startsWith("Audio only".toLowerCase())) {
              categories = Channel.CATEGORY_RADIO;
            }
          }
        }
        Channel ch = new Channel(this, serviceInfo.getName(), serviceInfo.getServiceID(), TimeZone
            .getTimeZone("GMT"), "gb", "(c) BBC", getChannelUrl(serviceInfo.getName()), mBbcChannelGroup, null, categories);
        channels.add(ch);
        mLog.fine("Channel : " + ch.getName() + '{' + ch.getId() + '}');
      }

      mChannels = channels;

      monitor.setMessage(mLocalizer.msg("done", "Done with BBC data"));

      return channels.toArray(new Channel[channels.size()]);
    } catch (TVAnytimeException tvae) {
      // Handle any other TVAnytime-specific exceptions that may be generated.
      // E.g. if the XML parser cannot be initialized.
      throw new TvBrowserException(getClass(), "error.1", "Problems while Parsing the Data.", tvae);

    } catch (IOException ioe) {
      // Handle IOExceptions: things like missing file
      throw new TvBrowserException(getClass(), "error.2", "Problems while loading the Data.", ioe);
    }
  }

  private String getChannelUrl(String channelName) {
    HashMap<String, String> urls = new HashMap<String, String>();
    urls.put("BBC Radio 1","http://www.bbc.co.uk/radio1/");
    urls.put("BBC Radio 2","http://www.bbc.co.uk/radio2/");
    urls.put("BBC Radio 3","http://www.bbc.co.uk/radio3/");
    urls.put("BBC Radio 4","http://www.bbc.co.uk/radio4/");
    urls.put("BBC Radio 5 Live","http://www.bbc.co.uk/fivelive/");
    urls.put("1Xtra","http://www.bbc.co.uk/1xtra/");
    urls.put("BBC 6 Music","http://www.bbc.co.uk/6music/");
    urls.put("BBC 7","http://www.bbc.co.uk/bbc7/");
    urls.put("BBC Asian Network","http://www.bbc.co.uk/asiannetwork/");
    urls.put("BBC World Service","http://www.bbc.co.uk/worldservice/");
    String url = urls.get(channelName);
    if (url != null) {
      return url;
    }
    return "http://bbc.co.uk";
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#getAvailableChannels(devplugin.ChannelGroup)
   */
  public Channel[] getAvailableChannels(ChannelGroup group) {
    return mChannels.toArray(new Channel[mChannels.size()]);
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#getAvailableGroups()
   */
  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[] { mBbcChannelGroup };
  }
  
  public static Version getVersion() {
    return new Version(2,60);
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#getInfo()
   */
  public PluginInfo getInfo() {
    return new PluginInfo(BbcBackstageDataService.class, mLocalizer.msg("name","BBC Data"), 
        mLocalizer.msg("desc", "Data from BBC Backstage."), "Bodo Tasche");
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#getSettingsPanel()
   */
  public SettingsPanel getSettingsPanel() {
    return null;
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#hasSettingsPanel()
   */
  public boolean hasSettingsPanel() {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#loadSettings(java.util.Properties)
   */
  public void loadSettings(Properties settings) {
    mLog.info("Loading settings in BbcBackstageDataService");
    
    int numChannels = Integer.parseInt(settings.getProperty("NumberOfChannels", "0"));
    
    mChannels = new ArrayList<Channel>();
    
    for (int i=0;i<numChannels;i++){
      String channelName = settings.getProperty("ChannelTitle-" + i, "");
      String channelId = settings.getProperty("ChannelId-" + i, "");
      int categories = Integer.parseInt(settings.getProperty("ChannelCategories-" + i, "0"));
      
      Channel ch = new Channel(this, channelName, channelId, TimeZone
          .getTimeZone("GMT+0:00"), "gb", "(c) BBC", getChannelUrl(channelName), mBbcChannelGroup, null, categories);
      mChannels.add(ch);
      mLog.fine("Channel : " + ch.getName() + '{' + ch.getId() + '}');
    }

    mLog.info("Finished loading settings for BbcBackstageDataService");
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#storeSettings()
   */
  public Properties storeSettings() {
    mLog.info("Storing settings for BbcBackstageDataService");

    Properties prop = new Properties();

    prop.setProperty("NumberOfChannels", Integer.toString(mChannels.size()));
    int max = mChannels.size();
    for (int i = 0; i < max; i++) {
      Channel ch = mChannels.get(i);
      prop.setProperty("ChannelId-" + i, ch.getId());
      prop.setProperty("ChannelTitle-" + i, ch.getName());
      prop.setProperty("ChannelCategories-" + i, Integer.toString(ch.getCategories()));
    }
    mLog.info("Finished storing settings for BbcBackstageDataService");

    return prop;
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#setWorkingDirectory(java.io.File)
   */
  public void setWorkingDirectory(File dataDir) {
    mWorkingDir = dataDir;
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#supportsDynamicChannelGroups()
   */
  public boolean supportsDynamicChannelGroups() {
    return false;
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#supportsDynamicChannelList()
   */
  public boolean supportsDynamicChannelList() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see devplugin.TvDataService#updateTvData(tvdataservice.TvDataUpdateManager, devplugin.Channel[], devplugin.Date, int, devplugin.ProgressMonitor)
   */
  public void updateTvData(TvDataUpdateManager updateManager, Channel[] channelArr, Date startDate, int dateCount,
      ProgressMonitor monitor) throws TvBrowserException {

    int max = channelArr.length;
    
    monitor.setMessage(mLocalizer.msg("loading", "Loading BBC data"));
    monitor.setMaximum(3+dateCount);

    mLog.info(mWorkingDir.getAbsolutePath());
    
    loadBBCData();
    
    monitor.setMessage(mLocalizer.msg("parsing", "Parsing BBC Data"));

    monitor.setValue(3);

    HashMap<Channel,HashMap<Date, MutableChannelDayProgram>> cache = new HashMap<Channel,HashMap<Date, MutableChannelDayProgram>>();

    for (int i=0;i<dateCount;i++) {
      monitor.setValue(3+i);
      StringBuilder date = new StringBuilder();
      date.append(startDate.getYear());
      date.append(addZero(startDate.getMonth()));
      date.append(addZero(startDate.getDayOfMonth()));
      
      for (int v=0;v<max;v++) {
        HashMap<Date, MutableChannelDayProgram> channelCache = cache.get(channelArr[v]);
        if (channelCache == null) {
          channelCache = new HashMap<Date, MutableChannelDayProgram>();
          cache.put(channelArr[v], channelCache);
        }
        Date channeldate = startDate;
        StringBuilder filename = new StringBuilder(date);
        filename.append(channelArr[v].getId());
        mLog.info(filename.toString());
        
        try {

          BbcFileParser bbcparser = new BbcFileParser(channelCache, channelArr[v], channeldate);
          
          bbcparser.parseFile(new File(mWorkingDir, filename.toString()));
        } catch (Exception e) {
          throw new TvBrowserException(getClass(), "error.1", "Error while parsing the Data.", e);
        }
        
      }

      startDate = startDate.addDays(1);
    }

    for (HashMap<Date, MutableChannelDayProgram> channelCache : cache.values()) {
      for (MutableChannelDayProgram mutDayProg : channelCache.values()) {
        updateManager.updateDayProgram(mutDayProg);
      }
    }

  }

  /**
   * Download .tar.gz and extract it into the working directory
   */
  private void loadBBCData() throws TvBrowserException{
    
    mLog.fine("Cleaning Directory");
    
    cleanWorkingDir();
    
    File download = new File(".");
    URL url = null;
    try {
      Date date = new Date();
      
      mLog.fine("Start Downloading BBC Data");
      
      InputStream in = null;
      
      int count = 0;
      do {
        try {
          url = new URL(BASEURL + date.getYear() + addZero(date.getMonth()) + addZero(date.getDayOfMonth()) + ".tar.gz");
          
          mLog.fine("URL : " + url.toString());
          
          in = IOUtilities.getStream(url);
        } catch (Exception e) {
          in = null;
        }
        count++;
        date = date.addDays(-1);
        
      } while ((in == null) && count < 4);
      
      if (in == null) {
        throw new TvBrowserException(getClass(), "error.3", "Downloading file from '{0}' to '{1}' failed", url, download.getAbsolutePath());
      }
      
      mLog.fine("Extracting BBC Data");

      download = new File(mWorkingDir, "file.tar.gz");
      OutputStream out = new FileOutputStream(download);
  
      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
      }
      in.close();
      out.close();
      
      File tar = new File(mWorkingDir, "file.tar");
      IOUtilities.ungzip(download, tar);
      download.delete();
      
      TarInputStream tarfile = new TarInputStream(new FileInputStream(tar));
      TarEntry entry;
      while ((entry = tarfile.getNextEntry()) != null) {
        if (!entry.isDirectory()) {
          String filename = entry.getName();
          filename = filename.substring(filename.lastIndexOf('/')+1);

          File tardown = new File(mWorkingDir, filename);
          OutputStream tarout = new FileOutputStream(tardown);
      
          // Transfer bytes from in to out
          while ((len = tarfile.read(buf)) > 0) {
              tarout.write(buf, 0, len);
          }
          tarout.close();
        }
      }
      tarfile.close();
      tar.delete();
    } catch (Exception e1) {
      throw new TvBrowserException(getClass(), "error.3", "Downloading file from '{0}' to '{1}' failed", url, download.getAbsolutePath(), e1);
    }
    
  }

  /**
   * Clean working directory
   */
  private void cleanWorkingDir() {
    File[] files = mWorkingDir.listFiles();
    if (files != null) {
      for (File file : files) {
        file.delete();
      }
    }
    else {
      mLog.warning("Cannot clean working directory: " + mWorkingDir);
    }
  }

  /**
   * Add one zero if neccessary
   * @param number
   * @return
   */
  private CharSequence addZero(int number) {
    StringBuilder builder = new StringBuilder();
    
    if (number < 10) {
      builder.append('0');
    }
    
    builder.append(Integer.toString(number));
    return builder.toString();
  }

}