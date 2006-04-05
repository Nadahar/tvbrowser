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

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The extended settings for the program showing in tray.
 * 
 * @author René Mach
 * 
 */
public class TrayExtendedSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
  .getLocalizerFor(TrayExtendedSettingsTab.class);
  
  private JCheckBox  
  mShowImportantDateChb, 
 mShowChannelTooltipChb,
 mShowNowRunningTimeChb,
 
 
 mShowTimeProgramsTimeChb,

 mShowImportantTimeChb;


private JRadioButton mShowNowRunningNotSubChb, mShowNowRunningSubChb,
 mShowImportantNotSubChb, mShowImportantSubChb;
  
private ColorLabel mTimeProgramLightColorLb,mTimeProgramDarkColorLb;

  public JPanel createSettingsPanel() {
    PanelBuilder pb = new PanelBuilder(new FormLayout(
        "5dlu,pref:grow,default,5dlu,default,5dlu",
        "pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, pref, pref, 10dlu, " +
        "pref, 5dlu, pref, pref, pref, pref, 10dlu, pref, 5dlu, pref, " +
        "pref"));
    CellConstraints cc = new CellConstraints();

    pb.setDefaultDialogBorder();
    
    boolean enabled = (Settings.propShowNowRunningProgramsInTray.getBoolean()
        || Settings.propShowImportantProgramsInTray.getBoolean() ||
        Settings.propShowTimeProgramsInTray.getBoolean()) 
                      && Settings.propTrayIsEnabled.getBoolean();
    
    mShowChannelTooltipChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showToolTip",
        "Show additional information of the program in a tool tip"), Settings.propProgramsInTrayShowTooltip.getBoolean());
    mShowChannelTooltipChb.setToolTipText(mLocalizer.msg(
        "programShowing.toolTipTip",
        "Tool tips are small helper to something, like this one."));
    
    mShowChannelTooltipChb.setEnabled(enabled);
    
    pb.addSeparator(mLocalizer.msg("programShowing.extendedMain",
    "Program showing - Basic"), cc.xyw(1, 1, 6)).getComponent(0).setEnabled(enabled);

    boolean checked = Settings.propShowImportantProgramsInTrayInSubMenu.getBoolean();
    mShowImportantSubChb = new JRadioButton(mLocalizer.msg(
        "programShowing.importantSubMenu",
        "Show important programs in a sub menu"), checked);
    mShowImportantNotSubChb = new JRadioButton(mLocalizer.msg(
        "programShowing.importantNotSub",
        "Show important programs direct in the tray menu"), !checked);    
    
    ButtonGroup bgImportant = new ButtonGroup();
    bgImportant.add(mShowImportantSubChb);
    bgImportant.add(mShowImportantNotSubChb);
    
    mShowImportantTimeChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showStartTime", "Show start time"), Settings.propImportantProgramsInTrayContainsStartTime
        .getBoolean());
    
    mTimeProgramLightColorLb = new ColorLabel(Settings.propTimeProgramsLightBackground.getColor());
    mTimeProgramLightColorLb.setStandardColor(Settings.propTimeProgramsLightBackground.getDefaultColor());
    mTimeProgramDarkColorLb = new ColorLabel(Settings.propTimeProgramsDarkBackground.getColor());
    mTimeProgramDarkColorLb.setStandardColor(Settings.propTimeProgramsDarkBackground.getDefaultColor());

    mShowImportantDateChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showDate", "Show date"), Settings.propImportantProgramsInTrayContainsDate.getBoolean());
    
    checked = Settings.propShowNowRunningProgramsInTrayInSubMenu.getBoolean() && Settings.propTrayIsEnabled.getBoolean();
    mShowNowRunningSubChb = new JRadioButton(mLocalizer.msg(
        "programShowing.runningSubMenu",
        "Show now running programs in a sub menu"), checked);
    mShowNowRunningNotSubChb = new JRadioButton(mLocalizer.msg(
        "programShowing.runningNotSub",
        "Show now running programs direct in the tray menu"), !checked);

    ButtonGroup bgNow = new ButtonGroup();
    bgNow.add(mShowNowRunningSubChb);
    bgNow.add(mShowNowRunningNotSubChb);

    mShowNowRunningTimeChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showStartTime", "Show start time"), Settings.propNowRunningProgramsInTrayContainsStartTime
        .getBoolean());

    mShowTimeProgramsTimeChb = new JCheckBox(mLocalizer.msg(
    "programShowing.showStartTime", "Show start time"), Settings.propTimeProgramsInTrayContainsTime
    .getBoolean());
    
    enabled = Settings.propShowNowRunningProgramsInTray.getBoolean() && Settings.propTrayIsEnabled.getBoolean();
    
    pb.add(mShowChannelTooltipChb, cc.xyw(2, 3, 4));

    JComponent c = pb.addSeparator(mLocalizer.msg("programShowing.extendedNow",
        "Program showing - Now running programs"), cc.xyw(1, 5, 6));
    c.getComponent(0).setEnabled(enabled);

    pb.add(mShowNowRunningTimeChb, cc.xyw(2, 7, 4));
    pb.add(mShowNowRunningNotSubChb, cc.xyw(2, 8, 4));
    pb.add(mShowNowRunningSubChb, cc.xyw(2, 9, 4));
    mShowNowRunningTimeChb.setEnabled(enabled);
    mShowNowRunningNotSubChb.setEnabled(enabled);
    mShowNowRunningSubChb.setEnabled(enabled);

    enabled = Settings.propShowImportantProgramsInTray.getBoolean() && Settings.propTrayIsEnabled.getBoolean();
    
    c = pb.addSeparator(mLocalizer.msg("programShowing.extendedImportant",
        "Program showing - Important programs"), cc.xyw(1, 11, 6));
    c.getComponent(0).setEnabled(enabled);
    
    pb.add(mShowImportantDateChb, cc.xyw(2, 13, 4));
    pb.add(mShowImportantTimeChb, cc.xyw(2, 14, 4));
    pb.add(mShowImportantNotSubChb, cc.xyw(2, 15, 4));
    pb.add(mShowImportantSubChb, cc.xyw(2, 16, 4));
    mShowImportantDateChb.setEnabled(enabled);
    mShowImportantTimeChb.setEnabled(enabled);
    mShowImportantNotSubChb.setEnabled(enabled);
    mShowImportantSubChb.setEnabled(enabled);
    
    ColorButton light = new ColorButton(mTimeProgramLightColorLb);
    ColorButton dark = new ColorButton(mTimeProgramDarkColorLb);
    
    enabled = Settings.propShowTimeProgramsInTray.getBoolean() && Settings.propTrayIsEnabled.getBoolean();
    
    light.setEnabled(enabled);
    dark.setEnabled(enabled);
    mTimeProgramLightColorLb.setEnabled(enabled);
    mTimeProgramDarkColorLb.setEnabled(enabled);
    mShowTimeProgramsTimeChb.setEnabled(enabled);
    
    c = pb.addSeparator(mLocalizer.msg("programShowing.extendedTime",
    "Program showing - Programs at..."), cc.xyw(1, 18, 6));
    c.getComponent(0).setEnabled(enabled);
    
    PanelBuilder colors = new PanelBuilder(new FormLayout(
        "default,5dlu,default,5dlu,default", "pref,2dlu,pref"));    
    
    colors.addLabel(
        mLocalizer.msg("programShowing.timeLight",
            "Background color of the programs at..."), cc.xy(1, 1)).setEnabled(enabled);
    colors.add(mTimeProgramLightColorLb, cc.xy(3, 1));
    colors.add(light,cc.xy(5, 1));

    colors.addLabel(
        mLocalizer.msg("programShowing.timeDark",
            "Progress color of the programs at..."), cc.xy(1, 3)).setEnabled(enabled);
    colors.add(mTimeProgramDarkColorLb, cc.xy(3, 3));
    colors.add(dark,cc.xy(5, 3));

    pb.add(mShowTimeProgramsTimeChb, cc.xyw(2,20,4));
    pb.add(colors.getPanel(), cc.xyw(2, 21, 4));    
    
    return pb.getPanel();
  }

  public void saveSettings() {
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

    if (mShowChannelTooltipChb != null)
      Settings.propProgramsInTrayShowTooltip.setBoolean(mShowChannelTooltipChb
          .isSelected());
    if (mShowTimeProgramsTimeChb != null)
      Settings.propTimeProgramsInTrayContainsTime.setBoolean(mShowTimeProgramsTimeChb.isSelected());
    if (mTimeProgramLightColorLb != null)
      Settings.propTimeProgramsLightBackground.setColor(mTimeProgramLightColorLb.getColor());
    if (mTimeProgramDarkColorLb != null)
      Settings.propTimeProgramsDarkBackground.setColor(mTimeProgramDarkColorLb.getColor());
    
    Settings.propShowProgramsInTrayWasConfigured.setBoolean(true);
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("programShowing.extendedSettings","Extended Tray settings");
  }

}
