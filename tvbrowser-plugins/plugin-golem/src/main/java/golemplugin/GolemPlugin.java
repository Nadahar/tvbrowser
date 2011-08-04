/*
 * Golem.de guckt - Plugin for TV-Browser
 * Copyright (C) 2010 Bodo Tasche
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
 * SVN information:
 *     $Date: 2010-02-20 13:09:24 +0100 (Sa, 20. Feb 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6530 $
 */
package golemplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramRatingIf;
import devplugin.SettingsTab;
import devplugin.Version;

public class GolemPlugin extends devplugin.Plugin {
  private static final boolean PLUGIN_IS_STABLE = true;
  private static final Version PLUGIN_VERSION = new Version(0, 3, 3, PLUGIN_IS_STABLE);

  static final Localizer mLocalizer = Localizer.getLocalizerFor(GolemPlugin.class);

  private boolean hasRightToDownload = false;
  private ImageIcon icon;
  private static GolemPlugin pluginInstance;
  private PluginTreeNode rootNode;
  private GolemSettings settings = new GolemSettings();

  public GolemPlugin() {
    pluginInstance = this;
  }

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }

  public PluginInfo getInfo() {
    return new PluginInfo(
        GolemPlugin.class,
        mLocalizer.msg("name", "Golem.de watches"),
        mLocalizer.msg("desc",
            "Golem.de watches marks shows that are highlighted by Golem.de"),
        "Bodo Tasche", "GPL");
  }

  public void handleTvBrowserStartFinished() {
    hasRightToDownload = true;
  }

  public void handleTvDataUpdateFinished() {
    if (hasRightToDownload) {
      hasRightToDownload = false;
      GolemUpdater.getInstance().update();
      hasRightToDownload = true;
    }
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new GolemSettingsTab();
  }

  public Icon getPluginIcon() {
    if (icon == null) {
      icon = createImageIcon("actions", "golem", 16);
    }
    return icon;
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  public static GolemPlugin getInstance() {
    return pluginInstance;
  }

  public GolemSettings getSettings() {
    return settings;
  }

  public Collection<Program> getProgramList() {
    return getSettings().getProgramList();
  }

  @Override
  public ProgramRatingIf[] getRatingInterfaces() {
    return new ProgramRatingIf[]{new GolemRating()};
  }

  @Override
  public int getMarkPriorityForProgram(Program p) {
    if (settings.isMarkEnabled()) {
      return settings.getMarkPriority();
    }

    return super.getMarkPriorityForProgram(p);
  }

  @Override
  public Icon[] getProgramTableIcons(Program p) {
    if (p == null || getProgramList().contains(p) || getPluginManager().getExampleProgram().equals(p)) {
      return new Icon[]{getPluginIcon()};
    }

    return null;
  }

  @Override
  public Icon[] getMarkIconsForProgram(Program p) {
    if (p == null || getPluginManager().getExampleProgram().equals(p) || (settings.isMarkEnabled() && getProgramList().contains(p))) {
      return new Icon[]{getPluginIcon()};
    }

    return null;
  }

  @Override
  public PluginTreeNode getRootNode() {
    if (rootNode == null) {
      rootNode = new PluginTreeNode(false, this);

      final AtomicReference<ActionMenu> update = new AtomicReference<ActionMenu>(new ActionMenu(new AbstractAction(mLocalizer.msg("update", "Update now")) {
        public void actionPerformed(final ActionEvent e) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              GolemUpdater.getInstance().update();
            }
          });
        }
      }));
      rootNode.addActionMenu(update.get());

      rootNode.update();
    }
    return rootNode;
  }

  @Override
  public ActionMenu getButtonAction() {
    final AbstractAction action = new AbstractAction() {
      public void actionPerformed(final ActionEvent evt) {
        showDialog();
      }
    };

    action.putValue(Action.NAME, mLocalizer.msg("name", "Golem.de watches"));
    action.putValue(Action.SMALL_ICON, getPluginIcon());
    action.putValue(BIG_ICON, getPluginIcon());

    return new ActionMenu(action);
  }

  private void showDialog() {
    GolemDialog d = new GolemDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()));
    UiUtilities.centerAndShow(d);
  }

  @Override
  public void writeData(ObjectOutputStream out) throws IOException {
    settings.writeData(out);
  }

  @Override
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    settings = new GolemSettings(in);
  }
}