/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package googlesearchplugin;

import java.io.IOException;

import util.ui.BrowserLauncher;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

/**
 * This Plugin starts a search in Google for the given Movie
 *
 * @author Bodo Tasche
 */
public class GoogleSearchPlugin extends devplugin.Plugin {
	
		private static final util.ui.Localizer mLocalizer
			= util.ui.Localizer.getLocalizerFor(GoogleSearchPlugin.class );
		
		public String getContextMenuItemText() {
			return mLocalizer.msg( "contextMenuText" ,"Search at Google" );
		}
  		
		public PluginInfo getInfo() {
			String name = mLocalizer.msg( "pluginName" ,"Google Search" );
			String desc = mLocalizer.msg( "description" ,"Searches at Google for a Movie" );
			String author = "Bodo Tasche" ;
			return new PluginInfo(name, desc, author, new Version(1, 0));
  		} 
  		
  		public String getButtonText() {
  			return null ;
  		}
  		
  		public void execute(Program program) {
  			String search = program.getTitle();
  			search = search.trim().replace(' ', '+');
  			
  			try {
				BrowserLauncher.openURL("http://www.google.com/search?q=" + search);
			} catch (IOException e) {
				e.printStackTrace();
			}
  		}
  		
  		public String getMarkIconName() {
  			return "googlesearchplugin/google.gif";
  		}
  		
  		public String getButtonIconName() {
  			return null ;
 		 }
  
} 