package tvbrowser.ui.pluginview;

import devplugin.ProgramItem;
import devplugin.Program;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 01.01.2005
 * Time: 20:25:18
 */
public class PluginTree extends JTree {

  public PluginTree(TreeModel model) {
    super(model);
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
        return program.getTitle()+ " (" + program.getChannel().getName()+" "+h+":"+(m<10?"0":"")+m+")";
      }
      else {
        return o.toString();
      }
    }
    else {
      System.out.println(value.getClass().getName());
      return value.toString();
    }
  }
}

