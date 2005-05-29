package com.gc.systray;

import java.awt.Point;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

/**
 * This is the Wrapper for the Windows SystemTray
 * 
 * @author bodum
 */
public class WinSystemTray implements SystemTrayIf {
  /** Logger */
  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(WinSystemTray.class.getName());

  /** The Image-Handle */
  private int mSystrayImageHandle;

  /** Use Systemtray ? */
  private boolean mUseSystemTray = false;

  /** The Tray-Manager */
  private SystemTrayIconManager mManager;

  /**
   * Create the SytemTray
   * 
   * @return true, if succesfull
   */
  public boolean init(JFrame parent, String image, String tooltip) {
    mSystrayImageHandle = -1;
    File iconTrayLib = new File("DesktopIndicator.dll");

    if (iconTrayLib.exists()) {
      mUseSystemTray = SystemTrayIconManager.initializeSystemDependent();
      if (!mUseSystemTray) {
        mLog.info("could not load library " + iconTrayLib.getAbsolutePath());
      } else {

        mSystrayImageHandle = SystemTrayIconManager.loadImage(image);
        if (mSystrayImageHandle == -1) {
          mLog.info("Could not load system tray icon");
          mUseSystemTray = false;
        }
      }
    }

    if (mUseSystemTray) {
      mManager = new SystemTrayIconManager(mSystrayImageHandle, tooltip);
    }
    return mUseSystemTray;
  }

  /**
   * Visibility of the Icon
   * @param b 
   */
  public void setVisible(boolean b) {
    if (mUseSystemTray) {
      // ATTENTION ! THIS IS FUCKING SLOW!
      mManager.setVisible(b);
    }
  }

  /**
   * Add a Left-DoubleClick-Action
   */
  public void addLeftDoubleClickAction(final ActionListener listener) {
    mManager.addSystemTrayIconListener(new SystemTrayIconListener() {

      public void mouseClickedLeftButton(Point pos, SystemTrayIconManager source) {
      }

      public void mouseClickedRightButton(Point pos, SystemTrayIconManager ssource) {
      }

      public void mouseLeftDoubleClicked(Point pos, SystemTrayIconManager source) {
        listener.actionPerformed(null);
      }

      public void mouseRightDoubleClicked(Point pos, SystemTrayIconManager source) {
      }
    });
  }

  /**
   * Set the JPopupMenu
   */
  public void setTrayPopUp(JPopupMenu trayMenu) {
    mManager.setRightClickView(trayMenu);
  }
}
