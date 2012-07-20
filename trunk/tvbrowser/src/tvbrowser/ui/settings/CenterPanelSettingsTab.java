/*
 * TV-Browser
 * Copyright (C) 2012 TV-Browser team (dev@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.customizableitems.SelectableItemList;
import util.ui.customizableitems.SortableItemList;

import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.SettingsTab;

public class CenterPanelSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(CenterPanelSettingsTab.class);

  private OrderChooser mPanelChooser;
  private JCheckBox mTabBarAlwaysVisible;
  private ArrayList<PluginCenterPanel> mAllPanelList;
  
  @Override
  public JPanel createSettingsPanel() {
    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    
    mAllPanelList = new ArrayList<PluginCenterPanel>();
    ArrayList<PluginCenterPanel> currentOrderList = new ArrayList<PluginCenterPanel>(); 
    
    mAllPanelList.add(MainFrame.getInstance().getProgramTableScrollPaneWrapper());

    for(PluginProxy plugin : plugins) {
      try {
        PluginCenterPanelWrapper wrapper = plugin.getPluginCenterPanelWrapper();
        
        if(wrapper != null) {
          PluginCenterPanel[] panels = wrapper.getCenterPanels();
          
          for(PluginCenterPanel panel : panels) {
            if(panel != null && panel.getName() != null && panel.getPanel() != null && panel.getId() != null) {
              mAllPanelList.add(panel);
            }
          }
        }
        // Prevent problems from Plugins
      }catch(Throwable t) {}
    }
    
    for(String id : Settings.propCenterPanelArr.getStringArray()) {
      for(PluginCenterPanel centerPanel : mAllPanelList) {
        if(id.equals(centerPanel.getId())) {
          currentOrderList.add(centerPanel);  
        }
      }
    }
    
    mPanelChooser = new OrderChooser(currentOrderList.toArray(), mAllPanelList.toArray());
    mTabBarAlwaysVisible = new JCheckBox(mLocalizer.msg("alwaysShowTabs", "Always show tabs"), Settings.propAlwaysShowTabBarForCenterPanel.getBoolean());
    
    CellConstraints cc = new CellConstraints();
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default:grow,5dlu","default,5dlu,fill:default:grow,5dlu,default"));
    
    pb.setDefaultDialogBorder();
    
    pb.addSeparator(mLocalizer.msg("info", "Shown tabs in the main window"), cc.xyw(1, 1, 3));
    pb.add(mPanelChooser, cc.xy(2,3));
    pb.add(mTabBarAlwaysVisible, cc.xy(2, 5));
    
    return pb.getPanel();
  }

  @Override
  public void saveSettings() {
    Object[] order = mPanelChooser.getOrder();
    ArrayList<String> idList = new ArrayList<String>(order.length);
    
    for(Object o : order) {
      if(o != null) {
        mAllPanelList.remove(o);
        idList.add(((PluginCenterPanel)o).getId());
      }
    }
    
    if(idList.isEmpty()) {
      idList.add(MainFrame.getInstance().getProgramTableScrollPaneWrapper().getId());
    }
    
    ArrayList<String> disabledIdList = new ArrayList<String>(mAllPanelList.size());
    
    for(PluginCenterPanel centerPanel :  mAllPanelList) {
      disabledIdList.add(centerPanel.getId());
    }
    
    Settings.propCenterPanelArr.setStringArray(idList.toArray(new String[idList.size()]));
    Settings.propAlwaysShowTabBarForCenterPanel.setBoolean(mTabBarAlwaysVisible.isSelected());
    Settings.propDisabledCenterPanelArr.setStringArray(disabledIdList.toArray(new String[disabledIdList.size()]));
  }

  @Override
  public Icon getIcon() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTitle() {
    return mLocalizer.msg("title", "Main window");
  }
}
