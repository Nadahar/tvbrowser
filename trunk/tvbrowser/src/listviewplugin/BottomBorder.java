/*
 * Created on 11.04.2004
 */
package listviewplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;


/**
 * Paints a thin line at the Bottom of the Component
 * @author bodo
 */
public class BottomBorder implements Border {
    /** Color to use */
    private Color _color;
    
    /**
     * Creates the BottomBorder
     * @param color Color to use
     */
    public BottomBorder(Color color) {
        _color = color;
    }
    
    
    /* (non-Javadoc)
     * @see javax.swing.border.Border#isBorderOpaque()
     */
    public boolean isBorderOpaque() {
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
            int height) {
        g.setColor(_color);
        g.drawLine(x, y+height-1, x+width, y+height-1);
    }

    /* (non-Javadoc)
     * @see javax.swing.border.Border#getBorderInsets(java.awt.Component)
     */
    public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 1, 0);
    }

}