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

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import tvbrowser.core.Settings;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class StartupSettingsTab implements devplugin.SettingsTab {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(StartupSettingsTab.class);

  private JPanel mSettingsPn;

  private JCheckBox mShowSplashChB, mMinimizeAfterStartUpChB;

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new FormLayout("5dlu, pref, 3dlu, pref, fill:3dlu:grow, 3dlu", "pref, 5dlu, pref, 3dlu, pref"));
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("title", "Startup")), cc.xyw(1,1,5));

    mMinimizeAfterStartUpChB = new JCheckBox(mLocalizer.msg("minimizeAfterStartup",
        "Minimize main window after start up"), Settings.propMinimizeAfterStartup.getBoolean());
    mSettingsPn.add(mMinimizeAfterStartUpChB, cc.xy(2,3));

    mShowSplashChB = new JCheckBox(mLocalizer.msg("showSplashScreen", "Show splash screen during start up"), Settings.propSplashShow.getBoolean());
    mSettingsPn.add(mShowSplashChB, cc.xy(2,5));
    
    
    return mSettingsPn;
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.propMinimizeAfterStartup.setBoolean(mMinimizeAfterStartUpChB.isSelected());
    Settings.propSplashShow.setBoolean(mShowSplashChB.isSelected());
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("title", "Startup");
  }
}