/*
 * ProcessHandler.java
 * Copyright (C) 2006 Probum
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

package dvbplugin.dvbviewer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.exc.ErrorHandler;
import util.ui.Localizer;

import devplugin.Plugin;
import dvbplugin.Settings;
import dvbplugin.Settings.TvbDvbVChannel;

/**
 * This class is an interface to the native methods provided by ProcessHandler.DLL
 *
 * @author Probum
 * @author UP
 */
public class ProcessHandler {
  /** window message ID to send commands to DVBViewer */
  static final int WM_DVBVIEWER = 0xB2C2;
  /** window message param to force DVBViewer to read the config files */
  static final int MSG_INIREFRESH = 0x1020;

  /** the version of the process handler DLL */
  private static final String DLL_VERSION = "12";

  /** the name of the process hander DLL */
  private static final String DLL_NAME = "CProcessHandler" + DLL_VERSION + ".dll";

  /** the path to the process handler DLL within the classpath (in dvbplugin.jar) */
  private static final String DLL_RESOURCE_PATH = "/dvbplugin/" + DLL_NAME;

  /** is the process handler DLL already loaded? */
  private static boolean isDllLoaded;

  /** Translator */
  private static final Localizer localizer = Localizer.getLocalizerFor(ProcessHandler.class);

  /** the usual logger */
  private static final Logger logger = Logger.getLogger(ProcessHandler.class.getName());

  public static final String DVBVIEWERPLUGIN_USER_PATH = Plugin.getPluginManager().getTvBrowserSettings()
                                                               .getTvBrowserUserHome() + File.separatorChar
                                                               + "dvbviewer";

  /** the path to the process handler DLL in the TVBrowser user home directory */
  private static final String DLL_USER_PATH = DVBVIEWERPLUGIN_USER_PATH + File.separatorChar + DLL_NAME;

  private static ProcessHandler processHandler;

  // remove old versions
  static {
    String [] names = {"CProcessHandler10.dll", "CProcessHandler11.dll"};
    for (String name : names) {
      String oldname = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome()
                        + File.separatorChar + name;
      try {
        File oldfile = new File(oldname);
        if (oldfile.exists()) {
          oldfile.delete();
        }
      } catch (Throwable t) {
        logger.log(Level.SEVERE, "Unable to delete old file " + oldname, t);
      }
    }
  }


  public static final boolean isDVBViewerActive(Settings set) {
    if (initProcessHandler()) {
      try {
      if (processHandler.isActive(set.getViewerExeName())) { return true; }
      } catch (Throwable t) {
        logger.log(Level.INFO, "Calling isactive failed", t);
      }
    }

    return false;
  }


  public static final void updateDvbViewer(Settings set) {
    if (initProcessHandler()) {
      if (processHandler.isActive(set.getViewerExeName())) {
        processHandler.sendMessage(set.getViewerExeName(), WM_DVBVIEWER, MSG_INIREFRESH);
      }
    }
  }


  public static final void runDvbViewer(Settings set, TvbDvbVChannel channel) {
    StringBuilder cmdline = new StringBuilder(128);
    try {
      cmdline.append(set.getViewerExePath());
      cmdline.append(" \"-c");
      cmdline.append(channel.getDVBChannel().name);
      cmdline.append("\"");

      Runtime.getRuntime().exec(cmdline.toString());
    } catch (Exception e) {
      ErrorHandler.handle(localizer.msg("err_run_dvbviewer_param",
                                        "Unable to start DVBViewer with commandline '{0}'",
                                        cmdline.toString()), e);
    }
  }


  public static final void runDvbViewer(Settings set) {
    try {
      Runtime.getRuntime().exec(set.getViewerExePath());
    } catch (Exception e) {
      ErrorHandler.handle(localizer.msg("err_run_dvbviewer", "Unable to start DVBViewer"), e);
    }
  }


  public static final void runDvbSchedulerUpdate(Settings set) {
    // if the dvbviewer task scheduler is used, then update its tasks
    if (set.isSchedulerUsed()) {
      File scheduler = new File(set.getSchedulerExePath());
      if (!scheduler.exists()) { return; }

      StringBuilder cmdline = new StringBuilder(128);
      try {
        cmdline.append(set.getSchedulerExePath());
        cmdline.append(" -4");

        Runtime.getRuntime().exec(cmdline.toString());
      } catch (Exception e) {
        ErrorHandler.handle(localizer.msg("err_run_scheduler",
                                          "Unable to start DVBTaskScheduler with commandline '{0}'",
                                          cmdline.toString()), e);
      }
    }
  }


  private static final boolean initProcessHandler() {
    if (null == processHandler) {
      try {
        processHandler = new ProcessHandler();
      } catch (IOException e) {
        ErrorHandler.handle(localizer.msg("err_init_processhandler",
                                          "Unable to initialize ProcessHandler DLL"), e);
        return false;
      }
    }

    return true;
  }


  /**
   * Creates a new instance of ProcessHandler
   *
   * @throws IOException
   */
  private ProcessHandler() throws IOException {
    if (!isDllLoaded) {
      if (checkForProcessDLL(DLL_USER_PATH)) {
        try {
          System.load(DLL_USER_PATH);
        } catch (UnsatisfiedLinkError e) {
          logger.log(Level.SEVERE, "Error while loading processhandler DLL", e);
          IOException ioe = new IOException("Error while loading processhandler DLL");
          ioe.initCause(e);
          throw ioe;
        }
      }

      isDllLoaded = true;
    }
  }


  /**
   * Checks for existence of the file <code>dllfilename</code>.
   * If this file does not exist it is copied from the classpath
   * to the location defined by <code>dllfilename</code>.
   *
   * @throws IOException
   */
  private boolean checkForProcessDLL(String dllfilename) throws IOException {
    File dllfile = new File(dllfilename);
    if (dllfile.exists()) {
      logger.log(Level.FINE, "found processhandler DLL at {0}", dllfile.getAbsolutePath());
      return true;
    }

    InputStream input = null;
    OutputStream output = null;
    try {
      logger.log(Level.FINE, "about to copy processhandler DLL to {0}", dllfile.getAbsolutePath());

      // check that the target dir exists
      dllfile.getParentFile().mkdirs();

      input = new BufferedInputStream(getClass().getResourceAsStream(DLL_RESOURCE_PATH));
      output = new BufferedOutputStream(new FileOutputStream(dllfile));
      byte[] buffer = new byte[4096];
      int count = 0;
      do {
        output.write(buffer, 0, count);
        count = input.read(buffer, 0, buffer.length);
      } while (count != -1);
    } catch (IOException e) {
      logger.log(Level.SEVERE,
              "Error while copying JNI DLL from dvbplugin.jar to user home directory", e);
      throw e;
    } finally {
      if (null != input) {
        try {
          input.close();
        } catch (IOException e) {
          // at least we tried it
          logger.log(Level.SEVERE,
                  "Unable to close the source file "+ DLL_RESOURCE_PATH, e);
        }
      }
      if (null != output) {
        try {
          output.close();
        } catch (IOException e) {
          // at least we tried it
          logger.log(Level.SEVERE,
                  "Unable to close the destination file "+ dllfilename, e);
        }
      }
    }

    return true;
  }


  /**
   * Check if process named <code>name</code> is active
   *
   * @param name of the process to be checked
   * @return true if the process is running false otherwise
   */
  native boolean isActive(String name);


  /** unused
   * Stops the process named <code>name</code> and waits for it to end
   * Attention: This only works for processes that do have a window defined (so
   * you cannot stop cmd.exe)
   *
   * @param name of the process to be stopped
   *
  native void stopProcess(String name);
  */


  /**
   * Sends a window message <code>msg</code> with parameter <code>wparam</code>
   * to a process named <code>name</code>.
   * If the process is not running, the message will not be sent.
   *
   * @param name of the process to send the message to
   * @param msg the message
   * @param wparam a param for the message
   */
  native void sendMessage(String name, int msg, int wparam);

}
