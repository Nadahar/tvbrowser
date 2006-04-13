package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The settings tab for the ON_TIME_TYPE of the ProgramMenuItem.
 * 
 * @author René Mach
 *
 */
public class TrayOnTimeSettingsTab implements SettingsTab {

  private JCheckBox mIsEnabled, mShowName, mShowIcon, mShowTime, mShowToolTip, mShowProgress;
  private static final Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  
  private ColorLabel mLightColorLb,mDarkColorLb;
  
  public JPanel createSettingsPanel() {
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,12dlu,pref:grow,5dlu",
        "pref,5dlu,pref,10dlu,pref,pref,pref,pref,pref,3dlu,pref"));
    builder.setDefaultDialogBorder();
    
    mIsEnabled = new JCheckBox(mLocalizer.msg("onTimeEnabled","Show programs at..."),Settings.propTrayOnTimeProgramsEnabled.getBoolean());
        
    mShowName = new JCheckBox(mLocalizer.msg("showName","Show channel name"),Settings.propTrayOnTimeProgramsContainsName.getBoolean());
    mShowIcon = new JCheckBox(mLocalizer.msg("showIcon","Show channel icon"),Settings.propTrayOnTimeProgramsContainsIcon.getBoolean());
    mShowTime = new JCheckBox(mLocalizer.msg("showTime","Show start time"),Settings.propTrayOnTimeProgramsContainsTime.getBoolean());
    mShowToolTip = new JCheckBox(mLocalizer.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.propTrayOnTimeProgramsContainsToolTip.getBoolean());
    mShowToolTip.setToolTipText(mLocalizer.msg("toolTipTip","Tool tips are small helper to something, like this one."));
    mShowProgress = new JCheckBox(mLocalizer.msg("showProgress","Show progress bar"), Settings.propTrayOnTimeProgramsShowProgress.getBoolean());
    
    mLightColorLb = new ColorLabel(Settings.propTrayOnTimeProgramsLightBackground.getColor());
    mLightColorLb.setStandardColor(Settings.propTrayOnTimeProgramsLightBackground.getDefaultColor());
    mDarkColorLb = new ColorLabel(Settings.propTrayOnTimeProgramsDarkBackground.getColor());
    mDarkColorLb.setStandardColor(Settings.propTrayOnTimeProgramsDarkBackground.getDefaultColor());
    
    final ColorButton light = new ColorButton(mLightColorLb);
    final ColorButton dark = new ColorButton(mDarkColorLb);
    
    PanelBuilder colors = new PanelBuilder(new FormLayout(
        "default,5dlu,default,5dlu,default", "pref,2dlu,pref"));    
    
    final JLabel darkLabel = colors.addLabel(
        mLocalizer.msg("progressLight",
            "Background color of the programs at..."), cc.xy(1, 1));
    colors.add(mLightColorLb, cc.xy(3, 1));
    colors.add(light,cc.xy(5, 1));
    

    final JLabel lightLabel = colors.addLabel(
        mLocalizer.msg("progressDark",
            "Progress color of the programs at..."), cc.xy(1, 3));
    colors.add(mDarkColorLb, cc.xy(3, 3));
    colors.add(dark,cc.xy(5, 3));   
        
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("onTime","Programs at..."), cc.xyw(1,1,4));
    builder.add(mIsEnabled, cc.xyw(2,3,2));

    builder.add(mShowName, cc.xyw(2,5,2));
    builder.add(mShowIcon, cc.xyw(2,6,2));
    builder.add(mShowTime, cc.xyw(2,7,2));
    builder.add(mShowToolTip, cc.xyw(2,8,2));
    builder.add(mShowProgress, cc.xyw(2,9,2));
    builder.add(colors.getPanel(), cc.xy(3,11));
    
    c.getComponent(0).setEnabled(Settings.propTrayIsEnabled.getBoolean());
    mIsEnabled.setEnabled(Settings.propTrayIsEnabled.getBoolean());
    mShowName.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowIcon.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowTime.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowProgress.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mLightColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mDarkColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    light.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    dark.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    darkLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    lightLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    
    mIsEnabled.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mShowName.setEnabled(mIsEnabled.isSelected());
        mShowIcon.setEnabled(mIsEnabled.isSelected());
        mShowTime.setEnabled(mIsEnabled.isSelected());
        mShowToolTip.setEnabled(mIsEnabled.isSelected());
        mShowProgress.setEnabled(mIsEnabled.isSelected());
        mLightColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        mDarkColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        light.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        dark.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        darkLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        lightLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
      }
    });
    
    mShowProgress.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mLightColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        mDarkColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        light.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        dark.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        darkLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        lightLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
      }
    });
    
    return builder.getPanel();
  }

  public void saveSettings() {
    if(mIsEnabled != null)
      Settings.propTrayOnTimeProgramsEnabled.setBoolean(mIsEnabled.isSelected());
    if(mShowName != null)
      Settings.propTrayOnTimeProgramsContainsName.setBoolean(mShowName.isSelected());
    if(mShowIcon != null)
      Settings.propTrayOnTimeProgramsContainsIcon.setBoolean(mShowIcon.isSelected());
    if(mShowTime != null)
      Settings.propTrayOnTimeProgramsContainsTime.setBoolean(mShowTime.isSelected());
    if(mShowToolTip != null)
      Settings.propTrayOnTimeProgramsContainsToolTip.setBoolean(mShowToolTip.isSelected());
    if(mShowProgress != null)
      Settings.propTrayOnTimeProgramsShowProgress.setBoolean(mShowProgress.isSelected());
    if(mLightColorLb != null)
      Settings.propTrayOnTimeProgramsLightBackground.setColor(mLightColorLb.getColor());
    if(mDarkColorLb != null)
      Settings.propTrayOnTimeProgramsDarkBackground.setColor(mDarkColorLb.getColor());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("onTime","Programs at...");
  }

}
