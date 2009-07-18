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
package util.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * This is a Button that allows the selection of a Color
 */
public class ColorButton extends JButton implements ActionListener{
    /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ColorButton.class);
    
    /** Color */
    private Color mColor;
    
    /** Standard color*/
    private Color mStandardColor;
    
    /**
     * Creates the Button.
     * Default Color is BLACK
     */
    public ColorButton() {
        super();
        mColor = Color.BLACK;
        mStandardColor = null;
        updateIcon();
        addActionListener(this);
    }

    /**
     * Creates the Button with a certain color
     * @param color
     */
    public ColorButton(Color color) {
        super();
        mColor = color;
        mStandardColor = null;
        updateIcon();
        addActionListener(this);
    }

    /**
     * Update the Icon
     */
    private void updateIcon() {
        setIcon(createIcon());
    }

    /**
     * Create a small Icon with the current Color
     * @return Icon with Color
     */
    private Icon createIcon() {
        BufferedImage img = new BufferedImage(50, 10, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(img);
        
        g.setColor(Color.WHITE);
        g.fillRect(0,0,50,10);
        g.setColor(mColor);
        g.fillRect(0,0,50,10);
        
        ImageIcon icon = new ImageIcon(img);
        
        return icon;
    }
    
    /**
     * Sets the standard color.
     * @param color The standard color.
     */
    public void setStandardColor(Color color) {
      mStandardColor = color;
    }

    public void actionPerformed(ActionEvent e) {
        mColor = AlphaColorChooser.showDialog(UiUtilities.getBestDialogParent(getParent()),
                mLocalizer.msg("ChooseColor", "Please choose the Color"), mColor, mStandardColor);
        
        updateIcon();
    }
    
    /**
     * Set the Color
     * @param c Color to use
     */
    public void setColor(Color c) {
        mColor = c;
        updateIcon();
    }
    
    /**
     * Returns the current selected Color
     */
    public Color getColor() {
        return mColor;
    }
}