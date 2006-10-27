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
 */
package tvbrowser.ui.settings;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The settings for the closing of the main window.
 * 
 * @author René Mach
 *
 */
public class CloseSettingsTab implements SettingsTab {
  private static Localizer mLocalizer = Localizer.getLocalizerFor(CloseSettingsTab.class);
  private JCheckBox mOnlyMinimizeWhenWindowClosingChB;
  
  
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu","pref,5dlu,pref"));
    pb.setDefaultDialogBorder();
    
    String msg = mLocalizer.msg("onlyMinimizeWhenWindowClosing",
    "When closing the main window only minimize TV-Browser, don't quit.");
    
    mOnlyMinimizeWhenWindowClosingChB = new JCheckBox(msg, Settings.propOnlyMinimizeWhenWindowClosing.getBoolean());     

    pb.addSeparator(mLocalizer.msg("closing","Closing"), cc.xyw(1,1,3));
    pb.add(mOnlyMinimizeWhenWindowClosingChB, cc.xy(2,3));
    
    return pb.getPanel();
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    
    return mLocalizer.msg("closing","Closing");
  }

  public void saveSettings() {
    if (mOnlyMinimizeWhenWindowClosingChB != null) {
      boolean checked = mOnlyMinimizeWhenWindowClosingChB.isSelected();
      Settings.propOnlyMinimizeWhenWindowClosing.setBoolean(checked);
    }
  }

}
