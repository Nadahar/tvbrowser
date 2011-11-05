/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package bbcdataservice;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.io.stream.BufferedReaderProcessor;
import util.io.stream.StreamUtilities;
import util.ui.html.HTMLTextHelper;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ChannelGroupImpl;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public final class BBCDataService extends AbstractTvDataService {

  private static final String COPYRIGHT = "(c) BBC";
  private static final String COUNTRY = "gb";
  private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT+0:00");
  private static final String CHANNEL_CATEGORIES = "ChannelCategories-";
  private static final String CHANNEL_TITLE = "ChannelTitle-";
  private static final String CHANNEL_ID = "ChannelId-";
  private static final String CHANNEL_WEBPAGE = "ChannelWebPage-";
  private static final String NUMBER_OF_CHANNELS = "NumberOfChannels";
  private static final String PROGRAMMES_URL = "http://www.bbc.co.uk";
  private static final String CHANNEL_LIST = "http://www.tvbrowser.org/mirrorlists/BBCDataService_channellist.gz";
  private static final boolean IS_STABLE = false;
  private static final Version mVersion = new Version(3, 11, 0, IS_STABLE);

  /**
   * created lazily on first access
   */
  private PluginInfo mPluginInfo = null;
  private File mWorkingDir;
  private ArrayList<Channel> mChannels = new ArrayList<Channel>();
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(BBCDataService.class);

  private static ChannelGroup CHANNEL_GROUP = new ChannelGroupImpl("bbcprogrammes", mLocalizer.msg("group.name",
      "BBC programmes"), mLocalizer.msg("group.description", "BBC programmes data"), mLocalizer.msg("group.provider",
      "BBC programmes"));

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "BBC Data Service");
      final String desc = mLocalizer.msg("description", "Loads BBC program data.");
      mPluginInfo = new PluginInfo(BBCDataService.class, name, desc, "Michael Keppler");
    }

    return mPluginInfo;
  }

  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor arg0) throws TvBrowserException {
    return getAvailableGroups();
  }

  public Channel[] checkForAvailableChannels(ChannelGroup channelGroup, final ProgressMonitor progress)
      throws TvBrowserException {
    
    progress.setMessage(mLocalizer.msg("search.index", "Reading BBC programmes index"));
    mChannels.addAll(getChannelListFromFile(CHANNEL_LIST));

    return mChannels.toArray(new Channel[mChannels.size()]);
  }

  public Channel[] getAvailableChannels(ChannelGroup channelGroup) {
    return mChannels.toArray(new Channel[mChannels.size()]);
  }

  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[] { CHANNEL_GROUP };
  }

  public SettingsPanel getSettingsPanel() {
    return null;
  }

  public boolean hasSettingsPanel() {
    return false;
  }

  public void setWorkingDirectory(File dir) {
    mWorkingDir = dir;
  }

  public void loadSettings(Properties settings) {
    int numChannels = Integer.parseInt(settings.getProperty(NUMBER_OF_CHANNELS, "0"));

    mChannels = new ArrayList<Channel>();

    for (int i = 0; i < numChannels; i++) {
      String channelName = settings.getProperty(CHANNEL_TITLE + i, "");
      String channelId = settings.getProperty(CHANNEL_ID + i, "");
      String channelPage = settings.getProperty(CHANNEL_WEBPAGE + i, "");
      int categories = Integer.parseInt(settings.getProperty(CHANNEL_CATEGORIES + i,
          String.valueOf(Channel.CATEGORY_NONE)));

      Channel ch = new Channel(this, channelName, channelId, TIME_ZONE, COUNTRY, COPYRIGHT, channelPage, CHANNEL_GROUP,
          null, categories);
      mChannels.add(ch);
    }
  }

  public Properties storeSettings() {
    Properties prop = new Properties();

    prop.setProperty(NUMBER_OF_CHANNELS, Integer.toString(mChannels.size()));
    int max = mChannels.size();
    for (int i = 0; i < max; i++) {
      Channel ch = mChannels.get(i);
      prop.setProperty(CHANNEL_ID + i, ch.getId());
      prop.setProperty(CHANNEL_TITLE + i, ch.getName());
      prop.setProperty(CHANNEL_CATEGORIES + i, Integer.toString(ch.getCategories()));
      prop.setProperty(CHANNEL_WEBPAGE + i, ch.getWebpage());
    }
    return prop;
  }

  public boolean supportsDynamicChannelGroups() {
    return false;
  }

  public boolean supportsDynamicChannelList() {
    return true;
  }

  public void updateTvData(final TvDataUpdateManager updateManager, final Channel[] channels, final Date startDate,
      final int days, final ProgressMonitor monitor) throws TvBrowserException {
    // // Check for connection
    // if (!updateManager.checkConnection()) {
    // return;
    // }
    monitor.setMessage(mLocalizer.msg("update", "Updating BBC data"));
    monitor.setMaximum(channels.length);
    int progress = 0;
    for (Channel channel : channels) {
      HashMap<Date, MutableChannelDayProgram> dayPrograms = new HashMap<Date, MutableChannelDayProgram>();
      monitor.setValue(progress++);
      for (int i = 0; i < days; i++) {
        Date date = startDate.addDays(i);
        String year = String.valueOf(date.getYear());
        String month = String.valueOf(date.getMonth());
        String day = String.valueOf(date.getDayOfMonth());
        String schedulePath = "/" + year + "/" + month + "/" + day + ".xml";
        String url = channel.getWebpage() + schedulePath;
        File file = new File(mWorkingDir, "bbc.xml");
        try {
          IOUtilities.download(new URL(url), file);
        } catch (MalformedURLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        boolean continueWithNextDay = false;
        try {
          continueWithNextDay = BBCProgrammesParser.parse(dayPrograms, file, channel, date);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        file.delete();
        if (!continueWithNextDay) {
          break;
        }
      }
      // store the received programs
      for (MutableChannelDayProgram dayProgram : dayPrograms.values()) {
        updateManager.updateDayProgram(dayProgram);
      }
    }
  }
  
  private ArrayList<Channel> getChannelListFromFile(String url) {

    final ArrayList<Channel> channels = new ArrayList<Channel>();

    BufferedInputStream stream = null;
    try {
      URL sourceUrl = new URL(url);
      stream = new BufferedInputStream(sourceUrl.openStream());

      InputStream gIn = IOUtilities.openSaveGZipInputStream(stream);
      BufferedReader reader = new BufferedReader(new InputStreamReader(gIn, "ISO-8859-15"));

      int lineCount = 1;

      String line = reader.readLine();
      String[] tokens;
      while (line != null) {
        tokens = line.split(";");

        String country = null, timezone = null, id = null, name = null, copyright = null, webpage = null, iconUrl = null, categoryStr = null, unescapedname = null;
        try {
          country = tokens[0];
          timezone = tokens[1];
          id = tokens[2];
          name = tokens[3];
          copyright = tokens[4];
          webpage = tokens[5];
          iconUrl = tokens[6];
          categoryStr = tokens[7];
          if (tokens.length > 8) {
            unescapedname = name;
            name = StringEscapeUtils.unescapeHtml(tokens[8]);
          }

        } catch (ArrayIndexOutOfBoundsException e) {
          // ignore
        }

        int categories = Channel.CATEGORY_NONE;
        if (categoryStr != null) {
          try {
            categories = Integer.parseInt(categoryStr);
          } catch (NumberFormatException e) {
            categories = Channel.CATEGORY_NONE;
          }
        }
        Channel channel = new Channel(BBCDataService.this, name, id, TimeZone.getTimeZone(timezone), country, copyright,
            webpage, CHANNEL_GROUP, null, categories, unescapedname);


        channels.add(channel);
        lineCount++;
        line = reader.readLine();
      } 

      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
      }
    }
    return channels;

  }

  private ArrayList<Channel> getRegionChannels(final String channelId, final String channelName, final String webSite,
      final int category, final ProgressMonitor progress) {
    final ArrayList<Channel> channels = new ArrayList<Channel>();
    try {
      File regionsFile = new File(mWorkingDir, "regions");
      IOUtilities.download(new URL(PROGRAMMES_URL + webSite), regionsFile);
      StreamUtilities.bufferedReader(regionsFile, new BufferedReaderProcessor() {

        public void process(BufferedReader reader) throws IOException {
          String line;
          while ((line = reader.readLine()) != null) {
            if (line.contains(webSite)) {
              line = StringUtils.substringAfter(line, "a href");
              String regionId = StringUtils.substringBetween(line, "schedules/", "\"");
              if ("today".equalsIgnoreCase(regionId) || "tomorrow".equalsIgnoreCase(regionId)
                  || "yesterday".equalsIgnoreCase(regionId)) {
                continue;
              }
              if (StringUtils.isNotEmpty(regionId) && !regionId.contains("/")) {
                String regionName = StringUtils.substringBetween(line, ">", "</a");
                regionName = HTMLTextHelper.convertHtmlToText(regionName);
                if ("Schedule".equalsIgnoreCase(regionName) || "View full schedule".equalsIgnoreCase(regionName)) {
                  continue;
                }
                String webSite = StringUtils.substringBetween(line, "=\"", "\"");
                boolean found = false;
                for (Channel channel : channels) {
                  if (channel.getWebpage().equalsIgnoreCase(PROGRAMMES_URL + webSite)) {
                    found = true;
                    break;
                  }
                }
                if (!found) {
                  String localName = channelName + " (" + regionName + ")";
                  String localId = channelId + "." + regionId;
                  if (StringUtils.isNotEmpty(localName) && StringUtils.isNotEmpty(localId)) {
                    progress.setMessage(mLocalizer.msg("search.channel", "Found channel: {0}", localName));
                    Channel channel = new Channel(BBCDataService.this, localName, localId, TIME_ZONE, COUNTRY, COPYRIGHT,
                        PROGRAMMES_URL + webSite, CHANNEL_GROUP, null, category);
                    channels.add(channel);
                  }
                }
              }
            }
          }
        }
      });
      regionsFile.delete();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    return channels;
  }

}
