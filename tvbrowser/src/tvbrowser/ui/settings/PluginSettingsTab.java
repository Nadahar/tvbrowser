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

import util.ui.TabLayout;

import tvbrowser.ui.customizableitems.*;
import tvbrowser.core.*;

import devplugin.Plugin;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class PluginSettingsTab
  implements devplugin.SettingsTab, CustomizableItemsListener
{
  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PluginSettingsTab.class);
  
  private JPanel mSettingsPn;
  
  private CustomizableItemsPanel panel;
  private PluginInfoPanel pluginInfoPanel;
  private HashSet buttonPluginSet;
  private JCheckBox addPicBtnCheckBox;
  private Plugin curSelectedPlugin=null;

  
  
  public PluginSettingsTab() {
  }
  
  
  
  private void showPluginInfo(Plugin plugin) {
    pluginInfoPanel.setPluginInfo(plugin.getInfo());
  }
  
  
  
  public void leftListSelectionChanged(ListSelectionEvent event) {
    PluginItem item = (PluginItem)panel.getLeftSelection();
    if (item != null) {
      curSelectedPlugin = item.getPlugin();
      
      showPluginInfo(curSelectedPlugin);
      addPicBtnCheckBox.setSelected(buttonPluginSet.contains(item.getPlugin()));
    }
  }
  
  
  
  public void rightListSelectionChanged(ListSelectionEvent event) {
    PluginItem item = (PluginItem)panel.getRightSelection();
    if (item != null) {
      curSelectedPlugin = item.getPlugin();
      
      showPluginInfo(curSelectedPlugin);
      addPicBtnCheckBox.setEnabled(item.getPlugin().getButtonText() != null);
      addPicBtnCheckBox.setSelected(buttonPluginSet.contains(item.getPlugin()));
    }
  }
 
  
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    String msg;

    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    String[] buttonPlugins=Settings.getButtonPlugins();
    buttonPluginSet = new HashSet();
    for (int i = 0; i < buttonPlugins.length; i++) {
      Plugin plugin = PluginManager.getPlugin(buttonPlugins[i]);
      if (plugin != null) {
        buttonPluginSet.add(plugin);
      }
    }
    
    pluginInfoPanel=new PluginInfoPanel();
    
    pluginInfoPanel=new PluginInfoPanel();
    
    String leftText = mLocalizer.msg("availablePlugins", "Available plugins");
    String rightText = mLocalizer.msg("subscribedPlugins", "Subscribed plugins");
    panel = CustomizableItemsPanel.createCustomizableItemsPanel(leftText, rightText);

    // Init the not installed plugins
    Plugin[] availablePluginArr = PluginManager.getAvailablePlugins();
    for (int i = 0; i < availablePluginArr.length; i++) {
      if (! PluginManager.isInstalled(availablePluginArr[i])) {
        panel.addElementLeft(new PluginItem(availablePluginArr[i]));
      }
    }

    // Init the installed plugins
    Plugin[] installedPluginArr = PluginManager.getInstalledPlugins();
    for (int i = 0; i < installedPluginArr.length; i++) {
      panel.addElementRight(new PluginItem(installedPluginArr[i]));
    }
    
    panel.addListSelectionListenerLeft(this);
    panel.addListSelectionListenerRight(this);
    
    mSettingsPn.add(panel, BorderLayout.CENTER);
    
    JPanel southPn = new JPanel(new TabLayout(1));
    mSettingsPn.add(southPn, BorderLayout.SOUTH);
    
    msg = mLocalizer.msg("selectedPlugin", "Selected Plugin");
    pluginInfoPanel.setBorder(BorderFactory.createTitledBorder(msg));
    
    southPn.add(pluginInfoPanel);
        
    JPanel panel1=new JPanel(new BorderLayout());
	String checkBoxText = mLocalizer.msg("toolbarPlugin", "Add plugin to Toolbar");
    addPicBtnCheckBox=new JCheckBox(checkBoxText);
    panel1.add(addPicBtnCheckBox,BorderLayout.WEST);
    southPn.add(panel1);
    
    addPicBtnCheckBox.addActionListener(new ActionListener() {  
      public void actionPerformed(ActionEvent e) {
        if (curSelectedPlugin==null) {
          return;
        }
        if (addPicBtnCheckBox.isSelected()) {
          buttonPluginSet.add((Plugin) curSelectedPlugin);
        }else{
          buttonPluginSet.remove((Plugin) curSelectedPlugin);
        }
      }
    });
    addPicBtnCheckBox.setEnabled(false);
    
    return mSettingsPn;
  }


  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    // Get the plugins that should be installed
	Object[] selectionArr = panel.getElementsRight();
    Plugin[] pluginArr = new Plugin[selectionArr.length];
    for (int i = 0; i < selectionArr.length; i++) {
      PluginItem item = (PluginItem) selectionArr[i];
      pluginArr[i] = item.getPlugin();
    }

    // Set the new plugins
    PluginManager.setInstalledPlugins(pluginArr);
    
    // Find out the plugins that should have a button
	ArrayList buttonPluginClassNameList = new ArrayList();
    for (int i = 0; i < pluginArr.length; i++) {
      if (buttonPluginSet.contains(pluginArr[i])) {
        buttonPluginClassNameList.add(pluginArr[i].getClass().getName());
      }
    }
    
    // Convert the buttonPluginList to an array    
    String[] buttonPluginClassNameArr = new String[buttonPluginClassNameList.size()];
    buttonPluginClassNameList.toArray(buttonPluginClassNameArr);

    // Set the buttonPluginArr
    Settings.setButtonPlugins(buttonPluginClassNameArr);
  }

  
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }
  
  
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("plugins", "Plugins");
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
      descriptionArea.setWrapStyleWord(true);
      descriptionArea.setEditable(false);
      descriptionArea.setOpaque(false);
      
      panel3.add(panel5,BorderLayout.CENTER);
      panel4.add(descriptionArea,BorderLayout.CENTER);
      
      leftPanel.add(panel1,BorderLayout.CENTER);
      rightPanel.add(panel2,BorderLayout.CENTER);
      
      add(leftPanel,BorderLayout.WEST);
      add(rightPanel,BorderLayout.CENTER);
    }
    
    
    
    public void setPluginInfo(devplugin.PluginInfo info) {
      nameLabel.setText(info.getName());
      versionLabel.setText(info.getVersion().toString());
      authorLabel.setText(info.getAuthor());
      descriptionArea.setText(info.getDescription());
    }
    
  } // inner class PluginInfoPanel
  
}