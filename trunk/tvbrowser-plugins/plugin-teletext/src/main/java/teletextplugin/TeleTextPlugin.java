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
import java.util.Enumeration;
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

final public class TeleTextPlugin extends Plugin {
  private static final Version mVersion = new Version(2, 70, 4);

  private PluginInfo mPluginInfo;

  private Properties mPages;

  private ImageIcon mIcon;

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TeleTextPlugin.class);

  private static java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(TeleTextPlugin.class.getName());

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "Teletext");
      final String desc = mLocalizer.msg("description",
          "Shows Internet based teletext pages.");
      final String author = "Michael Keppler";

      mPluginInfo = new PluginInfo(TeleTextPlugin.class, name, desc, author);
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
    final String url = getTextUrl(channel);
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

  private String getTextUrl(final Channel channel) {
    try {
      initializeProperties();
      final String channelCountry = channel.getCountry();
      final String channelName = channel.getDefaultName();
      for (final Enumeration<?> keys = mPages.propertyNames(); keys
          .hasMoreElements();) {
        final String key = (String) keys.nextElement();
        final int separatorIndex = key.indexOf(',');
        if (separatorIndex > 0) {
          final String[] countries = key.substring(0, separatorIndex).split(
              "\\W");
          final String name = "(?i)"
              + key.substring(separatorIndex + 1).replaceAll("\\*", ".*");
          for (String country : countries) {
            if (channelCountry.equalsIgnoreCase(country)
                && channelName.matches(name)) {
              String url = mPages.getProperty(key);
              if (isValidURL(url)) {
                return url;
              }
              else {
                url = mPages.getProperty(url);
                if (isValidURL(url)) {
                  return url;
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    // nothing found
    return null;
  }

  private boolean isValidURL(final String url) {
    return url != null && url.length() > 0 && url.startsWith("http");
  }

  private void initializeProperties() {
    // load the URLs
    if (mPages == null) {
      final InputStream urlStream = getClass().getResourceAsStream(
          "teletext.properties");
      mPages = new Properties();
      try {
        mPages.load(urlStream);
      } catch (IOException e) {
        e.printStackTrace();
      }
      // check all URLs on first load
      checkURLs();
    }
  }

  private void checkURLs() {
    for (final Enumeration<?> keys = mPages.propertyNames(); keys
        .hasMoreElements();) {
      final String key = (String) keys.nextElement();
      String url = mPages.getProperty(key);
      if (url != null && url.length() > 0) {
        // is this a mapping only?
        if (!url.startsWith("http")) {
          url = mPages.getProperty(url);
          if (url != null) {
            mPages.put(key, url);
          }
          else {
            mLog.warning("Bad teletext mapping for " + key);
          }
        }
        if (url == null || url.length() == 0 || !url.startsWith("http")) {
          mLog.warning("Bad teletext URL " + url);
        }
      } else {
        mLog.warning("Bad teletext key " + key + "=" + url);
      }
    }
  }


}
