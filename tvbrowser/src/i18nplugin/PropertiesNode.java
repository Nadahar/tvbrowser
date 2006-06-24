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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package i18nplugin;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * A Properties-File
 * 
 * @author bodum
 */
public class PropertiesNode extends DefaultMutableTreeNode {

  /**
   * Create the Properties-File
   * 
   * @param jarfile Jar-File that contains the Entry
   * @param entry Property-File
   */
  public PropertiesNode(JarFile jarfile, JarEntry entry) {
    super(entry.getName().substring(entry.getName().lastIndexOf('/')+1));
    
    createPropertyEntries(jarfile, entry);
  }

  /**
   * Creates all PropertyEntryNodes
   * 
   * @param jarfile
   * @param entry
   */
  private void createPropertyEntries(JarFile jarfile, JarEntry entry) {
    Properties prop = new Properties();
    try {
      prop.load(jarfile.getInputStream(entry));
      
      Enumeration keys = prop.keys();
      
      while (keys.hasMoreElements()) {
        add(new PropertiesEntryNode((String) keys.nextElement()));
      }
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  @SuppressWarnings("unchecked")
  @Override
  public void insert(MutableTreeNode newChild, int childIndex) {
    super.insert(newChild, childIndex);
    
    Collections.sort(children, new Comparator<TreeNode>() {
      public int compare(TreeNode o1, TreeNode o2) {
        return o1.toString().compareTo(o2.toString());
      }
    });
  }  
}