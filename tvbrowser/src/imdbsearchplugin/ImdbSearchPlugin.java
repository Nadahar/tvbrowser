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

package imdbsearchplugin;

import java.io.IOException;

import util.ui.BrowserLauncher;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

/**
 * This Plugin starts a search in IMDB for the given Movie
 *
 * @author Bodo Tasche
 */
public class ImdbSearchPlugin extends devplugin.Plugin {
	
		private static final util.ui.Localizer mLocalizer
			= util.ui.Localizer.getLocalizerFor(ImdbSearchPlugin.class );
		
		public String getContextMenuItemText() {
			return mLocalizer.msg( "contextMenuText" ,"Search at IMDB.com" );
  		}
  		
  		public PluginInfo getInfo() {
			String name = mLocalizer.msg( "pluginName" ,"IDMB Search" );
			String desc = mLocalizer.msg( "description" ,"Searches at IMDB for a Movie" );
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
				BrowserLauncher.openURL("http://www.imdb.com/Tsearch?title=" + search);
			} catch (IOException e) {
				e.printStackTrace();
			}
  		}
  		
  		public String getMarkIconName() {
  			return "imdbsearchplugin/imdb.gif";
  		}
  		
  		public String getButtonIconName() {
  			return null ;
 		 }
  
} 