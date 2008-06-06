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
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import util.misc.JavaVersion;
import util.ui.UiUtilities;

/**
 * Tray for TV-Browser on systems
 * with Java 6 and tray support.
 * 
 * @author Ren� Mach
 */
public class Java6Tray {
  /** Logger */
  private static java.util.logging.Logger mLog
  = java.util.logging.Logger.getLogger(Java6Tray.class.getName());
  
  private Class<?> mClass;
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
    if(JavaVersion.getVersion() >= JavaVersion.VERSION_1_6) {
      try {
        mClass = Class.forName("java.awt.SystemTray");
        
        Class<?> clazz = Class.forName("java.awt.SystemTray");
        boolean value = (Boolean)clazz.getMethod("isSupported",new Class[] {}).invoke(clazz,new Object[] {});
        
        if(value) {
          String trayIconFile = "imgs/TrayIcon.png";
          
          if(new File(trayIconFile).isFile()) {
            mTrayIcon = new TrayIcon(ImageIO.read(new File(trayIconFile)), tooltip);
          }
          else {
            try {
              Dimension trayIconSize = getTrayIconSize();
              BufferedImage trayIconImage = null;
              
              if(trayIconSize.height > 16 && trayIconSize.height <= 32) {                
                trayIconImage = UiUtilities.scaleIconToBufferedImage(ImageIO.read(new File("imgs/tvbrowser32.png")),
                    trayIconSize.width-1, trayIconSize.height-1, BufferedImage.TYPE_INT_ARGB);
              }
              else if(trayIconSize.height > 32 && trayIconSize.height <= 48) {
                trayIconImage = UiUtilities.scaleIconToBufferedImage(ImageIO.read(new File("imgs/tvbrowser48.png")),
                    trayIconSize.width-1, trayIconSize.height-1, BufferedImage.TYPE_INT_ARGB);                
              }
              else if(trayIconSize.height > 48) {
                trayIconImage = UiUtilities.scaleIconToBufferedImage(ImageIO.read(new File("imgs/tvbrowser128.png")),
                    trayIconSize.width-1, trayIconSize.height-1, BufferedImage.TYPE_INT_ARGB);
              }
              else {
                trayIconImage = ImageIO.read(new File("imgs/tvbrowser16.png"));
              }
              
              mTrayIcon = new TrayIcon(trayIconImage, tooltip);
            }catch(Exception sizeFault) {sizeFault.printStackTrace();
              mTrayIcon = new TrayIcon(ImageIO.read(new File("imgs/tvbrowser16.png")), tooltip);
            }
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
        
        return value;
      }catch(Exception e) {
        mLog.log(Level.SEVERE, "Java 6 Tray could not be inited.", e);
        mClass = null;
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
    mPopupMenu.setVisible(true);
    mPopupMenu.setVisible(false);
    
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
      };
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
        Object o = mClass.getMethod("getSystemTray",new Class[] {}).invoke(mClass,new Object[] {});      
        mClass.getMethod("add", new Class[] {mTrayIcon.getTrayIcon().getClass()}).invoke(o,mTrayIcon.getTrayIcon());      
      }catch(Exception e) {}
    }
    else {
      try {
        Object o = mClass.getMethod("getSystemTray",new Class[] {}).invoke(mClass,new Object[] {});      
        mClass.getMethod("remove", new Class[] {mTrayIcon.getTrayIcon().getClass()}).invoke(o,mTrayIcon.getTrayIcon());      
      }catch(Exception e) {}      
    }
  }
  
  public Dimension getTrayIconSize() {
    try {
      Class<?> clazz = Class.forName("java.awt.SystemTray");
      
      if(clazz != null) {
        Object o = clazz.getMethod("getSystemTray",new Class[] {}).invoke(clazz,new Object[] {});
        return (Dimension)clazz.getMethod("getTrayIconSize", new Class[] {}).invoke(o, new Object[] {});
      }
    }catch(Exception e) {e.printStackTrace();}
    
    return null;
  }
}
