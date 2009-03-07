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
package teletextplugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.browserlauncher.Launch;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

final public class TeleTextPlugin extends Plugin {
  private static final Version mVersion = new Version(2, 70, 4);

  private PluginInfo mPluginInfo;

  private ImageIcon mIcon;

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TeleTextPlugin.class);

  private TeleTextChannelProperties mUrlProperties = new TeleTextChannelProperties();

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Teletext");
      final String desc = mLocalizer.msg("description",
          "Shows Internet based teletext pages.");
      mPluginInfo = new PluginInfo(TeleTextPlugin.class, name, desc,
          "Michael Keppler", "GPL 3");
    }

    return mPluginInfo;
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    // special handling of example program
    if (program == null
        || program.equals(getPluginManager().getExampleProgram())
        || getPluginManager().getFilterManager() == null) {
      return new ActionMenu(new ContextMenuAction(mLocalizer.msg("contextMenu",
          "Teletext"), getPluginIcon()));
    }

    final Channel channel = program.getChannel();
    final String url = mUrlProperties.getProperty(channel);
    if (url != null && url.length() > 0) {
      final Action action = new AbstractAction(mLocalizer.msg("contextMenu",
          "Teletext"), getPluginIcon()) {

        public void actionPerformed(final ActionEvent e) {
          Launch.openURL(url);
        }
      };
      return new ActionMenu(action);
    }

    return null;
  }

  private Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("teletext.png"));
    }
    return mIcon;
  }

}
