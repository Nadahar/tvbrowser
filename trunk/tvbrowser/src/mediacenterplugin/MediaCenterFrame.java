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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package mediacenterplugin;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.JPanel;

import util.ui.ImageUtilities;

/**
 * This class represents the Frame.
 * 
 * It handles the Fullscreen/Windowed switching
 * 
 * @author bodum
 */
public class MediaCenterFrame extends Canvas {
  
  /** Current Mouse Position */
  private Point mLocation;
  /** Last MouseEvent */
  private MouseEvent mPressed;
  /** The MediaPanel to use*/
  private MediaPanel mMediaPanel;
  /** JFrame that is shown */
  private JFrame mFrame; 
  
  /**
   * Creates the Frame
   * 
   * @param x Width
   * @param y Height
   * @param fullscreen Fullscreen-Mode ?
   */
  public MediaCenterFrame(MediaCenterPlugin plugin, int x, int y, boolean fullscreen) {
    // create a frame to contain our game
    mFrame = new JFrame("TV-Browser MediaCenter");

    mFrame.setIconImage(ImageUtilities.createImageFromJar("mediacenterplugin/images/krdc16.png",
        MediaCenterPlugin.class));
    
    // get hold the content of the frame and set up the resolution of the game
    JPanel panel = (JPanel) mFrame.getContentPane();
    panel.setPreferredSize(new Dimension(x, y));
    panel.setLayout(null);

    // setup our canvas size and put it into the content of the frame
    setBounds(0, 0, x, y);
    panel.add(this);

    // finally make the window visible
    mFrame.pack();
    mFrame.setResizable(false);
    mFrame.setVisible(true);
    createBufferStrategy(2);
    BufferStrategy buffer = getBufferStrategy();

    mMediaPanel = new MediaPanel(plugin, this, x, y, buffer);

    KeyMediaAdapter keyAdapter = new KeyMediaAdapter(mMediaPanel);

    addKeyListener(keyAdapter);

    if (fullscreen) {
      GraphicsConfiguration gc = getGraphicsConfiguration();
      GraphicsDevice gd = gc.getDevice();
      mFrame.setUndecorated(true);

      DisplayMode oldDisplayMode = gd.getDisplayMode();
      try {
        gd.setFullScreenWindow(mFrame);
        gd.setDisplayMode(new DisplayMode(x, y, 16, 85));
      } finally {
        gd.setDisplayMode(oldDisplayMode);
        gd.setFullScreenWindow(null);
      }
    } else {
      addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent me) {
          mPressed = me;
        }

      });
      addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseDragged(MouseEvent me) {
          mLocation = mFrame.getLocation(mLocation);
          int x = mLocation.x - mPressed.getX() + me.getX();
          int y = mLocation.y - mPressed.getY() + me.getY();
          mFrame.setLocation(x, y);
        }

      });
    }

    // add a listener to respond to the user closing the window. If they
    // do we'd like to exit the game
    mFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        mMediaPanel.close();
      }
    });    
    
    // request the focus so key events come to us
    requestFocus();

    mMediaPanel.startLoop();
  }

  /**
   * Make the Frame Visible/Invisible
   * @param visible true if visible
   */
  public void setVisible(boolean visible) {
    mFrame.setVisible(visible);
  }

  /**
   * Map Paint-Methods to the MediaPanel
   */
  public void paint(Graphics g) {
    if (mMediaPanel != null) {
      mMediaPanel.doPaint();
    }
  }

}