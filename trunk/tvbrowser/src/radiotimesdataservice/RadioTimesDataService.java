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
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.ui.Localizer;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ChannelGroupImpl;
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
  private static final Logger mLog = Logger.getLogger(RadioTimesDataService.class
      .getName());

  private static final Version VERSION = new Version(3,0);

  /**
   * Channelgroup
   */
  private ChannelGroup mRadioTimesChannelGroup = new ChannelGroupImpl("radiotimes", "Radio Times", mLocalizer
      .msg("desc", "Data from Radio Times"), "Radio Times");

  /**
   * List of Channels
   */
  private ArrayList<Channel> mChannels = new ArrayList<Channel>();

  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor) throws TvBrowserException {
    return new ChannelGroup[] { mRadioTimesChannelGroup };
  }

  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
    try {
      ArrayList<Channel> channels = new ArrayList<Channel>();

      monitor.setMessage(mLocalizer.msg("loadingChannels", "Loading Radio Times Channel List"));

      // Do the parsing...
      BufferedReader reader = new BufferedReader(new InputStreamReader(IOUtilities.getStream(new URL(BASEURL + "channels.dat"))));

      String line;

      while ((line = reader.readLine()) != null) {
        String[] channel = line.split("\\|");

        // format of a line in this file:
        // 92|BBC1
        if (channel.length == 2) {
          String channelId = channel[0].trim();
          String channelName = channel[1].trim();
          int categories = Channel.CATEGORY_NONE;
          if (StringUtils.containsIgnoreCase(channelName, "TV") || StringUtils.containsIgnoreCase(channelName, "HD")) {
            categories = Channel.CATEGORY_TV;
          }
          Channel ch = new Channel(this, channelName, RADIOTIMES + channelId, TimeZone.getTimeZone("GMT+0:00"), "gb",
              "(c) Radio Times", "http://www.radiotimes.co.uk", mRadioTimesChannelGroup, null, categories);
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

  public Channel[] getAvailableChannels(ChannelGroup group) {
    return mChannels.toArray(new Channel[mChannels.size()]);
  }

  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[] { mRadioTimesChannelGroup };
  }

  public static Version getVersion() {
    return VERSION;
  }

  public PluginInfo getInfo() {
    return new PluginInfo(RadioTimesDataService.class, mLocalizer.msg("name", "Radio Times Data"), mLocalizer.msg("desc", "Data from Radio Times."),
        "Bodo Tasche");
  }

  public SettingsPanel getSettingsPanel() {
    return null;
  }

  public boolean hasSettingsPanel() {
    return false;
  }

  public void loadSettings(Properties settings) {
    mLog.info("Loading settings in RadioTimesDataService");

    int numChannels = Integer.parseInt(settings.getProperty("NumberOfChannels", "0"));

    mChannels = new ArrayList<Channel>();

    TimeZone timeZone = TimeZone.getTimeZone("GMT+0:00");
    for (int i = 0; i < numChannels; i++) {
    	String channelName = settings.getProperty("ChannelTitle-" + i, "");
      String channelId = settings.getProperty("ChannelId-" + i, "");
      Channel ch = new Channel(this, channelName, channelId, timeZone, "gb", "(c) Radio Times", "http://www.radiotimes.co.uk", mRadioTimesChannelGroup);
      mChannels.add(ch);
      mLog.fine("Channel : " + ch.getName() + "{" + ch.getId() + "}");
    }

    mLog.info("Finished loading settings for RadioTimesBackstageDataService");
  }

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
    mLog.info("Finished storing settings for RadioTimesDataService");

    return prop;
  }

  public void setWorkingDirectory(File dataDir) {
    // not used
  }

  public boolean supportsDynamicChannelGroups() {
    return false;
  }

  public boolean supportsDynamicChannelList() {
    return true;
  }

  public void updateTvData(TvDataUpdateManager updateManager, Channel[] channelArr, Date startDate, int dateCount,
      ProgressMonitor monitor) throws TvBrowserException {
    // Check for connection
    if (!updateManager.checkConnection()) {
      return;
    }

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
    monitor.setMessage("");
  }

}
