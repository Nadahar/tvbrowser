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
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tvbrowser.TVBrowser;
import tvbrowser.core.TvDataServiceManager;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvdataservice.TvDataService;
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
		
		Pattern[] regexPatternArr = new Pattern[] {
				// Example: "[MyPlugin:plugin]"	
				Pattern.compile("\\[(.*):(.*)\\]"),
				// Example: "key=value"
				Pattern.compile("(.*)=(.*)"),
				// Example: "version=0.1"
				Pattern.compile("(.*)=(\\d*).(\\d*)")		
		};
		
    String versionName=null;
    int major=-1, minor=-1;
    boolean isStable=false;
		String line=reader.readLine();
		HashSet updateItems=new HashSet();
		Matcher matcher;
		AbstractSoftwareUpdateItem curItem=null;
		while (line!=null) {
			matcher=regexPatternArr[0].matcher(line);
			if (matcher.find()) { // new plugin 
				String typeStr=matcher.group(1);
				String name=matcher.group(2);
        
        if (curItem!=null && major>=0) {
          curItem.setVersion(new Version(major,minor,isStable,versionName));
        }
        
				if ("plugin".equals(typeStr)) {
					curItem=new PluginSoftwareUpdateItem(name);
				}
				else if ("dataservice".equals(typeStr)) {
					curItem=new DataserviceSoftwareUpdateItem(name);
				}
				else if ("tvbrowser".equals(typeStr)) {
					curItem=new TvbrowserSoftwareUpdateItem(name);
				}
				if (curItem==null) {
				  throw new IOException("invalid software update file");	
				}
				
        updateItems.add(curItem);
									
			}else {
				matcher=regexPatternArr[1].matcher(line);  // key=value
				if (matcher.find()) {
					String key=matcher.group(1);
					String value=matcher.group(2);
					if ("version".equals(key)) {
						matcher=regexPatternArr[2].matcher(line);
						if (matcher.find()) {
							major=Integer.parseInt(matcher.group(2));
							minor=Integer.parseInt(matcher.group(3));
							//curItem.setVersion(new Version(major,minor,false));
   					}
					}else if ("requires".equals(key)) {
						matcher=regexPatternArr[2].matcher(line);
						if (matcher.find()) {
							int maj=Integer.parseInt(matcher.group(2));
							int min=Integer.parseInt(matcher.group(3));
							curItem.setRequiredVersion(new Version(maj,min,false));
													
						}
          }else if ("version.name".equals(key)) {
            versionName=value;
					}else if ("download".equals(key)) {
						curItem.setUrl(value);
					}else if ("description".equals(key)) {
						curItem.setDescription(value);
					}else if ("stable".equals(key)) {
            isStable="true".equals(value);
					}
					
				}
				
			} // end key-value
			line=reader.readLine();
		} // end loop
    
    if (curItem!=null && major>=0) {
      curItem.setVersion(new Version(major,minor,isStable,versionName));
    }
		
		
		Iterator it=updateItems.iterator();
		while (it.hasNext()) {
			SoftwareUpdateItem ui=(SoftwareUpdateItem)it.next();
      
      // remove incompatible items
			Version required=ui.getRequiredVerion();
			if (required!=null && TVBrowser.VERSION.compareTo(required)<0) {
        it.remove();	
			}      			
		}
    
    // remove already installed plugins
    it=updateItems.iterator();
    while (it.hasNext()) {
      SoftwareUpdateItem ui=(SoftwareUpdateItem)it.next();
      
      String name = ui.getName();
      String pluginId = "java." + name.toLowerCase() + "." + name;
      
      PluginProxy installedPlugin = PluginProxyManager.getInstance().getPluginForId(pluginId);
      
      if (installedPlugin!=null && installedPlugin.getInfo().getVersion().compareTo(ui.getVersion())>=0) {
        it.remove();
      }     
    }
    
    //  remove already installed dataservices
     it=updateItems.iterator();
     while (it.hasNext()) {
       SoftwareUpdateItem ui=(SoftwareUpdateItem)it.next();
       String name=ui.getName();
       TvDataService service=TvDataServiceManager.getInstance().getDataService(name.toLowerCase()+"."+name);
              
       if (service!=null && service.getInfo().getVersion().compareTo(ui.getVersion())>=0) {
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
	
		
	
	
	
	
	public SoftwareUpdateItem[] getAvailableSoftwareUpdateItems() {
		
		return mSoftwareUpdateItems;
	}
	
	
}