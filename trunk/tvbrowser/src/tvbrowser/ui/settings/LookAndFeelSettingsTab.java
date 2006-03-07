/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;

import tvbrowser.core.Settings;
import util.ui.LinkButton;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

public class LookAndFeelSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(LookAndFeelSettingsTab.class);

  private JRadioButton mUseSkinLFRb;

  private JRadioButton mUseJavaLFRb;

  private JComboBox mLfComboBox;

  private JPanel mSettingsPn;

  private final JTextField mThemepackTf = new JTextField();

  private JButton mChooseBtn;

  class LookAndFeelObj {
    private UIManager.LookAndFeelInfo info;

    public LookAndFeelObj(UIManager.LookAndFeelInfo info) {
      this.info = info;
    }

    public String toString() {
      return info.getName();
    }

    public String getLFClassName() {
      return info.getClassName();
    }
  }

  private LookAndFeelObj[] getLookAndFeelObjs() {
    UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
    LookAndFeelObj[] result = new LookAndFeelObj[info.length];
    for (int i = 0; i < info.length; i++) {
      result[i] = new LookAndFeelObj(info[i]);
    }

    return result;
  }

  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout("5dlu, right:pref, 3dlu, fill:pref:grow, 3dlu, pref, 5dlu", "");

    CellConstraints cc = new CellConstraints();
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);

    layout.appendRow(new RowSpec("pref"));
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator("Aussehen"), cc.xyw(1, 1, 7));

    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(new JLabel("Theme:"), cc.xy(2, 3));

    LookAndFeelObj[] obj = getLookAndFeelObjs();
    mLfComboBox = new JComboBox(obj);
    String lf = Settings.propLookAndFeel.getString();
    for (int i = 0; i < obj.length; i++) {
      if (obj[i].getLFClassName().equals(lf)) {
        mLfComboBox.setSelectedItem(obj[i]);
      }
    }

    mSettingsPn.add(mLfComboBox, cc.xy(4, 3));
    mSettingsPn.add(new JButton("Config"), cc.xy(6, 3));

    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(new JLabel("Icons:"), cc.xy(2, 5));
    mSettingsPn.add(new JComboBox(), cc.xy(4, 5));

    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));

    mSettingsPn.add(new LinkButton("You can find more Icons on our Web-Page.",
        "http://www.tvbrowser.org/iconthemes.php"), cc.xy(4, 7));

    return mSettingsPn;
  }

  private void setUseSkinLF(boolean b) {
    mLfComboBox.setEnabled(!b);
    mThemepackTf.setEnabled(b);
    mChooseBtn.setEnabled(b);

  }

  public void saveSettings() {
    LookAndFeelObj obj = (LookAndFeelObj) mLfComboBox.getSelectedItem();
    Settings.propLookAndFeel.setString(obj.getLFClassName());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return "Look&Feel";
  }

}