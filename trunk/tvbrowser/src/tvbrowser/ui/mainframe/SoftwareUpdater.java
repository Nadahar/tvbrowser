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
import devplugin.Version;

/**
 * Loads software update information.
 */
public final class SoftwareUpdater {
	private SoftwareUpdateItem[] mSoftwareUpdateItems;
	private String mBlockRequestingPluginId;
	private boolean mIsRequestingBlockArrayClear;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param url The url to download the informations from.
	 * @param onlyUpdates If only updates and not new items should be accepted.
	 * @param dragNdrop If the plugin was dropped.
	 * @throws IOException
	 */
	protected SoftwareUpdater(URL url, boolean onlyUpdates, boolean dragNdrop) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(IOUtilities.getStream(url, 300000),"ISO-8859-1"));
		
		mSoftwareUpdateItems=readSoftwareUpdateItems(reader,onlyUpdates, dragNdrop);
		
		reader.close();
	}
	
  private SoftwareUpdateItem[] readSoftwareUpdateItems(BufferedReader reader, boolean onlyUpdates, boolean dragNdrop) throws IOException {
    Pattern pluginTypePattern = Pattern.compile("\\[(.*):(.*)\\]");
    Pattern keyValuePattern = Pattern.compile("(.+?)=(.*)");
    Matcher matcher;
    
    ArrayList<SoftwareUpdateItem> updateItems = new ArrayList<SoftwareUpdateItem>();
    ArrayList<PluginsSoftwareUpdateItem> blockedItems = new ArrayList<PluginsSoftwareUpdateItem>(0);
    
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
          else if ("tvbrowser".equals(type))
            curItem=new TvbrowserSoftwareUpdateItem(className);
          
          if (curItem==null)
            throw new IOException("invalid software update file");    
                  
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
      
      // remove all items we can't use
      
      Iterator<SoftwareUpdateItem> it = updateItems.iterator();
      while (it.hasNext()) {
        SoftwareUpdateItem item = (SoftwareUpdateItem) it.next();
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
            if(item.getVersion().compareTo(required) <= 0 && item.getVersion().compareTo(maximum) >= 0) {
              it.remove();
            }
            
            if(item instanceof PluginsSoftwareUpdateItem) {
              blockedItems.add((PluginsSoftwareUpdateItem)item);
            }
          }
          else {
            it.remove();
          }
          
          continue;
        }
        
        if(onlyUpdates) {
          // remove all not installed plugins
          String pluginId = "java." + className.toLowerCase() + "." + className;      
          PluginProxy installedPlugin = PluginProxyManager.getInstance().getPluginForId(pluginId);        
          
          if (installedPlugin == null) {
            TvDataServiceProxy service = TvDataServiceProxyManager.getInstance().findDataServiceById(className.toLowerCase()+"."+className);
            
            if(service == null) {
              it.remove();
              continue;
            }
          }
        }
        
        // remove already installed plugins
        String pluginId = "java." + className.toLowerCase() + "." + className;      
        PluginProxy installedPlugin = PluginProxyManager.getInstance().getPluginForId(pluginId);
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
      }
      
      mIsRequestingBlockArrayClear = true;
      Settings.propBlockedPluginArray.clear(this);
      mIsRequestingBlockArrayClear = false;
      
      for(PluginsSoftwareUpdateItem blocked : blockedItems) {
        mBlockRequestingPluginId = blocked.getId();
        
        Settings.propBlockedPluginArray.addBlockedPlugin(this, mBlockRequestingPluginId, blocked.getMaximumVersion(), blocked.getRequiredVersion());
      }
      
      mBlockRequestingPluginId = null;
      
      PluginProxyManager.getInstance().firePluginBlockListRenewed();
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
}