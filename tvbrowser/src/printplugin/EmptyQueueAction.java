package printplugin;

import devplugin.ButtonAction;
import devplugin.PluginTreeNode;

import java.awt.event.ActionEvent;


public class EmptyQueueAction extends ButtonAction {

    public EmptyQueueAction() {
      super.setText("Druckerwarteschlange leeren");
      super.setSmallIcon(PrintPlugin.getInstance().createIcon("printplugin/imgs/Delete16.gif"));
    }

    public void actionPerformed(ActionEvent e) {
      PluginTreeNode root = PrintPlugin.getInstance().getRootNode();
      root.removeAllChildren();
      root.update();
    }
  }