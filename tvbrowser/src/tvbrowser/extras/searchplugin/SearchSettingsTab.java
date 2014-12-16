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
package tvbrowser.extras.searchplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import util.ui.SearchFormSettings;
import util.ui.TVBrowserIcons;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * This Class represents the SettingsTab for the Search-Plugin
 *
 * @author bodum
 */
class SearchSettingsTab implements SettingsTab {

  /** The localizer of this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SearchSettingsTab.class);

  private JCheckBox mAlwaysExpertMode;
  /**
   * Create the Settings-Panel
   * @return Settings-Panel
   */
  public JPanel createSettingsPanel() {
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default,0dlu:grow","default,5dlu,default,2dlu,default"));
    pb.border(Borders.DIALOG);
    
    mAlwaysExpertMode = new JCheckBox(mLocalizer.msg("alwaysExpert", "Use expert mode for repetition search also"), SearchPlugin.getAlwaysSearchExpert());
    
    JButton clearHistory = new JButton(mLocalizer.msg("clearHistory", "Clear Search History"));

    clearHistory.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SearchPlugin.setSearchHistory(new SearchFormSettings[0]);
      }
    });

    pb.addSeparator(mLocalizer.msg("title", "Search"), CC.xyw(1,1,3));
    pb.add(mAlwaysExpertMode, CC.xyw(2,3,2));
    pb.add(clearHistory, CC.xy(2,5));

    return pb.getPanel();
  }

  /**
   * Save Settings
   */
  public void saveSettings() {
    SearchPlugin.setAlwaysSearchExpert(mAlwaysExpertMode.isSelected());
  }

  /**
   * Get Icon
   * @return the Icon for this SettingsTab
   */
  public Icon getIcon() {
    return TVBrowserIcons.search(TVBrowserIcons.SIZE_SMALL);
  }

  /**
   * Get Title
   * @return the Title for this SettingsTab
   */
  public String getTitle() {
    return mLocalizer.msg("title", "Search");
  }

}