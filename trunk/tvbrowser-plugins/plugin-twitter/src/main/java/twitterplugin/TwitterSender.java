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
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import util.exc.ErrorHandler;
import util.ui.DontShowAgainMessageBox;
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

      if (settings.getAccessToken() == null) {
        final TwitterLoginDialog login = new TwitterLoginDialog(parentWindow, settings);

        if (!(login.askLogin() == JOptionPane.OK_OPTION)) {
          return;
        }
      }

      ConfigurationBuilder cb = new ConfigurationBuilder();
      cb.setDebugEnabled(true)
        .setOAuthConsumerKey(settings.getConsumerKey())
        .setOAuthConsumerSecret(settings.getConsumerSecret());
      TwitterFactory factory = new TwitterFactory(cb.build());
      AccessToken accessToken = settings.getAccessToken();
      Twitter twitter = factory.getInstance(accessToken);
      try {
        twitter.updateStatus(dialog.getMessage());
        DontShowAgainMessageBox.dontShowAgainMessageBox(TwitterPlugin.getInstance(), "tweetSent", parentWindow, mLocalizer.msg("tweetSend", "The tweet was sent."));
      } catch (TwitterException e) {
        e.printStackTrace();
        ErrorHandler.handle(mLocalizer.msg("error", "Could not send tweet..."), e);
      }
    }
  }

}
