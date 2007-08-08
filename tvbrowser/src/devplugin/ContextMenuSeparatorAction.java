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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package devplugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * A class that brings support for adding separators
 * to the context menu of a plugin.
 * 
 * Simply add the instance of this class to your context menu,
 * it will be replaced with a separator so the user will
 * see a separator in the context menu.
 * 
 * @author René Mach
 */
public final class ContextMenuSeparatorAction extends AbstractAction {
  private static ContextMenuSeparatorAction mInstance;
  private static final String SEPARATOR_NAME = "###PLUGIN###SEPARATOR###";

  private ContextMenuSeparatorAction() {
    mInstance = this;
    putValue(Action.NAME,SEPARATOR_NAME);
  }
  
  public static ContextMenuSeparatorAction getInstance() {
    if(mInstance == null) {
      new ContextMenuSeparatorAction();
    }
    
    return mInstance;
  }
  
  public void actionPerformed(ActionEvent e) {
    //Do nothing, it's only a action for separator support of plugin menus.
  }
  
  public boolean equals(Object o) {
    if(o instanceof Action) {
      String name = ((Action)o).getValue(Action.NAME).toString();
      
      if(name != null) {
        return name.equals(SEPARATOR_NAME);
      }
    }
    
    return false;
  }
}
