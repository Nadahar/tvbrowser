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

import java.awt.Font;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import util.ui.ScrollableMenu;

import devplugin.ActionMenu;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 02.01.2005
 * Time: 19:07:40
 */
public class MenuUtil {

  public static Font CONTEXT_MENU_PLAINFONT = new Font("Dialog", Font.PLAIN, 12);
  public static Font CONTEXT_MENU_BOLDFONT = new Font("Dialog", Font.BOLD, 12);
  public static Font CONTEXT_MENU_ITALICFONT = new Font("Dialog", Font.ITALIC, 12);
  public static Font CONTEXT_MENU_BOLDITALICFONT = new Font("Dialog", Font.BOLD + Font.ITALIC, 12);


  public static JMenuItem createMenuItem(String title) {
    JMenuItem result = new JMenuItem(title);
    result.setFont(CONTEXT_MENU_PLAINFONT);
    return result;
  }

  public static JMenuItem createMenuItem(ActionMenu menu) {
    return createMenuItem(menu, true);
  }
  
  public static JMenuItem createMenuItem(ActionMenu menu, boolean setFont) {
    if (menu == null) {
      return null;
    }
    JMenuItem result = null;
    if (menu.hasSubItems()) {
      result = new ScrollableMenu(menu.getAction());
      ActionMenu[] subItems = menu.getSubItems();
      for (int i=0; i<subItems.length; i++) {
        JMenuItem item = createMenuItem(subItems[i], setFont);
        if (item == null) {
          ((ScrollableMenu)result).addSeparator();
        }
        else {
          result.add(item);
        }
      }
    }
    else {
      if (menu.isSelected()) {
        result = new JCheckBoxMenuItem(menu.getAction().getValue(Action.NAME).toString(), true);
      }
      else if (menu.getAction()!=null) {
        result = new JMenuItem(menu.getAction());
      }
    }
    if (result != null && setFont) {
      result.setFont(CONTEXT_MENU_PLAINFONT);
    }
    return result;
  }


}