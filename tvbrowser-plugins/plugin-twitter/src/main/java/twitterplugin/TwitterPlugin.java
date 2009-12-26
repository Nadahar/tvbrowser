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

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import util.ui.DontShowAgainMessageBox;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;

public final class TwitterPlugin extends Plugin {
  private static final boolean PLUGIN_IS_STABLE = false;
	private static final Version PLUGIN_VERSION = new Version(0, 5, 1, PLUGIN_IS_STABLE);
	
	private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterPlugin.class);
  private static final String TWITTER_TARGET = "TWITTER_TARGET";
  /**
   * maximum number of programs which can be used with Twitter export target.
   * This avoids presenting a dozen dialogs for entering tweets.
   */
  private static final int MAX_PROGRAM_COUNT = 3;
  private TwitterSettings mSettings;
  private ImageIcon mIcon;
  private static TwitterPlugin mInstance;

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }

  /**
   * Creates an instance of this plugin.
   */
  public TwitterPlugin() {
    mInstance = this;
  }

  public PluginInfo getInfo() {
    return new PluginInfo(TwitterPlugin.class, mLocalizer.msg("pluginName", "Twitter Plugin"),
        mLocalizer.msg("description",
            "Creates twitter tweets.\nThis Plugin uses Twitter4J, Copyright (c) Yusuke Yamamoto"),
        "Bodo Tasche", "GPL3");
  }

  public Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("twitter.png"));
    }
    return mIcon;
  }

  public SettingsTab getSettingsTab() {
    return new TwitterSettingsTab(mSettings);
  }

  public ActionMenu getContextMenuActions(final Program program) {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            twitter(program);
          }
        });
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("contextMenuTweet", "Tweet this"));
    action.putValue(Action.SMALL_ICON, getPluginIcon());
    return new ActionMenu(action);
  }

  public void loadSettings(final Properties properties) {
    mSettings = new TwitterSettings(properties);
  }

  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  public static TwitterPlugin getInstance() {
    return mInstance;
  }
  
  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }
  
  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] {getTwitterTarget()};
  }

  private ProgramReceiveTarget getTwitterTarget() {
    return new ProgramReceiveTarget(this, mLocalizer.msg("targetTwitter", "Twitter"), TWITTER_TARGET);
  }
  
  @Override
  public boolean receivePrograms(final Program[] programArr, final ProgramReceiveTarget receiveTarget) {
    // if lots of programs are sent, this would lead to showing many dialogs and sending many tweets, so set an upper limit
    if (programArr.length > MAX_PROGRAM_COUNT) {
      DontShowAgainMessageBox.dontShowAgainMessageBox(this, "toManyProgramsToTweet", getParentFrame(), mLocalizer.msg(
          "toManyPrograms", "Please select at most {0} programs to be tweeted.", MAX_PROGRAM_COUNT));
      return false;
    }
    for (Program program : programArr) {
      twitter(program);
    }
    return true;
  }

  private void twitter(final Program program) {
    new TwitterSender().send(UiUtilities.getLastModalChildOf(getParentFrame()), program, mSettings);
  }
}
