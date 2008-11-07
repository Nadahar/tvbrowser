package dataviewerplugin;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * Settings tab for DataViewerPlugin
 * 
 * @author René Mach
 */
public class DataViewerPluginSettingsTab implements SettingsTab {
  private JSpinner mGapSpinner;
  
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default,5dlu,default,5dlu,default","5dlu,default"));
    
    mGapSpinner = new JSpinner(new SpinnerNumberModel(DataViewerPlugin.getInstance().getAcceptableGap(),1,15,1));
    
    pb.addLabel(DataViewerPlugin.mLocalizer.msg("settings1","Don't show gaps less than/equal to"), cc.xy(2,2));
    pb.add(mGapSpinner, cc.xy(4,2));
    pb.addLabel(DataViewerPlugin.mLocalizer.msg("settings2","minutes as error."), cc.xy(6,2));
    
    return pb.getPanel();
  }

  public Icon getIcon() {
    return DataViewerPlugin.getInstance().getIcon();
  }

  public String getTitle() {
    return DataViewerPlugin.mLocalizer.msg("data","Data viewer");
  }

  public void saveSettings() {
    DataViewerPlugin.getInstance().setAcceptableGap(((Integer)mGapSpinner.getValue()).intValue());
  }

}
