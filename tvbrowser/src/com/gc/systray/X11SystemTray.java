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

import util.misc.JavaVersion;

public class X11SystemTray extends MouseAdapter implements SystemTrayIf {

  private X11SystrayManager mManager;

  private ArrayList mLeftAction = new ArrayList();

  private JPopupMenu mPopupMenu;

  private JDialog mTrayParent;

  public boolean init(JFrame parent, String image, String tooltip) {
    
    if (JavaVersion.getVersion() < JavaVersion.VERSION_1_5) {
      return false;
    }
    
    mManager = new X11SystrayManager(image, tooltip);
    
    if (!mManager.isLoaded()) {
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

  public void setVisible(boolean b) {
    if (mManager.isLoaded()) {
      if (b) {
        mManager.systrayShow();
      } else {
        mManager.systrayHide();
      }
    }
  }

  public void addLeftDoubleClickAction(ActionListener listener) {
    mLeftAction.add(listener);
  }

  public void setTrayPopUp(JPopupMenu popupMenu) {
    mPopupMenu = popupMenu;
    popupMenu.setVisible(true);
    popupMenu.setVisible(false);
  }

  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger() && mPopupMenu != null) {
      // we have to calculate th leftTopX and Y value of the popupmenu
      int leftTopX, leftTopY;

      leftTopX = e.getPoint().x - mPopupMenu.getWidth();
      leftTopY = e.getPoint().y - mPopupMenu.getHeight();

      mTrayParent.setVisible(true);
      mPopupMenu.show(mManager.getSystemTray(), leftTopX, leftTopY);
    } else if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
      for (int i = 0; i < mLeftAction.size(); i++) {
        ((ActionListener) mLeftAction.get(i)).actionPerformed(null);
      }
    }
  }
}