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

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.tree.*;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.programtable.ProgramTable;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.pluginview.contextmenu.*;
import devplugin.*;
import util.ui.menu.MenuUtil;





public class PluginView extends JPanel implements MouseListener {
    
  private PluginTree mTree;
  private PluginTreeModel mModel;


  public PluginView() {
    super(new BorderLayout());

    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    mModel = PluginTreeModel.getInstance();
    for (int i=0; i<plugins.length; i++) {
      if (plugins[i].canUseProgramTree()) {
        mModel.addPluginTree(plugins[i]);
      }
    }

    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    renderer.setLeafIcon(null);
    mTree = new PluginTree(mModel);
    mTree.setSelectionModel(new PluginTreeSelectionModel());
    mTree.addMouseListener(this);
    mTree.setCellRenderer(renderer);
    add(new JScrollPane(mTree), BorderLayout.CENTER);


  }


  public void mouseClicked(MouseEvent e) {

    TreePath path = mTree.getPathForLocation(e.getX(), e.getY());
    if (path == null) {
      return;
    }
    boolean isRightClick = SwingUtilities.isRightMouseButton(e);
    boolean isDoubleClick = SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2);

    // After right click, there is should be only one path selected
    if (isRightClick) {
      if (!mTree.getSelectionModel().isPathSelected(path)) {
        mTree.setSelectionPath(path);
      }
    }


    TreePath[] selectedPaths = mTree.getSelectionPaths();

    ContextMenu menu = createContextMenu(selectedPaths);
    if (menu == null) {
      return;
    }

    if (isRightClick) {
      menu.getPopupMenu().show(mTree, e.getX(), e.getY());
    }
    else if (isDoubleClick) {
      Action defaultAction = menu.getDefaultAction();
      if (defaultAction != null) {
        defaultAction.actionPerformed(new ActionEvent(mTree, 0, ""));
      }
    }

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

      return new ProgramContextMenu(mTree, selectedPath, mModel.getPlugin(selectedPath[0]), selectedPrograms);
    }
    else if (node.getType() == Node.PLUGIN_ROOT) {
      return new PluginContextMenu(mTree, selectedPath[0], mModel.getPlugin(selectedPath[0]), node.getActionMenus());
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

  public void mousePressed(MouseEvent e) {

  }

  public void mouseReleased(MouseEvent e) {

  }




}