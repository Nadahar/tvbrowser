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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import util.ui.ChannelChooserDlg;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
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
  private ImdbSettings mSettings;
  private JLabel mLabelUpdate;
  private JLabel mLabelSize;
  private JLabel mLabelRatings;
  private JSpinner mMinRating;

  public ImdbSettingsTab(final JFrame parent, final ImdbPlugin imdbPlugin, final ImdbSettings settings) {
    mParent = parent;
    mImdbPlugin = imdbPlugin;
    mExcludedChannels = mImdbPlugin.getExcludedChannels();
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final PanelBuilder panel = new PanelBuilder(new FormLayout(
        "3dlu, fill:pref:grow, 3dlu, pref, 3dlu",
        "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));

    final CellConstraints cc = new CellConstraints();

    final JLabel excludedChannels = new JLabel(createExcludeChannelsLabelText());
    panel.add(excludedChannels, cc.xy(2, panel.getRow()));
    final JButton channelConfig = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT));
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

    panel.add(channelConfig, cc.xy(4,panel.getRow()));
    panel.nextRow(2);
    
    panel.add(new JLabel(mLocalizer.msg("minimumRating","Minimum rating to show in plugin tree")), cc.xy(2, panel.getRow()));
    mMinRating = new JSpinner(new SpinnerNumberModel(mSettings.getMinimumRating() / 10.0, 0.0, 10.0, 0.1));
    panel.add(mMinRating, cc.xy(4, panel.getRow()));
    panel.nextRow(2);

    panel.addSeparator(mLocalizer.msg("titleDatabase", "Database"), cc.xyw(1, panel.getRow(), 5));
    panel.nextRow(2);
    
    panel.add(new JLabel(mLocalizer.msg("lastUpdate", "Last update")), cc.xy(2, panel.getRow()));
    mLabelUpdate = new JLabel();
    panel.add(mLabelUpdate, cc.xy(4, panel.getRow()));
    panel.nextRow(2);

    panel.add(new JLabel(mLocalizer.msg("movies", "Movies")), cc.xy(2, panel.getRow()));
    mLabelRatings = new JLabel();
    panel.add(mLabelRatings, cc.xy(4, panel.getRow()));
    panel.nextRow(2);

    panel.add(new JLabel(mLocalizer.msg("size", "Size")), cc.xy(2, panel.getRow()));
    mLabelSize = new JLabel();
    panel.add(mLabelSize, cc.xy(4, panel.getRow()));
    panel.nextRow(2);
    
    updateStatistics();

    final JButton update = new JButton(mLocalizer.msg("updateDB",
        "Update Database"));
    update.addActionListener(new ActionListener(){

      public void actionPerformed(final ActionEvent e) {
        mImdbPlugin.showUpdateDialog();
        updateStatistics();
      }
    });

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttons.add(update);

    panel.add(buttons,cc.xyw(2,panel.getRow(), 3));
    panel.nextRow(2);

    return panel.getPanel();
  }

  protected void updateStatistics() {
    mLabelUpdate.setText(mSettings.getUpdateDate());
    mLabelRatings.setText(mSettings.getNumberOfMovies());
    mLabelSize.setText(ImdbPlugin.getInstance().getDatabaseSizeMB() + " MB");
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
    mSettings.setMinimumRating((int) (Math.round((Double)mMinRating.getValue() * 10.0)));
    mImdbPlugin.updateCurrentDateAndClearCache();
  }

  public Icon getIcon() {
    return new ImdbIcon(new ImdbRating(75, 100, "", ""));
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Imdb Plugin");
  }
}
