/*
 * TV-Browser
 * Copyright (C) 2013 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date: 2017-03-04 23:44:53 +0100 (Sa, 04 Mär 2017) $
 *   $Author: ds10 $
 * $Revision: 8599 $
 */
package util.settings;

import java.awt.event.MouseEvent;

import compat.MenuCompat;
import devplugin.ContextMenuIf;
import tvbrowser.core.contextmenu.ContextMenuManager;

/**
 * A setting for storing information about a context menu mouse action.
 * 
 * @author René Mach
 * @since 3.3.1
 */
public class ContextMenuMouseActionSetting {
  public static final String SEPARATOR = "//";
  
  private int mModifiersEx;
  private String mContextMenuId;
  private int mContextMenuActionId = MenuCompat.ID_ACTION_NONE;
  
  /**
   * @param modifiersEx The keyboard modifiers for this action setting.
   * @param contextMenuId The id of the ContextMenuIf to use with the given modifiers.
   */
  public ContextMenuMouseActionSetting(int modifiersEx, String contextMenuId, int contextMenuActionId) {
    mModifiersEx = modifiersEx;
    mContextMenuId = contextMenuId;
    mContextMenuActionId = contextMenuActionId;
  }
  
  /**
   * @param value The coded value for reading modifiers and context menu id.
   * @throws NullPointerException No <code>null</code> value accepted.
   */
  public ContextMenuMouseActionSetting(String value) throws NullPointerException {
    String[] parts = value.split(SEPARATOR);
    
    if(parts[0] != null && parts[0].length() > 0) {
      mModifiersEx = Integer.parseInt(parts[0]);
    }
    else {
      mModifiersEx = MouseEvent.BUTTON1_DOWN_MASK & ~MouseEvent.BUTTON1_DOWN_MASK;
    }
    
    mContextMenuId = parts[1];
    
    if(parts.length > 2) {
      mContextMenuActionId = Integer.parseInt(parts[2]);
    }
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder(String.valueOf(mModifiersEx));
    
    builder.append(SEPARATOR);
    builder.append(mContextMenuId);
    builder.append(SEPARATOR);
    builder.append(String.valueOf(mContextMenuActionId));
    
    return builder.toString();
  }
  
  /**
   * @return The keyboard modifiersEx for this setting.
   */
  public int getModifiersEx() {
    return mModifiersEx;
  }
  
  /**
   * @return The id of the context menu action to use.
   */
  public int getContextMenuActionId() {
    return mContextMenuActionId;
  }
  
  /**
   * @return The ContextMenuIf for this settings or <code>null</code> if there is no ContextMenuIf.
   */
  public ContextMenuIf getContextMenuIf() {
    return ContextMenuManager.getInstance().getContextMenuIfForId(mContextMenuId);
  }
}
