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

import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;

import java.net.*;
import java.io.*;

import javax.swing.JOptionPane;

public class PluginSoftwareUpdateItem extends AbstractSoftwareUpdateItem {
	
  
  private boolean mSuccess;
  
    private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PluginSoftwareUpdateItem.class);
  
  
	public PluginSoftwareUpdateItem(String name) {
		super(name);
	}
	
	public boolean download() throws TvBrowserException {
    
    final File toFile=new File("plugins",mName+".jar.inst");
    
    
    mSuccess=true;
    ProgressWindow progWin=new util.ui.progress.ProgressWindow(null,mLocalizer.msg("downloading","downloading update item..."));
    progWin.run(new Progress(){
      public void run() {
        
        try {
				  IOUtilities.download(new URL(mUrl),toFile);
		    } catch (MalformedURLException e) {
					mSuccess=false;
			  } catch (IOException e) {
          mSuccess=false;
        }
      }
    });
      
    if (mSuccess) {
      JOptionPane.showMessageDialog(null,mLocalizer.msg("restartprogram","please restart tvbrowser before..."));
    }
    else {
      JOptionPane.showMessageDialog(null,mLocalizer.msg("error.1","donwload failed",mUrl,toFile.getAbsolutePath()));
    }
  
    return mSuccess;
	}
	
	
}