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

import tvbrowser.ui.pluginview.Node;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramItem;

public final class EpisodeNode extends PluginTreeNode {

  @Override
  public boolean contains(final Program prog, final boolean recursive) {
    return false;
  }

  @Override
  public boolean contains(final Program prog) {
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
    treeNode.setIcon(MediathekPlugin.getInstance().getWebIcon());
    // add web action
    removeAllActions();
    addAction(new LaunchBrowserAction(episode.getUrl()));
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

}
