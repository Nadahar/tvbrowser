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
import devplugin.ActionMenu;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramItem;

public class EpisodeNode extends PluginTreeNode {

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

  public EpisodeNode(String title) {
    super(title);
    final Node treeNode = getMutableTreeNode();
    treeNode.setAllowsChildren(false);
    treeNode.setIcon(MediathekPlugin.getInstance().getWebIcon());
    removeAllActions();
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

}
