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
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;


public class DefaultTvDataServiceProxy extends AbstractTvDataServiceProxy {

  public devplugin.AbstractTvDataService mTvDataService;

  public DefaultTvDataServiceProxy(devplugin.AbstractTvDataService service) {
    mTvDataService = service;
  }


  public void setWorkingDirectory(File dataDir) {
    mTvDataService.setWorkingDirectory(dataDir);
  }

  public ChannelGroup[] getAvailableGroups() {
    return mTvDataService.getAvailableGroups();
  }

  public void updateTvData(TvDataUpdateManager updateManager, Channel[] channelArr, Date startDate, int dateCount, ProgressMonitor monitor) throws TvBrowserException {
    mTvDataService.updateTvData(updateManager,  channelArr, startDate, dateCount, monitor);
  }

  public void loadSettings(Properties settings) {
    mTvDataService.loadSettings(settings);
  }

  public Properties storeSettings() {
    return mTvDataService.storeSettings();
  }

  public boolean hasSettingsPanel() {
    return mTvDataService.hasSettingsPanel();
  }

  public SettingsPanel getSettingsPanel() {
    return mTvDataService.getSettingsPanel();
  }

  public Channel[] getAvailableChannels(ChannelGroup group) {
    return mTvDataService.getAvailableChannels(group);
  }

  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
    return mTvDataService.checkForAvailableChannels(group, monitor);
  }

  public ChannelGroup[] checkForAvailableGroups(ProgressMonitor monitor) throws TvBrowserException {
    return mTvDataService.checkForAvailableChannelGroups(monitor);
  }

  public boolean supportsDynamicChannelList() {
    return mTvDataService.supportsDynamicChannelList();
  }

  public boolean supportsDynamicChannelGroups() {
    return mTvDataService.supportsDynamicChannelGroups();
  }

  public PluginInfo getInfo() {
    return mTvDataService.getInfo();
  }

  public String getId() {
    return mTvDataService.getClass().getName();
  }


  /**
   * Called by the host-application to provide the parent frame.
   *
   * @param parent The parent frame.
   * @since 2.7
   */
  final public void setParent(Frame parent) {
    mTvDataService.setParent(parent);
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
    return mTvDataService.getParentFrame();
  }

  /**
   * This method is called when the TV-Browser start is complete.
   * @since 2.7
   */
  public void handleTvBrowserStartFinished() {
    mTvDataService.handleTvBrowserStartFinished();
  }
}
