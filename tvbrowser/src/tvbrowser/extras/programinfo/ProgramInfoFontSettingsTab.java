package tvbrowser.extras.programinfo;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.icontheme.IconLoader;
import util.ui.FontChooserPanel;

import devplugin.Plugin;
import devplugin.SettingsTab;

/**
 * The font settings for the ProgramInfo.
 * 
 * @author René Mach
 *
 */
public class ProgramInfoFontSettingsTab implements SettingsTab {

  private JCheckBox mUserFont, mAntiAliasing;
  private FontChooserPanel mTitleFont, mBodyFont;
  
  private String mOldTitleFont, mOldBodyFont,
  mOldTitleFontSize, mOldBodyFontSize, mOldUserFontSelected,
  mOldAntiAliasingSelected;
  
  private Properties mSettings;
  
  /**
   * Constructor
   * 
   */
  public ProgramInfoFontSettingsTab() {
    mSettings = ProgramInfo.getInstance().getSettings();
  }
  
  public JPanel createSettingsPanel() {
    mOldAntiAliasingSelected = mSettings.getProperty("antialiasing", "false");
    mOldUserFontSelected = mSettings.getProperty("userfont", "false");
    mOldTitleFontSize = mSettings.getProperty("title", "18");
    mOldBodyFontSize = mSettings.getProperty("small", "11");
    mOldTitleFont = mSettings.getProperty("titlefont", "Verdana");
    mOldBodyFont = mSettings.getProperty("bodyfont", "Verdana");  
    
    mAntiAliasing = new JCheckBox(ProgramInfo.mLocalizer
        .msg("antialiasing", "Antialiasing"));
    mAntiAliasing.setSelected(mOldAntiAliasingSelected.compareToIgnoreCase("true") == 0);

    mUserFont = new JCheckBox(ProgramInfo.mLocalizer.msg("userfont", "Use user fonts"));
    mUserFont.setSelected(mOldUserFontSelected.compareToIgnoreCase("true") == 0);

    int size = Integer.parseInt(mOldTitleFontSize);

    mTitleFont = new FontChooserPanel(null,
        new Font(mOldTitleFont, Font.PLAIN, size), false);
    mTitleFont.setMaximumSize(mTitleFont.getPreferredSize());
    mTitleFont.setAlignmentX(FontChooserPanel.LEFT_ALIGNMENT);
    mTitleFont.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 0));

    size = Integer.parseInt(mOldBodyFontSize);
    
    mBodyFont = new FontChooserPanel(null, new Font(mOldBodyFont,
            Font.PLAIN, size), false);
    mBodyFont.setMaximumSize(mBodyFont.getPreferredSize());
    mBodyFont.setAlignmentX(FontChooserPanel.LEFT_ALIGNMENT);
    mBodyFont.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

    mTitleFont.setEnabled(mUserFont.isSelected());
    mBodyFont.setEnabled(mUserFont.isSelected());
    
    
    JButton previewBtn = new JButton(ProgramInfo.mLocalizer.msg("preview", "Prewview"));
    previewBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        ProgramInfo.getInstance().showProgramInformation(
            Plugin.getPluginManager().getExampleProgram(), false);
        restoreSettings();
      }
    });

    JButton defaultBtn = new JButton(ProgramInfo.mLocalizer.msg("default", "Default"));
    defaultBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetSettings();
      }
    });
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,10dlu,pref,pref,pref:grow,5dlu","pref,5dlu,pref,pref,pref,pref,fill:pref:grow,pref"));
    builder.setDefaultDialogBorder();
    
    builder.addSeparator(ProgramInfo.mLocalizer.msg("font","Font settings"), cc.xyw(1,1,6));
    builder.add(mAntiAliasing, cc.xyw(2,3,4));
    builder.add(mUserFont, cc.xyw(2,4,4));
    final JLabel titleLabel = builder.addLabel(ProgramInfo.mLocalizer.msg("title", "Title font"), cc.xy(3,5));
    builder.add(mTitleFont, cc.xy(4,5));
    final JLabel bodyLabel = builder.addLabel(ProgramInfo.mLocalizer.msg("body", "Description font"), cc.xy(3,6));
    builder.add(mBodyFont, cc.xy(4,6));
    
    mUserFont.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mTitleFont.setEnabled(mUserFont.isSelected());
        mBodyFont.setEnabled(mUserFont.isSelected());
        titleLabel.setEnabled(mUserFont.isSelected());
        bodyLabel.setEnabled(mUserFont.isSelected());
      }
    });
    
    mTitleFont.setEnabled(mUserFont.isSelected());
    mBodyFont.setEnabled(mUserFont.isSelected());
    titleLabel.setEnabled(mUserFont.isSelected());
    bodyLabel.setEnabled(mUserFont.isSelected());
    
    
    FormLayout layout = new FormLayout("pref,pref:grow,pref","pref");
    layout.setColumnGroups(new int[][] {{1,3}});
    JPanel buttonPn = new JPanel(layout);
    buttonPn.add(previewBtn, cc.xy(3,1));
    buttonPn.add(defaultBtn, cc.xy(1,1));
    
    builder.add(buttonPn, cc.xyw(1,8,6));
    
    return builder.getPanel();
  }

  private void resetSettings() {
    mAntiAliasing.setSelected(false);
    mUserFont.setSelected(false);
  }
  
  private void restoreSettings() {
    mSettings.setProperty("antialiasing", mOldAntiAliasingSelected);
    mSettings.setProperty("userfont", mOldUserFontSelected);
    mSettings.setProperty("titlefont", mOldTitleFont);
    mSettings.setProperty("title", mOldTitleFontSize);
    mSettings.setProperty("bodyfont", mOldBodyFont);
    mSettings.setProperty("small", mOldBodyFontSize);   
  }
  
  public void saveSettings() {
    mSettings.setProperty("antialiasing", String.valueOf(mAntiAliasing
        .isSelected()));
    mSettings.setProperty("userfont", String.valueOf(mUserFont.isSelected()));

    Font f = mTitleFont.getChosenFont();
    mSettings.setProperty("titlefont", f.getFamily());
    mSettings.setProperty("title", String.valueOf(f.getSize()));

    f = mBodyFont.getChosenFont();
    mSettings.setProperty("bodyfont", f.getFamily());
    mSettings.setProperty("small", String.valueOf(f.getSize()));
    
    ProgramInfo.getInstance().setLook();
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "preferences-desktop-font", 16);
  }

  public String getTitle() {
    return ProgramInfo.mLocalizer.msg("font","Font settings");
  }

}
