/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.OrderChooser;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.SettingsTab;

/**
 * Settings for the Tray-Icon
 * 
 * @author bodum
 */
public class TraySettingsTab implements SettingsTab {
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TraySettingsTab.class);
  /** Checkboxes */
  private JCheckBox mOnlyMinimizeWhenWindowClosingChB, mMinimizeToTrayChb,
      mShowNowRunningChb, mShowImportantChb, mShowNowRunningTimeChb,
      mShowImportantTimeChb, mShowChannelNameChb, mShowChannelIconChb,
      mShowChannelTooltipChb, mShowSoonChb;

  private JRadioButton mShowNowRunningNotSubChb, mShowNowRunningSubChb,
      mShowImportantNotSubChb, mShowImportantSubChb;

  private JButton mAdditional;

  private JSpinner mImportantSize, mImportantHours;
  private JLabel mChannelLabel, mSizeLabel/* , mTimeLabel1, mTimeLabel2 */;
  private OrderChooser mChannelOCh;

  /**
   * Create the Settings-Dialog
   */
  public JPanel createSettingsPanel() {
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu, 10dlu, pref:grow, 25dlu, pref, 5dlu",

        "pref, 5dlu, pref, pref, 10dlu, pref, 5dlu, pref, pref, pref, "
            + "pref, pref, pref, 10dlu, pref, pref, 5dlu, pref, 10dlu, pref"));
    builder.setDefaultDialogBorder();
    CellConstraints cc = new CellConstraints();

    String msg = mLocalizer.msg("minimizeToTray", "Minimize to Tray");
    boolean checked = Settings.propMinimizeToTray.getBoolean();
    mMinimizeToTrayChb = new JCheckBox(msg, checked);

    msg = mLocalizer.msg("onlyMinimizeWhenWindowClosing",
        "When closing the main window only minimize TV-Browser, don't quit.");
    checked = Settings.propOnlyMinimizeWhenWindowClosing.getBoolean();
    mOnlyMinimizeWhenWindowClosingChB = new JCheckBox(msg, checked);

    checked = Settings.propProgramsInTrayContainsChannel.getBoolean();
    mShowChannelNameChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showChannelName", "Show channel name"), checked);

    checked = Settings.propProgramsInTrayContainsChannelIcon.getBoolean();
    mShowChannelIconChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showChannelIcons", "Show channel icon"), checked);

    checked = Settings.propShowImportantProgramsInTray.getBoolean();
    mShowImportantChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showImportant", "Show important programs"), checked);
    mShowImportantChb
        .setToolTipText(mLocalizer
            .msg("programShowing.toolTipImportant",
                "<html>Important programs are all marked<br>programs in the time range.<html>"));

    checked = Settings.propShowSoonProgramsInTray.getBoolean();
    mShowSoonChb = new JCheckBox(mLocalizer.msg("programShowing.showSoon",
        "Program showing enabled"), checked);

    mSizeLabel = new JLabel(mLocalizer.msg(
        "programShowing.importantMaxPrograms", "important programs to show"));
    mImportantSize = new JSpinner(new SpinnerNumberModel(
        Settings.propImportantProgramsInTraySize.getInt(), 1, 10, 1));

    PanelBuilder b2 = new PanelBuilder(new FormLayout("pref,3dlu,pref", "pref"));

    b2.add(mImportantSize, cc.xy(1, 1));
    b2.add(mSizeLabel, cc.xy(3, 1));
    mShowSoonChb.add(b2.getPanel());

    checked = Settings.propProgramsInTrayShowTooltip.getBoolean();
    mShowChannelTooltipChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showToolTip",
        "Show additional information of the program in a tool tip"), checked);
    mShowChannelTooltipChb.setToolTipText(mLocalizer.msg(
        "programShowing.toolTipTip",
        "Tool tips are small helper to something, like this one."));

    /*
     * mTimeLabel1 = new JLabel(mLocalizer.msg(
     * "programShowing.importantTimeRange", "Search through the next"));
     * mTimeLabel2 = new JLabel(mLocalizer.msg("programShowing.importantHours",
     * "hours for important programs"));
     */
    mImportantHours = new JSpinner(new SpinnerNumberModel(
        Settings.propImportantProgramsInTrayHours.getInt(), 1, 6, 1));

    checked = Settings.propShowImportantProgramsInTrayInSubMenu.getBoolean();
    mShowImportantSubChb = new JRadioButton(mLocalizer.msg(
        "programShowing.importantSubMenu",
        "Show important programs in a sub menu"), checked);
    mShowImportantNotSubChb = new JRadioButton(mLocalizer.msg(
        "programShowing.importantNotSub",
        "Show important programs direct in the tray menu"), !checked);

    ButtonGroup bgImportant = new ButtonGroup();
    bgImportant.add(mShowImportantSubChb);
    bgImportant.add(mShowImportantNotSubChb);

    checked = Settings.propImportantProgramsInTrayContainsStartTime
        .getBoolean();
    mShowImportantTimeChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showStartTime", "Show start time"), checked);
    mShowImportantSubChb.setVerticalTextPosition(JCheckBox.TOP);
    mShowImportantSubChb.setVerticalAlignment(JCheckBox.TOP);
    mShowImportantSubChb.setHorizontalTextPosition(JCheckBox.RIGHT);

    checked = Settings.propShowNowRunningProgramsInTray.getBoolean();
    mShowNowRunningChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showRunning", "Show now running programs"), checked);

    checked = Settings.propShowNowRunningProgramsInTrayInSubMenu.getBoolean();
    mShowNowRunningSubChb = new JRadioButton(mLocalizer.msg(
        "programShowing.runningSubMenu",
        "Show now running programs in a sub menu"), checked);
    mShowNowRunningNotSubChb = new JRadioButton(mLocalizer.msg(
        "programShowing.runningNotSub",
        "Show now running programs direct in the tray menu"), !checked);

    ButtonGroup bgNow = new ButtonGroup();
    bgNow.add(mShowNowRunningSubChb);
    bgNow.add(mShowNowRunningNotSubChb);

    checked = Settings.propNowRunningProgramsInTrayContainsStartTime
        .getBoolean();
    mShowNowRunningTimeChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showStartTime", "Show start time"), checked);

    mChannelOCh = new OrderChooser(
        Settings.propNowRunningProgramsInTrayChannels.getChannelArray(false),
        Settings.propSubscribedChannels.getChannelArray(false), true);

    builder.addSeparator(mLocalizer.msg("basics", "Basic settings"), cc.xyw(1,
        1, 6));
    builder.add(mMinimizeToTrayChb, cc.xyw(2, 3, 4));
    builder.add(mOnlyMinimizeWhenWindowClosingChB, cc.xyw(2, 4, 4));
    builder.addSeparator(mLocalizer.msg("programShowing", "Program showing"),
        cc.xyw(1, 6, 6));

    builder.add(mShowChannelNameChb, cc.xyw(2, 8, 4));
    builder.add(mShowChannelIconChb, cc.xyw(2, 9, 4));

    builder.add(mShowNowRunningChb, cc.xyw(2, 10, 4));
    builder.add(mShowSoonChb, cc.xyw(2, 11, 5));
    builder.add(mShowImportantChb, cc.xyw(2, 12, 2));
    builder.add(b2.getPanel(), cc.xy(5, 12));

    final JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg(
        "programShowing.runningChannels",
        "Which channels should be used for these displays?"), cc.xyw(2, 15, 4));
    builder.add(mChannelOCh, cc.xyw(2, 16, 4));

    mChannelLabel = (JLabel) c.getComponent(0);

    mAdditional = new JButton(mLocalizer.msg("programShowing.extendedSettings",
        "Extended settings"));
    mAdditional.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        extendedSettings();
      }
    });

    builder.add(new JSeparator(), cc.xyw(1, 18, 6));
    builder.add(mAdditional, cc.xy(5, 20));

    mShowImportantChb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        selectEnabled();
        mImportantSize.setEnabled(mShowImportantChb.isSelected());
        mSizeLabel.setEnabled(mShowImportantChb.isSelected());
      }
    });

    mShowNowRunningChb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        selectEnabled();
      }
    });

    mShowSoonChb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        selectEnabled();
      }
    });

    mShowSoonChb.getChangeListeners()[0].stateChanged(new ChangeEvent(
        mShowSoonChb));
    mShowImportantChb.getChangeListeners()[0].stateChanged(new ChangeEvent(
        mShowImportantChb));
    mShowNowRunningChb.getChangeListeners()[0].stateChanged(new ChangeEvent(
        mShowNowRunningChb));

    return builder.getPanel();
  }

  private void extendedSettings() {
    Window parent = UiUtilities.getBestDialogParent(MainFrame.getInstance());
    final JDialog dialog = UiUtilities.createDialog(parent, true);
    dialog.setTitle(mLocalizer.msg("programShowing.extendedTitle",
        "Extended Tray settings"));

    final boolean showTooltip = mShowChannelTooltipChb.isSelected(), showNowRunning = mShowNowRunningTimeChb
        .isSelected(), showNowRunningTime = mShowNowRunningTimeChb.isSelected(), showNowRunningNotSub = mShowNowRunningNotSubChb
        .isSelected(), showNowRunningSub = mShowNowRunningSubChb.isSelected(), showImportantTime = mShowImportantTimeChb
        .isSelected(), showImportantNotSub = mShowImportantNotSubChb
        .isSelected(), showImportantSub = mShowImportantSubChb.isSelected();

    final int importantHours = ((Integer) mImportantHours.getValue())
        .intValue();

    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        dialog.dispose();
        mShowChannelTooltipChb.setSelected(showTooltip);
        mShowNowRunningTimeChb.setSelected(showNowRunning);
        mShowNowRunningTimeChb.setSelected(showNowRunningTime);
        mShowNowRunningNotSubChb.setSelected(showNowRunningNotSub);
        mShowNowRunningSubChb.setSelected(showNowRunningSub);
        mShowImportantTimeChb.setSelected(showImportantTime);
        mShowImportantNotSubChb.setSelected(showImportantNotSub);
        mShowImportantSubChb.setSelected(showImportantSub);
        mImportantHours.setValue(new Integer(importantHours));
      }

      public JRootPane getRootPane() {
        return dialog.getRootPane();
      }
    });

    PanelBuilder pb = new PanelBuilder(new FormLayout(
        "5dlu,pref:grow,default,5dlu,default,5dlu",
        "pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, pref, pref, 10dlu, "
            + "pref, 5dlu, pref,pref,pref,pref, 5dlu, pref ,10dlu, pref"));
    CellConstraints cc = new CellConstraints();

    pb.setDefaultDialogBorder();

    pb.addSeparator(mLocalizer.msg("programShowing.extendedMain",
        "Program showing - Basic"), cc.xyw(1, 1, 6));
    pb.add(mShowChannelTooltipChb, cc.xyw(2, 3, 4));

    JComponent c = pb.addSeparator(mLocalizer.msg("programShowing.extendedNow",
        "Program showing - Now running programs"), cc.xyw(1, 5, 6));
    c.getComponent(0).setEnabled(mShowNowRunningChb.isSelected());

    pb.add(mShowNowRunningTimeChb, cc.xyw(2, 7, 4));
    pb.add(mShowNowRunningNotSubChb, cc.xyw(2, 8, 4));
    pb.add(mShowNowRunningSubChb, cc.xyw(2, 9, 4));
    mShowNowRunningTimeChb.setEnabled(mShowNowRunningChb.isSelected());
    mShowNowRunningNotSubChb.setEnabled(mShowNowRunningChb.isSelected());
    mShowNowRunningSubChb.setEnabled(mShowNowRunningChb.isSelected());

    c = pb.addSeparator(mLocalizer.msg("programShowing.extendedImportant",
        "Program showing - Important programs"), cc.xyw(1, 11, 6));
    c.getComponent(0).setEnabled(mShowImportantChb.isSelected());

    PanelBuilder pb2 = new PanelBuilder(new FormLayout(
        "pref,3dlu,pref,3dlu,pref", "pref"));

    pb2.addLabel(
        mLocalizer.msg("programShowing.importantTimeRange",
            "Show the important program of the next"), cc.xy(1, 1)).setEnabled(
        mShowImportantChb.isSelected());
    pb2.add(mImportantHours, cc.xy(3, 1));
    pb2.addLabel(mLocalizer.msg("programShowing.importantHours", "hours"),
        cc.xy(5, 1)).setEnabled(mShowImportantChb.isSelected());
    mImportantHours.setEnabled(mShowImportantChb.isSelected());

    pb.add(pb2.getPanel(), cc.xyw(2, 13, 4));

    pb.add(mShowImportantTimeChb, cc.xyw(2, 14, 4));
    pb.add(mShowImportantNotSubChb, cc.xyw(2, 15, 4));
    pb.add(mShowImportantSubChb, cc.xyw(2, 16, 4));
    mShowImportantTimeChb.setEnabled(mShowImportantChb.isSelected());
    mShowImportantNotSubChb.setEnabled(mShowImportantChb.isSelected());
    mShowImportantSubChb.setEnabled(mShowImportantChb.isSelected());

    pb.add(new JSeparator(), cc.xyw(1, 18, 6));

    JButton ok = new JButton(mLocalizer.msg("programShowing.ok", "OK"));

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });

    JButton cancel = new JButton(mLocalizer.msg("programShowing.cancel",
        "Cancel"));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
        mShowChannelTooltipChb.setSelected(showTooltip);
        mShowNowRunningTimeChb.setSelected(showNowRunning);
        mShowNowRunningTimeChb.setSelected(showNowRunningTime);
        mShowNowRunningNotSubChb.setSelected(showNowRunningNotSub);
        mShowNowRunningSubChb.setSelected(showNowRunningSub);
        mShowImportantTimeChb.setSelected(showImportantTime);
        mShowImportantNotSubChb.setSelected(showImportantNotSub);
        mShowImportantSubChb.setSelected(showImportantSub);
        mImportantHours.setValue(new Integer(importantHours));
      }
    });

    pb.add(ok, cc.xy(3, 20));
    pb.add(cancel, cc.xy(5, 20));

    dialog.getRootPane().setDefaultButton(ok);
    dialog.setContentPane(pb.getPanel());
    dialog.pack();
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);

  }

  private void selectEnabled() {
    boolean enabled = mShowSoonChb.isSelected()
        || mShowNowRunningChb.isSelected() || mShowImportantChb.isSelected();

    mChannelLabel.setEnabled(enabled);
    mShowChannelNameChb.setEnabled(enabled);
    mShowChannelIconChb.setEnabled(enabled);
    mChannelOCh.setEnabled(enabled);
    mAdditional.setEnabled(enabled);
  }

  /**
   * Save the Settings-Dialog
   */
  public void saveSettings() {
    if (mOnlyMinimizeWhenWindowClosingChB != null) {
      boolean checked = mOnlyMinimizeWhenWindowClosingChB.isSelected();
      Settings.propOnlyMinimizeWhenWindowClosing.setBoolean(checked);
    }

    if (mMinimizeToTrayChb != null) {
      boolean checked = mMinimizeToTrayChb.isSelected();
      Settings.propMinimizeToTray.setBoolean(checked);
    }

    if (mShowNowRunningChb != null)
      Settings.propShowNowRunningProgramsInTray.setBoolean(mShowNowRunningChb
          .isSelected());
    if (mShowImportantChb != null)
      Settings.propShowImportantProgramsInTray.setBoolean(mShowImportantChb
          .isSelected());
    if (mShowNowRunningSubChb != null)
      Settings.propShowNowRunningProgramsInTrayInSubMenu
          .setBoolean(mShowNowRunningSubChb.isSelected());
    if (mShowImportantSubChb != null)
      Settings.propShowImportantProgramsInTrayInSubMenu
          .setBoolean(mShowImportantSubChb.isSelected());
    if (mShowNowRunningTimeChb != null)
      Settings.propNowRunningProgramsInTrayContainsStartTime
          .setBoolean(mShowNowRunningTimeChb.isSelected());
    if (mShowImportantTimeChb != null)
      Settings.propImportantProgramsInTrayContainsStartTime
          .setBoolean(mShowImportantTimeChb.isSelected());
    if (mShowChannelNameChb != null)
      Settings.propProgramsInTrayContainsChannel.setBoolean(mShowChannelNameChb
          .isSelected());
    if (mShowChannelIconChb != null)
      Settings.propProgramsInTrayContainsChannelIcon
          .setBoolean(mShowChannelIconChb.isSelected());
    if (mShowChannelTooltipChb != null)
      Settings.propProgramsInTrayShowTooltip.setBoolean(mShowChannelTooltipChb
          .isSelected());
    if (mShowSoonChb != null)
      Settings.propShowSoonProgramsInTray.setBoolean(mShowSoonChb.isSelected());

    Object[] order = mChannelOCh.getOrder();
    Channel[] ch = new Channel[order.length];

    for (int i = 0; i < ch.length; i++)
      ch[i] = (Channel) order[i];

    if (order != null)
      Settings.propNowRunningProgramsInTrayChannels.setChannelArray(ch);

    Settings.propImportantProgramsInTraySize.setInt(((Integer) mImportantSize
        .getValue()).intValue());
    Settings.propImportantProgramsInTrayHours.setInt(((Integer) mImportantHours
        .getValue()).intValue());
    Settings.propShowProgramsInTrayWasConfigured.setBoolean(true);
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("tray", "Tray");
  }
}
