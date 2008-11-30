/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathekplugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import tvbrowser.ui.pluginview.Node;
import util.browserlauncher.Launch;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramItem;

public class EpisodeNode extends PluginTreeNode {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(EpisodeNode.class);

  @Override
  public ActionMenu[] getActionMenus() {
    // TODO Auto-generated method stub
    return super.getActionMenus();
  }

  @Override
  public boolean contains(Program prog, boolean recursive) {
    return false;
  }

  @Override
  public boolean contains(Program prog) {
    return false;
  }

  @Override
  public ProgramItem[] getProgramItems() {
    return new ProgramItem[0];
  }

  @Override
  public Program[] getPrograms() {
    return new Program[0];
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public int size() {
    return 0;
  }

  public EpisodeNode(final MediathekProgramItem episode) {
    super(episode.getTitle());
    final Node treeNode = getMutableTreeNode();
    treeNode.setAllowsChildren(false);
    final MediathekPlugin plugin = MediathekPlugin.getInstance();
    treeNode.setIcon(plugin.getWebIcon());
    // add web action
    removeAllActions();
    addAction(new AbstractAction(mLocalizer.msg("action.openMedia",
        "Open in browser"), Plugin.getPluginManager().getIconFromTheme(plugin,
        "apps", "internet-web-browser", 16)) {

      public void actionPerformed(ActionEvent e) {
        Launch.openURL(episode.getUrl());
      }
    });
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

}
