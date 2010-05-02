/*
 * Settings.java
 * Copyright (C) 2006 Ullrich Pollaehne (pollaehne@users.sourceforge.net)
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
package dvbplugin;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import dvbplugin.dvbviewer.DvbViewerSetup;
import dvbplugin.dvbviewer.ProcessHandler;

/**
 * TODO short description for Settings.
 * <p>
 * Long description for Settings.
 *
 * @author Pollaehne
 */
public final class Settings {
  /** name of DVBViewer's usermode.ini */
  private static final String DVBVIEWER_USERMODE_INI = "usermode.ini";

  /** property key of DVBViewer Path */
  private static final String PROPS_VIEWER_PATH = "viewerPath";

  /** property key of DVBViewer executable name */
  private static final String PROPS_VIEWER_EXE_NAME = "viewerExeName";

  /** property key of time to add to a scheduled recording */
  private static final String PROPS_RECORD_AFTER = "recordAfter";

  /** property key of time to start a scheduled recording ahead of time */
  private static final String PROPS_RECORD_BEFORE = "recordBefore";

  /** property key of last Y position of the recordings dialog */
  private static final String PROPS_LASTY_OF_RECORDINGS_PANEL = "lastYofRecordingsPanel";

  /** property key of last X position of the recordings dialog */
  private static final String PROPS_LASTX_OF_RECORDINGS_PANEL = "lastXofRecordingsPanel";

  /** property key of switch to mark scheduled recordings in TV-Browser's list */
  private static final String PROPS_MARK_RECORDINGS = "markRecordings";

  /** property key prefix of channel assignments (TV-Browser<->DVBViewer) */
  private static final String PROPS_CHANNEL_PREFIX = "channel.";

  /** Translator */
  private static final Localizer localizer = Localizer.getLocalizerFor(Settings.class);

  /** Value of viewer type for DVBViewer Pro */
  static final int VIEWERTYPE_PRO = 0;

  /** Value of viewer type for DVBViewer GE */
  static final int VIEWERTYPE_GE = 1;

  /** value of viewer mode for config data in application directory */
  static final int VIEWERMODE_APP = 0;

  /** value of viewer mode for config data in user application data directory */
  static final int VIEWERMODE_USER = 1;

  /** value of viewer mode for config data in allusers application data directory */
  static final int VIEWERMODE_ALLUSER = 2;

  /** the name of the DVBTaskScheduler executable */
  static final String SCHEDULER_EXE_NAME = "Scheduler.exe";

  /** used as initial value for string properties like viewerPath */
  public static final String EMPTYSTRING = "";

  private static final Logger logger = Logger.getLogger(Settings.class.getName());

  /** the one and only instance of this class */
  private static Settings instance;

  /** a logger that could be used */
  // private static final Logger logger = Logger.getLogger(dvbplugin.Settings.class.getName());

  /** text for an unknown (invalid) channel name */
  private static final String UNKNOWN = "UNKNOWN";

  /** an empty channel object to be returned if there is no match on a search */
  static final TvbDvbVChannel UNKNOWN_CHANNEL = new TvbDvbVChannel(UNKNOWN, UNKNOWN, UNKNOWN,
                                                                   UNKNOWN, UNKNOWN);

  /** the DVBViewer UserMode setting */
  private int viewerMode = VIEWERMODE_APP;

  /** the type of DVBViewer (Pro, GE) */
  private int viewerType;

  /** the path to DVBViewer */
  private String viewerPath = EMPTYSTRING;

  /** the name of the DVBViewer executable */
  private String viewerExeName = EMPTYSTRING;

  /**
   * the path to the file containing scheduled recordings (timers.xml for Pro,
   * setup.ini for GE). this path depends on the <code>viewerMode</code>
   */
  private String viewerTimersPath = EMPTYSTRING;

  /**
   * contains the last known x coordinate (upper left) of the recordings
   * panel/dialog
   */
  private int lastXofRecordingsPanel;

  /**
   * contains the last known y coordinate (upper left) of the recordings
   * panel/dialog
   */
  private int lastYofRecordingsPanel;

  /** should we visually mark scheduled recordings in the TV-Browser list? */
  private boolean markRecordings;

  /** the amount of minutes to start a recording ahead of time */
  private int recordBefore;

  /** the amount of minutes to stop a recording after scheduled time */
  private int recordAfter;

  /** the default action to be executed at recording start */
  private int defRecAction;

  /** the default action to be executed after recording end */
  private int defAfterAction;

  /** the default for AV disable */
  private boolean defAVdisabled;

  /** the default vaule for using dvbviewer task scheduler */
  private boolean useScheduler;

  /** the list of assigned channels */
  private List<TvbDvbVChannel> channels;

  /** a reference to the recordings marker */
  private Marker marker;


  /**
   * Returns the one and only instance of the Settings class
   *
   * @return the Settings instance
   */
  public static final Settings getSettings() {
    if (null == instance) {
      instance = new Settings();
    }

    return instance;
  }


  /**
   * Stops others from creating instances
   */
  private Settings() {
    channels = new ArrayList<TvbDvbVChannel>();
  }


  /**
   * Removes all currently loaded settings
   */
  static final void clear() {
    instance = null;
  }


  final void setMarker(Marker markerRef) {
    marker = markerRef;
  }


  final Marker getMarker() {
    if (null == marker) {
      marker = new Marker();
    }

    return marker;
  }


  /**
   * Checks if the path to DVBViewer is defined
   *
   * @return true if path to DVBViewer is set false otherwise
   */
  final boolean isValid() {
    return viewerPath != null && !EMPTYSTRING.equals(viewerPath);
  }


  /**
   * Called by the host-application during start-up.
   *
   * @param pluginSettings The settings for this plugin (May be empty).
   * @return true if path to DVBViewer is defined false otherwise
   */
  final boolean loadSettings(Properties settings) {
    // logger.info("start reading properties");

    int oldRecBefore = 0;
    int oldRecAfter = 0;

    // try to get the markRecordings value
    String temp = settings.getProperty(PROPS_MARK_RECORDINGS);
    if (null != temp) {
      setMarkRecordings(temp);
    } else {
      // try to get the old name
      setMarkRecordings(settings.getProperty("Mark"));
    }

    // try to get the lastXofRecordingsPanel value
    temp = settings.getProperty(PROPS_LASTX_OF_RECORDINGS_PANEL);
    if (null != temp) {
      setLastXofRecordingsPanel(temp);
    } else {
      setLastXofRecordingsPanel(settings.getProperty("Anzeige_X"));
    }

    // try to get the lastYofRecordingsPanel value
    temp = settings.getProperty(PROPS_LASTY_OF_RECORDINGS_PANEL);
    if (null != temp) {
      setLastYofRecordingsPanel(temp);
    } else {
      setLastYofRecordingsPanel(settings.getProperty("Anzeige_Y"));
    }

    // try to get the recordBefore value
    temp = settings.getProperty(PROPS_RECORD_BEFORE);
    if (null != temp) {
      setRecordBefore(temp);
    } else {
      setRecordBefore(settings.getProperty("StartOffset"));
    }
    oldRecBefore = getRecordBefore();

    // try to get the recordAfter value
    temp = settings.getProperty(PROPS_RECORD_AFTER);
    if (null != temp) {
      setRecordAfter(temp);
      recordAfter = Integer.parseInt(temp);
    } else {
      setRecordAfter(settings.getProperty("EndeOffset"));
    }
    oldRecAfter = getRecordAfter();

    // try to get the viewerExeName
    temp = settings.getProperty(PROPS_VIEWER_EXE_NAME);
    if (null != temp) {
      setViewerExeName(temp);
    } else {
      setViewerExeName(settings.getProperty("DVBExecute"));
    }

    // try to get the viewerPath
    temp = settings.getProperty(PROPS_VIEWER_PATH);
    if (null != temp) {
      setViewerPath(temp);
    } else {
      setViewerPath(settings.getProperty("DVBPath"));
    }


    // get the default values from DVBViewer's setup.xml
    Map<String, String> m = new HashMap<String, String>();
    m.put(DvbViewerSetup.DEF_REC_ACTION, "");
    m.put(DvbViewerSetup.DEF_AFTER_RECORD, "");
    m.put(DvbViewerSetup.EPGBEFORE, "");
    m.put(DvbViewerSetup.EPGAFTER, "");
    m.put(DvbViewerSetup.DEF_REC_AVDISABLED, "");
    m.put(DvbViewerSetup.USESCHEDULER, "");
    DvbViewerSetup.getEntries(viewerTimersPath, m);
    setDefRecAction(m.get(DvbViewerSetup.DEF_REC_ACTION));
    setDefAfterAction(m.get(DvbViewerSetup.DEF_AFTER_RECORD));
    setRecordBefore(m.get(DvbViewerSetup.EPGBEFORE));
    setRecordAfter(m.get(DvbViewerSetup.EPGAFTER));
    setDefAvDisabled("1".equals(m.get(DvbViewerSetup.DEF_REC_AVDISABLED)));
    setSchedulerUsed("1".equals(m.get(DvbViewerSetup.USESCHEDULER)));

    // now we have the recbefore and -after values from DVBViewer
    if (0 != oldRecBefore) {
      setRecordBefore(oldRecBefore);
    }
    if (0 != oldRecAfter) {
      setRecordAfter(oldRecAfter);
    }

    // check for the channel properties
    File propsFile = new File(ProcessHandler.DVBVIEWERPLUGIN_USER_PATH, "DVBPluginChannels.properties");
    if (!propsFile.exists()) {
      // check the old location
      propsFile = new File( tvbrowser.core.Settings.getUserDirectoryName(), "DVBPluginChannels.properties");
      File newProps = new File(ProcessHandler.DVBVIEWERPLUGIN_USER_PATH, "DVBPluginChannels.properties");
      newProps.getParentFile().mkdirs();
      propsFile.renameTo(newProps);
      propsFile = newProps;
    }

    // read the assigned channels
    Properties channelProps = null;
    for (Entry element : settings.entrySet()) {
      Entry<String,String> entry = element;
      String key = entry.getKey();
      String tvbName = null;
      if (key.startsWith(PROPS_CHANNEL_PREFIX)) {
        // found a new name
        tvbName = key.substring(PROPS_CHANNEL_PREFIX.length());
      } else if (key.startsWith("sender.")) {
        // found an old name
        tvbName = key.substring("sender.".length());
      }

      if (null != tvbName) {
        String tmp[] = entry.getValue().split("\\|");
        String sid = tmp[0];
        String dvbvName = tmp[1];
        String aid = null;
        String ttype = null;
        if (2 < tmp.length) {
          aid = tmp[2];
          ttype = tmp[3];
        } else {
          if (null == channelProps) {
            InputStream inStream = null;
            try {
              channelProps = new Properties();
              inStream = new BufferedInputStream(new FileInputStream(propsFile));
              channelProps.load(inStream);
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Unable to read the properties file "
                      + propsFile.getAbsolutePath(), e);
            } finally {
              if (null != inStream) {
                try {
                  inStream.close();
                } catch (IOException e) {
                  // at least we tried to be nice
                  logger.log(Level.SEVERE, "Unable to close the properties file "
                          + propsFile.getAbsolutePath(), e);
                }
              }
            }

            if (null != channelProps) {
              String value = channelProps.getProperty(dvbvName);
              tmp = value.split("\\|");
              sid = tmp[0];
              aid = tmp[1];
              ttype = tmp[2];
            }
          }
        }

        channels.add(new TvbDvbVChannel(tvbName, dvbvName, sid, aid, ttype));
      }
    }
    Collections.sort(channels);

    return !EMPTYSTRING.equals(viewerPath);
  }


  /**
   * Called by the host-application during shut-down.
   *
   * @return The settings for this plugin
   */
  final Properties storeSettings() {
    // store the values into our TV-Browser properties file
    Properties props = new Properties();
    props.setProperty(PROPS_MARK_RECORDINGS, String.valueOf(markRecordings));
    props.setProperty(PROPS_LASTX_OF_RECORDINGS_PANEL, String.valueOf(lastXofRecordingsPanel));
    props.setProperty(PROPS_LASTY_OF_RECORDINGS_PANEL, String.valueOf(lastYofRecordingsPanel));
    if (!EMPTYSTRING.equals(viewerPath)) {
      props.setProperty(PROPS_VIEWER_PATH, viewerPath);
    }
    if (!EMPTYSTRING.equals(viewerExeName)) {
      props.setProperty(PROPS_VIEWER_EXE_NAME, viewerExeName);
    }

    for (Iterator<TvbDvbVChannel> it = channels.iterator(); it.hasNext();) {
      TvbDvbVChannel channel = it.next();
      StringBuilder value = new StringBuilder(64);
      value.append(channel.getDVBChannel().serviceID).append('|');
      value.append(channel.getDVBChannel().name).append('|');
      value.append(channel.getDVBChannel().audioID).append('|');
      value.append(channel.getDVBChannel().tunerType);
      props.setProperty(PROPS_CHANNEL_PREFIX + channel.getTvBrowserName(), value.toString());
    }

/* disabled until ui refactoring
    // store the values into DVBViewer's setup.xml
    HashMap<String, String> items = new HashMap<String, String>();
    items.put(DvbViewerSetup.EPGBEFORE, String.valueOf(getRecordBefore()));
    items.put(DvbViewerSetup.EPGAFTER, String.valueOf(getRecordAfter()));
    items.put(DvbViewerSetup.DEF_REC_ACTION, String.valueOf(getDefRecAction()));
    items.put(DvbViewerSetup.DEF_AFTER_RECORD, String.valueOf(getDefAfterAction()));
    items.put(DvbViewerSetup.DEF_REC_AVDISABLED, isDefAvDisabled() ? "1" : "0");
    DvbViewerSetup.setEntries(viewerTimersPath, items);
*/

    return props;
  }


  /**
   * @return Returns value of markRecordings.
   */
  final boolean isMarkRecordings() {
    return markRecordings;
  }


  /**
   * @param mark New value for markRecordings.
   */
  final void setMarkRecordings(boolean mark) {
    markRecordings = mark;
  }


  /**
   * @param mark New value for markRecordings.
   */
  final void setMarkRecordings(String mark) {
    if (null == mark || 0 == mark.length()) { return; }

    markRecordings = Boolean.valueOf(mark).booleanValue();
  }


  /**
   * @return Returns value of lastXofRecordingsPanel.
   */
  final int getLastXofRecordingsPanel() {
    return lastXofRecordingsPanel;
  }


  /**
   * @param lastX New value for lastXofRecordingsPanel.
   */
  final void setLastXofRecordingsPanel(int lastX) {
    lastXofRecordingsPanel = lastX;
  }


  /**
   * @param lastX New value for lastXofRecordingsPanel.
   */
  final void setLastXofRecordingsPanel(String lastX) {
    if (null == lastX || 0 == lastX.length()) { return; }

    lastXofRecordingsPanel = Math.abs(Integer.parseInt(lastX));
  }


  /**
   * @return Returns value of lastYofRecordingsPanel.
   */
  final int getLastYofRecordingsPanel() {
    return lastYofRecordingsPanel;
  }


  /**
   * @param lastY New value for lastYofRecordingsPanel.
   */
  final void setLastYofRecordingsPanel(int lastY) {
    lastYofRecordingsPanel = lastY;
  }


  /**
   * @param lastY New value for lastYofRecordingsPanel.
   */
  final void setLastYofRecordingsPanel(String lastY) {
    if (null == lastY || 0 == lastY.length()) { return; }

    lastYofRecordingsPanel = Math.abs(Integer.parseInt(lastY));
  }


  /**
   * @return Returns value of recordAfter.
   */
  final int getRecordAfter() {
    return recordAfter;
  }


  /**
   * @param after New value for recordAfter.
   */
  final void setRecordAfter(int after) {
    recordAfter = after;
  }


  /**
   * @param after New value for recordAfter.
   */
  final void setRecordAfter(String after) {
    if (null == after || 0 == after.length()) { return; }

    recordAfter = Math.abs(Integer.parseInt(after));
  }


  /**
   * @return Returns value of recordBefore.
   */
  final int getRecordBefore() {
    return recordBefore;
  }


  /**
   * @param before New value for recordBefore.
   */
  final void setRecordBefore(int before) {
    recordBefore = before;
  }


  /**
   * @param before New value for recordBefore.
   */
  final void setRecordBefore(String before) {
    if (null == before || 0 == before.length()) { return; }

    recordBefore = Math.abs(Integer.parseInt(before));
  }


  /**
   * @param recAction the default action on start of recording
   */
  final void setDefRecAction(int recAction) {
    defRecAction = recAction;
  }


  /**
   * @param recAction the default action on start of recording
   */
  final void setDefRecAction(String recAction) {
    if (null == recAction || 0 == recAction.length()) { return; }

    defRecAction = Math.abs(Integer.parseInt(recAction));
  }


  /**
   * @return the default action on start of recording
   */
  final int getDefRecAction() {
    return defRecAction;
  }


  /**
   * @param afterAction the default action after end of recording
   */
  final void setDefAfterAction(int afterAction) {
    defAfterAction = afterAction;
  }


  /**
   * @param afterAction the default action after end of recording
   */
  final void setDefAfterAction(String afterAction) {
    if (null == afterAction || 0 == afterAction.length()) { return; }

    defAfterAction = Math.abs(Integer.parseInt(afterAction));
  }


  /**
   * @return the default action after end of recording
   */
  final int getDefAfterAction() {
    return defAfterAction;
  }


  /**
   * @param avDisabled the default for AV disabling during recording
   */
  final void setDefAvDisabled(boolean avDisabled) {
    defAVdisabled = avDisabled;
  }

  /**
   * @return the default for AV disabling during recording
   */
  final boolean isDefAvDisabled() {
    return defAVdisabled;
  }

  /**
   * @param schedulerUsed use dvbviewer task scheduler for recordings?
   */
  final void setSchedulerUsed(boolean schedulerUsed) {
    useScheduler = schedulerUsed;
  }

  /**
   * @return is dvbviewer task scheduler used for recordings?
   */
  public final boolean isSchedulerUsed() {
    return useScheduler;
  }


  /**
   * @return Returns value of viewerPath.
   */
  final String getViewerPath() {
    return viewerPath;
  }


  /**
   * @param path New value for viewerPath.
   */
  final void setViewerPath(String path) {
    if (null == path || 0 == path.length()) { return; }

    viewerPath = path;

    // (re-)read the DVBViewer config
    readViewerConf();
  }


  /**
   * @return Returns value of viewerExeName.
   */
  public final String getViewerExeName() {
    return viewerExeName;
  }


  /**
   * @param name New value for viewerExeName.
   */
  final void setViewerExeName(String name) {
    if (null == name || 0 == name.length()) { return; }

    viewerExeName = name;
  }


  /**
   * @return Returns the complete path to DVBViewer
   */
  public final String getViewerExePath() {
    if (viewerPath.endsWith(File.separator)) { return viewerPath + viewerExeName; }

    return viewerPath + File.separator + viewerExeName;
  }


  /**
   * @return Returns the complete path to DVBTaskScheduler
   */
  public final String getSchedulerExePath() {
    if (viewerPath.endsWith(File.separator)) { return viewerPath + SCHEDULER_EXE_NAME; }

    return viewerPath + File.separator + SCHEDULER_EXE_NAME;
  }


  /**
   * @return Returns value of viewerTimersPath.
   */
  final String getViewerTimersPath() {
    return viewerTimersPath;
  }


  /**
   * @param path New value for viewerTimersPath.
   */
  final void setViewerTimersPath(String path) {
    if (null == path || 0 == path.length()) { return; }

    viewerTimersPath = path;
  }


  /**
   * @return true if viewer type is DVBViewer Pro.
   */
  final boolean isPro() {
    return viewerType == VIEWERTYPE_PRO;
  }


  /**
   * @return true if viewer type is DVBViewer GE.
   */
  final boolean isGE() {
    return viewerType == VIEWERTYPE_GE;
  }


  /**
   * @return Returns value of viewerMode.
   */
  final int getViewerMode() {
    return viewerMode;
  }


  /**
   * @param mode New value for viewerMode.
   */
  private final void setViewerMode(String mode) {
    if (null == mode || 0 == mode.length()) { return; }

    switch (Integer.parseInt(mode)) {
      case VIEWERMODE_USER:
        viewerMode = VIEWERMODE_USER;
        break;
      case VIEWERMODE_ALLUSER:
        viewerMode = VIEWERMODE_ALLUSER;
        break;
      case VIEWERMODE_APP:
      default:
        viewerMode = VIEWERMODE_APP;
        break;
    }
  }


  /**
   * Reads information about the DVBViewer configuration
   */
  private void readViewerConf() {
    String viewerRoot = "DVBViewer Pro";
    File f = new File(viewerPath, DVBVIEWER_USERMODE_INI);
    if (f.exists()) {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader(f));
        String line = reader.readLine();
        while (line != null) {
          if (line.startsWith("UserMode")) {
            String ini[] = line.split("=");
            setViewerMode(ini[1]);
          } else if (line.startsWith("Root")) {
            String ini[] = line.split("=");
            viewerRoot = ini[1];
          }
          line = reader.readLine();
        }
      } catch (Exception e) {
        ErrorHandler.handle(localizer.msg("err_read_usermode",
        "Could not determine installation mode of DVBViewer"),
        e);
      } finally {
        if (null != reader) {
          try {
            reader.close();
          } catch (IOException e) {
            // at least we tried it
            logger.log(Level.SEVERE, "Could not close the file " + f.getAbsolutePath(), e);
          }
        }
      }
    } else {
      logger.log(Level.INFO, "The DVBViewer usermode ini file {0} does not exist," +
                             " assuming {1} as the configuration data directory.",
                 new Object[] {f.getAbsolutePath(), viewerPath});
    }

    switch (viewerMode) {
      case 0:
        setViewerTimersPath(viewerPath);
        break;
      case 1: {
        StringBuilder temp = new StringBuilder(260);
        temp.append(System.getenv("APPDATA"));
        temp.append(File.separatorChar);
        temp.append(viewerRoot);
        temp.append(File.separatorChar);
        viewerTimersPath = temp.toString();
        break;
      }
      case 2: {
        StringBuilder temp = new StringBuilder(260);
        String os = System.getProperty("os.name", "");
        if (-1 != os.indexOf("Vista")) {
          temp.append(System.getenv("ALLUSERSPROFILE"));
        } else {
          String appdata = System.getenv("APPDATA");
          temp.append(System.getenv("ALLUSERSPROFILE"));
          temp.append(appdata.substring(appdata.lastIndexOf(File.separatorChar)));
        }
        temp.append(File.separatorChar);
        temp.append(viewerRoot);
        viewerTimersPath = temp.toString();
        break;
      }
      default:
        logger.log(Level.WARNING, "Do not know how to handle UserMode={0}", String.valueOf(viewerMode));
      return;
    }

    // check the type of DVBViewer
    File setupXmlFile = new File(viewerTimersPath, "setup.xml");
    if (!setupXmlFile.exists()) {
      // no setup.xml => GE
      viewerType = VIEWERTYPE_GE;
    }
  }


  /**
   * Returns the count of assignments in the channel assignment list
   *
   * @return count of channel assignments
   */
  final int getChannelCount() {
    return channels.size();
  }


  /**
   * Returns an iterator to iterate over the list of channel assignments
   *
   * @return iterator over channel assignment list
   */
  final Iterator<TvbDvbVChannel> getChannelIterator() {
    return channels.iterator();
  }


  /**
   * Return a channel assigment that contains the TV-Browser channel identified
   * by <code>name</code>
   *
   * @param name the TV-Browser channel name
   * @return the channel assignment if found otherwise the unknown channel will
   *         be returned
   */
  public final TvbDvbVChannel getChannelByTVBrowserName(String name) {
    if (null == name) { return UNKNOWN_CHANNEL; }

    for (Iterator<TvbDvbVChannel> it = channels.iterator(); it.hasNext();) {
      TvbDvbVChannel ch = it.next();
      if (name.equals(ch.tvBrowserName)) { return ch; }
    }
    return UNKNOWN_CHANNEL;
  }


  /**
   * Return a channel assigment that contains the DVBViewer channel identified
   * by <code>name</code>
   *
   * @param name the DVBViewer channel name
   * @return the channel assignment if found otherwise the unknown channel will
   *         be returned
   */
  final TvbDvbVChannel getChannelByDVBViewerName(String name) {
    if (null == name) { return UNKNOWN_CHANNEL; }

    for (Iterator<TvbDvbVChannel> it = channels.iterator(); it.hasNext();) {
      TvbDvbVChannel ch = it.next();
      if (name.equals(ch.getDVBChannel().name)) { return ch; }
    }
    return UNKNOWN_CHANNEL;
  }


  /**
   * Remove a channel from the list of channel assignments The channel
   * assignment to be removed is identified by the TV-Browser channel name
   *
   * @param name the TV-Browser channel name
   */
  public final void removeChannelByTVBrowserName(String name) {
    if (null == name) { return; }

    for (Iterator<TvbDvbVChannel> it = channels.iterator(); it.hasNext();) {
      TvbDvbVChannel ch = it.next();
      if (name.equals(ch.tvBrowserName)) {
        it.remove();
        break;
      }
    }
  }


  /**
   * Add a channel to the list of channel assignments
   *
   * @param tvBrowser the TV-Browser channel name
   * @param dvbViewer the DVBViewer channel name
   * @param serviceID the DVBViewer service id
   * @param audioID the DBViewer audio id
   * @param tunerType the DVBViewer tuner type
   */
  public final void addChannel(String tvBrowser, DVBViewerChannel dvbchannel) {
    channels.add(new TvbDvbVChannel(tvBrowser, dvbchannel));
    Collections.sort(channels);
  }


  /**
   * Add a channel to the list of channel assignments
   *
   * @param tvBrowser the TV-Browser channel name
   * @param dvbViewer the DVBViewer channel name
   * @param serviceID the DVBViewer service id
   * @param audioID the DBViewer audio id
   * @param tunerType the DVBViewer tuner type
   */
  public final void addChannel(String tvBrowser, String dvbViewer, String serviceID, String audioID,
          String tunerType) {
    channels.add(new TvbDvbVChannel(tvBrowser, dvbViewer, serviceID, audioID, tunerType));
    Collections.sort(channels);
  }


  /**
   * Simple container for a TV-Browser to DVBViewer channel assignment
   *
   * @author pollaehne
   */
  public static final class TvbDvbVChannel implements Comparable<TvbDvbVChannel> {
    /** TV-Browser's name of the channel */
    private String tvBrowserName;

    /** contains the DVBViewer channel infos*/
    private DVBViewerChannel dvbChannel;


    /**
     * Creates a new channel object
     *
     * @param tvBrowser the TV-Browser's name of the channel
     * @param dvbchannel the DVBViewer's channel
     */
    TvbDvbVChannel(String tvBrowser, DVBViewerChannel dvbchannel) {
      tvBrowserName = tvBrowser;
      dvbChannel = dvbchannel;
    }


    /**
     * Creates a new channel object
     *
     * @param tvBrowser the TV-Browser's name of the channel
     * @param dvbViewer the DVBViewer's name of the channel
     * @param serviceID the DVBViewer's Service ID of the channel
     * @param audioID the DVBViewer's Audio ID of the channel
     * @param tunerType the DVBViewer's tuner type of the channel
     */
    TvbDvbVChannel(String tvBrowser, String dvbViewer, String serviceID, String audioID,
            String tunerType) {
      tvBrowserName = tvBrowser;
      dvbChannel = new DVBViewerChannel(dvbViewer, serviceID, audioID, tunerType);
    }


    /**
     * @return DVBViewer channel infos
     */
    public final DVBViewerChannel getDVBChannel() {
      return dvbChannel;
    }


    /**
     * @return Returns TV-Browser's name of this channel
     */
    final String getTvBrowserName() {
      return tvBrowserName;
    }


    /**
     * Compares this channel to another channel and returns the result of the
     * {@link String#compareTo(String)} method applied to the member
     * <code>tvBrowserName</code>
     *
     * @param o another channel object
     * @return result of comparing this.tvBrowserName with o.tvBrowserName
     */
    public final int compareTo(TvbDvbVChannel o) {
      if (o == null) { return -1; }
      TvbDvbVChannel other = o;

      return tvBrowserName.compareTo(other.tvBrowserName);
    }


    /**
     * @return true if this is a valid channel (not an unknown one)
     */
    public final boolean isValid() {
      return !tvBrowserName.equals(UNKNOWN);
    }
  }


  public final static class DVBViewerChannel {

    public String name;

    public String serviceID;

    public String audioID;

    public String tunerType;


    public DVBViewerChannel(String channelname, String serviceid, String audioid, String tunertype) {
      name = channelname;
      serviceID = serviceid;
      audioID = audioid;
      tunerType = tunertype;
    }


    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return name;
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
      if (this == obj) { return true; }
      if (!(obj instanceof DVBViewerChannel)) { return false; }

      DVBViewerChannel rhs = (DVBViewerChannel)obj;

      if (name.equals(rhs.name)) {
        if (serviceID.equals(rhs.serviceID)) {
          if (audioID.equals(rhs.audioID)) {
            if (tunerType.equals(rhs.tunerType)) { return true; }
          }
        }
      }

      return false;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
      return name.hashCode() + serviceID.hashCode() + audioID.hashCode() + tunerType.hashCode();
    }
  }
}
