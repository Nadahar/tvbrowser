/*
 * GrowlPlugin by Bodo Tasche
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
package growlplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

import util.paramhandler.ParamInputField;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.SettingsTab;

/**
 * The Settings-Tab for the Growl-Plugin
 * 
 * @author bodum
 */
public class GrowlSettingsTab implements SettingsTab {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GrowlSettingsTab.class);
  /** Settings to use */
  private GrowlSettings mSettings;
  /** Input-Fields */
  private ParamInputField mTitle, mDescription;
  /** Instance of Growl-Plugin */
  private GrowlPlugin mGrowlPlugin;
  
  /**
   * Create the Settings-Tab
   * @param plugin The Growl-Plugin
   * @param settings Settings-Tab
   */
  public GrowlSettingsTab(final GrowlPlugin plugin, final GrowlSettings settings) {
    mGrowlPlugin = plugin;
    mSettings = settings;
  }

  /**
   * Create the GUI
   * @return Panel
   */
  public JPanel createSettingsPanel() {
    
    final JPanel panel = new JPanel(
        new FormLayout(
            "5dlu, pref:grow, 3dlu, pref, 5dlu",
            "5dlu, pref, 3dlu, pref, 5dlu, fill:pref:grow, 3dlu, pref, 5dlu, fill:pref:grow, 3dlu, pref, 3dlu"));
    
    final CellConstraints cc = new CellConstraints();
    
    panel.add(UiUtilities.createHelpTextArea(
        mLocalizer.msg("help", "Help Text")), cc.xyw(2,2, 3));
    
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("title", "Title")), cc.xyw(1,4,5));
    
    mTitle = new ParamInputField(mSettings.getTitle());
    
    panel.add(mTitle, cc.xyw(2,6,3));
    
    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("description", "Description")), cc.xyw(1,8,5));
    
    mDescription = new ParamInputField(mSettings.getDescription());
    
    panel.add(mDescription, cc.xyw(2,10,3));
    
    final JButton testGrowl = new JButton(mLocalizer.msg("testGrowl",
        "Test Growl"));
    testGrowl.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent e) {
        final GrowlSettings testSettings = new GrowlSettings(null);
        testSettings.setTitle(mTitle.getText());
        testSettings.setDescription(mDescription.getText());
        mGrowlPlugin.getContainer().notifyGrowl(testSettings,
            Plugin.getPluginManager().getExampleProgram());
      }
      
    });
    
    panel.add(testGrowl, cc.xy(4,12));
    
    return panel;
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
    return mGrowlPlugin.getPluginIcon();
  }

  /**
   * Get the Title for this Tab
   * @return Title
   */
  public String getTitle() {
    return mLocalizer.msg("name", "Growl Notification");
  }

}