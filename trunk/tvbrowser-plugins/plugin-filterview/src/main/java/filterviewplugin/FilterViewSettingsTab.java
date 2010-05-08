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
package filterviewplugin;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.customizableitems.SelectableItemList;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;

import devplugin.SettingsTab;

class FilterViewSettingsTab implements SettingsTab {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FilterViewSettingsTab.class);
  private FilterViewSettings mSettings;
  private SelectableItemList mFilterList;
  private JSpinner mSpinner;

  public FilterViewSettingsTab(final FilterViewSettings settings) {
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final EnhancedPanelBuilder panelBuilder = new EnhancedPanelBuilder(FormFactory.RELATED_GAP_COLSPEC.encode() + ','
        + FormFactory.PREF_COLSPEC.encode() + ',' + FormFactory.RELATED_GAP_COLSPEC.encode() + ','
        + FormFactory.PREF_COLSPEC.encode() + ", fill:default:grow");
    final CellConstraints cc = new CellConstraints();

    final JLabel label = new JLabel(mLocalizer
        .msg("daysToShow", "Days to show"));

    panelBuilder.addRow();
    panelBuilder.add(label, cc.xy(2, panelBuilder.getRow()));

    final SpinnerNumberModel model = new SpinnerNumberModel(3, 1, 7, 1);
    mSpinner = new JSpinner(model);
    mSpinner.setValue(mSettings.getDays());
    panelBuilder.add(mSpinner, cc.xy(4, panelBuilder.getRow()));

    panelBuilder.addParagraph(mLocalizer.msg("filters", "Filters to show"));

    mFilterList = new SelectableItemList(mSettings.getActiveFilterNames(), mSettings.getAvailableFilterNames());
    panelBuilder.addGrowingRow();
    panelBuilder.add(mFilterList, cc.xyw(2, panelBuilder.getRow(), panelBuilder.getColumnCount() - 1));

    return panelBuilder.getPanel();
  }

  public Icon getIcon() {
    return FilterViewPlugin.getPluginIcon();
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Filter view");
  }

  public void saveSettings() {
    mSettings.setActiveFilterNames(mFilterList.getSelection());
    mSettings.setDays((Integer) mSpinner.getValue());
  }

}
