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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public abstract class AbstractHierarchicalNode extends DefaultMutableTreeNode
    implements LanguageNodeIf {

  protected String filter;
  protected List<TreeNode> filteredChildren = new ArrayList<TreeNode>();

  public AbstractHierarchicalNode() {
    super();
  }

  public AbstractHierarchicalNode(Object userObject) {
    super(userObject);
  }

  public AbstractHierarchicalNode(Object userObject, boolean allowsChildren) {
    super(userObject, allowsChildren);
  }

  public int translationStateFor(Locale locale) {
    int max = getChildCount();
    int result = STATE_OK;

    for (int i=0;i<max;i++) {
      int state = ((LanguageNodeIf)getChildAt(i)).translationStateFor(locale);
      if (state == STATE_MISSING_TRANSLATION) {
        return STATE_MISSING_TRANSLATION;
      }
      if (state >= STATE_NON_WELLFORMED && state != STATE_OK) {
        result = STATE_NON_WELLFORMED;
      }
    }

    return result;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  public boolean matches() {
    return filter != null && !filteredChildren.isEmpty();
  }

  public void setFilter(Locale locale, String filter) {
    this.filter = null;
    filteredChildren.clear();
    for (int i = 0; i < super.getChildCount(); i++) {
      TreeNode childAt = super.getChildAt(i);
      if (childAt instanceof FilterNodeIf) {
        FilterNodeIf node = (FilterNodeIf) childAt;
        node.setFilter(locale, filter);
        if (filter != null && node.matches()) {
          filteredChildren.add(childAt);
        }
      }
    }
    this.filter = filter;
  }

  @Override
  public int getChildCount() {
    if (filter == null) {
      return super.getChildCount();
    } else {
      return filteredChildren.size();
    }
  }

  @Override
  public TreeNode getChildAt(int index) {
    if (filter == null) {
      return super.getChildAt(index);
    } else {
      return filteredChildren.get(index);
    }
  }

  @Override
  public int getIndex(TreeNode child) {
    if (filter == null) {
      return super.getIndex(child);
    } else {
      return filteredChildren.indexOf(child);
    }
  }

  @Override
  public TreeNode getChildAfter(TreeNode child) {
    if (filter == null) {
      return super.getChildAfter(child);
    } else {
      if (child == null) {
        throw new IllegalArgumentException("argument is null");
      }

      int index = getIndex(child); // linear search

      if (index == -1) {
        throw new IllegalArgumentException("node is not a child");
      }

      if (index < getChildCount() - 1) {
        return getChildAt(index + 1);
      } else {
        return null;
      }
    }
  }

  @Override
  public TreeNode getChildBefore(TreeNode child) {
    if (filter == null) {
      return super.getChildBefore(child);
    } else {
      if (child == null) {
        throw new IllegalArgumentException("argument is null");
      }

      int index = getIndex(child); // linear search

      if (index == -1) {
        throw new IllegalArgumentException("argument is not a child");
      }

      if (index > 0) {
        return getChildAt(index - 1);
      } else {
        return null;
      }
    }
  }

}