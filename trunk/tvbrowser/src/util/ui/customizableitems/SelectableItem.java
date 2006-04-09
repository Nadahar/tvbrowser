/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package util.ui.customizableitems;

/**
 * A class for a selectable Item.
 * 
 * @author René Mach
 */
public class SelectableItem {
  private Object mItem;
  private boolean mSelected;

  /**
   * 
   * @param item The item.
   * @param selected Item is selected.
   */
  public SelectableItem(Object item, boolean selected) {
    mItem = item;
    mSelected = selected;
  }
  
  /**
   * Sets the selection state of the item.
   * 
   * @param selected If the item is selected.
   */
  public void setSelected(boolean selected) {
    mSelected = selected;
  }

  /**
   * @return If the item is selected.
   */
  public boolean isSelected() {
    return mSelected;
  }
  
  /**
   * @return The item.
   */
  public Object getItem() {
    return mItem;
  }
}
