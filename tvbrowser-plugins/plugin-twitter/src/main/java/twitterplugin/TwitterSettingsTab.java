/*
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
package twitterplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.paramhandler.ParamInputField;
import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Plugin;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;

public final class TwitterSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterSettingsTab.class);
  private boolean mUserStored = false;
  private ParamInputField mFormat;
  private TwitterSettings mSettings;

  public TwitterSettingsTab(final TwitterSettings settings) {
    mSettings = settings;
    mUserStored = mSettings.getStorePassword();
  }

  public JPanel createSettingsPanel() {
    final FormLayout layout = new FormLayout("3dlu,pref,fill:min:grow,3dlu");
    final CellConstraints cc = new CellConstraints();

    final PanelBuilder panel = new PanelBuilder(layout);

    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    String auth = mLocalizer.msg("auth.notDefined", "Not stored");
    boolean enableDelete = false;
    if (mSettings.getUseOAuth()) {
      if (mSettings.getAccessToken() != null) {
        auth = mLocalizer.msg("auth.oauth", "OAuth access token stored");
        enableDelete = true;
      }
    }
    else {
      if (mUserStored) {
        auth = mLocalizer.msg("auth.user", "Password stored for user {0}", mSettings.getUsername());
        enableDelete = true;
      }
    }

    final JLabel user = new JLabel(mLocalizer.msg("auth", "Authentication") + ": " + auth);
    panel.add(user, cc.xyw(2, panel.getRowCount(), panel.getColumnCount() - 1));

    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    final JButton delete = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE), Plugin.getPluginManager().getIconFromTheme(TwitterPlugin.getInstance(),
        new ThemeIcon("actions", "edit-delete", 16)));

    delete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mUserStored = false;
        user.setText(mLocalizer.msg("auth.notDefined", "Not stored"));
        delete.setEnabled(false);
      }
    });

    delete.setEnabled(enableDelete);

    panel.add(delete, cc.xy(2, panel.getRowCount()));

    layout.appendRow(RowSpec.decode("10dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    panel.addSeparator(mLocalizer.msg("format", "Twitter Format"), cc.xyw(1, panel.getRowCount(), panel.getColumnCount()));

    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("fill:min:grow"));

    mFormat = new ParamInputField(mSettings.getFormat());

    panel.add(mFormat, cc.xyw(2, panel.getRowCount(), panel.getColumnCount() - 2));

    return panel.getPanel();
  }

  public void saveSettings() {
    if (!mUserStored) {
      mSettings.clearAuthentication();
    }
    mSettings.setFormat(mFormat.getText());
  }

  public Icon getIcon() {
    return TwitterPlugin.getInstance().getPluginIcon();
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Title");
  }
}
