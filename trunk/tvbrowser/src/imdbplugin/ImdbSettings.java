package imdbplugin;

import devplugin.SettingsTab;
import devplugin.Channel;

import javax.swing.JPanel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JDialog;

import util.ui.Localizer;
import util.ui.ChannelChooserDlg;
import util.ui.UiUtilities;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.factories.DefaultComponentFactory;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import java.awt.Component;

public class ImdbSettings implements SettingsTab {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbSettings.class);
  private ImdbPlugin mImdbPlugin;
  private JFrame mParent;
  private Channel[] mExcludedChannels;

  public ImdbSettings(JFrame parent, ImdbPlugin imdbPlugin) {
    mParent = parent;
    mImdbPlugin = imdbPlugin;
    mExcludedChannels = mImdbPlugin.getExcludedChannels();
  }

  public JPanel createSettingsPanel() {
    JPanel panel = new JPanel(new FormLayout("3dlu, fill:pref:grow, 3dlu, pref, 3dlu", "pref, 3dlu, pref, 3dlu, pref"));

    CellConstraints cc = new CellConstraints();
    int y = 1;

    final JLabel excludedChannels = new JLabel(createExcludeChannelsLabelText());
    panel.add(excludedChannels, cc.xy(2,y));
    JButton channelConfig = new JButton("bearbeiten");
    channelConfig.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ChannelChooserDlg chooser;

        Component parent = UiUtilities.getBestDialogParent(mParent);
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

    JButton update = new JButton(mLocalizer.msg("updateDB", "Update Database"));
    update.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent e) {
        mImdbPlugin.showUpdateDialog();
      }
    });

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
