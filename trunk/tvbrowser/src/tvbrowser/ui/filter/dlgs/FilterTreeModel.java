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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;

import devplugin.Channel;
import devplugin.FilterChangeListenerV2;
import devplugin.PluginAccess;
import devplugin.PluginsProgramFilter;
import devplugin.ProgramFilter;
import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.filters.ParserException;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.core.filters.SingleChannelFilter;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.UiUtilities;

public class FilterTreeModel extends DefaultTreeModel {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FilterTreeModel.class);
  private static FilterTreeModel mInstance;
  
  private static ArrayList<FilterChangeListenerV2> CHANGE_LISTENER_LISTV2;
  
  public FilterTreeModel(TreeNode root) {
    super(root,true);
    
    CHANGE_LISTENER_LISTV2 = new ArrayList<FilterChangeListenerV2>(0);
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
          if(child.contains(filter) && child.isDeletingAllowed()) {
            node.remove(child);
            fireFilterRemoved(filter);
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
    if(filter != null) {
      addFilter(filter, (FilterNode) getRoot(), null);
    }
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
    if(filter != null) {
      if (parent == null) {
        parent = (FilterNode) getRoot();
      }
      FilterNode newNode = parent.addFilter(filter);
      
      fireFilterAdded(filter);
      if(tree != null) {
        reload(tree,parent);
      }
      else {
        reload(root);
      }
      return newNode;
    }
    
    return null;
  }
  
  /**
   * Adds a directory to this tree at the given target node.
   *
   * @param name
   *          The diretory name.
   * @param parent
   *          The parent node to add the directory to or <code>null</code> if the
   *          root node should be used.
   * @return the newly created node for the directory
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
  
  public void updateAvailableChannels(Channel[] channels, String directoryName, String aboveDirectory) {
    ArrayList<Channel> channelsToAddList = new ArrayList<Channel>();
    channelsToAddList.addAll(Arrays.asList(channels));
    
    FilterNode root = ((FilterNode)getRoot());
    
    LinkedList<FilterNode> list = new LinkedList<FilterNode>();
    list.push(root);
    
    do {
      FilterNode node = list.pop();
      
      if(node.isDirectoryNode()) {
        for(int i = 0; i < node.getChildCount(); i++) {
          list.push((FilterNode)node.getChildAt(i));
        }
      }
      else {
        if(node.getUserObject() instanceof SingleChannelFilter) {
          SingleChannelFilter filter = (SingleChannelFilter)node.getUserObject();
          boolean channelFound = false;
          
          for(Channel ch : channels) {
            if(filter.containsChannel(ch)) {
              channelFound = true;
              channelsToAddList.remove(ch);
              break;
            }
          }
          
          if(!channelFound) {
            ((FilterNode)node.getParent()).remove(node);
            fireFilterRemoved(filter);
          }
        }
        else if(node.getUserObject() instanceof UserFilter) {
          ((UserFilter)node.getUserObject()).updateSingleChannelFilters(channels);
        }
      }
    }while(!list.isEmpty());
        
    FilterNode channelDirectory = getDirectoryNode(directoryName, (FilterNode)getRoot());
    
    if(channelDirectory == null) {
      FilterNode aboveNode = getDirectoryNode(aboveDirectory, (FilterNode)getRoot());
      
      int index = aboveNode == null ? 0 : aboveNode.getParent().getIndex(aboveNode);
      
      channelDirectory = new FilterNode(directoryName);
      
      ((FilterNode)getRoot()).insert(channelDirectory, index+1);
    }
    
    for(Channel ch : channelsToAddList) {
      channelDirectory.addFilter(new SingleChannelFilter(ch));
    }
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
  
  public FilterNode getDirectoryNode(String name, FilterNode parent) {
    if (parent == null) {
      parent = (FilterNode)getRoot();
    }
    
    LinkedList<FilterNode> toSearch = new LinkedList<FilterNode>();
    toSearch.push(parent);
    
    do {
      FilterNode node = toSearch.pop();
      
      if(node.isDirectoryNode()) {
        if(node.toString().equals(name)) {
          return node;
        }
        else {
          for(int i = 0; i < node.getChildCount(); i++) {
            FilterNode child = (FilterNode)node.getChildAt(i);
            
            if(child.isDirectoryNode()) {
              toSearch.push(child);
            }
          }
        }
      }
    }while(!toSearch.isEmpty());
      	
  	return null;
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
  
  void fireFilterAdded(final ProgramFilter filter) {
    for(FilterChangeListenerV2 listener : CHANGE_LISTENER_LISTV2) {
      listener.filterAdded(filter);
    }
    
    if(isDefaultFilter(filter)) {
      fireFilterDefaultChanged(filter);
    }
  }

  void fireFilterRemoved(final ProgramFilter filter) {
    for(FilterChangeListenerV2 listener : CHANGE_LISTENER_LISTV2) {
      listener.filterRemoved(filter);
    }
    
    if(isDefaultFilter(filter)) {
      Settings.propDefaultFilter.resetToDefault();
      fireFilterDefaultChanged(FilterManagerImpl.getInstance().getAllFilter());
    }
  }
  
  void fireFilterTouched(final ProgramFilter filter) {
    for(FilterChangeListenerV2 listener : CHANGE_LISTENER_LISTV2) {
      listener.filterTouched(filter);
    }
  }
  
  void fireFilterDefaultChanged(final ProgramFilter filter) {
    for(FilterChangeListenerV2 listener : CHANGE_LISTENER_LISTV2) {
      listener.filterDefaultChanged(filter);
    }
  }
  
  public void updateFilterComponent(final String oldName, final FilterComponent filterComponent) {
    final UserFilter[] userFilters = FilterList.getInstance().getUserFilterArr();
    
    for(UserFilter filter : userFilters) {
      if(filter.containsRuleComponent(filterComponent.getName()) || filter.containsRuleComponent(oldName)) {
        fireFilterTouched(filter);
        
        if(!oldName.equals(filterComponent.getName())) {
          final String[] parts = filter.getRule().split("\\s+");
          final StringBuilder newRule = new StringBuilder();
          
          for(int i = 0; i < parts.length; i++) {
            if(parts[i].equals(oldName)) {
              parts[i] = filterComponent.getName();
            }
            
            if(newRule.length() > 0) {
              newRule.append(" ");
            }
            
            newRule.append(parts[i]);
          }
          
          try {
            filter.setRule(newRule.toString());
          } catch (ParserException e) {
            // Ignore
          }
        }
        
        filter.store();
      }
    }
  }
  
  public void registerFilterChangeListener(final FilterChangeListenerV2 listener) {
    CHANGE_LISTENER_LISTV2.add(listener);
  }
  
  public void unregisterFilterChangeListener(final FilterChangeListenerV2 listener) {
    CHANGE_LISTENER_LISTV2.remove(listener);
  }
  
  private boolean isDefaultFilter(final ProgramFilter filter) {
    String filterId = Settings.propDefaultFilter.getString();
    String filterName = null;
  
    if (StringUtils.isNotEmpty(filterId) && filter != null) {
      String[] filterValues = filterId.split("###");
      filterId = shortFilterClassName(filterValues[0]);
      filterName = filterValues[1];
      
      return shortFilterClassName(filter.getClass().getName()).equals(filterId) && filter.getName().equals(filterName);
    }
    
    return filter instanceof ShowAllFilter;
  }
  
  private String shortFilterClassName(final String className) {
    int index = className.lastIndexOf('$');
    if (index > 0) {
      return className.substring(0, index);
    }
    return className;
  }
  
  /**
   * Sorts the path from the given node to all leafs alphabetically.
   *
   * @param node The node to sort from.
   * @param comp Comparator for sorting
   * @param title Title of confirmation message dialog
   */
  public void sort(FilterNode node, Comparator<FilterNode> comp, String title) {
    String msg = mLocalizer.msg("reallySort",
        "Do you really want to sort '{0}'?\n\nThe current order will get lost.", node.toString());
    int result = JOptionPane.showConfirmDialog(UiUtilities
        .getLastModalChildOf(MainFrame.getInstance()), msg, title,
        JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
      sortNodeInternal(node, comp);
    }
  }
  
  /**
   * sort filter nodes (dialog handling must be done by caller)
   * @param node
   * @param comp
   */
  private void sortNodeInternal(FilterNode node, Comparator<FilterNode> comp) {
    @SuppressWarnings("unchecked")
    ArrayList<FilterNode> childNodes = Collections.list(node.children());
    Collections.sort(childNodes, comp);

    node.removeAllChildren();

    for(FilterNode child : childNodes) {
      node.add(child);
      if(child.isDirectoryNode()) {
        sortNodeInternal(child, comp);
      }
    }
  }
}
