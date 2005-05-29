package com.gc.systray;

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
    
    if (osname.startsWith("windows")) {
      return new WinSystemTray();
    } else if (osname.startsWith("linux")) {
      return new X11SystemTray();
    }
    
    return null;
  }
  
}