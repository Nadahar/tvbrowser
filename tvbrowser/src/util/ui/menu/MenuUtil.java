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

package util.ui.menu;

import devplugin.ActionMenu;

import javax.swing.*;
import java.awt.*;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 02.01.2005
 * Time: 19:07:40
 */
public class MenuUtil {

  public static Font CONTEXT_MENU_PLAINFONT = new Font("Dialog", Font.PLAIN, 12);
  public static Font CONTEXT_MENU_BOLDFONT = new Font("Dialog", Font.BOLD, 12);


  public static JMenuItem createMenuItem(ActionMenu menu) {
    JMenuItem result;
    if (menu.hasSubItems()) {
      result = new JMenu(menu.getAction());
      ActionMenu[] subItems = menu.getSubItems();
      for (int i=0; i<subItems.length; i++) {
        result.add(createMenuItem(subItems[i]));
      }
    }
    else {
      if (menu.isSelected()) {
        result = new JCheckBoxMenuItem(menu.getAction().getValue(Action.NAME).toString(), true);
      }
      else {
        result = new JMenuItem(menu.getAction());
      }
    }
    result.setFont(CONTEXT_MENU_PLAINFONT);
    return result;
  }


}
