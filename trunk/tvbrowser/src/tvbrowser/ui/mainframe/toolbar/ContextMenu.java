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

package tvbrowser.ui.mainframe.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.ToolBarDragAndDropSettings;

public class ContextMenu {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ContextMenu.class);

  private JComponent mComponent;
  private JPopupMenu mMenu;

  public ContextMenu(JComponent component) {
    if(ToolBarDragAndDropSettings.getInstance() != null)
      return;
    mComponent = component;    
    mMenu = new JPopupMenu();
  }

  public void show(int x, int y) {
    if(ToolBarDragAndDropSettings.getInstance() != null)
      return;
    update();
    mMenu.show(mComponent, x, y);
  }

  protected static JMenu getSubMenu() {
    JMenu menu = new JMenu(mLocalizer.msg("toolbar", "Toolbar"));
    menu.add(createViewMenu());
    if(Settings.propIsTooolbarVisible.getBoolean())
      menu.add(createViewSearchMenu());
    menu.addSeparator();
    menu.add(createConfigureItem());

    return menu;
  }

  private void update() {
    mMenu.removeAll();
    mMenu.add(createViewMenu());
    if(Settings.propIsTooolbarVisible.getBoolean())
      mMenu.add(createViewSearchMenu());
    mMenu.addSeparator();
    if (ToolBarDragAndDropSettings.getInstance() == null)
      mMenu.add(createConfigureItem());
  }

  private static JCheckBoxMenuItem createViewSearchMenu() {
    final JCheckBoxMenuItem showSearch = new JCheckBoxMenuItem(
        ToolBarDragAndDropSettings.mLocalizer
            .msg("showSearchField", "Show search field"));
    showSearch.setSelected(Settings.propIsSearchFieldVisible.getBoolean());
    showSearch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().setShowSearchField(showSearch.isSelected());
      }
    });    
    
    return showSearch;    
  }
  
  private static JCheckBoxMenuItem createViewMenu() {
    final JCheckBoxMenuItem show = new JCheckBoxMenuItem(
        ToolBarDragAndDropSettings.mLocalizer
            .msg("showToolbar", "Show toolbar"));
    show.setSelected(Settings.propIsTooolbarVisible.getBoolean());
    show.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().setShowToolbar(show.isSelected());
      }
    });    
    
    return show;
  }

  private static JMenuItem createConfigureItem() {
    JMenuItem item = new JMenuItem(mLocalizer.ellipsisMsg("configure", "Configure"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new ToolBarDragAndDropSettings();
      }
    });
    return item;
  }

}