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

package util.ui.toolbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


public class DefaultToolBarModel implements ToolBarModel {
    
  private ArrayList mListeners;  
  private HashSet mAvailableItems;
  private ArrayList mVisibleItems;
    
  public DefaultToolBarModel() {
    mListeners = new ArrayList();
    mAvailableItems = new HashSet();
    mVisibleItems=new ArrayList();
  }

  public void addToolBarModelListener(ToolBarModelListener listener) {
    mListeners.add(listener);
  }
  

  public void removeToolBarModelListener(ToolBarModelListener listener) {
    mListeners.remove(listener);
  }

  public void removeAllListeners() {
    mListeners.clear();
  }
  
  public void addAvailableItems(ToolBarItem[] items) {
    for (int i=0; i<items.length; i++) {
      mAvailableItems.add(items[i]);
    }
    fireAvailableItemsChanged();
  }
  
  public void setAvailableItems(ToolBarItem[] items) {
    mAvailableItems.clear();
    addAvailableItems(items);
  } 
  
  public void addAvailableItem(ToolBarItem item) {
    mAvailableItems.add(item);
    fireAvailableItemsChanged();
  }
  
  public void clearAvailableItems() {
    mAvailableItems.clear();
    fireAvailableItemsChanged();
  }

  public ToolBarItem[] getAvailableItems() {
    ToolBarItem[] result = new ToolBarItem[mAvailableItems.size()];
    mAvailableItems.toArray(result);
    return result;
  }

  public void setVisibleItems(ToolBarItem[] items) {
    mVisibleItems.clear();
    for (int i=0; i<items.length; i++) {
      mVisibleItems.add(items[i]);
    }
    fireVisibleItemsChanged();
  }
  
  private ToolBarItem findItemById(String id) {
    Iterator it = mAvailableItems.iterator();
    while (it.hasNext()) {
      ToolBarItem item = (ToolBarItem)it.next();
      if (id.equals(item.getId())) {
        return item;
      }
    }
    return null;
  }
  
  public void addSeparator() {
    ToolBarItem it = new Separator();
    mAvailableItems.add(it);
    mVisibleItems.add(it);
    fireAvailableItemsChanged();
    fireVisibleItemsChanged();
  }
  
  public void setVisibleItemsById(String[] items) {
    mVisibleItems.clear();
    for (int i=0; i<items.length; i++) {
        
      if ("#separator".equals(items[i])) {
        ToolBarItem it = new Separator();
        mAvailableItems.add(it);
        mVisibleItems.add(it);
      }
      else {  
        ToolBarItem item = findItemById(items[i]);
        if (item!=null) {
          mVisibleItems.add(item);
        }
      }
    }
    fireVisibleItemsChanged();
  }
  
  public ToolBarItem[] getVisibleItems() {
    ToolBarItem[] result = new ToolBarItem[mVisibleItems.size()];
    mVisibleItems.toArray(result);
    return result;
  }

  protected void fireAvailableItemsChanged() {
    Iterator it = mListeners.iterator();
    while (it.hasNext()) {
      ToolBarModelListener listener = (ToolBarModelListener)it.next();
      listener.availableItemsChanged();
    }
  }

  protected void fireVisibleItemsChanged() {
      Iterator it = mListeners.iterator();
      while (it.hasNext()) {
        ToolBarModelListener listener = (ToolBarModelListener)it.next();
        listener.visibleItemsChanged();
      }
    }


  public void addVisibleItem(ToolBarItem item) {
    mVisibleItems.add(item);
    fireVisibleItemsChanged();
  }

  public void removeVisibleItem(ToolBarItem item) {
    if (mVisibleItems.remove(item)) {
      fireVisibleItemsChanged();
    }
  }

  public boolean containsItem(ToolBarItem item) {
    return mVisibleItems.contains(item); 
  }
    
}