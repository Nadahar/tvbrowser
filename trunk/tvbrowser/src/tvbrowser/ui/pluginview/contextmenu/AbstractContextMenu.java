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
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.pluginview.Node;
import tvbrowser.ui.pluginview.PluginTree;
import util.ui.menu.MenuUtil;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramItem;
import devplugin.ProgramReceiveTarget;


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
    
    Object o = getObjectForNode(node);
    Plugin currentPlugin = null;
    
    if(o instanceof Plugin)
      currentPlugin = (Plugin)o;
    
    if(o != ReminderPlugin.getInstance().getRootNode().getMutableTreeNode()) {
      JMenuItem item = new JMenuItem(ReminderPlugin.getInstance().toString());
      item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
      item.setIcon(IconLoader.getInstance().getIconFromTheme("apps","appointment",16));
      menu.add(item);
      item.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          Program[] programs = collectProgramsFromNode(node);
          if ((programs != null) &&(programs.length > 0))
            ReminderPlugin.getInstance().addPrograms(programs);
        }
      });      
    }
    
    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    for (int i=0; i<plugins.length; i++) {
      if ((plugins[i].canReceivePrograms() || plugins[i].canReceiveProgramsWithTarget())  && plugins[i].getProgramReceiveTargets() != null && plugins[i].getProgramReceiveTargets().length > 0 ) {
        final PluginProxy plugin = plugins[i];        
        
        if ((currentPlugin == null) || (!currentPlugin.getId().equals(plugin.getId()))) {
          ProgramReceiveTarget[] targets = plugin.getProgramReceiveTargets();
          if(!plugins[i].canReceiveProgramsWithTarget()) {
            JMenuItem item = new JMenuItem(plugins[i].getInfo().getName());
            item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);

            Icon[] icons = plugins[i].getMarkIcons(Plugin.getPluginManager().getExampleProgram());
            
            item.setIcon(icons != null ? icons[0] : null);
            menu.add(item);
            item.addActionListener(new ActionListener(){
              public void actionPerformed(ActionEvent e) {
                Program[] programs = collectProgramsFromNode(node);
                if ((programs != null) &&(programs.length > 0)) {
                  plugin.receivePrograms(programs,ProgramReceiveTarget.createDefaultTargetForProgramReceiveIfId(plugin.getId()));
                }
              }
            });
          }
          else if(targets.length > 1) {
            JMenu subMenu = new JMenu(plugins[i].getInfo().getName());
            subMenu.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
            
            Icon[] icons = plugins[i].getMarkIcons(Plugin.getPluginManager().getExampleProgram());
            
            subMenu.setIcon(icons != null && icons.length > 0 ? icons[0] : null);
            menu.add(subMenu);
            
            for(int j = 0; j < targets.length; j++) {
              JMenuItem item = new JMenuItem(targets[j].toString());
              item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);              
              subMenu.add(item);
              
              final ProgramReceiveTarget target = targets[j];
              
              item.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                  Program[] programs = collectProgramsFromNode(node);
                  if ((programs != null) &&(programs.length > 0)) {
                    plugin.receivePrograms(programs,target);
                  }
                }
              });
              
            }
          }
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
  public Object getObjectForNode(Node node) {
    
    Node parent = node;
    
    while (parent != null && parent.getType() != Node.PLUGIN_ROOT && parent != ReminderPlugin.getInstance().getRootNode().getMutableTreeNode()) {
      parent = (Node) parent.getParent();
    }
    
    if (parent != null){
      Object o = parent.getUserObject();
      
      if(o instanceof Plugin)
        return o;
      else
        return parent;
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
      return new Program[]{ ((ProgramItem) node.getUserObject()).getProgram() };
    }
    
    if (node.getChildCount() == 0) {
      return null;
    }
    
    ArrayList<Program> array = new ArrayList<Program>();
    
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
    
    return array.toArray(new Program[0]);
  }
}