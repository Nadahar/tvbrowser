/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.misc.JavaVersion;
import util.misc.OperatingSystem;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The base settings for the tray.
 *
 * @author René Mach
 *
 */
public class TrayBaseSettingsTab implements SettingsTab {

  protected static final util.ui.Localizer mLocalizer = util.ui.Localizer
  .getLocalizerFor(TrayBaseSettingsTab.class);

  private JCheckBox mTrayIsEnabled, mMinimizeToTrayChb, mNowOnRestore, mTrayIsAnialiasing;
  private boolean mOldState;
  private static boolean mIsEnabled = Settings.propTrayIsEnabled.getBoolean();
  private JRadioButton mFilterAll,mNoMarkedFiltering,mNoFiltering;

  public JPanel createSettingsPanel() {

    final PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu, pref:grow, 5dlu",
        "pref, 5dlu, pref, pref, pref, pref, pref, 10dlu, pref, 5dlu, pref, pref, pref"));
    builder.setDefaultDialogBorder();
    CellConstraints cc = new CellConstraints();

    String msg = mLocalizer.msg("trayIsEnabled", "Tray activated");
    mOldState = Settings.propTrayIsEnabled.getBoolean();
    mTrayIsEnabled = new JCheckBox(msg, mOldState);

    msg = mLocalizer.msg("minimizeToTray", "Minimize to Tray");
    boolean checked = Settings.propTrayMinimizeTo.getBoolean();
    mMinimizeToTrayChb = new JCheckBox(msg, checked && mOldState);
    mMinimizeToTrayChb.setEnabled(mTrayIsEnabled.isSelected());

    msg = mLocalizer.msg("nowOnDeIconify", "Jump to now when restoring application");
    checked = Settings.propNowOnRestore.getBoolean();
    mNowOnRestore = new JCheckBox(msg, checked);

    msg = mLocalizer.msg("trayAntialiasing", "Antialiasing enabled");
    checked = Settings.propTrayIsAntialiasing.getBoolean();
    mTrayIsAnialiasing = new JCheckBox(msg, checked);
    
    if(System.getProperty("os.name").toLowerCase().startsWith("linux") && (JavaVersion.getVersion() < JavaVersion.VERSION_1_6 || OperatingSystem.isKDE())) {
      mMinimizeToTrayChb.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if(mMinimizeToTrayChb.isSelected()) {
            JOptionPane.showMessageDialog(builder.getPanel(),mLocalizer.msg("minimizeToTrayWarning","This function might not work as expected on Unix systems like KDE or Gnome.\nSo it's recommended not to select this checkbox."),mLocalizer.msg("warning","Warning"), JOptionPane.WARNING_MESSAGE);
          }
        }
    });
    }
    
    //filter settings
    ButtonGroup filter = new ButtonGroup();
    
    msg = mLocalizer.msg("trayFilterAll", "Filter all programs");
    mFilterAll = new JRadioButton(msg);

    msg = mLocalizer.msg("trayFilterNotMarked", "Filter programs, if not marked");
    checked = Settings.propTrayFilterNotMarked.getBoolean();
    mNoMarkedFiltering = new JRadioButton(msg, checked);

    msg = mLocalizer.msg("trayFilterNot", "Don't filter programs");
    checked = Settings.propTrayFilterNot.getBoolean();
    mNoFiltering = new JRadioButton(msg, checked);
    
    if(!mNoFiltering.isSelected() && !mNoMarkedFiltering.isSelected()) {
      mFilterAll.setSelected(true);
    }
    
    filter.add(mFilterAll);
    filter.add(mNoMarkedFiltering);
    filter.add(mNoFiltering);

    //create panel
    builder.addSeparator(mLocalizer.msg("basics", "Basic settings"), cc.xyw(1,1,3));
    builder.add(mTrayIsEnabled, cc.xy(2,3));
    builder.add(mTrayIsAnialiasing, cc.xy(2,4));
    builder.add(mMinimizeToTrayChb, cc.xy(2,5));
    builder.add(mNowOnRestore, cc.xy(2,6));
    
    builder.addSeparator(mLocalizer.msg("filter", "Filter settings"), cc.xyw(1,9,3));
    builder.add(mFilterAll, cc.xy(2,11));
    builder.add(mNoMarkedFiltering, cc.xy(2,12));
    builder.add(mNoFiltering, cc.xy(2,13));

    mTrayIsEnabled.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mIsEnabled = mTrayIsEnabled.isSelected();
        TrayImportantSettingsTab.setTrayIsEnabled(mIsEnabled);
        TrayNowSettingsTab.setTrayIsEnabled(mIsEnabled);
        TrayOnTimeSettingsTab.setTrayIsEnabled(mIsEnabled);
        TraySoonSettingsTab.setTrayIsEnabled(mIsEnabled);
        TrayProgramsChannelsSettingsTab.setTrayIsEnabled(mIsEnabled);
        mMinimizeToTrayChb.setEnabled(mTrayIsEnabled.isSelected());
        mNowOnRestore.setEnabled(mTrayIsEnabled.isSelected());
        mTrayIsAnialiasing.setEnabled(mTrayIsEnabled.isSelected());
        mFilterAll.setEnabled(mTrayIsEnabled.isSelected());
        mNoMarkedFiltering.setEnabled(mTrayIsEnabled.isSelected());
        mNoFiltering.setEnabled(mTrayIsEnabled.isSelected());
      }
    });

    return builder.getPanel();
  }

  public void saveSettings() {
    if (mTrayIsEnabled != null) {
      Settings.propTrayIsEnabled.setBoolean(mTrayIsEnabled.isSelected());
      if(mTrayIsEnabled.isSelected() && !mOldState) {
        TVBrowser.loadTray();
      } else if(!mTrayIsEnabled.isSelected() && mOldState) {
        TVBrowser.removeTray();
      }
    }
    if (mMinimizeToTrayChb != null) {
      boolean checked = mMinimizeToTrayChb.isSelected() && mTrayIsEnabled.isSelected();
      Settings.propTrayMinimizeTo.setBoolean(checked);
    }
    Settings.propNowOnRestore.setBoolean(mNowOnRestore.isSelected());
    Settings.propTrayIsAntialiasing.setBoolean(mTrayIsAnialiasing.isSelected());
    Settings.propTrayFilterNotMarked.setBoolean(mNoMarkedFiltering.isSelected());
    Settings.propTrayFilterNot.setBoolean(mNoFiltering.isSelected());
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions", "document-properties", 16);
  }

  public String getTitle() {
    return mLocalizer.msg("title","Tray settings");
  }

  protected static boolean isTrayEnabled() {
    return mIsEnabled;
  }
}
