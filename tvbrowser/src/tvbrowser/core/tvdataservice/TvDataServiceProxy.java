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

package tvbrowser.core.tvdataservice;

import java.awt.Frame;
import java.io.File;
import java.util.Properties;

import tvbrowser.core.plugin.ButtonActionIf;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ContextMenuIf;
import devplugin.Date;
import devplugin.InfoIf;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;


public interface TvDataServiceProxy extends ContextMenuIf, ButtonActionIf, InfoIf {


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
  public Channel[] getAvailableChannels();

  /**
   * Some TvDataServices may need to connect to the Internet to know their
   * channels. If {@link #supportsDynamicChannelList()} returns true, this method is
   * called to check for available channels.
   * @param monitor
   * @return the list of available channels
   * @throws TvBrowserException
   */
  public Channel[] checkForAvailableChannels(ProgressMonitor monitor) throws TvBrowserException;

  public ChannelGroup[] checkForAvailableGroups(ProgressMonitor monitor) throws TvBrowserException;

  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException;

  public Channel[] getAvailableChannels(ChannelGroup group);

  /**
   * @return The channels that have to be loaded on TVB start.
   * @since 2.3
   */
  public Channel[] getChannelsForTvBrowserStart();

  /**
   *
   * @return true, if this TvDataService can dynamically load other channels
   */
  public boolean supportsDynamicChannelList();

  public boolean supportsDynamicChannelGroups();

  /**
   * Gets information about this TvDataService
   */
  public PluginInfo getInfo();

  /**
   *
   * @return a unique id to identify this TvDataService
   */
  public String getId();

  /**
   * Called by the host-application to provide the parent frame.
   *
   * @param parent The parent frame.
   * @since 2.7
   */
  public void setParent(Frame parent);


  /**
   * Gets the parent frame.
   * <p>
   * The parent frame may be used for showing dialogs.
   *
   * @return The parent frame.
   * @since 2.7
   */
  public Frame getParentFrame();

  /**
   * This method is called when the TV-Browser start is complete.
   * @since 2.7
   */
  public void handleTvBrowserStartFinished();

  /**
   * Gets if the data service supports auto upate of data.
   * @return <code>True</code> if the data service supports the auto update,
   * <code>false</code> otherwise.
   * @since 2.7
   */
  public boolean supportsAutoUpdate();


  /**
   * comparator for data service proxies (sorting alphabetically by name)
   * @since 2.7
   * @author bananeweizen
   */
  public static class Comparator implements java.util.Comparator<TvDataServiceProxy> {

    public int compare(TvDataServiceProxy proxy1, TvDataServiceProxy proxy2) {
      return proxy1.getInfo().getName().compareTo(proxy2.getInfo().getName());
    }
  }


  /**
   * @return package name of data service
   * @since 3.0
   */
  public String getDataServicePackageName();
}
