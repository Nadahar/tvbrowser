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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicMenuUI;

import util.ui.ScrollableMenu;

import devplugin.ActionMenu;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.Program;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 02.01.2005
 * Time: 19:07:40
 */
public class MenuUtil {

  public static final Font CONTEXT_MENU_PLAINFONT = new Font("Dialog", Font.PLAIN, 12);
  public static final Font CONTEXT_MENU_BOLDFONT = new Font("Dialog", Font.BOLD, 12);
  public static final Font CONTEXT_MENU_ITALICFONT = new Font("Dialog", Font.ITALIC, 12);
  public static final Font CONTEXT_MENU_BOLDITALICFONT = new Font("Dialog", Font.BOLD + Font.ITALIC, 12);


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
      checkAndSetBackgroundColor(result);
      ActionMenu[] subItems = menu.getSubItems();
      for (ActionMenu subItem : subItems) {
        JMenuItem item = createMenuItem(subItem, setFont);

        if (item == null) {
          ((JMenu) result).addSeparator();
        } else {
          checkAndSetBackgroundColor(item);
          result.add(item);
        }
      }
    }
    else {
      if (menu.isSelected()) {
        result = new JCheckBoxMenuItem(menu.getAction().getValue(Action.NAME).toString(), true);
      }
      else if(ContextMenuSeparatorAction.getInstance().equals(menu.getAction())) {
        return null;
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

  private static void checkAndSetBackgroundColor(JMenuItem item) {
    Action action = item.getAction();
    if (action == null) {
      return;
    }
    Object o = action.getValue(Program.MARK_PRIORITY);
    
    if(o != null && o instanceof Integer && !UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
      Color color = Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority((Integer)o);
      
      if(color == null) {
        color = item.getBackground();
      }
      
      final Color co = color;
      
      if(item instanceof ScrollableMenu) {
        item.setUI(new BasicMenuUI() {
          protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
            if(!menuItem.isSelected()) {
              Insets i = menuItem.getInsets();
              
              g.clearRect(0,0,menuItem.getWidth(),menuItem.getHeight());
              g.setColor(menuItem.getBackground());
              g.fillRect(0,0,menuItem.getWidth(),menuItem.getHeight());
              g.setColor(Color.white);
              
              g.fillRect(i.left,i.top,menuItem.getWidth()-i.left-i.right,menuItem.getHeight()-i.top-i.bottom);
              g.setColor(co);
              g.fillRect(i.left,i.top,menuItem.getWidth()-i.left-i.right,menuItem.getHeight()-i.top-i.bottom);
            }
            else {
              super.paintBackground(g,menuItem,bgColor);
            }          }
          
          protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect,
              String text) {
            if (menuItem.isSelected()) {
              g.setColor(selectionForeground);
            }else {
              g.setColor(menuItem.getForeground());
            }
            
            g.drawString(menuItem.getText(), textRect.x, textRect.y + menuItem.getFontMetrics(menuItem.getFont()).getAscent());
          }
        });
      }
      else {
        item.setUI(new BasicMenuItemUI() {
          protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
            if(!menuItem.isArmed()) {
              Insets i = menuItem.getInsets();
  
              g.clearRect(0,0,menuItem.getWidth(),menuItem.getHeight());
              g.setColor(menuItem.getBackground());
              g.fillRect(0,0,menuItem.getWidth(),menuItem.getHeight());
              g.setColor(Color.white);
              
              g.fillRect(i.left,i.top,menuItem.getWidth()-i.left-i.right,menuItem.getHeight()-i.top-i.bottom);
              g.setColor(co);
              g.fillRect(i.left,i.top,menuItem.getWidth()-i.left-i.right,menuItem.getHeight()-i.top-i.bottom);
            }
            else {
              super.paintBackground(g,menuItem,bgColor);
            }
          }
          
          protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect,
              String text) {
            if (menuItem.isArmed()) {
              g.setColor(selectionForeground);
            }else {
              g.setColor(menuItem.getForeground());
            }
            
            g.drawString(text, textRect.x, textRect.y + menuItem.getFontMetrics(menuItem.getFont()).getAscent());
          }
        });    
      }
    }
  }
}