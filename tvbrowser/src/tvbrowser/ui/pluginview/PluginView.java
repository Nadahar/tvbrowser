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
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.programtable.ProgramTable;
import tvbrowser.ui.mainframe.MainFrame;
import devplugin.*;





public class PluginView extends JPanel implements MouseListener {
    
  private JTree mTree;
  private PluginTreeModel mModel;

  private static Font CONTEXT_MENU_PLAINFONT = new Font("Dialog", Font.PLAIN, 12);
  private static Font CONTEXT_MENU_BOLDFONT = new Font("Dialog", Font.BOLD, 12);

  public PluginView() {
    super(new BorderLayout());

    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    mModel = PluginTreeModel.getInstance();
    for (int i=0; i<plugins.length; i++) {
      if (plugins[i].canUseProgramTree()) {
        mModel.addPluginTree(plugins[i]);
      }
    }
    
    mTree = new JTree(mModel);
    mTree.setSelectionModel(new PluginTreeSelectionModel());
    mTree.addMouseListener(this);
    add(new JScrollPane(mTree), BorderLayout.CENTER);
  }


  public void mouseClicked(MouseEvent e) {
    ProgramItem programItem = null;

    if (SwingUtilities.isRightMouseButton(e)) {
      TreePath[] paths = mTree.getSelectionPaths();
      if (paths!=null && paths.length>0) {
        showContextMenu(mTree.getSelectionPaths(), e.getX(), e.getY());
      }
    }
    else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) mTree.getLastSelectedPathComponent();
      Object o = node.getUserObject();
      if (o instanceof ProgramItem) {
        programItem = (ProgramItem)o;
      }
      else {
        return;
      }
      MainFrame.getInstance().scrollToProgram(programItem.getProgram());
    }
    
  }


  private void showContextMenu(TreePath[] paths, int x, int y) {
 /*   if (paths.length==1) {
      TreePath path = paths[0];
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
      Object o = node.getUserObject();
      if (o instanceof ProgramItem) {
        showContextMenu(mModel.getPlugin(path),(ProgramItem)o, x, y);
      }
    }
    else {   */
      Program[] programs = new Program[paths.length];
      for (int i=0; i<programs.length; i++) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
        programs[i] = ((ProgramItem)node.getUserObject()).getProgram();
      }
      showContextMenu(mModel.getPlugin(paths[0]), programs, x, y);
   // }
  }

  private void showContextMenu(Plugin rootNodePlugin, final Program[] programs, int x, int y) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem showInTableMI = new JMenuItem("Show");
    showInTableMI.setFont(CONTEXT_MENU_BOLDFONT);
    showInTableMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().scrollToProgram(programs[0]);
      }
    });
    showInTableMI.setEnabled(programs.length == 1);

    menu.add(showInTableMI);

    JMenu copyMenu = new JMenu("Export...");
    copyMenu.setFont(CONTEXT_MENU_PLAINFONT);
    menu.add(copyMenu);

    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    for (int i=0; i<plugins.length; i++) {
      if (plugins[i].canReceivePrograms()) {
        final PluginProxy plugin = plugins[i];
        if (!rootNodePlugin.getId().equals(plugin.getId())) {
          JMenuItem item = new JMenuItem(plugins[i].getInfo().getName());
          item.setFont(CONTEXT_MENU_PLAINFONT);
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

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {

  }

  public void mousePressed(MouseEvent e) {

  }

  public void mouseReleased(MouseEvent e) {

  }

}