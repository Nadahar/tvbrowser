package com.gc.systray;

public class SystemTrayFactory {

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
