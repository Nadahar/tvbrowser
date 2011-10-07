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
 *     $Date: 2009-09-04 13:38:57 +0200 (Fr, 04 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5954 $
 */
package clipboardplugin;

import javax.swing.Icon;
import javax.swing.JPanel;

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.PluginProgramConfigurationPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

import devplugin.SettingsTab;

/**
 * This is the Settings-Tab for the ClipboardPlugin
 * 
 * @author bodum
 */
public class ClipboardSettingsTab implements SettingsTab {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ClipboardSettingsTab.class);

  /** Plugin */
  private ClipboardPlugin mPlugin;
  
  private PluginProgramConfigurationPanel mConfigPanel;
  
  /**
   * Creates the SettingsTab
   * 
   * @param plugin The plugin instance.
   */
  public ClipboardSettingsTab(ClipboardPlugin plugin) {
    mPlugin = plugin;
  }

  /**
   * Creates the SettingsPanel
   * 
   * @return Settings-Panel
   */
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + ",fill:default:grow");
    CellConstraints cc = new CellConstraints();
    
    mConfigPanel = new PluginProgramConfigurationPanel(mPlugin.getSelectedPluginProgramFormatings(), mPlugin.getAvailableLocalPluginProgramFormatings(), ClipboardPlugin.getDefaultFormating(),false,false);
    
    pb.addRow();
    pb.add(mConfigPanel, cc.xyw(1,pb.getRow(),pb.getColumnCount()));

    return pb.getPanel();
  }

  /**
   * Save the Settings
   */
  public void saveSettings() {
    mPlugin.setAvailableLocalPluginProgramFormatings(mConfigPanel.getAvailableLocalPluginProgramFormatings());
    mPlugin.setSelectedPluginProgramFormatings(mConfigPanel.getSelectedPluginProgramFormatings());
  }

  /**
   * Get the Tab-Icon
   * 
   * @return Icon
   */
  public Icon getIcon() {
    return mPlugin.createImageIcon("actions", "edit-paste", 16);
  }

  /**
   * Get the Title for this Tab
   * 
   * @return Tab-Title
   */
  public String getTitle() {
    return mLocalizer.msg("title", "Clipboard Settings");
  }
}