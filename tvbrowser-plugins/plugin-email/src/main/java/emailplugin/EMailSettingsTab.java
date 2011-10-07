/*
 * EMailPlugin by Bodo Tasche
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
 *     $Date: 2010-01-16 08:54:02 +0100 (Sa, 16 Jan 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6320 $
 */
package emailplugin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.misc.OperatingSystem;
import util.paramhandler.ParamInputField;
import util.ui.Localizer;
import util.ui.PluginProgramConfigurationPanel;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * This Class implements the Settings-Tab
 * 
 * @author bodum
 */
public final class EMailSettingsTab implements SettingsTab {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(EMailSettingsTab.class);

  /** Settings */
  private EMailSettings mSettings;

  /** Application-Path */
  private JTextField mApplication;

  /** Parameters */
  private ParamInputField mParameter;

  /** Plugin */
  private EMailPlugin mPlugin;
  
  /** Use the default Application on that OS ? */
  private JCheckBox mDefaultApplication;

  /** Opens a File-Select Dialog for the App*/
  private JButton mAppFinder;

  /** The Help-Text */
  private JEditorPane mHelpText;  /** Parameter-Label */
  private JLabel mParameterLabel;
  /** Application-Label */
  private JLabel mAppLabel;
  
  private PluginProgramConfigurationPanel mConfigPanel;
  
  /**
   * Creates the SettingsTab
   * 
   * @param plugin Plugin
   * @param settings Settings to use
   */
  public EMailSettingsTab(EMailPlugin plugin, EMailSettings settings) {
    mPlugin = plugin;
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final JPanel configPanel = new JPanel();

    FormLayout layout = new FormLayout("5dlu, pref, 3dlu, pref:grow, fill:75dlu, 3dlu, pref, 5dlu",
        "5dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 10dlu, fill:default:grow, 3dlu");
    configPanel.setLayout(layout);

    CellConstraints cc = new CellConstraints();

    boolean osOk = OperatingSystem.isMacOs() || OperatingSystem.isWindows();

    mDefaultApplication = new JCheckBox();
    mDefaultApplication.setEnabled(osOk);

    if (!osOk) {
      mDefaultApplication.setText(mLocalizer.msg("defaultApp", "Default Application")
                  + " (" +mLocalizer.msg("notOnYourOS", "Function only Available on Windows and Mac OS") + ")");
    } else {
      mDefaultApplication.setText(mLocalizer.msg("defaultApp", "Default Application"));
      mDefaultApplication.setSelected(mSettings.getUseDefaultApplication());
    }
    
    configPanel.add(mDefaultApplication, cc.xyw(2,2, 6));
    
    mAppLabel = new JLabel(mLocalizer.msg("Application", "Application") + ":");
    configPanel.add(mAppLabel, cc.xy(2, 4));

    mApplication = new JTextField(mSettings.getApplication());

    configPanel.add(mApplication, cc.xyw(4, 4, 2));

    mAppFinder = new JButton("...");
    mAppFinder.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        findApplication(configPanel);
      }

    });

    configPanel.add(mAppFinder, cc.xy(7, 4));

    mParameterLabel = new JLabel(mLocalizer.msg("Parameter", "Parameter") + ":");
    configPanel.add(mParameterLabel, cc.xy(2, 6));

    mParameter = new ParamInputField(new EMailParamLibrary("mailto:?body="), mSettings.getParameter(), true);

    configPanel.add(mParameter, cc.xyw(4, 6, 4));

    mHelpText = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("Desc","Desc", "{" + EMailParamLibrary.KEY_MAIL_TEXT + "}"));
    configPanel.add(mHelpText, cc.xyw(2,8,6));

    mDefaultApplication.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setInputState();
      }
    });
    
    setInputState();

    mConfigPanel = new PluginProgramConfigurationPanel(mPlugin.getSelectedPluginProgramFormattings(), mPlugin.getAvailableLocalPluginProgramFormattings(), EMailPlugin.getDefaultFormatting(), true ,true);

    configPanel.add(mConfigPanel, cc.xyw(1, 10, 7));
    
    JPanel panel = new JPanel(new BorderLayout());
    
    panel.add(configPanel, BorderLayout.NORTH);
    
    return panel;
  }

  /**
   * Sets the input states of the dialogs
   */
  private void setInputState() {
    mApplication.setEnabled(!mDefaultApplication.isSelected());
    mParameter.setEnabled(!mDefaultApplication.isSelected());
    mAppFinder.setEnabled(!mDefaultApplication.isSelected());
    mHelpText.setEnabled(!mDefaultApplication.isSelected());
    mParameterLabel.setEnabled(!mDefaultApplication.isSelected());
    mAppLabel.setEnabled(!mDefaultApplication.isSelected());
  }
  
  /**
   * Opens a FileChooser and let the User open a File
   */
  private void findApplication(JPanel panel) {
    JFileChooser chooser = new JFileChooser(new File(mApplication.getText()));
    int returnVal = chooser.showOpenDialog(UiUtilities.getBestDialogParent(panel));
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      mApplication.setText(chooser.getSelectedFile().getAbsolutePath());
    }
  }

  public void saveSettings() {
    mSettings.setApplication(mApplication.getText());
    mSettings.setParameter(mParameter.getText());
    mSettings.setDefaultApplication(mDefaultApplication.isSelected());
   
    mPlugin.setAvailableLocalPluginProgramFormattings(mConfigPanel.getAvailableLocalPluginProgramFormatings());
    mPlugin.setSelectedPluginProgramFormattings(mConfigPanel.getSelectedPluginProgramFormatings());
  }

  public Icon getIcon() {
    return mPlugin.createImageIcon("action", "mail-message-new", 16);
  }

  public String getTitle() {
    return mLocalizer.msg("name", "Send EMail");
  }
}