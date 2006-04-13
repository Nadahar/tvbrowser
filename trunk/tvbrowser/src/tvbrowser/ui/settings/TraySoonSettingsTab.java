package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
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
  
  public JPanel createSettingsPanel() {
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu",
        "pref,5dlu,pref,10dlu,pref,pref,pref,pref"));
    builder.setDefaultDialogBorder();
    
    mIsEnabled = new JCheckBox(mLocalizer.msg("soonEnabled","Show Soon running programs"),Settings.propTraySoonProgramsEnabled.getBoolean());
        
    mShowName = new JCheckBox(mLocalizer.msg("showName","Show channel name"),Settings.propTraySoonProgramsContainsName.getBoolean());
    mShowIcon = new JCheckBox(mLocalizer.msg("showIcon","Show channel icon"),Settings.propTraySoonProgramsContainsIcon.getBoolean());
    mShowTime = new JCheckBox(mLocalizer.msg("showTime","Show start time"),Settings.propTraySoonProgramsContainsTime.getBoolean());
    mShowToolTip = new JCheckBox(mLocalizer.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.propTraySoonProgramsContainsToolTip.getBoolean());
    mShowToolTip.setToolTipText(mLocalizer.msg("toolTipTip","Tool tips are small helper to something, like this one."));
        
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("soon","Soon running programs"), cc.xyw(1,1,3));
    builder.add(mIsEnabled, cc.xy(2,3));
    builder.add(mShowName, cc.xy(2,5));
    builder.add(mShowIcon, cc.xy(2,6));
    builder.add(mShowTime, cc.xy(2,7));
    builder.add(mShowToolTip, cc.xy(2,8));
    
    c.getComponent(0).setEnabled(Settings.propTrayIsEnabled.getBoolean());
    mIsEnabled.setEnabled(Settings.propTrayIsEnabled.getBoolean());
    mShowName.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowIcon.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowTime.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    
    mIsEnabled.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mShowName.setEnabled(mIsEnabled.isSelected());
        mShowIcon.setEnabled(mIsEnabled.isSelected());
        mShowTime.setEnabled(mIsEnabled.isSelected());
        mShowToolTip.setEnabled(mIsEnabled.isSelected());
      }
    });
    
    return builder.getPanel();
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
}
