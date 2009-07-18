package zattooplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import util.browserlauncher.Launch;
import util.exc.ErrorHandler;
import util.io.ExecutionHandler;
import util.misc.OperatingSystem;
import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

public final class ZattooPlugin extends Plugin {

  private static final boolean PLUGIN_IS_STABLE = true;
  private static final Version PLUGIN_VERSION = new Version(0, 5, PLUGIN_IS_STABLE);

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ZattooPlugin.class);
  private static Logger mLog = Logger.getLogger(ZattooPlugin.class.getName());

  private ImageIcon mIcon;
  private static ZattooPlugin mInstance;
  private ZattooSettings mSettings;
  private PluginsProgramFilter mFilters;
  private ZattooChannelProperties mChannelIds;

  /**
   * Creates an instance of this plugin.
   */
  public ZattooPlugin() throws IOException {
    mInstance = this;
  }

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }

  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  @Override
  public void loadSettings(final Properties properties) {
    mSettings = new ZattooSettings(properties);

    changeCountry(mSettings.getCountry());
  }

  public void changeCountry(final String country) {
    try {
      mChannelIds = new ZattooChannelProperties("channels_" + country);
      mSettings.setCountry(country);
    } catch (Exception e) {
      mLog.log(Level.WARNING, "Could not load File for Country " + country + ".", e);
    }
  }

  public PluginInfo getInfo() {
    return new PluginInfo(ZattooPlugin.class, mLocalizer.msg("pluginName", "Zattoo"), mLocalizer.msg("description",
        "Switches channels in Zattoo"), "Bodo Tasche, Michael Keppler", "GPL");
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new ZattooSettingsTab(mSettings);
  }

  public Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("zattoo.png"));
    }
    return mIcon;
  }

  public ActionMenu getContextMenuActions(final Program program) {
    if (getPluginManager().getExampleProgram().equals(program) || isChannelSupported(program.getChannel())) {
      return getSwitchActionMenu(program.getChannel());
    }
    return null;
  }

  public ActionMenu getContextMenuActions(final Channel channel) {
    if (channel != null && isChannelSupported(channel)) {
      return getSwitchActionMenu(channel);
    }
    return null;
  }

  private ActionMenu getSwitchActionMenu(final Channel channel) {
    final AbstractAction action = new AbstractAction() {
      public void actionPerformed(final ActionEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            openChannel(channel);
          }
        });
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("contextMenuZattoo", "Switch Channel"));
    action.putValue(Action.SMALL_ICON, getPluginIcon());
    return new ActionMenu(action);
  }

  private void openChannel(final Channel channel) {
    final String id = getChannelId(channel);
    if (id == null) {
      return;
    }

    String url = "https://watch.zattoo.com/view/" + id;
    ExecutionHandler executionHandler = null;
    if (mSettings.getUseWebPlayer()) {
      Launch.openURL(url);
    }
    else if (mSettings.getPrismPlayer()) {
      executionHandler = new ExecutionHandler("-uri " + url + " -name tvbrowser-zattoo", "prism");
    }
    else {
      final String zattooURI = "zattoo://channel/" + id;
      if (OperatingSystem.isLinux()) {
        executionHandler = new ExecutionHandler(zattooURI, "zattoo-uri-handler");
      } else if (OperatingSystem.isMacOs()) {
        executionHandler = new ExecutionHandler(zattooURI, "open");
      } else {
        executionHandler = new ExecutionHandler(
            new String[] { "rundll32.exe", "url.dll,FileProtocolHandler", zattooURI });
      }
    }
    if (executionHandler != null) {
      try {
        executionHandler.execute(false);
      } catch (IOException e) {
        e.printStackTrace();
        ErrorHandler.handle(mLocalizer.msg("error.zatto", "Could not start zattoo"), e);
      }
    }
  }

  private String getChannelId(final Channel channel) {
    final String id = mChannelIds.getProperty(channel);
    if (id == null) {
      mLog.log(Level.INFO, "No zattoo channel mapping found for " + channel.getUniqueId());
      return null;
    }
    int comma = id.indexOf(',');
    if (mSettings.getUseWebPlayer() && comma >= 0) {
      return id.substring(comma + 1).trim();
    } else if (mSettings.getUseWebPlayer() && comma == -1) {
      return null;
    } else if (comma >= 0) {
      return id.substring(0, comma);
    }
    return id;
  }

  public static ZattooPlugin getInstance() {
    return mInstance;
  }

  @Override
  public PluginsProgramFilter[] getAvailableFilter() {
    if (mFilters == null) {
      mFilters = new PluginsProgramFilter(this) {

        @Override
        public String getSubName() {
          return mLocalizer.msg("supportedChannels", "Supported channels");
        }

        public boolean accept(final Program program) {
          return isChannelSupported(program.getChannel());
        }
      };
    }
    return new PluginsProgramFilter[] { mFilters };
  }

  @Override
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return (Class<? extends PluginsFilterComponent>[]) new Class[] { ZattooFilterComponent.class };
  }

  public boolean isChannelSupported(final Channel channel) {
    return getChannelId(channel) != null;
  }

  public static boolean canUseLocalPlayer() {
    return OperatingSystem.isMacOs() || OperatingSystem.isWindows();
  }

}
