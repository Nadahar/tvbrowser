package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The settings tab for the SOON_TYPE of the ProgramMenuItem.
 * 
 * @author René Mach
 *
 */
public class TraySoonSettingsTab implements SettingsTab {

  private static final Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  private JCheckBox mIsEnabled, mShowName, mShowIcon, mShowTime, mShowToolTip;
  private JLabel mSeparator1, mSeparator2, mHelpLabel; 
  private static boolean mTrayIsEnabled = Settings.propTrayIsEnabled.getBoolean();
  
  private static TraySoonSettingsTab mInstance;
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu",
        "pref,5dlu,pref,10dlu,pref,5dlu,pref,pref,pref,pref,fill:pref:grow,pref"));
    builder.setDefaultDialogBorder();
    
    mIsEnabled = new JCheckBox(mLocalizer.msg("soonEnabled","Show Soon running programs"),Settings.propTraySoonProgramsEnabled.getBoolean());
        
    mShowName = new JCheckBox(mLocalizer.msg("showName","Show channel name"),Settings.propTraySoonProgramsContainsName.getBoolean());
    mShowIcon = new JCheckBox(mLocalizer.msg("showIcon","Show channel icon"),Settings.propTraySoonProgramsContainsIcon.getBoolean());
    mShowTime = new JCheckBox(mLocalizer.msg("showTime","Show start time"),Settings.propTraySoonProgramsContainsTime.getBoolean());
    mShowToolTip = new JCheckBox(mLocalizer.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.propTraySoonProgramsContainsToolTip.getBoolean());
    mShowToolTip.setToolTipText(mLocalizer.msg("toolTipTip","Tool tips are small helper to something, like this one."));
    
    mHelpLabel = new JLabel();
        
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("soon","Soon running programs"), cc.xyw(1,1,3));
    builder.add(mIsEnabled, cc.xy(2,3));
    
    JPanel c1 = (JPanel) builder.addSeparator(mLocalizer.msg("settings","Settings"), cc.xyw(1,5,3));
    
    builder.add(mShowName, cc.xy(2,7));
    builder.add(mShowIcon, cc.xy(2,8));
    builder.add(mShowTime, cc.xy(2,9));
    builder.add(mShowToolTip, cc.xy(2,10));
    builder.add(mHelpLabel, cc.xyw(1,12,3));
    
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
    
    TrayProgramsChannelsSettingsTab.setSoonIsEnabled(mIsEnabled.isSelected());
    mSeparator2.setEnabled(mTrayIsEnabled);
    mIsEnabled.setEnabled(mTrayIsEnabled);
    mShowName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIcon.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowTime.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
  }
  
  public void saveSettings() {
    if(mIsEnabled != null)
      Settings.propTraySoonProgramsEnabled.setBoolean(mIsEnabled.isSelected());
    if(mShowName != null)
      Settings.propTraySoonProgramsContainsName.setBoolean(mShowName.isSelected());
    if(mShowIcon != null)
      Settings.propTraySoonProgramsContainsIcon.setBoolean(mShowIcon.isSelected());
    if(mShowTime != null)
      Settings.propTraySoonProgramsContainsTime.setBoolean(mShowTime.isSelected());
    if(mShowToolTip != null)
      Settings.propTraySoonProgramsContainsToolTip.setBoolean(mShowToolTip.isSelected());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("soon","Soon running programs");
  }
  
  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null)
      mInstance.setEnabled(true);
  }
}
