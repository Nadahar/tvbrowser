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
 *     $Date: 2010-05-02 20:34:40 +0200 (So, 02 Mai 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6612 $
 */
package i18nplugin;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.commons.lang.StringUtils;

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
            String dir = StringUtils.substringBeforeLast(name, "/");

            if (dir.contains("/")) {
							dir = StringUtils.substringAfterLast(dir, "/");
						}

            name = StringUtils.substringAfterLast(name,"/");

            if (name.equals(dir)) {
							addEntry(jarfile, entry);
						}
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

          if (deep == strings.length-1) {
						return child;
					}

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
}