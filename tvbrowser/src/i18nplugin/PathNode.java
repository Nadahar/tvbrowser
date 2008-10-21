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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package i18nplugin;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * A Path-Entry
 * 
 * @author bodum
 */
public class PathNode extends AbstractHierarchicalNode implements LanguageNodeIf, FilterNodeIf {
  
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

  /*
   * (non-Javadoc)
   * @see i18nplugin.LanguageNodeIf#save()
   */
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