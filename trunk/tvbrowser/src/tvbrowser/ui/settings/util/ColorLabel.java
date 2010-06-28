/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.ui.settings.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 01.05.2005
 * Time: 14:21:28
 */

public class ColorLabel extends JLabel {

  private Color mColor, mStandardColor;

  public ColorLabel(Color color) {
    super();
    mStandardColor = null;
    setColor(color);
  }

  public void setColor(Color color) {
    mColor = color;
    setIcon(createIcon());
  }

  public Color getColor() {
    return mColor;
  }

  /**
   * Returns the standard color of this label
   * 
   * @return Color
   */
  public Color getStandardColor() {
    return mStandardColor;
  }

  /**
   * Sets the standard color of this label
   * 
   * @param color Standard color
   */
  public void setStandardColor(Color color) {
    mStandardColor = color;
  }

  private Icon createIcon() {
    BufferedImage img = new BufferedImage(25, 15, BufferedImage.TYPE_INT_RGB);

    Graphics2D g = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(img);

    g.setColor(Color.WHITE);
    g.fillRect(1, 1, 23, 13);
    g.setColor(mColor);
    g.fillRect(1, 1, 23, 13);
    g.setColor(Color.BLACK);
    g.drawRect(0, 0, 24, 14);
    
    ImageIcon icon = new ImageIcon(img);

    return icon;
  }

}
