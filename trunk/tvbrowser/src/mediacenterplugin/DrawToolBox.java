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

/**
 * This class is a utility-class for various Paint-Routines
 * 
 * @author bodum
 */
public class DrawToolBox {

  /**
   * Draws a white Text with a Shadows 
   * @param g2d Graphics-Object to use
   * @param string Text to draw
   * @param x X-Offset
   * @param y Y-Offset
   * @param offset Offest of the Shadow
   * @param textFont Font to use
   */
  public static void drawFontWithShadow(Graphics2D g2d, String string, int x, int y, int offset,  Font textFont) {
    g2d.setColor(Color.BLACK);
    g2d.setFont(textFont);
    g2d.drawString(string, x+offset, y+offset);
    
    g2d.setColor(Color.WHITE);
    g2d.drawString(string, x, y);
  }
  
}