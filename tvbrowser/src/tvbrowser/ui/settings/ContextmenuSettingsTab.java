/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import devplugin.Plugin;
import devplugin.SettingsTab;
import tvbrowser.core.PluginManager;
import tvbrowser.core.Settings;
import tvbrowser.ui.customizableitems.SortableItemList;

 
public class ContextmenuSettingsTab implements devplugin.SettingsTab, ActionListener, SettingsChangeListener {


  class ContextMenuCellRenderer extends DefaultListCellRenderer {
    
    public Component getListCellRendererComponent(JList list, Object value,
         int index, boolean isSelected, boolean cellHasFocus) {
           
         JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
           index, isSelected, cellHasFocus);

         if (value instanceof Plugin) {
           Plugin plugin=(Plugin)value;
           JPopupMenu menu=new JPopupMenu();
           Font f;
           if (plugin.equals(mDefaultPlugin)) {
              f=new Font("Dialog",Font.BOLD,12);
           }
           else {
             f=new Font("Dialog",Font.PLAIN,12);
           }
           label.setFont(f);        
           label.setText(plugin.getContextMenuItemText());
           
           label.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
           label.setOpaque(false);
           label.setBackground(menu.getBackground());
           JPanel panel=new JPanel(new BorderLayout());
           panel.add(label,BorderLayout.CENTER);
           panel.add(new JLabel(plugin.getMarkIcon()),BorderLayout.WEST);
           if (isSelected) {
             panel.setBackground(Color.gray);
           }
           return panel;
         }

         return label;
       }

	
    
  }


  private JButton mDefaultPluginBt;
  private Plugin mDefaultPlugin;
  private SortableItemList mList;
  
  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ContextmenuSettingsTab.class);


	public JPanel createSettingsPanel() {
    
    String defaultPluginClassName = Settings.getDefaultContextMenuPlugin();
    mDefaultPlugin=PluginManager.getInstance().getPlugin(defaultPluginClassName);
    
    JPanel contentPanel=new JPanel(new BorderLayout(0,15));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(5,8,5,8));
    
    JPanel panel1=new JPanel();
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
    mList=new SortableItemList(mLocalizer.msg("title","context menu"));
    panel1.add(mList);
   
    mList.getList().setVisibleRowCount(10);
    
    mList.getList().addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e){
        if(e.getClickCount() == 2) {
          int inx = mList.getList().locationToIndex(e.getPoint());
          if (inx>=0) {
            Object item = mList.getList().getModel().getElementAt(inx);;
            mList.getList().ensureIndexIsVisible(inx);
            mDefaultPlugin=(Plugin)mList.getList().getSelectedValue();
            mList.updateUI();
          }          
        }
      }
    });
    
    fillListbox(PluginManager.getInstance().getContextMenuPlugins());
    
    mDefaultPluginBt=new JButton(mLocalizer.msg("defaultPluginBtn",""));
    mDefaultPluginBt.addActionListener(this);
    JPanel panel2=new JPanel(new BorderLayout());
    panel2.add(mDefaultPluginBt,BorderLayout.WEST);
    panel1.add(panel2);
    
    contentPanel.add(panel1,BorderLayout.NORTH);
    
    JTextArea descBox=new JTextArea(mLocalizer.msg("description",""));
    descBox.setFocusable(false);
    descBox.setOpaque(false);
    descBox.setWrapStyleWord(true);
    descBox.setLineWrap(true);
    
    contentPanel.add(descBox,BorderLayout.CENTER);
    
    mList.setCellRenderer(new ContextMenuCellRenderer());
    mList.getList().setOpaque(false);
  
		return contentPanel;
	}
  
  private void fillListbox(Plugin[] pluginList) {
    if (mList==null) {
      return;
    }
    mList.removeAllElements();
    
    for (int i=0;i<pluginList.length;i++) {
      if (pluginList[i].getContextMenuItemText()!=null) {
        mList.addElement(pluginList[i]);
      }
    }
    
  }
  
  public void actionPerformed(ActionEvent event) {
    Object o=event.getSource();
    if (o==mDefaultPluginBt) {
      mDefaultPlugin=(Plugin)mList.getList().getSelectedValue();
      mList.updateUI();
    }
    
  }

	
	public void saveSettings() {
		
    Object o[]=mList.getItems();
    
    if (!mList.contains(mDefaultPlugin)) {
      mDefaultPlugin=null;
    }
    
    if (mDefaultPlugin!=null) {      
        Settings.setDefaultContextMenuPlugin(mDefaultPlugin.getClass().getName());         
    }else{
      if (o.length>0) {
        Settings.setDefaultContextMenuPlugin(((Plugin)o[0]).getClass().getName());
      }
    }     
    
    String plugins[]=new String[o.length];
    for (int i=0;i<o.length;i++) {
      plugins[i]=((Plugin)o[i]).getClass().getName();
    }
    Settings.setContextMenuItemPlugins(plugins);    
	}

	
	public Icon getIcon() {
		return null;
	}

	
	public String getTitle() {
		return mLocalizer.msg("title","context menu");
	}

	
	public void settingsChanged(SettingsTab tab, Object obj) {
    fillListbox((Plugin[])obj);
		
	}
  
  
}
 
 