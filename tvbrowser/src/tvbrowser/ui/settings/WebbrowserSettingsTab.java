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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
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

  private JCheckBox mUseWebbrowser;

  private JButton mChooseButton;
  
  /**
   * Creates a new instance of ProxySettingsTab.
   */
  public WebbrowserSettingsTab() {
  }

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new FormLayout("5dlu, 10dlu, pref, 3dlu, pref, fill:3dlu:grow", "pref, 5dlu, pref, 3dlu, pref, 3dlu, pref"));
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("title", "Webbrowser")), cc.xyw(1,1,6));
    
    mSettingsPn.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help", "Help")), cc.xyw(2,3,4));
    
    mUseWebbrowser = new JCheckBox(mLocalizer.msg("userDefinedWebbrowser","user defined webbrowser"));
    mUseWebbrowser.setSelected(Settings.propUserDefinedWebbrowser.getString() != null);

    mUseWebbrowser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateInputFields();
      }

    });
    
    mSettingsPn.add(mUseWebbrowser, cc.xyw(2, 5, 2));
    
    mFileTextField = new JTextField(30);
    mFileTextField.setText(Settings.propUserDefinedWebbrowser.getString());
    mSettingsPn.add(mFileTextField, cc.xy(3, 7));
    
    mChooseButton = new JButton(mLocalizer.msg("choose", "Choose"));
    mChooseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (mFileChooser==null) {
          mFileChooser=new JFileChooser();
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

    updateInputFields();
    
    mSettingsPn.add(mChooseButton, cc.xy(5, 7));
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
    return mLocalizer.msg("title", "Webbrowser");
  }

}