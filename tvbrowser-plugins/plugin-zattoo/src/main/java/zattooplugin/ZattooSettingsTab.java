package zattooplugin;

import devplugin.SettingsTab;

import javax.swing.JPanel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JComboBox;

import util.ui.Localizer;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.factories.Borders;

import java.util.Vector;

public class ZattooSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ZattooSettingsTab.class);
  private JComboBox mCountry;

  public JPanel createSettingsPanel() {
    final JPanel panel = new JPanel(new FormLayout("pref, 3dlu, pref", "pref"));
    panel.setBorder(Borders.DLU4_BORDER);

    final CellConstraints cc = new CellConstraints();

    panel.add(new JLabel(mLocalizer.msg("country","Country:")), cc.xy(1,1));

    Vector<ZattooCountry> data = new Vector<ZattooCountry>();

    data.add(new ZattooCountry("de", mLocalizer.msg("country_de", "Germany")));
    data.add(new ZattooCountry("at", mLocalizer.msg("country_at", "Austria")));
    data.add(new ZattooCountry("ch", mLocalizer.msg("country_ch", "Switzerland")));
    data.add(new ZattooCountry("fr", mLocalizer.msg("country_fr", "France")));

    mCountry = new JComboBox(data);
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
