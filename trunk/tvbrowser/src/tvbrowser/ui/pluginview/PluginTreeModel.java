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


import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import tvbrowser.core.plugin.PluginProxy;
import devplugin.PluginTreeNode;
import devplugin.Plugin;
import devplugin.PluginAccess;


public class PluginTreeModel extends DefaultTreeModel {

  private static PluginTreeModel sInstance;

  

  private PluginTreeModel() {
    super(new DefaultMutableTreeNode("Plugins"));        
  }
  
  
  public void addPluginTree(PluginProxy plugin) {
    PluginTreeNode pluginRoot = plugin.getRootNode();
    MutableTreeNode root = (MutableTreeNode)this.getRoot();

    root.insert(pluginRoot.getMutableTreeNode(), 0);
  }

  public Plugin getPlugin(TreePath path) {
    if (path.getPathCount()>0) {
      Object o = path.getPathComponent(1);
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)o;
      o = node.getUserObject();
      if (o instanceof Plugin) {
        Plugin plugin = (Plugin)o;
        return plugin;
      }

    }
    return null;
  }

  public static PluginTreeModel getInstance() {
    if (sInstance == null) {
      sInstance = new PluginTreeModel();
    }
    return sInstance;
  }
    
    
}

