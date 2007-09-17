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

import util.exc.TvBrowserException;

/**
 * Superclass for all TvDataServices.
 * <p>
 * Extend this class to provide your own TvDataService.
 */
public abstract class AbstractTvDataService implements devplugin.TvDataService, tvdataservice.TvDataService {

  /**
   * The plugin manager. It's the connection to TV-Browser.
   * <p>
   * Every communication between TV-Browser and the data server is either initiated
   * by TV-Browser or made by using the plugin manager.
   */
  private static PluginManager mPluginManager;

  final public Channel[] getAvailableChannels() {
    return getAvailableChannels(null);
  }

  final public Channel[] checkForAvailableChannels(ProgressMonitor monitor) throws TvBrowserException {
    return checkForAvailableChannels(null, monitor);
  }

  final public Version getAPIVersion() {
    return new Version(1,0);
  }
  
  /**
   * Use this method to call methods of the plugin manager.
   * <p>
   * The plugin manager is your connection to TV-Browser. Every communication
   * between TV-Browser and the data service is either initiated by TV-Browser or made
   * by using the plugin manager.
   * 
   * @return The plugin manager.
   * @since 2.6
   */
  final public static PluginManager getPluginManager() {
    return mPluginManager;
  }

  /**
   * Called by the host-application to provide access to the plugin manager.
   * 
   * @param manager The plugin manager the plugins should use.
   */
  final public static void setPluginManager(PluginManager manager) {
    if (mPluginManager == null ) {
      mPluginManager = manager;
    }
  }
  
  /**
   * Gets the version of this data service.
   * 
   * @return The version of this data service.
   */
  public static Version getVersion() {
    return new Version(0,0);
  }
}
