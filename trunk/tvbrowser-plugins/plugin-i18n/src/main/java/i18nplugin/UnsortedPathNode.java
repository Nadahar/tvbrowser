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
 *     $Date: 2009-09-27 22:30:36 +0200 (So, 27 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5980 $
 */
package i18nplugin;

import java.io.IOException;

import javax.swing.tree.TreeNode;

/**
 * A Path-Entry
 *
 * @author bodum
 */
public class UnsortedPathNode extends AbstractHierarchicalNode implements FilterNodeIf {

  /**
   * Create Path-Entry
   * @param string Path-Entry
   */
  public UnsortedPathNode(String string) {
    super(string);
  }

  public void save() throws IOException{
    int max = getChildCount();
    for (int i=0;i<max;i++) {
      ((LanguageNodeIf)getChildAt(i)).save();
    }
  }

  public int getMatchCount() {
    if (filter == null) {
      return 0;
    }
    int count = 0;
    for (TreeNode node : filteredChildren) {
      if (node instanceof FilterNodeIf) {
        count += ((FilterNodeIf) node).getMatchCount();
      }
    }
    return count;
  }

}