/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
 * CVS information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.GenericFilterMap;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.filter.dlgs.EditFilterDlg;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItem;
import util.ui.customizableitems.SelectableItemList;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

public class GenericPluginFilterSettingsTab implements SettingsTab {
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(GenericPluginFilterSettingsTab.class); 

  private SelectableItemList mGenericPluginFilterList;
  private ArrayList<PluginProxy> mCurrentlySelecteedList; 
  
  @Override
  public JPanel createSettingsPanel() {
    PluginProxy[] currentlySelected = GenericFilterMap.getInstance().getActivatedGenericPluginFilterProxies();
    
    mCurrentlySelecteedList = new ArrayList<PluginProxy>();
    mCurrentlySelecteedList.addAll(Arrays.asList(currentlySelected));
    
    mGenericPluginFilterList = new SelectableItemList(currentlySelected, PluginProxyManager.getInstance().getActivatedPlugins());
    mGenericPluginFilterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    JScrollPane scrollPane = new JScrollPane(mGenericPluginFilterList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    
    final JButton edit = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT), TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    edit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SelectableItem item = (SelectableItem)mGenericPluginFilterList.getSelectedValue();
        PluginProxy proxy = (PluginProxy)item.getItem();
        
        UserFilter filter = GenericFilterMap.getInstance().getGenericPluginFilter(proxy, false);
        
        if(filter == null) {
          filter = new UserFilter(proxy.getInfo().getName());
        }
        
        EditFilterDlg editFilter = new EditFilterDlg(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), FilterList.getInstance(), filter, false);
        
        if(editFilter.getOkWasPressed()) {
          GenericFilterMap.getInstance().updateGenericPluginFilter(proxy, filter, item.isSelected());
        }
      }
    });
    edit.setEnabled(false);
    
    mGenericPluginFilterList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          edit.setEnabled(e.getFirstIndex() >= 0);
          
          if(edit.isEnabled()) {
            SelectableItem item = (SelectableItem)mGenericPluginFilterList.getSelectedValue();
            PluginProxy proxy = (PluginProxy)item.getItem();
            
            if(!mCurrentlySelecteedList.contains(proxy)) {
              mCurrentlySelecteedList.add(proxy);
            }
          }
        }
      }
    });
    
    FormLayout layout = new FormLayout("10dlu,100dlu:grow,default,5dlu","default,5dlu,default,5dlu,fill:default:grow,3dlu,default");
    
    PanelBuilder pb = new PanelBuilder(layout);
    pb.border(Borders.DIALOG);
    pb.addSeparator(getTitle(), CC.xyw(1,1,4));
    pb.add(UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help", 
        "Activate and setup filter for each plugin to pre filter the highlightings and the context menu of the plugin. (Only for programs that are accepted by an activated filter can be hightlighted by the plugin and context menu can be shown.)")),
        CC.xyw(2,3,2));
    pb.add(scrollPane, CC.xyw(2,5,2));
    pb.add(edit, CC.xy(3,7));
    
    return pb.getPanel();
  }

  @Override
  public void saveSettings() {
    Object[] selectedPlugins = mGenericPluginFilterList.getSelection();
    ArrayList<PluginProxy> newSelection = new ArrayList<PluginProxy>();
    
    for(Object o : selectedPlugins) {
      GenericFilterMap.getInstance().updateGenericPluginFilterActivated((PluginProxy)o, true);
      mCurrentlySelecteedList.remove(o);
      newSelection.add((PluginProxy)o);
    }
    
    for(PluginProxy unselected : mCurrentlySelecteedList) {
      GenericFilterMap.getInstance().updateGenericPluginFilterActivated((PluginProxy)unselected, false);
    }
    
    mCurrentlySelecteedList.clear();
    mCurrentlySelecteedList = null;
    mCurrentlySelecteedList = newSelection;
    
    GenericFilterMap.getInstance().storeGenericFilters();
  }

  @Override
  public Icon getIcon() {
    return TVBrowserIcons.filter(TVBrowserIcons.SIZE_SMALL);
  }

  @Override
  public String getTitle() {
    return LOCALIZER.msg("title", "Highlighting filters");
  }

}
