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

import java.awt.Image;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

/**
 * A class that uses reflection to get support for Java 6
 * tray classes. If Java 6 isn't installed the tray simply
 * doesn't work but the program runs normal with Java 5:
 * 
 * Copied from WinTVCap_GUI and changed for TV-Browser. 
 * 
 * @author René Mach
 */
public class TrayIcon {
  private Class<?> mClass;
  private Object mTrayIcon;
  
  /**
   * Creates an instance of TrayIcon.
   * 
   * @param icon The icon image.
   * @param toolTip The tray tooltip text.
   */
  public TrayIcon(BufferedImage icon, String toolTip) {
    try {
      mClass = Class.forName("java.awt.TrayIcon");
      mTrayIcon = mClass.getConstructor(new Class[] {Image.class,String.class}).newInstance(new Object[] {icon,toolTip});
    }catch(Exception e) {} 
  }
  
  /**
   * Sets the tooltip text of the tray icon.
   * 
   * @param toolTip The new tooltip text.
   */
  public void setToolTip(String toolTip) {
    try {
      mClass.getMethod("setToolTip",new Class[] {String.class}).invoke(mTrayIcon,new Object[] {toolTip});
    } catch (Exception e) {
    }
  }
  
  /**
   * Gets the Java 6 tray icon object.
   * 
   * @return The Java 6 tray icon object.
   */
  public Object getTrayIcon() {
    return mTrayIcon;
  }
  
  /**
   * Adds a mouse listener to the tray icon.
   * 
   * @param listener The listener to add.
   */
  public void addMouseListener(MouseListener listener)  {
    try {
      mClass.getMethod("addMouseListener",new Class[] {MouseListener.class}).invoke(mTrayIcon, new Object[] {listener});
    }catch(Exception e) {}
  }
}
