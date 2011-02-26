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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

public final class TwitterLoginDialog extends JDialog implements WindowClosingIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterLoginDialog.class);

  /** Which Button was pressed ? */
  private int mReturnValue = JOptionPane.CANCEL_OPTION;

  private JTextField mPIN;

  private TwitterSettings mSettings;

  private RequestToken mRequestToken;

  private String mAuthorizationUrl;

  private JButton mUrlButton;

  private JLabel mLabelPin;

  private JLabel mLabelBrowser;

  /**
   * Create Dialog
   *
   * @param owner
   *          Parent-Frame
   * @param username
   *          Username
   * @param mLabelPassword
   *          Password
   * @param storePassword
   *          store password ?
   */
  public TwitterLoginDialog(final Window owner, final TwitterSettings settings) {
    super(owner);
    setModal(true);
    mSettings = settings;
    createGui();
  }

  /**
   * Create Gui
   */
  private void createGui() {
    setTitle(mLocalizer.msg("login", "Login"));

    UiUtilities.registerForClosing(this);

    PanelBuilder content = new PanelBuilder(new FormLayout(
        "5dlu, pref:grow(0.5), 3dlu, 100dlu, fill:pref:grow(0.5), 5dlu"));

    CellConstraints cc = new CellConstraints();

    final Twitter twitter = new TwitterFactory().getInstance();
    twitter.setOAuthConsumer(mSettings.getConsumerKey(), mSettings.getConsumerSecret());
    try {
      mRequestToken = twitter.getOAuthRequestToken();
      mAuthorizationUrl = mRequestToken.getAuthorizationURL();
    } catch (TwitterException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    content.appendRow("3dlu");
    content.appendRow("pref");
    mLabelBrowser = new JLabel("1. " + mLocalizer.msg("step1", "Open authentication page on Twitter"));
    content.add(mLabelBrowser, cc.xy(2, content.getRowCount()));
    mUrlButton = new JButton(mLocalizer.msg("openBrowser", "Open browser"));
    mUrlButton.setToolTipText(mAuthorizationUrl);
    content.add(mUrlButton, cc.xy(4, content.getRowCount()));
    mUrlButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        Launch.openURL(mAuthorizationUrl);
      }
    });

    content.appendRow("3dlu");
    content.appendRow("pref");
    mLabelPin = new JLabel("2. " + mLocalizer.msg("step2", "Enter PIN from web page"));
    content.add(mLabelPin, cc.xy(2, content.getRowCount()));
    mPIN = new JTextField();
    content.add(mPIN, cc.xy(4, content.getRowCount()));

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mReturnValue = JOptionPane.OK_OPTION;
        AccessToken accessToken = null;
        try {
          String pin = mPIN.getText().trim();
          if (pin.length() > 0) {
            accessToken = twitter.getOAuthAccessToken(mRequestToken, pin);
          } else {
            accessToken = twitter.getOAuthAccessToken();
          }
        } catch (TwitterException te) {
          if (401 == te.getStatusCode()) {
            System.out.println("Unable to get the access token.");
          } else {
            te.printStackTrace();
          }
        }
        try {
          twitter.verifyCredentials().getId();
          mSettings.setAccessToken(accessToken);
        } catch (TwitterException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        setVisible(false);
      }
    });

    getRootPane().setDefaultButton(ok);

    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    builder.addGriddedButtons(new JButton[] { ok, cancel });

    content.appendRow("fill:pref:grow");
    content.appendRow("pref");
    content.add(builder.getPanel(), cc.xyw(1, content.getRowCount(), 4));

    content.appendRow("5dlu");
    content.setBorder(Borders.DIALOG_BORDER);
    getContentPane().add(content.getPanel());

    UiUtilities.setSize(this, Sizes.dialogUnitXAsPixel(200, this), Sizes.dialogUnitYAsPixel(140, this));
  }

  /**
   * Dialog was closed
   */
  public void close() {
    mReturnValue = JOptionPane.CANCEL_OPTION;
    setVisible(false);
  }

  /**
   * Show the Dialog
   *
   * @return Which Button was pressed ? (JOptionpane.OK_OPTION / CANCEL_OPTION)
   */
  public int askLogin() {
    UiUtilities.centerAndShow(this);
    return mReturnValue;
  }

}
