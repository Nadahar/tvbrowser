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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import tvbrowser.ui.customizableitems.*;
import tvbrowser.core.*;

import devplugin.Plugin;
import devplugin.PluginInfo;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class PluginSettingsTab extends devplugin.SettingsTab implements CustomizableItemsListener {
  
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(PluginSettingsTab.class);
  
  private CustomizableItemsPanel panel;
  private PluginInfoPanel pluginInfoPanel;
  private HashSet buttonPluginSet;
  private JCheckBox addPicBtnCheckBox;
  private Plugin curSelectedPlugin=null;
  
  public String getName() {
    return mLocalizer.msg("plugins", "Plugins");
  }
  
  public void ok() {
  	
	ArrayList oldPlugins=new ArrayList();
	ArrayList newPlugins=new ArrayList();
	ArrayList buttonPlugins=new ArrayList();
	Object[] o;
	int i;
  	
	o=Settings.getInstalledPlugins();
	for (i=0;i<o.length;i++) {
		oldPlugins.add(o[i]);
	}
  	
	o=panel.getElementsRight();
	for (i=0;i<o.length;i++) {
		newPlugins.add(o[i]);
	}
  	
	oldPlugins.removeAll(newPlugins); 
	// oldPlugins contains now all unused plugins;
  	
	Iterator it=oldPlugins.iterator();  // uninstall unused plugins
	while (it.hasNext()) {
		String cur=(String)it.next();
		PluginManager.uninstallPlugin(cur);
	}
  	
	String[] instPlugins=new String[newPlugins.size()];
	it=newPlugins.iterator();
	i=0;
	while (it.hasNext()) {
		
		Plugin obj=((PluginItem)it.next()).getPlugin();
		//System.out.println(obj.getClass());
		//String cur=(String)obj;
		String cur=obj.getClass().getName();
		if (!PluginManager.isInstalled(cur)) {
			PluginManager.installPlugin(cur);
		}
		instPlugins[i]=cur;
		if (buttonPluginSet.contains(cur)) {
			buttonPlugins.add(cur);
		}
		i++;
	}
	
	Settings.setInstalledPlugins(instPlugins);
  	
	o=buttonPlugins.toArray();
	String []s=new String[o.length];
	for (i=0;i<o.length;i++) {
		s[i]=(String)o[i];
	}
	
	Settings.setButtonPlugins(s);
	   
	   
 }
  
  
  public PluginSettingsTab() {
    super();
    
    String[] buttonPlugins=Settings.getButtonPlugins();
    buttonPluginSet=new HashSet();
    for (int i=0;i<buttonPlugins.length;i++) {
      buttonPluginSet.add(buttonPlugins[i]);
    }
    
    setLayout(new BorderLayout());
    JPanel content=new JPanel();
    content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
    
    pluginInfoPanel=new PluginInfoPanel();
    
    
    setLayout(new BorderLayout());
    
    String msg;
    
    pluginInfoPanel=new PluginInfoPanel();
    
    String leftText = mLocalizer.msg("availablePlugins", "Available plugins");
    String rightText = mLocalizer.msg("subscribedPlugins", "Subscribed plugins");
    panel = CustomizableItemsPanel.createCustomizableItemsPanel(leftText, rightText);
    
    Object[] o=PluginManager.getAvailablePlugins();
    for (int i=0;i<o.length;i++) {
      Plugin plugin = (Plugin)o[i];
      if (PluginManager.isInstalled(plugin.getClass().getName())) {
        panel.addElementRight(new PluginItem(plugin));
      } else {
        panel.addElementLeft(new PluginItem(plugin));
      }
    }
    
    panel.addListSelectionListenerLeft(this);
    panel.addListSelectionListenerRight(this);
    
    // add(panel,BorderLayout.NORTH);
    
    content.add(panel);
    
    // JPanel panel1=new JPanel(new BorderLayout());
    msg = mLocalizer.msg("selectedPlugin", "Selected Plugin");
    pluginInfoPanel.setBorder(BorderFactory.createTitledBorder(msg));
    
    //panel1.add(pluginInfoPanel,BorderLayout.NORTH);
    
    // add(panel1,BorderLayout.CENTER);
    content.add(pluginInfoPanel);
    
    
    JPanel panel1=new JPanel(new BorderLayout());
    addPicBtnCheckBox=new JCheckBox("Add plugin to Toolbar");
    panel1.add(addPicBtnCheckBox,BorderLayout.WEST);
    content.add(panel1);
    
    addPicBtnCheckBox.addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
        if (curSelectedPlugin==null) {
          return;
        }
        String pluginName=((Plugin)curSelectedPlugin).getClass().getName();
        if (addPicBtnCheckBox.isSelected()) {
          buttonPluginSet.add(pluginName);
        }else{
          buttonPluginSet.remove(pluginName);
        }
      }
      
    }
    );
    
    add(content,BorderLayout.NORTH);
    
    
    
/*
 
    JPanel panel1=new JPanel(new BorderLayout());
    msg = mLocalizer.msg("selectedPlugin", "Selected Plugin");
    pluginInfoPanel.setBorder(BorderFactory.createTitledBorder(msg));
 
    panel1.add(pluginInfoPanel,BorderLayout.NORTH);
 
    add(panel1,BorderLayout.CENTER);
 */
  }
  
  
  
  private void showPluginInfo(Plugin plugin) {
    pluginInfoPanel.setPluginInfo(plugin.getInfo());
  }
  
  
  
  public void leftListSelectionChanged(ListSelectionEvent event) {
    PluginItem item = (PluginItem)panel.getLeftSelection();
    if (item != null) {
      curSelectedPlugin = item.getPlugin();
      
      showPluginInfo(curSelectedPlugin);
      String pluginClassName = item.getPlugin().getClass().getName();
      addPicBtnCheckBox.setSelected(buttonPluginSet.contains(pluginClassName));
    }
  }
  
  
  
  public void rightListSelectionChanged(ListSelectionEvent event) {
    PluginItem item = (PluginItem)panel.getRightSelection();
    if (item != null) {
      curSelectedPlugin = item.getPlugin();
      
      showPluginInfo(curSelectedPlugin);
      addPicBtnCheckBox.setEnabled(item.getPlugin().getButtonText() != null);
      String pluginClassName = item.getPlugin().getClass().getName();
      addPicBtnCheckBox.setSelected(buttonPluginSet.contains(pluginClassName));
    }
  }
  
  
  // inner class PluginItem
  
  
  class PluginItem {
    
    private Plugin mPlugin;
    
    
    public PluginItem(Plugin plugin) {
      mPlugin = plugin;
    }
    
    
    public Plugin getPlugin() {
      return mPlugin;
    }
    
    
    public String toString() {
      return mPlugin.getInfo().getName();
    }
    
  } // inner class PluginItem
  
  
  // inner class PluginInfoPanel
  
  
  class PluginInfoPanel extends JPanel {
    
    private JLabel nameLabel;
    private JLabel versionLabel;
    private JLabel authorLabel;
    private JTextArea descriptionArea;
    
    public PluginInfoPanel() {
      setLayout(new BorderLayout(10,0));
      
      String msg;
      
      JPanel leftPanel=new JPanel(new BorderLayout());
      JPanel rightPanel=new JPanel(new BorderLayout());
      
      msg = mLocalizer.msg("name", "Name");
      leftPanel.add(new JLabel(msg), BorderLayout.NORTH);
      rightPanel.add(nameLabel=new JLabel("-"),BorderLayout.NORTH);
      
      JPanel panel1=new JPanel(new BorderLayout());
      JPanel panel2=new JPanel(new BorderLayout());
      
      msg = mLocalizer.msg("version", "Version");
      panel1.add(new JLabel(msg), BorderLayout.NORTH);
      panel2.add(versionLabel=new JLabel("-"),BorderLayout.NORTH);
      
      JPanel panel3=new JPanel(new BorderLayout());
      JPanel panel4=new JPanel(new BorderLayout());
      
      msg = mLocalizer.msg("author", "Author");
      panel3.add(new JLabel(msg), BorderLayout.NORTH);
      panel4.add(authorLabel=new JLabel("-"),BorderLayout.NORTH);
      
      panel1.add(panel3,BorderLayout.CENTER);
      panel2.add(panel4,BorderLayout.CENTER);
      
      JPanel panel5=new JPanel(new BorderLayout());
      msg = mLocalizer.msg("description", "Description");
      panel5.add(new JLabel(msg), BorderLayout.NORTH);
      
      descriptionArea=new JTextArea(3,40);
      descriptionArea.setLineWrap(true);
      descriptionArea.setEditable(false);
      descriptionArea.setOpaque(false);
      
      panel3.add(panel5,BorderLayout.CENTER);
      panel4.add(descriptionArea,BorderLayout.CENTER);
      
      leftPanel.add(panel1,BorderLayout.CENTER);
      rightPanel.add(panel2,BorderLayout.CENTER);
      
      add(leftPanel,BorderLayout.WEST);
      add(rightPanel,BorderLayout.CENTER);
    }
    
    
    
    public void setPluginInfo(PluginInfo info) {
      nameLabel.setText(info.getName());
      versionLabel.setText(info.getVersion().toString());
      authorLabel.setText(info.getAuthor());
      descriptionArea.setText(info.getDescription());
    }
    
  } // inner class PluginInfoPanel
  
}