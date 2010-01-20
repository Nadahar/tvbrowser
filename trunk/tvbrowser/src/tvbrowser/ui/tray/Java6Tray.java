/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.tray;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import util.misc.JavaVersion;
import util.misc.OperatingSystem;
import util.ui.UiUtilities;

/**
 * Tray for TV-Browser on systems
 * with Java 6 and tray support.
 * 
 * @author René Mach
 */
public class Java6Tray {
  /** Logger */
  private static final Logger mLog
  = Logger.getLogger(Java6Tray.class.getName());
  
  private java.awt.SystemTray mSystemTray;
  private TrayIcon mTrayIcon;
  private JPopupMenu mPopupMenu;
  private ActionListener mLeftClickListener, mLeftDoubleClickListener, mRightClickListener;
  
  private JDialog mTrayParent;
  
  private static Java6Tray mInstance;
  
  private Java6Tray() {
    mInstance = this;
  }
  
  /**
   * Creates the Java 6 tray.
   * 
   * @return The Java 6 tray instance.
   */
  public static Java6Tray create() {
    if(mInstance == null) {
      new Java6Tray();
    }
    
    return mInstance;
  }
  
  /**
   * Add a Left-Click-Action
   * @param listener Action that is triggered on left click
   */
  public void addLeftClickAction(ActionListener listener) {
    mLeftClickListener = listener;
  }

  /**
   * Add a Left-DoubleClick-Action
   * @param listener Action that is triggered on left doubleclick
   */
  public void addLeftDoubleClickAction(ActionListener listener) {
    mLeftDoubleClickListener = listener;
  }

  /**
   * Add a Right-Click-Action
   * @param listener Action that is triggered on right click
   */
  public void addRightClickAction(ActionListener listener) {
    mRightClickListener = listener;
  }

  /**
   * Init the System-Tray
   * 
   * @param parent Parent-Frame
   * @param tooltip Tooltip
   * @return true, if successfull
   */
  public boolean init(JFrame parent, String tooltip) {
    if(JavaVersion.getVersion() >= JavaVersion.VERSION_1_6 && !OperatingSystem.isMacOs()) {
      try {
        mSystemTray = java.awt.SystemTray.getSystemTray();
        boolean isSupported = java.awt.SystemTray.isSupported();
        if(isSupported) {
          try {
            if(new File("imgs/TrayIcon.png").isFile()) {
              mTrayIcon = new TrayIcon(ImageIO.read(new File("imgs/TrayIcon.png")), tooltip);
            }
            else {            
              Dimension trayIconSize = getTrayIconSize();
              BufferedImage trayIconImage = null;
              
              if(trayIconSize.height > 16 && trayIconSize.height <= 32 && new File("imgs/tvbrowser32.png").isFile()) {
                trayIconImage = UiUtilities.scaleIconToBufferedImage(ImageIO.read(new File("imgs/tvbrowser32.png")),
                    trayIconSize.width-1, trayIconSize.height-1, BufferedImage.TYPE_INT_ARGB);
              }
              else if(trayIconSize.height > 32 && trayIconSize.height <= 48 && new File("imgs/tvbrowser48.png").isFile()) {
                trayIconImage = UiUtilities.scaleIconToBufferedImage(ImageIO.read(new File("imgs/tvbrowser48.png")),
                    trayIconSize.width-1, trayIconSize.height-1, BufferedImage.TYPE_INT_ARGB);                
              }
              else if(trayIconSize.height > 48 && new File("imgs/tvbrowser128.png").isFile()) {
                trayIconImage = UiUtilities.scaleIconToBufferedImage(ImageIO.read(new File("imgs/tvbrowser128.png")),
                    trayIconSize.width-1, trayIconSize.height-1, BufferedImage.TYPE_INT_ARGB);
              }
              else {
                trayIconImage = ImageIO.read(new File("imgs/tvbrowser16.png"));
              }
              
              mTrayIcon = new TrayIcon(trayIconImage, tooltip);
            }
          }catch(Throwable sizeFault) {
            mTrayIcon = new TrayIcon(ImageIO.read(new File("imgs/tvbrowser16.png")), tooltip);
          }
          
          mTrayParent = new JDialog();
          mTrayParent.setTitle("Tray-Menu");
  
          mTrayParent.setSize(0, 0);
          mTrayParent.setUndecorated(true);
          mTrayParent.setAlwaysOnTop(true);
          mTrayParent.setVisible(false);
          
          mLog.info("Java 6 Tray inited.");
        }
        else {
          mLog.info("Java 6 Tray is not supported on current platform.");
        }
        
        return isSupported;
      }catch(Exception e) {
        mLog.log(Level.SEVERE, "Java 6 Tray could not be inited.", e);
        mSystemTray = null;
        return false;
      }
    }
    else {
      mLog.info("Tray not supported: At least Java 6 is needed to get tray support.");
      return false;
    }
  }
  /**
   * Add Popup to Tray-Icon
   * @param trayMenu Popup
   */
  public void setTrayPopUp(JPopupMenu trayMenu) {
    mPopupMenu = trayMenu;  
    
    mPopupMenu.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          mTrayParent.setVisible(false);
      }

      public void popupMenuCanceled(PopupMenuEvent e) {}
    });
    
    mTrayIcon.addMouseListener(new MouseAdapter() {

      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)) {
          if(e.getClickCount() == 1 && mLeftClickListener != null)
            mLeftClickListener.actionPerformed(null);
          else if(e.getClickCount() == 2 && mLeftDoubleClickListener != null)
            mLeftDoubleClickListener.actionPerformed(null);
        }
      }
      
      public void mousePressed(MouseEvent e) {        
        if (e.isPopupTrigger()) {
          if(SwingUtilities.isRightMouseButton(e) && mRightClickListener != null)
            mRightClickListener.actionPerformed(null);

          showPopup(e.getPoint());
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          if(SwingUtilities.isRightMouseButton(e) && mRightClickListener != null)
            mRightClickListener.actionPerformed(null);

          showPopup(e.getPoint());
        }
      }
    });
  }
  
  private void showPopup(final Point p) {
    mTrayParent.setVisible(true);
    mTrayParent.toFront();
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Point p2 = computeDisplayPoint(p.x,p.y,mPopupMenu.getPreferredSize());
        
        mPopupMenu.show(mTrayParent,p2.x - mTrayParent.getLocation().x,p2.y - mTrayParent.getLocation().y);
      }
    });
  }
  
  /**
   * Compute the proper position for a popup
   */
  private Point computeDisplayPoint(int x, int y, Dimension dim) {
      if (x - dim.width > 0) x -= dim.width;
      if (y - dim.height > 0) y -= dim.height;
      return new Point(x, y);
  }

  /**
   * Set the visibility of the TrayIcon
   * @param b Visibility
   */
  public void setVisible(boolean b) {
    if(b) {
      try {
        mSystemTray.add(mTrayIcon);
      }catch(Exception e) {}
    }
    else {
      try {
        mSystemTray.remove(mTrayIcon);
      }catch(Exception e) {}      
    }
  }
  
  /**
   * Gets the useable size for tray icon.
   * <p>
   * @return The useable size for tray icon.
   */
  public Dimension getTrayIconSize() {
    try {
      if(mSystemTray != null) {
        return mSystemTray.getTrayIconSize();
      }
    }catch(Exception e) {}
    
    return null;
  }
  
  /**
   * Shows a balloon tip on the TV-Browser tray icon.
   * <p>
   * @param caption The caption of the displayed message.
   * @param message The message to display in the balloon tip.
   * @param messageType The type of the displayed balllon tip.
   * @return If the balloon tip could be shown.
   */
  public boolean showBalloonTip(String caption, String message, java.awt.TrayIcon.MessageType messageType) {
    if(mSystemTray != null) {
      mTrayIcon.displayMessage(caption,message,messageType);
      
      return true;
    }
    
    return false;
  }
}
