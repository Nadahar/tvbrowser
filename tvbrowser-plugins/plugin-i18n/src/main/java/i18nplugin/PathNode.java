/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date: 2011-03-13 09:27:31 +0100 (So, 13 Mrz 2011) $
 *   $Author: bananeweizen $
 * $Revision: 6946 $
 */
package i18nplugin;

import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * A Path-Entry
 *
 * @author bodum
 */
public class PathNode extends UnsortedPathNode implements FilterNodeIf {

  /**
   * Create Path-Entry
   * @param string Path-Entry
   */
  public PathNode(String string) {
    super(string);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void insert(MutableTreeNode newChild, int childIndex) {
    super.insert(newChild, childIndex);

    Collections.sort(children, new Comparator<TreeNode>() {
      public int compare(TreeNode o1, TreeNode o2) {

        if ((o1 instanceof PropertiesNode) && !(o2 instanceof PropertiesNode)) {
          return 1;
        }

        if ((o2 instanceof PropertiesNode) && !(o1 instanceof PropertiesNode)) {
          return -1;
        }

        return o1.toString().compareTo(o2.toString());
      }
    });
  }
}