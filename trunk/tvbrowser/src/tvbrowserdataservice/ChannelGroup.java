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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import tvbrowserdataservice.file.ChannelList;
import tvbrowserdataservice.file.FileFormatException;
import tvbrowserdataservice.file.Mirror;
import tvbrowserdataservice.file.SummaryFile;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import devplugin.Channel;
import devplugin.Date;
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

  private static final int MAX_UP_TO_DATE_CHECKS = 10;

  private static final int MAX_LAST_UPDATE_DAYS = 5;

  /** List of blocked Servers */
  private static ArrayList<String> BLOCKEDSERVERS = new ArrayList<String>();
  /** Mirror-Download Running?*/
  private static boolean mMirrorDownloadRunning = true;
  /** Exception on downloading in Thread */
  private static boolean mDownloadException = false;
  /** Data of Mirror-Download*/
  private static byte[] mMirrorDownloadData = null;

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
  public Iterator getChannels() {
    return mChannels.iterator();
  }

  private String getLocaleProperty(Properties prop, String key, String defaultValue) {
    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();
    String result = prop.getProperty(new StringBuffer(key).append('_').append(language).toString());
    if (result == null) {
      result = prop.getProperty(new StringBuffer(key).append("_default").toString(), defaultValue);
    }
    return result;

  }

  public String getDescription() {
    if (mDescription != null) {
      return mDescription;
    }

    File file = new File(mDataDir, mID + "_info");
    if (!file.exists()) { return ""; }
    Properties prop = new Properties();
    try {
    	  // TODO init all props at once
      prop.load(new BufferedInputStream(new FileInputStream(file), 0x400));
      mDescription = getLocaleProperty(prop, "description", "");
      return mDescription;
    } catch (IOException e) {
      return "";
    }

  }

  public String getName() {
    if (mName != null) { return mName; }

    File file = new File(mDataDir, mID + "_info");
    if (!file.exists()) { return mID; }

    Properties prop = new Properties();
    try {
      prop.load(new BufferedInputStream(new FileInputStream(file), 0x400));
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
    Properties prop = new Properties();
    try {
      prop.load(new BufferedInputStream(new FileInputStream(file), 0x400));
      providerName = prop.getProperty("provider",mLocalizer.msg("unknownProvider","unknown"));
      mSettings.setProperty(mID + "_provider", providerName);
      mProviderName = providerName;
      return providerName;
    } catch (IOException e) {
      mLog.log(Level.SEVERE, "Could not read provider name",e);
      return mLocalizer.msg("unknownProvider","unknown");
    }
  }

  public String toString() {
    return getName();
  }

  protected void chooseMirrors() throws TvBrowserException {
    // load the mirror list
    Mirror[] mirrorArr = loadMirrorList();

    // Get a random Mirror that is up to date
    mCurMirror = chooseUpToDateMirror(mirrorArr, null, getName(), mID);

    mLog.info("Using mirror " + mCurMirror.getUrl());
    // monitor.setMessage(mLocalizer.msg("info.1","Downloading from mirror
    // {0}",mirror.getUrl()));

    // Update the mirrorlist (for the next time)
    updateMetaFile(mCurMirror.getUrl(), mID + "_" + Mirror.MIRROR_LIST_FILE_NAME);

    // Update the channel list
    // NOTE: We have to load the channel list before the programs, because
    // we need it for the programs.
    updateChannelList(mCurMirror);

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

    if (!groupFile.exists())
      mLog.info("Group file '"+TvBrowserDataService.CHANNEL_GROUPS_FILENAME+"' does not exist");
    else {
      BufferedReader in = null;

      try {
        in = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(groupFile), 0x1000), "utf-8"));
        String line = in.readLine();
        while (line != null) {
          String[] s = line.split(";");
          
          if (s.length>=5 && s[0].compareTo(mID) == 0) {
            int n = s.length-4;
            mirrorArr = new Mirror[n];
            
            for(int i = 0; i < n; i++)
              mirrorArr[i] = new Mirror(s[i+4], 1);
            
            break;
          }

          line = in.readLine();
        }
        in.close();
      } catch (IOException e) {
        mLog.log(Level.SEVERE, "Could not read group list "+TvBrowserDataService.CHANNEL_GROUPS_FILENAME, e);
      }
      finally {
        if(in != null)
          try {
            in.close();
          }catch(Exception ee) {}
      }
    }

    return mirrorArr;
  }

  private Mirror[] loadMirrorList() {
    File file = new File(mDataDir, mID + "_" + Mirror.MIRROR_LIST_FILE_NAME);

    try {
      ArrayList<Mirror> mirrorList = new ArrayList<Mirror>(Arrays.asList(Mirror.readMirrorListFromFile(file)));

      for (int i=0;i<mMirrorUrlArr.length;i++) {
        Mirror basemirror = mirrorList.get(i);
        if (!mirrorList.contains(basemirror)) {
          mirrorList.add(basemirror);
        }
      }

      Mirror[] groupmirrorArr = getServerDefinedMirrors();

      if(groupmirrorArr != null)
        for(int i = 0; i < groupmirrorArr.length; i++)
          if(!mirrorList.contains(groupmirrorArr[i]))
            mirrorList.add(groupmirrorArr[i]);

      return mirrorList.toArray(new Mirror[mirrorList.size()]);
    } catch (Exception exc) {
      ArrayList<Mirror> mirrorList = new ArrayList<Mirror>();

      for (int i = 0; i < mMirrorUrlArr.length; i++) {
        if (!BLOCKEDSERVERS.contains(getServerBase(mMirrorUrlArr[i])) && mMirrorUrlArr[i] != null) {
          mirrorList.add(new Mirror(mMirrorUrlArr[i]));
        }
      }
      
      return mirrorList.toArray(new Mirror[mirrorList.size()]);
    }
  }

  private static Mirror chooseMirror(Mirror[] mirrorArr, Mirror oldMirror, String name) throws TvBrowserException {
    Mirror[] oldMirrorArr = mirrorArr;
    
    /* remove the old mirror from the mirrorlist */
    if (oldMirror != null) {
      ArrayList<Mirror> mirrors = new ArrayList<Mirror>();
      for (int i = 0; i < mirrorArr.length; i++) {
        if (oldMirror != mirrorArr[i]) {
          mirrors.add(mirrorArr[i]);
        }
      }
      mirrorArr = new Mirror[mirrors.size()];
      mirrors.toArray(mirrorArr);
    }

    // Get the total weight
    int totalWeight = 0;
    for (int i = 0; i < mirrorArr.length; i++) {
      totalWeight += mirrorArr[i].getWeight();
    }

    // Choose a weight
    int chosenWeight = (int) (Math.random() * totalWeight);

    // Find the chosen mirror
    int currWeight = 0;
    for (int i = 0; i < mirrorArr.length; i++) {
      currWeight += mirrorArr[i].getWeight();
      if (currWeight > chosenWeight) {
        Mirror mirror = mirrorArr[i];
        // Check whether this is the old mirror or Mirror is Blocked
        if (((mirror == oldMirror) || BLOCKEDSERVERS.contains(getServerBase(mirror.getUrl()))) && (mirrorArr.length > 1)) {
          // We chose the old mirror -> chose another one
          return chooseMirror(mirrorArr, oldMirror, name);
        } else {
          return mirror;
        }
      }
    }

    // We didn't find a mirror? This should not happen -> throw exception
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < oldMirrorArr.length; i++) {
      buf.append(oldMirrorArr[i].getUrl()).append("\n");
    }

    throw new TvBrowserException(ChannelGroup.class, "error.2", "No mirror found\ntried following mirrors: ", name, buf.toString());
  }

  private static boolean mirrorIsUpToDate(Mirror mirror, String id) throws TvBrowserException {
    // Load the lastupdate file and parse it
    final String url = mirror.getUrl() + (mirror.getUrl().endsWith("/") ? "" : "/") + id + "_lastupdate";
    Date lastupdated;
    mMirrorDownloadRunning = true;
    mMirrorDownloadData = null;
    mDownloadException = false;
    
    mLog.info("Loading MirrorDate from " + url);
    
    new Thread(new Runnable() {
      public void run() {
        try {
          mMirrorDownloadData = IOUtilities.loadFileFromHttpServer(new URL(url), 60000);
        } catch (Exception e) {
          mDownloadException = true;
        }
        mMirrorDownloadRunning = false;
      };
    }).start();

    int num = 0;
    // Wait till second Thread is finished or 15000 ms reached
    while ((mMirrorDownloadRunning) && (num < 150)) {
      num++;
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    if (mMirrorDownloadRunning || mMirrorDownloadData == null || mDownloadException) {
      mLog.info("Server " + url +" is down!");
      return false;
    }
    
    try {
      // Parse is. E.g.: '2003-10-09 11:48:45'
      String asString = new String(mMirrorDownloadData);    
      int year = Integer.parseInt(asString.substring(0, 4));
      int month = Integer.parseInt(asString.substring(5, 7));
      int day = Integer.parseInt(asString.substring(8, 10));
      lastupdated = new Date(year, month, day);
      
      mLog.info("Done !");
      
      return lastupdated.compareTo(new Date().addDays(-MAX_LAST_UPDATE_DAYS)) >= 0;
    }catch(NumberFormatException parseException) {
      mLog.info("The file on the server has the wrong format!");
    }

    return false;
  }

  protected static Mirror chooseUpToDateMirror(Mirror[] mirrorArr, ProgressMonitor monitor, String name, String id) throws TvBrowserException {

    // Choose a random Mirror
    Mirror mirror = chooseMirror(mirrorArr, null, name);
    if (monitor != null) {
      monitor.setMessage(mLocalizer.msg("info.3", "Try to connect to mirror {0}", mirror.getUrl()));
    }
    // Check whether the mirror is up to date and available
    for (int i = 0; i < MAX_UP_TO_DATE_CHECKS; i++) {
      try {
        if (mirrorIsUpToDate(mirror, id)) {
          break;
        } else {
          // This one is not up to date -> choose another one
          Mirror oldMirror = mirror;
          mirror = chooseMirror(mirrorArr, mirror, name);
          mLog.info("Mirror " + oldMirror.getUrl() + " is out of date or down. Choosing " + mirror.getUrl() + " instead.");
          if (monitor != null) {
            monitor.setMessage(mLocalizer.msg("info.4", "Mirror {0} is out of date or down. Choosing {1}", oldMirror.getUrl(), mirror
                    .getUrl()));
          }
        }
      } catch (TvBrowserException exc) {
        String blockedServer = getServerBase(mirror.getUrl()); 
        BLOCKEDSERVERS.add(blockedServer);
        mLog.info("Server blocked : " + blockedServer);
        
        if(mirrorArr.length == 1 && mirrorArr[0].equals(mirror))
          throw new TvBrowserException(ChannelGroup.class, "noUpToDateServer", "The mirror {0} is out of date or down and no other mirror is available. Please contact the tv data provider for help.", mirror.getUrl());
        
        // This one is not available -> choose another one
        Mirror oldMirror = mirror;
        mirror = chooseMirror(mirrorArr, mirror, name);
        mLog.info("Mirror " + oldMirror.getUrl() + " is not available. Choosing " + mirror.getUrl() + " instead.");
        if (monitor != null) {
          monitor.setMessage(mLocalizer.msg("info.5", "Mirror {0} is not available. Choosing {1}", oldMirror.getUrl(), mirror
                  .getUrl()));
        }
      }
    }

    // Return the mirror
    return mirror;   
  }

  /**
   * Get the Server-Domain of the Url
   * @param url Url to fetch the Server-Domain from
   * @return Server-Domain 
   */
  private static String getServerBase(String url) {
    if (url.startsWith("http://"))
      url = url.substring(7);
    if (url.indexOf('/') >= 0)
      url = url.substring(0, url.indexOf('/'));
    
    return url;
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
      mLog.fine("Updateing Metafile " + url);
      try {
        IOUtilities.download(new URL(url), file);
      } catch (IOException exc) {
        throw new TvBrowserException(getClass(), "error.1", "Downloading file from '{0}' to '{1}' failed", url, file
                .getAbsolutePath(), exc);
      }
    }
  }

  private void updateChannelList(Mirror mirror) throws TvBrowserException {
    updateChannelList(mirror, false);
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
    Mirror[] mirrorArr = loadMirrorList();

    // Get a random Mirror that is up to date
    Mirror mirror = chooseUpToDateMirror(mirrorArr, monitor, getName(), mID);
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
  public Channel[] getAvailableChannels() {
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

  public boolean equals(Object obj) {

    if (obj instanceof devplugin.ChannelGroup) {
      devplugin.ChannelGroup group = (devplugin.ChannelGroup) obj;
      return group.getId().equalsIgnoreCase(mID);
    }
    return false;

  }


  public int hashCode() {
    return mID.toLowerCase().hashCode();
  }

  /**
   * Reset the List of banned Servers
   */
  public static void resetBannedServers() {
    BLOCKEDSERVERS.clear();
  }


}
