/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *
 */
package tvbrowser.core.contextmenu;

import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuIf;
import devplugin.Program;

/**
 * An item for the mouse actions that does nothing.
 * 
 * @author René Mach
 */
public class DoNothingContextMenuItem implements ContextMenuIf  {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(DoNothingContextMenuItem.class);
  protected static final String DONOTHING = "######DONOTHING######";
  private static DoNothingContextMenuItem mInstance;
  
  private DoNothingContextMenuItem() {
    mInstance = this;
  }
  
  /**
   * Gets the instance of this class.
   * <p>
   * @return The instance of this class.
   */
  public static synchronized DoNothingContextMenuItem getInstance() {
    if(mInstance == null) {
      new DoNothingContextMenuItem();
    }
    
    return mInstance;
  }
  
  public ActionMenu getContextMenuActions(Program program) {
    return new ActionMenu(new ContextMenuAction(mLocalizer.msg("doNothing","No action")));
  }

  public String getId() {
    return DONOTHING;
  }

}
