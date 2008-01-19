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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import util.ui.UiUtilities;

public class DataServiceSettingsTab implements devplugin.SettingsTab {
 
  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(DataServiceSettingsTab.class);
 
 
  public JPanel createSettingsPanel() {
    
    JPanel mainPanel=new JPanel(new BorderLayout());
    
    mainPanel.setBorder(BorderFactory.createEmptyBorder(7,7,7,7));
    
    JTextArea ta = UiUtilities.createHelpTextArea(mLocalizer.msg("description","description"));

    mainPanel.add(ta,BorderLayout.NORTH);
    
    return mainPanel;
  }

  
    /**
     * Called by the host-application, if the user wants to save the settings.
     */
    public void saveSettings() {
      
    }

  
    /**
     * Returns the name of the tab-sheet.
     */
    public Icon getIcon() {
      return null;
    }
  
  
    /**
     * Returns the title of the tab-sheet.
     */
    public String getTitle() {
      return mLocalizer.msg("title", "TV-Data Plugin");
    }
  
}