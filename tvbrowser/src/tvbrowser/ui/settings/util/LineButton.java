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
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * A Simple Button that contains a Line as Icon.
 * 
 * @author bodum
 */
public class LineButton extends JButton {

  public LineButton() {
    super();
    
    BufferedImage image = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.setColor(new Color(255,0,0,0));
    g2.fillRect(0, 0, 22, 22);
    
    g2.setColor(getForeground());
    g2.drawLine(0, 11, 22, 11);
    setIcon(new ImageIcon(image));
  }
  
}
