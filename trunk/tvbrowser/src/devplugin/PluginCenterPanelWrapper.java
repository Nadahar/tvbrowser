/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package devplugin;

/**
 * A class that is used to support the
 * adding of panels in the center window of
 * TV-Browser.
 * 
 * Create an instance of this interface to use it
 * for your Plugin.
 * 
 * @author René Mach
 * @since 3.2
 */
public abstract class PluginCenterPanelWrapper {
  /**
   * Gets the PluginCenterPanel that should be
   * available for your Plugin.
   * <p>
   * @return The available {@link PluginCenterPanel}
   */
  public abstract PluginCenterPanel[] getCenterPanels();
  
  /**
   * Informs this wrapper about a program selection
   * of the TV-Browser program table.
   * <p>
   * @param prog The selected program.
   */
  public void programSelected(Program prog) {
    
  }
  
  /**
   * Informs this wrapper about a program scrolling
   * of the TV-Browser program table.
   * <p>
   * @param prog The program to scroll to.
   */
  public void programScrolled(Program prog) {
    
  }
  
  /**
   * Informs this wrapper about a changing of the 
   * used program filter of the TV-Browser program
   * table.
   * <p>
   * @param filter The new selected filter.
   */
  public void filterSelected(ProgramFilter filter) {
    
  }
  
  /**
   * Informs this wrapper about a changing of the
   * selected channel in the TV-Browser program
   * table.
   * <p>
   * @param channel The new selected channel.
   */
  public void scrolledToChannel(Channel channel) {
    
  }
  
  /**
   * Informs this wrapper about a changing of the
   * selected date of the TV-Browser program
   * table.
   * <p>
   * @param date The new selected date.
   * @param minute The minute of the date to scroll to.
   */
  public void scrolledToDate(Date date) {
    
  }
  
  /**
   * Informs this wrapper about a changing of the
   * selected time of the TV-Browser program
   * table.
   * <p>
   * @param time The new selected time-
   */
  public void scrolledToTime(int time) {
    
  }
  
  /**
   * Informs this wrapper about a scrolling to
   * now in the TV-Browser program table.
   */
  public void scrolledToNow() {
    
  }
  
  /**
   * Informs this wrapper about a time event.
   * (It is triggered by the program table of
   * TV-Browser, normally every minute one time.)
   */
  public void timeEvent() {
    
  }
}
