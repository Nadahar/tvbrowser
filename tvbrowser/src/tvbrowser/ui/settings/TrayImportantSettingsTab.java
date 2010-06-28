package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import util.ui.MarkPriorityComboBoxRenderer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * The settings tab for the IMPORTANT_TYPE of the ProgramMenuItem.
 * 
 * @author René Mach
 *
 */
public class TrayImportantSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  
  private JCheckBox mIsEnabled, mShowDate, mShowTime, mShowToolTip;
  private JRadioButton mShowInSubMenu, mShowInTray;
  private JSpinner mSize;
  private JLabel mIconSeparator, mSeparator1, mSeparator2, mSizeLabel, mSizeInfo;
  
  private JEditorPane mHelpLabel;
  private JRadioButton mShowIconAndName, mShowName, mShowIcon;
  
  private JComboBox mPriority;
  private JLabel mPriorityText;
  
  private static boolean mTrayIsEnabled = Settings.propTrayIsEnabled.getBoolean();
  private static TrayImportantSettingsTab mInstance;
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,12dlu,pref,5dlu,pref,5dlu,pref:grow,5dlu",
        "pref,5dlu,pref,pref,pref,pref,pref,10dlu,pref,5dlu,pref," +
        "pref,pref,10dlu,pref,5dlu,pref,pref,pref,fill:pref:grow,pref"));
    builder.setDefaultDialogBorder();
    
    mIsEnabled = new JCheckBox(mLocalizer.msg("importantEnabled","Show important programs"),Settings.propTrayImportantProgramsEnabled.getBoolean());
    mIsEnabled.setToolTipText(mLocalizer.msg("importantToolTip","Important programs are all marked programs."));
    
    ButtonGroup bg = new ButtonGroup();
    
    mShowInSubMenu = new JRadioButton(mLocalizer.msg("inSubMenu","in a sub menu"),Settings.propTrayImportantProgramsInSubMenu.getBoolean());
    mShowInTray = new JRadioButton(mLocalizer.msg("inTray","in the tray menu"), !mShowInSubMenu.isSelected());
    
    bg.add(mShowInSubMenu);
    bg.add(mShowInTray);
    
    int maxSizeValue = Settings.propTrayImportantProgramsInSubMenu.getBoolean() ? 30 : 15;
    
    mSize = new JSpinner(new SpinnerNumberModel(Settings.propTrayImportantProgramsSize.getInt(), 1, maxSizeValue, 1));
    
    mShowIconAndName = new JRadioButton(mLocalizer.msg("showIconName","Show channel icon and channel name"),Settings.propTrayImportantProgramsContainsName.getBoolean() && Settings.propTrayImportantProgramsContainsIcon.getBoolean());
    mShowName = new JRadioButton(mLocalizer.msg("showName","Show channel name"),Settings.propTrayImportantProgramsContainsName.getBoolean() && !Settings.propTrayImportantProgramsContainsIcon.getBoolean());
    mShowIcon = new JRadioButton(mLocalizer.msg("showIcon","Show channel icon"),!Settings.propTrayImportantProgramsContainsName.getBoolean() && Settings.propTrayImportantProgramsContainsIcon.getBoolean());
    
    ButtonGroup bg1 = new ButtonGroup();
    bg1.add(mShowIconAndName);
    bg1.add(mShowIcon);
    bg1.add(mShowName);
    
    mShowDate = new JCheckBox(mLocalizer.msg("showDate","Show date"),Settings.propTrayImportantProgramsContainsDate.getBoolean());
    mShowTime = new JCheckBox(mLocalizer.msg("showTime","Show start time"),Settings.propTrayImportantProgramsContainsTime.getBoolean());
    mShowToolTip = new JCheckBox(mLocalizer.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.propTrayImportantProgramsContainsToolTip.getBoolean());
    mShowToolTip.setToolTipText(mLocalizer.msg("toolTipTip","Tool tips are small helper to something, like this one."));
        
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help","The Tray is deactivated. To activate these settings activate the option <b>Tray activated</b> in the <a href=\"#link\">Tray Base settings</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.TRAY);
        }
      }
    });
    
    JPanel priority = new JPanel(new FormLayout("pref,5dlu,pref","1dlu,pref"));
    
    Localizer localizer = MarkingsSettingsTab.mLocalizer;
    
    String[] colors = {localizer.msg("color.minPriority","1. Color (minimum priority)"),localizer.msg("color.lowerMediumPriority","2. Color (lower medium priority)"),localizer.msg("color.mediumPriority","3. Color (Medium priority)"),localizer.msg("color.higherMediumPriority","4. Color (higher medium priority)"),localizer.msg("color.maxPriority","5. Color (maximum priority)")};
    
    mPriorityText = new JLabel(mLocalizer.msg("importantMarkPriority","Mark priority higher or the same like:"));
    
    mPriority = new JComboBox(colors);
    mPriority.setSelectedIndex(Settings.propTrayImportantProgramsPriority.getInt());
    mPriority.setRenderer(new MarkPriorityComboBoxRenderer());

    priority.add(mPriorityText, cc.xy(1,2));
    priority.add(mPriority, cc.xy(3,2));
    
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("important","Important programs"), cc.xyw(1,1,8));
    builder.add(mIsEnabled, cc.xyw(2,3,6));
    builder.add(mShowInTray, cc.xyw(3,4,5));
    builder.add(mShowInSubMenu, cc.xyw(3,5,5));
    mSizeLabel = builder.addLabel(mLocalizer.msg("importantSize","Number of shown programs:"), cc.xy(3,6));
    builder.add(mSize, cc.xy(5,6));
    mSizeInfo = builder.addLabel(mLocalizer.msg("sizeInfo","(maximum: {0})",maxSizeValue), cc.xy(7,6));
    builder.add(priority, cc.xyw(3,7,5));
    
    JPanel c1 = (JPanel) builder.addSeparator(mLocalizer.msg("iconNameSeparator","Channel icons/channel name"), cc.xyw(1,9,8));
    builder.add(mShowIconAndName, cc.xyw(2,11,6));
    builder.add(mShowIcon, cc.xyw(2,12,6));
    builder.add(mShowName, cc.xyw(2,13,6));
    
    JPanel c2 = (JPanel) builder.addSeparator(mLocalizer.msg("settings","Settings"), cc.xyw(1,15,8));
    builder.add(mShowDate, cc.xyw(2,17,6));
    builder.add(mShowTime, cc.xyw(2,18,6));
    builder.add(mShowToolTip, cc.xyw(2,19,6));
    builder.add(mHelpLabel, cc.xyw(1,21,8));
    
    mSeparator1 = (JLabel)c.getComponent(0);
    mIconSeparator = (JLabel)c1.getComponent(0);
    mSeparator2 = (JLabel)c2.getComponent(0);
    
    setEnabled(true);
    
    mShowInSubMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mSize.setModel(new SpinnerNumberModel(((Integer)mSize.getValue()).intValue(), 1, 30, 1));
        mSizeInfo.setText(mLocalizer.msg("sizeInfo","(maximum: {0})",30));
      }
    });

    mShowInTray.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int value = ((Integer)mSize.getValue()).intValue();
        mSize.setModel(new SpinnerNumberModel(value > 15 ? 15 : value, 1, 15, 1));
        mSizeInfo.setText(mLocalizer.msg("sizeInfo","(maximum: {0})",15));
      }
    });
    
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
    mShowName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIconAndName.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowIcon.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowDate.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowTime.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSizeLabel.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSize.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mSizeInfo.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mPriority.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
    mPriorityText.setEnabled(mIsEnabled.isSelected() && mTrayIsEnabled);
  }
  
  public void saveSettings() {
    if(mIsEnabled != null) {
      Settings.propTrayImportantProgramsEnabled.setBoolean(mIsEnabled.isSelected());
    }
    if(mShowInSubMenu != null) {
      Settings.propTrayImportantProgramsInSubMenu.setBoolean(mShowInSubMenu.isSelected());
    }
    if(mSize != null) {
      Settings.propTrayImportantProgramsSize.setInt(((Integer)mSize.getValue()).intValue());
    }
    if(mShowIconAndName != null && mShowName != null && mShowIcon != null) {
      Settings.propTrayImportantProgramsContainsName.setBoolean(mShowIconAndName.isSelected() || mShowName.isSelected());
      Settings.propTrayImportantProgramsContainsIcon.setBoolean(mShowIconAndName.isSelected() || mShowIcon.isSelected());
    }
    if(mShowDate != null) {
      Settings.propTrayImportantProgramsContainsDate.setBoolean(mShowDate.isSelected());
    }
    if(mShowTime != null) {
      Settings.propTrayImportantProgramsContainsTime.setBoolean(mShowTime.isSelected());
    }
    if(mShowToolTip != null) {
      Settings.propTrayImportantProgramsContainsToolTip.setBoolean(mShowToolTip.isSelected());
    }
    if(mPriority != null) {
      Settings.propTrayImportantProgramsPriority.setInt(mPriority.getSelectedIndex());
    }
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("important","Important programs");
  }
  
  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null) {
      mInstance.setEnabled(true);
    }
  }

}
