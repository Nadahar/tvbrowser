/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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

package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.browserlauncher.Launch;
import util.misc.OperatingSystem;
import util.ui.LinkButton;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Settings for the Webbrowser
 */
public class WebbrowserSettingsTab implements devplugin.SettingsTab {
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(WebbrowserSettingsTab.class);

  private JPanel mSettingsPn;
  private JFileChooser mFileChooser;
  private JTextField mFileTextField;
  private JTextField mParams;

  private JRadioButton mUseWebbrowser;

  private JButton mChooseButton;
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new FormLayout("5dlu, 10dlu, pref, 3dlu, pref, 3dlu, pref, fill:3dlu:grow, 3dlu", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("browser", "Web browser")), cc.xyw(1,1,9));
    
    JButton testButton = new LinkButton(mLocalizer.msg("testBrowser", "Test Webbrowser"), "http://www.tvbrowser.org", LinkButton.LEFT, false);
    testButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String buffer = Settings.propUserDefinedWebbrowser.getString();
        String bufferParams = Settings.propUserDefinedWebbrowserParams.getString();
        saveSettings();
        Launch.openURL("http://www.tvbrowser.org");
        Settings.propUserDefinedWebbrowser.setString(buffer);
        Settings.propUserDefinedWebbrowserParams.setString(bufferParams);
      }
    });

    mSettingsPn.add(UiUtilities.createHelpTextArea(mLocalizer.msg("help", "Help Text")), cc.xyw(2,3,7));
    
    mSettingsPn.add(testButton, cc.xyw(2, 5, 7));
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("whichBrowser", "which browser")), cc.xyw(2,7,7));

    JRadioButton useDefault = new JRadioButton(mLocalizer.msg("defaultWebbrowser", "Default Webbrowser"));
    useDefault.setSelected(Settings.propUserDefinedWebbrowser.getString() == null);
    useDefault.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateInputFields();
      }
    });
    
    mSettingsPn.add(useDefault, cc.xyw(2, 9, 4));
    
    mUseWebbrowser = new JRadioButton(mLocalizer.msg("userDefinedWebbrowser","user defined webbrowser"));
    mUseWebbrowser.setSelected(Settings.propUserDefinedWebbrowser.getString() != null);

    mUseWebbrowser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateInputFields();
      }
    });

    ButtonGroup group = new ButtonGroup();
    group.add(useDefault);
    group.add(mUseWebbrowser);
    
    mSettingsPn.add(mUseWebbrowser, cc.xyw(2, 11, 7));
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("browserExecutable", "Executable") + ":"), cc.xy(3, 13));


    mFileTextField = new JTextField(30);
    mFileTextField.setText(Settings.propUserDefinedWebbrowser.getString());
    mSettingsPn.add(mFileTextField, cc.xy(5, 13));
    
    mChooseButton = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    mChooseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (mFileChooser==null) {
          mFileChooser=new JFileChooser();

          if (OperatingSystem.isMacOs()) {
            mFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
          } else {
            mFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          }
        }
        
        int retVal = mFileChooser.showOpenDialog(mSettingsPn.getParent());
        if (retVal == JFileChooser.APPROVE_OPTION) {
          File f=mFileChooser.getSelectedFile();
          if (f!=null) {
            mFileTextField.setText(f.getAbsolutePath());
          }
        }
      }
    });
    mSettingsPn.add(mChooseButton, cc.xy(7, 13));

    mSettingsPn.add(new JLabel(mLocalizer.msg("browserParameter", "Parameter") + ":"), cc.xy(3,15));

    mParams = new JTextField();
    mParams.setText(Settings.propUserDefinedWebbrowserParams.getString());
    mSettingsPn.add(mParams, cc.xy(5,15));

    mSettingsPn.add(new JLabel(mLocalizer.msg("browserParameterHelp", "{0} will be replaced by the url.")), cc.xyw(5,17,4));

    updateInputFields();
    
    return mSettingsPn;
  }

  /**
   * Update the State of the Input-Fields
   */
  private void updateInputFields() {
    mFileTextField.setEnabled(mUseWebbrowser.isSelected());
    mChooseButton.setEnabled(mUseWebbrowser.isSelected());
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    String webbrowser;
    if (mUseWebbrowser.isSelected()) {
      webbrowser = mFileTextField.getText();

      String params = mParams.getText().trim();
      if (params.length() == 0) {
        params = "{0}";
      }
      Settings.propUserDefinedWebbrowserParams.setString(params);
    } else {
      webbrowser = null;
    }
    Settings.propUserDefinedWebbrowser.setString(webbrowser);
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "internet-web-browser", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("browser", "Web browser");
  }

}