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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
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
   * Refresh all plugin nodes. This method should be called continuously to
   * remove expired programs from the tree.
   */
  public void update() {
    if (!mDisableUpdate) {
      MutableTreeNode rootNode = (MutableTreeNode) this.getRoot();
      @SuppressWarnings("unchecked")
      Enumeration<DefaultMutableTreeNode> e = rootNode.children();
      while (e.hasMoreElements() && !mDisableUpdate) {
        DefaultMutableTreeNode n = e.nextElement();

        Object o = n.getUserObject();
        if (o instanceof Plugin) {
          Plugin p = (Plugin) o;
          p.getRootNode().update();
        }
        else if(n.equals(FavoritesPlugin.getRootNode().getMutableTreeNode())) {
          FavoritesPlugin.getRootNode().update();
        } else if(n.equals(ReminderPlugin.getRootNode().getMutableTreeNode())) {
          ReminderPlugin.getRootNode().update();
        }
      }
    }
  }

  public void setDisableUpdate(boolean disabled) {
    mDisableUpdate = disabled;
  }


  public void addCustomNode(PluginTreeNode n) {
    insertSorted(n);
  }


  public void addPluginTree(PluginProxy plugin) {
    PluginTreeNode pluginRoot;
    if (plugin.hasArtificialPluginTree()) {
      pluginRoot = plugin.getArtificialRootNode();
    }
    else {
      pluginRoot = plugin.getRootNode();
    }
    addCustomNode(pluginRoot);
  }

  private void insertSorted(PluginTreeNode pluginRoot) {
    MutableTreeNode rootNode = (MutableTreeNode) this.getRoot();
    ArrayList<String> pluginNames = new ArrayList<String>();

    for (int i = 0; i < rootNode.getChildCount(); i++) {
      pluginNames.add(rootNode.getChildAt(i).toString());
    }

    Collections.sort(pluginNames);
    int index = pluginNames.indexOf(pluginRoot.getUserObject().toString());

    if(index == -1) {
      index = Collections.binarySearch(pluginNames, pluginRoot.getUserObject().toString());
    }
    else {
      index = -index-1;
    }

    rootNode.insert(pluginRoot.getMutableTreeNode(), -index-1);

    if(pluginRoot.getMutableTreeNode().getIcon() == null) {
      if(pluginRoot.getUserObject() instanceof Plugin) {
        pluginRoot.getMutableTreeNode().setIcon(PluginProxyManager.getInstance().getActivatedPluginForId(((Plugin)pluginRoot.getUserObject()).getId()).getPluginIcon());
      }
      else if(pluginRoot.getUserObject() instanceof PluginProxy) {
        pluginRoot.getMutableTreeNode().setIcon(((PluginProxy)pluginRoot.getUserObject()).getPluginIcon());
      }
    }
  }

  /**
   * Removes all ChildNodes from this Tree
   */
  public void removeAllChildNodes() {
    MutableTreeNode rootNode = (MutableTreeNode) this.getRoot();
    while (rootNode.getChildCount() > 0) {
      rootNode.remove(0);
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

  public static PluginProxy getPluginProxy(TreePath path) {
    if (path.getPathCount() > 1) {
      Object o = path.getPathComponent(1);
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
      o = node.getUserObject();
      if (o instanceof PluginProxy) {
        PluginProxy proxy = (PluginProxy) o;
        return proxy;
      }
    }
    return null;
  }

  public void reload(TreeNode node) {
    TreePath selection = PluginTree.getInstance() != null ? PluginTree.getInstance().getSelectionPath() : null;

    TreePath treePath = new TreePath(getPathToRoot(node));
    Enumeration<TreePath> e = null;

    if (treePath != null) {
      PluginTree t = PluginTree.getInstance();
      if (t != null) {
        e = PluginTree.getInstance().getExpandedDescendants(treePath);
      }
    }

    super.reload(node);

    if (e != null) {
      while (e.hasMoreElements()) {
        TreePath tree = e.nextElement();

        Object[] o = tree.getPath();

        for (int i = 1; i < o.length; i++) {
          TreeNode[] pathNodes = getPathToRoot((TreeNode) o[i]);

          if (node == null || pathNodes[0].toString().compareTo("Plugins") != 0) {
            TreeNode n1 = (TreeNode) o[i - 1];
            @SuppressWarnings("unchecked")
            Enumeration<DefaultMutableTreeNode> e1 = n1.children();

            while (e1.hasMoreElements()) {
              TreeNode n2 = e1.nextElement();
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

    if(selection != null) {
      PluginTree.getInstance().setSelectionPath(selection);
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
