/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package util.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import devplugin.Channel;


/**
 * A Label for Channels. It shows the Icon and/or the Channel-Name
 */
public class ChannelLabel extends JLabel {

    /**
     * Creates the ChannelLabel
     */
    public ChannelLabel() {
    }

    /**
     * Creates the ChannelLabel
     * @param ch Channel to display
     */
    public ChannelLabel(Channel ch) {
        setChannel(ch);
    }
    
    /**
     * Sets the Channel to display
     * @param ch Channel to display
     */
    public void setChannel(Channel ch) {
        setIcon(ch.getIcon());
        setText(ch.getName());
    }
    
    /**
     * Sets the Icon
     * @param ic Icon
     */
    public void setIcon(Icon ic) {
        if (ic != null) {
            BufferedImage img = new BufferedImage(42, 22,BufferedImage.TYPE_INT_RGB);
        
        	Graphics2D g = img.createGraphics();
        
        	g.setColor(Color.BLACK);
        	g.fillRect(0, 0, 42, 22);
        	
        	g.setColor(Color.WHITE);
        	g.fillRect(1, 1, 40, 20);
        	
        	int x = 1+20-ic.getIconWidth() / 2;
        	int y = 1+10-ic.getIconHeight() / 2;

        	ic.paintIcon(this, g, x, y);
        
        	ImageIcon imgIcon = new ImageIcon(img);
        	super.setIcon(imgIcon);
        } else {
            super.setIcon(null);
        }
    }
    

    /**
     * Sets the Text
     * @param text Text
     */
    public void setText(String text) {
        super.setText(text);
    }
}
