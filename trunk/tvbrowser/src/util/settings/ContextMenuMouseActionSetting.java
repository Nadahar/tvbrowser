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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.settings;

import tvbrowser.core.contextmenu.ContextMenuManager;
import devplugin.ContextMenuIf;

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
  
  /**
   * @param modifiersEx The keyboard modifiers for this action setting.
   * @param contextMenuId The id of the ContextMenuIf to use with the given modifiers.
   */
  public ContextMenuMouseActionSetting(int modifiersEx, String contextMenuId) {
    mModifiersEx = modifiersEx;
    mContextMenuId = contextMenuId;
  }
  
  /**
   * @param value The coded value for reading modifiers and context menu id.
   * @throws NullPointerException No <code>null</code> value accepted.
   */
  public ContextMenuMouseActionSetting(String value) throws NullPointerException {
    String[] parts = value.split(SEPARATOR);
    
    mModifiersEx = Integer.parseInt(parts[0]);
    mContextMenuId = parts[1];
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder(mModifiersEx);
    
    builder.append(SEPARATOR);
    builder.append(mContextMenuId);
    
    return builder.toString();
  }
  
  /**
   * @return The keyboard modifiersEx for this setting.
   */
  public int getModifiersEx() {
    return mModifiersEx;
  }
  
  /**
   * @return The ContextMenuIf for this settings or <code>null</code> if there is no ContextMenuIf.
   */
  public ContextMenuIf getContextMenuIf() {
    return ContextMenuManager.getInstance().getContextMenuIfForId(mContextMenuId);
  }
}
