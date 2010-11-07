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
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import util.browserlauncher.Launch;
import util.ui.UiUtilities;
import devplugin.Channel;

public class WebMediathek {
  private String mTitle;
  private String mUrl;
  private Pattern mPattern;
  private Icon mIcon;

  public WebMediathek(final String title, final String regex, final String url) {
    mTitle = title;
    mPattern = Pattern.compile("(?i)" + regex);
    mUrl = url;
  }

  boolean acceptsChannel(final Channel channel) {
    return mPattern.matcher(channel.getName()).matches();
  }

  void openURL() {
    Launch.openURL(mUrl);
  }

  AbstractAction getAction(boolean showChannelIcon) {
    if (mIcon == null) {
      for (Channel channel : MediathekPlugin.getPluginManager().getSubscribedChannels()) {
        if (acceptsChannel(channel)) {
          mIcon = UiUtilities.createChannelIcon(channel.getIcon());
          break;
        }
      }
    }
    return new AbstractAction(mTitle, showChannelIcon ? mIcon : MediathekPlugin.getInstance().getContextMenuIcon()) {
      public void actionPerformed(final ActionEvent e) {
        openURL();
      }
    };
  }
}
