/*
 * DVBViewerDataService.java
 * Copyright (C) 2007 Ullrich Pollaehne (pollaehne@users.sourceforge.net)
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
 *     $Date: $
 *   $Author: $
 * $Revision: $
 */
package dvbviewerdataservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;
import devplugin.TvBrowserSettings;
import devplugin.Version;
import dvbviewer.DVBViewerCOM;
import dvbviewer.DVBViewerChannel;


/**
 * Read EPG Data from DVBViewer Pro
 * <p>
 * Long description for DVBViewerDataService.
 *
 * @author pollaehne
 * @version $Revision: $
 */
public class DVBViewerDataService extends AbstractTvDataService {

  /** Translator */
  private static final Localizer localizer = Localizer.getLocalizerFor(DVBViewerDataService.class);

  /** version of this provider */
  private static final Version version = new Version(0, 20, false);

  /** the name of the channel file stored in the data directory */
  private static final String CHANNELFILENAME = "channellist";

  /** the channel list compressed with GZip */
  private static final String COMPRESSEDCHANNELLIST = CHANNELFILENAME + ".gz";

  /** Logger */
  private static java.util.logging.Logger logger = Logger.getLogger(DVBViewerDataService.class.getName());

  /** data directory */
  private File workingDir;

  /** Channelgroup */
  private ChannelGroup channelGroup = new DVBViewerChannelGroup(localizer.msg("desc", "EPG Data from DVBViewer Pro"));
  private ChannelGroup [] groups = {channelGroup};

  /** List of Channels */
  private ArrayList<Channel> channels = new ArrayList<Channel>();

  /** container for EPG update settings */
  private Settings epgUpdateSettings = new Settings();

  /**
   * Settings for EPG update
   * <p>
   * Settings that define the behaviour of the EPG update
   * in DVBViewer
   */
  public static final class Settings {
    /** name of the updateEPG setting */
    static final String UPDATEEPG = "updateEPG";
    /** name of the fetchTime setting */
    static final String FETCHTIME = "fetchTime";
    /** name of the timeBeforeRecording setting */
    static final String TIMEBEFORERECORDING = "timeBeforeRecording";

    /** should the EPG of DVBViewer be updated? */
    boolean updateEPG = false;
    /** time in seconds to get EPG from a channel */
    int fetchTime = 10;
    /** minimum time before a recording to update the EPG */
    int timeBeforeRecording = 60;
  }

  /**
   *
   */
  public DVBViewerDataService() {
  // nothing to do
  }


  /**
   * @see devplugin.TvDataService#checkForAvailableChannelGroups(devplugin.ProgressMonitor)
   */
  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor)
          throws TvBrowserException {
    return groups;
  }


  /**
   * @see devplugin.TvDataService#checkForAvailableChannels(devplugin.ChannelGroup, devplugin.ProgressMonitor)
   */
  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor)
          throws TvBrowserException {
    if (! channelGroup.equals(group)) {
      return null;
    }

    monitor.setMessage(localizer.msg("loadingChannels", "Loading DVBViewer Channel List"));

    boolean stopviewer = false;
    int progress = 0;
    BufferedWriter writer = null;

    try {
      // start DVBViewer if necessary
      stopviewer = startDvbViewer();

      DVBViewerCOM dvb = DVBViewerCOM.getInstance();

      // clear the old data
      channels.clear();
      dvb.clearChannels();

      List<DVBViewerChannel> dvbchannels = dvb.getChannels(monitor);
      progress = dvbchannels.size();

      File channelFile = new File(workingDir, COMPRESSEDCHANNELLIST);
      writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(channelFile)), "UTF-8"));

      // process the channels
      for (DVBViewerChannel ch : dvbchannels) {
        monitor.setValue(progress++);

        writer.append(ch.getSerializedChannel());
        writer.newLine();

        // create the TV Browser channel and store it in the list
        String id = addChannel(ch);
        logger.fine("Channel : " + ch.getName() + " {" + id + "}");
      }
    } catch (TvBrowserException e) {
      throw e;
    } catch (Exception e) {
      throw new TvBrowserException(getClass(), "channelreaderror", "Reading channels failed", e);
    } finally {
      if (null != writer) {
        try {
          writer.close();
        }
        catch (IOException e) {
          logger.log(Level.SEVERE, "Error while closing channel list file", e);
          e.printStackTrace();
        }
      }
      if (stopviewer) {
        DVBViewerCOM.getInstance().stopDVBViewer();
      }
    }

    monitor.setMessage(localizer.msg("done", "Done with DVBViewer Channel List"));

    return channels.toArray(new Channel[channels.size()]);
  }


  /**
   * @see devplugin.TvDataService#getAvailableChannels(devplugin.ChannelGroup)
   */
  public Channel[] getAvailableChannels(ChannelGroup group) {
    if (! channelGroup.equals(group)) {
      return null;
    }

    return channels.toArray(new Channel[channels.size()]);
  }


  /**
   * @see devplugin.TvDataService#getAvailableGroups()
   */
  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[] { channelGroup };
  }


  /**
   * @see devplugin.TvDataService#getInfo()
   */
  public PluginInfo getInfo() {
    return new PluginInfo(DVBViewerDataService.class,
                          localizer.msg("name", "DVBViewer EPG"),
                          localizer.msg("desc", "EPG Data from DVBViewer Pro."),
                          "Ullrich Poll\u00E4hne");
  }


  /**
   * @see devplugin.TvDataService#getSettingsPanel()
   */
  public SettingsPanel getSettingsPanel() {
    return new DVBViewerSettingsPanel(epgUpdateSettings);
  }


  /**
   * @see devplugin.TvDataService#hasSettingsPanel()
   */
  public boolean hasSettingsPanel() {
    return true;
  }


  /**
   * @see devplugin.TvDataService#setWorkingDirectory(java.io.File)
   */
  public void setWorkingDirectory(File dir) {
    workingDir = dir;
  }

  public static Version getVersion() {
    return version;
  }


  /**
   * @see devplugin.TvDataService#loadSettings(java.util.Properties)
   */
  public void loadSettings(Properties settings) {
    logger.info("Loading settings in DVBViewerDataService");

    String epgupd = settings.getProperty(Settings.UPDATEEPG);
    if (null != epgupd) {
      epgUpdateSettings.updateEPG = Boolean.parseBoolean(epgupd);
    }
    epgupd = settings.getProperty(Settings.FETCHTIME);
    if (null != epgupd) {
      epgUpdateSettings.fetchTime = Integer.parseInt(epgupd);
    }
    epgupd = settings.getProperty(Settings.TIMEBEFORERECORDING);
    if (null != epgupd) {
      epgUpdateSettings.timeBeforeRecording = Integer.parseInt(epgupd);
    }

    // get the channel list
    File channelfile = new File(workingDir, COMPRESSEDCHANNELLIST);
    if (channelfile.exists()) {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(channelfile)), "UTF-8"));
        String line = reader.readLine();
        List<DVBViewerChannel> list = new ArrayList<DVBViewerChannel>();
        while (null != line) {
          DVBViewerChannel ch = new DVBViewerChannel(line);
          list.add(ch);
          addChannel(ch);

          line = reader.readLine();
        }

        DVBViewerCOM.getInstance().setChannels(list);
      }
      catch (IOException e) {
        logger.log(Level.SEVERE, "Error while reading channel list file", e);
      }
      catch (TvBrowserException e) {
        logger.log(Level.SEVERE, "Error while creating DVBViewer instance", e);
      }
      finally {
        if (null != reader) {
          try {
            reader.close();
          }
          catch (IOException e) {
            logger.log(Level.SEVERE, "unable to close channellist file", e);
          }
        }
      }
    }


    logger.info("Finished loading settings for DVBViewerDataService");
  }


  /**
   * @see devplugin.TvDataService#storeSettings()
   */
  public Properties storeSettings() {
    logger.info("Storing settings for DVBViewerDataService");

    Properties prop = new Properties();

    prop.setProperty(Settings.UPDATEEPG, String.valueOf(epgUpdateSettings.updateEPG));
    prop.setProperty(Settings.FETCHTIME, String.valueOf(epgUpdateSettings.fetchTime));
    prop.setProperty(Settings.TIMEBEFORERECORDING, String.valueOf(epgUpdateSettings.timeBeforeRecording));

    logger.info("Finished storing settings for DVBViewerDataService");

    return prop;
  }


  /**
   * @see devplugin.TvDataService#supportsDynamicChannelGroups()
   */
  public boolean supportsDynamicChannelGroups() {
    return false;
  }


  /**
   * @see devplugin.TvDataService#supportsDynamicChannelList()
   */
  public boolean supportsDynamicChannelList() {
    return true;
  }


  /**
   * @see devplugin.TvDataService#updateTvData(tvdataservice.TvDataUpdateManager, devplugin.Channel[], devplugin.Date, int, devplugin.ProgressMonitor)
   */
  public void updateTvData(TvDataUpdateManager updateManager,
          Channel[] channelArr, Date startDate, int dateCount, ProgressMonitor monitor) throws TvBrowserException {
    int channelCount = channelArr.length;

    monitor.setMaximum(channelCount);
    monitor.setValue(0);
    monitor.setMessage(localizer.msg("parsing", "Parsing DVBViewer EPG Data"));

    boolean stopviewer = false;
    try {
      stopviewer = startDvbViewer();

      DVBViewerCOM dvbViewer = DVBViewerCOM.getInstance();
      logger.finer("Loading DVBViewer Channellist");
      List<DVBViewerChannel> chnllst = dvbViewer.getChannels(monitor);
      List<String> transponderIDs = new ArrayList<String>();
      List<DVBViewerChannel> epgUpdateChannels = new ArrayList<DVBViewerChannel>();

      int result = 0;
      if (epgUpdateSettings.updateEPG) {
        monitor.setMessage(localizer.msg("epgupd", "Updating DVBViewer EPG"));

        for (int i = 0; i < channelCount; ++i) {
          for (DVBViewerChannel channel : chnllst) {
            String id = getIDString(channel);
            if (!id.equals(channelArr[i].getId())) {
              continue;
            }
            String transponderID = channel.getTransponderID();
            if (transponderIDs.contains(transponderID)) {
              continue;
            }
            transponderIDs.add(transponderID);
              epgUpdateChannels.add(channel);
            }
          }

        result = dvbViewer.tuneChannelsforEPG(epgUpdateChannels, epgUpdateSettings.fetchTime,
                                              epgUpdateSettings.timeBeforeRecording, monitor);
      }

      if (0 == result) {
        final TvBrowserSettings tvBrowserSettings = getPluginManager().getTvBrowserSettings();
        int eod = tvBrowserSettings.getProgramTableEndOfDay();
        int sod = tvBrowserSettings.getProgramTableStartOfDay();

        monitor.setMessage(localizer.msg("parsing", "Parsing DVBViewer EPG Data"));

        logger.finer("Getting EPG data from " + startDate + " for " + dateCount + " days");
        for (int i = 0; i < channelCount; ++i) {
          monitor.setValue(i);
          for (DVBViewerChannel channel : chnllst) {
            String id = getIDString(channel);
            if (!id.equals(channelArr[i].getId())) {
              continue;
            }

            DVBViewerEPGParser parser = new DVBViewerEPGParser(channelArr[i], dvbViewer, channel, sod, eod);
            parser.parse(updateManager, startDate, dateCount);
          }
        }
      }
    } catch (TvBrowserException e) {
      throw e;
    } catch (Exception e) {
      throw new TvBrowserException(getClass(), "epgparseerror", "Reading Data failed", e);
    } finally {
      if (stopviewer) {
        DVBViewerCOM.getInstance().stopDVBViewer();
      }
    }

    logger.finer("Processing done");
    monitor.setMessage("");
  }


  /**
   * @return true if DVBViewer was started
   *         false if DVBViewer is already running
   * @throws TvBrowserException if DVBViewer cannot be started
   */
  private boolean startDvbViewer() throws TvBrowserException {
    logger.finer("Loading DVBViewer");
    try {
      DVBViewerCOM dvbViewer = DVBViewerCOM.getInstance();
      if (!dvbViewer.isDVBViewerActive()) {
        if (!dvbViewer.startDVBViewer()) {
          throw new TvBrowserException(getClass(), "dvbstarterror", "Starting DVBViewer failed");
        }

        return true;
      }
    } catch (NoClassDefFoundError e) {
      throw new TvBrowserException(getClass(), "dvbstarterror", "Starting DVBViewer failed", e);
    } catch (IOException e) {
      throw new TvBrowserException(getClass(), "dvbstarterror", "Starting DVBViewer failed", e);
    }

    return false;
  }


  private String addChannel(DVBViewerChannel ch) {
    String name = ch.getName();
    String id = getIDString(ch);

    // if EPG display is disabled for this channel in DVBViewer do not show this channel
    if (!ch.isDisplayEPG()) {
      logger.finer("No EPG display for Channel : " + name + "{" + id + "}");
      return id;
    }

    // do we have an EPG ID?
    int epgid = ch.getEPGID();
    if (-1 == epgid) {
      logger.finer("No EPG ID for Channel : " + name + "{" + id + "}");
      return id;
    }

    // process the categories
    int categories = Channel.CATEGORY_NONE;
    if (ch.isRadio()) {
      categories |= Channel.CATEGORY_RADIO;
    } else {
      categories |= Channel.CATEGORY_TV | Channel.CATEGORY_DIGITAL;
    }
    if (ch.isEncrypted()) {
      categories |= Channel.CATEGORY_PAY_TV;
    }

    channels.add(new Channel(this, name, id, TimeZone.getDefault(), "de",
                             "(c) by " + name + "/DVBViewer", null, channelGroup, null, categories));

    return id;
  }


  /**
   * Process the channelID to return a readable channel ID
   * than can be used for filenames also
   *
   * @param channelID the original ID
   * @return the modified ID
   */
  static final String getIDString(DVBViewerChannel ch) {

    StringBuilder buffer = new StringBuilder(64);
    buffer.append(ch.getRoot());
    buffer.append('^');
    buffer.append(ch.getID().replace('|', '^'));


    return buffer.toString();
  }
}
