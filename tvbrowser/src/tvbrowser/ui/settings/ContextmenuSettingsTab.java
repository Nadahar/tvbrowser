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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateAdapter;
import util.ui.customizableitems.SortableItemList;
import util.ui.UiUtilities;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ActionMenu;

public class ContextmenuSettingsTab implements devplugin.SettingsTab, ActionListener {


  class ContextMenuCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value,
         int index, boolean isSelected, boolean cellHasFocus) {

         JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
           index, isSelected, cellHasFocus);

         if (value instanceof PluginProxy) {
           PluginProxy plugin = (PluginProxy) value;
           Program exampleProgram = Plugin.getPluginManager().getExampleProgram();

           JPopupMenu menu=new JPopupMenu();
           Font f;
           /* If the Plugin is the Plugin for double and middle
            * click make the text bold and italic.*/
           if (plugin.equals(mDefaultPlugin) && plugin.equals(mMiddleClickPlugin)) {
              f=new Font("Dialog",Font.BOLD + Font.ITALIC,12);
           }
           else if (plugin.equals(mDefaultPlugin)) {
              f=new Font("Dialog",Font.BOLD,12);
           }
           else if (plugin.equals(mMiddleClickPlugin)) {
              f=new Font("Dialog",Font.ITALIC,12);
           }
           else {
             f=new Font("Dialog",Font.PLAIN,12);
           }
           label.setFont(f);

           // Get the context menu item text
           String text = null;
           Icon icon = null;
           //Action[] actionArr = plugin.getContextMenuActions(exampleProgram);
           ActionMenu actionMenu = plugin.getContextMenuActions(exampleProgram);
           if (actionMenu != null) {
             Action action = actionMenu.getAction();
             if (action != null) {
               text = (String) action.getValue(Action.NAME);
               icon = (Icon) action.getValue(Action.SMALL_ICON);
             }
             else {
               text = plugin.getInfo().getName();
               icon = plugin.getMarkIcon();
             }
           }

           label.setText(text);

           label.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
           label.setOpaque(false);
           label.setBackground(menu.getBackground());
           JPanel panel=new JPanel(new BorderLayout());
           panel.add(label,BorderLayout.CENTER);
           panel.add(new JLabel(icon),BorderLayout.WEST);
           if (isSelected) {
             panel.setBackground(Color.gray);
           }
           return panel;
         }

         return label;
       }



  }

  private JButton mDefaultPluginBt, mMiddleClickPluginBt;
  private PluginProxy mDefaultPlugin, mMiddleClickPlugin;
  private SortableItemList mList;

  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ContextmenuSettingsTab.class);

  public ContextmenuSettingsTab() {
    mList=new SortableItemList(mLocalizer.msg("title","context menu"));
    mList.getList().setVisibleRowCount(10);
    mList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

       mList.getList().addMouseListener(new MouseAdapter(){
         public void mouseClicked(MouseEvent e){
           if(SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
             int inx = mList.getList().locationToIndex(e.getPoint());
             if (inx>=0) {
               mList.getList().ensureIndexIsVisible(inx);
               mDefaultPlugin = (PluginProxy) mList.getList().getSelectedValue();
               mList.updateUI();
             }
           }
           if(SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
             int inx = mList.getList().locationToIndex(e.getPoint());
             if (inx>=0) {
               mList.getList().ensureIndexIsVisible(inx);
               mList.getList().setSelectedIndex(inx);
               mMiddleClickPlugin = (PluginProxy) mList.getList().getSelectedValue();
               mList.updateUI();
             }
           }
         }
       });
    mList.setCellRenderer(new ContextMenuCellRenderer());
        mList.getList().setOpaque(false);
    fillListbox();

    PluginProxyManager.getInstance().addPluginStateListener(
      new PluginStateAdapter() {
        public void pluginActivated(Plugin p) {
          fillListbox();
        }

        public void pluginDeactivated(Plugin p) {
          fillListbox();
        }
      });
  }


  public JPanel createSettingsPanel() {
    mDefaultPlugin = PluginProxyManager.getInstance().getDefaultContextMenuPlugin();
    mMiddleClickPlugin = PluginProxyManager.getInstance().getMiddleClickPlugin();

    JPanel contentPanel=new JPanel(new BorderLayout(0,15));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(5,8,5,8));

    JPanel panel1=new JPanel();
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
    panel1.add(mList);


    mDefaultPluginBt=new JButton(mLocalizer.msg("defaultPluginBtn",""));
    mDefaultPluginBt.addActionListener(this);
    JPanel panel2=new JPanel(new BorderLayout());
    panel2.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));
    panel2.add(mDefaultPluginBt,BorderLayout.CENTER);
    panel1.add(panel2);

    mMiddleClickPluginBt=new JButton(mLocalizer.msg("middleClickPluginBtn",""));
    mMiddleClickPluginBt.addActionListener(this);
    JPanel panel3=new JPanel(new BorderLayout());
    panel3.add(mMiddleClickPluginBt,BorderLayout.CENTER);
    panel3.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    panel1.add(panel3);

    contentPanel.add(panel1,BorderLayout.NORTH);

    JTextArea descBox = UiUtilities.createHelpTextArea(mLocalizer.msg("description",""));
    contentPanel.add(descBox,BorderLayout.CENTER);

    return contentPanel;
  }

  private void fillListbox() {
    if (mList==null) {
      return;
    }
    mList.removeAllElements();

    PluginProxy[] pluginList = PluginProxyManager.getInstance().getActivatedPlugins();
    Program exampleProgram = Plugin.getPluginManager().getExampleProgram();
    for (int i = 0; i < pluginList.length; i++) {
      ActionMenu actionMenu = pluginList[i].getContextMenuActions(exampleProgram);
      if (actionMenu != null) {
        mList.addElement(pluginList[i]);
      }
    }
  }


  public void actionPerformed(ActionEvent event) {
    Object o=event.getSource();
    if (o==mDefaultPluginBt) {
      mDefaultPlugin = (PluginProxy) mList.getList().getSelectedValue();
      mList.updateUI();
    }
    if(o==mMiddleClickPluginBt) {
      mMiddleClickPlugin = (PluginProxy) mList.getList().getSelectedValue();
      mList.updateUI();
    }
  }


  public void saveSettings() {
    Object o[] = mList.getItems();

    String pluginIDs[] = new String[o.length];
    for (int i = 0; i < o.length; i++) {
      PluginProxy plugin = (PluginProxy) o[i];
      pluginIDs[i] = plugin.getId();
    }

    Settings.propPluginOrder.setStringArray(pluginIDs);

    PluginProxyManager.getInstance().setPluginOrder(pluginIDs);

    if (!mList.contains(mDefaultPlugin)) {
      mDefaultPlugin = null;
    }
    if (!mList.contains(mMiddleClickPlugin)) {
      mMiddleClickPlugin = null;
    }

    PluginProxyManager.getInstance().setDefaultContextMenuPlugin(mDefaultPlugin);
    if (mDefaultPlugin != null) {
      Settings.propDefaultContextMenuPlugin.setString(mDefaultPlugin.getId());
    } else {
      Settings.propDefaultContextMenuPlugin.setString(null);
    }

    PluginProxyManager.getInstance().setMiddleClickPlugin(mMiddleClickPlugin);
    if (mMiddleClickPlugin != null) {
      Settings.propMiddleClickPlugin.setString(mMiddleClickPlugin.getId());
    } else {
      Settings.propMiddleClickPlugin.setString(null);
    }
  }


  public Icon getIcon() {
    return null;
  }


  public String getTitle() {
    return mLocalizer.msg("title", "context menu");
  }

	/*
	public void settingsChanged(SettingsTab tab, Object obj) {
    Object[] currentPlugins=mList.getItems();
    Plugin[] installedPlugins=(Plugin[])obj;

    // remove all plugins which are not installed any more
    for (int i=0;i<currentPlugins.length;i++) {
      Plugin p=(Plugin)currentPlugins[i];
      boolean isInstalled=false;
      for (int j=0;j<installedPlugins.length&&!isInstalled;j++) {
        if (p.equals(installedPlugins[j])) {
          isInstalled=true;
        }
      }
      if (!isInstalled) {
        mList.removeElement(currentPlugins[i]);
      }
    }

    // add all other plugins
    //Plugin[] pluginList=PluginManager.getInstance().getAvailablePlugins();
    for (int i=0;i<installedPlugins.length;i++) {
      if (installedPlugins[i].getContextMenuItemText()!=null && !mList.contains(installedPlugins[i])) {
        mList.addElement(installedPlugins[i]);
      }
    }


	}
  */

}