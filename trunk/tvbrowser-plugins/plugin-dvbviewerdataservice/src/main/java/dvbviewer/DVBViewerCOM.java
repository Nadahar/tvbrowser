/*
 * DVBViewerCOMmunication.java
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
package dvbviewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import ca.beq.util.win32.registry.RegistryException;
import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RegistryValue;
import ca.beq.util.win32.registry.RootKey;

import com4j.COM4J;
import com4j.ExecutionException;
import com4j.Holder;

import devplugin.ProgressMonitor;
import dvbviewer.com4j.ClassFactory;
import dvbviewer.com4j.IChannelCollection;
import dvbviewer.com4j.IDVBViewer;
import dvbviewer.com4j.IEPGCollection;
import dvbviewer.com4j.IEPGItem;
import dvbviewer.com4j.IEPGManager;
import dvbviewer.com4j.ITimerCollection;
import dvbviewer.com4j.ITimerItem;


/**
 * TODO short description for DVBViewerCOMmunication.
 * <p>
 * Long description for DVBViewerCOMmunication.
 *
 * @author pollaehne
 * @version $Revision: $
 */
public class DVBViewerCOM {

  /** name of the key for the default value of time after recording */
  public static final String SETUP_EPGBEFORE = "EPGBefore";

  /** name of the key for the default value of time before recording */
  public static final String SETUP_EPGAFTER = "EPGAfter";

  /** name of the key for the default value of action before recording */
  public static final String SETUP_DEF_REC_ACTION = "DefRecAction";

  /** name of the key for the default value of action after recording */
  public static final String SETUP_DEF_AFTER_RECORD = "DefAfterRecord";

  /** name of the key for the default value of A/V disabling */
  public static final String SETUP_DEF_REC_AVDISABLED = "DefRecAVDisabled";

  /** the setup section used for the keys and values */
  public static final String SETUP_SECTION_GENERAL = "General";

  /** the class id of the DVBViewer COM object (DVBViewerServer.DVBViewer) */
  private static final String DVBVIEWER_CLSID = "{D0B1ACAD-1190-4E6D-BD60-41DFA6A28E30}";

  /** registry key below HKEY_CLASSES_ROOT that contains the COM registration of DVBViewerServer.DVBViewer */
  private static final String LOCALSERVER32 = "CLSID\\" + DVBVIEWER_CLSID + "\\LocalServer32";

  /** the one and only instance */
  private static DVBViewerCOM instance;

  /** Translator */
  private static final Localizer localizer = Localizer.getLocalizerFor(DVBViewerCOM.class);

  static final Logger logger = Logger.getLogger(DVBViewerCOM.class.getName());

  /** The one and only COM reference to DVBViewer */
  private IDVBViewer dvbViewer;

  /** keep a record of what to do on stopDVBViewer */
  private boolean weStartedDVBViewer = false;

  /** did we start DVBViewer with CreateObject? */
  private boolean mustKeepRef = false;

  /** path and name of DVBViewer executable */
  private String dvbViewerExe;

  /** the list of DVBViewers channels */
  private List<DVBViewerChannel> dvbChannels;


  /**
   * keep others from creating instances
   * @throws TvBrowserException
   */
  private DVBViewerCOM() throws TvBrowserException {
    // try to get the Path
    try {
      RegistryKey dvbviewerKey = new RegistryKey(RootKey.HKEY_CLASSES_ROOT, LOCALSERVER32);
      if (!dvbviewerKey.exists()) {
        throw new TvBrowserException(getClass(), "err_dvbviewer_registry", "DVBViewer is not correctly installed");
      }
      RegistryValue localServer = dvbviewerKey.getValue("");
      String temp = localServer.getStringValue();
      if (null != temp && 0 != temp.length()) {
        dvbViewerExe = temp;
      }
    } catch ( RegistryException e) {
      throw new TvBrowserException(getClass(), "err_reading_registry", "Cannot read DVBViewer path from registry", e);
    } catch (UnsatisfiedLinkError e) {
      logger.log(Level.SEVERE, "Unable to read the path to DVBViewer from the registry", e);
    }
  }


  /**
   * @return the instance of DVBViewerCOMmunication
   * @throws TvBrowserException
   */
  public static DVBViewerCOM getInstance() throws TvBrowserException {
    if (null == instance) {
      instance = new DVBViewerCOM();
    }
    return instance;
  }


  /**
   * Get access to a running DVBViewer COM server
   *
   * @return reference to a running DVBViewer COM Server
   *         or null if there is no running server
   */
  private static IDVBViewer getDVBViewer() {
    try {
      return COM4J.getActiveObject(IDVBViewer.class, DVBVIEWER_CLSID);
    } catch (ExecutionException e) {
      return null;
    }
  }


  /**
   * Start DVBViewer minimized if it is not already running
   *
   * @throws IOException if starting DVBViewer fails
   */
  public final boolean startDVBViewer() throws IOException {
    startDVBViewer(true);
    return weStartedDVBViewer;
  }


  public final IDVBViewer startDVBViewer(boolean minimized) throws IOException {
    return startDVBViewer(minimized, false);
  }

  /**
   * @return reference to the DVBViewer COM object
   *
   * @param minimized if DVBViewer needs to be started defines if starting minimized
   * @param getRef get a COM reference in any case (especially after starting DVBViewer)
   * @throws IOException if starting DVBViewer fails
   */
  public final IDVBViewer startDVBViewer(boolean minimized, boolean getRef) throws IOException {
    if (null == dvbViewer) {
      dvbViewer = getDVBViewer();
    }

    if (null == dvbViewer) {
      if (null != dvbViewerExe) {
        StringBuilder cmdline = new StringBuilder(260);
        cmdline.append(dvbViewerExe);
        if (minimized) {
          cmdline.append(" -m -c");
        }
        cmdline.append(" -nosplash");

        Runtime.getRuntime().exec(cmdline.toString());
        if (getRef) {
          do {
            dvbViewer = getDVBViewer();
          } while (null == dvbViewer);
        }
      } else {
        // we need to keep the reference and must not dispose it since we create the COM object
        mustKeepRef = true;

        dvbViewer = ClassFactory.createDVBViewer();
        if (minimized) {
          dvbViewer.sendCommand(16382); // WindowMinimize=16382
        }
      }

      weStartedDVBViewer = true;
    }

    return dvbViewer;
  }

  /**
   * Get rid of the DVBViewer COM reference if we did not start it with CreateObject
   */
  private final void disposeViewer(IDVBViewer ref) {
    if (dvbViewer == ref) {
      if (!mustKeepRef) {
        if (null != dvbViewer) {
          dvbViewer.dispose();
          dvbViewer = null;
        }
      }
    } else if (null != ref) {
      ref.dispose();
    }
  }

  /**
   * Stops a running DVBViewer if we have started it.
   * Otherwise only the COM reference is removed.
   */
  public final void stopDVBViewer() {
    if (weStartedDVBViewer) {
      // we started it so close it
      if (null == dvbViewer) {
        dvbViewer = getDVBViewer();
        if (null != dvbViewer) {
          dvbViewer.quit();
        }
      }
      weStartedDVBViewer = false;
    }

    if (null != dvbViewer) {
      // get rid of the COM reference
      dvbViewer.dispose();
      dvbViewer = null;
    }
  }

  /**
   * Checks if DVBViewer is running
   *
   * @return true if DVBViewer is active
   */
  public final boolean isDVBViewerActive() {
    if (null == dvbViewer) {
      dvbViewer = getDVBViewer();
      if (null != dvbViewer) {
        dvbViewer.dispose();
        dvbViewer = null;
        return true;
      }
      return false;
    }
    return true;
  }


  /**
   * Tells DVBViewer to tune to <code>channel</code>.
   * If DVBViewer is not running it will be started
   *
   * @param channel the channel to tune to
   */
  public final void runDvbViewer(DVBViewerChannel channel) {
    IDVBViewer ref = null;
    try {
      ref = startDVBViewer(false, true);

      // the user wants to see DVBViewer so we do not stop it anymore
      weStartedDVBViewer = false;

      if (-1 != channel.getChannelNr()) {
        ref.currentChannelNr(channel.getChannelNr());
      } else {
        ref.currentChannelNr(ref.channelManager().getbyChannelname(channel.getName()));
      }
      // restore the window and bring it to top
      ref.sendCommand(16397); // WindowRestore=16397
      ref.sendCommand(1);     // OnTop=1
      ref.sendCommand(1);     // OnTop=1
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to start DVBViewer", e);
    } finally {
      disposeViewer(ref);
    }
  }


  public final int tuneChannelsforEPG(List<DVBViewerChannel> channels, int time, int time2Record, ProgressMonitor monitor) {
    IDVBViewer ref = null;
    ITimerCollection timers = null;
    int currentChannelNr = -1;
    int previousChannelNr = -1;
    int rc = 0;

    if (null == channels || 0 == channels.size()) {
      return rc;
    }

    try {
      ref = startDVBViewer(false, true);

      if (ref.isTimeshift()) {
        logger.fine("Timeshift active no tuning to channel possible");
        return 1;
      }

      timers = ref.timerManager();
      if (timers.recording()) {
        logger.fine("Recording active no tuning to channel possible");
        return 2;
      }

      long nextRecording = timers.nextRecordingTime().getTime();

      currentChannelNr = previousChannelNr = ref.currentChannelNr();

      for (DVBViewerChannel channel : channels) {
        long now = System.currentTimeMillis();
        if (now < nextRecording) {
          if (nextRecording < now + 1000 * time2Record + 1000 * time) {
            logger.fine("Scheduled recording near no tuning to channel possible");
            rc = 3;
            break;
          }
        }

        if (null != monitor) {
          monitor.setMessage(localizer.msg("epgupd", "Updating DVBViewer EPG for {0}", channel.getCategory()));
        }

        // set the channel
        ref.currentChannelNr(channel.getChannelNr());
        currentChannelNr = ref.currentChannelNr();

        // wait for the given time
        try {
          Thread.sleep(1000 * time);
        } catch (InterruptedException e) {
          // ignored
        }
      }

      if (currentChannelNr != previousChannelNr) {

        // return to the previous channel
        ref.currentChannelNr(previousChannelNr);
        try {
          // wait for the channel to be tuned in
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // ignored
        }
      }

      ref.sendCommand(53); // rebuildGraph
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to start DVBViewer", e);
      return -1;
    } finally {
      if (null != timers) {
        timers.dispose();
      }

      disposeViewer(ref);
    }

    return rc;
  }


  /**
   * Clears the list of DVBViewer channels
   */
  public final void clearChannels() {
	if (null != dvbChannels) {
    dvbChannels.clear();
	}

    dvbChannels = null;
  }


  /**
   * Sets the list of DVBViewer channels
   */
  public final void setChannels(List<DVBViewerChannel>channels) {
    if (null != dvbChannels) {
      dvbChannels.clear();
    }
    dvbChannels = channels;
  }


  /**
   * @return Returns value of dvbChannels.
   */
  public final List<DVBViewerChannel> getChannels(ProgressMonitor monitor) {
    if (null == dvbChannels) {
      dvbChannels = readChannels(monitor);
    }
    return dvbChannels;
  }


  private List<DVBViewerChannel> readChannels(ProgressMonitor monitor) {
    List<DVBViewerChannel> channelList = new ArrayList<DVBViewerChannel>();

    IDVBViewer ref = null;
    try {
      ref = startDVBViewer(true, true);

      IChannelCollection channels = ref.channelManager();
      int count = channels.count();
      monitor.setMaximum(count*2);
      monitor.setValue(0);
      for (int i = 0; i < count; ++i) {
        channelList.add(new DVBViewerChannel(i, channels.item(i)));
        monitor.setValue(i);
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to start DVBViewer", e);
    } finally {
      disposeViewer(ref);
    }

    return channelList;
  }


  public List<IEPGItem> getEPG(DVBViewerChannel channel, Calendar startDate, Calendar endDate) {
    List<IEPGItem> items = new ArrayList<IEPGItem>();
    try {
      IDVBViewer ref = startDVBViewer(true, true);
      IEPGManager manager = ref.epgManager();

      if (manager.hasEPG(channel.getEPGID())) {
        Holder<Integer> sid = new Holder<Integer>();
        Holder<Integer> tid = new Holder<Integer>();

        manager.splitEPGChannelID(channel.getEPGID(), sid, tid);
        IEPGCollection coll = manager.get(sid.value.intValue(), tid.value.intValue(), startDate.getTime(), endDate.getTime());
        int count = coll.count();
        for (int i = 0; i < count; ++i) {
          IEPGItem item = coll.item(i);
          items.add(item);
        }
      }

      manager.dispose();
      disposeViewer(ref);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to start DVBViewer", e);
    }


    return items;
  }


  /**
   * Tries to find entries in the DVBViewer setup whose name is a key in the <code>items</code> Map
   * and stores the corresponding value into the Map
   *
   * @return Map containing the name and value as String of the above entries from setup.xml
   */
  public void getSetupEntries(Map<String, String> items) {

    try {
      IDVBViewer ref = startDVBViewer(true, true);

      for (Entry<String, String> mapEntry : items.entrySet()) {
        String key = mapEntry.getKey();
        String value = ref.getSetupValue(DVBViewerCOM.SETUP_SECTION_GENERAL, key, "");
        if (!"".equals(value)) {
          logger.log(Level.FINE, "Found " + key + " with value {0}", value);
          mapEntry.setValue(value);
        }
      }

      disposeViewer(ref);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to start DVBViewer", e);
    }
  }


  /**
   * Stores the items in the Map into setup.xml if they have changed compared to the
   * value in the setup.xml
   *
   * Attention: The Map <code>items</code> will be empty on return
   *
   * @param path the path where the setup.xml is located
   * @param items the Map with the items to be written
   */
  public void setSetupEntries(String path, Map<String, String> items) {
    int size = items.size();

    if (0 == size) {return;}

    try {
      IDVBViewer ref = startDVBViewer(true, true);

      for (Entry<String, String> mapEntry : items.entrySet()) {
        String key = mapEntry.getKey();
        String value = ref.getSetupValue(DVBViewerCOM.SETUP_SECTION_GENERAL, key, "");
        if (value.equals(mapEntry.getValue())) {
          logger.log(Level.FINE, "Value of key {0} remains unchanged", key);
        } else {
          logger.log(Level.FINE, "Setting {0] to value " +value, key);
          ref.setSetupValue(DVBViewerCOM.SETUP_SECTION_GENERAL, key, value);
        }
      }

      disposeViewer(ref);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to start DVBViewer", e);
    }
  }


  /**
   * Gets the entries of the DVBViewer timers list, stores them into a List of ScheduledRecording objects
   *
   * @return List containing ScheduledRecording objects of the timers.xml
   *         entries
   */
  public List<TimerItem> getTimersEntries() {
    List<TimerItem> recordings = new ArrayList<TimerItem>();
    try {
      IDVBViewer ref = startDVBViewer(true, true);

      try {
        ITimerCollection timers = ref.timerManager();
        int count = timers.count();
        for (int i = 0; i < count; ++i) {
          ITimerItem item = timers.item(i);
          logger.log(Level.FINE, "Adding timers item: {0}" , item.description());
          recordings.add(new TimerItem(item));
        }
      } catch (Exception e) {
        ErrorHandler.handle(localizer.msg("err_timers_reading",
                            "Unable to read the scheduled DVBViewer recordings from '{0}'",
                            "DVBViewer COM"), e);
      }

      disposeViewer(ref);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to start DVBViewer", e);
    }

    return recordings;
  }


  /**
   * Stores the entries located in the Vector to the timers.xml
   * @param v the Vector with the entries
   */
  public void setTimersEntries(List<TimerItem> v) {
    try {
      IDVBViewer ref = startDVBViewer(true, true);

      try {
        ITimerCollection timers = ref.timerManager();
        for (TimerItem rec : v) {
          timers.addItem(rec.getDvbViewerChannel(),
                         rec.getStart(),
                         rec.getStart(),
                         rec.getStop(),
                         rec.getProgramTitle(),
                         rec.isAvDisable(),
                         rec.isEnabled(),
                         rec.getRecAction().ordinal(),
                         rec.getAfterAction().ordinal(),
                         rec.getRepetitionDays());
        }
      } catch (Exception e) {
        ErrorHandler.handle(localizer.msg("err_timers_writing",
                                          "Unable to write the scheduled DVBViewer recordings to '{0}'",
                                          "DVBViewer COM"), e);
      }

      disposeViewer(ref);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to start DVBViewer", e);
    }
  }


  /**
   * Store the entry to DVBViewer
   * @param rec the scheduled recording entry
   * @return true if there is an overlapping timer
   *         false otherwise
   */
  public boolean addTimersEntry(TimerItem rec) {
    boolean overlapping = false;
    try {
      IDVBViewer ref = startDVBViewer(true, true);

      try {
        ITimerCollection timers = ref.timerManager();
        ITimerItem item = timers.addItem(rec.getDvbViewerChannel(),
                                         rec.getStart(),
                                         rec.getStart(),
                                         rec.getStop(),
                                         rec.getProgramTitle(),
                                         rec.isAvDisable(),
                                         rec.isEnabled(),
                                         rec.getRecAction().ordinal(),
                                         rec.getAfterAction().ordinal(),
                                         rec.getRepetitionDays());

        if (!item.executeable()) {
// yeah, it would be bloody nice if overlap would work as expected
//          ITimerItem overlap = timers.overlap(item);
//          if (null != overlap) {
//            overlapping = new TimerItem(overlap);
//          }

          //remove the added item
          int count = timers.count();
          for (int i = 0; i < count; ++i) {
            ITimerItem curr = timers.item(i);
            if (curr.channelNr() == item.channelNr()) {
              if (0 == curr.date().compareTo(item.date())) {
                if (0 == curr.startTime().compareTo(item.startTime())) {
                  timers.remove(i);
                  break;
                }
              }
            }
          }
          overlapping = true;
        }
      } catch (Exception e) {
        ErrorHandler.handle(localizer.msg("err_timers_writing",
                                          "Unable to write the scheduled DVBViewer recordings to '{0}'",
                                          "DVBViewer COM"), e);
      }

      disposeViewer(ref);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to start DVBViewer", e);
    }

    return overlapping;
  }


  /**
   * Removes the entry from DVBViewer
   * @param rec the scheduled recording entry
   */
  public boolean removeTimersEntry(TimerItem rec) {
    boolean result = false;
    try {
      IDVBViewer ref = startDVBViewer(true, true);

      try {
        ITimerCollection timers = ref.timerManager();
        int count = timers.count();
        for (int i = 0; i < count; ++i) {
          ITimerItem item = timers.item(i);
          if (rec.getTimerID() == item.id()) {
            timers.remove(i);
            result = true;
            break;
          }
        }
      } catch (Exception e) {
        ErrorHandler.handle(localizer.msg("err_timers_writing",
                                          "Unable to write the scheduled DVBViewer recordings to '{0}'",
                                          "DVBViewer COM"), e);
      }

      disposeViewer(ref);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to start DVBViewer", e);
    }


    return result;
  }

}



