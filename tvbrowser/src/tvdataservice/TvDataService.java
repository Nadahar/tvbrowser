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

package tvdataservice;

import util.exc.TvBrowserException;

import devplugin.ChannelDayProgram;

/**
 * To write your own TV data service implement this interface.
 *
 * @author Martin Oberhauser
 */
public interface TvDataService {

 
  /**
   * Called by the host-application before starting to download.
   */
  public void connect() throws TvBrowserException;

  /**
   * Returns the whole program of the channel on the specified date.
   */
  public ChannelDayProgram downloadDayProgram(devplugin.Date date,
    devplugin.Channel channel) throws TvBrowserException;

  /**
   * After the download is done, this method is called. Use this method for clean-up.
   */
  public void disconnect() throws TvBrowserException;

	/**
	 * Called by the host-application during start-up. Implements this method to
	 * load your dataservices settings from the file system.
	 */
  public void loadSettings(java.util.Properties settings);

  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public java.util.Properties storeSettings();

  public SettingsPanel getSettingsPanel();

  public boolean hasSettingsPanel();

  /**
   * Gets the list of the channels that are available by this data service.
   */
  public devplugin.Channel[] getAvailableChannels();
  
  /**
   * Gets information about this TvDataService
   * 
   */
  public devplugin.PluginInfo getInfo();
  
  
 // public int getTimeZone

}