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
package tvbrowser.extras.programinfo;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * This Class rendes a Horizontal Line
 *
 * @author bodum
 */
public class HorizontalLine extends JComponent {

  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_OFF);

    g2d.setColor(getBackground());
    g2d.fillRect(0, 0, getWidth(), getHeight());
    
    g2d.setColor(getForeground());
    g2d.setStroke( new BasicStroke( 1,
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
        1, new float[]{ 1 }, 0 ));
    g2d.drawLine(0, 0, getWidth(), 0);
  }
  
}