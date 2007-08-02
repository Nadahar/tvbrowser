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
package radiotimesdataservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TimeZone;

import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.ui.Localizer;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;
import devplugin.Version;

/**
 * This Dataservice collects Data from
 * 
 * http://xmltv.radiotimes.com/xmltv/channels.dat
 * 
 * and
 * 
 * http://xmltv.radiotimes.com/xmltv/92.dat
 * 
 * @author bodum
 */
public class RadioTimesDataService extends AbstractTvDataService {
  /** Prefix for Channels */
  static final String RADIOTIMES = "RADIOTIMES";

  /** Base-URL */
  static final String BASEURL = "http://xmltv.radiotimes.com/xmltv/";

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(RadioTimesDataService.class);

  /**
   * Logger
   */
  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(RadioTimesDataService.class
      .getName());

  /**
   * Channelgroup
   */
  private ChannelGroup mRadioTimesChannelGroup = new RadioTimesChannelGroup("Radio Times", "radiotimes", mLocalizer
      .msg("desc", "Data from Radio Times"), "Radio Times");

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
   * 
   * @see devplugin.TvDataService#checkForAvailableChannelGroups(devplugin.ProgressMonitor)
   */
  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor) throws TvBrowserException {
    return new ChannelGroup[] { mRadioTimesChannelGroup };
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#checkForAvailableChannels(devplugin.ChannelGroup,
   *      devplugin.ProgressMonitor)
   */
  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
    try {
      ArrayList<Channel> channels = new ArrayList<Channel>();

      monitor.setMessage(mLocalizer.msg("loadingChannels", "Loading Radio Times Channel List"));

      // Do the parsing...
      BufferedReader reader = new BufferedReader(new InputStreamReader(IOUtilities.getStream(new URL(BASEURL + "channels.dat"))));
      
      String line;

      while ((line = reader.readLine()) != null) {
        String[] channel = line.split("\\|");

        if (channel.length == 2) {
          Channel ch = new Channel(this, channel[1], RADIOTIMES + channel[0], TimeZone.getTimeZone("GMT+0:00"), "gb",
              "(c) Radio Times", "http://www.radiotimes.co.uk", mRadioTimesChannelGroup);
          channels.add(ch);
          mLog.fine("Channel : " + ch.getName() + "{" + ch.getId() + "}");
        }

      }

      reader.close();

      mChannels = channels;

      monitor.setMessage(mLocalizer.msg("done", "Done with Radio Times data"));

      return channels.toArray(new Channel[channels.size()]);
    } catch (Exception e) {
      throw new TvBrowserException(getClass(), "error.1", "Downloading Channellist failed", e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#getAvailableChannels(devplugin.ChannelGroup)
   */
  public Channel[] getAvailableChannels(ChannelGroup group) {
    return mChannels.toArray(new Channel[mChannels.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#getAvailableGroups()
   */
  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[] { mRadioTimesChannelGroup };
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#getInfo()
   */
  public PluginInfo getInfo() {
    return new PluginInfo(mLocalizer.msg("name", "Radio Times Data"), mLocalizer.msg("desc", "Data from Radio Times."),
        "Bodo Tasche", new Version(0, 31));
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#getSettingsPanel()
   */
  public SettingsPanel getSettingsPanel() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#hasSettingsPanel()
   */
  public boolean hasSettingsPanel() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#loadSettings(java.util.Properties)
   */
  public void loadSettings(Properties settings) {
    mLog.info("Loading settings in RadioTimesDataService");

    int numChannels = Integer.parseInt(settings.getProperty("NumberOfChannels", "0"));

    mChannels = new ArrayList<Channel>();

    TimeZone timeZone = TimeZone.getTimeZone("GMT+0:00");
    for (int i = 0; i < numChannels; i++) {
    	Channel ch = new Channel(this, settings.getProperty("ChannelTitle-" + i, ""), settings.getProperty("ChannelId-"
          + i, ""), timeZone, "gb", "(c) Radio Times", "http://www.radiotimes.co.uk",
          mRadioTimesChannelGroup);
      mChannels.add(ch);
      mLog.fine("Channel : " + ch.getName() + "{" + ch.getId() + "}");
    }

    mLog.info("Finished loading settings for RadioTimesBackstageDataService");
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#storeSettings()
   */
  public Properties storeSettings() {
    mLog.info("Storing settings for RadioTimesDataService");

    Properties prop = new Properties();

    prop.setProperty("NumberOfChannels", Integer.toString(mChannels.size()));
    int max = mChannels.size();
    for (int i = 0; i < max; i++) {
      Channel ch = mChannels.get(i);
      prop.setProperty("ChannelId-" + i, ch.getId());
      prop.setProperty("ChannelTitle-" + i, ch.getName());
    }
    mLog.info("Finnished storing settings for RadioTimesDataService");

    return prop;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#setWorkingDirectory(java.io.File)
   */
  public void setWorkingDirectory(File dataDir) {
    mWorkingDir = dataDir;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#supportsDynamicChannelGroups()
   */
  public boolean supportsDynamicChannelGroups() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#supportsDynamicChannelList()
   */
  public boolean supportsDynamicChannelList() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.TvDataService#updateTvData(tvdataservice.TvDataUpdateManager,
   *      devplugin.Channel[], devplugin.Date, int, devplugin.ProgressMonitor)
   */
  public void updateTvData(TvDataUpdateManager updateManager, Channel[] channelArr, Date startDate, int dateCount,
      ProgressMonitor monitor) throws TvBrowserException {

    int max = channelArr.length;
    Date endDate = startDate.addDays(dateCount-1); 

    monitor.setMaximum(max);
    monitor.setMessage(mLocalizer.msg("parsing", "Parsing Radio Times Data"));

    try {
      for (int i = 0; i < max; i++) {
        monitor.setValue(i);
        RadioTimesFileParser parser = new RadioTimesFileParser(channelArr[i]);
        parser.parse(updateManager, endDate);
      }
    } catch (Exception e) {
      throw new TvBrowserException(getClass(), "error.2", "Downloading Data failed", e);
    }
  }

}