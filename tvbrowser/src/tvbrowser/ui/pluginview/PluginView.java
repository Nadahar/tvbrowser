/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.ui.pluginview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.pluginview.contextmenu.ContextMenu;
import tvbrowser.ui.pluginview.contextmenu.CustomNodeContextMenu;
import tvbrowser.ui.pluginview.contextmenu.PluginBasedPluginContextMenu;
import tvbrowser.ui.pluginview.contextmenu.ProgramContextMenu;
import tvbrowser.ui.pluginview.contextmenu.ProxyBasedPluginContextMenu;
import tvbrowser.ui.pluginview.contextmenu.RootNodeContextMenu;
import tvbrowser.ui.pluginview.contextmenu.StructureNodeContextMenu;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramItem;


public class PluginView extends JPanel implements MouseListener {
    
  private PluginTree mTree;
  private PluginTreeModel mModel;


  public PluginView() {
    super(new BorderLayout());


    mModel = PluginTreeModel.getInstance();

    insertPluginRootNodes();

    PluginTreeCellRenderer renderer = new PluginTreeCellRenderer();
    renderer.setLeafIcon(null);
    mTree = new PluginTree(mModel);
    mTree.setSelectionModel(new PluginTreeSelectionModel());
    mTree.addMouseListener(this);
    mTree.setCellRenderer(renderer);
    add(new JScrollPane(mTree), BorderLayout.CENTER);


  }

  /**
   * Refreshes the Tree
   */
  public void refreshTree() {
    mModel.removeAllChildNodes();
    insertPluginRootNodes();
    mModel.reload();
  }


  private void insertPluginRootNodes() {
    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    ContextMenuIf[] menuIfs = ContextMenuManager.getInstance().getAvailableContextMenuIfs(true,true);

    ArrayList<String> pluginList = new ArrayList<String>();

    for (PluginProxy proxy : plugins) {
      // show real plugin tree always, show artificial tree only if it has children
      if(proxy.canUseProgramTree() && !(proxy.hasArtificialPluginTree() && proxy.getArtificialRootNode().size() == 0)) {
        pluginList.add(proxy.getId());
      }
    }

    for (int i = menuIfs.length - 1; i >= 0; i--) {
      if(menuIfs[i].getId().compareTo(FavoritesPlugin.getInstance().getId()) == 0) {
        mModel.addCustomNode(FavoritesPlugin.getInstance().getRootNode());
      } else if(menuIfs[i].getId().compareTo(ReminderPlugin.getInstance().getId()) == 0) {
        mModel.addCustomNode(ReminderPlugin.getInstance().getRootNode());
      } else if(pluginList.contains(menuIfs[i].getId())) {
        PluginProxy plugin = PluginProxyManager.getInstance().getPluginForId(menuIfs[i].getId());
        mModel.addPluginTree(plugin);
        pluginList.remove(menuIfs[i].getId());
      }
    }

    for(int i = 0; i < pluginList.size(); i++) {
      mModel.addPluginTree(PluginProxyManager.getInstance().getPluginForId(pluginList.get(i)));
    }
  }
  
  public void update() {
    mModel.update();
  }

  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) {
      showPopup(e);
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger()) {
      showPopup(e);
    }
  }    
  

  public void mouseClicked(MouseEvent e) {

    TreePath path = mTree.getPathForLocation(e.getX(), e.getY());
    if (path == null) {
      return;
    }
    boolean isDoubleClick = SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2);

    TreePath[] selectedPaths = mTree.getSelectionPaths();

    ContextMenu menu = createContextMenu(selectedPaths);
    if (menu == null) {
      return;
    }

    if (isDoubleClick) {
      Action defaultAction = menu.getDefaultAction();
      if (defaultAction != null) {
        defaultAction.actionPerformed(new ActionEvent(mTree, 0, ""));
      }
    }

  }

  /**
   * Shows the Popup
   * @param e MouseEvent for X/Y Coordinates
   */
  private void showPopup(MouseEvent e) {
    TreePath path = mTree.getPathForLocation(e.getX(), e.getY());
    if (path == null) {
      path = new TreePath(mTree.getModel().getRoot());
      mTree.setSelectionPath(path);
    }

    // After Popup-Trigger, there is should be only one path selected
    if (!mTree.getSelectionModel().isPathSelected(path)) {
      mTree.setSelectionPath(path);
    }
    
    TreePath[] selectedPaths = mTree.getSelectionPaths();

    ContextMenu menu = createContextMenu(selectedPaths);
    if (menu == null) {
      return;
    }
    
    menu.getPopupMenu().show(mTree, e.getX(), e.getY());
  }
  

  public ContextMenu createContextMenu(TreePath[] selectedPath) {
    if (selectedPath == null || selectedPath.length == 0) {
      return null;
    }

    Node node = (Node)selectedPath[0].getLastPathComponent();
    if (node.getType() == Node.PROGRAM) {
      Program[] selectedPrograms = new Program[selectedPath.length];
      for (int i=0; i<selectedPath.length; i++) {
        DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) selectedPath[i].getLastPathComponent();
        selectedPrograms[i] = ((ProgramItem)curNode.getUserObject()).getProgram();
      }

      return new ProgramContextMenu(mTree, selectedPath, PluginTreeModel.getPlugin(selectedPath[0]), selectedPrograms);
    }
    else if (node.getType() == Node.PLUGIN_ROOT) {
      Plugin plugin = PluginTreeModel.getPlugin(selectedPath[0]);
      if (plugin != null) {
        return new PluginBasedPluginContextMenu(mTree, selectedPath[0], plugin, node.getActionMenus());
      }
      else {
        PluginProxy proxy = PluginTreeModel.getPluginProxy(selectedPath[0]);
        if (proxy != null) {
          return new ProxyBasedPluginContextMenu(mTree, selectedPath[0], proxy, node.getActionMenus());
        }
      }
    }
    else if (node.getType() == Node.CUSTOM_NODE) {
      return new CustomNodeContextMenu(mTree, selectedPath[0], node.getActionMenus());
    }
    else if (node.getType() == Node.ROOT) {
      return new RootNodeContextMenu(mTree, selectedPath[0]);
    }
    else if (node.getType() == Node.STRUCTURE_NODE) {
      return new StructureNodeContextMenu(mTree, selectedPath[0]);
    }
    return null;

  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {

  }

  private class PluginTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel,
        boolean expanded,
        boolean leaf, int row,
        boolean hasFocus) {
      JLabel label = (JLabel)super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
      
      if (leaf && value instanceof Node) {
        Node node = (Node)value; 
        if(node.isDirectoryNode()) {
          label.setIcon(getClosedIcon());
        }
        else if (node.getType() == Node.PROGRAM) {
          Program program = ((ProgramItem)node.getUserObject()).getProgram();
          if (program.isExpired() && !sel) {
            label.setForeground(UIManager.getColor("Label.disabledForeground"));
          }
          else if (!FilterManagerImpl.getInstance().getCurrentFilter().accept(program) && !sel) {
            label.setForeground(Color.red);
          }
        }
      }
      
      return label;
    }
  }
}