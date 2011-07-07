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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.Logger;

import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgressMonitor;

public class DefaultTvDataServiceProxy extends AbstractTvDataServiceProxy {

  /** The logger for this class */
  private static final Logger mLog = Logger
      .getLogger(DefaultTvDataServiceProxy.class.getName());

  private devplugin.AbstractTvDataService mTvDataService;

  public DefaultTvDataServiceProxy(devplugin.AbstractTvDataService service) {
    mTvDataService = service;
  }

  private void logError(Throwable e, String action) {
    StringBuilder errorMsg = new StringBuilder("The TV data service '");
    errorMsg.append(mTvDataService.getInfo().getName());
    errorMsg.append("' has caused an error during ");
    errorMsg.append(action).append(": ");
    errorMsg.append(e.getLocalizedMessage()).append("\n");

    // print stack trace
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter, true);
    e.printStackTrace(printWriter);
    printWriter.flush();
    stringWriter.flush();
    errorMsg.append(stringWriter.toString());

    mLog.severe(errorMsg.toString());
  }

  public void setWorkingDirectory(File dataDir) {
    try {
      mTvDataService.setWorkingDirectory(dataDir);
    } catch (Throwable e) {
      logError(e, "set working directory");
    }
  }

  public ChannelGroup[] getAvailableGroups() {
    try {
      return mTvDataService.getAvailableGroups();
    } catch (Throwable t) {
      logError(t, "get available groups");
    }
    return null;
  }

  public void updateTvData(TvDataUpdateManager updateManager,
      Channel[] channelArr, Date startDate, int dateCount,
      ProgressMonitor monitor) throws TvBrowserException {
    try {
      mTvDataService.updateTvData(updateManager, channelArr, startDate,
          dateCount, monitor);
    } catch (Throwable t) {
      logError(t, "update TV data");
    }
  }

  public void loadSettings(Properties settings) {
    try {
      mTvDataService.loadSettings(settings);
    } catch (Throwable t) {
      logError(t, "load settings");
    }
  }

  public Properties storeSettings() {
    try {
      return mTvDataService.storeSettings();
    } catch (Throwable e) {
      logError(e, "store settings");
      return null;
    }
  }

  public boolean hasSettingsPanel() {
    try {
      return mTvDataService.hasSettingsPanel();
    } catch (Throwable t) {
      logError(t, "has settings panel");
    }
    return false;
  }

  public SettingsPanel getSettingsPanel() {
    try {
      return mTvDataService.getSettingsPanel();
    } catch (Throwable t) {
      logError(t, "get settings panel");
    }
    return null;
  }

  public Channel[] getAvailableChannels(ChannelGroup group) {
    try {
      return mTvDataService.getAvailableChannels(group);
    } catch (Throwable t) {
      logError(t, "get available channels");
    }
    return null;
  }

  public Channel[] checkForAvailableChannels(ChannelGroup group,
      ProgressMonitor monitor) throws TvBrowserException {
    try {
      return mTvDataService.checkForAvailableChannels(group, monitor);
    } catch (Throwable t) {
      logError(t, "check available channels");
    }
    return null;
  }

  public ChannelGroup[] checkForAvailableGroups(ProgressMonitor monitor)
      throws TvBrowserException {
    try {
      return mTvDataService.checkForAvailableChannelGroups(monitor);
    } catch (Throwable t) {
      logError(t, "check available groups");
    }
    return null;
  }

  public boolean supportsDynamicChannelList() {
    try {
      return mTvDataService.supportsDynamicChannelList();
    } catch (Throwable t) {
      logError(t, "supports dynamic channels");
    }
    return false;
  }

  public boolean supportsDynamicChannelGroups() {
    try {
      return mTvDataService.supportsDynamicChannelGroups();
    } catch (Throwable t) {
      logError(t, "supports dynamic groups");
    }
    return false;
  }

  public PluginInfo getInfo() {
    try {
      return mTvDataService.getInfo();
    } catch (Throwable t) {
      logError(t, "get plugin info");
    }
    return null;
  }

  public String getId() {
    return mTvDataService.getClass().getName();
  }

  /**
   * Called by the host-application to provide the parent frame.
   *
   * @param parent
   *          The parent frame.
   * @since 2.7
   */
  final public void setParent(Frame parent) {
    try {
      mTvDataService.setParent(parent);
    } catch (Throwable t) {
      logError(t, "set parent frame");
    }
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
    try {
      return mTvDataService.getParentFrame();
    } catch (Throwable t) {
      logError(t, "get parent frame");
    }
    return null;
  }

  /**
   * This method is called when the TV-Browser start is complete.
   *
   * @since 2.7
   */
  public void handleTvBrowserStartFinished() {
    try {
      mTvDataService.handleTvBrowserStartFinished();
    } catch (Throwable t) {
      logError(t, "handle start finished");
    }
  }

  /**
   * Gets if the data service supports auto upate of data.
   *
   * @return <code>True</code> if the data service supports the auto update,
   *         <code>false</code> otherwise.
   * @since 2.7
   */
  public boolean supportsAutoUpdate() {
    try {
      return mTvDataService.supportsAutoUpdate();
    } catch (Throwable t) {
      logError(t, "supports auto update");
    }
    return false;
  }

  /**
   * Gets the action menu with the action supported for toolbar actions.
   * @return The action menu with the supported toolbar actions
   */
  public ActionMenu getButtonAction() {
    return mTvDataService.getButtonAction();
  }

  /**
   * Gets the description for this ButtonActionIf.
   * @return The description for this ButtonActionIf.
   */
  public String getButtonActionDescription() {
    return mTvDataService.getButtonActionDescription();
  }


  /**
   * Gets the actions for the context menu of a program.
   *
   * @param program The program the context menu will be shown for.
   * @return the actions this plugin provides for the given program or
   *         <code>null</code> if the plugin does not provide this feature.
   */
  public ActionMenu getContextMenuActions(Program program) {
    return mTvDataService.getContextMenuActions(program);
  }

  @Override
  public String getDataServicePackageName() {
    return mTvDataService.getClass().getPackage().getName();
  }

  @Override
  public String getPluginCategory() {
    return mTvDataService.getPluginCategory();
  }

  @Override
  public SettingsPanel getAuthenticationPanel() {
    return mTvDataService.getAuthenticationPanel();
  }
  
  
}