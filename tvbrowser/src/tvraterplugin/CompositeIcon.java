/*
 * TV-Browser Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package tvraterplugin;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * This class is a Wrapper for two Icons.
 * 
 * @author bodo
 */
public class CompositeIcon implements Icon {
	
	/**
	 * Icon 1
	 */
	private Icon icon1;
	/**
	 * Icon 2
	 */
	private Icon icon2;
	
	/**
	 * Creates the CompositeIcon
	 * 
	 * @param icon1 Icon 1
	 * @param icon2 Icon 2
	 */
	public CompositeIcon(Icon icon1, Icon icon2) {
		this.icon1 = icon1;
		this.icon2 = icon2;
	}
	
	/**
	 * Gets the Height
	 * @retun Height
	 */
	public int getIconHeight() {
		return Math.max(icon1.getIconHeight(), icon2.getIconHeight());
	}
	
	/**
	 * Gets the Width
	 * @return Width
	 */
	public int getIconWidth() {
		return icon1.getIconWidth() + icon2.getIconWidth() + 2;
	}
	
	/**
	 * Paints the Icon
	 * @param c Component to paint on
	 * @param g Graphics-Object to use
	 * @param x X-Value
	 * @param y Y-Value
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		icon1.paintIcon(c, g, x, y);
		icon2.paintIcon(c, g, x + icon1.getIconWidth() + 2, y);
	}
}