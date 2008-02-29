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

import java.io.File;
import java.util.Properties;
import java.awt.*;

import tvdataservice.SettingsPanel;
import tvdataservice.TvDataService;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ChannelGroupImpl;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;


/**
 * This is a wrapper class for the old TvDataService interface.
 */
public class DeprecatedTvDataServiceProxy extends AbstractTvDataServiceProxy {

  private TvDataService mDataService;
  private String mId;
  private Frame mParentFrame;

  public DeprecatedTvDataServiceProxy(TvDataService dataService) {
    mDataService = dataService;
    mId = dataService.getClass().getName();
  }

  public void setWorkingDirectory(File dataDir) {
    mDataService.setWorkingDirectory(dataDir);
  }

  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[]{
        new ChannelGroupImpl("ID_"+mDataService.getInfo().getName(),mDataService.getInfo().getName(),mDataService.getInfo().getDescription(),mDataService.getInfo().getAuthor())
    };
  }

  /**
   * Updates the TV listings provided by this data service.
   *
   * @throws util.exc.TvBrowserException
   */
  public void updateTvData(TvDataUpdateManager updateManager,
                           Channel[] channelArr, Date startDate, int dateCount, ProgressMonitor monitor)
    throws TvBrowserException {

    mDataService.updateTvData(updateManager, channelArr, startDate, dateCount, monitor);
  }

  /**
   * Called by the host-application during start-up. Implements this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(Properties settings) {
    mDataService.loadSettings(settings);
  }

  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public Properties storeSettings() {
    return mDataService.storeSettings();
  }

  public boolean hasSettingsPanel() {
    return mDataService.hasSettingsPanel();
  }

  public SettingsPanel getSettingsPanel() {
    return mDataService.getSettingsPanel();
  }

  /**
   * Gets the list of the channels that are available by this data service.
   */
  public Channel[] getAvailableChannels(ChannelGroup group) {
    return mDataService.getAvailableChannels();
  }

  /**
   * method not supported
   * @param monitor
   * @return
   * @throws TvBrowserException
   */
  public ChannelGroup[] checkForAvailableGroups(ProgressMonitor monitor) throws TvBrowserException {
    return null;
  }

  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
    return mDataService.checkForAvailableChannels(monitor);
  }

  public boolean supportsDynamicChannelList() {
    return mDataService.supportsDynamicChannelList();
  }

  public boolean supportsDynamicChannelGroups() {
    return false;
  }

  /**
   * Gets information about this TvDataService
   */
  public PluginInfo getInfo() {
    return mDataService.getInfo();
  }


  public String getId() {
    return mId;
  }
  /**
   * Called by the host-application to provide the parent frame.
   *
   * @param parent The parent frame.
   * @since 2.7
   */
  final public void setParent(Frame parent) {
    mParentFrame = parent;
  }


  /**
   * Gets the parent frame.
   * <p>
   * The parent frame may be used for showing dialogs.
   *
   * @return The parent frame.
   * @since 2.7
   */
  final public Frame getParentFrame() {
    return mParentFrame;
  }

  /**
   * This method is called when the TV-Browser start is complete.
   * @since 2.7
   */
  public void handleTvBrowserStartFinished() {

  }
  
  /**
   * Gets if the data service supports auto upate of data.
   * @return <code>True</code> if the data service supports the auto update,
   * <code>false</code> otherwise.
   * @since 2.7
   */
  public boolean supportsAutoUpdate() {
    return mDataService.supportsAutoUpdate();
  }

}
