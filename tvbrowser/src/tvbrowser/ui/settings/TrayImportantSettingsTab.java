package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import tvbrowser.core.Settings;
import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The settings tab for the IMPORTANT_TYPE of the ProgramMenuItem.
 *  
 * @author René Mach
 *
 */
public class TrayImportantSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  
  private JCheckBox mIsEnabled, mShowName, mShowIcon, mShowDate, mShowTime, mShowToolTip;
  private JRadioButton mShowInSubMenu, mShowInTray;
  private JSpinner mSize;
  
  
  public JPanel createSettingsPanel() {
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref,5dlu,pref,pref:grow,5dlu",
        "pref,5dlu,pref,pref,pref,pref,10dlu,pref,pref,pref,pref,pref"));
    builder.setDefaultDialogBorder();
    
    mIsEnabled = new JCheckBox(mLocalizer.msg("importantEnabled","Show important programs"),Settings.propTrayImportantProgramsEnabled.getBoolean());
    mIsEnabled.setToolTipText(mLocalizer.msg("importantToolTip","Important programs are all marked programs."));
    
    ButtonGroup bg = new ButtonGroup();
    
    mShowInSubMenu = new JRadioButton(mLocalizer.msg("inSubMenu","in a sub menu"),Settings.propTrayImportantProgramsInSubMenu.getBoolean());
    mShowInTray = new JRadioButton(mLocalizer.msg("inTray","in the tray menu"), !mShowInSubMenu.isSelected());
    
    bg.add(mShowInSubMenu);
    bg.add(mShowInTray);
    
    mSize = new JSpinner(new SpinnerNumberModel(Settings.propTrayImportantProgramsSize.getInt(), 1, 10, 1));
    
    mShowName = new JCheckBox(mLocalizer.msg("showName","Show channel name"),Settings.propTrayImportantProgramsContainsName.getBoolean());
    mShowIcon = new JCheckBox(mLocalizer.msg("showIcon","Show channel icon"),Settings.propTrayImportantProgramsContainsIcon.getBoolean());
    mShowDate = new JCheckBox(mLocalizer.msg("showDate","Show date"),Settings.propTrayImportantProgramsContainsDate.getBoolean());
    mShowTime = new JCheckBox(mLocalizer.msg("showTime","Show start time"),Settings.propTrayImportantProgramsContainsTime.getBoolean());
    mShowToolTip = new JCheckBox(mLocalizer.msg("showToolTip","Show additional information of the program in a tool tip"),Settings.propTrayImportantProgramsContainsToolTip.getBoolean());
    mShowToolTip.setToolTipText(mLocalizer.msg("toolTipTip","Tool tips are small helper to something, like this one."));
        
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg("important","Important programs"), cc.xyw(1,1,6));
    builder.add(mIsEnabled, cc.xyw(2,3,4));
    builder.add(mShowInTray, cc.xyw(2,4,4));
    builder.add(mShowInSubMenu, cc.xyw(2,5,4));
    final JLabel sizeLabel = builder.addLabel(mLocalizer.msg("importantSize","Number of shown programs:"), cc.xy(2,6));
    builder.add(mSize, cc.xy(4,6));
    
    builder.add(mShowName, cc.xyw(2,8,4));
    builder.add(mShowIcon, cc.xyw(2,9,4));
    builder.add(mShowDate, cc.xyw(2,10,4));
    builder.add(mShowTime, cc.xyw(2,11,4));
    builder.add(mShowToolTip, cc.xyw(2,12,4));
    
    c.getComponent(0).setEnabled(Settings.propTrayIsEnabled.getBoolean());
    mIsEnabled.setEnabled(Settings.propTrayIsEnabled.getBoolean());
    mShowInSubMenu.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowInTray.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowName.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowIcon.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowDate.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowTime.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    mShowToolTip.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    sizeLabel.setEnabled(mIsEnabled.isSelected() && Settings.propTrayIsEnabled.getBoolean());
    
    mIsEnabled.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mShowInSubMenu.setEnabled(mIsEnabled.isSelected());
        mShowInTray.setEnabled(mIsEnabled.isSelected());
        mShowName.setEnabled(mIsEnabled.isSelected());
        mShowIcon.setEnabled(mIsEnabled.isSelected());
        mShowDate.setEnabled(mIsEnabled.isSelected());
        mShowTime.setEnabled(mIsEnabled.isSelected());
        mShowToolTip.setEnabled(mIsEnabled.isSelected());
        sizeLabel.setEnabled(mIsEnabled.isSelected());
      }
    });
    
    return builder.getPanel();
  }

  public void saveSettings() {
    if(mIsEnabled != null)
      Settings.propTrayImportantProgramsEnabled.setBoolean(mIsEnabled.isSelected());
    if(mShowInSubMenu != null)
      Settings.propTrayImportantProgramsInSubMenu.setBoolean(mShowInSubMenu.isSelected());
    if(mSize != null)
      Settings.propTrayImportantProgramsSize.setInt(((Integer)mSize.getValue()).intValue());
    if(mShowName != null)
      Settings.propTrayImportantProgramsContainsName.setBoolean(mShowName.isSelected());
    if(mShowIcon != null)
      Settings.propTrayImportantProgramsContainsIcon.setBoolean(mShowIcon.isSelected());
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

}
