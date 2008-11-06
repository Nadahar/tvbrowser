package twitterplugin;

import devplugin.Program;

import java.awt.Frame;

import twitter4j.Twitter;
import twitter4j.Status;
import twitter4j.TwitterException;
import util.exc.ErrorHandler;
import util.ui.Localizer;

public class TwitterSender {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterSender.class);

  public TwitterSender() {
  }

  public void send(Frame parentFrame, Program program) {
    TwitterDialog dialog = new TwitterDialog(parentFrame, program);
    dialog.setVisible(true);

    if (dialog.wasOkPressed()) {
      // ToDo: Login and sending of Message
      final Twitter twitter = new Twitter("twitterId","twitterPassword");
      try {
        twitter.update(dialog.getMessage());
      } catch (TwitterException e) {
        e.printStackTrace();
        ErrorHandler.handle(mLocalizer.msg("error", "Could not send tweet..."), e);
      }
    }
  }
}
