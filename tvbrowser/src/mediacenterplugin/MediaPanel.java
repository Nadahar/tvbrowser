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
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;

/**
 * @author bodum
 */
public class MediaPanel {

  int mCount = 0;

  MediaCenterFrame mParent;

  BufferStrategy mStrategy;

  boolean stopLoop = false;

  boolean repaint = false;
  
  public MediaPanel(MediaCenterFrame parent, BufferStrategy strategy) {
    mParent = parent;
    mStrategy = strategy;
  }

  public void startLoop() {
    Color bgknd = new Color(30, 30, 150);
    Color box = bgknd.brighter();
    Font textFont = new Font("SansSerif", Font.BOLD, 20);

    while (!stopLoop) {
      Graphics2D g2d = (Graphics2D) mStrategy.getDrawGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      g2d.setColor(bgknd);

      g2d.fillRect(0, 0, mParent.getWidth(), mParent.getHeight());

      g2d.setColor(box);
      g2d.fillRoundRect(50, 10, mParent.getWidth()-100, 100, 10, 10);

      DrawToolBox.drawFontWithShadow(g2d, "Hello2 : " + mCount, 100, 40, 2, textFont);

      // finally, we've completed drawing so clear up the graphics
      // and flip the buffer over
      g2d.dispose();
      mStrategy.show();
      repaint = false;

      try {
        while (!repaint && !stopLoop) {
          Thread.sleep(10);
        }
      } catch (Exception e) {
      }
    }
    
    mParent.setVisible(false);
  }


  public void doPaint() {
    repaint = true;
  }
  
  public void nextDay() {
    mCount++;
    doPaint();
  }

  public void lastDay() {
    mCount--;
    doPaint();
  }

  public void close() {
    stopLoop = true;
  }

}