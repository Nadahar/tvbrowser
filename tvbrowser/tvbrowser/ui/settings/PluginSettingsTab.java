/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package tvbrowser.ui.settings;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

import tvbrowser.ui.customizableitems.*;
import tvbrowser.core.*;

import devplugin.Plugin;
import devplugin.PluginInfo;

public class PluginSettingsTab extends devplugin.SettingsTab implements CustomizableItemsListener {

  private CustomizableItemsPanel panel;
  private PluginInfoPanel pluginInfoPanel;

  public String getName() {
    return "Plugins";
  }

  public void ok() {
    Object[] o=panel.getElementsRight();
    for (int i=0;i<o.length;i++) {
      PluginManager.installPlugin((String)o[i]);
    }
    Settings.setInstalledPlugins(o);

  }

  public PluginSettingsTab() {
    super();
    setLayout(new BorderLayout());

    pluginInfoPanel=new PluginInfoPanel();

    panel=CustomizableItemsPanel.createCustomizableItemsPanel("Available plugins:","Installed Plugins:");

    Object[] o=PluginManager.getAvailablePlugins();
    for (int i=0;i<o.length;i++) {
      String plugin=((Plugin)o[i]).getClass().getName();
      if (PluginManager.isInstalled(plugin)) {
        panel.addElementRight(plugin);
      }else{
        panel.addElementLeft(plugin);
      }
    }

    panel.addListSelectionListenerLeft(this);
    panel.addListSelectionListenerRight(this);

    add(panel,BorderLayout.NORTH);

    JPanel panel1=new JPanel(new BorderLayout());

    pluginInfoPanel.setBorder(BorderFactory.createTitledBorder("Selected Plugin:"));

    panel1.add(pluginInfoPanel,BorderLayout.NORTH);

    add(panel1,BorderLayout.CENTER);
  }

  private void showPluginInfo(String pluginName) {
    Plugin plugin=PluginManager.getPlugin(pluginName);
    if (plugin==null) {

    }else{
      pluginInfoPanel.setPluginInfo(plugin.getInfo());
    }
  }

  public void leftListSelectionChanged(ListSelectionEvent event) {
    showPluginInfo(panel.getLeftSelection());
  }

  public void rightListSelectionChanged(ListSelectionEvent event) {
    showPluginInfo(panel.getRightSelection());
  }
}

class PluginInfoPanel extends JPanel {

  private JLabel nameLabel;
  private JLabel versionLabel;
  private JLabel authorLabel;
  private JTextArea descriptionArea;

  public PluginInfoPanel() {
    setLayout(new BorderLayout(10,0));

    JPanel leftPanel=new JPanel(new BorderLayout());
    JPanel rightPanel=new JPanel(new BorderLayout());

    leftPanel.add(new JLabel("Name:"),BorderLayout.NORTH);
    rightPanel.add(nameLabel=new JLabel("-"),BorderLayout.NORTH);

    JPanel panel1=new JPanel(new BorderLayout());
    JPanel panel2=new JPanel(new BorderLayout());

    panel1.add(new JLabel("Version:"),BorderLayout.NORTH);
    panel2.add(versionLabel=new JLabel("-"),BorderLayout.NORTH);

    JPanel panel3=new JPanel(new BorderLayout());
    JPanel panel4=new JPanel(new BorderLayout());

    panel3.add(new JLabel("Author:"),BorderLayout.NORTH);
    panel4.add(authorLabel=new JLabel("-"),BorderLayout.NORTH);

    panel1.add(panel3,BorderLayout.CENTER);
    panel2.add(panel4,BorderLayout.CENTER);

    JPanel panel5=new JPanel(new BorderLayout());
    panel5.add(new JLabel("Description:"),BorderLayout.NORTH);

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


}