/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
  public boolean contains(final Program prog, final boolean recursive) {
    return false;
  }

  @Override
  public boolean contains(final Program prog) {
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

  public MediathekProgramNode(final MediathekProgram mediathekProgram) {
    super(mediathekProgram.getTitle());
    setGroupingByDateEnabled(false);
    getMutableTreeNode().setShowLeafCountEnabled(false);
    final MediathekPlugin plugin = MediathekPlugin.getInstance();
    addAction(new AbstractAction(mLocalizer.msg("action.search",
        "Search programs"), Plugin.getPluginManager().getIconFromTheme(plugin,
        "action", "system-search", 16)) {

      public void actionPerformed(final ActionEvent e) {
        final ArrayList<Channel> channels = new ArrayList<Channel>();

        for (Channel channel : Plugin.getPluginManager()
            .getSubscribedChannels()) {
          if (plugin.isSupportedChannel(channel)) {
            channels.add(channel);
          }
        }
        String title = mediathekProgram.getTitle();
        if (title.endsWith("...")) {
          title = title.substring(0, title.length() - 3).trim();
        }
        final SearchFormSettings searchSettings = new SearchFormSettings(title);
        final Channel[] array = new Channel[channels.size()];
        channels.toArray(array);
        searchSettings.setChannels(array);
        searchSettings.setCaseSensitive(false);
        SearchHelper.search(plugin.getFrame(), searchSettings);
      }
    });
    addAction(new LaunchBrowserAction(mediathekProgram.getUrl()));
  }

}
