/*
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
package blogthisplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.PluginProgramConfigurationPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

import devplugin.SettingsTab;

/**
 * Settings for the BlogSettings
 * 
 * @author bodum
 */
public class BlogSettingsTab implements SettingsTab {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(BlogSettingsTab.class);

  /** Plugin */
  private BlogThisPlugin mPlugin;

  /** Settings for this Plugin */
  private BlogSettings mSettings;

  /** ComboBox for Blog-Service */
  private JComboBox mServiceCombo;

  /** Label for the Url-Inputfield */
  private JLabel mServiceUrlLabel;

  /** Textfield for Url */
  private JTextField mServiceUrlField;

  private PluginProgramConfigurationPanel mConfigPanel;

  /**
   * Create Plugin-Settingstab
   * 
   * @param plugin
   *          Plugin
   * @param settings
   *          Settings of this Plugin
   */
  public BlogSettingsTab(BlogThisPlugin plugin, BlogSettings settings) {
    mPlugin = plugin;
    mSettings = settings;
  }

  /**
   * Create the SettingsPanel
   */
  public JPanel createSettingsPanel() {
    final EnhancedPanelBuilder settingsPanel = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + ","
        + FormFactory.PREF_COLSPEC.encode() + "," + FormFactory.RELATED_GAP_COLSPEC.encode() + ","
        + FormFactory.PREF_COLSPEC.encode() + ", fill:pref:grow");

    String[] services = { "", "Blogger.com", "Wordpress", "b2evolution" };

    mServiceCombo = new JComboBox(services);

    if (mSettings.getBlogService() == BlogService.Blogger) {
      mServiceCombo.setSelectedIndex(1);
    } else if (mSettings.getBlogService() == BlogService.WordPress) {
      mServiceCombo.setSelectedIndex(2);
    } else if (mSettings.getBlogService() == BlogService.B2Evolution) {
      mServiceCombo.setSelectedIndex(3);
    }

    mServiceCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateAfterServiceSelection();
      }
    });

    mServiceUrlLabel = new JLabel(mLocalizer.msg("Url", "Url") + ':');
    mServiceUrlLabel.setEnabled(mServiceCombo.getSelectedIndex() >= 2);

    mServiceUrlField = new JTextField();
    mServiceUrlField.setEnabled(mServiceCombo.getSelectedIndex() >= 2);
    mServiceUrlField.setText(mSettings.getBlogUrl());

    CellConstraints cc = new CellConstraints();

    settingsPanel.addRow();
    settingsPanel.add(new JLabel(mLocalizer.msg("Service", "Blog-Service") + ':'), cc.xy(2, settingsPanel.getRow()));
    settingsPanel.add(mServiceCombo, cc.xy(4, settingsPanel.getRow()));

    settingsPanel.addRow();
    settingsPanel.add(mServiceUrlLabel, cc.xy(2, settingsPanel.getRow()));
    settingsPanel.add(mServiceUrlField, cc.xyw(4, settingsPanel.getRow(), 2));

    mConfigPanel = new PluginProgramConfigurationPanel(mPlugin.getSelectedPluginProgramFormattings(), mPlugin
        .getAvailableLocalPluginProgramFormattings(), BlogThisPlugin.getDefaultFormatting(), true, false);

    settingsPanel.addParagraph(mLocalizer.msg("formattings", "Formattings"));

    settingsPanel.addGrowingRow();
    settingsPanel.add(mConfigPanel, cc.xyw(1, settingsPanel.getRow(), 5));

    return settingsPanel.getPanel();
  }

  /**
   * Updates the GUI after a new Service is selected
   */
  private void updateAfterServiceSelection() {
    mServiceUrlLabel.setEnabled(mServiceCombo.getSelectedIndex() >= 2);
    mServiceUrlField.setEnabled(mServiceCombo.getSelectedIndex() >= 2);

    if (mServiceCombo.getSelectedIndex() == 2) {
      mServiceUrlField.setText(BlogThisPlugin.URL_WORDPRESS);
    } else if (mServiceCombo.getSelectedIndex() == 3) {
      mServiceUrlField.setText(BlogThisPlugin.URL_B2EVOLUTION);
    } else {
      mServiceUrlField.setText("");
    }
  }

  /**
   * Saves the Settings
   */
  public void saveSettings() {
    switch (mServiceCombo.getSelectedIndex()) {
    case 1:
      mSettings.setService(BlogService.Blogger);
      break;
    case 2:
      mSettings.setService(BlogService.WordPress);
      break;
    case 3:
      mSettings.setService(BlogService.B2Evolution);
      break;

    default:
      mSettings.setService(null);
      break;
    }
    mSettings.setBlogUrl(mServiceUrlField.getText());

    mPlugin.setAvailableLocalPluginProgramFormattings(mConfigPanel.getAvailableLocalPluginProgramFormatings());
    mPlugin.setSelectedPluginProgramFormattings(mConfigPanel.getSelectedPluginProgramFormatings());
  }

  public Icon getIcon() {
    return mPlugin.createImageIcon("apps", "internet-web-browser", 16);
  }

  public String getTitle() {
    return mLocalizer.msg("Name", "Blog this!");
  }

}
