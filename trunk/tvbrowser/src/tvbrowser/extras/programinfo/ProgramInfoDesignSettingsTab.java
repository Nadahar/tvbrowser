package tvbrowser.extras.programinfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import tvbrowser.core.icontheme.IconLoader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import devplugin.Plugin;
import devplugin.SettingsTab;

/**
 * The design settings for the ProgramInfo.
 * 
 * @author René Mach
 *
 */
public class ProgramInfoDesignSettingsTab implements SettingsTab {
  
  private String mOldLook;
  
  private JComboBox mLook;
  
  private String[] mLf = {
      "com.l2fprod.common.swing.plaf.aqua.AquaLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.metal.MetalLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.motif.MotifLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.windows.WindowsLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.windows.WindowsClassicLookAndFeelAddons"
  };
  
  public JPanel createSettingsPanel() {
    mOldLook = ProgramInfo.getInstance().getProperty("look", "");
    
    String[] lf = {"Aqua", "Metal", "Motif", "Windows XP",
    "Windows Classic"};
    
    mLook = new JComboBox(lf);
    
    String look = mOldLook.length() > 0 ? mOldLook : LookAndFeelAddons.getBestMatchAddonClassName();
    
    for(int i = 0; i < mLf.length; i++)
      if(look.toLowerCase().indexOf(mLf[i].toLowerCase()) != -1) {
        mLook.setSelectedIndex(i);
        break;
      }
    
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
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref,pref:grow","pref,5dlu,pref,fill:pref:grow,pref"));
    builder.setDefaultDialogBorder();
    
    builder.addSeparator(ProgramInfo.mLocalizer.msg("design","Design"), cc.xyw(1,1,3));
    builder.add(mLook, cc.xy(2,3));  
    
    FormLayout layout = new FormLayout("pref,pref:grow,pref","pref");
    layout.setColumnGroups(new int[][] {{1,3}});
    JPanel buttonPn = new JPanel(layout);
    buttonPn.add(previewBtn, cc.xy(3,1));
    buttonPn.add(defaultBtn, cc.xy(1,1));
    
    builder.add(buttonPn, cc.xyw(1,5,3));
    
    return builder.getPanel();
  }
  
  private void resetSettings() {
    String look = LookAndFeelAddons.getBestMatchAddonClassName();
    
    for(int i = 0; i < mLf.length; i++)
      if(look.toLowerCase().indexOf(mLf[i].toLowerCase()) != -1) {
        mLook.setSelectedIndex(i);
        break;
      }
  }

  public void saveSettings() {
    ProgramInfo.getInstance().getSettings().setProperty("look", mLf[mLook.getSelectedIndex()]);
    ProgramInfo.getInstance().setLook();
  }

  private void restoreSettings() {
    ProgramInfo.getInstance().getSettings().setProperty("look", mOldLook);
    ProgramInfo.getInstance().setLook();
  }
  
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "preferences-desktop-wallpaper", 16);
  }

  public String getTitle() {
    return ProgramInfo.mLocalizer.msg("design","Design");
  }

}
