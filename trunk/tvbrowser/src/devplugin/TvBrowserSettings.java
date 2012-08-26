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

package devplugin;

import java.awt.Color;

/**
 * Provides information of the current user settings.
 * 
 */
public interface TvBrowserSettings {

  /**
   * @return the directory name of the user settings (e.g linux: ~/home/.tvbrowser/);
   */
  public String getTvBrowserUserHome();

  /**
   * @return the times of the time buttons (in minutes)
   */
  public int[] getTimeButtonTimes();

  /**
   * @return the date of the previous donwload
   */
  public Date getLastDownloadDate();
  
  /**
   * @return The default network connection timeout
   * @since 2.5.3
   */
  public int getDefaultNetworkConnectionTimeout();

  /**
   * Gets the color for a marking priority.
   * 
   * @param priority The priority to get the color for.
   * @return The color for the given priority or <code>null</code>
   *         if the given priority don't exists.
   * @since 2.6
   */
  public Color getColorForMarkingPriority(int priority);

  /**
   * Gets the start of day time of the program table.
   * 
   * @return start of day in minutes since midnight
   * @since 2.6
   */
  public int getProgramTableStartOfDay();

  /**
   * Gets the end of day time of the program table.
   * 
   * @return end of day in minutes since midnight
   * @since 2.6
   */
  public int getProgramTableEndOfDay();
  
  /**
   * Gets the light color of an on air program.
   * 
   * @return The light color of an on air program.
   * @since 2.6
   */
  public Color getProgramPanelOnAirLightColor();
  
  /**
   * Gets the dark color of an on air program.
   * 
   * @return The dark color of an on air program.
   * @since 2.6
   */
  public Color getProgramPanelOnAirDarkColor();
  
  /**
   * Gets if the marking have a colored border.
   * 
   * @return <code>True</code> if the border is painted,
   * <code>false</code> otherwise.
   * @since 2.6
   */
  public boolean isMarkingBorderPainted();
  
  /**
   * Gets if extra space for the plugin icons is used.
   * 
   * @return <code>True</code> if extra space for the plugin icons is used,
   * <code>false</code> otherwise.
   * @since 2.6
   */
  public boolean isUsingExtraSpaceForMarkIcons();
  
  /**
   * Gets the time to wait for data automatically update.
   * 
   * @return The time to wait before performing data update in seconds.
   * @since 2.7.1
   */
  public short getAutoDownloadWaitingTime();
  
  /**
   * Gets the color of the mouse over function of the program table.
   * <p>
   * @return The color of the mouse over function of the program table
   * or <code>null</code> if that function is deactivated.
   * @since 3.2
   */
  public Color getProgramTableMouseOverColor();
  
  /**
   * Gets the foreground color of the program table.
   * <p>
   * @return The foreground color of the program table.
   * @since 3.2
   */
  public Color getProgramTableForegroundColor();
  
  /**
   * Gets the color for selection of a program panel.
   * <p>
   * @return The color for selection of a program panel.
   * @since 3.2
   */
  public Color getProgramPanelSelectionColor();
}
