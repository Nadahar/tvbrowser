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

import java.awt.event.KeyEvent;

import javax.swing.InputMap;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import util.ui.OverlayListener;
import devplugin.Program;
import devplugin.ProgramItem;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 01.01.2005
 * Time: 20:25:18
 */
public class PluginTree extends JTree {

  public PluginTree(TreeModel model) {
    super(model);

    /* remove the F2 key from the keyboard bindings of the JTree */
    InputMap inputMap = getInputMap();
    KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
    inputMap.put(keyStroke,"none");
    
    new OverlayListener(this);
  }

  public String convertValueToText(Object value, boolean selected,
                                     boolean expanded, boolean leaf, int row,
                                     boolean hasFocus) {
    if (value instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
      Object o = node.getUserObject();
      if (o instanceof ProgramItem) {
        Program program = ((ProgramItem)o).getProgram();
        int h = program.getHours();
        int m = program.getMinutes();
        return h+":"+(m<10?"0":"")+m+"  " + program.getTitle()+ " (" + program.getChannel().getName()+")";
      }
      else if (o != null) {
        return o.toString();
      }
      else {
        return "null";
      }
    }
    else {
      return value.toString();
    }
  }


 

  public void expandAll(TreePath path) {
    expandPath(path);
    TreeModel model = getModel();
    if (path != null && model != null) {
      Object comp = path.getLastPathComponent();
      int cnt = model.getChildCount(comp);
      for (int i=0; i<cnt; i++) {
        Object node = model.getChild(comp, i);
        expandAll(path.pathByAddingChild(node));
      }
    }


   /* //expandPath(path);

    // Only expand if not leaf!
	  TreeModel          model = getModel();
	  if (path != null && model != null) {
      Object comp = path.getLastPathComponent();
      path.
      if (!model.isLeaf(comp)) {
        setExpandedState(path, true);
        int cnt = model.getChildCount(comp);
        for (int i=0; i<cnt; i++) {
          Object o = model.getChild(comp, i);
          setExpandedState()
        }
      }
   //   model.getChildCount()
      //&& !model.isLeaf(path.getLastPathComponent())) {
    //}
	    //setExpandedState(path, true);
	  } */
  }
}

