package tvbrowser.extras.programinfo;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The functions settings for the ProgramInfo.
 * 
 * @author René Mach
 *
 */
public class ProgramInfoFunctionsSettingsTab implements SettingsTab {

  private JCheckBox mShowFunctions;
  
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,pref:grow,5dlu","pref,5dlu,pref"));
    builder.setDefaultDialogBorder();
    
    mShowFunctions = new JCheckBox(ProgramInfo.mLocalizer.msg("showFunctions","Show Functions"),ProgramInfo.getInstance().isShowFunctions());
        
    builder.addSeparator(ProgramInfoDialog.mLocalizer.msg("functions","Functions"), cc.xyw(1,1,3));
    builder.add(mShowFunctions, cc.xy(2,3));
    
    return builder.getPanel();
  }

  public void saveSettings() {
    if(mShowFunctions != null)
      ProgramInfo.getInstance().setShowFunctions(mShowFunctions.isSelected());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return ProgramInfoDialog.mLocalizer.msg("functions","Functions");
  }

}
