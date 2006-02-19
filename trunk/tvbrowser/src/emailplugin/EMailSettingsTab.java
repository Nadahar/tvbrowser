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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package emailplugin;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * This Class implements the Settings-Tab
 * 
 * @author bodum
 */
public class EMailSettingsTab implements SettingsTab {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(EMailSettingsTab.class);

  /** Settings */
  private Properties mSettings;

  /** Application-Path */
  private JTextField mApplication;

  /** Parameters */
  private JTextField mParameter;

  /** Plugin */
  private EMailPlugin mPlugin;
  
  /**
   * Creates the SettingsTab
   * 
   * @param plugin Plugin
   * @param settings Settings to use
   */
  public EMailSettingsTab(EMailPlugin plugin, Properties settings) {
    mPlugin = plugin;
    mSettings = settings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#createSettingsPanel()
   */
  public JPanel createSettingsPanel() {
    final JPanel configPanel = new JPanel();

    configPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("configuration", "Configuration")));

    FormLayout layout = new FormLayout("3dlu, pref, 3dlu, pref:grow, fill:75dlu, 3dlu, pref, 3dlu",
        "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu");
    configPanel.setLayout(layout);

    CellConstraints cc = new CellConstraints();

    configPanel.add(new JLabel(mLocalizer.msg("Application", "Application") + ":"), cc.xy(2, 2));

    mApplication = new JTextField(mSettings.getProperty("application"));

    configPanel.add(mApplication, cc.xyw(4, 2, 2));

    JButton appFinder = new JButton("...");
    appFinder.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        findApplictation(configPanel);
      }

    });

    configPanel.add(appFinder, cc.xy(7, 2));

    configPanel.add(new JLabel(mLocalizer.msg("Parameter", "Parameter") + ":"), cc.xy(2, 4));

    mParameter = new JTextField(mSettings.getProperty("parameter", "{0}"));

    configPanel.add(mParameter, cc.xyw(4, 4, 4));

    JTextArea ta = UiUtilities.createHelpTextArea(mLocalizer.msg("Desc","Desc"));
    configPanel.add(ta, cc.xyw(2,6,6));
    
    
//    mParamText = new ParamInputField(mSettings.getProperty("paramToUse", EMailPlugin.DEFAULT_PARAMETER));
//    mParamText.setBorder(BorderFactory.createTitledBorder(
//        mLocalizer.msg("createText", "Text to create for each Program")));
    
    JButton extended = new JButton(mLocalizer.msg("extended", "Extended Settings"));
    
    extended.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showExtendedDialog(configPanel);
      }
    });

    configPanel.add(extended, cc.xyw(5, 8, 3));
    
    JPanel panel = new JPanel(new BorderLayout());
    
    panel.add(configPanel, BorderLayout.NORTH);
    
    return panel;
  }
  
  /**
   * Shows the Dialog with the extended Settings
   * @param panel Parent-Panel
   */
  private void showExtendedDialog(JPanel panel) {
    ExtendedDialog dialog;
    
    Window comp = UiUtilities.getBestDialogParent(panel);
    
    if (comp instanceof JFrame) {
      dialog = new ExtendedDialog((JFrame) comp, mSettings);
    } else {
      dialog = new ExtendedDialog((JDialog) comp, mSettings);
    }
    
    UiUtilities.centerAndShow(dialog);
  }
  
  /**
   * Opens a FileChooser and let the User open a File
   */
  private void findApplictation(JPanel panel) {
    JFileChooser chooser = new JFileChooser(new File(mApplication.getText()));
    int returnVal = chooser.showOpenDialog(UiUtilities.getBestDialogParent(panel));
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      mApplication.setText(chooser.getSelectedFile().getAbsolutePath());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#saveSettings()
   */
  public void saveSettings() {
    mSettings.put("application", mApplication.getText());
    mSettings.put("parameter", mParameter.getText());
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#getIcon()
   */
  public Icon getIcon() {
    return mPlugin.createImageIcon("action", "mail-message-new", 16);
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#getTitle()
   */
  public String getTitle() {
    return mLocalizer.msg("name", "Send EMail");
  }
}