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
package mediathekplugin;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;

import devplugin.SettingsTab;

public final class MediathekSettingsTab implements SettingsTab {

  private static final Localizer localizer = Localizer
      .getLocalizerFor(MediathekSettingsTab.class);
  private MediathekSettings mSettings;
  private JTextField mPath;

  public JPanel createSettingsPanel() {
    final CellConstraints cc = new CellConstraints();
    EnhancedPanelBuilder panelBuilder = new EnhancedPanelBuilder("5dlu, pref, 3dlu, pref, fill:default:grow");

    panelBuilder.addRow();
    JEditorPane help = UiUtilities.createHtmlHelpTextArea(localizer.msg("help", "The <a href=\"{0}\">Mediathek</a> application needs to be installed.", "http://zdfmediathk.sourceforge.net/"));
    panelBuilder.add(help, cc.xyw(2, panelBuilder.getRowCount(), 4));

    panelBuilder.addRow();
    JLabel label = new JLabel(localizer.msg("path", "Mediathek installation path"));
    panelBuilder.add(label, cc.xy(2, panelBuilder.getRowCount()));

    mPath = new JTextField(mSettings.getMediathekPath());
    panelBuilder.add(mPath, cc.xyw(4, panelBuilder.getRowCount(), 2));
    return panelBuilder.getPanel();
  }

  public Icon getIcon() {
    return MediathekPlugin.getInstance().getPluginIcon();
  }

  public String getTitle() {
    return localizer.msg("title", "Mediathek");
  }

  public void saveSettings() {
    mSettings.setMediathekPath(mPath.getText().trim());
    MediathekPlugin.getInstance().readMediathekContents();
  }

  public MediathekSettingsTab(final MediathekSettings settings) {
    this.mSettings = settings;
  }
}
