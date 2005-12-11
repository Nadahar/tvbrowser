package printplugin;

import java.awt.event.ActionEvent;

import devplugin.ButtonAction;
import devplugin.PluginTreeNode;


public class EmptyQueueAction extends ButtonAction {

    public EmptyQueueAction() {
      super.setText("Druckerwarteschlange leeren");
      super.setSmallIcon(PrintPlugin.getInstance().createImageIcon("actions", "edit-delete", 16));
    }

    public void actionPerformed(ActionEvent e) {
      PluginTreeNode root = PrintPlugin.getInstance().getRootNode();
      root.removeAllChildren();
      root.update();
    }
  }