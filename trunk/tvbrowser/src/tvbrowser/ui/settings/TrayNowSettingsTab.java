package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tvbrowser.core.Settings;
import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The settings tab for the NOW_TYPE of the ProgramMenuItem.
 * 
 * @author René Mach
 *
 */
public class TrayNowSettingsTab implements SettingsTab {

  private JCheckBox mIsEnabled, mShowName, mShowIcon, mShowTime, mShowToolTip;
  private JRadioButton mShowInSubMenu, mShowInTray;
  private static final Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  private JLabel mSeparator1, mSeparator2, mHelpLabel; 
  private static boolean mTrayIsEnabled = Settings.propTrayIsEnabled.getBoolean();
  private static TrayNowSettingsTab mInstance = null;
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,12dlu,pref:grow,5dlu",
        "pref,5dlu,pref,pref,pref,10dlu,pref,5dlu,pref,pref,pref,pref,fill:pref:grow,pref"));
    builder.setDefaultDialogBorder();
    
    mIsEnabled = new JCheckBox(mLocalizer.msg("nowEnabled","Show Now running programs"),Settings.propTrayNowProgramsEnabled.getBoolean());
    
    ButtonGroup bg = new ButtonGroup();
    
    mShowInSubMenu = new JRadioButton(mLocalizer.msg("inSubMenu","in a sub menu"),Settings.propTrayNowProgramsInSubMenu.getBoolean());
    mShowInTray = new JRadioButton(mLocalizer.msg("inTray","in the tray menu"), !mShowInSubMenu.isSelected());
    
    bg.add(mShowInSubMenu);
    bg.add(mShowInTray);
    
    mShowName = new JCheckBox(mLocalizer.msg("showName","Show channel name"),Settings.propTrayNowProgramsContainsName.getBoolean());
    mShowIcon = new JCheckBox(mLocalizer.msg("showIcon","Show channel icon"),Settings.propTrayNowProgramsContainsIcon.getBoolean());
    mShowTime = new JCheckBox(mLocalizer.msg("showTime","Show start time"),Settings.propTrayNowProgramsContainsTime.getBoolean());
    mShowToolTip = new JCheckBox(mLocalizer.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.propTrayNowProgramsContainsToolTip.getBoolean());
    mShowToolTip.setToolTipText(mLocalizer.msg("toolTipTip","Tool tips are small helper to something, like this one."));
    mHelpLabel = new JLabel();
        
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("now","Now running programs"), cc.xyw(1,1,4));
    builder.add(mIsEnabled, cc.xyw(2,3,2));
    builder.add(mShowInTray, cc.xy(3,4));
    builder.add(mShowInSubMenu, cc.xy(3,5));
    
    JPanel c1 = (JPanel) builder.addSeparator(mLocalizer.msg("settings","Settings"), cc.xyw(1,7,4));
    builder.add(mShowName, cc.xyw(2,9,2));
    builder.add(mShowIcon, cc.xyw(2,10,2));
    builder.add(mShowTime, cc.xyw(2,11,2));
    builder.add(mShowToolTip, cc.xyw(2,12,2));
    builder.add(mHelpLabel, cc.xyw(1,14,4));
    
    mSeparator1 = (JLabel)c.getComponent(0);
    mSeparator2 = (JLabel)c1.getComponent(0);
        
    setEnabled(true);
    
    mIsEnabled.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setEnabled(false);
      }
    });
    
    return builder.getPanel();
  }
  
  private void setEnabled(boolean trayStateChange) {
    if(mTrayIsEnabled)
      mHelpLabel.setText("");
    else
      mHelpLabel.setText(mLocalizer.msg("help","<html>The Tray is deactivated. To activate these settings activate the option <b>Tray activated</b> in the Tray Base settings.</html>"));
    
    if(trayStateChange) {
      mSeparator1.setEnabled(mTrayIsEnabled);
      mIsEnabled.setEnabled(mTrayIsEnabled);
    }
    
    TrayProgramsChannelsSettingsTab.setNowIsEnabled(mIsEnabled.isSelected());
    mSeparator2.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowInSubMenu.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowInTray.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIcon.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled && Settings.propEnableChannelIcons.getBoolean());
    mShowTime.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
  }
  
  public void saveSettings() {
    if(mIsEnabled != null)
      Settings.propTrayNowProgramsEnabled.setBoolean(mIsEnabled.isSelected());
    if(mShowInSubMenu != null)
      Settings.propTrayNowProgramsInSubMenu.setBoolean(mShowInSubMenu.isSelected());
    if(mShowName != null)
      Settings.propTrayNowProgramsContainsName.setBoolean(mShowName.isSelected());
    if(mShowIcon != null)
      Settings.propTrayNowProgramsContainsIcon.setBoolean(mShowIcon.isSelected());
    if(mShowTime != null)
      Settings.propTrayNowProgramsContainsTime.setBoolean(mShowTime.isSelected());
    if(mShowToolTip != null)
      Settings.propTrayNowProgramsContainsToolTip.setBoolean(mShowToolTip.isSelected());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("now","Now running programs");
  }

  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null)
      mInstance.setEnabled(true);
  }
}
