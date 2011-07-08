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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.update;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JLabel;

import tvbrowser.core.PluginLoader;
import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.SoftwareUpdater;
import util.io.IOUtilities;
import util.io.Mirror;
import util.ui.Localizer;

/**
 * A class that searchs for updates of the installed plugins.
 * 
 * @author René Mach
 */
public class PluginAutoUpdater {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PluginAutoUpdater.class);
  /** The name of the updates file. */
  public static final String PLUGIN_UPDATES_FILENAME = "plugins.txt";
  /** The default plugins download url */
  public static final String DEFAULT_PLUGINS_DOWNLOAD_URL = "http://www.tvbrowser.org/plugins";
  
  /** Contains the mirror urls useable for receiving the groups.txt from. */
  private static final String[] DEFAULT_PLUGINS_UPDATE_MIRRORS = {
    "http://tvbrowser.dyndns.tv",
    "http://daten.wannawork.de",
    "http://www.gfx-software.de/tvbrowserorg",
    "http://tvbrowser1.sam-schwedler.de",
    "http://tvbrowser.nicht-langweilig.de/data"
    /*"http://tvbrowser.dyndns.tv",
    "http://hdtv-online.org/TVB",
    "http://www.tvbrowserserver.de/"*/
  };
  
  /**
   * Gets the the update items for plugins on TV-Browser version change.
   * @return The update items.
   * @throws IOException
   */
  public static SoftwareUpdateItem[] getUpdateItemsForVersionChange() throws IOException {
    String baseUrl = getPluginUpdatesMirror().getUrl();
    
    try {
      String name = PLUGIN_UPDATES_FILENAME.substring(0,
          PLUGIN_UPDATES_FILENAME.indexOf('.'))
          + "_" + Mirror.MIRROR_LIST_FILE_NAME;
      IOUtilities.download(new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + name), new File(Settings.getUserSettingsDirName(), name));
    } catch(Exception ee) {}
    
    java.net.URL url = new java.net.URL(baseUrl + "/" + PluginAutoUpdater.PLUGIN_UPDATES_FILENAME);
    SoftwareUpdater softwareUpdater = new SoftwareUpdater(url,PluginLoader.getInstance().getInfoOfAvailablePlugins());
    
    return softwareUpdater.getAvailableSoftwareUpdateItems();
  }
  
  /**
   * Gets the the data services on first TV-Browser start.
   * @return The data services.
   * @throws IOException
   */
  public static SoftwareUpdateItem[] getDataServicesForFirstStartup() throws IOException {
    String baseUrl = getPluginUpdatesMirror().getUrl();
    
    try {
      String name = PLUGIN_UPDATES_FILENAME.substring(0,
          PLUGIN_UPDATES_FILENAME.indexOf('.'))
          + "_" + Mirror.MIRROR_LIST_FILE_NAME;
      IOUtilities.download(new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + name), new File(Settings.getUserSettingsDirName(), name));
    } catch(Exception ee) {}
    
    java.net.URL url = new java.net.URL(baseUrl + "/" + PluginAutoUpdater.PLUGIN_UPDATES_FILENAME);
    SoftwareUpdater softwareUpdater = new SoftwareUpdater(url,SoftwareUpdater.ONLY_DATA_SERVICE_TYPE,null);
    
    return softwareUpdater.getAvailableSoftwareUpdateItems();
  }
  
  /**
   * Search for plugin updates. (And only updates)
   * @param infoLabel The label to show the info in.
   */
  public static void searchForPluginUpdates(final JLabel infoLabel) {
    new Thread("Plugins update thread") {
      public void run() {
        infoLabel.setText(mLocalizer.msg("searchForServer","Search for plugin update server..."));
        String url = getPluginUpdatesMirror().getUrl();
        
        try {
          String name = PLUGIN_UPDATES_FILENAME.substring(0,
              PLUGIN_UPDATES_FILENAME.indexOf('.'))
              + "_" + Mirror.MIRROR_LIST_FILE_NAME;
          IOUtilities.download(new URL(url + (url.endsWith("/") ? "" : "/") + name), new File(Settings.getUserSettingsDirName(), name));
        } catch(Exception ee) {}
        
        MainFrame.getInstance().updatePlugins(url, SoftwareUpdater.ONLY_UPDATE_TYPE, infoLabel, Settings.propAutoUpdatePlugins.getBoolean());
      }
    }.start();
  }


  private static Mirror getPluginUpdatesMirror() {
    File file = new File(new File(Settings.getUserSettingsDirName()),
        PLUGIN_UPDATES_FILENAME.substring(0, PLUGIN_UPDATES_FILENAME
            .indexOf('.'))
            + "_" + Mirror.MIRROR_LIST_FILE_NAME);
    
    try {
      return Mirror.chooseUpToDateMirror(Mirror.readMirrorListFromFile(file),null,PLUGIN_UPDATES_FILENAME, "plugins", PluginAutoUpdater.class, mLocalizer.msg("error.additional"," Please inform the TV-Browser team."));
    } catch (Exception exc) {
      try {
        if(DEFAULT_PLUGINS_UPDATE_MIRRORS.length > 0) {
          Mirror[] mirr = new Mirror[DEFAULT_PLUGINS_UPDATE_MIRRORS.length];
          
          for(int i = 0; i < DEFAULT_PLUGINS_UPDATE_MIRRORS.length; i++) {
            mirr[i] = new Mirror(DEFAULT_PLUGINS_UPDATE_MIRRORS[i]);
          }
          
          return Mirror.chooseUpToDateMirror(mirr,null,PLUGIN_UPDATES_FILENAME, "plugins", PluginAutoUpdater.class, mLocalizer.msg("error.additional"," Please inform the TV-Browser team."));
        } else {
          throw exc;
        }
      }catch (Exception exc2) {
        return new Mirror(DEFAULT_PLUGINS_DOWNLOAD_URL);
      }
    }
  }
}
