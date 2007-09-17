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

import java.io.File;
import java.util.Properties;

import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;

/**
 * Interface for data services methods.
 * 
 * @deprecated since 2.2.4/2.6 Your TvDataService should not implement this interface.
 * The visibility will be set to protected with TV-Browser 3.0 and a later release of
 * the 2.2.x branch, so your TvDataService will not be able to implement this interface.
 * You have to extend the class devplugin.AbstractTvDataService instead
 * to provide your data service.
 */
public interface TvDataService {

  /**
   * This method is called by the host application to set the working folder.
   * If required, TvDataService implementations should store their data
   * within this 'dataDir' directory
   * @param dataDir
   */
  public void setWorkingDirectory(File dataDir);

  /**
   * @return an array of the available channel groups.

   */
  public ChannelGroup[] getAvailableGroups();

  /**
   * Updates the TV listings provided by this data service.
   *
   * @throws util.exc.TvBrowserException
   */
  public void updateTvData(TvDataUpdateManager updateManager,
                           Channel[] channelArr, Date startDate, int dateCount, ProgressMonitor monitor)
    throws TvBrowserException;

  /**
   * Called by the host-application during start-up. Implement this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(Properties settings);

  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public Properties storeSettings();

  /**
   * A TvDataService can have a settings panel within the settings dialog.
   * If the hasSettingsPanel() method returns false, the getSettingsPanel()
   * method is never called.
   * @return true, if the settings panel feature is used by this TvDataService
   */
  public boolean hasSettingsPanel();

  /**
   *
   * @return the SettingsPanel of this TvDataService
   */
  public SettingsPanel getSettingsPanel();

  /**
   * Gets the list of the channels that are available for the given channel group.
   */
  public Channel[] getAvailableChannels(ChannelGroup group);

  /**
   * Some TvDataServices may need to connect to the internet to know their
   * channels. If supportsDanymicChannelList() returns true, this method is
   * called to check for availabel channels.
   * @param group
   * @param monitor
   * @return
   * @throws TvBrowserException
   */
  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException;

  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor monitor) throws TvBrowserException;

  /**
   *
   * @return true, if this TvDataService can dynamically load other channels
   */
  public boolean supportsDynamicChannelList();

  /**
   *
   * @return true, if this TvDataService can dynamically load other groups
   */
  public boolean supportsDynamicChannelGroups();

  /**
   * Gets information about this TvDataService
   */
  public PluginInfo getInfo();

}
