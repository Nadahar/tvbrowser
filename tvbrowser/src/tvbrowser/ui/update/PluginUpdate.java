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

import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.util.regex.*;

import tvbrowser.TVBrowser;
import util.exc.TvBrowserException;
import util.ui.UiUtilities;


public class PluginUpdate {
	
	/** The localizer for this class. */
		  private static final util.ui.Localizer mLocalizer
			= util.ui.Localizer.getLocalizerFor(PluginUpdate.class);
	
	
	public static void updatePlugins(JFrame mainFrame) {
		
		Object[] options = {mLocalizer.msg("checknow","Check now"),
							mLocalizer.msg("cancel","Cancel")};
		String msg=mLocalizer.msg("question.1","do you want to check for new plugins");			
			int n = JOptionPane.showOptionDialog(mainFrame,msg,mLocalizer.msg("title.1","update plugins"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]); 
		
			if (n==JOptionPane.YES_OPTION) {
					final util.ui.ProgressWindow win=new util.ui.ProgressWindow(mainFrame);
					win.show();
					win.setText(mLocalizer.msg("title.2","searching for new plugins..."));
					UpdateItem[] list=null;
					try {
						list=PluginUpdate.getPluginList();
					}catch (TvBrowserException exc) {
						util.exc.ErrorHandler.handle(exc);
						return;
					}finally{	
						win.dispose();
					}
					if (list.length==0) {
						JOptionPane.showMessageDialog(mainFrame,mLocalizer.msg("nopluginsfound","Sorry, no new plugins available"));
					}else {
						SelectPluginsDlg selDlg=new SelectPluginsDlg(mainFrame,list);
						UiUtilities.centerAndShow(selDlg);				
						selDlg.dispose();
						if (selDlg.getResult()==SelectPluginsDlg.OK) {
							DownloadPluginsDlg dlDlg=new DownloadPluginsDlg(mainFrame,list);
					
							dlDlg.dispose();
							JOptionPane.showMessageDialog(mainFrame,mLocalizer.msg("restartprogram","Pleas  restart TV-Browser"));		
						}						
					}
			}	
	}
	
	private static UpdateItem[] getPluginList() throws TvBrowserException {
		UpdateItem[] result;
		
		try {

			URL url=new URL("http://tvbrowser.sourceforge.net/plugins/plugins.txt");
			URLConnection con=url.openConnection();
		
			InputStream in=con.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
			result=getEntries(reader);
		
			reader.close();
		}catch (IOException e) {
			throw new TvBrowserException(PluginUpdate.class,"error.1","Could not read plugin list from server",e);
		}
		return result;
	}
	
	private static UpdateItem[] getEntries(BufferedReader reader) throws IOException {
		
			Pattern[] regexPatternArr = new Pattern[] {
						// Example: "[MyPlugin:plugin]"	
						Pattern.compile("\\[(.*):(.*)\\]"),
						// Example: "key=value"
						Pattern.compile("(.*)=(.*)"),
						// Example: "version=0.1"
						Pattern.compile("(.*)=(\\d+).(\\d+)")		
						};
					
			String line=reader.readLine();
			HashMap updateItems=new HashMap();
			UpdateItem curUpdateItem=null;
			VersionItem curVersionItem=null;
			Matcher matcher;
			while (line!=null) {
			
				matcher=regexPatternArr[0].matcher(line);
				if (matcher.find()) { // new plugin 
					String name=matcher.group(1);
					String typeStr=matcher.group(2);
					int type=-1;
					if ("plugin".equals(typeStr)) {
						type=UpdateItem.PLUGIN;
					}
					else if ("dataservice".equals(typeStr)) {
						type=UpdateItem.DATASERVICE;
					}
					
					curUpdateItem=(UpdateItem)updateItems.get(name+type);
					if (curUpdateItem==null) {
						curUpdateItem=new UpdateItem(name,type);
						updateItems.put(name+type,curUpdateItem);
					}
					curVersionItem=new VersionItem();
					curUpdateItem.addVersionItem(curVersionItem);								
				}else {
					matcher=regexPatternArr[1].matcher(line);  // key=value
					if (matcher.find()) {
						String key=matcher.group(1);
						String value=matcher.group(2);
						if ("version".equals(key)) {
							matcher=regexPatternArr[2].matcher(line);
							if (matcher.find()) {
								int major=Integer.parseInt(matcher.group(2));
								int minor=Integer.parseInt(matcher.group(3));
								curVersionItem.setVersion(new devplugin.Version(major,minor));							
							}
						}else if ("requires".equals(key)) {
							matcher=regexPatternArr[2].matcher(line);
							if (matcher.find()) {
								int major=Integer.parseInt(matcher.group(2));
								int minor=Integer.parseInt(matcher.group(3));
								devplugin.Version v=new devplugin.Version(major,minor);
								v.setStable(false);
								curVersionItem.setRequired(v);							
							}
						}else if ("download".equals(key)) {
							curVersionItem.setSource(value);
						}else if ("stable".equals(key)) {
							curVersionItem.setStable("yes".equals(value));	
						}				
					
					}
		
				} // end key-value
				line=reader.readLine();
			} // end loop
		
		// remove incompatible items
		Iterator it=updateItems.values().iterator();
		while (it.hasNext()) {
			UpdateItem ui=(UpdateItem)it.next();
			ui.removeIncompatibleVersions(TVBrowser.VERSION);
			if (ui.getNumOfVersions()==0) {
				it.remove();
			} 	
		}
		
		
		Object[]objs=updateItems.values().toArray();
		UpdateItem[] ui=new UpdateItem[objs.length];
		for (int i=0;i<ui.length;i++) {
			ui[i]=(UpdateItem)objs[i];
		}
		return ui;
		}
	
	
}