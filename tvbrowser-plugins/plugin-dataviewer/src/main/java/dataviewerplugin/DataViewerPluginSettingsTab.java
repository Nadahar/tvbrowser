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
public final class DataViewerPluginSettingsTab implements SettingsTab {
  private JSpinner mGapSpinner;
  private JSpinner mDurationSpinner;
  
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default,5dlu,default,5dlu,default","5dlu,default,5dlu,default"));
    
    mGapSpinner = new JSpinner(new SpinnerNumberModel(DataViewerPlugin.getInstance().getAcceptableGap(),1,15,1));
    mDurationSpinner = new JSpinner(new SpinnerNumberModel(DataViewerPlugin.getInstance().getAcceptableDuration(),6,18,1));
    
    pb.addLabel(DataViewerPlugin.mLocalizer.msg("settings1","Don't show gaps less than/equal to"), cc.xy(2,2));
    pb.add(mGapSpinner, cc.xy(4,2));
    pb.addLabel(DataViewerPlugin.mLocalizer.msg("settings2","minutes as error."), cc.xy(6,2));
    pb.addLabel(DataViewerPlugin.mLocalizer.msg("settings3","Show error for days with programs with duartion more than"), cc.xy(2,4));
    pb.add(mDurationSpinner, cc.xy(4,4));
    pb.addLabel(DataViewerPlugin.mLocalizer.msg("settings4","hours."),cc.xy(6,4));
    
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
    DataViewerPlugin.getInstance().setAcceptableDuration(((Integer)mDurationSpinner.getValue()).intValue());
  }

}
