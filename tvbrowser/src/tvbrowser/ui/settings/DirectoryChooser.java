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

package tvbrowser.ui.settings;

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.*;


public class DirectoryChooser extends JPanel {
   
    /** The localizer for this class. */
       private static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(DirectoryChooser.class);
 
   
    private HashSet mSet;
    
    public DirectoryChooser() {
      setLayout(new GridLayout(0,1,0,3));
    
      setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("UserDefinedFolders", "User defined folders")));
      mSet=new HashSet();
    }
    
    

    public void addDirectoryChooserPanel(util.ui.DirectoryChooserPanel panel) {
      super.add(panel);
      mSet.add(panel);
    }
    
    public void setEnabled(boolean enabled) {
      super.setEnabled(enabled);
      Iterator it=mSet.iterator();
      while (it.hasNext()) {
        ((JPanel)it.next()).setEnabled(enabled);
      }
      
    }
    
  }