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

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import util.ui.OrderChooser;
import devplugin.Channel;
import devplugin.SettingsTab;

/**
 * Settings for the Tray-Icon
 *  
 * @author bodum
 */
public class TraySettingsTab implements SettingsTab {
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(TraySettingsTab.class);
  /** Checkboxes */
  private JCheckBox mOnlyMinimizeWhenWindowClosingChB, mMinimizeToTrayChb, mSingeClickTrayChb,
  mShowNowRunningChb, mShowImportantChb, mShowNowRunningSubChb, mShowImportantSubChb,
  mShowNowRunningTimeChb, mShowImportantTimeChb, mShowChannelNameChb, mShowChannelIconChb,
  mShowChannelTooltipChb, mShowProgramsChb;

  private JSpinner mImportantSize,mImportantHours;
  
  private OrderChooser mChannelOCh;
  /**
   * Create the Settings-Dialog
   */
  public JPanel createSettingsPanel() {
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu, 10dlu, pref, 5dlu, pref, 5dlu, pref:grow, 15dlu",
        
        "pref, 5dlu, pref, pref, pref, 10dlu, pref, 5dlu, pref, 5dlu, " +
        "pref, pref, pref, 10dlu, pref, pref, pref, 2dlu, pref, pref, " +
        "10dlu, pref, pref, pref, 5dlu, pref, 2dlu, top:default"
        ));
    builder.setDefaultDialogBorder();
    CellConstraints cc = new CellConstraints();
        
    String msg = mLocalizer.msg("minimizeToTray", "Minimize to Tray");
    boolean checked = Settings.propMinimizeToTray.getBoolean();
    mMinimizeToTrayChb = new JCheckBox(msg, checked);
    
    msg = mLocalizer.msg("onlyMinimizeWhenWindowClosing", "When closing the main window only minimize TV-Browser, don't quit.");
    checked = Settings.propOnlyMinimizeWhenWindowClosing.getBoolean();
    mOnlyMinimizeWhenWindowClosingChB = new JCheckBox(msg, checked);

    checked = Settings.propUseSingeClickInTray.getBoolean();
    mSingeClickTrayChb = new JCheckBox(mLocalizer.msg("useSingleClick","Use single Click in Tray to hide and show window"), checked);
    
    boolean enabled = Settings.propShowProgramsInTrayEnabled.getBoolean();
    mShowProgramsChb = new JCheckBox(mLocalizer.msg("programShowing.enabled","Program showing enabled"), enabled);
    
    checked = Settings.propProgramsInTrayContainsChannel.getBoolean();
    mShowChannelNameChb = new JCheckBox(mLocalizer.msg("programShowing.showChannelName","Show channel name"), checked);
    
    checked = Settings.propProgramsInTrayContainsChannelIcon.getBoolean();
    mShowChannelIconChb = new JCheckBox(mLocalizer.msg("programShowing.showChannelIcons","Show channel icon"), checked);
    
    checked = Settings.propProgramsInTrayShowTooltip.getBoolean();
    mShowChannelTooltipChb = new JCheckBox(mLocalizer.msg("programShowing.showToolTip","Show additional information of the program in a tool tip"), checked);
    mShowChannelTooltipChb.setToolTipText(mLocalizer.msg("programShowing.toolTipTip","Tool tips are small helper to something, like this one."));
    
    checked = Settings.propShowImportantProgramsInTray.getBoolean();
    mShowImportantChb = new JCheckBox(mLocalizer.msg("programShowing.showImportant","Show important programs"), checked);
    mShowImportantChb.setToolTipText(mLocalizer.msg("programShowing.toolTipImportant","<html>Important programs are all marked<br>programs in the time range.<html>"));
    
    final JLabel sizeLabel1 = new JLabel(mLocalizer.msg("programShowing.importantMaxNumber","Show a maximum of"));
    final JLabel sizeLabel2 = new JLabel(mLocalizer.msg("programShowing.importantMaxPrograms","important programs"));
    mImportantSize = new JSpinner(new SpinnerNumberModel(Settings.propImportantProgramsInTraySize.getInt(),1,10,1));
    
    final JLabel timeLabel1 = new JLabel(mLocalizer.msg("programShowing.importantTimeRange","Search through the next"));
    final JLabel timeLabel2 = new JLabel(mLocalizer.msg("programShowing.importantHours","hours for important programs"));
    mImportantHours = new JSpinner(new SpinnerNumberModel(Settings.propImportantProgramsInTrayHours.getInt(),1,6,1));
    
    checked = Settings.propShowImportantProgramsInTrayInSubMenu.getBoolean();
    mShowImportantSubChb = new JCheckBox(mLocalizer.msg("programShowing.importantSubMenu","Group the important programs in a submenu"), checked);
    
    checked = Settings.propImportantProgramsInTrayContainsStartTime.getBoolean();
    mShowImportantTimeChb = new JCheckBox(mLocalizer.msg("programShowing.showStartTime","Show start time"), checked);
    
    checked = Settings.propShowNowRunningProgramsInTray.getBoolean();
    mShowNowRunningChb = new JCheckBox(mLocalizer.msg("programShowing.showRunning","Show now running programs"), checked);
    
    checked = Settings.propShowNowRunningProgramsInTrayInSubMenu.getBoolean();
    mShowNowRunningSubChb = new JCheckBox(mLocalizer.msg("programShowing.runningSubMenu","Group the now running programs in a submenu"), checked);

    checked = Settings.propNowRunningProgramsInTrayContainsStartTime.getBoolean();
    mShowNowRunningTimeChb = new JCheckBox(mLocalizer.msg("programShowing.showStartTime","Show start time"), checked);
    
    mChannelOCh = new OrderChooser(Settings.propNowRunningProgramsInTrayChannels.getChannelArray(false),Settings.propSubscribedChannels.getChannelArray(false), true);
    
    
    builder.addSeparator(mLocalizer.msg("basics","Basic settings"),cc.xyw(1,1,8));
    builder.add(mMinimizeToTrayChb, cc.xyw(2,3,6));
    builder.add(mOnlyMinimizeWhenWindowClosingChB, cc.xyw(2,4,6));
    builder.addSeparator(mLocalizer.msg("programShowing","Program showing"), cc.xyw(1,7,8));
    builder.add(mSingeClickTrayChb, cc.xyw(2,5,6));
    builder.add(mShowProgramsChb, cc.xyw(2,9,6));
    builder.add(mShowChannelNameChb, cc.xyw(2,11,6));
    builder.add(mShowChannelIconChb, cc.xyw(2,12,6));
    builder.add(mShowChannelTooltipChb, cc.xyw(2,13,6));
   
    builder.add(mShowImportantChb, cc.xyw(2,15,6));

    builder.add(sizeLabel1, cc.xy(3,16));
    builder.add(mImportantSize, cc.xy(5,16));
    builder.add(sizeLabel2, cc.xy(7,16));
    
    builder.add(timeLabel1, cc.xy(3,17));
    builder.add(mImportantHours, cc.xy(5,17));
    builder.add(timeLabel2, cc.xy(7,17));
    
    builder.add(mShowImportantSubChb, cc.xyw(3,19,5));
    builder.add(mShowImportantTimeChb, cc.xyw(3,20,5));
    
    builder.add(mShowNowRunningChb, cc.xyw(2,22,6));
    builder.add(mShowNowRunningSubChb, cc.xyw(3,23,5));
    builder.add(mShowNowRunningTimeChb, cc.xyw(3,24,5));
    
    builder.addSeparator(mLocalizer.msg("programShowing.runningChannels",
        "Which channels should be used for showing now running programs?"), cc.xyw(2,26,6));
    builder.add(mChannelOCh, cc.xyw(2,28,6));
    
    mShowProgramsChb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mShowChannelNameChb.setEnabled(mShowProgramsChb.isSelected());
        mShowChannelIconChb.setEnabled(mShowProgramsChb.isSelected());
        mShowChannelTooltipChb.setEnabled(mShowProgramsChb.isSelected());
        mShowImportantChb.setEnabled(mShowProgramsChb.isSelected());
        mImportantSize.setEnabled(mShowProgramsChb.isSelected());
        mImportantHours.setEnabled(mShowProgramsChb.isSelected());
        mShowImportantSubChb.setEnabled(mShowProgramsChb.isSelected());
        mShowImportantTimeChb.setEnabled(mShowProgramsChb.isSelected());
        mShowNowRunningChb.setEnabled(mShowProgramsChb.isSelected());
        mShowNowRunningSubChb.setEnabled(mShowProgramsChb.isSelected());
        mShowNowRunningTimeChb.setEnabled(mShowProgramsChb.isSelected());
        mChannelOCh.setEnabled(mShowProgramsChb.isSelected());
        sizeLabel1.setEnabled(mShowProgramsChb.isSelected());
        sizeLabel2.setEnabled(mShowProgramsChb.isSelected());
        timeLabel1.setEnabled(mShowProgramsChb.isSelected());
        timeLabel2.setEnabled(mShowProgramsChb.isSelected());
      }
    });
    
    mShowProgramsChb.getChangeListeners()[0].stateChanged(new ChangeEvent(mShowProgramsChb));
    
    return builder.getPanel();
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

    if (mSingeClickTrayChb != null) {
      boolean checked = mSingeClickTrayChb.isSelected();
      Settings.propUseSingeClickInTray.setBoolean(checked);
    }
    
    if(mShowNowRunningChb != null)
      Settings.propShowNowRunningProgramsInTray.setBoolean(mShowNowRunningChb.isSelected());
    if(mShowImportantChb != null)
      Settings.propShowImportantProgramsInTray.setBoolean(mShowImportantChb.isSelected());
    if(mShowNowRunningSubChb != null)
      Settings.propShowNowRunningProgramsInTrayInSubMenu.setBoolean(mShowNowRunningSubChb.isSelected());
    if(mShowImportantSubChb != null)
      Settings.propShowImportantProgramsInTrayInSubMenu.setBoolean(mShowImportantSubChb.isSelected());
    if(mShowNowRunningTimeChb != null)
      Settings.propNowRunningProgramsInTrayContainsStartTime.setBoolean(mShowNowRunningTimeChb.isSelected());
    if(mShowImportantTimeChb != null)
      Settings.propImportantProgramsInTrayContainsStartTime.setBoolean(mShowImportantTimeChb.isSelected());
    if(mShowChannelNameChb != null)
      Settings.propProgramsInTrayContainsChannel.setBoolean(mShowChannelNameChb.isSelected());
    if(mShowChannelIconChb != null)
      Settings.propProgramsInTrayContainsChannelIcon.setBoolean(mShowChannelIconChb.isSelected());
    if(mShowChannelTooltipChb != null)
      Settings.propProgramsInTrayShowTooltip.setBoolean(mShowChannelTooltipChb.isSelected());
    if(mShowProgramsChb != null)
      Settings.propShowProgramsInTrayEnabled.setBoolean(mShowProgramsChb.isSelected());
    
    
    Object[] order = mChannelOCh.getOrder();
    Channel[] ch = new Channel[order.length];
    
    for(int i = 0; i < ch.length; i++)
      ch[i] = (Channel)order[i];
    
    if(order != null)
    Settings.propNowRunningProgramsInTrayChannels.setChannelArray(ch);
    
    Settings.propImportantProgramsInTraySize.setInt(((Integer)mImportantSize.getValue()).intValue());
    Settings.propImportantProgramsInTrayHours.setInt(((Integer)mImportantHours.getValue()).intValue());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("tray","Tray");
  }
}
