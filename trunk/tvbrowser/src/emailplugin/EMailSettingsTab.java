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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import util.ui.ImageUtilities;
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

  /** The Settings-Panel */
  private JPanel mPanel;

  /**
   * Creates the SettingsTab
   * 
   * @param settings Settings to use
   */
  public EMailSettingsTab(Properties settings) {
    mSettings = settings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#createSettingsPanel()
   */
  public JPanel createSettingsPanel() {
    mPanel = new JPanel();

    mPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("configuration", "Configuration")));

    FormLayout layout = new FormLayout("3dlu, pref, 3dlu, pref:grow, 3dlu, pref, 3dlu",
        "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu");
    mPanel.setLayout(layout);

    CellConstraints cc = new CellConstraints();

    mPanel.add(new JLabel(mLocalizer.msg("Application", "Application") + ":"), cc.xy(2, 2));

    mApplication = new JTextField(mSettings.getProperty("application"));

    mPanel.add(mApplication, cc.xy(4, 2));

    JButton appFinder = new JButton("...");
    appFinder.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        findApplictation();
      }

    });

    mPanel.add(appFinder, cc.xy(6, 2));

    mPanel.add(new JLabel(mLocalizer.msg("Parameter", "Parameter") + ":"), cc.xy(2, 4));

    mParameter = new JTextField(mSettings.getProperty("parameter", "{0}"));

    mPanel.add(mParameter, cc.xyw(4, 4, 3));

    JTextArea ta = new JTextArea(mLocalizer.msg("Desc", "Desc"));
    ta.setWrapStyleWord(true);
    ta.setLineWrap(true);
    ta.setOpaque(false);
    ta.setEditable(false);
    ta.setFocusable(false);

    mPanel.add(ta, cc.xyw(2, 6, 4));

    return mPanel;
  }

  /**
   * Opens a FileChooser and let the User open a File
   */
  public void findApplictation() {
    JFileChooser chooser = new JFileChooser(new File(mSettings.getProperty("application", "")));
    int returnVal = chooser.showOpenDialog(UiUtilities.getBestDialogParent(mPanel));
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
    return new ImageIcon(ImageUtilities.createImageFromJar("emailplugin/email.gif", EMailSettingsTab.class));
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