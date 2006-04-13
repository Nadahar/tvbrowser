package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
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
  
  public JPanel createSettingsPanel() {
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu",
        "pref,5dlu,pref,pref,pref,10dlu,pref,pref,pref,pref"));
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
        
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("now","Now running programs"), cc.xyw(1,1,3));
    builder.add(mIsEnabled, cc.xy(2,3));
    builder.add(mShowInTray, cc.xy(2,4));
    builder.add(mShowInSubMenu, cc.xy(2,5));
    builder.add(mShowName, cc.xy(2,7));
    builder.add(mShowIcon, cc.xy(2,8));
    builder.add(mShowTime, cc.xy(2,9));
    builder.add(mShowToolTip, cc.xy(2,10));
    
    c.getComponent(0).setEnabled(Settings.propTrayIsEnabled.getBoolean());
    mIsEnabled.setEnabled(Settings.propTrayIsEnabled.getBoolean());
    mShowInSubMenu.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowInTray.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowName.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowIcon.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowTime.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    
    mIsEnabled.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mShowInSubMenu.setEnabled(mIsEnabled.isSelected());
        mShowInTray.setEnabled(mIsEnabled.isSelected());
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

}
