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


package tvbrowser.ui.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tvbrowser.TVBrowser;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import util.io.IOUtilities;
import devplugin.Version;

/**
 * Loads software update information.
 */
public class SoftwareUpdater {
	private SoftwareUpdateItem[] mSoftwareUpdateItems;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param url The url to download the informations from.
	 * @param onlyUpdates If only updates and not new items should be accepted.
	 * @throws IOException
	 */
	public SoftwareUpdater(URL url, boolean onlyUpdates) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(IOUtilities.getStream(url, 300000),"ISO-8859-1"));
		
		mSoftwareUpdateItems=readSoftwareUpdateItems(reader,onlyUpdates);
		
		reader.close();
	}
	
  private SoftwareUpdateItem[] readSoftwareUpdateItems(BufferedReader reader, boolean onlyUpdates) throws IOException {
    Pattern pluginTypePattern = Pattern.compile("\\[(.*):(.*)\\]");
    Pattern keyValuePattern = Pattern.compile("(.+?)=(.*)");
    Matcher matcher;
    
    ArrayList<SoftwareUpdateItem> updateItems = new ArrayList<SoftwareUpdateItem>();
    
    SoftwareUpdateItem curItem=null;
    String line=reader.readLine();
    
    while (line != null) {
      matcher=pluginTypePattern.matcher(line);
      if (matcher.find()) { // new plugin 
        String type=matcher.group(1);
        String className=matcher.group(2);
        
        if ("plugin".equals(type) || "dataservice".equals(type))
          curItem=new PluginSoftwareUpdateItem(className); 
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
          value = value.replaceAll("\\\\&", "&"); // fix wrong HTML encoding in plugin descriptions
          
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
        it.remove();
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
      if (installedPlugin!=null && installedPlugin.getInfo().getVersion().compareTo(item.getVersion())>=0) {
        it.remove();
        continue;
      }
      
      // remove already installed dataservices
      TvDataServiceProxy service= TvDataServiceProxyManager.getInstance().findDataServiceById(className.toLowerCase()+"."+className);
      if (service!=null && service.getInfo().getVersion().compareTo(item.getVersion())>=0) {
        it.remove();
        continue;
      }
      
      if(item.isOnlyUpdate() && installedPlugin == null && service == null) {
        it.remove();
      }
    }
    
    Object[]objs=updateItems.toArray();
    SoftwareUpdateItem[] ui=new SoftwareUpdateItem[objs.length];
    for (int i=0;i<ui.length;i++) {
      ui[i]=(SoftwareUpdateItem)objs[i];
    }
    return ui;
    
  }	
	
	/**
	 * Gets all available update items in an array.
	 * 
	 * @return All available update items in an array.
	 */
	public SoftwareUpdateItem[] getAvailableSoftwareUpdateItems() {		
		return mSoftwareUpdateItems;
	}
}