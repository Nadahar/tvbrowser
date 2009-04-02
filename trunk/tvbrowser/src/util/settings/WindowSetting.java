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
package util.settings;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A class with the position and size settings for a window.
 * 
 * @author René Mach
 * @since 2.7
 */
public final class WindowSetting {
  private Window mWindowCache;
  
  private int mXPos;
  private int mYPos;
  private int mWidth;
  private int mHeight;

  private Dimension mMinSize;
  
  
  /**
   * Creates an instance of this class with the values read from the stream.
   * 
   * @param in The stream to read the settings from.
   * @throws IOException Thrown if something went wrong.
   */
  public WindowSetting(ObjectInputStream in) throws IOException {
    in.readInt(); // read version;
    
    mXPos = in.readInt();
    mYPos = in.readInt();
    mWidth = in.readInt();
    mHeight = in.readInt();
  }
  
  /**
   * Creates an instance of this class with the default setting -1 for all values.
   * @param size The default size of the window.
   */
  public WindowSetting(Dimension size) {
    mXPos = -1;
    mYPos = -1;
    
    if(size == null) {
      mWidth = -1;
      mHeight = -1;
    }
    else {
      mWidth = size.width;
      mHeight = size.height;
    }
  }
  
  /**
   * Saves the values in the given stream.
   * 
   * @param out The stream to save the values in.
   * @throws IOException
   */
  public void saveSettings(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // write version
    
    out.writeInt(mXPos);
    out.writeInt(mYPos);
    out.writeInt(mWidth);
    out.writeInt(mHeight);
  }
  
  /**
   * Sets the values to the given window.
   * 
   * @param window The window to set the values for.
   */
  public void layout(final Window window) {
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    
    int width = mWidth;
    int height = mHeight;
    
    if(width < 20 || width > d.width + 10) {
      window.pack();
      width = window.getWidth();
    }
    
    if(height < 20 || height > d.height + 10) {
      window.pack();
      height = window.getHeight();
    }

    // never make the dialog smaller than minimum size
    window.pack();
    mMinSize = window.getMinimumSize();
    if (width < mMinSize.width) {
      width = mMinSize.width;
    }
    if (height < mMinSize.height) {
      height = mMinSize.height;
    }
    
    window.setSize(width, height);
    
    if(mXPos < 0 || mYPos < 0 || mXPos > d.width || mYPos > d.height) {
      window.setLocationRelativeTo(null);
    }
    else {
      window.setLocation(mXPos, mYPos);
    }
    
    if(mWindowCache == null || !window.equals(mWindowCache)) {
      window.addComponentListener(new ComponentListener() {
  
        public void componentHidden(ComponentEvent e) {
          savePos(e);
        }
  
        public void componentMoved(ComponentEvent e) {
          savePos(e);
        }
  
        public void componentResized(ComponentEvent e) {
          if (mMinSize != null) {
            int width = window.getWidth();
            int height = window.getHeight();
            boolean resize = false;
            if (width < mMinSize.getWidth()) {
              width = mMinSize.width;
              resize = true;
            }
            if (height < mMinSize.getHeight()) {
              height = mMinSize.height;
              resize = true;
            }
            if (resize) {
              window.setSize(width, height);
            }
          }
          saveSize(e);
        }
  
        public void componentShown(ComponentEvent e) {
          savePos(e);
        }
        
        private void savePos(ComponentEvent e) {
          mXPos = e.getComponent().getX();
          mYPos = e.getComponent().getY();
        }

        private void saveSize(ComponentEvent e) {
          mWidth = e.getComponent().getWidth();
          mHeight = e.getComponent().getHeight();
        }
      });
    }
    
    mWindowCache = window;
  }
  
  
  public String toString() {
    return new StringBuilder("x:").append(mXPos).append(" y:").append(mYPos)
        .append(" ").append(mWidth).append("x").append(mHeight).toString();
  }
}

