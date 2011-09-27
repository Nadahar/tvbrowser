/*
 * TV-Browser
 * Copyright (C) 2011 TV-Browser team (dev@tvbrowser.org)
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
package util.ui.persona;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * A JPanel that contains the header and footer images of the Persona.
 * <p>
 * @author René Mach
 * @since 3.1
 */
public class PersonaBackgroundPanel extends JPanel {
  protected void paintComponent(Graphics g) {
    if(Persona.getInstance().getAccentColor() != null) {
      g.setColor(Persona.getInstance().getAccentColor());
      g.fillRect(0,0,getWidth(),getHeight());
    }
    else {
      super.paintComponent(g);
    }
    
    BufferedImage headerImage = Persona.getInstance().getHeaderImage();
    BufferedImage footerImage = Persona.getInstance().getFooterImage();
    
    if(headerImage != null) {
      g.drawImage(headerImage,0,0,getWidth(),headerImage.getHeight(),headerImage.getWidth()-getWidth(),0,headerImage.getWidth(),headerImage.getHeight(),null);
    }
    if(footerImage != null) {
      g.drawImage(footerImage,0,getHeight()-footerImage.getHeight(),footerImage.getWidth(),getHeight(),0,0,footerImage.getWidth(),footerImage.getHeight(),null);
    }
  }
}
