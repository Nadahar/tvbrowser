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


        /*
  public void mouseClicked(MouseEvent e) {
    ProgramItem programItem = null;

    if (SwingUtilities.isRightMouseButton(e)) {

      TreePath p = mTree.getPathForLocation(e.getX(), e.getY());
      if (p!=null) {
        if (!mTree.getSelectionModel().isPathSelected(p)) {
          mTree.setSelectionPath(p);
          showContextMenu(p, e.getX(), e.getY());
        }
        else {
          showContextMenu(p, e.getX(), e.getY());
        }

      }
    }
    else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
      TreePath[] paths = mTree.getSelectionPaths();
      Node node = (Node)paths[0].getLastPathComponent();
      Object o = node.getUserObject();
      if (node.getType() == Node.PROGRAM) {
        programItem = (ProgramItem)o;
        MainFrame.getInstance().scrollToProgram(programItem.getProgram());
      }
      else if (node.getType() == Node.PLUGIN_ROOT) {
      //  Plugin plugin = (Plugin)o;
      //  plugin.getButtonAction().actionPerformed(null);
      }
      else {
        return;
      }

    }
    
  }


  private void showContextMenu(TreePath path, int x, int y) {

    Node node = (Node)path.getLastPathComponent();
    if (node.getType() == Node.PROGRAM) {
   //   Program[] programs = new Program[paths.length];
   //   for (int i=0; i<programs.length; i++) {
        DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        Program program = ((ProgramItem)curNode.getUserObject()).getProgram();
    //  }
      showContextMenu(path, mModel.getPlugin(path), new Program[]{program}, x, y);
    }
    else if (node.getType() == Node.PLUGIN_ROOT) {
      showPluginContextMenu(path, mModel.getPlugin(path), x, y);
    }
    else if (node.getType() == Node.CUSTOM_NODE) {
      ActionMenu[] menus = node.getActionMenus();
      showContextMenu(path, menus, x, y);

    }




  }


  private JMenuItem createCollapseExpandMenuItem(final TreePath treePath) {
    JMenuItem collapseExpandMI = new JMenuItem();

    collapseExpandMI.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);
    if (mTree.isExpanded(treePath)) {
      collapseExpandMI.setText("Collapse");
      collapseExpandMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          mTree.collapsePath(treePath);
        }
      });
    }
    else {
      collapseExpandMI.setText("Expand");
      collapseExpandMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          mTree.expandPath(treePath);
        }
      });
    }
    return collapseExpandMI;
  }

  private void showContextMenu(TreePath path, ActionMenu[] actionMenus, int x, int y) {
    JPopupMenu menu = new JPopupMenu();

    menu.add(createCollapseExpandMenuItem(path));

    if (actionMenus.length > 0) {
      for (int i=0; i<actionMenus.length; i++) {
        JMenuItem menuItem = MenuUtil.createMenuItem(actionMenus[i]);
        menu.add(menuItem);
      }
    }
    menu.show(mTree, x, y);
  }

  private void showPluginContextMenu(TreePath treePath, Plugin plugin, int x, int y) {
    JPopupMenu menu = new JPopupMenu();

    menu.add(createCollapseExpandMenuItem(treePath));



    ActionMenu actionMenu = plugin.getButtonAction();
    if (actionMenu != null) {
      menu.addSeparator();
      JMenuItem actionMI = MenuUtil.createMenuItem(actionMenu);
      menu.add(actionMI);
    }




    menu.show(mTree, x-10, y-10);
  }

  private void showContextMenu(TreePath treePath, Plugin rootNodePlugin, final Program[] programs, int x, int y) {
    JPopupMenu menu = new JPopupMenu();

    menu.add(createCollapseExpandMenuItem(treePath));

    menu.addSeparator();

    JMenuItem showInTableMI = new JMenuItem("Show");
    showInTableMI.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);
    showInTableMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().scrollToProgram(programs[0]);
      }
    });
    showInTableMI.setEnabled(programs.length == 1);

    menu.add(showInTableMI);

    JMenu copyMenu = new JMenu("Export...");
    copyMenu.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
    menu.add(copyMenu);

    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    for (int i=0; i<plugins.length; i++) {
      if (plugins[i].canReceivePrograms()) {
        final PluginProxy plugin = plugins[i];
        if (!rootNodePlugin.getId().equals(plugin.getId())) {
          JMenuItem item = new JMenuItem(plugins[i].getInfo().getName());
          item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
          copyMenu.add(item);
          item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
              plugin.receivePrograms(programs);
            }
          });
        }
      }
    }

    menu.addSeparator();

    JMenuItem[] pluginMenuItems = PluginProxyManager.createPluginContextMenuItems(programs[0], false);
    for (int i=0; i<pluginMenuItems.length; i++) {
      menu.add(pluginMenuItems[i]);
      pluginMenuItems[i].setEnabled(programs.length == 1);
    }


    menu.show(mTree, x-10, y-10);

  }
                  */
  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {

  }

  public void mousePressed(MouseEvent e) {

  }

  public void mouseReleased(MouseEvent e) {

  }




}