/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *     $Date: 2007-03-14 15:13:35 +0100 (Mi, 14 Mrz 2007) $
 *   $Author: ds10 $
 * $Revision: 3224 $
 */
package tvbrowser.extras.searchplugin;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tvbrowser.core.ChannelList;
import tvbrowser.core.Settings;
import util.settings.PluginPictureSettings;
import util.ui.ChannelListCellRenderer;
import util.ui.Localizer;
import util.ui.SearchFormSettings;
import util.ui.SearchHelper;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Channel;
import devplugin.PluginManager;

/**
 * A dialog specifically for repetitions. It only shows a simple Input-Form and
 * helps the User to focus on the Task.
 */
class RepetitionDialog extends JDialog implements WindowClosingIf {
  /**
   * The localizer of this class.
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(RepetitionDialog.class);

  /**
   * Search for this text
   */
  private JTextField mText;
  /**
   * Search in this channel
   */
  private JComboBox mChannelChooser;
  /**
   * Search in this timespan
   */
  private JComboBox mTimeChooser;

  /**
   * Create the dialog
   *
   * @param parent
   *          Parent-Dialog
   */
  RepetitionDialog(Window parent) {
    this(parent, null);
  }

  /**
   * Create the dialog
   *
   * @param parent
   *          Parent-Dialog
   * @param channel
   *          defines the first channel of the channel list
   */
  RepetitionDialog(Window parent, Channel channel) {
    super(parent);
    setModal(true);
    createGui(channel);
  }

  /**
   * Create the Gui
   * @param channel
   *          defines the first channel of the channel list
   */
  private void createGui(Channel channel) {
    setTitle(mLocalizer.msg("title", "Search repetition"));

    JPanel panel = (JPanel) getContentPane();
    panel.setLayout(new FormLayout("pref, 3dlu, 0dlu:grow",
        "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, fill:5dlu:grow, pref"));
    panel.setBorder(Borders.DLU7_BORDER);

    CellConstraints cc = new CellConstraints();

    panel.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("searchForRepetition", "Search for repetitions")), cc
        .xyw(1, 1, 3));

    panel.add(new JLabel(mLocalizer.msg("forProgram", "of:")), cc.xy(1, 3));
    mText = new JTextField();
    panel.add(mText, cc.xy(3, 3));

    panel.add(new JLabel(mLocalizer.msg("on", "on:")), cc.xy(1, 5));

    final Vector<Object> list = new Vector<Object>();
    list.add(mLocalizer.msg("allChannels", "All channels"));
    // We need a modify-able list here
    List<Channel> channelList = new ArrayList<Channel>(Arrays.asList(ChannelList.getSubscribedChannels()));
    if (channel != null) {
      // bring the defined channel on the first position
      list.add(channel);
      channelList.remove(channel);
    }
    list.addAll(channelList);

    mChannelChooser = new JComboBox(list);
    mChannelChooser.setRenderer(new ChannelListCellRenderer(true, true));

    panel.add(mChannelChooser, cc.xy(3, 5));

    panel.add(new JLabel(mLocalizer.msg("when", "when:")), cc.xy(1, 7));

    String[] dates = { Localizer.getLocalization(Localizer.I18N_TODAY),
        Localizer.getLocalization(Localizer.I18N_TOMORROW),
        mLocalizer.msg("oneWeek", "one week"),
        mLocalizer.msg("twoWeeks", "two weeks"),
        mLocalizer.msg("threeWeeks", "three weeks"),
        mLocalizer.msg("allData", "all data") };

    mTimeChooser = new JComboBox(dates);
    mTimeChooser.setSelectedIndex(SearchPlugin.getInstance()
        .getRepetitionTimeSelection());

    panel.add(mTimeChooser, cc.xy(3, 7));

    JButton stdSearch = new JButton(mLocalizer.ellipsisMsg("more", "More"));
    stdSearch.setToolTipText(mLocalizer.msg("standardSearch",
        "Open standard search"));

    stdSearch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
        SearchPlugin.getInstance().openSearchDialog(mText.getText());
      }
    });

    ButtonBarBuilder2 builder = new ButtonBarBuilder2();
    builder.addButton(stdSearch);
    builder.addUnrelatedGap();
    builder.addGlue();

    JButton go = new JButton(mLocalizer.msg("go", "go"));
    go.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        search();
      }
    });

    JButton cancel = new JButton(Localizer
        .getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        close();
      }
    });

    builder.addButton(new JButton[] { go, cancel });
    panel.add(builder.getPanel(), cc.xyw(1, 9, 3));

    Settings.layoutWindow("extras.repetitionDialog", this, new Dimension(Sizes
        .dialogUnitXAsPixel(220, this), Sizes.dialogUnitYAsPixel(125, this)));

    UiUtilities.registerForClosing(this);
    getRootPane().setDefaultButton(go);
  }

  /**
   * do the search
   */
  private void search() {
    setVisible(false);

    SearchFormSettings settings = new SearchFormSettings(mText.getText());

    int days = 1;

    switch (mTimeChooser.getSelectedIndex()) {
    case 0:
      days = 0;
      break;
    case 1:
      days = 1;
      break;
    case 2:
      days = 7;
      break;
    case 3:
      days = 14;
      break;
    case 4:
      days = 21;
      break;
    default:
      days = -1;
    }

    SearchPlugin.getInstance().setRepetitionTimeSelection(
        mTimeChooser.getSelectedIndex());

    settings.setNrDays(days);
    settings.setSearchIn(SearchFormSettings.SEARCH_IN_TITLE);
    settings.setSearcherType(PluginManager.SEARCHER_TYPE_EXACTLY);
    settings.setCaseSensitive(false);

    if (mChannelChooser.getSelectedIndex() > 0) {
      settings.setChannels(new Channel[] { (Channel) mChannelChooser
          .getSelectedItem() });
    }

    SearchHelper.search(getParent(), new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), settings, true);
  }

  /**
   * Set the text for the search
   *
   * @param text
   *          text to search
   */
  public void setPatternText(String text) {
    mText.setText(text);
  }

  /**
   * Close the dialog
   */
  public void close() {
    setVisible(false);
  }
}
