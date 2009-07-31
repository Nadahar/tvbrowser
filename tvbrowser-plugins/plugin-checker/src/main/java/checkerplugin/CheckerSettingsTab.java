/*
 * Copyright Michael Keppler
 * 
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
package checkerplugin;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * @author bananeweizen
 *
 */
public class CheckerSettingsTab implements SettingsTab {

  private JCheckBox mAutoStart;
  private CheckerSettings mSettings;

  public CheckerSettingsTab(final CheckerSettings settings) {
    mSettings = settings;
  }

  @Override
  public JPanel createSettingsPanel() {
    JPanel panel = new JPanel(new FormLayout(FormFactory.RELATED_GAP_COLSPEC.encode() + "," + FormFactory.PREF_COLSPEC.encode(), "pref"));
    mAutoStart = new JCheckBox(CheckerPlugin.mLocalizer.msg("autostart", "Run checks at startup"));
    mAutoStart.setSelected(mSettings.getAutostart());
    panel.add(mAutoStart, new CellConstraints().xy(2, 1));
    return panel;
  }

  @Override
  public Icon getIcon() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTitle() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void saveSettings() {
    mSettings.setAutostart(mAutoStart.isSelected());
  }

}
