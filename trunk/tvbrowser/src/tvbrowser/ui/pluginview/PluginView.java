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

package tvbrowser.ui.pluginview;

import java.awt.BorderLayout;
import javax.swing.*;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.TreeNode;



public class PluginView extends JPanel {
    
  private JTree mTree;  
    
  public PluginView() {
    super(new BorderLayout());
    PluginTreeModel model = PluginTreeModel.getInstance();
    
    PluginAccess[] plugins = Plugin.getPluginManager().getActivatedPlugins();
    for (int i=0; i<plugins.length; i++) {
      if (plugins[i].canUseProgramTree()) {
        TreeNode n = Plugin.getPluginManager().getTree(plugins[i].getId());
        model.getPluginNode().add(n);
      }
    }         
    
    
    mTree = new JTree(model);
    add(new JScrollPane(mTree), BorderLayout.CENTER);
  }
 
}