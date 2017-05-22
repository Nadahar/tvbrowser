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
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.SettingsTab;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.ui.DontShowAgainOptionBox;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.UiUtilities;

public class CenterPanelSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(CenterPanelSettingsTab.class);

  private OrderChooser<PluginCenterPanel> mPanelChooser;
  private JCheckBox mTabBarAlwaysVisible;
  private ArrayList<PluginCenterPanel> mAllPanelList;
  
  private JRadioButton mNameOnly, mIconOnly, mNameAndIcon;
  
  @Override
  public JPanel createSettingsPanel() {
    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
    
    mAllPanelList = new ArrayList<PluginCenterPanel>();
    ArrayList<PluginCenterPanel> currentOrderList = new ArrayList<PluginCenterPanel>(); 
    
    mAllPanelList.add(MainFrame.getInstance().getProgramTableScrollPaneWrapper());
    mAllPanelList.add(MainFrame.getInstance().getPluginViewWrapper());

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
    
    for(InternalPluginProxyIf internalPlugin : internalPlugins) {
      try {
        PluginCenterPanelWrapper wrapper = internalPlugin.getPluginCenterPanelWrapper();
        
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
    
    mPanelChooser = new OrderChooser<>(currentOrderList.toArray(new PluginCenterPanel[currentOrderList.size()]), mAllPanelList.toArray(new PluginCenterPanel[mAllPanelList.size()]));
    mTabBarAlwaysVisible = new JCheckBox(mLocalizer.msg("alwaysShowTabs", "Always show tabs"), Settings.propAlwaysShowTabBarForCenterPanel.getBoolean());
    
    mNameOnly = new JRadioButton(mLocalizer.msg("nameOnly", "Name only"), Settings.propTabBarCenterPanelNameIconConfig.getInt() == Settings.VALUE_NAME_ONLY);
    mIconOnly = new JRadioButton(mLocalizer.msg("iconOnly", "Icon only (if available)"), Settings.propTabBarCenterPanelNameIconConfig.getInt() == Settings.VALUE_ICON_ONLY);
    mNameAndIcon = new JRadioButton(mLocalizer.msg("nameAndIcon", "Name and icon"), Settings.propTabBarCenterPanelNameIconConfig.getInt() == Settings.VALUE_NAME_AND_ICON);
    
    final ButtonGroup bg = new ButtonGroup();
    
    bg.add(mNameOnly);
    bg.add(mIconOnly);
    bg.add(mNameAndIcon);
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default:grow,5dlu",
        "default,5dlu,fill:default:grow,5dlu,default,10dlu,default,5dlu,default,1dlu,default,1dlu,default"));
    
    pb.border(Borders.DIALOG);
    
    pb.addSeparator(mLocalizer.msg("info", "Shown tabs in the main window"), CC.xyw(1, 1, 3));
    pb.add(mPanelChooser, CC.xy(2,3));
    pb.add(mTabBarAlwaysVisible, CC.xy(2, 5));
    
    pb.addSeparator(mLocalizer.msg("nameAndIconSep", "Name and icon display"), CC.xyw(1, 7, 3));
    pb.add(mNameOnly, CC.xy(2, 9));
    pb.add(mIconOnly, CC.xy(2, 11));
    pb.add(mNameAndIcon, CC.xy(2, 13));
    
    return pb.getPanel();
  }

  @Override
  public void saveSettings() {
    List<PluginCenterPanel> order = mPanelChooser.getOrderList();
    ArrayList<String> idList = new ArrayList<String>(order.size());
    
    for(PluginCenterPanel panel : order) {
      if(panel != null) {
        mAllPanelList.remove(panel);
        idList.add(panel.getId());
      }
    }
    
    if(!idList.contains(MainFrame.getInstance().getProgramTableScrollPaneWrapper().getId())) {
      String[] options = new String[] {mLocalizer.msg("programTableTabKeepDeactivated", "Keep deactivated"),mLocalizer.msg("programTableTabActivate", "Activate program table tab again")};
      
      if(DontShowAgainOptionBox.showOptionDialog("CenterPanelSettings.programTableTabdeselected", UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("programTableDeselected", "You have deselected the program table you might miss some programs in the future.\nAre you sure?"),mLocalizer.msg("programTableDeselectedTitle", "Program table deselected"),JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION,options,options[1],null) == JOptionPane.NO_OPTION) {
        idList.add(0, MainFrame.getInstance().getProgramTableScrollPaneWrapper().getId());
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
    
    int selection = Settings.VALUE_NAME_AND_ICON;
    
    if(mNameOnly.isSelected()) {
      selection = Settings.VALUE_NAME_ONLY;
    }
    else if(mIconOnly.isSelected()) {
      selection = Settings.VALUE_ICON_ONLY;
    }
    
    Settings.propTabBarCenterPanelNameIconConfig.setInt(selection);
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getTitle() {
    return mLocalizer.msg("title", "Main window");
  }
}
