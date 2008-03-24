package com.gc.systray;

import java.awt.Point;

/**
 * This interface should be implemented by a class that wants to manage
 * the icon events.
 *
 * @author <a href="mail:gciubotaru@yahoo.com">George Ciubotaru</a>
 * @version 1.0
 */
public interface SystemTrayIconListener
{
    /**
     * This method will be called when the icon is left clicked
     *
     * @param pos the mouse position on the screen when mouse click
     * @param source the SystemTrayIconManager instance
     */
    public void mouseClickedLeftButton(Point pos, SystemTrayIconManager source);

    /**
     * This method will be called when the icon is right clicked
     *
     * @param pos the mouse position on the screen when mouse click
     * @param source the SystemTrayIconManager instance
     */
    public void mouseClickedRightButton(Point pos, SystemTrayIconManager source);

    /**
     * This method will be called when the icon is double right clicked
     *
     * @param pos the mouse position on the screen when mouse click
     * @param source the SystemTrayIconManager instance
     */
    public void mouseRightDoubleClicked(Point pos, SystemTrayIconManager source);

    /**
     * This method will be called when the icon is double left clicked
     *
     * @param pos the mouse position on the screen when mouse click
     * @param source the SystemTrayIconManager instance
     */
    public void mouseLeftDoubleClicked(Point pos, SystemTrayIconManager source);
}
