package twitterplugin;

import devplugin.Program;

import java.awt.Frame;
import java.util.Properties;

import twitter4j.Twitter;
import twitter4j.Status;
import twitter4j.TwitterException;
import util.exc.ErrorHandler;
import util.ui.Localizer;
import util.io.IOUtilities;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class TwitterSender {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterSender.class);

  public TwitterSender() {
  }

  public void send(JFrame parentFrame, Program program) {
    TwitterDialog dialog = new TwitterDialog(parentFrame, program);
    dialog.setLocationRelativeTo(parentFrame);
    dialog.setVisible(true);

    if (dialog.wasOkPressed()) {
      Properties settings = TwitterPlugin.getInstance().getSettings();

      String username = settings.getProperty(TwitterPlugin.USERNAME, "");
      String password;
      if ("false".equalsIgnoreCase(settings.getProperty(TwitterPlugin.STORE_PASSWORD, "false"))) {
        final TwitterLoginDialog login = new TwitterLoginDialog(parentFrame,
            username,
            "", false);
        if (!(login.askLogin() == JOptionPane.OK_OPTION)) {
          return;
        }
        
        username = login.getUsername();
        password = login.getPassword().trim();
        settings.setProperty(TwitterPlugin.USERNAME, username);
        if (login.storePasswords()) {
          settings.setProperty(TwitterPlugin.PASSWORD, IOUtilities.xorEncode(password, 54673578));
          settings.setProperty(TwitterPlugin.STORE_PASSWORD, "true");
        }
      } else {
        password = IOUtilities.xorDecode(settings.getProperty(TwitterPlugin.PASSWORD, ""), 54673578);
      }

      final Twitter twitter = new Twitter(username, password);
      try {
        twitter.setSource("TV-Browser");
        twitter.update(dialog.getMessage());
        JOptionPane.showMessageDialog(parentFrame, mLocalizer.msg("tweetSend", "The tweet was send."));
      } catch (TwitterException e) {
        e.printStackTrace();
        ErrorHandler.handle(mLocalizer.msg("error", "Could not send tweet..."), e);
      }
    }
  }
}
