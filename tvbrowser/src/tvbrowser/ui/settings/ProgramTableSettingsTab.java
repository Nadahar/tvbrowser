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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.tablebackgroundstyles.DayTimeBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.SingleColorBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.SingleImageBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.TableBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.TimeBlockBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.UiColorBackgroundStyle;
import tvbrowser.ui.settings.tablebackgroundstyles.UiTimeBlockBackgroundStyle;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.CaretPositionCorrector;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramTableSettingsTab implements SettingsTab, ActionListener {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ProgramTableSettingsTab.class);

  private JPanel mSettingsPn;

  private JComboBox<String> mProgramArrangementCB;

  private JComboBox<TableBackgroundStyle> mBackgroundStyleCB;

  private JButton mConfigBackgroundStyleBt;

  private JButton mDefaultBtn;

  private JSpinner mColWidth, mStartOfDayTimeSp, mEndOfDayTimeSp;

  private JCheckBox mMouseOverCb;

  private ColorLabel mMouseOverColorLb;
  
  private ColorLabel mForegroundColorLb;
  
  private short mLastSelectedLayoutIndex;

  private JCheckBox mCutLongTitlesCB;

  private JSpinner mCutLongTitlesSelection;

  private JCheckBox mAutoScrollCb;
  private JCheckBox mScrollHorizontalCb;

  private JSpinner mDescriptionLines;

  private JLabel mCutLongTitlesLabel;

  private JCheckBox mShortProgramsCB;

  private JSpinner mShortProgramsMinutes;

  private JLabel mShortProgramsLabel;
  
  private ColorButton mProgramPanelForegroundColorChangeBtn;
  
  private JComponent mForegroundSeparator;
  
  private JLabel mForegroundLabel;
  
  private JCheckBox mTypeAsYouFind;
  
  private JCheckBox mAutoChangeDate;

  private ColorLabel mLightColorLb,mDarkColorLb;
  private ColorButton mLight, mDark;
  private JCheckBox mShowScrollHighlight;
  
  private JCheckBox mShowScrollChannelHighlight;
  private ColorLabel mScrollChannelLb;
  private ColorButton mScrollChannelColor;
  
  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if (source == mDefaultBtn) {
      mColWidth.setValue(Settings.propColumnWidth.getDefault());
    }
  }
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout("5dlu, pref, 3dlu, pref, 3dlu, pref, fill:pref:grow, 3dlu", "");
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DIALOG);

    CellConstraints cc = new CellConstraints();
    int currentRow = 1;
    
    // Layout-Rows ****************************************
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("10dlu"));
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("layout", "Layout")), cc.xyw(1, currentRow, 8));
    mSettingsPn.add(new JLabel(mLocalizer.msg("programArrangement",
        "Program arrangement")), cc.xy(2, (currentRow += 2)));
    
    // program table layout
    String[] arrangementArr = { mLocalizer.msg(Settings.LAYOUT_TIME_SYNCHRONOUS, "Time synchronous"),
        mLocalizer.msg(Settings.LAYOUT_REAL_SYNCHRONOUS, "Real time synchronous"),
        mLocalizer.msg(Settings.LAYOUT_COMPACT, "Compact"),mLocalizer.msg(Settings.LAYOUT_REAL_COMPACT, "Real compact"),
        mLocalizer.msg(Settings.LAYOUT_TIME_BLOCK, "Time block"),
        mLocalizer.msg(Settings.LAYOUT_COMPACT_TIME_BLOCK, "Compact time block"),
        mLocalizer.msg(Settings.LAYOUT_OPTIMIZED_COMPACT_TIME_BLOCK, "Optimized compact time block")};
    mProgramArrangementCB = new JComboBox<>(arrangementArr);
    if (Settings.propTableLayout.getString().equals(Settings.LAYOUT_COMPACT)) {
      mProgramArrangementCB.setSelectedIndex(2);
    } else if (Settings.propTableLayout.getString().equals(Settings.LAYOUT_REAL_COMPACT)) {
      mProgramArrangementCB.setSelectedIndex(3);
    } else if (Settings.propTableLayout.getString().equals(Settings.LAYOUT_TIME_SYNCHRONOUS)) {
      mProgramArrangementCB.setSelectedIndex(0);
    } else if (Settings.propTableLayout.getString().equals(Settings.LAYOUT_TIME_BLOCK)) {
      mProgramArrangementCB.setSelectedIndex(4);
    } else if (Settings.propTableLayout.getString().equals(Settings.LAYOUT_COMPACT_TIME_BLOCK)) {
      mProgramArrangementCB.setSelectedIndex(5);
    } else if (Settings.propTableLayout.getString().equals(Settings.LAYOUT_OPTIMIZED_COMPACT_TIME_BLOCK)) {
      mProgramArrangementCB.setSelectedIndex(6);
    } else {
      mProgramArrangementCB.setSelectedIndex(1);
    }
    
    mLastSelectedLayoutIndex = (short)mProgramArrangementCB.getSelectedIndex();

    mSettingsPn.add(mProgramArrangementCB, cc.xy(4, currentRow));

    // Cut long titles
    mCutLongTitlesCB = new JCheckBox(mLocalizer.msg("cutTitle",
        "Cut long titles"), Settings.propProgramTableCutTitle.getBoolean());
    mSettingsPn.add(mCutLongTitlesCB, cc.xyw(2, (currentRow += 2), 2));
    mCutLongTitlesSelection = new JSpinner(new SpinnerNumberModel(
        Settings.propProgramTableCutTitleLines.getInt(), 1, 3, 1));
    mSettingsPn.add(mCutLongTitlesSelection, cc.xy(4, currentRow));
    mCutLongTitlesLabel = new JLabel(mLocalizer.msg("lines", "Lines"));
    mSettingsPn.add(mCutLongTitlesLabel, cc.xy(6, currentRow));
    
    mCutLongTitlesCB.addActionListener(e -> {
      mCutLongTitlesSelection.setEnabled(mCutLongTitlesCB.isSelected());
      mCutLongTitlesLabel.setEnabled(mCutLongTitlesCB.isSelected());
    });
    mCutLongTitlesCB.getActionListeners()[0].actionPerformed(null);
    
    // Short descriptions N lines
    mDescriptionLines = new JSpinner(new SpinnerNumberModel(
        Settings.propProgramPanelMaxLines.getInt(), 1, 5, 1));
    mSettingsPn.add(new JLabel(mLocalizer.msg("shortDescription",
        "Short description")), cc.xyw(2, currentRow += 2, 2));
    mSettingsPn.add(mDescriptionLines, cc.xy(4, currentRow));
    mSettingsPn.add(new JLabel(mLocalizer.msg("lines", "Lines")), cc.xy(6,
        currentRow));
    
    // Short programs no description
    mShortProgramsCB = new JCheckBox(mLocalizer.msg("shortPrograms",
        "If duration less than"),
        Settings.propProgramPanelShortDurationActive.getBoolean());
    mSettingsPn.add(mShortProgramsCB, cc.xyw(2, (currentRow += 2), 2));
    mShortProgramsMinutes = new JSpinner(new SpinnerNumberModel(
        Settings.propProgramPanelShortDurationMinutes.getInt(), 1, 30, 1));
    mSettingsPn.add(mShortProgramsMinutes, cc.xy(4, currentRow));
    mShortProgramsLabel = new JLabel(mLocalizer.msg("shortPrograms2",
        "minutes, then hide description"));
    mSettingsPn.add(mShortProgramsLabel, cc.xy(6, currentRow));

    mShortProgramsCB.addActionListener(e -> {
      mShortProgramsMinutes.setEnabled(mShortProgramsCB.isSelected());
      mShortProgramsLabel.setEnabled(mShortProgramsCB.isSelected());
    });
    mShortProgramsCB.getActionListeners()[0].actionPerformed(null);
    
    // Column Rows ***************************************
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("10dlu"));
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("columnwidth", "column width")), cc.xyw(1,
        (currentRow += 2), 8));
    
    // column width
    int width = Settings.propColumnWidth.getInt();

    if (width > Settings.MAX_COLUMN_WIDTH) {
      width = Settings.MAX_COLUMN_WIDTH;
    }

    if (width < Settings.MIN_COLUMN_WIDTH) {
      width = Settings.MIN_COLUMN_WIDTH;
    }

    mColWidth = new JSpinner(new SpinnerNumberModel(
            width, Settings.MIN_COLUMN_WIDTH, Settings.MAX_COLUMN_WIDTH, 1));

    mSettingsPn.add(new JLabel(mLocalizer.msg("widthInPixels", "Width in Pixels")), cc.xy(2, (currentRow += 2)));
    mSettingsPn.add(mColWidth, cc.xy(4, currentRow));

    mDefaultBtn = new JButton(Localizer.getLocalization(Localizer.I18N_DEFAULT));
    mDefaultBtn.addActionListener(this);

    mSettingsPn.add(mDefaultBtn, cc.xy(6, currentRow));
    
    // Column Rows ***************************************
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("10dlu"));
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("range", "Range")), cc.xyw(1, (currentRow += 2), 8));
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("startOfDay", "Start of day")),
        cc.xy(2, (currentRow += 2)));
    
    TwoSpinnerDateModel startModel = new TwoSpinnerDateModel();
    
    mStartOfDayTimeSp = new JSpinner(startModel);
    startModel.setMe(mStartOfDayTimeSp);
    
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mStartOfDayTimeSp, Settings.getTimePattern());
    mStartOfDayTimeSp.setEditor(dateEditor);
    mSettingsPn.add(mStartOfDayTimeSp, cc.xy(4, currentRow));
    mSettingsPn.add(new JLabel("("
        + Localizer.getLocalization(Localizer.I18N_TODAY) + ")"), cc.xy(6,
        currentRow));
    CaretPositionCorrector.createCorrector(dateEditor.getTextField(), new char[] {':'}, -1);
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("endOfDay", "End of day")), cc
        .xy(2, (currentRow += 2)));
    
    TwoSpinnerDateModel endModel = new TwoSpinnerDateModel();
    
    mEndOfDayTimeSp = new JSpinner(endModel);
    endModel.setMe(mEndOfDayTimeSp);
    
    dateEditor = new JSpinner.DateEditor(mEndOfDayTimeSp, Settings.getTimePattern());
    mEndOfDayTimeSp.setEditor(dateEditor);
    mSettingsPn.add(mEndOfDayTimeSp, cc.xy(4, currentRow));
    mSettingsPn.add(new JLabel("(" + mLocalizer.msg("nextDay", "next day")
        + ")"), cc.xy(6, currentRow));
    CaretPositionCorrector.createCorrector(dateEditor.getTextField(), new char[] {':'}, -1);
    
    int minutes;
    Calendar cal = Calendar.getInstance();
    minutes = Settings.propProgramTableStartOfDay.getInt();
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    mStartOfDayTimeSp.setValue(cal.getTime());
    
    
    minutes = Settings.propProgramTableEndOfDay.getInt();
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    mEndOfDayTimeSp.setValue(cal.getTime());
    
    
    
    // Table Background ***************************************
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("10dlu"));
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("tableBackground", "Table background")), cc.xyw(1,(currentRow += 2), 8));

    mSettingsPn.add(new JLabel(mLocalizer.msg("tableBackgroundStyle",
        "Table background style")), cc.xy(2, (currentRow += 2)));
    
    TableBackgroundStyle[] styles = getTableBackgroundStyles();
    mBackgroundStyleCB = new JComboBox<>(styles);

    String style = Settings.propTableBackgroundStyle.getString();
    for (int i = 0; i < styles.length; i++) {
      if (styles[i].getSettingsString().equals(style)) {
        mBackgroundStyleCB.setSelectedIndex(i);
        break;
      }
    }

    mBackgroundStyleCB.addItemListener(e -> {
      updateBackgroundStyleConfigureButton();
    });

    mSettingsPn.add(mBackgroundStyleCB, cc.xy(4, currentRow));
    
    mConfigBackgroundStyleBt = new JButton(mLocalizer.ellipsisMsg("configure", "Configure"));

    mConfigBackgroundStyleBt.addActionListener(e -> {
      ConfigureBackgroundStyleDialog dlg = new ConfigureBackgroundStyleDialog(mBackgroundStyleCB,
          (TableBackgroundStyle) mBackgroundStyleCB.getSelectedItem());
      dlg.show();
    });

    mSettingsPn.add(mConfigBackgroundStyleBt, cc.xy(6, currentRow));
        
    // Foreground color
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));

    mForegroundColorLb = new ColorLabel(Settings.propProgramPanelForegroundColor.getColor());
    mForegroundColorLb.setStandardColor(Settings.propProgramPanelForegroundColor.getDefaultColor());
    mProgramPanelForegroundColorChangeBtn = new ColorButton(mForegroundColorLb);

    mForegroundSeparator = DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("foreground", "Foreground")); 
    mForegroundLabel = new JLabel(mLocalizer.msg("fontColor", "Font color"));
    
    mSettingsPn.add(mForegroundSeparator, cc.xyw(1,(currentRow += 2), 8));
    
    mSettingsPn.add(mForegroundLabel, cc
        .xy(2,
        (currentRow += 2)));
    mSettingsPn.add(mForegroundColorLb, cc.xy(4, currentRow));
    mSettingsPn.add(mProgramPanelForegroundColorChangeBtn, cc.xy(6, currentRow));
    
    // Mouse *********************************************
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    
    // scroll markings
    layout.appendRow(RowSpec.decode("10dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    
    // channel scroll markings
    layout.appendRow(RowSpec.decode("10dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    
    // Miscellaneous *********************************************
    layout.appendRow(RowSpec.decode("10dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("mouse", "Mouse")), cc.xyw(1,
        (currentRow += 2), 8));

    mMouseOverCb = new JCheckBox(mLocalizer.msg("MouseOver", "Mouse-Over-Effect"));
    mMouseOverCb.setSelected(Settings.propProgramTableMouseOver.getBoolean());
    mSettingsPn.add(mMouseOverCb, cc.xy(2, (currentRow += 2)));
    
    mMouseOverColorLb = new ColorLabel(Settings.propProgramTableMouseOverColor.getColor());
    mMouseOverColorLb.setStandardColor(Settings.propProgramTableMouseOverColor.getDefaultColor());
    final ColorButton mouseOverColorChangeBtn = new ColorButton(mMouseOverColorLb);
    mMouseOverCb.addActionListener(e -> {
      boolean enabled = mMouseOverCb.isSelected();
      mMouseOverColorLb.setEnabled(enabled);
      mouseOverColorChangeBtn.setEnabled(enabled);
    });
    mMouseOverCb.getActionListeners()[0].actionPerformed(null);
    
    mTypeAsYouFind = new JCheckBox(mLocalizer.msg("typeAsYouFind", "Type-As-You-Find enabled"),Settings.propTypeAsYouFindEnabled.getBoolean());

    mSettingsPn.add(mMouseOverColorLb, cc.xy(4, currentRow));
    mSettingsPn.add(mouseOverColorChangeBtn, cc.xy(6, currentRow));
    
    // auto scrolling
    mAutoScrollCb = new JCheckBox(mLocalizer.msg("mouseAutoScroll",
        "Throw'n scroll"));
    mAutoScrollCb.setSelected(Settings.propProgramTableMouseAutoScroll
        .getBoolean());
    mSettingsPn.add(mAutoScrollCb, cc.xyw(2, (currentRow += 2), 6));
        
    mScrollHorizontalCb = new JCheckBox(mLocalizer.msg("mouseScrollHorizontal", "Scroll horizontal with mouse wheel"));
    mScrollHorizontalCb.setSelected(Settings.propProgramTableScrollHorizontal.getBoolean());
    mSettingsPn.add(mScrollHorizontalCb, cc.xyw(2, (currentRow += 2), 6));
    
    mAutoChangeDate = new JCheckBox(mLocalizer.msg("mouseAutoChangeDate",
        "Automatically change date when scrolling with mouse wheel against top and bottom"));
    mAutoChangeDate.setSelected(Settings.propProgramTableAutoChangeDate.getBoolean());
    mSettingsPn.add(mAutoChangeDate, cc.xyw(2, (currentRow += 2), 6));
    
    mShowScrollHighlight = new JCheckBox(mLocalizer.msg("activated","Activated"), Settings.propScrollToTimeMarkingActivated.getBoolean());
    
    mLightColorLb = new ColorLabel(Settings.propScrollToTimeProgramsLightBackground.getColor());
    mLightColorLb.setStandardColor(Settings.propScrollToTimeProgramsLightBackground.getDefaultColor());
    mLightColorLb.setEnabled(mShowScrollHighlight.isSelected());
    mDarkColorLb = new ColorLabel(Settings.propScrollToTimeProgramsDarkBackground.getColor());
    mDarkColorLb.setStandardColor(Settings.propScrollToTimeProgramsDarkBackground.getDefaultColor());
    mDarkColorLb.setEnabled(mShowScrollHighlight.isSelected());
    
    mLight = new ColorButton(mLightColorLb);
    mLight.setEnabled(mShowScrollHighlight.isSelected());
    
    mDark = new ColorButton(mDarkColorLb);
    mDark.setEnabled(mShowScrollHighlight.isSelected());
    
    final JLabel mLightDesc = new JLabel(TrayBaseSettingsTab.mLocalizer.msg("progressLight", "Background color"));
    final JLabel mDarkDesc = new JLabel(TrayBaseSettingsTab.mLocalizer.msg("progressDark", "Progress color"));
    
    mShowScrollHighlight.addItemListener(e -> {
      mLightColorLb.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      mDarkColorLb.setEnabled(mLightColorLb.isEnabled());
      mLight.setEnabled(mLightColorLb.isEnabled());
      mDark.setEnabled(mLightColorLb.isEnabled());
      mLightDesc.setEnabled(mLightColorLb.isEnabled());
      mDarkDesc.setEnabled(mLightColorLb.isEnabled());
    });
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("scrollToTimeTitle", "Highlight programs running at jump time")), cc.xyw(1,
        (currentRow += 2), 8));
    
    mSettingsPn.add(mShowScrollHighlight, cc.xyw(2, (currentRow += 2), 2));
    mSettingsPn.add(mLightDesc, cc.xy(2, (currentRow += 2)));
    mSettingsPn.add(mLightColorLb, cc.xy(4, currentRow));
    mSettingsPn.add(mLight, cc.xy(6, currentRow));
    mSettingsPn.add(mDarkDesc, cc.xy(2, (currentRow += 2)));
    mSettingsPn.add(mDarkColorLb, cc.xy(4, currentRow));    
    mSettingsPn.add(mDark, cc.xy(6, currentRow));
        
    mShowScrollChannelHighlight = new JCheckBox(mLocalizer.msg("activated","Activated"), Settings.propScrollToChannnelMarkingActivated.getBoolean());
    
    mScrollChannelLb = new ColorLabel(Settings.propScrollToChannelProgramsBackground.getColor());
    mScrollChannelLb.setStandardColor(Settings.propScrollToChannelProgramsBackground.getDefaultColor());
    mScrollChannelLb.setEnabled(mShowScrollChannelHighlight.isSelected());
    
    mScrollChannelColor = new ColorButton(mScrollChannelLb);
    mScrollChannelColor.setEnabled(mShowScrollChannelHighlight.isSelected());
    
    mShowScrollChannelHighlight.addItemListener(e -> {
      mScrollChannelLb.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      mScrollChannelColor.setEnabled(mScrollChannelLb.isEnabled());
    });
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("scrollToChannelTitle", "Highlight channel selected")), cc.xyw(1,
        (currentRow += 2), 8));
    mSettingsPn.add(mShowScrollChannelHighlight, cc.xy(2, (currentRow += 2)));
    mSettingsPn.add(mScrollChannelLb, cc.xy(4, currentRow));
    mSettingsPn.add(mScrollChannelColor, cc.xy(6, currentRow));
        
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("misc", "Misc")), cc.xyw(1,
        (currentRow += 2), 8));
    
    mSettingsPn.add(mTypeAsYouFind, cc.xyw(2, (currentRow += 2), 6));

    updateBackgroundStyleConfigureButton();

    return mSettingsPn;
  }
  
  private void updateBackgroundStyleConfigureButton() {
    TableBackgroundStyle style = (TableBackgroundStyle) mBackgroundStyleCB.getSelectedItem();
    
    mForegroundColorLb.setEnabled(!style.getSettingsString().equals("uiTimeBlock") && !style.getSettingsString().equals("uiColor"));
    mProgramPanelForegroundColorChangeBtn.setEnabled(mForegroundColorLb.isEnabled());
    mForegroundLabel.setEnabled(mForegroundColorLb.isEnabled());
    mForegroundSeparator.getComponent(0).setEnabled(mForegroundColorLb.isEnabled());
    
    mConfigBackgroundStyleBt.setEnabled(style.hasContent());
  }

  public static JButton createBrowseButton(final Component parent, final JTextField tf) {
    JButton bt = new JButton(mLocalizer.msg("change", "Change"));
    bt.addActionListener(event -> {
      File file = new File(tf.getText());
      JFileChooser fileChooser = new JFileChooser(file.getParent());
      String[] extArr = { ".jpg", ".jpeg", ".gif", ".png" };
      fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(extArr, ".jpg, .gif, png"));
      if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
        File selection = fileChooser.getSelectedFile();
        if (selection != null) {
          tf.setText(selection.getAbsolutePath());
        }
      }
    });

    Dimension size = bt.getPreferredSize();

    if (tf.getPreferredSize().height > size.height) {
      size.height = tf.getPreferredSize().height;
      bt.setPreferredSize(size);
    }

    return bt;
  }
  
  private void setBackgroundStyleForTimeBlockLayout() {
    if(!Settings.propTableLayout.getString().equals(Settings.LAYOUT_TIME_BLOCK) &&
        !Settings.propTableLayout.getString().equals(Settings.LAYOUT_COMPACT_TIME_BLOCK)) {
      Settings.propTableBackgroundStyle.setString("uiTimeBlock");      
      Settings.propTimeBlockShowWest.setBoolean(true);
    }
  }
  
  private void resetBackgroundStyle() {
    if(Settings.propTableLayout.getString().equals(Settings.LAYOUT_TIME_BLOCK) ||
        Settings.propTableLayout.getString().equals(Settings.LAYOUT_COMPACT_TIME_BLOCK) ||
        Settings.propTableLayout.getString().equals(Settings.LAYOUT_OPTIMIZED_COMPACT_TIME_BLOCK)) {
      Settings.propTableBackgroundStyle.setString("uiColor");
      Settings.propTimeBlockShowWest.setBoolean(false);
    }
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    String backgroundStyle = ((TableBackgroundStyle) mBackgroundStyleCB.getSelectedItem()).getSettingsString();

    Settings.propTableBackgroundStyle.setString(backgroundStyle);
    
    if (mProgramArrangementCB.getSelectedIndex() == 2 && mLastSelectedLayoutIndex != 2) {
      resetBackgroundStyle();
      Settings.propTableLayout.setString(Settings.LAYOUT_COMPACT);
    } else if (mProgramArrangementCB.getSelectedIndex() == 3 && mLastSelectedLayoutIndex != 3) {
      resetBackgroundStyle();
      Settings.propTableLayout.setString(Settings.LAYOUT_REAL_COMPACT);
    } else if (mProgramArrangementCB.getSelectedIndex() == 0 && mLastSelectedLayoutIndex != 0) {
      resetBackgroundStyle();
      Settings.propTableLayout.setString(Settings.LAYOUT_TIME_SYNCHRONOUS);
    } else if (mProgramArrangementCB.getSelectedIndex() == 4 && mLastSelectedLayoutIndex != 4) {
      setBackgroundStyleForTimeBlockLayout();
      Settings.propTableLayout.setString(Settings.LAYOUT_TIME_BLOCK);
    } else if (mProgramArrangementCB.getSelectedIndex() == 5 && mLastSelectedLayoutIndex != 5) {
      setBackgroundStyleForTimeBlockLayout();
      Settings.propTableLayout.setString(Settings.LAYOUT_COMPACT_TIME_BLOCK);
    } else if (mProgramArrangementCB.getSelectedIndex() == 6 && mLastSelectedLayoutIndex != 6) {
      setBackgroundStyleForTimeBlockLayout();
      Settings.propTableLayout.setString(Settings.LAYOUT_OPTIMIZED_COMPACT_TIME_BLOCK);
    } else if (mProgramArrangementCB.getSelectedIndex() == 1 && mLastSelectedLayoutIndex != 1){
      resetBackgroundStyle();
      Settings.propTableLayout.setString(Settings.LAYOUT_REAL_SYNCHRONOUS);
    }
    
    Settings.propColumnWidth.setInt((Integer)mColWidth.getValue());
    Settings.propProgramPanelForegroundColor.setColor(mForegroundColorLb.getColor());

    Calendar cal = Calendar.getInstance();
    cal.setTime((Date) mStartOfDayTimeSp.getValue());
    int minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Settings.propProgramTableStartOfDay.setInt(minutes);

    cal.setTime((Date) mEndOfDayTimeSp.getValue());
    minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Settings.propProgramTableEndOfDay.setInt(minutes);

    Settings.propProgramTableMouseOver.setBoolean(mMouseOverCb.isSelected());
    Settings.propProgramTableScrollHorizontal.setBoolean(mScrollHorizontalCb.isSelected());
    Settings.propProgramTableAutoChangeDate.setBoolean(mAutoChangeDate.isSelected());
    
    Settings.propProgramTableMouseOverColor.setColor(mMouseOverColorLb.getColor());
    Settings.propProgramTableCutTitle.setBoolean(mCutLongTitlesCB.isSelected());
    Settings.propProgramTableCutTitleLines
        .setInt((Integer) mCutLongTitlesSelection.getValue());
    Settings.propProgramTableMouseAutoScroll.setBoolean(mAutoScrollCb
        .isSelected());
    Settings.propProgramPanelMaxLines.setInt((Integer) mDescriptionLines
        .getValue());
    Settings.propProgramPanelShortDurationActive.setBoolean(mShortProgramsCB
        .isSelected());
    Settings.propProgramPanelShortDurationMinutes
        .setInt((Integer) mShortProgramsMinutes.getValue());
    Settings.propScrollToTimeMarkingActivated.setBoolean(mShowScrollHighlight.isSelected());
    Settings.propScrollToTimeProgramsLightBackground.setColor(mLightColorLb.getColor());
    Settings.propScrollToTimeProgramsDarkBackground.setColor(mDarkColorLb.getColor());
    
    Settings.propScrollToChannnelMarkingActivated.setBoolean(mShowScrollChannelHighlight.isSelected());
    Settings.propScrollToChannelProgramsBackground.setColor(mScrollChannelLb.getColor());
    
    Settings.propTypeAsYouFindEnabled.setBoolean(mTypeAsYouFind.isSelected());
    
    MainFrame.getInstance().getProgramTableScrollPane().getProgramTable().clearTimeMarkings();
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("title", "Program table");
  }

  private TableBackgroundStyle[] getTableBackgroundStyles() {

    return new TableBackgroundStyle[] { new SingleColorBackgroundStyle(), new UiColorBackgroundStyle(), 
        new SingleImageBackgroundStyle(), new TimeBlockBackgroundStyle(), new UiTimeBlockBackgroundStyle(),
        new DayTimeBackgroundStyle() };

  }

  private static class ConfigureBackgroundStyleDialog {

    private JDialog mDialog;

    private TableBackgroundStyle mStyle;

    public ConfigureBackgroundStyleDialog(Component parent, TableBackgroundStyle style) {
      mStyle = style;

      mDialog = UiUtilities.createDialog(parent, true);
      mDialog.setTitle(mLocalizer.msg("configureBackgroundStyleDialogTitle", "Configure background style '{0}'", style
          .getName()));

      JPanel dialogContent = (JPanel) mDialog.getContentPane();
      dialogContent.setBorder(new EmptyBorder(10, 10, 11, 11));
      dialogContent.setLayout(new BorderLayout(0, 15));

      JPanel content = new JPanel(new BorderLayout());

      content.add(style.createSettingsContent(), BorderLayout.NORTH);
      dialogContent.add(content, BorderLayout.CENTER);

      JPanel buttonPn = new JPanel(new BorderLayout());
      JPanel pn = new JPanel();
      pn.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));

      JButton okBtn = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
      JButton cancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
      pn.add(okBtn);
      pn.add(cancelBtn);

      okBtn.addActionListener(e -> {
        mStyle.storeSettings();
        mDialog.setVisible(false);
      });

      cancelBtn.addActionListener(e -> {
        mDialog.setVisible(false);
      });

      buttonPn.add(pn, BorderLayout.EAST);
      dialogContent.add(buttonPn, BorderLayout.SOUTH);
      mDialog.pack();

    }

    public void show() {
      UiUtilities.centerAndShow(mDialog);
    }

  }
  
  
  private class TwoSpinnerDateModel extends SpinnerDateModel {
    private JSpinner mMeSpinner;
    
    protected void setMe(JSpinner me) {
      mMeSpinner = me;
    }
    
    public void setValue(Object value) {
      correctValues((Date)value);
      
      super.setValue(value);
    }
    
    public Object getPreviousValue() {
      Date d = (Date)super.getPreviousValue();
      correctValues(d);
      
      return d;
    }
    
    public Object getNextValue() {
      Date d = (Date)super.getNextValue();
      correctValues(d);
      
      return d;
    }
    
    private void correctValues(Date d) {
      if(mMeSpinner != null && mStartOfDayTimeSp != null && mEndOfDayTimeSp != null) {
        Calendar cal = Calendar.getInstance();
        int endTime, startTime;
        
        if(mMeSpinner.equals(mStartOfDayTimeSp)) {
          cal.setTime((Date)mEndOfDayTimeSp.getValue());
          endTime = cal.get(Calendar.HOUR_OF_DAY) * 60
              + cal.get(Calendar.MINUTE);
          
          cal.setTime(d);
          startTime = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
          
          if(endTime - startTime < -1) {
            mEndOfDayTimeSp.setValue(d);
          }
        }
        else {
          cal.setTime(d);
          endTime = cal.get(Calendar.HOUR_OF_DAY) * 60
              + cal.get(Calendar.MINUTE);
          
          cal.setTime((Date)mStartOfDayTimeSp.getValue());
          startTime = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
          
          if(endTime - startTime < -1) {
            mStartOfDayTimeSp.setValue(d);
          }
        }
      }
    }
  }
}