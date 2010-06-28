package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * The settings tab for the ON_TIME_TYPE of the ProgramMenuItem.
 * 
 * @author René Mach
 *
 */
public class TrayOnTimeSettingsTab implements SettingsTab {

  private JCheckBox mIsEnabled, mShowTime, mShowToolTip, mShowProgress;
  private JRadioButton mShowInSubMenu, mShowInTray;
  private static final Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  private JLabel mIconSeparator, mSeparator1, mSeparator2, mDarkLabel, mLightLabel;
  private static boolean mTrayIsEnabled = Settings.propTrayIsEnabled.getBoolean();
  
  private JEditorPane mHelpLabel, mInfo, mTimeHelp;
  private JRadioButton mShowIconAndName, mShowName, mShowIcon;
  
  private ColorLabel mLightColorLb,mDarkColorLb;
  private ColorButton mLight, mDark;
  
  private static TrayOnTimeSettingsTab mInstance;
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,12dlu,pref:grow,5dlu",
        "pref,5dlu,pref,pref,pref,5dlu,pref,10dlu,pref,5dlu,pref,pref,pref," +
        "10dlu,pref,5dlu,pref,pref,pref,3dlu,pref,5dlu,pref,fill:pref:grow,pref"));
    builder.setDefaultDialogBorder();
    
    mIsEnabled = new JCheckBox(mLocalizer.msg("onTimeEnabled","Show programs at..."),Settings.propTrayOnTimeProgramsEnabled.getBoolean());
    
    ButtonGroup bg = new ButtonGroup();
    
    mShowInSubMenu = new JRadioButton(mLocalizer.msg("inSubMenu","in a sub menu"),Settings.propTrayOnTimeProgramsInSubMenu.getBoolean());
    mShowInTray = new JRadioButton(mLocalizer.msg("inTray","in the tray menu"), !mShowInSubMenu.isSelected());
    
    bg.add(mShowInSubMenu);
    bg.add(mShowInTray);
    
    mShowIconAndName = new JRadioButton(mLocalizer.msg("showIconName","Show channel icon and channel name"),Settings.propTrayOnTimeProgramsContainsName.getBoolean() && Settings.propTrayOnTimeProgramsContainsIcon.getBoolean());
    mShowIcon = new JRadioButton(mLocalizer.msg("showIcon","Show channel icon"),Settings.propTrayOnTimeProgramsContainsIcon.getBoolean() && !Settings.propTrayOnTimeProgramsContainsName.getBoolean());
    mShowName = new JRadioButton(mLocalizer.msg("showName","Show channel name"),!Settings.propTrayOnTimeProgramsContainsIcon.getBoolean() && Settings.propTrayOnTimeProgramsContainsName.getBoolean());
    
    ButtonGroup bg1 = new ButtonGroup();
    bg1.add(mShowIconAndName);
    bg1.add(mShowIcon);
    bg1.add(mShowName);
    
    mShowTime = new JCheckBox(mLocalizer.msg("showTime","Show start time"),Settings.propTrayOnTimeProgramsContainsTime.getBoolean());
    mShowToolTip = new JCheckBox(mLocalizer.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.propTrayOnTimeProgramsContainsToolTip.getBoolean());
    mShowToolTip.setToolTipText(mLocalizer.msg("toolTipTip","Tool tips are small helper to something, like this one."));
    mShowProgress = new JCheckBox(mLocalizer.msg("showProgress","Show progress bar"), Settings.propTrayOnTimeProgramsShowProgress.getBoolean());
    
    mLightColorLb = new ColorLabel(Settings.propTrayOnTimeProgramsLightBackground.getColor());
    mLightColorLb.setStandardColor(Settings.propTrayOnTimeProgramsLightBackground.getDefaultColor());
    mDarkColorLb = new ColorLabel(Settings.propTrayOnTimeProgramsDarkBackground.getColor());
    mDarkColorLb.setStandardColor(Settings.propTrayOnTimeProgramsDarkBackground.getDefaultColor());
    
    mTimeHelp =  UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("helpTime","If you want to change the times of this view, you simply have to change the times of the <a href=\"#link\">time buttons</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.TIMEBUTTONS);
        }
      }
    });
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help","The Tray is deactivated. To activate these settings activate the option <b>Tray activated</b> in the <a href=\"#link\">Tray Base settings</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.TRAY);
        }
      }
    });
    
    mInfo = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("trayProgressInfo","The progress bar simulates the progress of the program if the time would be reached."));
    
    mLight = new ColorButton(mLightColorLb);
    mDark = new ColorButton(mDarkColorLb);
    
    PanelBuilder colors = new PanelBuilder(new FormLayout(
        "default,5dlu,default,5dlu,default", "pref,2dlu,pref"));
    
    mDarkLabel = colors.addLabel(
        mLocalizer.msg("progressLight",
            "Background color of the programs at..."), cc.xy(1, 1));
    colors.add(mLightColorLb, cc.xy(3, 1));
    colors.add(mLight,cc.xy(5, 1));

    mLightLabel = colors.addLabel(
        mLocalizer.msg("progressDark",
            "Progress color of the programs at..."), cc.xy(1, 3));
    colors.add(mDarkColorLb, cc.xy(3, 3));
    colors.add(mDark,cc.xy(5, 3));
        
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("onTime","Programs at..."), cc.xyw(1,1,4));
    builder.add(mIsEnabled, cc.xyw(2,3,2));
    builder.add(mShowInTray, cc.xy(3,4));
    builder.add(mShowInSubMenu, cc.xy(3,5));
    builder.add(mTimeHelp, cc.xyw(2,7,2));

    JPanel c1 = (JPanel) builder.addSeparator(mLocalizer.msg("iconNameSeparator","Channel icons/channel name"), cc.xyw(1,9,4));
    builder.add(mShowIconAndName, cc.xyw(2,11,2));
    builder.add(mShowIcon, cc.xyw(2,12,2));
    builder.add(mShowName, cc.xyw(2,13,2));
    
    JPanel c2 = (JPanel) builder.addSeparator(mLocalizer.msg("settings","Settings"), cc.xyw(1,15,4));
    builder.add(mShowTime, cc.xyw(2,17,2));
    builder.add(mShowToolTip, cc.xyw(2,18,2));
    builder.add(mShowProgress, cc.xyw(2,19,2));
    builder.add(colors.getPanel(), cc.xy(3,21));
    builder.add(mInfo, cc.xyw(2,23,2));
    builder.add(mHelpLabel, cc.xyw(1,25,4));
    
    mSeparator1 = (JLabel)c.getComponent(0);
    mIconSeparator = (JLabel)c1.getComponent(0);
    mSeparator2 = (JLabel)c2.getComponent(0);
    
    setEnabled(true);
    
    mIsEnabled.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setEnabled(false);
      }
    });
    
    mShowProgress.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mLightColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        mDarkColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        mLight.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        mDark.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        mDarkLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        mLightLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
        mInfo.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected());
      }
    });
    
    return builder.getPanel();
  }
  
  private void setEnabled(boolean trayStateChange) {
    mHelpLabel.setVisible(!mTrayIsEnabled);
    
    if(trayStateChange) {
      mSeparator1.setEnabled(mTrayIsEnabled);
      mIsEnabled.setEnabled(mTrayIsEnabled);
    }
    
    TrayProgramsChannelsSettingsTab.setOnTimeIsEnabled(mIsEnabled.isSelected());
    mSeparator2.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowInSubMenu.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowInTray.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mIconSeparator.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mTimeHelp.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIconAndName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIcon.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowTime.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowProgress.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mLightColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mDarkColorLb.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mLight.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mDark.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mDarkLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mLightLabel.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
    mInfo.setEnabled(mIsEnabled.isSelected() && mShowProgress.isSelected() && mTrayIsEnabled);
  }

  public void saveSettings() {
    if(mIsEnabled != null) {
      Settings.propTrayOnTimeProgramsEnabled.setBoolean(mIsEnabled.isSelected());
    }
    if(mShowInSubMenu != null) {
      Settings.propTrayOnTimeProgramsInSubMenu.setBoolean(mShowInSubMenu.isSelected());
    }
    if(mShowIconAndName != null && mShowName != null && mShowIcon != null) {
      Settings.propTrayOnTimeProgramsContainsName.setBoolean(mShowIconAndName.isSelected() || mShowName.isSelected());
      Settings.propTrayOnTimeProgramsContainsIcon.setBoolean(mShowIconAndName.isSelected() || mShowIcon.isSelected());
    }
    if(mShowTime != null) {
      Settings.propTrayOnTimeProgramsContainsTime.setBoolean(mShowTime.isSelected());
    }
    if(mShowToolTip != null) {
      Settings.propTrayOnTimeProgramsContainsToolTip.setBoolean(mShowToolTip.isSelected());
    }
    if(mShowProgress != null) {
      Settings.propTrayOnTimeProgramsShowProgress.setBoolean(mShowProgress.isSelected());
    }
    if(mLightColorLb != null) {
      Settings.propTrayOnTimeProgramsLightBackground.setColor(mLightColorLb.getColor());
    }
    if(mDarkColorLb != null) {
      Settings.propTrayOnTimeProgramsDarkBackground.setColor(mDarkColorLb.getColor());
    }
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return getName();
  }
  
  /**
   * Gets the name of this settings tab.
   * 
   * @return The name of this settings tab.
   */
  public static String getName() {
    return mLocalizer.msg("onTime","Programs at...");
  }
  
  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null) {
      mInstance.setEnabled(true);
    }
  }
}
