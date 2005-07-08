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

package tvbrowser.ui.pluginview.contextmenu;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.pluginview.Node;
import tvbrowser.ui.pluginview.PluginTree;
import util.ui.menu.MenuUtil;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramItem;


/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 03.01.2005
 * Time: 22:12:32
 */
public abstract class AbstractContextMenu implements ContextMenu {

  /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(AbstractContextMenu.class);

  private PluginTree mTree;

  protected AbstractContextMenu(PluginTree tree) {
    mTree = tree;
  }


  protected JMenuItem getExpandAllMenuItem(final TreePath treePath) {

    Action action = new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        mTree.expandAll(treePath);
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("expandAll","Expand All"));

    JMenuItem item = new JMenuItem(action);
    item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
    return item;
  }

  protected Action getCollapseExpandAction(final TreePath treePath) {

    final boolean mIsExpanded = mTree.isExpanded(treePath);

    Action action = new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        if (mIsExpanded) {
          mTree.collapsePath(treePath);
        }
        else {
          mTree.expandPath(treePath);
        }
      }
    };
    if (mIsExpanded) {
      action.putValue(Action.NAME, mLocalizer.msg("collapse","collapse"));
    }
    else {
      action.putValue(Action.NAME, mLocalizer.msg("expand","expand"));
    }

    return action;
  }
  
  /**
   * Create a Export-To-Other-Plugins Action
   * @return Export-To-Other-Plugins Action
   */
  protected JMenu getExportMenu(TreePath paths) {
    final Node node = (Node) paths.getLastPathComponent();
    
    JMenu menu = new JMenu(mLocalizer.msg("export","Export"));
    menu.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
    
    if ((node.getChildCount() == 0) && (node.getType() != Node.PROGRAM)) {
      menu.setEnabled(false);
      return menu;
    }
    
    Plugin currentPlugin = getPluginForNode(node);
    
    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    for (int i=0; i<plugins.length; i++) {
      if (plugins[i].canReceivePrograms()) {
        final PluginProxy plugin = plugins[i];
        if ((currentPlugin == null) || (!currentPlugin.getId().equals(plugin.getId()))) {
          JMenuItem item = new JMenuItem(plugins[i].getInfo().getName());
          item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
          item.setIcon(plugins[i].getMarkIcon());
          menu.add(item);
          item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
              Program[] programs = collectProgramsFromNode(node);
              if ((programs != null) &&(programs.length > 0))
                plugin.receivePrograms(programs);
            }
          });
        }
      }
    }

    return menu;
  }
  
  /**
   * Returns the Plugin for this Node.
   * It searches for a Parent-Node containing a Plugin.
   * 
   * @param node Node to use
   * @return Plugin-Parent of this Node
   */
  public Plugin getPluginForNode(Node node) {
    
    Node parent = node;
    
    while ((parent.getType() != Node.PLUGIN_ROOT) || (parent == null)) {
      parent = (Node) parent.getParent();
    }
    
    if (parent != null){
      return (Plugin) parent.getUserObject();
    }
    
    return null;
  }
  
  /**
   * Runs through all Child-Nodes and collects the Program-Elements
   * 
   * @param node Node to search in
   * @return all found Programs within this Node
   */
  public Program[] collectProgramsFromNode(Node node) {
    
    if (node.getType() == Node.PROGRAM) {
      Program[] prg = { ((ProgramItem) node.getUserObject()).getProgram()};
      return prg;
    } 
    
    if (node.getChildCount() == 0) {
      return null;
    }
    
    ArrayList array = new ArrayList();
    
    for (int i=0;i<node.getChildCount();i++) {
      
      Program[] prg = collectProgramsFromNode((Node)node.getChildAt(i));
      if ((prg != null) && (prg.length != 0)) {
       
        for (int v = 0;v<prg.length;v++) {
          if (!array.contains(prg[v])) {
            array.add(prg[v]);
          }
        }
      }
    }
    
    return (Program[])array.toArray(new Program[0]);
  }
}