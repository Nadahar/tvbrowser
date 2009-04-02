/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowserdataservice;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import tvbrowserdataservice.file.ChannelList;
import tvbrowserdataservice.file.SummaryFile;
import util.exc.TvBrowserException;
import util.io.FileFormatException;
import util.io.IOUtilities;
import util.io.Mirror;
import util.io.stream.InputStreamProcessor;
import util.io.stream.StreamUtilities;
import devplugin.Channel;
import devplugin.ProgressMonitor;

/**
 * The ChannelGroup implementation of the TvBrowserDataService
 */
public class ChannelGroup implements devplugin.ChannelGroup {

  private String mID;

  private String[] mMirrorUrlArr;

  private File mDataDir;

  private Channel[] mAvailableChannelArr;

  private Mirror mCurMirror;

  private SummaryFile mSummary;

  private String mName = null;

  private TvBrowserDataService mDataService;

  private String mDescription, mProviderName;

  private HashSet<Channel> mChannels;

  /**
   * Settings for storing changes in ProviderName/Id
   */
  private Properties mSettings;

  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ChannelGroup.class.getName());

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelGroup.class);

  private static final int MAX_META_DATA_AGE = 2;

  /**
   * Creates a new ChannelGroup
   * 
   * @param dataservice The data service of the group.
   * @param id The id of the group.
   * @param name The name of the group.
   * @param description The description for the group.
   * @param provider The provider of the group.
   * @param mirrorUrls The mirror urls of the group.
   * @param settings The properties of the group.
   */
  public ChannelGroup(TvBrowserDataService dataservice, String id, String name, String description, String provider, String[] mirrorUrls, Properties settings) {
    mID = id;
    mName = name;
    mDescription = description;
    mProviderName = provider;
    mDataService = dataservice;
    mMirrorUrlArr = mirrorUrls;
    mChannels = new HashSet<Channel>();
    mDataDir = dataservice.getDataDir();
    mSettings = settings;
  }

  /**
   * Creates a new ChannelGroup
   *
   * @param dataservice The data service of the group.
   * @param id The id of the group.
   * @param mirrorUrls The mirror urls of the group.
   * @param settings The properties of the group.
   */
  public ChannelGroup(TvBrowserDataService dataservice, String id, String[] mirrorUrls, Properties settings) {
    this(dataservice, id, null, null, null, mirrorUrls, settings);
  }


  protected String[] getMirrorArr() {
    return mMirrorUrlArr;
  }

  /**
   * Checks if a channel is a member of this group.
   *
   * @param ch The channel to check.
   * @return True if the channel is a member of this group, false otherwise.
   */
  public boolean isGroupMember(Channel ch) {
    return ch.getGroup() != null && ch.getGroup().getId() != null && ch.getGroup().getId().equalsIgnoreCase(mID);
  }

  /**
   * Sets the data directory.
   *
   * @param dataDir The data directory.
   */
  public void setWorkingDirectory(File dataDir) {
    mDataDir = dataDir;
  }

  /**
   * Add a channel to this group.
   *
   * @param ch The channel to add.
   */
  public void addChannel(Channel ch) {
    mChannels.add(ch);
  }

  /**
   * Get all channels.
   *
   * @return An Iterator with the channels.
   */
  public Iterator<Channel> getChannels() {
    return mChannels.iterator();
  }

  private String getLocaleProperty(Properties prop, String key, String defaultValue) {
    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();
    String result = prop.getProperty(new StringBuilder(key).append('_').append(
        language).toString());
    if (result == null) {
      result = prop.getProperty(new StringBuilder(key).append("_default")
          .toString(), defaultValue);
    }
    return result;

  }

  public String getDescription() {
    if (mDescription != null) {
      return mDescription;
    }

    File file = new File(mDataDir, mID + "_info");
    if (!file.exists()) { return ""; }
    final Properties prop = new Properties();
    // TODO init all props at once
    StreamUtilities.inputStreamIgnoringExceptions(file,
        new InputStreamProcessor() {
          public void process(InputStream input) throws IOException {
            prop.load(input);
          }
        });
    mDescription = getLocaleProperty(prop, "description", "");
    return mDescription;
  }

  public String getName() {
    if (mName != null) { return mName; }

    File file = new File(mDataDir, mID + "_info");
    if (!file.exists()) { return mID; }

    final Properties prop = new Properties();
    try {
      StreamUtilities.inputStream(file, new InputStreamProcessor() {
        public void process(InputStream input) throws IOException {
          prop.load(input);
        }
      });
      Locale locale = Locale.getDefault();
      String language = locale.getLanguage();
      String result = prop.getProperty(language);
      if (result == null) {
        result = prop.getProperty("default", mID);
      }
      return result;

    } catch (IOException e) {
      return mID;
    }

  }


  public String getProviderName() {
    if (mProviderName != null) {
      return mProviderName;
    }
    String providerName = mSettings.getProperty(mID + "_provider");
    if (providerName != null) {
      mProviderName = providerName;
      return providerName;
    }
    File file = new File(mDataDir, mID + "_info");
    if (!file.exists()) { return ""; }
    final Properties prop = new Properties();
    try {
      StreamUtilities.inputStream(file, new InputStreamProcessor() {
        public void process(InputStream input) throws IOException {
          prop.load(input);
        }
      });
      providerName = prop.getProperty("provider",mLocalizer.msg("unknownProvider","unknown"));
      mSettings.setProperty(mID + "_provider", providerName);
      mProviderName = providerName;
      return providerName;
    } catch (IOException e) {
      mLog.log(Level.SEVERE, "Could not read provider name",e);
      return mLocalizer.msg("unknownProvider","unknown");
    }
  }

  @Override
  public String toString() {
    return getName();
  }

  protected void chooseMirrors() throws TvBrowserException {
    // load the mirror list
    Mirror[] serverDefindeMirros = getServerDefinedMirrors();
    Mirror[] mirrorArr = Mirror.loadMirrorList(new File(mDataDir, mID + "_" + Mirror.MIRROR_LIST_FILE_NAME), mMirrorUrlArr, serverDefindeMirros);

    // Get a random Mirror that is up to date
    mCurMirror = Mirror.chooseUpToDateMirror(mirrorArr, null, getName(), mID, ChannelGroup.class, " Please contact the TV data provider for help.");

    mLog.info("Using mirror " + mCurMirror.getUrl());
    // monitor.setMessage(mLocalizer.msg("info.1","Downloading from mirror
    // {0}",mirror.getUrl()));

    // Update the mirrorlist (for the next time)
    updateMetaFile(mCurMirror.getUrl(), mID + "_" + Mirror.MIRROR_LIST_FILE_NAME);

    // Update the channel list
    // NOTE: We have to load the channel list before the programs, because
    // we need it for the programs.
    updateChannelList(mCurMirror, false);

    try {
      mSummary = loadSummaryFile(mCurMirror);
    } catch (Exception exc) {
      mLog.log(Level.WARNING, "Getting summary file from mirror " + mCurMirror.getUrl() + " failed.", exc);
      mSummary = null;
    }
  }

  /**
   * @return The summary file.
   */
  public SummaryFile getSummary() {
    return mSummary;
  }

  /**
   * Get the current mirror.
   *
   * @return The current mirror.
   */
  public Mirror getMirror() {
    return mCurMirror;
  }

  private SummaryFile loadSummaryFile(Mirror mirror) throws IOException, FileFormatException {
    String url = mirror.getUrl() + (mirror.getUrl().endsWith("/") ? "" : "/") + mID + "_" + SummaryFile.SUMMARY_FILE_NAME;

    InputStream stream = null;
    try {
      stream = IOUtilities.getStream(new URL(url));

      SummaryFile summary = new SummaryFile();
      summary.readFromStream(stream, null);

      return summary;
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  protected void setBaseMirror(String[] mirrorUrl) {
    mMirrorUrlArr = mirrorUrl;
  }

  private Mirror[] getServerDefinedMirrors() {
    File groupFile = new File(mDataDir, TvBrowserDataService.CHANNEL_GROUPS_FILENAME);
    Mirror[] mirrorArr = null;

    if (!groupFile.exists()) {
      mLog.info("Group file '"+TvBrowserDataService.CHANNEL_GROUPS_FILENAME+"' does not exist");
    } else {
      BufferedReader in = null;

      try {
        in = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(groupFile), 0x1000), "utf-8"));
        String line = in.readLine();
        while (line != null) {
          String[] s = line.split(";");
          
          if (s.length>=5 && s[0].compareTo(mID) == 0) {
            int n = s.length-4;
            mirrorArr = new Mirror[n];
            
            for(int i = 0; i < n; i++) {
              mirrorArr[i] = new Mirror(s[i+4], 1);
            }
            
            break;
          }

          line = in.readLine();
        }
        in.close();
      } catch (IOException e) {
        mLog.log(Level.SEVERE, "Could not read group list "+TvBrowserDataService.CHANNEL_GROUPS_FILENAME, e);
      }
      finally {
        if(in != null) {
          try {
            in.close();
          }catch(Exception ee) {}
        }
      }
    }

    return mirrorArr;
  }
  
  private boolean needsUpdate(File file) {
    if (!file.exists()) {
      return true;
    } else {
      long minLastModified = System.currentTimeMillis() - (MAX_META_DATA_AGE * 24L * 60L * 60L * 1000L);
      return file.lastModified() < minLastModified;
    }
  }

  private void updateMetaFile(String serverUrl, String metaFileName) throws TvBrowserException {
    File file = new File(mDataDir, metaFileName);
    
    // Download the new file if needed
    if (needsUpdate(file)) {
      String url = serverUrl + "/" + metaFileName;
      mLog.fine("Updating Metafile " + url);
      try {
        IOUtilities.download(new URL(url), file);
      } catch (IOException exc) {
        throw new TvBrowserException(getClass(), "error.1", "Downloading file from '{0}' to '{1}' failed", url, file
                .getAbsolutePath(), exc);
      }
    }
  }

  private void updateChannelList(Mirror mirror, boolean forceUpdate) throws TvBrowserException {
    String fileName = mID + "_" + ChannelList.FILE_NAME;
    File file = new File(mDataDir, fileName + ".new");
    if (forceUpdate || needsUpdate(file)) {
      String url = mirror.getUrl() + (mirror.getUrl().endsWith("/") ? "" : "/") + fileName;
      try {
        IOUtilities.download(new URL(url), file);
        if (file.canRead() && file.length() > 0) {
          // try reading the file
          devplugin.ChannelGroup group = new devplugin.ChannelGroupImpl(mID, getName(), null, getProviderName());
          ChannelList channelList = new ChannelList(group);
          channelList.readFromFile(file, mDataService);
          // ok, we can read it, so use this new file instead of the old
          File oldFile = new File(mDataDir, fileName);
          oldFile.delete();
          file.renameTo(oldFile);
          // Invalidate the channel list
          mAvailableChannelArr = null;
        }
      } catch (Exception exc) {
        throw new TvBrowserException(getClass(), "error.4", "Server has no channel list: {0}", mirror.getUrl(), exc);
      }
    }
  }

  /**
   * Checks and returns the available channels of this group.
   * 
   * @param monitor The progress monitor that is to be used.
   * @return The available channel array.
   * @throws TvBrowserException
   */
  public Channel[] checkForAvailableChannels(ProgressMonitor monitor) throws TvBrowserException {
    // load the mirror list
    Mirror[] serverDefindeMirros = getServerDefinedMirrors();
    Mirror[] mirrorArr = Mirror.loadMirrorList(new File(mDataDir, mID + "_" + Mirror.MIRROR_LIST_FILE_NAME), mMirrorUrlArr, serverDefindeMirros);

    // Get a random Mirror that is up to date
    Mirror mirror = Mirror.chooseUpToDateMirror(mirrorArr, monitor, getName(), mID, ChannelGroup.class, " Please contact the TV data provider for help.");
    mLog.info("Using mirror " + mirror.getUrl());

    // Update the mirrorlist (for the next time)
    updateMetaFile(mirror.getUrl(), mID + "_" + Mirror.MIRROR_LIST_FILE_NAME);

    // Update the groupname file
    updateMetaFile(mirror.getUrl(), mID + "_info");

    // Update the channel list
    updateChannelList(mirror, true);
    return getAvailableChannels();
  }

  /**
   * Gets the list of the channels that are available by this data service.
   * 
   * @return The available channel array.
   */
  public synchronized Channel[] getAvailableChannels() {
    if (mAvailableChannelArr == null) {
      File channelFile = new File(mDataDir, mID + "_" + ChannelList.FILE_NAME);
      if (channelFile.exists()) {
        try {
          devplugin.ChannelGroup group = new devplugin.ChannelGroupImpl(mID, getName(), null, getProviderName());
          ChannelList channelList = new ChannelList(group);
          channelList.readFromFile(channelFile, mDataService);
          mAvailableChannelArr = channelList.createChannelArray();
        } catch (Exception exc) {
          mLog.log(Level.WARNING, "Loading channellist failed: " + channelFile.getAbsolutePath(), exc);
        }
      }

      if (mAvailableChannelArr == null) {
        // There is no channel file or loading failed
        // -> create a list without any channels
        mLog.warning("No channels available for group '"+mID+"' no channellist available?");
        mAvailableChannelArr = new Channel[] {};
      }
    }

    return mAvailableChannelArr;
  }

  public String getId() {
    return mID;
  }

  @Override
  public boolean equals(Object obj) {

    if (obj instanceof devplugin.ChannelGroup) {
      devplugin.ChannelGroup group = (devplugin.ChannelGroup) obj;
      return group.getId().equalsIgnoreCase(mID);
    }
    return false;

  }

  @Override
  public int hashCode() {
    return mID.toLowerCase().hashCode();
  }
  
  /**
   * delete all the files created by this channel group,
   * only to be called on deletion of a channel group
   * @since 2.6
   */
  public void deleteAllFiles() {
    File channelFile = new File(mDataDir, mID + "_" + ChannelList.FILE_NAME);
    if (channelFile.exists()) {
      try {
        channelFile.delete();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    File mirrorFile = new File(mDataDir, mID + "_" + Mirror.MIRROR_LIST_FILE_NAME);
    if (mirrorFile.exists()) {
      try {
        mirrorFile.delete();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    File infoFile = new File(mDataDir, mID + "_info");
    if (infoFile.exists()) {
      try {
        infoFile.delete();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
