/*
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
package wirschauenplugin;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import util.io.IOUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;
import util.ui.UiUtilities;

/**
 * @author uzi
 */
public class WirSchauenSettingsTab implements SettingsTab
{
  /**
   * the checkbox that holds the marker-option (whether or not to mark the linked programs).
   */
  private JCheckBox mMarkerCheckbox;

  /**
   * a panel to select the color for marking linked programs.
   */
  private DefaultMarkingPrioritySelectionPanel mMarkingColorChooser;

  /**
   * the underlying settings.
   */
  private WirSchauenSettings mSettings;

  private JCheckBox mSpellCheck;
  
  private JTextField mOmdbUsername;
  private JPasswordField mOmdbPassword;

  /**
   * @param settings the underlying settings for this tab.
   */
  public WirSchauenSettingsTab(final WirSchauenSettings settings) {
    mSettings = settings;
  }


  /**
   * {@inheritDoc}
   * @see devplugin.SettingsTab#createSettingsPanel()
   */
  public JPanel createSettingsPanel()
  {
    mMarkerCheckbox = new JCheckBox(WirSchauenPlugin.LOCALIZER.msg("Settings.ShowMarking", "Highlight programs which are linked with the OMDB."), mSettings.getMarkPrograms());
    mMarkingColorChooser = DefaultMarkingPrioritySelectionPanel.createPanel(new int[] {mSettings.getMarkPriorityForOmdbLink(), mSettings.getMarkPriorityForOwnOmdbLink()}, new String[] {WirSchauenPlugin.LOCALIZER.msg("Settings.LinkedPrio", "How to mark programs which are linked with omdb.org."), WirSchauenPlugin.LOCALIZER.msg("Settings.OwnLinkedPrio", "How to mark programs which I have linked with omdb.org.")}, false, false, false);
//    mLinkedProgramColorChooser.setEnabled(mMarkerCheckbox.isSelected());

    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, pref, 5dlu, default:grow", "5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

    pb.add(mMarkerCheckbox, cc.xyw(2, 2, 3));
    pb.add(mMarkingColorChooser, cc.xyw(2, 4, 3));

//    mMarkerCheckbox.addItemListener(new ItemListener() {
//      public void itemStateChanged(final ItemEvent e) {
//        mLinkedProgramColorChooser.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
//      }
//    });
    mSpellCheck = new JCheckBox(WirSchauenPlugin.LOCALIZER.msg("Settings.SpellCheck", "Activate spell checker"), mSettings.getSpellChecking());
    pb.add(mSpellCheck, cc.xy(2,6));
    
    pb.addSeparator(WirSchauenPlugin.LOCALIZER.msg("Settings.OmdbAuthentification", "Omdb authentification"), cc.xyw(2,8,3));
    
    pb.add(UiUtilities.createHtmlHelpTextArea(WirSchauenPlugin.LOCALIZER.msg("Settings.OmdbAuthentificationInfo","Only needed to directly edit Omdb descriptions via the Wirschauen plugin.")),cc.xyw(2,10,3));

    pb.addLabel(WirSchauenPlugin.LOCALIZER.msg("Settings.OmdbUsername", "Username") + ":",cc.xy(2,12));

    mOmdbUsername = new JTextField(mSettings.getOmdbUsername());
    pb.add(mOmdbUsername, cc.xy(4,12));
    
    pb.addLabel(WirSchauenPlugin.LOCALIZER.msg("Settings.OmdbPassword", "Password") + ":",cc.xy(2,14));
   
    mOmdbPassword = new JPasswordField(IOUtilities.xorDecode(mSettings.getOmdbPassword(),WirSchauenSettings.PASSWORDSEED));
    pb.add(mOmdbPassword, cc.xy(4,14));


    return pb.getPanel();
  }


  /**
   * {@inheritDoc}
   * @see devplugin.SettingsTab#getIcon()
   */
  public Icon getIcon()
  {
    //use image caching and lazy init from plugin class
    return WirSchauenPlugin.getInstance().getIcon();
  }


  /**
   * {@inheritDoc}
   * @see devplugin.SettingsTab#getTitle()
   */
  public String getTitle()
  {
    return WirSchauenPlugin.LOCALIZER.msg("name", "WirSchauen");
  }

  /**
   * {@inheritDoc}
   * @see devplugin.SettingsTab#saveSettings()
   */
  public void saveSettings()
  {
    //this is not the persistence api for the settings, but the callback
    //for the ok-button. as soon as the user accepts the settings, this
    //method will be called. so tell the plugin the new settings.
    mSettings.setMarkPrograms(mMarkerCheckbox.isSelected());
    mSettings.setMarkPriorityForOmdbLink(mMarkingColorChooser.getSelectedPriority(0));
    mSettings.setMarkPriorityForOwnOmdbLink(mMarkingColorChooser.getSelectedPriority(1));
    mSettings.setOmdbUsername(mOmdbUsername.getText());
    mSettings.setOmdbPassword(IOUtilities.xorEncode(new String(mOmdbPassword.getPassword()), WirSchauenSettings.PASSWORDSEED));
    mSettings.setSpellChecking(mSpellCheck.isSelected());
  }
}
