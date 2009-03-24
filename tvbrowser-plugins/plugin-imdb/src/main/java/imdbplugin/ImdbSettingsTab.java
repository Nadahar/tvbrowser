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
package imdbplugin;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.ChannelChooserDlg;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.SettingsTab;

public final class ImdbSettingsTab implements SettingsTab {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbSettingsTab.class);
  private ImdbPlugin mImdbPlugin;
  private JFrame mParent;
  private Channel[] mExcludedChannels;

  public ImdbSettingsTab(final JFrame parent, final ImdbPlugin imdbPlugin) {
    mParent = parent;
    mImdbPlugin = imdbPlugin;
    mExcludedChannels = mImdbPlugin.getExcludedChannels();
  }

  public JPanel createSettingsPanel() {
    final JPanel panel = new JPanel(new FormLayout(
        "3dlu, fill:pref:grow, 3dlu, pref, 3dlu",
        "pref, 3dlu, pref, 3dlu, pref"));

    final CellConstraints cc = new CellConstraints();
    int y = 1;

    final JLabel excludedChannels = new JLabel(createExcludeChannelsLabelText());
    panel.add(excludedChannels, cc.xy(2,y));
    final JButton channelConfig = new JButton("bearbeiten");
    channelConfig.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        ChannelChooserDlg chooser;

        final Component parent = UiUtilities.getBestDialogParent(mParent);
        if (parent instanceof JFrame) {
          chooser = new ChannelChooserDlg((JFrame) parent, mExcludedChannels, null, ChannelChooserDlg.SELECTABLE_ITEM_LIST);
        } else {
          chooser = new ChannelChooserDlg((JDialog) parent, mExcludedChannels, null, ChannelChooserDlg.SELECTABLE_ITEM_LIST);
        }
        UiUtilities.centerAndShow(chooser);

        mExcludedChannels = chooser.getChannels();
        excludedChannels.setText(createExcludeChannelsLabelText());
      }
    });

    panel.add(channelConfig, cc.xy(4,y));
    y += 2;

    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("titleDatabase", "Database")), cc.xyw(1, y, 5));
    y += 2;

    final JButton update = new JButton(mLocalizer.msg("updateDB",
        "Update Database"));
    update.addActionListener(new ActionListener(){

      public void actionPerformed(final ActionEvent e) {
        mImdbPlugin.showUpdateDialog();
      }
    });

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttons.add(update);

    panel.add(buttons,cc.xyw(2,y, 3));
    y += 2;

    return panel;
  }

  private String createExcludeChannelsLabelText() {
    String channels = mLocalizer.msg("noChannel","keine");

    if (mExcludedChannels.length == 1) {
      channels = mExcludedChannels[0].getName();
    } else if (mExcludedChannels.length == 2) {
      channels = mExcludedChannels[0].getName() + ", " + mExcludedChannels[1].getName();
    } else if (mExcludedChannels.length > 2) {
      channels = mExcludedChannels[0].getName() + ", " + mExcludedChannels[1].getName() + " ( " + mLocalizer.msg("moreChannels", "{0} more", mExcludedChannels.length -2) + ")";
    }

    return "<html><body><strong>" + mLocalizer.msg("excludedChannels", "Excluded channels:") + "</strong> " + channels +"</body></html>";
  }

  public void saveSettings() {
    mImdbPlugin.setExcludedChannels(mExcludedChannels);
    mImdbPlugin.updateCurrentDateAndClearCache();
  }

  public Icon getIcon() {
    return new ImdbIcon(new ImdbRating(75, 100, "", ""));
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Imdb Plugin");
  }
}
