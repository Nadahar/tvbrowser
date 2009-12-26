package printplugin;

import java.awt.event.ActionEvent;

import util.ui.TVBrowserIcons;
import devplugin.ButtonAction;
import devplugin.PluginTreeNode;


public class EmptyQueueAction extends ButtonAction {
    /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
        = util.ui.Localizer.getLocalizerFor(EmptyQueueAction.class);

    public EmptyQueueAction() {
      super.setText(mLocalizer.msg("emptyQueue" ,"Druckerwarteschlange leeren"));
      super.setSmallIcon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    }

    public void actionPerformed(ActionEvent e) {
      PluginTreeNode root = PrintPlugin.getInstance().getRootNode();
      root.removeAllChildren();
      root.update();
    }
  }