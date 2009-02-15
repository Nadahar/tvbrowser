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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.pluginview.PluginTree;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.menu.MenuUtil;
import devplugin.ActionMenu;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 03.01.2005
 * Time: 22:07:57
 */
public abstract class PluginContextMenu extends AbstractContextMenu {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(PluginContextMenu.class);

  private ActionMenu[] mActionMenus;
  private Action mDefaultAction;
  private TreePath mPath;

  public PluginContextMenu(PluginTree tree, TreePath path, ActionMenu[] menus) {
    super(tree);
    initDefaultActions(path, menus);
  }

  private void initDefaultActions(TreePath path, ActionMenu[] menus) {
    mDefaultAction = getCollapseExpandAction(path);
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
    menu.add(getExpandAllMenuItem(mPath));
    menu.add(getCollapseAllMenuItem(mPath));
    menu.add(getFilterMenuItem(mPath));
    menu.add(getExportMenu( mPath));
    
    ActionMenu pluginAction = getButtonAction();
    if (pluginAction != null) {
      menu.addSeparator();
      Action action = pluginAction.getAction();
      JMenuItem pluginMI = new JMenuItem(action);
      pluginMI.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
      menu.add(pluginMI);
    }

    if (mActionMenus.length>0) {
      for (ActionMenu actionMenu : mActionMenus) {
        JMenuItem menuItem = MenuUtil.createMenuItem(actionMenu);
        menu.add(menuItem);
      }
    }

    menu.addSeparator();
    if (hasSettingsTab()) {
      JMenuItem menuItem = MenuUtil.createMenuItem(Localizer.getLocalization(Localizer.I18N_SETTINGS)+"...");
      menuItem.setIcon(IconLoader.getInstance().getIconFromTheme("categories", "preferences-system", 16));
      menuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          MainFrame.getInstance().showSettingsDialog(getPluginId());
        }
      });
      menu.add(menuItem);
    }
    JMenuItem menuItem = MenuUtil.createMenuItem(mLocalizer.msg(
        "disablePlugin", "Disable plugin"));
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final PluginProxy plugin = PluginProxyManager.getInstance()
            .getPluginForId(getPluginId());
        if (plugin != null) {
          try {
            PluginProxyManager.getInstance().deactivatePlugin(plugin);
          } catch (TvBrowserException e1) {
            e1.printStackTrace();
          }
        }
      }
    });
    menu.add(menuItem);
    

    return menu;
  }

  protected abstract String getPluginId();
  protected abstract boolean hasSettingsTab();
  protected abstract ActionMenu getButtonAction();

  public Action getDefaultAction() {
    return null;
  }
}
