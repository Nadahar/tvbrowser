/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */


public class PluginSettingsTab implements devplugin.SettingsTab {
  
  class ContextMenuCellRenderer extends DefaultListCellRenderer {
    
     public Component getListCellRendererComponent(JList list, Object value,
          int index, boolean isSelected, boolean cellHasFocus) {
           
          JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
            index, isSelected, cellHasFocus);

          if (value instanceof PluginProxy) {
            PluginProxy plugin = (PluginProxy) value;
            JPopupMenu menu=new JPopupMenu();
            
            label.setEnabled(plugin.isActivated());
            //label.setEnabled(mActivatedPlugins.contains(plugin));
            label.setText(plugin.getInfo().getName());
           
            label.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
            label.setOpaque(false);
            label.setBackground(menu.getBackground());
            JPanel panel=new JPanel(new BorderLayout());
            panel.add(label,BorderLayout.CENTER);
            Icon ico=plugin.getMarkIcon();
            if (ico==null) {
              ico=new ImageIcon("imgs/Jar16.gif");
            }
            panel.add(new JLabel(ico),BorderLayout.WEST);
            if (isSelected) {
              panel.setBackground(Color.gray);
            }
            return panel;
          }

          return label;
        }

  
    
   }
  
  
  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(PluginSettingsTab.class);
  
  private JList mList;
  private JButton mStartStopBtn;
  private DefaultListModel mListModel;
  private Collection mChangeListener;
  private PluginInfoPanel mPluginInfoPanel;
  //private Collection mActivatedPlugins;
  
  public PluginSettingsTab() {
    mChangeListener=new HashSet();
  }

	public JPanel createSettingsPanel() {
    
    JPanel contentPanel=new JPanel(new BorderLayout());
    contentPanel.setBorder(BorderFactory.createEmptyBorder(5,8,5,8));
    
    JPanel pluginListPanel=new JPanel(new BorderLayout(5,5));
    
    mListModel=new DefaultListModel();
    mList=new JList(mListModel);
    mList.setCellRenderer(new ContextMenuCellRenderer());
    mList.setVisibleRowCount(10);
    mList.setOpaque(false);
    mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent evt) {
        updateBtns();
        PluginProxy plugin = (PluginProxy) mList.getSelectedValue();
        if (plugin != null) {
          mPluginInfoPanel.setPluginInfo(plugin.getInfo());
        }
      }
    });
    
    mList.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e){
        if(e.getClickCount() == 2) {
          int inx = mList.locationToIndex(e.getPoint());
          if (inx>=0) {
            Object item = mListModel.getElementAt(inx);;
            mList.ensureIndexIsVisible(inx);
            onStartStopBtnClicked((PluginProxy) item);
          }
        }
      }
    });
    
    //mActivatedPlugins=new HashSet();
    //Plugin pluginList[]=PluginManager.getInstance().getAvailablePlugins();
    PluginProxy[] pluginList = PluginProxyManager.getInstance().getAllPlugins();
    
    
    Arrays.sort(pluginList, new Comparator() {

        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    });    
    
    for (int i=0;i<pluginList.length;i++) {
      mListModel.addElement(pluginList[i]);
 //     if (PluginLoader.getInstance().isActivePlugin(pluginList[i])) {
 //       mActivatedPlugins.add(pluginList[i]);
 //     }
    }
    
    pluginListPanel.add(new JScrollPane(mList),BorderLayout.CENTER);
    pluginListPanel.add(new JLabel(mLocalizer.msg("availablePlugins", "Available plugins")),BorderLayout.NORTH);
    
    JPanel btnPanel=new JPanel(new BorderLayout());
    mStartStopBtn=new JButton();
    mStartStopBtn.setPreferredSize(new Dimension(140,30));
    mStartStopBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        PluginProxy plugin = (PluginProxy) mList.getSelectedValue();
        onStartStopBtnClicked(plugin);
      }
    });
    btnPanel.add(mStartStopBtn,BorderLayout.NORTH);
    pluginListPanel.add(btnPanel,BorderLayout.EAST);
    
    contentPanel.add(pluginListPanel,BorderLayout.NORTH); 
    
    
    JPanel infoPanel=new JPanel(new BorderLayout());
    contentPanel.add(infoPanel,BorderLayout.CENTER);
    
    mPluginInfoPanel=new PluginInfoPanel();
    infoPanel.add(mPluginInfoPanel,BorderLayout.NORTH);
    
    updateBtns();
    
		return contentPanel;
	}


  private void updateBtns() {
    PluginProxy plugin = (PluginProxy) mList.getSelectedValue();
    mStartStopBtn.setEnabled(plugin!=null);
    
    if ((plugin != null) && plugin.isActivated()) {
      mStartStopBtn.setEnabled(true);
      mStartStopBtn.setIcon(new ImageIcon("imgs/Stop24.gif"));
      mStartStopBtn.setText(mLocalizer.msg("deactivate",""));
    } else {
      mStartStopBtn.setEnabled(plugin != null);
      mStartStopBtn.setIcon(new ImageIcon("imgs/Refresh24.gif"));
      mStartStopBtn.setText(mLocalizer.msg("activate",""));
    }
  }


  private void onStartStopBtnClicked(PluginProxy plugin) {
    if (plugin != null) {
      try {
        if (plugin.isActivated()) {
          PluginProxyManager.getInstance().deactivatePlugin(plugin);
        } else {
          PluginProxyManager.getInstance().activatePlugin(plugin);
        }
      }
      catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
      
      mList.updateUI();
      updateBtns();          
    }    
    
    // Update the settings
    String[] deactivatedPlugins = PluginProxyManager.getInstance().getDeactivatedPluginIds();
    Settings.propDeactivatedPlugins.setStringArray(deactivatedPlugins);    
  }
  

  public void addSettingsChangeListener(SettingsChangeListener listener) {
    mChangeListener.add(listener);
  }

	public void saveSettings() {
    
	}

	
	public Icon getIcon() {
		return null;
	}

	
	public String getTitle() {
    return mLocalizer.msg("plugins", "Plugins");
	}
  
  
}

