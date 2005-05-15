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

import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import util.paramhandler.ParamInputField;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
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
  private Properties mSettings;
  /** Input-Fields */
  private ParamInputField mTitle, mDescription;
  /** Was the Plugin initialized correctly ? */
  private boolean mInitialized;
  
  /**
   * Create the Settings-Tab
   * @param initialized True, if the OS is Mac and the Plugin was initialized correctly
   * @param settings Settings-Tab
   */
  public GrowlSettingsTab(boolean initialized, Properties settings) {
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
    
    
    JPanel panel = new JPanel(new FormLayout("default:grow", "default, 3dlu, fill:default:grow, 3dlu, fill:default:grow"));
    
    CellConstraints cc = new CellConstraints();
    
    panel.add(UiUtilities.createHelpTextArea(
        mLocalizer.msg("help", "Help Text")), cc.xy(1,1));
    
    mTitle = new ParamInputField(mSettings.getProperty("title"));
    mTitle.setBorder(BorderFactory.createTitledBorder(
        mLocalizer.msg("title", "Title")));
    
    panel.add(mTitle, cc.xy(1,3));
    
    mDescription = new ParamInputField(mSettings.getProperty("description"));
    mDescription.setBorder(BorderFactory.createTitledBorder(
        mLocalizer.msg("description", "Description")));
    
    panel.add(mDescription, cc.xy(1,5));
    
    return panel;
  }

  /**
   * Save the Input-Field
   */
  public void saveSettings() {
    if (!mInitialized){
      mSettings.setProperty("title", mTitle.getText());
      mSettings.setProperty("description", mDescription.getText());      
    }
  }

  /**
   * Get the Icon for this Tab
   * @return Icon
   */
  public Icon getIcon() {
    return new ImageIcon(ImageUtilities.createImageFromJar("growlplugin/growlclaw.png", GrowlSettingsTab.class));
  }

  /**
   * Get the Title for this Tab
   * @return Title
   */
  public String getTitle() {
    return mLocalizer.msg("name", "Growl Notification");
  }

}