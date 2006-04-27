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
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * The settings tab for the SOON_TYPE of the ProgramMenuItem.
 * 
 * @author Ren� Mach
 *
 */
public class TraySoonSettingsTab implements SettingsTab {

  private static final Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  private JCheckBox mIsEnabled, mShowTime, mShowToolTip;
  private JLabel mIconSeparator,mSeparator1, mSeparator2, mHelpLabel; 
  private static boolean mTrayIsEnabled = Settings.propTrayIsEnabled.getBoolean();
  
  private JRadioButton mShowIconAndName, mShowName, mShowIcon;
  
  private static TraySoonSettingsTab mInstance;
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu",
        "pref,5dlu,pref,10dlu,pref,5dlu,pref,pref,pref,3dlu,pref," +
        "10dlu,pref,5dlu,pref,pref,fill:pref:grow,pref"));
    builder.setDefaultDialogBorder();
    
    mIsEnabled = new JCheckBox(mLocalizer.msg("soonEnabled","Show Soon running programs"),Settings.propTraySoonProgramsEnabled.getBoolean());
    
    mShowIconAndName = new JRadioButton(mLocalizer.msg("showIconName","Show channel icon and channel name"),Settings.propTraySoonProgramsContainsName.getBoolean() && Settings.propTraySoonProgramsContainsIcon.getBoolean());
    mShowName = new JRadioButton(mLocalizer.msg("showName","Show channel name"),Settings.propTraySoonProgramsContainsName.getBoolean() && !Settings.propTraySoonProgramsContainsIcon.getBoolean());
    mShowIcon = new JRadioButton(mLocalizer.msg("showIcon","Show channel icon"),!Settings.propTraySoonProgramsContainsName.getBoolean() && Settings.propTraySoonProgramsContainsIcon.getBoolean());
    
    ButtonGroup bg = new ButtonGroup();
    bg.add(mShowIconAndName);
    bg.add(mShowIcon);
    bg.add(mShowName);
    
    mShowName.setSelected(!Settings.propEnableChannelIcons.getBoolean());
    
    mShowTime = new JCheckBox(mLocalizer.msg("showTime","Show start time"),Settings.propTraySoonProgramsContainsTime.getBoolean());
    mShowToolTip = new JCheckBox(mLocalizer.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.propTraySoonProgramsContainsToolTip.getBoolean());
    mShowToolTip.setToolTipText(mLocalizer.msg("toolTipTip","Tool tips are small helper to something, like this one."));
    
    JEditorPane pane = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("goToLook","To disable/enable Channel Icons globally, please look <a href=\"#link\">here</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.LOOKANDFEEL);
        }
      }
    });
    
    mHelpLabel = new JLabel();
        
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("soon","Soon running programs"), cc.xyw(1,1,3));
    builder.add(mIsEnabled, cc.xy(2,3));
    
    JPanel c1 = (JPanel) builder.addSeparator(mLocalizer.msg("iconNameSeparator","Channel icons/channel name"), cc.xyw(1,5,3));
    
    builder.add(mShowIconAndName, cc.xy(2,7));
    builder.add(mShowIcon, cc.xy(2,8));
    builder.add(mShowName, cc.xy(2,9));
    builder.add(pane, cc.xy(2,11));
    
    JPanel c2 = (JPanel) builder.addSeparator(mLocalizer.msg("settings","Settings"), cc.xyw(1,13,3));
    
    builder.add(mShowTime, cc.xy(2,15));
    builder.add(mShowToolTip, cc.xy(2,16));
    builder.add(mHelpLabel, cc.xyw(1,18,3));
    
    mSeparator1 = (JLabel)c.getComponent(0);
    mIconSeparator = (JLabel)c1.getComponent(0);
    mSeparator2 = (JLabel)c2.getComponent(0);
    
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
    mIconSeparator.setEnabled(mTrayIsEnabled);
    mSeparator2.setEnabled(mTrayIsEnabled);
    mIsEnabled.setEnabled(mTrayIsEnabled);
    mShowName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIcon.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled && Settings.propEnableChannelIcons.getBoolean());
    mShowIconAndName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled && Settings.propEnableChannelIcons.getBoolean());
    
    mShowTime.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
  }
  
  public void saveSettings() {
    if(mIsEnabled != null)
      Settings.propTraySoonProgramsEnabled.setBoolean(mIsEnabled.isSelected());
    if(mShowIconAndName != null && mShowName != null && mShowIcon != null) {
      Settings.propTraySoonProgramsContainsName.setBoolean(mShowIconAndName.isSelected() || mShowName.isSelected());
      Settings.propTraySoonProgramsContainsIcon.setBoolean(mShowIconAndName.isSelected() || mShowIcon.isSelected());
    }
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
