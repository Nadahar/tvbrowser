package zattooplugin;

import devplugin.Plugin;
import devplugin.Version;
import devplugin.PluginInfo;
import devplugin.ActionMenu;
import devplugin.Program;
import devplugin.Channel;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.misc.OperatingSystem;
import util.io.ExecutionHandler;
import util.exc.ErrorHandler;

import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ZattooPlugin extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ZattooPlugin.class);
  private static Logger mLog = Logger.getLogger(ZattooPlugin.class.getName());

  private static final String COUNTRY = "COUNTRY";

  private ImageIcon mIcon;
  protected static ZattooPlugin mInstance;
  private Properties mChannelMapping;
  private Properties mSettings;

  /**
   * Creates an instance of this plugin.
   */
  public ZattooPlugin() throws IOException {
    mInstance = this;
    mChannelMapping = new Properties();
  }

  public static Version getVersion() {
    return new Version(0, 1, false);
  }

  @Override
  public Properties storeSettings() {
    return mSettings;
  }

  @Override
  public void loadSettings(Properties properties) {
    mSettings = properties;

    changeCountry(mSettings.getProperty(COUNTRY, "de"));
  }

  private void changeCountry(String country) {
    mChannelMapping = new Properties();

    final InputStream stream = ZattooPlugin.class.getResourceAsStream("channelid_" + country + ".properties");
    if (stream != null) {
      try {
        mChannelMapping.load(stream);
      } catch (IOException e) {
        mLog.log(Level.WARNING, "Could not load File for Country " + country + ".", e);
      }
    } else {
      mLog.log(Level.WARNING, "Could not find File for Country " + country + ".");
    }
  }

  public PluginInfo getInfo() {
    return new PluginInfo(ZattooPlugin.class, mLocalizer.msg("pluginName", "Zattoo Plugin"),
        mLocalizer.msg("description", "Switches channels in Zattoo"),
        "Bodo Tasche", "GPL");
  }

  public Icon getPluginIcon() {
    if (mIcon == null) {
      //mIcon = new ImageIcon(getClass().getResource("zattoo.png"));
    }
    return mIcon;
  }

  public ActionMenu getContextMenuActions(final Program program) {
    if (getPluginManager().getExampleProgram().equals(program) || getChannelId(program.getChannel()) != null) {
      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              openChannel(program.getChannel());
            }
          });
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("contextMenuTweet", "Switch Channel"));
      action.putValue(Action.SMALL_ICON, getPluginIcon());
      return new ActionMenu(action);
    }
    return null;
  }

  private void openChannel(final Channel channel) {
    final String id = getChannelId(channel);
    if (id != null && !OperatingSystem.isOther()) {
      ExecutionHandler executionHandler;
      if (OperatingSystem.isLinux()) {
        executionHandler = new ExecutionHandler("zattoo://channel/" + id, "zattoo-uri-handler");
      } else if (OperatingSystem.isMacOs()) {
        executionHandler = new ExecutionHandler("zattoo://channel/" + id, "open");
      } else {
        executionHandler = new ExecutionHandler("zattoo://channel/" + id, "start");
      }
      
      try {
        executionHandler.execute(false);
      } catch (IOException e) {
        e.printStackTrace();
        ErrorHandler.handle(mLocalizer.msg("error.zatto", "Could not start zattoo"), e);
      }
    }
  }

  private String getChannelId(final Channel channel) {
    final String ret = mChannelMapping.getProperty(channel.getUniqueId());
    if (ret == null) {
      mLog.log(Level.INFO, "No channelmapping found for " + channel.getUniqueId());
    }
    return ret;
  }

  public static ZattooPlugin getInstance() {
    return mInstance;
  }

}

