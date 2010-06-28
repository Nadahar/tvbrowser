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


package tvbrowser.ui.pluginview.contextmenu;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.pluginview.Node;
import tvbrowser.ui.pluginview.PluginTree;
import tvbrowser.ui.programtable.ProgramTable;
import util.ui.menu.MenuUtil;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramItem;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 03.01.2005
 * Time: 22:07:30
 */
public class ProgramContextMenu extends AbstractContextMenu {

  /**
   * The localizer for this class.
   */
  private static final util.ui.Localizer mLocalizer
          = util.ui.Localizer.getLocalizerFor(ProgramContextMenu.class);

  private TreePath[] mPaths;
  private Action mDefaultAction;
  private Program[] mPrograms;

  public ProgramContextMenu(PluginTree tree, TreePath[] paths, Plugin plugin, Program[] programs) {
    super(tree);
    mPaths = paths;
    mPrograms = programs;
    mDefaultAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Node node = (Node) mPaths[0].getLastPathComponent();
        ProgramItem programItem = (ProgramItem) node.getUserObject();
        final Program program = programItem.getProgram();
        MainFrame.getInstance().scrollToProgram(program, new Runnable() {
          public void run() {
            ProgramTable table = MainFrame.getInstance().getProgramTableScrollPane().getProgramTable();
            table.deSelectItem();
            table.selectProgram(program);
          }});
      }
    };
    mDefaultAction.putValue(Action.NAME, mLocalizer.msg("show", "show"));
  }

  public JPopupMenu getPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem showMI = new JMenuItem(mDefaultAction);
    showMI.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);

    showMI.setEnabled(mPaths.length == 1);
    menu.add(showMI);

    menu.add(getExportMenu(mPaths[0]));

    menu.addSeparator();

    JMenu menus = ContextMenuManager.getInstance().createContextMenuItems(null, mPrograms[0], false);

    Component[] comps = menus.getMenuComponents();
    for (Component comp : comps) {
      menu.add(comp);
      comp.setEnabled(mPrograms.length == 1);
    }

    return menu;
  }

  public Action getDefaultAction() {
    if (mPaths.length == 1) {
      return mDefaultAction;
    } else {
      return null;
    }
  }
}