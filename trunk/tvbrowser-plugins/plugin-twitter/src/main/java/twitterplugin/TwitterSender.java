/*
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
package twitterplugin;

import java.awt.Window;

import javax.swing.JOptionPane;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import util.exc.ErrorHandler;
import util.io.IOUtilities;
import util.ui.Localizer;
import devplugin.Program;

public class TwitterSender {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterSender.class);

  public TwitterSender() {
  }

  public void send(final Window parentWindow, final Program program, final TwitterSettings settings) {
    TwitterDialog dialog = new TwitterDialog(parentWindow, program, settings.getFormat());
    dialog.setLocationRelativeTo(parentWindow);
    dialog.setVisible(true);

    if (dialog.wasOkPressed()) {

      if ((settings.getUseOAuth() && settings.getAccessToken() == null)
          || (!settings.getUseOAuth() && !settings.getStorePassword())) {
        final TwitterLoginDialog login = new TwitterLoginDialog(parentWindow, settings);

        if (!(login.askLogin() == JOptionPane.OK_OPTION)) {
          return;
        }
      }
      final Twitter twitter;
      TwitterFactory factory = new TwitterFactory();
      if (settings.getUseOAuth()) {
        twitter = factory.getInstance();
        twitter.setOAuthConsumer(settings.getConsumerKey(), settings.getConsumerSecret());
        AccessToken accessToken = settings.getAccessToken();
        twitter.setOAuthAccessToken(accessToken);
      } else {
        String username = settings.getUsername();
        String password = IOUtilities.xorDecode(settings.getPassword(), 54673578);
        twitter = factory.getInstance(username, password);
      }
      try {
        twitter.updateStatus(dialog.getMessage());
        JOptionPane.showMessageDialog(parentWindow, mLocalizer.msg("tweetSend", "The tweet was sent."));
      } catch (TwitterException e) {
        e.printStackTrace();
        ErrorHandler.handle(mLocalizer.msg("error", "Could not send tweet..."), e);
      }
    }
  }

}
