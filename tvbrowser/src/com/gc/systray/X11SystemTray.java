package com.gc.systray;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import util.misc.JavaVersion;

/**
 * This is the Wrapper for the Windows SystemTray
 * 
 * @author bodum
 */
public class X11SystemTray extends MouseAdapter implements SystemTrayIf {
  /** Logger */
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(X11SystemTray.class.getName());

  /** Tray-Manager */
  private X11SystrayManager mManager;
  /** Left-Click-Mouseclick-Actions */
  private ArrayList mLeftAction = new ArrayList();
  /** Left-DoubleClick-Mouseclick-Actions */
  private ArrayList mLeftDoubleAction = new ArrayList();
  /** The Popup-Menu */
  private JPopupMenu mPopupMenu;
  /** Tray-Parent */
  private JDialog mTrayParent;

  /**
   * Init the SystemTray
   */
  public boolean init(JFrame parent, String image, String tooltip) {
    
    if (JavaVersion.getVersion() < JavaVersion.VERSION_1_5) {
      mLog.info("Tray needs Java 1.5 or higher.");
      return false;
    }
    
    mManager = new X11SystrayManager(image, tooltip);
    
    if (!mManager.isLoaded()) {
      mLog.info("Could not load Tray-Library.");
      return false;
    }
    
    mManager.addMouseListener(this);

    mTrayParent = new JDialog();
    mTrayParent.setTitle("Tray-Menu");

    mTrayParent.setSize(0, 0);
    mTrayParent.setUndecorated(true);
    mTrayParent.setVisible(false);
    mTrayParent.addWindowFocusListener(new WindowFocusListener() {

      public void windowGainedFocus(WindowEvent e) {
      }

      public void windowLostFocus(WindowEvent e) {
        if ((mPopupMenu != null) && (mPopupMenu.isVisible()))
          mPopupMenu.setVisible(false);
      }

    });

    return mManager.isLoaded();
  }

  /**
   * Set the visibility of the Icon
   * @param b true to make the TrayIcon visible
   */
  public void setVisible(boolean b) {
    if (mManager.isLoaded()) {
      if (b) {
        mManager.systrayShow();
      } else {
        mManager.systrayHide();
      }
    }
  }

  /**
   * Add a Left-DoubleClick-Action
   */
  public void addLeftClickAction(ActionListener listener) {
    mLeftAction.add(listener);
  }
  
  /**
   * Add a Left-DoubleClick-Action
   */
  public void addLeftDoubleClickAction(ActionListener listener) {
    mLeftDoubleAction.add(listener);
  }

  /**
   * Set the Popup
   */
  public void setTrayPopUp(JPopupMenu popupMenu) {
    mPopupMenu = popupMenu;
    
    mPopupMenu.addPopupMenuListener(new PopupMenuListener() {

      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      }

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        mTrayParent.setVisible(false);
      }

      public void popupMenuCanceled(PopupMenuEvent e) {
      }
      
    });
    popupMenu.setVisible(true);
    popupMenu.setVisible(false);
  }

  /**
   * If a Mouse was pressed
   */
  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger() && mPopupMenu != null) {
      // we have to calculate th leftTopX and Y value of the popupmenu
      int leftTopX, leftTopY;

      leftTopX = e.getPoint().x - mPopupMenu.getWidth();
      leftTopY = e.getPoint().y - mPopupMenu.getHeight();

      mTrayParent.setVisible(true);
      mPopupMenu.show(mManager.getSystemTray(), leftTopX, leftTopY);
    } else if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
      for (int i = 0; i < mLeftDoubleAction.size(); i++) {
        ((ActionListener) mLeftDoubleAction.get(i)).actionPerformed(null);
      }
    } else if ((e.getButton() == MouseEvent.BUTTON1)) {
      for (int i = 0; i < mLeftAction.size(); i++) {
        ((ActionListener) mLeftAction.get(i)).actionPerformed(null);
      }
    }
  }
}