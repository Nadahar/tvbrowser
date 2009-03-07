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
package widgetplugin;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

public class WidgetSettingsTab implements SettingsTab {

	private static final Localizer mLocalizer = Localizer
			.getLocalizerFor(WidgetSettingsTab.class);

	private WidgetPlugin mPlugin;

	private JSpinner mSpinner;

	private WidgetSettings mSettings;

	private JCheckBox mRefresh;

	protected WidgetSettingsTab(final WidgetPlugin plugin,
      final WidgetSettings settings) {
		super();
		mPlugin = plugin;
		mSettings = settings;
	}

	public JPanel createSettingsPanel() {
		int currentRow = 1;
		final FormLayout layout = new FormLayout(
				"5dlu, pref, 3dlu, pref, fill:default:grow",
				"");
		final PanelBuilder panelBuilder = new PanelBuilder(layout);
    final CellConstraints cc = new CellConstraints();

		// settings
		layout.appendRow(RowSpec.decode("pref"));
		layout.appendRow(RowSpec.decode("5dlu"));
		layout.appendRow(RowSpec.decode("pref"));
		layout.appendRow(RowSpec.decode("5dlu"));

		// port
    final JLabel label = new JLabel(mLocalizer.msg("portNumber", "Port number"));
		panelBuilder.add(label, cc.xy(2, currentRow));

		final SpinnerNumberModel model = new SpinnerNumberModel(34567, 1, 65535, 1);
		mSpinner = new JSpinner(model);
		mSpinner.setValue(mSettings.getPortNumber());
		panelBuilder.add(mSpinner, cc.xy(4, currentRow));

		// refresh
		mRefresh = new JCheckBox(mLocalizer.msg("refresh", "Automatic refresh"));
		mRefresh.setSelected(mSettings.getRefresh());
		panelBuilder.add(mRefresh, cc.xyw(2, (currentRow += 2), 4));
    /*
     * // layout settings layout.appendRow(RowSpec.decode("pref"));
     * layout.appendRow(RowSpec.decode("2dlu"));
     * layout.appendRow(RowSpec.decode("fill:default:grow"));
     * 
     * // layout panelBuilder.addSeparator(mLocalizer.msg("layout", "Layout"),
     * cc.xyw(1, (currentRow += 2), 5));
     */
		return panelBuilder.getPanel();
	}

	public Icon getIcon() {
		return WidgetPlugin.getInstance().getPluginIcon();
	}

	public String getTitle() {
		return mLocalizer.msg("title", "Widgets");
	}

	public void saveSettings() {
	  mSettings.setPortNumber((Integer) mSpinner.getValue());
    mSettings.setRefresh(mRefresh.isSelected());
		mPlugin.storeSettings();
		mPlugin.restartServer();
	}

}