/*
 * TV-Browser
 * Copyright (C) 2011 TV-Browser team (dev@tvbrowser.org)
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Program;

public class SelectProgramContextMenuItem implements ContextMenuIf {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(SelectProgramContextMenuItem.class);
  
  private static SelectProgramContextMenuItem mInstance;
  
  protected static final String SELECTPROGRAM = "######SELECTPROGRAM######";
  
  private SelectProgramContextMenuItem() {
    mInstance = this;
  }
  
  public static SelectProgramContextMenuItem getInstance() {
    if(mInstance == null) {
      new SelectProgramContextMenuItem();
    }
    
    return mInstance;
  }
  
  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    AbstractAction action = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            MainFrame.getInstance().selectProgram(program);          
          }
        });
      }
    };
    action.putValue(Action.NAME,toString());
    
    return new ActionMenu(action);
  }

  @Override
  public String getId() {
    return SELECTPROGRAM;
  }
  
  public String toString() {
    return mLocalizer.msg("selectProgram","Select programs");
  }

}
