package tvbrowser.ui.pluginview;

import devplugin.ProgramItem;
import devplugin.Plugin;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 01.01.2005
 * Time: 21:41:07
 */
public class Node extends DefaultMutableTreeNode {

  // We distinguish between the following node types:
  public static final int ROOT = 0;
  public static final int PLUGIN_ROOT = 1;
  public static final int PROGRAM = 2;
  public static final int STRUCTURE_NODE = 3; // a node created by the PluginTreeNode object
  public static final int SORTING_NODE = 4;   // a node created by the plugin

  private int mType;


  public Node(int type, Object o) {
    super(o);
    mType = type;
  }

  public Node(ProgramItem programItem) {
    this(PROGRAM, programItem);
    setAllowsChildren(false);
  }

 

 /* public void setType(int type) {
    mType = type;
  } */

  public int getType() {
    return mType;
  }

  public boolean isLeaf() {
    return !getAllowsChildren();
  }

}
