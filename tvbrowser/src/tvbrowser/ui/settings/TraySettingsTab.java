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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
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
  private JCheckBox mOnlyMinimizeWhenWindowClosingChB, mMinimizeToTrayChb,
  mShowNowRunningChb, mShowImportantChb, mShowNowRunningSubChb, mShowImportantSubChb,
  mShowNowRunningTimeChb, mShowImportantTimeChb, mShowChannelNameChb, mShowChannelIconChb,
  mShowChannelTooltipChb, mShowSoonChb;

  private JButton mAdditional;
  
  private JSpinner mImportantSize,mImportantHours;
  private JLabel mSizeLabel1,mSizeLabel2,mTimeLabel1,mTimeLabel2;
  private OrderChooser mChannelOCh;
  
  /**
   * Create the Settings-Dialog
   */
  public JPanel createSettingsPanel() {
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu, 10dlu, pref:grow, 25dlu, pref, 5dlu",
        
        "pref, 5dlu, pref, pref, 10dlu, pref, 5dlu, pref, pref, pref, " +
        "pref, pref, pref, 10dlu, pref, pref, 5dlu, pref, 10dlu, pref"
        ));
    builder.setDefaultDialogBorder();
    CellConstraints cc = new CellConstraints();
        
    String msg = mLocalizer.msg("minimizeToTray", "Minimize to Tray");
    boolean checked = Settings.propMinimizeToTray.getBoolean();
    mMinimizeToTrayChb = new JCheckBox(msg, checked);
    
    msg = mLocalizer.msg("onlyMinimizeWhenWindowClosing", "When closing the main window only minimize TV-Browser, don't quit.");
    checked = Settings.propOnlyMinimizeWhenWindowClosing.getBoolean();
    mOnlyMinimizeWhenWindowClosingChB = new JCheckBox(msg, checked);
    
    
    checked = Settings.propProgramsInTrayContainsChannel.getBoolean();
    mShowChannelNameChb = new JCheckBox(mLocalizer.msg("programShowing.showChannelName","Show channel name"), checked);
    
    checked = Settings.propProgramsInTrayContainsChannelIcon.getBoolean();
    mShowChannelIconChb = new JCheckBox(mLocalizer.msg("programShowing.showChannelIcons","Show channel icon"), checked);
    

    
    checked = Settings.propShowImportantProgramsInTray.getBoolean();
    mShowImportantChb = new JCheckBox(mLocalizer.msg("programShowing.showImportant","Show important programs"), checked);
    mShowImportantChb.setToolTipText(mLocalizer.msg("programShowing.toolTipImportant","<html>Important programs are all marked<br>programs in the time range.<html>"));

    
    checked = Settings.propShowSoonProgramsInTray.getBoolean();
    mShowSoonChb = new JCheckBox(mLocalizer.msg("programShowing.showSoon","Program showing enabled"), checked);


    

    
    
    
    
    mSizeLabel2 = new JLabel(mLocalizer.msg("programShowing.importantMaxPrograms","important programs"));
    mImportantSize = new JSpinner(new SpinnerNumberModel(Settings.propImportantProgramsInTraySize.getInt(),1,10,1));
    
    PanelBuilder b2 = new PanelBuilder(new FormLayout("pref,3dlu,pref","pref"));
    
    b2.add(mImportantSize, cc.xy(1,1));
    b2.add(mSizeLabel2, cc.xy(3,1));
    mShowSoonChb.add(b2.getPanel());
    
    checked = Settings.propProgramsInTrayShowTooltip.getBoolean();
    mShowChannelTooltipChb = new JCheckBox(mLocalizer.msg("programShowing.showToolTip","Show additional information of the program in a tool tip"), checked);
    mShowChannelTooltipChb.setToolTipText(mLocalizer.msg("programShowing.toolTipTip","Tool tips are small helper to something, like this one."));
    
    
    
    mTimeLabel1 = new JLabel(mLocalizer.msg("programShowing.importantTimeRange","Search through the next"));
    mTimeLabel2 = new JLabel(mLocalizer.msg("programShowing.importantHours","hours for important programs"));
    mImportantHours = new JSpinner(new SpinnerNumberModel(Settings.propImportantProgramsInTrayHours.getInt(),1,6,1));
    
    
    
    
    checked = Settings.propShowImportantProgramsInTrayInSubMenu.getBoolean();
    mShowImportantSubChb = new JCheckBox("<html>"+mLocalizer.msg("programShowing.importantSubMenu","Group the important programs in a submenu"+"<html>"), checked);
    
    checked = Settings.propImportantProgramsInTrayContainsStartTime.getBoolean();
    mShowImportantTimeChb = new JCheckBox(mLocalizer.msg("programShowing.showStartTime","Show start time"), checked);
    mShowImportantSubChb.setVerticalTextPosition(JCheckBox.TOP);
    mShowImportantSubChb.setVerticalAlignment(JCheckBox.TOP);
    mShowImportantSubChb.setHorizontalTextPosition(JCheckBox.RIGHT);
    
    checked= Settings.propShowNowRunningProgramsInTray.getBoolean();
    mShowNowRunningChb = new JCheckBox(mLocalizer.msg("programShowing.showRunning","Show now running programs"), checked);
    
    checked = Settings.propShowNowRunningProgramsInTrayInSubMenu.getBoolean();
    mShowNowRunningSubChb = new JCheckBox(mLocalizer.msg("programShowing.runningSubMenu","Group the now running programs in a submenu"), checked);

    checked = Settings.propNowRunningProgramsInTrayContainsStartTime.getBoolean();
    mShowNowRunningTimeChb = new JCheckBox(mLocalizer.msg("programShowing.showStartTime","Show start time"), checked);
    
    mChannelOCh = new OrderChooser(Settings.propNowRunningProgramsInTrayChannels.getChannelArray(false),Settings.propSubscribedChannels.getChannelArray(false), true);
    
    builder.addSeparator(mLocalizer.msg("basics","Basic settings"),cc.xyw(1,1,6));
    builder.add(mMinimizeToTrayChb, cc.xyw(2,3,4));
    builder.add(mOnlyMinimizeWhenWindowClosingChB, cc.xyw(2,4,4));
    builder.addSeparator(mLocalizer.msg("programShowing","Program showing"), cc.xyw(1,6,6));
    
    builder.add(mShowChannelNameChb, cc.xyw(2,8,4));
    builder.add(mShowChannelIconChb, cc.xyw(2,9,4));
    
    builder.add(mShowNowRunningChb, cc.xyw(2,10,4));
    builder.add(mShowSoonChb, cc.xyw(2,11,5));
    builder.add(mShowImportantChb, cc.xyw(2,12,2));
    builder.add(b2.getPanel(), cc.xy(5,12));
       
    final JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("programShowing.runningChannels",
    "Which channels should be used for showing now running programs?"), cc.xyw(2,15,4));
    builder.add(mChannelOCh, cc.xyw(2,16,4));

    
    mSizeLabel1 =(JLabel)c.getComponent(0);
    
    mAdditional = new JButton("Erweiterte Einstellungen");
    
    builder.add(new JSeparator(), cc.xyw(1,18,6));
    builder.add(mAdditional, cc.xy(5,20));
    
    
    
    //builder.add(mShowChannelTooltipChb, cc.xyw(2,13,6));
   
    

  /*  builder.add(mSizeLabel1, cc.xy(3,16));
    builder.add(mImportantSize, cc.xy(5,16));
    builder.add(mSizeLabel2, cc.xy(7,16));
    
    builder.add(mTimeLabel1, cc.xy(3,17));
    builder.add(mImportantHours, cc.xy(5,17));
    builder.add(mTimeLabel2, cc.xy(7,17));
    
    builder.add(mShowImportantSubChb, cc.xyw(3,19,4));
    builder.add(mShowImportantTimeChb, cc.xyw(3,20,5));
    
    
    builder.add(mShowNowRunningSubChb, cc.xyw(3,23,5));
    builder.add(mShowNowRunningTimeChb, cc.xyw(3,24,5));
    builder.add(new JSeparator(JSeparator.VERTICAL), cc.xywh(8,9,1,14));*/
    
    
    mShowImportantChb.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        selectEnabled();
        mImportantSize.setEnabled(mShowImportantChb.isSelected());
        mSizeLabel2.setEnabled(mShowImportantChb.isSelected());
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
    
    mShowSoonChb.getChangeListeners()[0].stateChanged(new ChangeEvent(mShowSoonChb));
    mShowImportantChb.getChangeListeners()[0].stateChanged(new ChangeEvent(mShowImportantChb));
    mShowNowRunningChb.getChangeListeners()[0].stateChanged(new ChangeEvent(mShowNowRunningChb));
    
    return builder.getPanel();
  }
  
  private void selectEnabled() {    
      boolean enabled = mShowSoonChb.isSelected() ||
      mShowNowRunningChb.isSelected() ||
      mShowImportantChb.isSelected();
      
      mSizeLabel1.setEnabled(enabled);
      mShowChannelNameChb.setEnabled(enabled);
      mShowChannelIconChb.setEnabled(enabled);
      //mShowChannelTooltipChb.setEnabled(mShowSoonChb.isSelected());
      //mShowImportantChb.setEnabled(mShowSoonChb.isSelected());
      //selectImportantState();
      //mShowNowRunningChb.setEnabled(mShowSoonChb.isSelected());
      //selectNowRunningState();
      mChannelOCh.setEnabled(enabled);
      mAdditional.setEnabled(enabled);
  }
  
  private void selectImportantState() {
  /*  mImportantSize.setEnabled(mShowSoonChb.isSelected() && mShowImportantChb.isSelected());
    mImportantHours.setEnabled(mShowSoonChb.isSelected() && mShowImportantChb.isSelected());
    mShowImportantSubChb.setEnabled(mShowSoonChb.isSelected() && mShowImportantChb.isSelected());
    mShowImportantTimeChb.setEnabled(mShowSoonChb.isSelected() && mShowImportantChb.isSelected());
    mSizeLabel1.setEnabled(mShowSoonChb.isSelected() && mShowImportantChb.isSelected());
    mSizeLabel2.setEnabled(mShowSoonChb.isSelected() && mShowImportantChb.isSelected());
    mTimeLabel1.setEnabled(mShowSoonChb.isSelected() && mShowImportantChb.isSelected());
    mTimeLabel2.setEnabled(mShowSoonChb.isSelected() && mShowImportantChb.isSelected());*/

  }
  
  private void selectNowRunningState() {
   /* mShowNowRunningSubChb.setEnabled(mShowSoonChb.isSelected() && mShowNowRunningChb.isSelected());
    mShowNowRunningTimeChb.setEnabled(mShowSoonChb.isSelected() && mShowNowRunningChb.isSelected());*/
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
    if(mShowSoonChb != null)
      Settings.propShowSoonProgramsInTray.setBoolean(mShowSoonChb.isSelected());
    
    
    Object[] order = mChannelOCh.getOrder();
    Channel[] ch = new Channel[order.length];
    
    for(int i = 0; i < ch.length; i++)
      ch[i] = (Channel)order[i];
    
    if(order != null)
    Settings.propNowRunningProgramsInTrayChannels.setChannelArray(ch);
    
    Settings.propImportantProgramsInTraySize.setInt(((Integer)mImportantSize.getValue()).intValue());
    Settings.propImportantProgramsInTrayHours.setInt(((Integer)mImportantHours.getValue()).intValue());
    Settings.propShowProgramsInTrayWasConfigured.setBoolean(true);
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("tray","Tray");
  }
}
