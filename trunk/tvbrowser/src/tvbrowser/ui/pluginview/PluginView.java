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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.pluginview.contextmenu.ContextMenu;
import tvbrowser.ui.pluginview.contextmenu.CustomNodeContextMenu;
import tvbrowser.ui.pluginview.contextmenu.PluginContextMenu;
import tvbrowser.ui.pluginview.contextmenu.ProgramContextMenu;
import tvbrowser.ui.pluginview.contextmenu.RootNodeContextMenu;
import tvbrowser.ui.pluginview.contextmenu.StructureNodeContextMenu;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import devplugin.Program;
import devplugin.ProgramItem;


public class PluginView extends JPanel implements MouseListener {
    
  private PluginTree mTree;
  private PluginTreeModel mModel;


  public PluginView() {
    super(new BorderLayout());

    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    mModel = PluginTreeModel.getInstance();

   for (int i=plugins.length - 1; i>=0; i--) {
      if (plugins[i].canUseProgramTree()) {
        mModel.addPluginTree(plugins[i]);
      }
    }


    mModel.addCustomNode(FavoritesPlugin.getInstance().getRootNode());
    mModel.addCustomNode(ReminderPlugin.getInstance().getRootNode());


    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
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
    
    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    
    for (int i=plugins.length - 1; i>=0; i--) {
      if (plugins[i].canUseProgramTree()) {
        mModel.addPluginTree(plugins[i]);
      }
    }
    
    mModel.reload();
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
      return;
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
        Program program = ((ProgramItem)curNode.getUserObject()).getProgram();
        selectedPrograms[i] = program;
      }

      return new ProgramContextMenu(mTree, selectedPath, PluginTreeModel.getPlugin(selectedPath[0]), selectedPrograms);
    }
    else if (node.getType() == Node.PLUGIN_ROOT) {
      return new PluginContextMenu(mTree, selectedPath[0], PluginTreeModel.getPlugin(selectedPath[0]), node.getActionMenus());
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


}