package mediathekplugin;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;

import util.ui.SearchFormSettings;
import util.ui.SearchHelper;
import devplugin.Channel;
import devplugin.Plugin;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramItem;

public class MediathekProgramNode extends PluginTreeNode {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(MediathekProgramNode.class);

  @Override
  public boolean contains(Program prog, boolean recursive) {
    return false;
  }

  @Override
  public boolean contains(Program prog) {
    return false;
  }

  @Override
  public ProgramItem[] getProgramItems() {
    return new ProgramItem[0];
  }

  @Override
  public Program[] getPrograms() {
    return new Program[0];
  }

  public MediathekProgramNode(final String title) {
    super(title);
    setGroupingByDateEnabled(false);
    getMutableTreeNode().setShowLeafCountEnabled(false);
    final MediathekPlugin plugin = MediathekPlugin.getInstance();
    addAction(new AbstractAction(mLocalizer.msg("action.search",
        "Search programs"), Plugin.getPluginManager().getIconFromTheme(plugin,
        "action", "system-search", 16)) {

      public void actionPerformed(ActionEvent e) {
        ArrayList<Channel> channels = new ArrayList<Channel>();
        
        for (Channel channel : Plugin.getPluginManager()
            .getSubscribedChannels()) {
          if (plugin.isSupportedChannel(channel)) {
            channels.add(channel);
          }
        }
        final SearchFormSettings searchSettings = new SearchFormSettings(title);
        Channel[] array = new Channel[channels.size()];
        channels.toArray(array);
        searchSettings.setChannels(array);
        SearchHelper.search(plugin.getFrame(), searchSettings);
      }
    });
  }

}
