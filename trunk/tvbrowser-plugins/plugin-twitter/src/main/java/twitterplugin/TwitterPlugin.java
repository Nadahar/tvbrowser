package twitterplugin;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;
import devplugin.ActionMenu;
import devplugin.Program;
import util.ui.Localizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import java.util.Properties;
import java.awt.event.ActionEvent;
import java.awt.Frame;

public class TwitterPlugin extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterPlugin.class);
  public static final String DEFAULT_FORMAT = "{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}. {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")} {channel_name} - {title}";

  public static final String STORE_PASSWORD = "STOREPASSWORD";
  public static final String USERNAME = "USERNAME";
  public static final String PASSWORD = "PASSWORD";
  protected static final String FORMAT = "paramForProgram";

  private Properties mSettings;
  private ImageIcon mIcon;
  protected static TwitterPlugin mInstance;

  public static Version getVersion() {
    return new Version(0, 3, false);
  }

  /**
   * Creates an instance of this plugin.
   */
  public TwitterPlugin() {
    mInstance = this;
  }

  public PluginInfo getInfo() {
    return new PluginInfo(TwitterPlugin.class, mLocalizer.msg("pluginName", "Twitter Plugin"),
        mLocalizer.msg("description", "Creates twitter tweets.\nThis Plugin uses Twitter4J, Copyright (c) 2007-2008, Yusuke Yamamoto"), 
        "Bodo Tasche", "GPL");
  }

  public Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("twitter.png"));
    }
    return mIcon;
  }

  public SettingsTab getSettingsTab() {
    return new TwitterSettingsTab();
  }

  public ActionMenu getContextMenuActions(final Program program) {
      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              new TwitterSender().send((JFrame)getParentFrame(), program);
            }
          });
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("contextMenuTweet", "Tweet this"));
      action.putValue(Action.SMALL_ICON, getPluginIcon());
      return new ActionMenu(action);
  }

  public void loadSettings(Properties settings) {
    if (settings == null) {
      mSettings = new Properties();
    } else {
      mSettings = settings;
    }
  }

  public Properties storeSettings() {
    return mSettings;
  }

  public Properties getSettings() {
    return mSettings;
  }

  public static TwitterPlugin getInstance() {
    return mInstance;
  }

}
