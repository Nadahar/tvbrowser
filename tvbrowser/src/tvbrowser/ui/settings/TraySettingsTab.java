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

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.Borders;

import tvbrowser.core.Settings;
import util.ui.TabLayout;
import devplugin.SettingsTab;

/**
 * Settings for the Tray-Icon
 *  
 * @author bodum
 */
public class TraySettingsTab implements SettingsTab {
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(TraySettingsTab.class);
  /** Checkboxes */
  private JCheckBox mOnlyMinimizeWhenWindowClosingChB, mMinimizeToTrayChb, mSingeClickTrayChb;

  /**
   * Create the Settings-Dialog
   */
  public JPanel createSettingsPanel() {
    JPanel trayPn = new JPanel(new TabLayout(1));

    trayPn.setBorder(Borders.DLU4_BORDER);
    
    String msg = mLocalizer.msg("minimizeToTray", "Minimize to Tray");
    boolean checked = Settings.propMinimizeToTray.getBoolean();
    mMinimizeToTrayChb = new JCheckBox(msg, checked);
    trayPn.add(mMinimizeToTrayChb);
    
    msg = mLocalizer.msg("onlyMinimizeWhenWindowClosing", "When closing the main window only minimize TV-Browser, don't quit.");
    checked = Settings.propOnlyMinimizeWhenWindowClosing.getBoolean();
    mOnlyMinimizeWhenWindowClosingChB = new JCheckBox(msg, checked);
    trayPn.add(mOnlyMinimizeWhenWindowClosingChB);

    checked = Settings.propUseSingeClickInTray.getBoolean();
    mSingeClickTrayChb = new JCheckBox(mLocalizer.msg("useSingleClick","Use single Click in Tray to hide and show window"), checked);
    trayPn.add(mSingeClickTrayChb);
    
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(trayPn, BorderLayout.NORTH);
    return panel;
  }

  /**
   * Save the Settings-Dialog
   */
  public void saveSettings() {
    if (mOnlyMinimizeWhenWindowClosingChB != null) {
      boolean checked = mOnlyMinimizeWhenWindowClosingChB.isSelected();
      Settings.propOnlyMinimizeWhenWindowClosing.setBoolean(checked);
    }

    if (mMinimizeToTrayChb != null) {
      boolean checked = mMinimizeToTrayChb.isSelected();
      Settings.propMinimizeToTray.setBoolean(checked);
    }

    if (mSingeClickTrayChb != null) {
      boolean checked = mSingeClickTrayChb.isSelected();
      Settings.propUseSingeClickInTray.setBoolean(checked);
    }
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("tray","Tray");
  }

}
