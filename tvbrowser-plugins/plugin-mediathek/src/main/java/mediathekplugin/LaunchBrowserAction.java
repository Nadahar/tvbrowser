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

import javax.swing.AbstractAction;

import util.browserlauncher.Launch;
import devplugin.Plugin;

public class LaunchBrowserAction extends AbstractAction {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(LaunchBrowserAction.class);
  private final String url;

  LaunchBrowserAction(String url) {
    this(url, mLocalizer.msg("openMedia", "Open in browser"));
  }

  public LaunchBrowserAction(String url, String title) {
    super(title, Plugin
        .getPluginManager().getIconFromTheme(MediathekPlugin.getInstance(),
            "apps", "internet-web-browser", 16));
    this.url = url;
  }

  public void actionPerformed(ActionEvent e) {
    Launch.openURL(url);
  }
}