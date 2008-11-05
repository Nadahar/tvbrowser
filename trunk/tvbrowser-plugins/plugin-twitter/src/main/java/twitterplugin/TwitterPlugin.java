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
import java.util.Properties;
import java.awt.event.ActionEvent;

public class TwitterPlugin extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterPlugin.class);
  private Properties mSettings;
  private ImageIcon mIcon;
  protected static TwitterPlugin mInstance;

  public static Version getVersion() {
    return new Version(0, 1, false);
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

  private Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("twitter.png"));
    }
    return mIcon;
  }

  public SettingsTab getSettingsTab() {
    return null; //new TwitterPlugin(mSettings);
  }

  public ActionMenu getContextMenuActions(Program program) {
      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
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
}