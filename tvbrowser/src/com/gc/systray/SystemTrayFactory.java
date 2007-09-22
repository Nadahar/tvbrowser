package com.gc.systray;

import util.misc.JavaVersion;

/**
 * This Factory creates the correct Tray-Wrapper
 *  
 * @author bodum
 */
public class SystemTrayFactory {
  /** Logger */
  private static java.util.logging.Logger mLog
  = java.util.logging.Logger.getLogger(SystemTrayFactory.class.getName());

  /**
   * Create the Tray-Wrapper for the OS.
   * @return SystemTray-Wrapper
   */
  public static SystemTrayIf createSystemTray() {
    String osname = System.getProperty("os.name").toLowerCase();    
    
    boolean kde = false;
    
    try {
      String kdeSession = System.getenv("KDE_FULL_SESSION");
      if (kdeSession != null) {
        kde = kdeSession.compareToIgnoreCase("true") == 0;
        
        if(kde) {
          mLog.info("KDE check done. KDE is used.");
        }
      }
    }catch(Exception e) {}
    
    if(JavaVersion.getVersion() >= JavaVersion.VERSION_1_6 && (!kde || System.getProperty("os.arch").equalsIgnoreCase("amd64"))) {
      mLog.info("Try using Java 6 Tray.");
      return new Java6Tray();
    } else if (osname.startsWith("windows")) {
      return new WinSystemTray();
    } else if (osname.startsWith("linux")) {
      return new X11SystemTray();
    }
    
    return null;
  }
  
}