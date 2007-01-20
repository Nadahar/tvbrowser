package com.gc.systray;

import util.misc.JavaVersion;

/**
 * This Factory creates the correct Tray-Wrapper
 *  
 * @author bodum
 */
public class SystemTrayFactory {

  /**
   * Create the Tray-Wrapper for the OS.
   * @return SystemTray-Wrapper
   */
  public static SystemTrayIf createSystemTray() {
    String osname = System.getProperty("os.name").toLowerCase();
    
    boolean kde = false;
    
    try {
      kde = System.getenv("KDE_FULL_SESSION").compareToIgnoreCase("true") == 0;
    }catch(Exception e) {}
    
    if (osname.startsWith("windows") && !osname.contains("vista")) {
      return new WinSystemTray();
    } else if (osname.startsWith("linux") && kde) {
      return new X11SystemTray();
    } else if(JavaVersion.getVersion() >= JavaVersion.VERSION_1_6) {
      return new Java6Tray();
    }
    
    return null;
  }
  
}