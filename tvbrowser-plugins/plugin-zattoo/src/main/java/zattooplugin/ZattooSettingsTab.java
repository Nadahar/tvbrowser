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
package zattooplugin;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;

import devplugin.SettingsTab;

public final class ZattooSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ZattooSettingsTab.class);
  private JComboBox mCountry;
  private ZattooSettings mSettings;
  private JRadioButton mRbLocalPlayer;
  private JRadioButton mRbWebPlayer;
  private JRadioButton mRbPrism;

  public ZattooSettingsTab(ZattooSettings settings) {
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final PanelBuilder builder = new PanelBuilder(new FormLayout(
        FormSpecs.RELATED_GAP_COLSPEC.encode() + ","
        + FormSpecs.PREF_COLSPEC.encode() + ","
        + FormSpecs.RELATED_GAP_COLSPEC.encode() + ","
        + FormSpecs.PREF_COLSPEC.encode() + ","
        + FormSpecs.GLUE_COLSPEC.encode(), ""));
    final CellConstraints cc = new CellConstraints();

    final ZattooCountry[] countries = new ZattooCountry[] {
        new ZattooCountry("de", mLocalizer.msg("country_de", "Germany")),
        // new ZattooCountry("at", mLocalizer.msg("country_at", "Austria")),
        new ZattooCountry("ch", mLocalizer.msg("country_ch", "Switzerland")) };

    mCountry = new JComboBox(countries);
    mCountry.setSelectedItem(new ZattooCountry(mSettings.getCountry(), ""));

    builder.appendRow(FormSpecs.LINE_GAP_ROWSPEC);
    builder.appendRow(FormSpecs.PREF_ROWSPEC);
    builder.nextRow();
    builder.add(new JLabel(mLocalizer.msg("country","Country:")), cc.xy(2, builder.getRow()));
    builder.add(mCountry, cc.xy(4, builder.getRow()));
    
    mRbLocalPlayer = new JRadioButton(mLocalizer.msg("localPlayer", "Use local player"), mSettings.getUseLocalPlayer());
    mRbLocalPlayer.setEnabled(ZattooPlugin.canUseLocalPlayer());
    mRbWebPlayer = new JRadioButton(mLocalizer.msg("webPlayer", "Use web player"), mSettings.getUseWebPlayer());
    mRbPrism = new JRadioButton(mLocalizer.msg("prism", "Mozilla Prism"), mSettings.getUsePrismPlayer());
    
    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(mRbLocalPlayer);
    buttonGroup.add(mRbWebPlayer);
    buttonGroup.add(mRbPrism);

    builder.appendRow(FormSpecs.PARAGRAPH_GAP_ROWSPEC);
    builder.appendRow(FormSpecs.PREF_ROWSPEC);
    builder.nextRow(2);
    builder.addSeparator(mLocalizer.msg("player", "Player"));
    
    builder.appendRow(FormSpecs.LINE_GAP_ROWSPEC);
    builder.appendRow(FormSpecs.PREF_ROWSPEC);
    builder.nextRow(2);
    builder.add(mRbLocalPlayer, cc.xyw(2, builder.getRow(), builder.getColumnCount() - 1));
    
    builder.appendRow(FormSpecs.LINE_GAP_ROWSPEC);
    builder.appendRow(FormSpecs.PREF_ROWSPEC);
    builder.nextRow(2);
    builder.add(mRbWebPlayer, cc.xyw(2, builder.getRow(), builder.getColumnCount() - 1));
    
    builder.appendRow(FormSpecs.LINE_GAP_ROWSPEC);
    builder.appendRow(FormSpecs.PREF_ROWSPEC);
    builder.nextRow(2);
    builder.add(mRbPrism, cc.xyw(2, builder.getRow(), builder.getColumnCount() - 1));
    
    return builder.getPanel();
  }

  public void saveSettings() {
    ZattooPlugin.getInstance().changeCountry(((ZattooCountry)mCountry.getSelectedItem()).getCode());
    if (mRbLocalPlayer.isSelected() && ZattooPlugin.canUseLocalPlayer()) {
      mSettings.setUseLocalPlayer();
    }
    else if (mRbPrism.isSelected()) {
      mSettings.setUsePrismPlayer();
    }
    else {
      mSettings.setUseWebPlayer();
    }
  }

  public Icon getIcon() {
    return ZattooPlugin.getInstance().getPluginIcon();
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Zattoo");
  }
}
