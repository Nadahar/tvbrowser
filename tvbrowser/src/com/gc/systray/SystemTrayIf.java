package com.gc.systray;

import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

/**
 * A generic Interface for the different SystemTray-Libraries
 * 
 * @author bodum
 */
public interface SystemTrayIf {

  /**
   * Init the System-Tray
   * 
   * @param parent Parent-Frame
   * @param image Image-File for Tray-Icon
   * @param tooltip Tooltip
   * @return true, if successfull
   */
  public boolean init(JFrame parent, String image, String tooltip);

  /**
   * Set the visibility of the TrayIcon
   * @param b Visibility
   */
  public void setVisible(boolean b);

  /**
   * Add a Left-Click-Action
   * @param listener Action that is triggered on left click
   */
  public void addLeftClickAction(ActionListener listener);  
  
  /**
   * Add a Left-DoubleClick-Action
   * @param listener Action that is triggered on left doubleclick
   */
  public void addLeftDoubleClickAction(ActionListener listener);

  
  /**
   * Add a Right-Click-Action
   * @param listener Action that is triggered on right click
   */
  public void addRightClickAction(ActionListener listener);
  
  /**
   * Add Popup to Tray-Icon
   * @param trayMenu Popup
   */
  public void setTrayPopUp(JPopupMenu trayMenu);
  
}
