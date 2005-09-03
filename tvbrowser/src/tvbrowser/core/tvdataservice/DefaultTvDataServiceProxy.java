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

package tvbrowser.core.tvdataservice;

import devplugin.*;

import java.io.File;
import java.util.Properties;

import tvdataservice.TvDataUpdateManager;
import tvdataservice.SettingsPanel;

import util.exc.TvBrowserException;


public class DefaultTvDataServiceProxy implements TvDataServiceProxy {

  public TvDataService mTvDataService;

  public DefaultTvDataServiceProxy(TvDataService service) {
    mTvDataService = service;
  }

  public void setWorkingDirectory(File dataDir) {
    mTvDataService.setWorkingDirectory(dataDir);
  }

  public ChannelGroup[] getAvailableGroups() {
    return mTvDataService.getAvailableGroups();
  }

  public void updateTvData(ChannelGroup group, TvDataUpdateManager updateManager, Channel[] channelArr, Date startDate, int dateCount, ProgressMonitor monitor) throws TvBrowserException {
    mTvDataService.updateTvData(group, updateManager,  channelArr, startDate, dateCount, monitor);
  }

  public void loadSettings(Properties settings) {
    mTvDataService.loadSettings(settings);
  }

  public Properties storeSettings() {
    return null;
  }

  public boolean hasSettingsPanel() {
    return false;
  }

  public SettingsPanel getSettingsPanel() {
    return null;
  }

  public Channel[] getAvailableChannels(ChannelGroup group) {
    return new Channel[0];
  }

  public Channel[] checkForAvailableChannels(ChannelGroup group, ProgressMonitor monitor) throws TvBrowserException {
    return new Channel[0];
  }

  public boolean supportsDynamicChannelList() {
    return false;
  }

  public PluginInfo getInfo() {
    return null;
  }

  public String getId() {
    return null;
  }
}
