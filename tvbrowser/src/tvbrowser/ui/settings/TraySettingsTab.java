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

import java.awt.Color;
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

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
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
      mShowImportantTimeChb, mShowImportantDateChb, mShowChannelNameChb, mShowChannelIconChb,
      mShowChannelTooltipChb, mShowTimeProgramsChb, mShowTimeProgramsTimeChb, mTrayIsEnabled;

  private JRadioButton mShowNowRunningNotSubChb, mShowNowRunningSubChb,
      mShowImportantNotSubChb, mShowImportantSubChb;

  private JButton mAdditional;

  private JSpinner mImportantSize;
  private JLabel mChannelLabel, mSizeLabel;
  private OrderChooser mChannelOCh;
  private ColorLabel mTimeProgramLightColorLb,mTimeProgramDarkColorLb;
  private boolean mOldTrayState;

  /**
   * Create the Settings-Dialog
   */
  public JPanel createSettingsPanel() {
    
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu, 10dlu, pref:grow, 25dlu, pref, 5dlu",

        "pref,10dlu," +
        "pref, 5dlu, pref, pref, 10dlu, pref, 5dlu, pref, pref, pref, " +
        "pref, pref, pref, 10dlu, pref, pref, 5dlu, pref, 10dlu, pref"));
    builder.setDefaultDialogBorder();
    CellConstraints cc = new CellConstraints();

    String msg = mLocalizer.msg("trayIsEnabled", "Tray activated");
    mOldTrayState = Settings.propTrayIsEnabled.getBoolean();
    mTrayIsEnabled = new JCheckBox(msg, mOldTrayState);

    msg = mLocalizer.msg("minimizeToTray", "Minimize to Tray");
    boolean checked = Settings.propMinimizeToTray.getBoolean();
    mMinimizeToTrayChb = new JCheckBox(msg, checked && mOldTrayState);

    msg = mLocalizer.msg("onlyMinimizeWhenWindowClosing",
        "When closing the main window only minimize TV-Browser, don't quit.");
    checked = Settings.propOnlyMinimizeWhenWindowClosing.getBoolean() && mOldTrayState;
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

    checked = Settings.propShowTimeProgramsInTray.getBoolean();
    mShowTimeProgramsChb = new JCheckBox(mLocalizer.msg("programShowing.showProgramsAt",
        "Show programs at..."), checked);

    mSizeLabel = new JLabel(mLocalizer.msg(
        "programShowing.importantMaxPrograms", "important programs to show"));
    mImportantSize = new JSpinner(new SpinnerNumberModel(
        Settings.propImportantProgramsInTraySize.getInt(), 1, 10, 1));

    PanelBuilder b2 = new PanelBuilder(new FormLayout("pref,3dlu,pref", "pref"));

    b2.add(mImportantSize, cc.xy(1, 1));
    b2.add(mSizeLabel, cc.xy(3, 1));

    checked = Settings.propProgramsInTrayShowTooltip.getBoolean();
    mShowChannelTooltipChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showToolTip",
        "Show additional information of the program in a tool tip"), checked);
    mShowChannelTooltipChb.setToolTipText(mLocalizer.msg(
        "programShowing.toolTipTip",
        "Tool tips are small helper to something, like this one."));

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
    
    mTimeProgramLightColorLb = new ColorLabel(Settings.propTimeProgramsLightBackground.getColor());
    mTimeProgramLightColorLb.setStandardColor(Settings.propTimeProgramsLightBackground.getDefaultColor());
    mTimeProgramDarkColorLb = new ColorLabel(Settings.propTimeProgramsDarkBackground.getColor());
    mTimeProgramDarkColorLb.setStandardColor(Settings.propTimeProgramsDarkBackground.getDefaultColor());

    checked = Settings.propImportantProgramsInTrayContainsDate.getBoolean();
    mShowImportantDateChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showDate", "Show date"), checked);
    
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

    checked = Settings.propTimeProgramsInTrayContainsTime
    .getBoolean();
    mShowTimeProgramsTimeChb = new JCheckBox(mLocalizer.msg(
    "programShowing.showStartTime", "Show start time"), checked);
    
    mChannelOCh = new OrderChooser(
        Settings.propNowRunningProgramsInTrayChannels.getChannelArray(false),
        Settings.propSubscribedChannels.getChannelArray(false), true);

    builder.add(mTrayIsEnabled, cc.xyw(1,1,6));
    
    final JPanel b = (JPanel)builder.addSeparator(mLocalizer.msg("basics", "Basic settings"), cc.xyw(1,
        3, 6));
    builder.add(mMinimizeToTrayChb, cc.xyw(2, 5, 4));
    builder.add(mOnlyMinimizeWhenWindowClosingChB, cc.xyw(2, 6, 4));
    final JPanel pS = (JPanel)builder.addSeparator(mLocalizer.msg("programShowing", "Program showing"),
        cc.xyw(1, 8, 6));

    builder.add(mShowChannelNameChb, cc.xyw(2, 10, 4));
    builder.add(mShowChannelIconChb, cc.xyw(2, 11, 4));

    builder.add(mShowNowRunningChb, cc.xyw(2, 12, 4));
    builder.add(mShowTimeProgramsChb, cc.xyw(2, 13, 5));
    builder.add(mShowImportantChb, cc.xyw(2, 14, 2));
    builder.add(b2.getPanel(), cc.xy(5, 14));

    final JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg(
        "programShowing.runningChannels",
        "Which channels should be used for these displays?"), cc.xyw(2, 17, 4));
    builder.add(mChannelOCh, cc.xyw(2, 18, 4));

    mChannelLabel = (JLabel) c.getComponent(0);
    
    mAdditional = new JButton(mLocalizer.msg("programShowing.extendedSettings",
        "Extended settings"));
    mAdditional.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        extendedSettings();
      }
    });

    builder.add(new JSeparator(), cc.xyw(1, 20, 6));
    builder.add(mAdditional, cc.xy(5, 22));

    mTrayIsEnabled.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        b.getComponent(0).setEnabled(mTrayIsEnabled.isSelected());
        pS.getComponent(0).setEnabled(mTrayIsEnabled.isSelected());
        c.getComponent(0).setEnabled(mTrayIsEnabled.isSelected());
        mChannelLabel.setEnabled(mTrayIsEnabled.isSelected());
        mImportantSize.setEnabled(mTrayIsEnabled.isSelected());
        mSizeLabel.setEnabled(mTrayIsEnabled.isSelected());
        mShowTimeProgramsChb.setEnabled(mTrayIsEnabled.isSelected());
        mShowImportantChb.setEnabled(mTrayIsEnabled.isSelected());
        mMinimizeToTrayChb.setEnabled(mTrayIsEnabled.isSelected());
        mOnlyMinimizeWhenWindowClosingChB.setEnabled(mTrayIsEnabled.isSelected());
        mShowNowRunningChb.setEnabled(mTrayIsEnabled.isSelected());
        mChannelOCh.setEnabled(mTrayIsEnabled.isSelected());
        mAdditional.setEnabled(mTrayIsEnabled.isSelected());
        mShowChannelNameChb.setEnabled(mTrayIsEnabled.isSelected());
        mShowChannelIconChb.setEnabled(mTrayIsEnabled.isSelected());
      }
    });
    
    mShowImportantChb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        selectEnabled();
        mImportantSize.setEnabled(mShowImportantChb.isSelected() && mTrayIsEnabled.isSelected());
        mSizeLabel.setEnabled(mShowImportantChb.isSelected() && mTrayIsEnabled.isSelected());
      }
    });

    mShowNowRunningChb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        selectEnabled();
      }
    });

    mShowTimeProgramsChb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        selectEnabled();
      }
    });

    mShowTimeProgramsChb.getChangeListeners()[0].stateChanged(new ChangeEvent(
        mShowTimeProgramsChb));
    mShowImportantChb.getChangeListeners()[0].stateChanged(new ChangeEvent(
        mShowImportantChb));
    mShowNowRunningChb.getChangeListeners()[0].stateChanged(new ChangeEvent(
        mShowNowRunningChb));
    mTrayIsEnabled.getChangeListeners()[0].stateChanged(new ChangeEvent(
        mTrayIsEnabled));
    
    return builder.getPanel();
  }

  private void extendedSettings() {
    Window parent = UiUtilities.getBestDialogParent(MainFrame.getInstance());
    final JDialog dialog = UiUtilities.createDialog(parent, true);
    dialog.setTitle(mLocalizer.msg("programShowing.extendedTitle",
        "Extended Tray settings"));

    final boolean showTooltip = mShowChannelTooltipChb.isSelected(),
                  showNowRunning = mShowNowRunningTimeChb.isSelected(),
                  showNowRunningTime = mShowNowRunningTimeChb.isSelected(),
                  showNowRunningNotSub = mShowNowRunningNotSubChb.isSelected(),
                  showNowRunningSub = mShowNowRunningSubChb.isSelected(),
                  showImportantTime = mShowImportantTimeChb.isSelected(),
                  showTimeProgramsTime = mShowTimeProgramsTimeChb.isSelected(),
                  showImportantDate = mShowImportantDateChb.isSelected(),
                  showImportantNotSub = mShowImportantNotSubChb.isSelected(),
                  showImportantSub = mShowImportantSubChb.isSelected();

    final Color timeTrayLight = Settings.propTimeProgramsLightBackground.getColor(),
                timeTrayDark = Settings.propTimeProgramsDarkBackground.getColor();

    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        dialog.dispose();
        mShowChannelTooltipChb.setSelected(showTooltip);
        mShowNowRunningTimeChb.setSelected(showNowRunning);
        mShowNowRunningTimeChb.setSelected(showNowRunningTime);
        mShowTimeProgramsTimeChb.setSelected(showTimeProgramsTime);
        mShowNowRunningNotSubChb.setSelected(showNowRunningNotSub);
        mShowNowRunningSubChb.setSelected(showNowRunningSub);
        mShowImportantTimeChb.setSelected(showImportantTime);
        mShowImportantDateChb.setSelected(showImportantDate);
        mShowImportantNotSubChb.setSelected(showImportantNotSub);
        mShowImportantSubChb.setSelected(showImportantSub);
        mTimeProgramLightColorLb.setColor(timeTrayLight);
        mTimeProgramDarkColorLb.setColor(timeTrayDark);
      }

      public JRootPane getRootPane() {
        return dialog.getRootPane();
      }
    });

    PanelBuilder pb = new PanelBuilder(new FormLayout(
        "5dlu,pref:grow,default,5dlu,default,5dlu",
        "pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, pref, pref, 10dlu, " +
        "pref, 5dlu, pref, pref, pref, pref, 10dlu, pref, 5dlu, pref, " +
        "pref, 5dlu, pref ,10dlu, pref"));
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
    
    pb.add(mShowImportantDateChb, cc.xyw(2, 13, 4));
    pb.add(mShowImportantTimeChb, cc.xyw(2, 14, 4));
    pb.add(mShowImportantNotSubChb, cc.xyw(2, 15, 4));
    pb.add(mShowImportantSubChb, cc.xyw(2, 16, 4));
    mShowImportantTimeChb.setEnabled(mShowImportantChb.isSelected());
    mShowImportantNotSubChb.setEnabled(mShowImportantChb.isSelected());
    mShowImportantSubChb.setEnabled(mShowImportantChb.isSelected());

    
    ColorButton light = new ColorButton(mTimeProgramLightColorLb);
    ColorButton dark = new ColorButton(mTimeProgramDarkColorLb);
    
    light.setEnabled(mShowTimeProgramsChb.isSelected());
    dark.setEnabled(mShowTimeProgramsChb.isSelected());
    mTimeProgramLightColorLb.setEnabled(mShowTimeProgramsChb.isSelected());
    mTimeProgramDarkColorLb.setEnabled(mShowTimeProgramsChb.isSelected());
    mShowTimeProgramsTimeChb.setEnabled(mShowTimeProgramsChb.isSelected());
    
    c = pb.addSeparator(mLocalizer.msg("programShowing.extendedTime",
    "Program showing - Programs at..."), cc.xyw(1, 18, 6));
    c.getComponent(0).setEnabled(mShowTimeProgramsChb.isSelected());
    
    PanelBuilder colors = new PanelBuilder(new FormLayout(
        "default,5dlu,default,5dlu,default", "pref,2dlu,pref"));    
    
    colors.addLabel(
        mLocalizer.msg("programShowing.timeLight",
            "Background color of the programs at..."), cc.xy(1, 1)).setEnabled(
                mShowTimeProgramsChb.isSelected());
    colors.add(mTimeProgramLightColorLb, cc.xy(3, 1));
    colors.add(light,cc.xy(5, 1));

    colors.addLabel(
        mLocalizer.msg("programShowing.timeDark",
            "Progress color of the programs at..."), cc.xy(1, 3)).setEnabled(
                mShowTimeProgramsChb.isSelected());
    colors.add(mTimeProgramDarkColorLb, cc.xy(3, 3));
    colors.add(dark,cc.xy(5, 3));

    pb.add(mShowTimeProgramsTimeChb, cc.xyw(2,20,4));
    pb.add(colors.getPanel(), cc.xyw(2, 21, 4));
    pb.add(new JSeparator(), cc.xyw(1, 23, 6));

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
        mShowTimeProgramsTimeChb.setSelected(showTimeProgramsTime);
        mShowImportantNotSubChb.setSelected(showImportantNotSub);
        mShowImportantSubChb.setSelected(showImportantSub);
        mTimeProgramLightColorLb.setColor(timeTrayLight);
        mTimeProgramDarkColorLb.setColor(timeTrayDark);
      }
    });

    pb.add(ok, cc.xy(3, 25));
    pb.add(cancel, cc.xy(5, 25));

    dialog.getRootPane().setDefaultButton(ok);
    dialog.setContentPane(pb.getPanel());
    dialog.pack();
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);

  }

  private void selectEnabled() {
    boolean enabled = mShowTimeProgramsChb.isSelected()
        || mShowNowRunningChb.isSelected() || mShowImportantChb.isSelected();

    mChannelLabel.setEnabled((mShowTimeProgramsChb.isSelected()
        || mShowNowRunningChb.isSelected()) && mTrayIsEnabled.isSelected());
    mShowChannelNameChb.setEnabled(enabled && mTrayIsEnabled.isSelected());
    mShowChannelIconChb.setEnabled(enabled && mTrayIsEnabled.isSelected());
    mChannelOCh.setEnabled((mShowTimeProgramsChb.isSelected()
        || mShowNowRunningChb.isSelected()) && mTrayIsEnabled.isSelected());
    mAdditional.setEnabled(enabled && mTrayIsEnabled.isSelected());
  }

  /**
   * Save the Settings-Dialog
   */
  public void saveSettings() {
    if (mOnlyMinimizeWhenWindowClosingChB != null) {
      boolean checked = mOnlyMinimizeWhenWindowClosingChB.isSelected() && mTrayIsEnabled.isSelected();
      Settings.propOnlyMinimizeWhenWindowClosing.setBoolean(checked);
    }
    if (mMinimizeToTrayChb != null) {
      boolean checked = mMinimizeToTrayChb.isSelected() && mTrayIsEnabled.isSelected();
      Settings.propMinimizeToTray.setBoolean(checked);
    }

    if (mTrayIsEnabled != null) {
      Settings.propTrayIsEnabled.setBoolean(mTrayIsEnabled.isSelected());
      if(mTrayIsEnabled.isSelected() && !mOldTrayState)
        TVBrowser.loadTray();
      else if(!mTrayIsEnabled.isSelected() && mOldTrayState)
        TVBrowser.removeTray();
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
    if (mShowImportantDateChb != null)
      Settings.propImportantProgramsInTrayContainsDate
          .setBoolean(mShowImportantDateChb.isSelected());
    if (mShowChannelNameChb != null)
      Settings.propProgramsInTrayContainsChannel.setBoolean(mShowChannelNameChb
          .isSelected());
    if (mShowChannelIconChb != null)
      Settings.propProgramsInTrayContainsChannelIcon
          .setBoolean(mShowChannelIconChb.isSelected());
    if (mShowChannelTooltipChb != null)
      Settings.propProgramsInTrayShowTooltip.setBoolean(mShowChannelTooltipChb
          .isSelected());
    if (mShowTimeProgramsChb != null)
      Settings.propShowTimeProgramsInTray.setBoolean(mShowTimeProgramsChb.isSelected());
    if (mShowTimeProgramsTimeChb != null)
      Settings.propTimeProgramsInTrayContainsTime.setBoolean(mShowTimeProgramsTimeChb.isSelected());
    if (mTimeProgramLightColorLb != null)
      Settings.propTimeProgramsLightBackground.setColor(mTimeProgramLightColorLb.getColor());
    if (mTimeProgramDarkColorLb != null)
      Settings.propTimeProgramsDarkBackground.setColor(mTimeProgramDarkColorLb.getColor());    
    
    Object[] order = mChannelOCh.getOrder();
    Channel[] ch = new Channel[order.length];

    for (int i = 0; i < ch.length; i++)
      ch[i] = (Channel) order[i];

    if (order != null)
      Settings.propNowRunningProgramsInTrayChannels.setChannelArray(ch);

    Settings.propImportantProgramsInTraySize.setInt(((Integer) mImportantSize
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
