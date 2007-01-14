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
package com.gc.systray;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Tray for TV-Browser on systems
 * with Java 6 and tray support.
 * 
 * @author René Mach
 */
public class Java6Tray implements SystemTrayIf {
  
  private Class mClass;
  private TrayIcon mTrayIcon;
  private JPopupMenu mPopupMenu;
  private ActionListener mLeftClickListener, mLeftDoubleClickListener, mRightClickListener;
  
  private JDialog mTrayParent;
    
  public void addLeftClickAction(ActionListener listener) {
    mLeftClickListener = listener;
  }

  public void addLeftDoubleClickAction(ActionListener listener) {
    mLeftDoubleClickListener = listener;
  }

  public void addRightClickAction(ActionListener listener) {
    mRightClickListener = listener;
  }

  public boolean init(JFrame parent, String image, String tooltip) {
    try {
      mClass = Class.forName("java.awt.SystemTray");
      
      Class clazz = Class.forName("java.awt.SystemTray");
      boolean value = (Boolean)clazz.getMethod("isSupported",new Class[] {}).invoke(clazz,new Object[] {});
      
      if(value) {
        mTrayIcon = new TrayIcon(ImageIO.read(new File(image)), tooltip);
       
        mTrayParent = new JDialog();
        mTrayParent.setTitle("Tray-Menu");

        mTrayParent.setSize(0, 0);
        mTrayParent.setUndecorated(true);
        mTrayParent.setVisible(false);
      }
      
      return value;
    }catch(Exception e) {
      mClass = null;
      return false;
    }
  }

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
}
