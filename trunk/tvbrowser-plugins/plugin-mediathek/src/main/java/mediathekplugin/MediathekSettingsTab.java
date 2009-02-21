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

import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

public final class MediathekSettingsTab implements SettingsTab {

  private static final Localizer localizer = Localizer
      .getLocalizerFor(MediathekSettingsTab.class);
  private Properties settings;
  private JCheckBox autoReadPrograms;

  public JPanel createSettingsPanel() {
    final int currentRow = 1;
    final FormLayout layout = new FormLayout("5dlu, pref, fill:default:grow",
        "");
    final PanelBuilder panelBuilder = new PanelBuilder(layout);
    final CellConstraints cc = new CellConstraints();

    // settings
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));

    // automatic program reading
    autoReadPrograms = new JCheckBox(localizer.msg("readProgramsOnStartup",
        "Automatically read programs from internet on startup"), settings
        .getProperty(IMediathekProperties.readProgramsOnStart, "true").equals(
            "true"));
    panelBuilder.add(autoReadPrograms, cc.xy(2, currentRow));
    return panelBuilder.getPanel();
  }

  public Icon getIcon() {
    return MediathekPlugin.getInstance().getPluginIcon();
  }

  public String getTitle() {
    return localizer.msg("title", "Mediathek");
  }

  public void saveSettings() {
    settings.setProperty(IMediathekProperties.readProgramsOnStart,
        autoReadPrograms.isSelected() ? "true" : "false");
  }

  public MediathekSettingsTab(final Properties settings) {
    this.settings = settings;
  }

}
