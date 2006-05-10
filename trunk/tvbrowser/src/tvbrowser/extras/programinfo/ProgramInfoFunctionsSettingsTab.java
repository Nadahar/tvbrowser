package tvbrowser.extras.programinfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

  private JCheckBox mShowFunctions, mShowTextSearchButton;
  
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,10dlu,pref:grow,5dlu","pref,5dlu,pref,1dlu,pref"));
    builder.setDefaultDialogBorder();
    
    mShowFunctions = new JCheckBox(ProgramInfo.mLocalizer.msg("showFunctions","Show Functions"),ProgramInfo.getInstance().isShowFunctions());
    mShowTextSearchButton  = new JCheckBox(ProgramInfo.mLocalizer.msg("showTextSearchButton","Show \"Search in program\""),ProgramInfo.getInstance().isShowTextSearchButton());
        
    builder.addSeparator(ProgramInfoDialog.mLocalizer.msg("functions","Functions"), cc.xyw(1,1,3));
    builder.add(mShowFunctions, cc.xyw(2,3,2));
    builder.add(mShowTextSearchButton, cc.xy(3,5));
    
    mShowTextSearchButton.setEnabled(mShowFunctions.isSelected());
    
    mShowFunctions.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mShowTextSearchButton.setEnabled(mShowFunctions.isSelected());
      }
    });
    
    return builder.getPanel();
  }

  public void saveSettings() {
    if(mShowFunctions != null)
      ProgramInfo.getInstance().setShowFunctions(mShowFunctions.isSelected());
    if(mShowTextSearchButton != null)
      ProgramInfo.getInstance().setShowTextSearchButton(mShowTextSearchButton.isSelected());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return ProgramInfoDialog.mLocalizer.msg("functions","Functions");
  }

}
