package justmark;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import devplugin.ContextMenuAction;
import devplugin.PluginTreeNode;

/**
 * Unmark all marked programs of a PluginTreeNode.
 * 
 * @author René Mach
 * 
 */
public class GroupUnmarkAction extends ContextMenuAction implements
    ActionListener {

  private static final long serialVersionUID = 1L;
  private PluginTreeNode mNode;

  /**
   * The Constructor.
   * 
   * @param node
   *          The PluginTreeNode to unmark it's programs.
   */
  public GroupUnmarkAction(PluginTreeNode node) {
    super();
    mNode = node;
  }

  public void actionPerformed(ActionEvent e) {
    JustMark.getInstance().handleAction(mNode, e);
  }
}
