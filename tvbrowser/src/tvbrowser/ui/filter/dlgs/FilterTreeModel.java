/*
 * TV-Browser
 * Copyright (C) 04-2003 TV-Browser-Team (dev@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.filter.dlgs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import javax.swing.JMenu;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;

import devplugin.PluginAccess;
import devplugin.PluginsProgramFilter;
import devplugin.ProgramFilter;

import tvbrowser.core.plugin.PluginManagerImpl;
import util.ui.Localizer;

public class FilterTreeModel extends DefaultTreeModel {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FilterTreeModel.class);
  private static FilterTreeModel mInstance;
  
  public FilterTreeModel(TreeNode root) {
    super(root,true);
    // TODO Auto-generated constructor stub
  }

  public static FilterTreeModel initInstance(ProgramFilter[] filterArr) {
    FilterNode rootNode = new FilterNode("");
    fixRootNode(rootNode);
    for(ProgramFilter filter : filterArr) {
      rootNode.addFilter(filter);
    }

    mInstance = new FilterTreeModel(rootNode);
    return mInstance;
  }
  
  public static FilterTreeModel initInstance(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();
    FilterNode rootNode = new FilterNode(in, version);
    fixRootNode(rootNode);
    mInstance = new FilterTreeModel(rootNode);
    return mInstance;
  }
  
  /**
   * Saves the data of this tree into the given stream.
   *
   * @param out The stream to write the data to.
   * @throws IOException Thrown if something went wrong
   */
  public void storeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);
    ((FilterNode)getRoot()).store(out);
  }
  
  /**
   * change the label of the root node after it has been read from disk
   * @param rootNode
   */
  private static void fixRootNode(final FilterNode rootNode) {
    String rootLabel = mLocalizer.msg("rootLabel", "All filters");
    if (StringUtils.isEmpty(rootLabel)) {
      rootLabel = "FILTERS_ROOT";
    }
    rootNode.setUserObject(rootLabel);
  }
  
  
  public static FilterTreeModel getInstance() {
    if (mInstance == null) {
      mInstance = initInstance(new ProgramFilter[0]);
    }

    return mInstance;
  }
  
  public boolean isLeaf(Object nodeObject) {
    if (nodeObject instanceof FilterNode) {
      FilterNode node = (FilterNode) nodeObject;
      return node.getChildCount() == 0;
    }
    return super.isLeaf(nodeObject);
  }
  
  public void deleteFilter(ProgramFilter filter) {
    deleteFilter((FilterNode)getRoot(), filter);
  }

  private void deleteFilter(FilterNode node, ProgramFilter filter) {
    if(node.isDirectoryNode()) {
      @SuppressWarnings("unchecked")
      Enumeration<FilterNode> e = node.children();

      while(e.hasMoreElements()) {
        FilterNode child = e.nextElement();

        if(child.isDirectoryNode()) {
          deleteFilter(child, filter);
        } else if(child.containsFilter() || child.containsSeparator()) {
          if(child.equals(filter) && child.isDeletingAllowed()) {
            node.remove(child);
          }
        }
      }
    }
  }
  

  /**
   * Adds a filter to this tree at the root node.
   *
   * @param filter The filter to add.
   */
  public void addFilter(ProgramFilter filter) {
    addFilter(filter, (FilterNode) getRoot(), null);
  }

  /**
   * Adds a filter to this tree at the given target node.
   *
   * @param filter
   *          The filter to add.
   * @param parent
   *          The parent node to add the filter to or <code>null</code> if the
   *          root node should be used.
   * @return the newly created node for the filter
   */
  public FilterNode addFilter(ProgramFilter filter, FilterNode parent, FilterTree tree) {
    if (parent == null) {
      parent = (FilterNode) getRoot();
    }
    FilterNode newNode = parent.addFilter(filter);
    if(tree != null) {
      reload(tree,parent);
    }
    else {
      reload(root);
    }
    return newNode;
  }
  
  /**
   * Adds a filter to this tree at the given target node.
   *
   * @param filter
   *          The filter to add.
   * @param parent
   *          The parent node to add the filter to or <code>null</code> if the
   *          root node should be used.
   * @return the newly created node for the filter
   */
  public FilterNode addDirectory(String name, FilterNode parent) {
    if (parent == null) {
      parent = (FilterNode) getRoot();
    }
    FilterNode newNode = parent.addDirectory(name);
    reload(parent);
    return newNode;
  }
  
  public ProgramFilter[] getAllFilters() {
    return ((FilterNode)getRoot()).getAllFilters();
  }
  
  public void addPluginsProgramFilters() {
    PluginAccess[] plugins = PluginManagerImpl.getInstance().getActivatedPlugins();

    for (PluginAccess plugin : plugins) {
      PluginsProgramFilter[] filters = plugin.getAvailableFilter();

      if (filters != null) {
        for (PluginsProgramFilter filter : filters) {
          if(!((FilterNode)getRoot()).testAndSetToPluginsProgramFilter(filter)) {
            addFilter(filter);
          }
        }
      }
    }
  }
  
  public void createMenu(JMenu menu, ProgramFilter curFilter) {
    ((FilterNode)getRoot()).createMenu(menu,curFilter);
  }
  
  public void reload(FilterTree tree, TreeNode node) {
    super.reload(node);
    @SuppressWarnings("unchecked")
    Enumeration<FilterNode> e = node.children();

    while(e.hasMoreElements()) {
      FilterNode child = e.nextElement();

      if(child.isDirectoryNode()) {
        reload(tree, child);
      }
    }

    FilterNode parent = (FilterNode)node;

    if(parent.wasExpanded()) {
      tree.expandPath(new TreePath((tree.getModel()).getPathToRoot(node)));
    } else {
      tree.collapsePath(new TreePath((tree.getModel()).getPathToRoot(node)));
    }
  }
}
