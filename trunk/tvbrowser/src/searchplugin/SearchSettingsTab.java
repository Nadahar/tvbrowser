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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

import util.ui.SearchFormSettings;
import devplugin.SettingsTab;

/**
 * This Class represents the SettingsTab for the Search-Plugin
 * 
 * @author bodum
 */
public class SearchSettingsTab implements SettingsTab {

  /** The localizer of this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SearchSettingsTab.class);
 
  /**
   * Create the Settings-Panel
   * @return Settings-Panel
   */
  public JPanel createSettingsPanel() {
    
    JPanel panel = new JPanel();
    
    JButton clearHistory = new JButton(mLocalizer.msg("clearHistory", "Clear Search History"));

    clearHistory.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SearchPlugin.setSearchHistory(new SearchFormSettings[0]);
      }
    });
    
    panel.add(clearHistory);
    
    return panel;
  }

  /**
   * Save Settigns
   */
  public void saveSettings() {
  }

  /**
   * Get Icon
   * @return the Icon for this SettingsTab
   */
  public Icon getIcon() {
    return SearchPlugin.getInstance().createImageIcon("actions", "system-search", 16);
  }

  /**
   * Get Title
   * @return the Title for this SettingsTab
   */
  public String getTitle() {
    return mLocalizer.msg("title", "Search");
  }

}