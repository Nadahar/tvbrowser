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
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.pluginview.Node;
import tvbrowser.ui.pluginview.PluginTree;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.menu.MenuUtil;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgramItem;
import devplugin.ProgramReceiveTarget;


/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 03.01.2005
 * Time: 22:12:32
 */
public abstract class AbstractContextMenu implements ContextMenu {

  /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(AbstractContextMenu.class);

  private PluginTree mTree;

  protected AbstractContextMenu(PluginTree tree) {
    mTree = tree;
  }


  protected JMenuItem getExpandAllMenuItem(final TreePath treePath) {

    Action action = new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        mTree.expandAll(treePath);
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("expandAll","Expand All"));

    JMenuItem item = new JMenuItem(action);
    item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
    return item;
  }

  protected JMenuItem getCollapseAllMenuItem(final TreePath treePath) {

		Action action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				mTree.collapseAll(treePath);
			}
		};

		action.putValue(Action.NAME, mLocalizer.msg("collapseAll",
				"Collapse All"));

		JMenuItem item = new JMenuItem(action);
		item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
		return item;
	}

  protected Action getCollapseExpandAction(final TreePath treePath) {

    final boolean mIsExpanded = mTree.isExpanded(treePath);

    Action action = new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        if (mIsExpanded) {
          mTree.collapsePath(treePath);
        }
        else {
          mTree.expandPath(treePath);
        }
      }
    };
    if (mIsExpanded) {
      action.putValue(Action.NAME, mLocalizer.msg("collapse","collapse"));
    }
    else {
      action.putValue(Action.NAME, mLocalizer.msg("expand","expand"));
    }

    return action;
  }

  /**
   * Create a Export-To-Other-Plugins Action
   * @return Export-To-Other-Plugins Action
   * @param paths create action for this TreePath
   */
  protected JMenu getExportMenu(TreePath paths) {
    final Node node = (Node) paths.getLastPathComponent();

    JMenu menu = new JMenu(mLocalizer.msg("export","Export"));
    menu.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);

    if ((node.getChildCount() == 0) && (node.getType() != Node.PROGRAM)) {
      menu.setEnabled(false);
      return menu;
    }

    Object o = getObjectForNode(node);
    Plugin currentPlugin = null;

    if(o instanceof Plugin) {
      currentPlugin = (Plugin)o;
    }

    if(o != ReminderPlugin.getRootNode().getMutableTreeNode()) {
      final ProgramReceiveTarget reminderTarget = ReminderPluginProxy
          .getInstance().getProgramReceiveTargets()[0];
      JMenuItem item = new JMenuItem(reminderTarget.getTargetName());
      item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
      item.setIcon(IconLoader.getInstance().getIconFromTheme("apps","appointment",16));
      menu.add(item);
      item.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          Program[] programs = collectProgramsFromNode(node);
          if ((programs != null) &&(programs.length > 0)) {
            ReminderPluginProxy.getInstance().receivePrograms(programs,
                reminderTarget);
          }
        }
      });
    }

    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    for (final PluginProxy plugin : plugins) {
     if ((plugin.canReceiveProgramsWithTarget())
          && plugin.getProgramReceiveTargets() != null
          && plugin.getProgramReceiveTargets().length > 0) {
        if ((currentPlugin == null) || (!currentPlugin.getId().equals(plugin.getId()))) {
          ProgramReceiveTarget[] targets = plugin.getProgramReceiveTargets();
          if (!plugin.canReceiveProgramsWithTarget()) {
            JMenuItem item = new JMenuItem(plugin.getInfo().getName());
            item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);

            Icon icon = plugin.getPluginIcon();

            item.setIcon(icon != null ? icon : null);
            menu.add(item);
            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                Program[] programs = collectProgramsFromNode(node);
                if ((programs != null) && (programs.length > 0)) {
                  plugin.receivePrograms(programs, ProgramReceiveTarget.createDefaultTargetForProgramReceiveIfId(plugin.getId()));
                }
              }
            });
          } else if (targets.length == 1 && (!(o instanceof ProgramReceiveTarget) || !o.equals(targets[0]))) {
            JMenuItem item = new JMenuItem(targets[0].toString());
            item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);

            Icon icon = plugin.getPluginIcon();

            item.setIcon(icon != null ? icon : null);
            menu.add(item);

            final ProgramReceiveTarget target = targets[0];

            item.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                Program[] programs = collectProgramsFromNode(node);
                if ((programs != null) && (programs.length > 0)) {
                  plugin.receivePrograms(programs, target);
                }
              }
            });
          } else if (targets.length >= 1) {
            JMenu subMenu = new JMenu(plugin.getInfo().getName());
            subMenu.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);

            Icon icon = plugin.getPluginIcon();

            subMenu.setIcon(icon != null ? icon : null);
            menu.add(subMenu);

            for (final ProgramReceiveTarget target : targets) {
              if (o == null || !o.equals(target)) {
                JMenuItem item = new JMenuItem(target.toString());
                item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
                subMenu.add(item);

                item.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                    Program[] programs = collectProgramsFromNode(node);
                    if ((programs != null) && (programs.length > 0)) {
                      plugin.receivePrograms(programs, target);
                    }
                  }
                });
              }
            }
          }
        }
      }
    }

    return menu;
  }

  protected JMenuItem getFilterMenuItem(final TreePath treePath) {
    final Node node = (Node) treePath.getLastPathComponent();
    String pathName = "";
    for (int i = 1; i < treePath.getPathCount(); i++) {
      if (i > 1) {
        pathName = pathName + "/";
      }
      pathName = pathName + treePath.getPathComponent(i);
    }
    final String filterName = mLocalizer.msg("pluginFilter.name", "Plugin filter ({0})", pathName);

    Action action = new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        Program[] programs = collectProgramsFromNode(node);
        final ArrayList<Program> programList = new ArrayList<Program>();
        if (programs != null) {
          for (Program program : programs) {
            programList.add(program);
          }
          if (programs.length > 0) {
            ProgramFilter pluginFilter = new ProgramFilter() {

              public boolean accept(Program prog) {
                return programList.contains(prog);
              }

              public String getName() {
                return filterName;
              }
            };
            MainFrame.getInstance().setProgramFilter(pluginFilter);
          }
          else {
            JOptionPane
                .showMessageDialog(
                    UiUtilities.getBestDialogParent(MainFrame.getInstance()),
                    mLocalizer
                        .msg(
                            "pluginFilter.noPrograms",
                            "The plugin has marked no program, therefore your current filter will remain active."),
                    mLocalizer.msg("pluginFilter.noProgramsTitle",
                        "No programs marked"), JOptionPane.INFORMATION_MESSAGE);
          }
        }
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("filter","Show only contained programs"));

    JMenuItem item = new JMenuItem(action);
    item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
    item.setIcon(TVBrowserIcons.filter(TVBrowserIcons.SIZE_SMALL));
    return item;
  }

  /**
   * Returns the Plugin for this Node.
   * It searches for a Parent-Node containing a Plugin.
   *
   * @param node Node to use
   * @return Plugin-Parent of this Node
   */
  public Object getObjectForNode(Node node) {

    Node parent = node;

    while (parent != null && parent.getType() != Node.PLUGIN_ROOT && parent != ReminderPlugin.getRootNode().getMutableTreeNode() && parent.getProgramReceiveTarget() == null) {
      parent = (Node) parent.getParent();
    }

    if (parent != null){
      if(parent.getProgramReceiveTarget() != null) {
        return parent.getProgramReceiveTarget();
      }

      Object o = parent.getUserObject();

      if(o instanceof Plugin) {
        return o;
      } else {
        return parent;
      }
    }

    return null;
  }

  /**
   * Runs through all Child-Nodes and collects the Program-Elements
   *
   * @param node Node to search in
   * @return all found Programs within this Node
   */
  public Program[] collectProgramsFromNode(Node node) {

    if (node.getType() == Node.PROGRAM) {
      return new Program[]{ ((ProgramItem) node.getUserObject()).getProgram() };
    }

    if (node.getChildCount() == 0) {
      return null;
    }

    ArrayList<Program> array = new ArrayList<Program>();

    for (int i=0;i<node.getChildCount();i++) {

      Program[] programs = collectProgramsFromNode((Node)node.getChildAt(i));
      if ((programs != null) && (programs.length != 0)) {

        for (Program prg : programs) {
          if (!array.contains(prg)) {
            array.add(prg);
          }
        }
      }
    }

    return array.toArray(new Program[array.size()]);
  }
}