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

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.InfoBitFilter;
import tvbrowser.core.filters.ParserException;
import tvbrowser.core.filters.PluginFilter;
import tvbrowser.core.filters.SeparatorFilter;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.core.filters.SingleChannelFilter;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.core.FavoriteFilter;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.ScrollableMenu;

import devplugin.Channel;
import devplugin.PluginsProgramFilter;
import devplugin.ProgramFilter;

/**
 * The node for the filter tree.
 * <p>
 * @author René Mach
 * @since 3.0.2
 */
public class FilterNode extends DefaultMutableTreeNode {
  private static final String FAVORITE_FILTER_KEY = "FAVORITE_FILTER###";
  private static final String PLUGIN_FILTER_KEY = "PLUGIN_FILTER###";
  private static final String INFOBIT_FILTER_KEY = "INFOBIT_FILTER###";
  private static final String SINGLE_CHANNEL_FILTER_KEY = "SINGLE_CHANNEL_FILTER###";
  private boolean mWasExpanded;
  
  /**
   * Creates an instance of this class with
   * the given Object as userObject of this node.
   * 
   * @param userObject The user object for this node.
   */
  public FilterNode(Object userObject) {
    if(userObject instanceof ProgramFilter) {
      setAllowsChildren(false);
    }
    
    this.userObject = userObject;
  }
  
  /**
   * Reads the node from an ObjectInputStream.
   * 
   * @param in The ObjectInputStream to read from.
   * @param version The version of the data file.
   * @throws IOException Thrown if something went wrong.
   * @throws ClassNotFoundException Thrown if something went wrong.
   */
  protected FilterNode(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    try {
    if(in.readBoolean()) {
      int n = in.readInt();
      setUserObject(in.readUTF());
      
      mWasExpanded = in.readBoolean();
      
      boolean lastWasSeparator = false;
      
      for(int i = 0; i < n; i++) {
        FilterNode node = new FilterNode(in, version);
        
        if(node.getUserObject() != null && (!lastWasSeparator || !(node.getUserObject() instanceof SeparatorFilter))) {
          add(node);
          lastWasSeparator = node.getUserObject() instanceof SeparatorFilter;
        }
      }
    }
    else {
      setAllowsChildren(false);
      
      String name = in.readUTF();
      
      if(name.equals(ShowAllFilter.KEY)) {
        userObject = new ShowAllFilter();
      }
      else if(name.equals(PluginFilter.KEY)) {
        userObject = new PluginFilter();
      }
      else if(name.equals(InfoBitFilter.SUBTITLE_FILTER_KEY) || name.equals(InfoBitFilter.AUDIO_DESCRIPTION_FILTER_KEY)) {
         userObject = new InfoBitFilter(name);
      }
      else if(name.equals(SeparatorFilter.KEY)) {
        userObject = new SeparatorFilter();
      }
      else if(name.startsWith(INFOBIT_FILTER_KEY)) {
        String infobitKey = name.substring(INFOBIT_FILTER_KEY.length());
        userObject = new InfoBitFilter(infobitKey);
      }
      else if(name.startsWith(PLUGIN_FILTER_KEY)) {
        String pluginKey = name.substring(PLUGIN_FILTER_KEY.length());
        userObject = pluginKey;
        setAllowsChildren(false);
      }
      else if(name.startsWith(FAVORITE_FILTER_KEY)) {
        String filterKey = name.substring(FAVORITE_FILTER_KEY.length());
        
        userObject = FavoritesPlugin.getInstance().getFilterForKeyValue(filterKey);
      }
      else if(name.startsWith(SINGLE_CHANNEL_FILTER_KEY)) {
        SingleChannelFilter filter = new SingleChannelFilter(Channel.readData(in, true));
        
        if(filter.isValidChannel()) {
          userObject = filter;
        }
      }
      else {
        File userFilterFile = new File(FilterList.getFilterDirectory(),name + ".filter");
        
        if(userFilterFile.isFile()) {
          try {
            userObject = new UserFilter(userFilterFile);
          } catch (ParserException e) {
            // Ignore
          }
        }
      }
    }
    }catch(Throwable t) {t.printStackTrace();}
  }
  
  public boolean isValidFilter() {
    if(userObject instanceof PluginsProgramFilter && ((PluginsProgramFilter)(userObject)).getPluginAccessOfFilter() == null || userObject == null) {
      return false;
    }
    
    return true;
  }
  
  public void store(ObjectOutputStream out) throws IOException {    
    out.writeBoolean(isDirectoryNode());
    
    if(isDirectoryNode()) {
      ArrayList<FilterNode> filterNodeList = new ArrayList<FilterNode>();
      
      for(int i = 0; i < getChildCount(); i++) {
        FilterNode test = (FilterNode)getChildAt(i);
        
        if(test.isValidFilter()) {
          filterNodeList.add(test);
        }
      }
      
      out.writeInt(filterNodeList.size());
      out.writeUTF(userObject.toString());
      out.writeBoolean(mWasExpanded);
      
      for(FilterNode node : filterNodeList) {
        node.store(out);
      }
    }
    else {
      if(userObject instanceof ShowAllFilter) {
        out.writeUTF(ShowAllFilter.KEY);
      }
      else if(userObject instanceof PluginFilter) {
        out.writeUTF(PluginFilter.KEY);
      }
      else if(userObject instanceof SeparatorFilter) {
        out.writeUTF(SeparatorFilter.KEY);
      }
      else if(userObject instanceof UserFilter){
        UserFilter filter = (UserFilter)userObject;
        
        out.writeUTF(filter.getName());
        filter.store();
      }
      else if(userObject instanceof PluginsProgramFilter) {
        out.writeUTF(PLUGIN_FILTER_KEY + ((PluginsProgramFilter)userObject).getName());
      }
      else if(userObject instanceof InfoBitFilter) {
        out.writeUTF(INFOBIT_FILTER_KEY + ((InfoBitFilter)userObject).getKey());
      }
      else if(userObject instanceof FavoriteFilter) {
        out.writeUTF(FAVORITE_FILTER_KEY + ((FavoriteFilter)userObject).getKeyValue());
      }
      else if(userObject instanceof SingleChannelFilter) {
        ((SingleChannelFilter)userObject).store(SINGLE_CHANNEL_FILTER_KEY, out);
      }
    }
  }
  
  public String toString() {
    if(userObject instanceof String) {
      return userObject.toString();
    } else if(userObject != null) {
      return ((ProgramFilter)userObject).getName();
    } else {
      return "NULL";
    }
  }

  /**
   * Gets if this node contains a separator.
   * 
   * @return <code>True</code> if this node contains a separator, <code>false</code> otherwise.
   */
  public boolean containsSeparator() {
    return userObject instanceof SeparatorFilter;
  }
  
  /**
   * Gets if this node contains a filter.
   * 
   * @return <code>True</code> if this node contains a filter, <code>false</code> otherwise.
   */
  public boolean containsFilter() {
    return userObject instanceof ProgramFilter && !containsSeparator();
  }
  
  /**
   * Gets if this node is a directory node.
   * 
   * @return <code>True</code> if this node is a directory node, <code>false</code> otherwise.
   */
  public boolean isDirectoryNode() {
    return userObject instanceof String;
  }
  
  /**
   * Gets the filter contained by this node if there is one.
   * 
   * @return The filter contained by this node or <code>null</code> if
   * there is no filter.
   */
  public ProgramFilter getFilter() {
    if(containsFilter() || containsSeparator()) {
      return (ProgramFilter)userObject;
    }
    else {
      return null;
    }
  }
  
  protected boolean wasExpanded() {
    return mWasExpanded || isRoot();
  }
  
  protected void setWasExpanded(boolean expanded) {
    mWasExpanded = expanded;
  }
  
  /**
   * Adds a filter to this node if this is a directory node.
   * <p>
   * @param filter The filter to add.
   * @return The added filter node or <code>null</code> if no node was added.
   */
  public FilterNode addFilter(ProgramFilter filter) {
    if(allowsChildren) {
      final FilterNode childNode = new FilterNode(filter);
      super.add(childNode);
      return childNode;
    }
    
    return null;
  }
  
  public FilterNode addDirectory(String value) {
    if(allowsChildren) {
      final FilterNode childNode = new FilterNode(value);
      super.add(childNode);
      return childNode;
    }
    
    return null;
  }
  
  public boolean contains(Object o) {
    if(o instanceof SeparatorFilter) {
      return containsSeparator() && userObject == o;
    }
    else if(o instanceof ProgramFilter) {
      return containsFilter() && userObject == o;
    }
    
    return false;
  }
  
  public boolean equals(Object o) {
    if(o instanceof SeparatorFilter) {
      return containsSeparator() && userObject == o;
    } else if(o instanceof ProgramFilter) {
      return containsFilter() && userObject == o;
    } else {
      return this == o;
    }
  }
  
  /**
   * Gets all filters contained in this node and all children of it.
   * <p>
   * @return All filters contained in this node and all children of it.
   */
  public ProgramFilter[] getAllFilters() {
    try{
    if(isDirectoryNode()) {
      ProgramFilter[] filters = new ProgramFilter[0];
      
      for(int i = 0; i < getChildCount(); i++) {
        ProgramFilter[] filter = ((FilterNode)getChildAt(i)).getAllFilters();
        
        if(filter != null && filter.length > 0 ) {
          ProgramFilter[] newArr = new ProgramFilter[filters.length + filter.length];
          
          System.arraycopy(filters,0,newArr,0,filters.length);
          System.arraycopy(filter,0,newArr,filters.length,filter.length);
          
          filters = newArr;
        }
      }
      
      return filters;
    }
    else {
      return new ProgramFilter[] {getFilter()};
    }
    }catch(Exception e) {e.printStackTrace();}
    return null;
  }
  
  public boolean isDeletingAllowed() {
    return !(userObject instanceof ShowAllFilter || userObject instanceof PluginFilter ||
        userObject instanceof PluginsProgramFilter || userObject instanceof InfoBitFilter || 
        userObject instanceof SingleChannelFilter ||
        getChildCount() > 0);
  }
  
  public boolean isUserDeletingAllowed() {
    return isDeletingAllowed() && !(userObject instanceof FavoriteFilter);
  }
  
  public boolean testAndSetToPluginsProgramFilter(PluginsProgramFilter filter) {
    boolean returnValue = false;
    
    if(super.allowsChildren) {
      for(int i = 0; i < getChildCount(); i++) {
        returnValue = returnValue || ((FilterNode)getChildAt(i)).testAndSetToPluginsProgramFilter(filter);
      }
    }
    if(userObject instanceof String && toString().equals(filter.getName())) {
      userObject = filter;
      
      return true;
    }
    
    return returnValue;
  }
  
  public void createMenu(JMenu menu, ProgramFilter curFilter) {
    ButtonGroup group = new ButtonGroup();
    createMenuInternal(this,menu,group,curFilter);
  }
  
  private void createMenuInternal(FilterNode node, JMenu parent, ButtonGroup group, ProgramFilter curFilter) {
    if(allowsChildren) {
      for(int i = 0; i < getChildCount(); i++) {
        final FilterNode test = (FilterNode)getChildAt(i);
        
        if(test.isDirectoryNode() && !test.isLeaf()) {
          JMenu dir = new ScrollableMenu(test.getUserObject().toString());
          parent.add(dir);
          test.createMenuInternal(test,dir,group,curFilter);
        }
        else {
          if(test.containsFilter()) {
            final JRadioButtonMenuItem item = new JRadioButtonMenuItem(test.toString());
            group.add(item);
            item.addActionListener(e -> {
              MainFrame.getInstance().setProgramFilter(test.getFilter());
              item.setSelected(true);
            });
            parent.add(item);
            
            if (curFilter != null && (curFilter.getName().equals(test.getFilter().getName()))) {
              item.setSelected(true);
            }
            else if ((curFilter == null) && (test.getFilter() instanceof ShowAllFilter)) {
              item.setSelected(true);
            }
                  
            String id = test.getFilter().getClass().getName();
            String name = test.getFilter().getName();
            
            if((Settings.propDefaultFilter.getString().equals(id + "###" + name)) ||
                (Settings.propDefaultFilter.getString().trim().length() < 1 && test.getFilter() instanceof ShowAllFilter)) {
              item.setFont(item.getFont().deriveFont(Font.BOLD));
            }
            
            if(test.getFilter() instanceof UserFilter) {
              item.setText(((UserFilter)test.getFilter()).getViewString());
            }
          }
          else if(test.containsSeparator()) {
            parent.addSeparator();
          }
        }
      }
    }
  }
}
