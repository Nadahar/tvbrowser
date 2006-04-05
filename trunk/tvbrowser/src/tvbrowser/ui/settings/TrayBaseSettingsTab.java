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

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The base settings for the tray.
 * 
 * @author René Mach
 *
 */
public class TrayBaseSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
  .getLocalizerFor(TrayBaseSettingsTab.class);
  
  private JCheckBox mTrayIsEnabled, mMinimizeToTrayChb, mOnlyMinimizeWhenWindowClosingChB,
  mShowChannelNameChb, mShowChannelIconChb, mShowNowRunningChb, mShowTimeProgramsChb, 
  
  mShowImportantChb;
  
  private JSpinner mImportantSize;
  private JLabel mSizeLabel;

  private boolean mOldState; 
    
  public JPanel createSettingsPanel() {
    
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu, pref:grow, 25dlu, pref, 5dlu",        
        "pref, 5dlu, pref, pref, pref, 10dlu, pref, 5dlu, pref, pref," +
        "pref, pref, pref"));
    builder.setDefaultDialogBorder();
    CellConstraints cc = new CellConstraints();
    
    String msg = mLocalizer.msg("trayIsEnabled", "Tray activated");
    mOldState = Settings.propTrayIsEnabled.getBoolean();
    mTrayIsEnabled = new JCheckBox(msg, mOldState);

    msg = mLocalizer.msg("minimizeToTray", "Minimize to Tray");
    boolean checked = Settings.propMinimizeToTray.getBoolean();
    mMinimizeToTrayChb = new JCheckBox(msg, checked && mOldState);
    
    msg = mLocalizer.msg("onlyMinimizeWhenWindowClosing",
        "When closing the main window only minimize TV-Browser, don't quit.");
    checked = Settings.propOnlyMinimizeWhenWindowClosing.getBoolean() && mOldState;
    mOnlyMinimizeWhenWindowClosingChB = new JCheckBox(msg, checked); 
    
    mShowChannelNameChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showChannelName", "Show channel name"), Settings.propProgramsInTrayContainsChannel.getBoolean());
    mShowChannelIconChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showChannelIcons", "Show channel icon"), Settings.propProgramsInTrayContainsChannelIcon.getBoolean());
    mShowNowRunningChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showRunning", "Show now running programs"), Settings.propShowNowRunningProgramsInTray.getBoolean());    
    mShowTimeProgramsChb = new JCheckBox(mLocalizer.msg("programShowing.showProgramsAt",
        "Show programs at..."), Settings.propShowTimeProgramsInTray.getBoolean());
    mShowImportantChb = new JCheckBox(mLocalizer.msg(
        "programShowing.showImportant", "Show important programs"), Settings.propShowImportantProgramsInTray.getBoolean());
    mShowImportantChb
        .setToolTipText(mLocalizer
            .msg("programShowing.toolTipImportant",
                "<html>Important programs are all marked<br>programs in the time range.<html>"));  

    mSizeLabel = new JLabel(mLocalizer.msg(
        "programShowing.importantMaxPrograms", "important programs to show"));
    mImportantSize = new JSpinner(new SpinnerNumberModel(
        Settings.propImportantProgramsInTraySize.getInt(), 1, 10, 1));

    PanelBuilder b2 = new PanelBuilder(new FormLayout("pref,3dlu,pref", "pref"));

    b2.add(mImportantSize, cc.xy(1, 1));
    b2.add(mSizeLabel, cc.xy(3, 1));  
    
    builder.addSeparator(mLocalizer.msg("basics", "Basic settings"), cc.xyw(1,1,5));    
    builder.add(mTrayIsEnabled, cc.xyw(2,3,3));
    builder.add(mMinimizeToTrayChb, cc.xyw(2,4,3));
    builder.add(mOnlyMinimizeWhenWindowClosingChB, cc.xyw(2,5,3));
    
    final JPanel pS = (JPanel)builder.addSeparator(mLocalizer.msg("programShowing", "Program showing"),
        cc.xyw(1, 7, 5));

    builder.add(mShowChannelNameChb, cc.xyw(2, 9, 3));
    builder.add(mShowChannelIconChb, cc.xyw(2, 10, 3));

    builder.add(mShowNowRunningChb, cc.xyw(2, 11, 3));
    builder.add(mShowTimeProgramsChb, cc.xyw(2, 12, 3));
    builder.add(mShowImportantChb, cc.xy(2, 13));
    builder.add(b2.getPanel(), cc.xy(4, 13));

    
    mTrayIsEnabled.addChangeListener(new ChangeListener() {
    public void stateChanged(ChangeEvent e) {  
      pS.getComponent(0).setEnabled(mTrayIsEnabled.isSelected());
      mImportantSize.setEnabled(mTrayIsEnabled.isSelected() && mShowImportantChb.isSelected());
      mSizeLabel.setEnabled(mTrayIsEnabled.isSelected() && mShowImportantChb.isSelected());
      mShowTimeProgramsChb.setEnabled(mTrayIsEnabled.isSelected());
      mShowImportantChb.setEnabled(mTrayIsEnabled.isSelected());
      mMinimizeToTrayChb.setEnabled(mTrayIsEnabled.isSelected());
      mOnlyMinimizeWhenWindowClosingChB.setEnabled(mTrayIsEnabled.isSelected());
      mShowNowRunningChb.setEnabled(mTrayIsEnabled.isSelected());
      mShowChannelNameChb.setEnabled(mTrayIsEnabled.isSelected());
      mShowChannelIconChb.setEnabled(mTrayIsEnabled.isSelected());
      selectEnabled();
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
    
    mShowImportantChb.getChangeListeners()[0].stateChanged(null);
    mTrayIsEnabled.getChangeListeners()[0].stateChanged(null);
    mShowTimeProgramsChb.getChangeListeners()[0].stateChanged(null);
    mShowNowRunningChb.getChangeListeners()[0].stateChanged(null);
    
    
    return builder.getPanel();
  }
  
  private void selectEnabled() {
      boolean enabled = mShowTimeProgramsChb.isSelected()
          || mShowNowRunningChb.isSelected() || mShowImportantChb.isSelected();

      mShowChannelNameChb.setEnabled(enabled && mTrayIsEnabled.isSelected());
      mShowChannelIconChb.setEnabled(enabled && mTrayIsEnabled.isSelected());
  }

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
      if(mTrayIsEnabled.isSelected() && !mOldState)
        TVBrowser.loadTray();
      else if(!mTrayIsEnabled.isSelected() && mOldState)
        TVBrowser.removeTray();
    }
    
    if (mShowNowRunningChb != null)
      Settings.propShowNowRunningProgramsInTray.setBoolean(mShowNowRunningChb
          .isSelected());
    if (mShowImportantChb != null)
      Settings.propShowImportantProgramsInTray.setBoolean(mShowImportantChb
          .isSelected());
    if (mShowChannelNameChb != null)
      Settings.propProgramsInTrayContainsChannel.setBoolean(mShowChannelNameChb
          .isSelected());
    if (mShowChannelIconChb != null)
      Settings.propProgramsInTrayContainsChannelIcon
          .setBoolean(mShowChannelIconChb.isSelected());
    
    if (mShowTimeProgramsChb != null)
      Settings.propShowTimeProgramsInTray.setBoolean(mShowTimeProgramsChb.isSelected());

    Settings.propImportantProgramsInTraySize.setInt(((Integer) mImportantSize
        .getValue()).intValue());
    Settings.propShowProgramsInTrayWasConfigured.setBoolean(true);
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("basics","Basic settings");
  }
}
