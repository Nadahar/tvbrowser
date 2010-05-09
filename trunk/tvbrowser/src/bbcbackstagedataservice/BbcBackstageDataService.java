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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang.StringUtils;

import tvdataservice.MutableChannelDayProgram;
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
  private static final Logger mLog = Logger.getLogger(BbcBackstageDataService.class
      .getName());

  private static final Version VERSION = new Version(3,0);

  private static HashMap<String, String> KNOWN_URLS;
  static {
    KNOWN_URLS = new HashMap<String, String>();
    KNOWN_URLS.put("BBC Radio 1","http://www.bbc.co.uk/radio1/");
    KNOWN_URLS.put("BBC Radio 2","http://www.bbc.co.uk/radio2/");
    KNOWN_URLS.put("BBC Radio 3","http://www.bbc.co.uk/radio3/");
    KNOWN_URLS.put("BBC Radio 4","http://www.bbc.co.uk/radio4/");
    KNOWN_URLS.put("BBC Radio 5 Live","http://www.bbc.co.uk/fivelive/");
    KNOWN_URLS.put("1Xtra","http://www.bbc.co.uk/1xtra/");
    KNOWN_URLS.put("BBC 6 Music","http://www.bbc.co.uk/6music/");
    KNOWN_URLS.put("BBC 7","http://www.bbc.co.uk/bbc7/");
    KNOWN_URLS.put("BBC Asian Network","http://www.bbc.co.uk/asiannetwork/");
    KNOWN_URLS.put("BBC World Service","http://www.bbc.co.uk/worldservice/");
  }

  /**
   * Channelgroup
   */
  private ChannelGroup mBbcChannelGroup = new ChannelGroupImpl("BBC Backstage", "bbcbackstage",
      mLocalizer.msg("desc", "Data from BBC Backstage"), "BBC Backstage");

  /**
   * List of Channels
   */
  private ArrayList<Channel> mChannels = new ArrayList<Channel>();

  /**
   * Working-Directory
   */
  private File mWorkingDir;

  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor) throws TvBrowserException {
    return new ChannelGroup[] { mBbcChannelGroup };
  }

  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
    try {
      ArrayList<Channel> channels = new ArrayList<Channel>();

      monitor.setMessage(mLocalizer.msg("loading", "Loading BBC data"));

      String channelInfo = new String(IOUtilities.loadFileFromHttpServer(new URL("http://www0.rdthdo.bbc.co.uk/cgi-perl/api/query.pl?method=bbc.channel.list&format=simple")), "ISO-8859-9");

      Matcher m = Pattern.compile("<channel channel_id=\"(.*)\" name=\"(.*)\" />", Pattern.MULTILINE).matcher(channelInfo);

      Pattern genrePattern = Pattern.compile("<genre genre_id=\".*\" name=\"(.*)/>", Pattern.MULTILINE);
      Pattern iconPattern = Pattern.compile("<logo url=\"(.*_small.gif)\" type=\"image/gif\" width=\"\\d*\" height=\"\\d*\" />", Pattern.MULTILINE);

      while (m.find()) {
        int categories = Channel.CATEGORY_NONE;

        String details = new String(IOUtilities.loadFileFromHttpServer(new URL("http://www0.rdthdo.bbc.co.uk/cgi-perl/api/query.pl?method=bbc.channel.getInfo&channel_id="+ m.group(1))));

        Matcher ma = genrePattern.matcher(details);

        if (ma.find()) {
          // find out, whether this is radio or TV
          if (ma.group(1).equals("Audio and video")) {
            categories = Channel.CATEGORY_TV;
          }
          else if (ma.group(1).equals("Audio only".toLowerCase())) {
            categories = Channel.CATEGORY_RADIO;
          }
        }

        Icon icon = null;
        ma = iconPattern.matcher(details);
        if (ma.find()) {
          System.out.println(mWorkingDir.getAbsolutePath());
          IOUtilities.download(new URL(ma.group(1)), new File(mWorkingDir, m.group(1) + ".gif"));
          icon = new ImageIcon(new File(mWorkingDir, m.group(1) + ".gif").getAbsolutePath());
        }

        Channel ch = new Channel(this, m.group(2), m.group(1), TimeZone
            .getTimeZone("GMT"), "gb", "(c) BBC", getChannelUrl(m.group(2)), mBbcChannelGroup, icon, categories);
        channels.add(ch);

        mLog.fine("Channel : " + ch.getName() + '{' + ch.getId() + '}');
      }

      mChannels = channels;

      monitor.setMessage(mLocalizer.msg("done", "Done with BBC data"));

      return channels.toArray(new Channel[channels.size()]);
    } catch (IOException ioe) {
      // Handle IOExceptions: things like missing file
      throw new TvBrowserException(getClass(), "error.2", "Problems while loading the Data.", ioe);
    }
  }

  private String getChannelUrl(String channelName) {
    String url = KNOWN_URLS.get(channelName);
    if (url != null) {
      return url;
    }
    return "http://bbc.co.uk";
  }

  public Channel[] getAvailableChannels(ChannelGroup group) {
    return mChannels.toArray(new Channel[mChannels.size()]);
  }

  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[] { mBbcChannelGroup };
  }

  public static Version getVersion() {
    return VERSION;
  }

  public PluginInfo getInfo() {
    return new PluginInfo(BbcBackstageDataService.class, mLocalizer.msg("name","BBC Data"),
        mLocalizer.msg("desc", "Data from BBC Backstage."), "Bodo Tasche");
  }

  public SettingsPanel getSettingsPanel() {
    return null;
  }

  public boolean hasSettingsPanel() {
    return false;
  }

  public void loadSettings(Properties settings) {
    mLog.info("Loading settings in BbcBackstageDataService");

    int numChannels = Integer.parseInt(settings.getProperty("NumberOfChannels", "0"));

    mChannels = new ArrayList<Channel>();

    for (int i=0;i<numChannels;i++){
      String channelName = settings.getProperty("ChannelTitle-" + i, "");
      String channelId = settings.getProperty("ChannelId-" + i, "");
      int categories = Integer.parseInt(settings.getProperty("ChannelCategories-" + i, "0"));

      File iconFile = new File(mWorkingDir, channelId + ".gif");

      Icon icon = null;

      if (iconFile.exists()) {
        icon = new ImageIcon(iconFile.getAbsolutePath());
      }

      Channel ch = new Channel(this, channelName, channelId, TimeZone
          .getTimeZone("GMT+0:00"), "gb", "(c) BBC", getChannelUrl(channelName), mBbcChannelGroup, icon, categories);
      mChannels.add(ch);
      mLog.fine("Channel : " + ch.getName() + '{' + ch.getId() + '}');
    }

    mLog.info("Finished loading settings for BbcBackstageDataService");
  }

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

  public void setWorkingDirectory(File dataDir) {
    mWorkingDir = dataDir;
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

      TarArchiveInputStream tarfile = new TarArchiveInputStream(new FileInputStream(tar));
      ArchiveEntry entry;
      while ((entry = tarfile.getNextEntry()) != null) {
        if (!entry.isDirectory()) {
          String filename = entry.getName();
          filename = StringUtils.substringAfterLast(filename, "/");

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
        if (file.getName().endsWith(".xml")) {
          file.delete();
        }
      }
    }
    else {
      mLog.warning("Cannot clean working directory: " + mWorkingDir);
    }
  }

  /**
   * Add one zero if necessary
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