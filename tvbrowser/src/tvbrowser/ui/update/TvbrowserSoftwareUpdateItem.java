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

import util.browserlauncher.Launch;
import util.exc.TvBrowserException;
import util.ui.BrowserLauncher;

public class TvbrowserSoftwareUpdateItem extends SoftwareUpdateItem {

  public TvbrowserSoftwareUpdateItem(String name) {
    super(name);
  }

  protected boolean download(String url) throws TvBrowserException {
    try {
      Launch.openURL("http://www.tvbrowser.org");
    } catch (Exception exc) {
      throw new TvBrowserException(BrowserLauncher.class, "error.1", "Could not open webbrowser", exc);
    }
    
    return true;
  }

}