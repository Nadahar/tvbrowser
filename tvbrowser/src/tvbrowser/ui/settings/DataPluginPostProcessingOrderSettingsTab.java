package tvbrowser.ui.settings;

import java.util.Arrays;
import java.util.Comparator;

import javax.swing.Icon;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.customizableitems.SortableItemList;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;

import devplugin.SettingsTab;

public class DataPluginPostProcessingOrderSettingsTab implements SettingsTab {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(DataPluginPostProcessingOrderSettingsTab.class);
  
  private SortableItemList mSortablePluginList;
  
  @Override
  public JPanel createSettingsPanel() {
    PluginProxy[] available = PluginProxyManager.getInstance().getActivatedPlugins();
    
    final String[] sortedPlugins = Settings.propDataPluginPostProcessingOrder.getStringArray();
    
    Arrays.sort(available, new Comparator<PluginProxy>() {

      @Override
      public int compare(PluginProxy pp1, PluginProxy pp2) {
        int result = 0;
        
        int pp1Index = -1;
        int pp2Index = -1;
        
        for(int i = 0; i < sortedPlugins.length; i++) {
          if(pp1.getId().equals(sortedPlugins[i])) {
            pp1Index = i;
          }
          else if(pp2.getId().equals(sortedPlugins[i])) {
            pp2Index = i;
          }
        }
        
        if(pp1Index != -1 && pp2Index != -1) {
          if(pp1Index > pp2Index) {
            result = 1;
          }
          else if(pp1Index < pp2Index) {
            result = -1;
          }
        }
        else if(pp1Index == -1 && pp2Index == -1) {
          result = pp1.getInfo().getName().compareToIgnoreCase(pp2.getInfo().getName());
        }
        else if(pp1Index == -1) {
          result = -1;
        }
        else if(pp2Index == -1) {
          result = 1;
        }
        
        return result;
      }
    });
    
    mSortablePluginList = new SortableItemList("", available);
    
    final EnhancedPanelBuilder pb = new EnhancedPanelBuilder("0dlu,5dlu,default:grow,0dlu");
    pb.border(Borders.DIALOG);
    pb.addRow("default",false);
    pb.addSeparator(getTitle(), CC.xyw(2,pb.getRowCount(),2));
    pb.addRow("5dlu",false);
    pb.addRow("default",false);
    pb.add(UiUtilities.createHelpTextArea(LOCALIZER.msg("help", "The order set here is used to defines the priority every plugin has to post process and change the updated data. The plugin at the top of this is called last to post process to give it the chance to be the last to change data.")), CC.xy(3, pb.getRowCount()));
    pb.addRow("3dlu",false);
    pb.addRow("fill:300dlu:grow",false);
    pb.add(mSortablePluginList, CC.xy(3, pb.getRowCount()));
    
    return pb.getPanel();
  }

  @Override
  public void saveSettings() {
    Object[] sorted = mSortablePluginList.getItems();
    String[] result = new String[sorted.length];
    
    for(int i = 0; i < sorted.length; i++) {
      result[i] = ((PluginProxy)sorted[i]).getId();
    }
    
    Settings.propDataPluginPostProcessingOrder.setStringArray(result);
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getTitle() {
    return LOCALIZER.msg("title", "Data post processing");
  }
}
