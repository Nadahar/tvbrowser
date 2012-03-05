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


package tvbrowser.ui.mainframe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginBaseInfo;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.ui.update.DataServiceSoftwareUpdateItem;
import tvbrowser.ui.update.PluginSoftwareUpdateItem;
import tvbrowser.ui.update.PluginsSoftwareUpdateItem;
import tvbrowser.ui.update.SoftwareUpdateItem;
import tvbrowser.ui.update.TvbrowserSoftwareUpdateItem;
import util.io.IOUtilities;
import devplugin.Plugin;
import devplugin.Version;

/**
 * Loads software update information.
 */
public final class SoftwareUpdater {
  public static final int ALL_TYPE = 0;
  public static final int ONLY_UPDATE_TYPE = 1;
  public static final int ONLY_DATA_SERVICE_TYPE = 2;
  public static final int DRAG_AND_DROP_TYPE = 3;
  
	private SoftwareUpdateItem[] mSoftwareUpdateItems;
	private String mBlockRequestingPluginId;
	private boolean mIsRequestingBlockArrayClear;

	 /**
   * Creates an instance of this class.
   *
   * @param url The url to download the informations from.
   * @param baseInfos The base infos for all available plugins.
   * @throws IOException
   */
  public SoftwareUpdater(URL url, PluginBaseInfo[] baseInfos) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(
    	IOUtilities.openSaveGZipInputStream(IOUtilities.getStream(url, 300000)),"ISO-8859-1"));

    mSoftwareUpdateItems=readSoftwareUpdateItems(reader,ONLY_UPDATE_TYPE,false,baseInfos);

    reader.close();
  }
  
  /**
   * Creates an instance of this class.
   *
   * @param url The url to download the informations from.
   * @param dialogType The type of this update dialog.
   * @param baseInfos The base infos for all available plugins.
   * @throws IOException
   */
  public SoftwareUpdater(URL url, int dialogType, PluginBaseInfo[] baseInfos) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(
    	IOUtilities.openSaveGZipInputStream(IOUtilities.getStream(url, 300000)),"ISO-8859-1"));

    mSoftwareUpdateItems=readSoftwareUpdateItems(reader,dialogType,false,baseInfos);
    
    reader.close();
  }
	
	/**
	 * Creates an instance of this class.
	 *
	 * @param url The url to download the informations from.
	 * @param dialogType The type of this update dialog.
	 * @param dragNdrop If the plugin was dropped.
	 * @throws IOException
	 */
	SoftwareUpdater(URL url, int dialogType, boolean dragNdrop) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
			IOUtilities.openSaveGZipInputStream(IOUtilities.getStream(url, 300000)),"ISO-8859-1"));

		mSoftwareUpdateItems=readSoftwareUpdateItems(reader,dialogType,dragNdrop,null);

		reader.close();
	}

  private SoftwareUpdateItem[] readSoftwareUpdateItems(BufferedReader reader, int dialogType, boolean dragNdrop, PluginBaseInfo[] baseInfos) throws IOException {
    Pattern pluginTypePattern = Pattern.compile("\\[(.*):(.*)\\]");
    Pattern keyValuePattern = Pattern.compile("(.+?)=(.*)");
    Matcher matcher;

    ArrayList<SoftwareUpdateItem> updateItems = new ArrayList<SoftwareUpdateItem>();
    //ArrayList<PluginsSoftwareUpdateItem> blockedItems = new ArrayList<PluginsSoftwareUpdateItem>(0);

    try {
      SoftwareUpdateItem curItem=null;
      String line=reader.readLine();

      while (line != null) {
        matcher=pluginTypePattern.matcher(line);
        if (matcher.find()) { // new plugin
          String type=matcher.group(1);
          String className=matcher.group(2);

          if ("plugin".equals(type)) {
            curItem=new PluginSoftwareUpdateItem(className);
          }
          else if ("dataservice".equals(type)) {
            curItem=new DataServiceSoftwareUpdateItem(className);
          }
          else if ("tvbrowser".equals(type)) {
            curItem=new TvbrowserSoftwareUpdateItem(className);
          }

          if (curItem==null) {
            throw new IOException("invalid software update file");
          }

          updateItems.add(curItem);
        }
        else {
          matcher=keyValuePattern.matcher(line);

          if (matcher.find()) { // new plugin
            String value = matcher.group(2);
            value = value.replaceAll("\\\\", ""); // fix wrong HTML encoding in plugin descriptions

            if(curItem != null) {
              curItem.addProperty(matcher.group(1), value);
            }
          }
        }
        line=reader.readLine();
      }

      mIsRequestingBlockArrayClear = true;
      Settings.propBlockedPluginArray.clear(this);
      mIsRequestingBlockArrayClear = false;

      // remove all items we can't use

      Iterator<SoftwareUpdateItem> it = updateItems.iterator();
      while (it.hasNext()) {
        SoftwareUpdateItem item = it.next();
        String className = item.getClassName();

        // remove incompatible items
        Version required = item.getRequiredVersion();
        Version maximum = item.getMaximumVersion();
        if ((required!=null && TVBrowser.VERSION.compareTo(required)<0) ||
            (maximum != null && TVBrowser.VERSION.compareTo(maximum)>0) ||
            !item.getProperty("filename").toLowerCase().endsWith(".jar") ||
            !item.isSupportingCurrentOs()) {

          /* maximum contains the block start version if this is a block plugin entry
           * required cotains the block end version
           */
          if(required!=null && maximum != null && required.compareTo(maximum) > 0) {
            if(item instanceof PluginsSoftwareUpdateItem) {
              PluginsSoftwareUpdateItem blocked = (PluginsSoftwareUpdateItem)item;
              mBlockRequestingPluginId = blocked.getId();
              Settings.propBlockedPluginArray.addBlockedPlugin(this, mBlockRequestingPluginId, blocked.getMaximumVersion(), blocked.getRequiredVersion());
              mBlockRequestingPluginId = null;
            }

            if(item.getVersion().compareTo(required) <= 0 && item.getVersion().compareTo(maximum) >= 0) {
              it.remove();
              continue;
            }
          }
          else {
            it.remove();
            continue;
          }
        }

        // remove already installed plugins
        String pluginId = "java." + className.toLowerCase() + "." + className;
        
        if(baseInfos == null) {
          PluginProxy installedPlugin = PluginProxyManager.getInstance().getPluginForId(pluginId);
  
          if(dialogType == ONLY_UPDATE_TYPE) {
            // remove all not installed plugins
            if (installedPlugin == null) {
              TvDataServiceProxy service = TvDataServiceProxyManager.getInstance().findDataServiceById(className.toLowerCase()+"."+className);
  
              if(service == null) {
                it.remove();
                continue;
              }
            }
          }
          else if(dialogType == ONLY_DATA_SERVICE_TYPE) {
            if(!item.getCategory().equals(Plugin.ADDITONAL_DATA_SERVICE_SOFTWARE_CATEGORY) && !item.getCategory().equals(Plugin.ADDITONAL_DATA_SERVICE_HARDWARE_CATEGORY)) {
              it.remove();
              continue;
            }
          }
  
          if (installedPlugin!=null && ((installedPlugin.getInfo().getVersion().compareTo(item.getVersion())>0 ||
              (installedPlugin.getInfo().getVersion().compareTo(item.getVersion())==0 && (!dragNdrop || item.getVersion().isStable()))))) {
            it.remove();
            continue;
          }
  
          // remove already installed dataservices
          TvDataServiceProxy service= TvDataServiceProxyManager.getInstance().findDataServiceById(className.toLowerCase()+"."+className);
          if (service!=null && ((service.getInfo().getVersion().compareTo(item.getVersion())>0) ||
              (service.getInfo().getVersion().compareTo(item.getVersion())==0 && (!dragNdrop || item.getVersion().isStable())))) {
            it.remove();
            continue;
          }
  
          if(item.isOnlyUpdate() && installedPlugin == null && service == null) {
            it.remove();
          }
          
          PluginProxyManager.getInstance().firePluginBlockListRenewed();
        }
        else {
          PluginBaseInfo baseInfo = getBaseInfoFor(pluginId,baseInfos);
          
          if(baseInfo == null) {
            it.remove();
            continue;
          }
          else if(baseInfo.getVersion().compareTo(item.getVersion()) >= 0) {
            it.remove();
            continue;
          }
        }
      }

      
    } catch (RuntimeException e) {
      e.printStackTrace();
    }

    return updateItems.toArray(new SoftwareUpdateItem[updateItems.size()]);
  }

	/**
	 * Gets all available update items in an array.
	 *
	 * @return All available update items in an array.
	 */
	public SoftwareUpdateItem[] getAvailableSoftwareUpdateItems() {
		return mSoftwareUpdateItems;
	}

	/**
	 * @param pluginId The id that is requested to be blocked.
	 * @return <code>True</code> if this updater is requesting a block.
	 */
	public boolean isRequestingToBlockAPlugin(String pluginId) {
	  return mBlockRequestingPluginId != null && pluginId != null &&
	  mBlockRequestingPluginId.equals(pluginId);
	}

	/**
	 * @return <code>True</code> if this updater is requesting
	 * to clear the block array.
	 */
	public boolean isRequestingBlockArrayClear() {
	  return mIsRequestingBlockArrayClear;
	}
	
	private PluginBaseInfo getBaseInfoFor(String className, PluginBaseInfo[] availablePlugins) {
	  for(PluginBaseInfo baseInfo : availablePlugins) {	    
	    if(baseInfo.getPluginId().equals(className)) {
	      return baseInfo;
	    }
	  }
	  
	  return null;
	}	
}