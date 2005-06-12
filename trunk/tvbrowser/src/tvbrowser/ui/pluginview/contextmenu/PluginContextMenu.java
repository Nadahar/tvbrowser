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

import devplugin.Plugin;
import devplugin.ActionMenu;
import devplugin.SettingsTab;

import javax.swing.tree.TreePath;
import javax.swing.*;

import util.ui.menu.MenuUtil;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import tvbrowser.ui.mainframe.MainFrame;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 03.01.2005
 * Time: 22:07:57
 */
public class PluginContextMenu extends AbstractContextMenu {

  /** The localizer for this class. */
     private static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(PluginContextMenu.class);


  private ActionMenu[] mActionMenus;
  private Action mDefaultAction;
  private Plugin mPlugin;
  private TreePath mPath;

  public PluginContextMenu(JTree tree, TreePath path, Plugin plugin, ActionMenu[] menus) {
    super(tree);
    mDefaultAction = getCollapseExpandAction(path);
    mPlugin = plugin;
    mPath = path;
    mActionMenus = menus;
    if (mActionMenus == null) {
      mActionMenus = new ActionMenu[]{};
    }
  }

  public JPopupMenu getPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem defaultMI = new JMenuItem(mDefaultAction);
    menu.add(defaultMI);
    defaultMI.setFont(MenuUtil.CONTEXT_MENU_BOLDFONT);

    menu.add(getExportMenu( mPath));
    
    ActionMenu pluginAction = mPlugin.getButtonAction();
    if (pluginAction != null) {
      menu.addSeparator();
      Action action = pluginAction.getAction();
      JMenuItem pluginMI = new JMenuItem(action);
      pluginMI.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
      menu.add(pluginMI);
    }

    if (mActionMenus.length>0) {
      for (int i=0; i<mActionMenus.length; i++) {
        JMenuItem menuItem = MenuUtil.createMenuItem(mActionMenus[i]);
        menu.add(menuItem);
      }
    }


    SettingsTab settingsTab = mPlugin.getSettingsTab();
    if (settingsTab != null) {
      menu.addSeparator();
      JMenuItem menuItem = MenuUtil.createMenuItem(mLocalizer.msg("settings","settings"));
      menu.add(menuItem);
      menuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          MainFrame.getInstance().showSettingsDialog(mPlugin.getId());
        }
      });
    }

    return menu;
  }

  public Action getDefaultAction() {
    return null;
  }
}
