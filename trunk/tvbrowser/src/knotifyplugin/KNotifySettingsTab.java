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
package knotifyplugin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import util.paramhandler.ParamInputField;
import util.ui.EnhancedPanelBuilder;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

import devplugin.Plugin;
import devplugin.SettingsTab;

/**
 * The Settings-Tab for the KNotify-Plugin
 * 
 * @author Michael Keppler
 */
public class KNotifySettingsTab implements SettingsTab {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(KNotifySettingsTab.class);

  /** Settings to use */
  private KNotifySettings mSettings;

  /** Input-Fields */
  private ParamInputField mTitle, mDescription;

  /** was the plugin initialized correctly? */
  private boolean mInitialized;

  /** instance of plugin */
  private KNotifyPlugin mPlugin;
  
  /**
   * Create the settings tab
   * @param plugin the Plugin
   * @param initialized <code>true</code>, if the plugin was initialized correctly and runs on KDE
   * @param settings properties of the plugin
   */
  public KNotifySettingsTab(KNotifyPlugin plugin, boolean initialized, KNotifySettings settings) {
    mPlugin = plugin;
    mSettings = settings;
    mInitialized = initialized;
  }

  /**
   * Create the GUI
   * @return Panel
   */
  public JPanel createSettingsPanel() {
    if (!mInitialized) {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(UiUtilities.createHelpTextArea(
          mLocalizer.msg("notinit", "Error, System not init")), BorderLayout.NORTH);
      return panel;
    }
    
    EnhancedPanelBuilder panel = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + ", pref:grow, " + FormFactory.RELATED_GAP_COLSPEC.encode() +"," + FormFactory.PREF_COLSPEC.encode());
    
    CellConstraints cc = new CellConstraints();
    
    panel.addRow();
    panel.add(UiUtilities.createHelpTextArea(
        mLocalizer.msg("help", "Help Text")), cc.xyw(2,panel.getRow(), 3));
    panel.addParagraph(mLocalizer.msg("title", "Title"));
    mTitle = new ParamInputField(mSettings.getTitle());
    panel.addGrowingRow();
    panel.add(mTitle, cc.xyw(2,panel.getRow(),3));
    
    panel.addParagraph(mLocalizer.msg("description", "Description"));
    
    mDescription = new ParamInputField(mSettings.getDescription());
    panel.addGrowingRow();
    panel.add(mDescription, cc.xyw(2,panel.getRow(),3));
    
    JButton testGrowl = new JButton(mLocalizer.msg("testKNotify", "Test KNotify"));
    testGrowl.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        mPlugin.sendToKNotify(mTitle.getText(), mDescription.getText(), Plugin.getPluginManager().getExampleProgram());
      }
      
    });
    panel.addRow();
    panel.add(testGrowl, cc.xy(4,12));
    return panel.getPanel();
  }

  /**
   * Save the Input-Field
   */
  public void saveSettings() {
    if (mInitialized && mSettings != null){
      mSettings.setTitle(mTitle.getText());
      mSettings.setDescription(mDescription.getText());
    }
  }

  /**
   * Get the Icon for this Tab
   * @return Icon
   */
  public Icon getIcon() {
    return new ImageIcon(ImageUtilities.createImageFromJar("knotifyplugin/knotify.png", KNotifySettingsTab.class));
  }

  /**
   * Get the Title for this Tab
   * @return Title
   */
  public String getTitle() {
    return mLocalizer.msg("name", "KNotify notification");
  }

}