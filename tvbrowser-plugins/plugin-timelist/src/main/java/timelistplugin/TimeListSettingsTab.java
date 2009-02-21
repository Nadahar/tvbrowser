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
package timelistplugin;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import util.ui.ImageUtilities;
import util.ui.Localizer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * @author bananeweizen
 *
 */
public final class TimeListSettingsTab implements SettingsTab {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TimeListSettingsTab.class);
  
  /**
   * plugin instance 
   */
  private TimeListPlugin mPlugin;
  
  /**
   * show program description in list view 
   */
  private JCheckBox mDescription;
  
  /**
   * show expired programs in the list view 
   */
  private JCheckBox mExpired;

  public TimeListSettingsTab(TimeListPlugin plugin) {
    mPlugin = plugin;
  }

  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout("5dlu,pref,5dlu,pref:grow",
    "5dlu,pref,3dlu,pref,default:grow");
    CellConstraints cc = new CellConstraints();

    final JPanel configPanel = new JPanel();
    configPanel.setLayout(layout);
    mDescription = new JCheckBox(mLocalizer.msg("showDesc", "Show program description"));
    mDescription.setSelected(mPlugin.isShowDescriptions());
    configPanel.add(mDescription, cc.xy(2, 2));

    mExpired = new JCheckBox(mLocalizer.msg("showExpired", "Show expired programs"));
    mExpired.setSelected(mPlugin.isShowExpired());
    configPanel.add(mExpired, cc.xy(2, 4));

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(configPanel, BorderLayout.WEST);
    return panel;
  }

  public Icon getIcon() {
    return new ImageIcon(ImageUtilities.createImageFromJar("timelistplugin/timelist-16.png", TimeListPlugin.class));
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Time list");
  }

  public void saveSettings() {
    mPlugin.saveSettings(mDescription.isSelected(), mExpired.isSelected());
  }

}