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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package listviewplugin;

import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import util.ui.EnhancedPanelBuilder;
import util.ui.PluginsPictureSettingsPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

import devplugin.SettingsTab;

/**
 * Configuration Dialog for the ListView Plugin
 * 
 * @author bodum
 */
public class ListViewSettings implements SettingsTab {
  /** Translator */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ListViewSettings.class);
 
  /** The Settings */
  private Properties mSettings;
  /** Checkbox for showing at startup*/
  private JCheckBox mShowAtStart;
  /** Picture settings */
  private PluginsPictureSettingsPanel mPictureSettings;
  
  /**
   * Create the SettingsTab
   * @param settings Settings
   */
  public ListViewSettings(Properties settings) {
    mSettings = settings;
  }
  
  /**
   * Create the Panel
   */
  public JPanel createSettingsPanel() {
    EnhancedPanelBuilder panel = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + ",default:grow");
    
    CellConstraints cc = new CellConstraints();
    
    mShowAtStart = new JCheckBox(mLocalizer.msg("showAtStart", "Show at startup"));
    mShowAtStart.setSelected(mSettings.getProperty("showAtStartup", "false").equals("true"));

    mPictureSettings = new PluginsPictureSettingsPanel(ListViewPlugin.getInstance().getPictureSettings(), false);
    
    panel.addRow();
    panel.add(mShowAtStart, cc.xy(2,panel.getRow()));
    
    panel.addParagraph(PluginsPictureSettingsPanel.getTitle());
    
    panel.addGrowingRow();
    panel.add(mPictureSettings, cc.xy(2,panel.getRow()));
        
    return panel.getPanel();
  }

  /**
   * Save the Settings
   */
  public void saveSettings() {
    mSettings.setProperty("showAtStartup", String.valueOf(mShowAtStart.isSelected()));
    mSettings.setProperty("pictureSettings", String.valueOf(mPictureSettings.getSettings().getType()));
  }

  /**
   * Icon for the SettingsTab
   */
  public Icon getIcon() {
    return ListViewPlugin.getInstance().createImageIcon("actions", "view-list", 16);
  }

  /**
   * Get the Title
   */
  public String getTitle() {
    return mLocalizer.msg("settingsTabName", "ListView Plugin");
  }

}