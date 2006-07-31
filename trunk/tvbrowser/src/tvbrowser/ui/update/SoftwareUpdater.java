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
import devplugin.Version;

public class SoftwareUpdater {
	
 
	private SoftwareUpdateItem[] mSoftwareUpdateItems;
	
	public SoftwareUpdater(URL url) throws IOException {
		URLConnection con=url.openConnection();
		
		InputStream in=con.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		mSoftwareUpdateItems=readSoftwareUpdateItems(reader);
		
		reader.close();
	}
	
  private SoftwareUpdateItem[] readSoftwareUpdateItems(BufferedReader reader) throws IOException {
    Pattern pluginTypePattern = Pattern.compile("\\[(.*):(.*)\\]");
    Pattern keyValuePattern = Pattern.compile("(.+?)=(.*)");
    Matcher matcher;
    
    ArrayList updateItems = new ArrayList();
    
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
        
        if (matcher.find()) // new plugin 
          curItem.addProperty(matcher.group(1), matcher.group(2));
      }
      line=reader.readLine();
    }
    
    // remove all items we can't use
    
    Iterator it = updateItems.iterator();
    while (it.hasNext()) {
      SoftwareUpdateItem item = (SoftwareUpdateItem) it.next();
      String className = item.getClassName();
      
      // remove incompatible items
      Version required = item.getRequiredVersion();
      Version maximum = item.getMaximumVersion();
      if ((required!=null && TVBrowser.VERSION.compareTo(required)<0) ||
          (maximum != null && TVBrowser.VERSION.compareTo(maximum)>0)) {
        it.remove();
        continue;
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
      
      if(item.isOnlyUpdate() && installedPlugin == null && service == null)
        it.remove();
    }
    
    Object[]objs=updateItems.toArray();
    SoftwareUpdateItem[] ui=new SoftwareUpdateItem[objs.length];
    for (int i=0;i<ui.length;i++) {
      ui[i]=(SoftwareUpdateItem)objs[i];
    }
    return ui;
    
  }
	
	
	
	public SoftwareUpdateItem[] getAvailableSoftwareUpdateItems() {
		
		return mSoftwareUpdateItems;
	}
	
	
}