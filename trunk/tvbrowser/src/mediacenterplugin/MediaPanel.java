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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import devplugin.Plugin;
import devplugin.Program;

/**
 * @author bodum
 */
public class MediaPanel {

  private int mCount = 0;

  private MediaCenterFrame mParent;

  private BufferStrategy mStrategy;

  private boolean mStopLoop = false;

  private boolean mRepaint = false;

  private int mWidth, mHeight;

  private Image background;
  
  private MediaProgramPanel mProgramPanel;
  

  public MediaPanel(MediaCenterFrame parent, int width, int height, BufferStrategy strategy) {
    mParent = parent;
    mStrategy = strategy;

    mWidth = width;
    mHeight = height;

    background = createBackgroundImage();
    
    mProgramPanel = new MediaProgramPanel();
  }

  private Image createBackgroundImage() {
    Image image;

    try {
      URL url = this.getClass().getClassLoader().getResource("mediacenterplugin/images/default_blue.jpg");

      Image sourceImage = ImageIO.read(url);

      GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
          .getDefaultConfiguration();
      image = gc.createCompatibleImage(mWidth, mHeight, Transparency.OPAQUE);

      Graphics2D g2d = (Graphics2D) image.getGraphics();

      g2d.drawImage(sourceImage.getScaledInstance(mWidth, mHeight, Image.SCALE_SMOOTH), 0, 0, mWidth, mHeight, null);

      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      g2d.setColor(new Color(180, 180, 180, 100));
      g2d.fillRoundRect(50, 10, mWidth - 100, 100, 10, 10);

    } catch (Exception e) {
      e.printStackTrace();
      image = new BufferedImage(0, 0, BufferedImage.TYPE_INT_RGB);
    }

    return image;
  }

  public void startLoop() {
    Color box = new Color(180, 180, 180, 100);
    Font textFont = new Font("SansSerif", Font.BOLD, 14);

    while (!mStopLoop) {
      
      synchronized (this) {
      Graphics2D g2d = (Graphics2D) mStrategy.getDrawGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      g2d.drawImage(background, 0, 0, null);

      Program prg = Plugin.getPluginManager().getExampleProgram();

      drawProgram(g2d, prg, textFont);
      
      // finally, we've completed drawing so clear up the graphics
      // and flip the buffer over
      g2d.dispose();
      }

      mStrategy.show();
      mRepaint = false;

      try {
        while (!mRepaint && !mStopLoop) {
          Thread.sleep(10);
        }
      } catch (Exception e) {
      }
    }

    mParent.setVisible(false);
  }

  private void drawProgram(Graphics2D g2d, Program prg, Font textFont) {
    mProgramPanel.setProgram(prg, textFont, 480);
    mProgramPanel.paintPanel(g2d, textFont, 60, 10);
  }
  
  public synchronized void doPaint() {
    mRepaint = true;
  }

  public synchronized void nextLineInDescription() {
    mProgramPanel.nextLine();
    doPaint();
  }

  public synchronized void lastLineInDescription() {
    mProgramPanel.lastLine();
    doPaint();
  }
  
  
  public synchronized void nextDay() {
  }

  public synchronized void lastDay() {
  }

  public void close() {
    mStopLoop = true;
  }

}