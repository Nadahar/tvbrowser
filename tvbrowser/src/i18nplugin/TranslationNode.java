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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * This Node creates a Tree of all Properties in a certain Jar-File
 * 
 * @author bodum
 */
public class TranslationNode extends PathNode {
  
  /**
   * Create Tree
   * @param string Name of Tree-Node
   * @param file Jar-File with Properties
   */
  public TranslationNode(String string, File file) {
    super(string);
    
    try {
      JarFile jarfile = new JarFile(file);
      
      Enumeration<JarEntry> entries = jarfile.entries();
      
      while(entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        
        if (entry.getName().endsWith(".properties")) {
          String name = entry.getName();

          name = name.substring(0, name.length()-11);
          
          if (name.indexOf('/') >= 0) {
            String dir = name.substring(0, name.lastIndexOf('/'));
            
            if (dir.contains("/"))
              dir = dir.substring(dir.lastIndexOf('/')+1);

            name = name.substring(name.lastIndexOf('/')+1);
            
            if (name.equals(dir))
              addEntry(jarfile, entry);
          }
        }
      }
      
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }

  /**
   * Add a Property-File
   * @param jarfile Main-Jar
   * @param entry Property-File
   */
  private void addEntry(JarFile jarfile, JarEntry entry) {
    String name = entry.getName();
    DefaultMutableTreeNode path = getPath(name.substring(0, name.lastIndexOf('/')));
    path.add(new PropertiesNode(jarfile, entry));
  }

  /**
   * Get Path for String. If Path doesn't exist, it will be created
   * 
   * @param string Path
   * @return Node
   */
  private PathNode getPath(String string) {
    return findPath(this, string.split("/"), 0);
  }

  /**
   * Find the path recursive. If the Path was not found, it will
   * be created 
   * 
   * @param node Node to add Path
   * @param strings Path-Elements
   * @param deep Current Path
   * @return created Node
   */
  private PathNode findPath(PathNode node, String[] strings, int deep) {
    int max = node.getChildCount();
    for (int i=0;i<max;i++) {
      if (node.getChildAt(i) instanceof PathNode) {
        PathNode child = (PathNode) node.getChildAt(i);
        if(child.toString().equals(strings[deep])) {
          
          if (deep == strings.length-1)
            return child;
          
          return findPath(child, strings, deep+1);
        }
      }
    }

    PathNode child = node;
    
    for (int i=deep;i<strings.length;i++) {
      PathNode newchild = new PathNode(strings[i]);
      child.add(newchild);
      child = newchild;
    }
    
    return child;
  }
  
  @Override
  public boolean isLeaf() {
    return false;
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

      int index = getIndex(child);   // linear search

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

      int index = getIndex(child);   // linear search

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