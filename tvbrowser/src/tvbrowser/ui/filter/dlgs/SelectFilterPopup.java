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

package tvbrowser.ui.filter.dlgs;

import javax.swing.JMenuItem;

import tvbrowser.ui.mainframe.MainFrame;
import util.ui.ScrollableMenu;


/**
 * Creates a Popup for Filtering
 */
public class SelectFilterPopup extends ScrollableMenu{
    
    /** MainFrame */
    private MainFrame mMainFrame;
    
    /**
     * Creates the Popup
     * @param mainFrame MainFrame
     */
    public SelectFilterPopup(MainFrame mainFrame) {
        mMainFrame = mainFrame;
        FilterButtons filterButtons = new FilterButtons(mMainFrame);
        
        JMenuItem[] filterMenuItems = filterButtons.createFilterMenuItems();
        for (JMenuItem filterMenuItem : filterMenuItems) {
          if (filterMenuItem != null) {
              add(filterMenuItem);
          } else {
              addSeparator();
          }
        }

    }

}