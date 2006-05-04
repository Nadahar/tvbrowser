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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
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
 * The settings tab for the IMPORTANT_TYPE of the ProgramMenuItem.
 *  
 * @author Ren� Mach
 *
 */
public class TrayImportantSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  
  private JCheckBox mIsEnabled, mShowDate, mShowTime, mShowToolTip;
  private JRadioButton mShowInSubMenu, mShowInTray;
  private JSpinner mSize;
  private JLabel mIconSeparator, mSeparator1, mSeparator2, mSizeLabel;
  
  private JEditorPane mHelpLabel, mLookHelpLink;
  private JRadioButton mShowIconAndName, mShowName, mShowIcon;
  
  private static boolean mTrayIsEnabled = Settings.propTrayIsEnabled.getBoolean();
  private static TrayImportantSettingsTab mInstance;
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,12dlu,pref,5dlu,pref,pref:grow,5dlu",
        "pref,5dlu,pref,pref,pref,pref,10dlu,pref,5dlu,pref," +
        "pref,pref,3dlu,pref,10dlu,pref,5dlu,pref,pref,pref,fill:pref:grow,pref"));
    builder.setDefaultDialogBorder();
    
    mIsEnabled = new JCheckBox(mLocalizer.msg("importantEnabled","Show important programs"),Settings.propTrayImportantProgramsEnabled.getBoolean());
    mIsEnabled.setToolTipText(mLocalizer.msg("importantToolTip","Important programs are all marked programs."));
    
    ButtonGroup bg = new ButtonGroup();
    
    mShowInSubMenu = new JRadioButton(mLocalizer.msg("inSubMenu","in a sub menu"),Settings.propTrayImportantProgramsInSubMenu.getBoolean());
    mShowInTray = new JRadioButton(mLocalizer.msg("inTray","in the tray menu"), !mShowInSubMenu.isSelected());
    
    bg.add(mShowInSubMenu);
    bg.add(mShowInTray);
    
    mSize = new JSpinner(new SpinnerNumberModel(Settings.propTrayImportantProgramsSize.getInt(), 1, 10, 1));
    
    mShowIconAndName = new JRadioButton(mLocalizer.msg("showIconName","Show channel icon and channel name"),Settings.propTrayImportantProgramsContainsName.getBoolean() && Settings.propTrayImportantProgramsContainsIcon.getBoolean());
    mShowName = new JRadioButton(mLocalizer.msg("showName","Show channel name"),Settings.propTrayImportantProgramsContainsName.getBoolean() && !Settings.propTrayImportantProgramsContainsIcon.getBoolean());
    mShowIcon = new JRadioButton(mLocalizer.msg("showIcon","Show channel icon"),!Settings.propTrayImportantProgramsContainsName.getBoolean() && Settings.propTrayImportantProgramsContainsIcon.getBoolean());
    
    ButtonGroup bg1 = new ButtonGroup();
    bg1.add(mShowIconAndName);
    bg1.add(mShowIcon);
    bg1.add(mShowName);
    
    mShowName.setSelected(!Settings.propEnableChannelIcons.getBoolean());
    
    mShowDate = new JCheckBox(mLocalizer.msg("showDate","Show date"),Settings.propTrayImportantProgramsContainsDate.getBoolean());
    mShowTime = new JCheckBox(mLocalizer.msg("showTime","Show start time"),Settings.propTrayImportantProgramsContainsTime.getBoolean());
    mShowToolTip = new JCheckBox(mLocalizer.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.propTrayImportantProgramsContainsToolTip.getBoolean());
    mShowToolTip.setToolTipText(mLocalizer.msg("toolTipTip","Tool tips are small helper to something, like this one."));
    
    mLookHelpLink = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("goToLook","To disable/enable Channel Icons globally, please look <a href=\"#link\">here</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.LOOKANDFEEL);
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
        
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("important","Important programs"), cc.xyw(1,1,7));
    builder.add(mIsEnabled, cc.xyw(2,3,5));
    builder.add(mShowInTray, cc.xyw(3,4,4));
    builder.add(mShowInSubMenu, cc.xyw(3,5,4));
    mSizeLabel = builder.addLabel(mLocalizer.msg("importantSize","Number of shown programs:"), cc.xy(3,6));
    builder.add(mSize, cc.xy(5,6));
    
    JPanel c1 = (JPanel) builder.addSeparator(mLocalizer.msg("iconNameSeparator","Channel icons/channel name"), cc.xyw(1,8,7));
    builder.add(mShowIconAndName, cc.xyw(2,10,5));
    builder.add(mShowIcon, cc.xyw(2,11,5));
    builder.add(mShowName, cc.xyw(2,12,5));
    builder.add(mLookHelpLink, cc.xyw(2,14,5));
    
    JPanel c2 = (JPanel) builder.addSeparator(mLocalizer.msg("settings","Settings"), cc.xyw(1,16,7));
    builder.add(mShowDate, cc.xyw(2,18,5));
    builder.add(mShowTime, cc.xyw(2,19,5));
    builder.add(mShowToolTip, cc.xyw(2,20,5));
    builder.add(mHelpLabel, cc.xyw(1,22,7));
    
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
    mHelpLabel.setVisible(!mTrayIsEnabled);
    
    if(trayStateChange) {
      mSeparator1.setEnabled(mTrayIsEnabled);
      mIsEnabled.setEnabled(mTrayIsEnabled);
    }
    
    mIconSeparator.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSeparator2.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowInSubMenu.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowInTray.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mLookHelpLink.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIconAndName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled && Settings.propEnableChannelIcons.getBoolean());
    mShowIcon.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled && Settings.propEnableChannelIcons.getBoolean());
    mShowDate.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowTime.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSizeLabel.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSize.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
  }
  
  public void saveSettings() {
    if(mIsEnabled != null)
      Settings.propTrayImportantProgramsEnabled.setBoolean(mIsEnabled.isSelected());
    if(mShowInSubMenu != null)
      Settings.propTrayImportantProgramsInSubMenu.setBoolean(mShowInSubMenu.isSelected());
    if(mSize != null)
      Settings.propTrayImportantProgramsSize.setInt(((Integer)mSize.getValue()).intValue());
    if(mShowIconAndName != null && mShowName != null && mShowIcon != null) {
      Settings.propTrayImportantProgramsContainsName.setBoolean(mShowIconAndName.isSelected() || mShowName.isSelected());
      Settings.propTrayImportantProgramsContainsIcon.setBoolean(mShowIconAndName.isSelected() || mShowIcon.isSelected());
    }
    if(mShowDate != null)
      Settings.propTrayImportantProgramsContainsDate.setBoolean(mShowDate.isSelected());
    if(mShowTime != null)
      Settings.propTrayImportantProgramsContainsTime.setBoolean(mShowTime.isSelected());
    if(mShowToolTip != null)
      Settings.propTrayImportantProgramsContainsToolTip.setBoolean(mShowToolTip.isSelected());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("important","Important programs");
  }
  
  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null)
      mInstance.setEnabled(true);
  }

}
