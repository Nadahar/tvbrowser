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

import java.io.File;
import java.net.URL;

import javax.swing.JOptionPane;

import util.exc.TvBrowserException;
import util.io.IOUtilities;

public class DataserviceSoftwareUpdateItem extends AbstractSoftwareUpdateItem {
	
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(DataserviceSoftwareUpdateItem.class);
  
  
	public DataserviceSoftwareUpdateItem(String name) {
		super(name);
	}
	
  public boolean download() throws TvBrowserException {
    
    boolean success=false;
    File toFile=new File("dataservice",mName+".jar.inst");
    try {
      IOUtilities.download(new URL(mUrl),toFile);
      JOptionPane.showMessageDialog(null,mLocalizer.msg("restartprogram","please restart tvbrowser before..."));
      success=true;
    }catch (Exception exc) {
      throw new TvBrowserException(AbstractSoftwareUpdateItem.class, "error.1",
              "Download failed", mUrl,toFile.getAbsolutePath(),exc);
    }
    
    
    return success;
  }
	
	
}