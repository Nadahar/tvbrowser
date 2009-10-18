/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package notifyosdplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

import util.paramhandler.ParamInputField;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

import devplugin.Plugin;
import devplugin.SettingsTab;

/**
 * settings tab for the notifyOSD plugin
 * 
 */
public class NotifyOSDSettingsTab implements SettingsTab {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(NotifyOSDSettingsTab.class);
  /** Settings to use */
  private NotifyOSDSettings mSettings;
  /** Input-Fields */
  private ParamInputField mTitle, mDescription;
  /** Instance of plugin */
  private NotifyOSDPlugin mPlugin;
  
  /**
   * Create the Settings-Tab
   * @param plugin The plugin
   * @param settings Settings-Tab
   */
  public NotifyOSDSettingsTab(final NotifyOSDPlugin plugin, final NotifyOSDSettings settings) {
    mPlugin = plugin;
    mSettings = settings;
  }

  /**
   * Create the GUI
   * @return Panel
   */
  public JPanel createSettingsPanel() {
    final EnhancedPanelBuilder panel = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + "," + FormFactory.PREF_COLSPEC.encode() + "," + FormFactory.RELATED_GAP_COLSPEC.encode() + ",pref:grow");
    final CellConstraints cc = new CellConstraints();
    
    panel.addRow();
    panel.add(UiUtilities.createHelpTextArea(
        mLocalizer.msg("help", "Help Text")), cc.xyw(2,panel.getRow(), 3));
    
    panel.addParagraph(mLocalizer.msg("title", "Title"));
    
    mTitle = new ParamInputField(mSettings.getTitle());
    
    panel.addRow();
    panel.add(mTitle, cc.xyw(2,panel.getRow(),3));
    
    panel.addParagraph(mLocalizer.msg("description", "Description"));
    
    mDescription = new ParamInputField(mSettings.getDescription());
    
    panel.addGrowingRow();
    panel.add(mDescription, cc.xyw(2,panel.getRow(),3));
    
    final JButton testGrowl = new JButton(mLocalizer.msg("test",
        "Test notification"));
    testGrowl.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent e) {
        mPlugin.showSingleNotification(Plugin.getPluginManager().getExampleProgram());
      }
      
    });
    
    panel.addParagraph(mLocalizer.msg("test","Test"));
    panel.addRow();
    panel.add(testGrowl, cc.xy(2,panel.getRowCount()));
    
    return panel.getPanel();
  }

  /**
   * Save the Input-Field
   */
  public void saveSettings() {
    mSettings.setTitle(mTitle.getText());
    mSettings.setDescription(mDescription.getText());
  }

  /**
   * Get the Icon for this Tab
   * @return Icon
   */
  public Icon getIcon() {
    return mPlugin.getPluginIcon();
  }

  /**
   * Get the Title for this Tab
   * @return Title
   */
  public String getTitle() {
    return mLocalizer.msg("name", "Growl Notification");
  }

}