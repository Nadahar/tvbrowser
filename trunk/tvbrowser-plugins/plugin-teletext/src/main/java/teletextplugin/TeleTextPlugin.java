/*
 * TeleTextPlugin by Michael Keppler
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
 * VCS information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package teletextplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

public class TeleTextPlugin extends Plugin {
  private static final Version mVersion = new Version(2, 70);

  private PluginInfo mPluginInfo;

  private Properties mPages;

  private ImageIcon mIcon;

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TeleTextPlugin.class);

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      String name = mLocalizer.msg("name", "Teletext");
      String desc = mLocalizer.msg("description",
          "Shows Internet based teletext pages.");
      String author = "Michael Keppler";

      mPluginInfo = new PluginInfo(TeleTextPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  @Override
  public ActionMenu getContextMenuActions(Program program) {
    // special handling of example program
    if (program == null
        || program.equals(getPluginManager().getExampleProgram())
        || getPluginManager().getFilterManager() == null) {
      return new ActionMenu(new ContextMenuAction(mLocalizer.msg("contextMenu",
          "Teletext"), getPluginIcon()));
    }

    Channel channel = program.getChannel();
    final String url = getTextUrl(channel);
    if (url != null && url.length() > 0) {
      Action action = new AbstractAction(mLocalizer.msg("contextMenu",
          "Teletext"), getPluginIcon()) {

        public void actionPerformed(ActionEvent e) {
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

  private String getTextUrl(Channel channel) {
    initializeProperties();
    String url = mPages.getProperty(channel.getId().toLowerCase().replaceAll(
        " ", "_"));
    if (url != null && url.length() > 0) {
      // is this a mapping only?
      if (!url.startsWith("http")) {
        url = mPages.getProperty(url);
      }
      if (url != null && url.length() > 0 && url.startsWith("http")) {
        return url;
      }
    }
    return null;
  }

  private void initializeProperties() {
    if (mPages == null) {
      InputStream is = getClass().getResourceAsStream("teletext.properties");
      mPages = new Properties();
      try {
        mPages.load(is);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }


}
