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
  private static ContextMenuSeparatorAction mDisabledOnTaskMenuInstance;
  
  private static final String SEPARATOR_NAME = "###PLUGIN###SEPARATOR###";

  private ContextMenuSeparatorAction() {
    super.putValue(Action.NAME,SEPARATOR_NAME);
  }
  
  /**
   * Gets an instance of this class.
   * The instance is also shown in task menu of the ProgramInfoDialog.
   * 
   * @see {@link #getDisabledOnTaskMenuInstance()} to get an instance that
   * isn't shown in the task menu of the ProgramInfoDialog. 
   * 
   * @return The instance of this class, that is shown in the task
   * menu of the ProgramInfoDialog.
   */
  public static ContextMenuSeparatorAction getInstance() {
    if(mInstance == null) {
      mInstance = new ContextMenuSeparatorAction();
    }
    
    return mInstance;
  }
  
  /**
   * Gets an instance of this class.
   * The instance is <b>not</b> shown in task menu of the ProgramInfoDialog.
   * 
   * @see {@link #getInstance()} to get an instance that
   * is shown in the task menu of the ProgramInfoDialog. 
   * 
   * @return The instance of this class, that is <b>not</b> shown in the task
   * menu of the ProgramInfoDialog.
   */
  public static ContextMenuSeparatorAction getDisabledOnTaskMenuInstance() {
    if(mDisabledOnTaskMenuInstance == null) {
      mDisabledOnTaskMenuInstance = new ContextMenuSeparatorAction();
      mDisabledOnTaskMenuInstance.setDisabledOnTaskMenu();
    }
    
    return mDisabledOnTaskMenuInstance;
  }
  
  private void setDisabledOnTaskMenu() {
    super.putValue(Plugin.DISABLED_ON_TASK_MENU, true);
  }

  /**
   * Overwritten to disable changes of values of this class. So use it or don't
   * use it, it has no effect.
   */
  public void putValue(String key, Object newValue) {}
  
  public void actionPerformed(ActionEvent e) {
    //Do nothing, it's only a action for separator support of plugin menus.
  }
  
  public boolean equals(Object o) {
    if(o instanceof Action) {
      String name = ((Action)o).getValue(Action.NAME).toString();
      return SEPARATOR_NAME.equals(name);
    }
    
    return false;
  }
}
