package zattooplugin;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.Localizer;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

public final class ZattooSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ZattooSettingsTab.class);
  private JComboBox mCountry;

  public JPanel createSettingsPanel() {
    final JPanel panel = new JPanel(new FormLayout("pref, 3dlu, pref", "pref"));
    panel.setBorder(Borders.DLU4_BORDER);

    final CellConstraints cc = new CellConstraints();

    panel.add(new JLabel(mLocalizer.msg("country","Country:")), cc.xy(1,1));

    final ZattooCountry[] countries = new ZattooCountry[] {
        new ZattooCountry("de", mLocalizer.msg("country_de", "Germany")),
        // new ZattooCountry("at", mLocalizer.msg("country_at", "Austria")),
        new ZattooCountry("ch", mLocalizer.msg("country_ch", "Switzerland")) };

    mCountry = new JComboBox(countries);
    mCountry.setSelectedItem(new ZattooCountry(ZattooPlugin.getInstance().getCurrentCountry(), ""));

    panel.add(mCountry, cc.xy(3,1));

    return panel;
  }

  public void saveSettings() {
    ZattooPlugin.getInstance().changeCountry(((ZattooCountry)mCountry.getSelectedItem()).getCode());
  }

  public Icon getIcon() {
    return ZattooPlugin.getInstance().getPluginIcon();
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Zattoo");
  }
}
