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

package searchplugin;

import util.ui.UiUtilities;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;


import devplugin.*;

/**
 * Provides a dialog for searching programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SearchPlugin extends Plugin {

  /** The localizer for this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SearchPlugin.class);

private static SearchPlugin mInstance;

 static ArrayList searchHistory=new ArrayList();
 static final int MAXHISTORYLENGTH=10;
  
  /**
   * Creates a new instance of SearchPlugin.
   */
  public SearchPlugin() {
  	mInstance=this;
  }

  public void readData(ObjectInputStream in)
	  throws /*IOException, */ClassNotFoundException
	{
	  try {
	  	int version = in.readInt();
	  	int cntHistItems=in.readInt();
	  	//searchHistory.new HistoryItem[cntHistItems];
	  	
	  	for (int i=0;i<cntHistItems;i++) {
	  		//searchHistory[i]=new HistoryItem(in);
	  		searchHistory.add(new SearchSettings(in));
	  	}
	  	
	  }catch (IOException e) {
	  	
	  }

	}
	
	public void writeData(ObjectOutputStream out) throws IOException {
		out.writeInt(1); // version

		out.writeInt(searchHistory.size());
		Iterator iterator=searchHistory.iterator();
		while (iterator.hasNext()) {
			((SearchSettings)iterator.next()).writeData(out);			
		}		
	  }
  
  
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the menu.
   */
  public void execute() {
    SearchDialog dlg = new SearchDialog(getParentFrame());
    UiUtilities.centerAndShow(dlg);
  }



  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the context menu.
   */
  public void execute(Program program) {
    SearchDialog dlg = new SearchDialog(getParentFrame());
    dlg.setPatternText(program.getTitle());
    UiUtilities.centerAndShow(dlg);
  }



  /**
   * Returns the name of the file, containing your plugin icon (in the jar-File).
   */ 
  public String getMarkIconName() {
    return "searchplugin/Find16.gif";
  }



  /**
   * This method is called by the host-application to show the plugin in the
   * context menu.
   */
  public String getContextMenuItemText() {
    return mLocalizer.msg( "searchRepetion", "Search repetition" );
  }

  
  public String getButtonIconName() {
    return "searchplugin/Find16.gif";
  }
  
  public String getButtonText() {
  	return mLocalizer.msg( "searchPrograms", "Search programs" );
  }
  
  public PluginInfo getInfo() {
    String name = mLocalizer.msg( "searchPrograms" ,"Search programs" );
    String desc = mLocalizer.msg( "description" ,"Allows searching programs containing a certain text." );
    String author = "Til Schneider, www.murfman.de" ;

    return new PluginInfo(name, desc, author, new Version(1, 4));
  }
  
  public static SearchPlugin getInstance() {
	return mInstance;
  } 

} 