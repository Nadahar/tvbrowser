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

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.Arrays;

import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.ChannelListCellRenderer;
import util.ui.SearchFormSettings;
import util.ui.SearchHelper;
import util.ui.Localizer;
import tvbrowser.core.ChannelList;
import devplugin.PluginManager;

/**
 * A dialog specifically for repetitions. It only shows a simple Input-Form and
 * helps the User to focus on the Task.
 */
public class RepetitionDialog extends JDialog implements WindowClosingIf {
  /** The localizer of this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(RepetitionDialog.class);

  /** Search for this text */
  private JTextField mText;
  /** Search in this channel */
  private JComboBox mChannelChooser;
  /** Search in this timespan */
  private JComboBox mTimeChooser;

  /**
   * Create the dialog
   * @param dialog Parent-Dialog
   */
  public RepetitionDialog(Dialog dialog) {
    super(dialog, true);
    createGui();
  }

  /**
   * Create the dialog
   * @param frame Parent-Frame
   */
  public RepetitionDialog(Frame frame) {
    super(frame, true);
    createGui();
  }

  /**
   * Create the Gui
   */
  private void createGui() {
    setTitle(mLocalizer.msg("title", "Search repetition!"));

    JPanel panel = (JPanel) getContentPane();
    panel.setLayout(new FormLayout("right:pref, 3dlu, fill:pref:grow", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, fill:3dlu:grow, pref"));
    panel.setBorder(Borders.DLU7_BORDER);

    CellConstraints cc = new CellConstraints();

    panel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("searchForRepetition","Search for repetitions")), cc.xyw(1,1,3));

    panel.add(new JLabel(mLocalizer.msg("forProgram","for program:")), cc.xy(1,3));
    mText = new JTextField();
    panel.add(mText, cc.xy(3,3));

    panel.add(new JLabel(mLocalizer.msg("on","on:")), cc.xy(1,5));

    final Vector list = new Vector();
    list.add(mLocalizer.msg("allChannels","Alle Sender"));
    list.addAll(Arrays.asList(ChannelList.getSubscribedChannels()));

    mChannelChooser = new JComboBox(list);
    mChannelChooser.setRenderer(new ChannelListCellRenderer(true, true));

    panel.add(mChannelChooser, cc.xy(3,5));

    panel.add(new JLabel(mLocalizer.msg("when", "when:")), cc.xy(1,7));

    String[] dates = {
        mLocalizer.msg("today", "today"),
        mLocalizer.msg("tomorrow", "tomorrow"),
        mLocalizer.msg("oneWeek", "one week"),
        mLocalizer.msg("twoWeeks", "two weeks"),
        mLocalizer.msg("threeWeeks", "three weeks"),
        mLocalizer.msg("allData", "all data")
    };

    mTimeChooser = new JComboBox(dates);
    mTimeChooser.setSelectedIndex(3);

    panel.add(mTimeChooser, cc.xy(3,7));

    ButtonBarBuilder builder = new ButtonBarBuilder();

    builder.addGlue();

    JButton go = new JButton(mLocalizer.msg("go", "go"));
    go.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent actionEvent) {
        search();
      }
    });

    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent actionEvent) {
        close();
      }
    });

    builder.addGriddedButtons(new JButton[]{go, cancel});

    panel.add(builder.getPanel(), cc.xyw(1,9,3));

    setSize(Sizes.dialogUnitXAsPixel(220, this), Sizes.dialogUnitYAsPixel(125, this));

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
      case 0: days = 0; break;
      case 1: days = 1; break;
      case 2: days = 7; break;
      case 3: days = 14; break;
      case 4: days = 21; break;
      case 5: days = -1; break;
    }

    settings.setNrDays(days);
    settings.setSearchIn(SearchFormSettings.SEARCH_IN_TITLE);
    settings.setSearcherType(PluginManager.SEARCHER_TYPE_KEYWORD);
    settings.setCaseSensitive(false);

    // ToDo: Use Channel-Settings !!

    SearchHelper.search(getParent(), settings, SearchPlugin.getInstance().getProgramPanelSettings());
  }

  /**
   * Set the text for the search
   * @param text text to search
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
