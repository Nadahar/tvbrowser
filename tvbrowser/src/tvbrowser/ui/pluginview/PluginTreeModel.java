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

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import devplugin.Plugin;
import devplugin.PluginTreeNode;

public class PluginTreeModel extends DefaultTreeModel {

  private static PluginTreeModel sInstance;

  private boolean mDisableUpdate;




  private PluginTreeModel() {
    super(new Node(Node.ROOT, "Plugins"));
    
    TvDataUpdater.getInstance().addTvDataUpdateListener(new TvDataUpdateListener() {
      public void tvDataUpdateFinished() {
        mDisableUpdate = false;
      }

      public void tvDataUpdateStarted() {
        mDisableUpdate = true;
      }
    });
  }

  /**
   * Refresh all plugin nodes. This method sould be called continuously to
   * remove expired programs from the tree.
   */
  public void update() {
    if (!mDisableUpdate) {
      MutableTreeNode root = (MutableTreeNode) this.getRoot();
      Enumeration e = root.children();
      while (e.hasMoreElements() && !mDisableUpdate) {
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
        
        Object o = n.getUserObject();
        if (o instanceof Plugin) {
          Plugin p = (Plugin) o;
          p.getRootNode().update();
        }
        else if(n.equals(FavoritesPlugin.getInstance().getRootNode().getMutableTreeNode())) {
          FavoritesPlugin.getInstance().getRootNode().update();
        } else if(n.equals(ReminderPlugin.getInstance().getRootNode().getMutableTreeNode())) {
          ReminderPlugin.getInstance().getRootNode().update();
        }
      }
    }
  }

  public void setDisableUpdate(boolean disabled) {
    mDisableUpdate = disabled;
  }


  public void addCustomNode(PluginTreeNode n) {

    MutableTreeNode root = (MutableTreeNode) this.getRoot();
    root.insert(n.getMutableTreeNode(), 0);
  }


  public void addPluginTree(PluginProxy plugin) {
    PluginTreeNode pluginRoot;
    if (plugin.hasArtificialPluginTree()) {
      pluginRoot = plugin.getArtificialRootNode();
    }
    else {
      pluginRoot = plugin.getRootNode();
    }
    MutableTreeNode root = (MutableTreeNode) this.getRoot();
    root.insert(pluginRoot.getMutableTreeNode(), 0);

  }




  /**
   * Removes all ChildNodes from this Tree
   */
  public void removeAllChildNodes() {
    MutableTreeNode root = (MutableTreeNode) this.getRoot();
    int size = root.getChildCount();

    while (root.getChildCount() > 0) {
      root.remove(0);
    }
  }

  public static Plugin getPlugin(TreePath path) {
    if (path.getPathCount() > 1) {
      Object o = path.getPathComponent(1);
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
      o = node.getUserObject();
      if (o instanceof Plugin) {
        Plugin plugin = (Plugin) o;
        return plugin;
      }

    }
    return null;
  }

  public void reload(TreeNode node) {
    TreePath treePath = new TreePath(getPathToRoot(node));
    Enumeration e = null;

    if (treePath != null) {
      PluginTree t = PluginTree.getInstance();
      if (t != null) {
        e = PluginTree.getInstance().getExpandedDescendants(treePath);
      }
    }

    super.reload(node);

    if (e != null) {
      while (e.hasMoreElements()) {
        TreePath tree = (TreePath) e.nextElement();

        Object[] o = tree.getPath();

        for (int i = 1; i < o.length; i++) {
          TreeNode[] pathNodes = getPathToRoot((TreeNode) o[i]);

          if (node == null || pathNodes[0].toString().compareTo("Plugins") != 0) {
            TreeNode n1 = (TreeNode) o[i - 1];
            Enumeration e1 = n1.children();

            while (e1.hasMoreElements()) {
              TreeNode n2 = (TreeNode) e1.nextElement();
              if (n2.toString().compareTo(o[i].toString()) == 0) {
                o[i] = n2;
                break;
              }
            }
          }
        }

        tree = new TreePath(o);
        PluginTree.getInstance().expandPath(tree);
      }
    }
  }

  public static PluginTreeModel getInstance() {
    if (sInstance == null) {
      sInstance = new PluginTreeModel();
    }
    return sInstance;
  }

  @Override
  public boolean isLeaf(Object nodeObject) {
    if (nodeObject instanceof Node) {
      Node node = (Node) nodeObject;
      return node.getChildCount() == 0;
    }
    return super.isLeaf(nodeObject);
  }

}
