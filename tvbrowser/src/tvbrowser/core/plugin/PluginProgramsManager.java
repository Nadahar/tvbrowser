/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.core.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;



import tvbrowser.core.Settings;
import tvbrowser.ui.pluginview.PluginTreeModel;
import devplugin.PluginAccess;





public class PluginProgramsManager {
  
  private static String DIRECTORY_NAME = "nodes"; 
      
  private static PluginProgramsManager mInstance;
  private HashMap mPluginTrees;
    
  private PluginProgramsManager() {
    mPluginTrees = new HashMap();  
  }
  
  public static PluginProgramsManager getInstance() {
    if (mInstance == null) {
      mInstance = new PluginProgramsManager();
    }
    return mInstance;
  }
    /*
  public TreeNode getTree(PluginAccess plugin) {
    TreeNode node = (devplugin.TreeNode)mPluginTrees.get(plugin);
    if (node == null) {
      node = loadTree(plugin);
      mPluginTrees.put(plugin, node);
    }
    return node;
  }
  
  public void storeTrees() {
    Set plugins = mPluginTrees.keySet();
    Iterator it = plugins.iterator();
    while (it.hasNext()) {
      PluginAccess plugin = (PluginAccess)it.next();
      storeTree(plugin);
    }
  }
 
  private void storeTree(PluginAccess plugin) {
    File nodesDirectory = new File(Settings.getUserDirectoryName(),DIRECTORY_NAME);
    nodesDirectory.mkdirs();
    File f = new File(nodesDirectory,plugin.getId()+".nodes");
    TreeNodeImpl node = (TreeNodeImpl)mPluginTrees.get(plugin);
    try {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
        node.write(out);
        out.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
    
  }
  
  private TreeNode loadTree(PluginAccess plugin) {
    File nodesDirectory = new File(Settings.getUserDirectoryName(),DIRECTORY_NAME);
    
    PluginTreeModel model = PluginTreeModel.getInstance();
    TreeNodeImpl node = model.createNode(plugin.getId(), plugin.getInfo().getName());
    try {
      File f = new File(nodesDirectory,plugin.getId()+".nodes");
      if (!f.exists()) {
        return node;
      }
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
      node.read(in);
      in.close();
    }catch(IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return node; 
     
  }
    */
    
}