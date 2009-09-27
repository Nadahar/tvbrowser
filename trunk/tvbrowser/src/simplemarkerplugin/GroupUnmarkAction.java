/*
 * SimpleMarkerPlugin by René Mach
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package simplemarkerplugin;

import java.awt.event.ActionEvent;

import devplugin.ContextMenuAction;
import devplugin.PluginTreeNode;

/**
 * SimpleMarkerPlugin 1.4 Plugin for TV-Browser since version 2.3
 * to only mark programs and add them to the Plugin tree.
 * 
 * (Formerly known as Just_Mark ;-))
 * 
 * Unmark all marked programs of a PluginTreeNode.
 * 
 * @author René Mach
 * 
 */
public class GroupUnmarkAction extends ContextMenuAction {

  private static final long serialVersionUID = 1L;
  private PluginTreeNode mNode;
  private MarkList mList;

  /**
   * The Constructor.
   * 
   * @param node
   *          The PluginTreeNode to unmark it's programs.
   * @param list
   *          The list that is related to the node.
   */
  public GroupUnmarkAction(PluginTreeNode node, MarkList list) {
    super();
    mNode = node;
    mList = list;
  }

  public void actionPerformed(ActionEvent e) {
    mList.handleAction(mNode);
  }
}
