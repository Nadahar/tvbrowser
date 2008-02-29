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

import java.util.Properties;

import util.exc.TvBrowserException;
import devplugin.Channel;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;

/**
 *
 * To write your own TV listings service implement this interface.
 *
 * @deprecated use {@link devplugin.TvDataService} instead.
 *
 * @author Martin Oberhauser
 */
public interface TvDataService {
  
  public void setWorkingDirectory(java.io.File dataDir);

  /**
   * Updates the TV listings provided by this data service.
   * 
   * @throws TvBrowserException
   */  
  public void updateTvData(TvDataUpdateManager updateManager,
    Channel[] channelArr, Date startDate, int dateCount, ProgressMonitor monitor)
    throws TvBrowserException;

  /**
   * Called by the host-application during start-up. Implements this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(Properties settings);

  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public Properties storeSettings();

  public boolean hasSettingsPanel();

  public SettingsPanel getSettingsPanel();

  /**
   * Gets the list of the channels that are available by this data service.
   */
  public Channel[] getAvailableChannels();
  public Channel[] checkForAvailableChannels(ProgressMonitor monitor) throws TvBrowserException;
  public boolean supportsDynamicChannelList();
  
  /**
   * Gets information about this TvDataService
   */
  public PluginInfo getInfo();
  
  /**
   * Gets the Version of the implemented API
   * Since TV-Browser 0.9.7 getAPIVersion must return 1.0 
   */
  public devplugin.Version getAPIVersion();
  
  /**
   * Gets if the data service supports auto upate of data.
   * @return <code>True</code> if the data service supports the auto update,
   * <code>false</code> otherwise.
   * @since 2.7
   */
  public boolean supportsAutoUpdate();
 

}