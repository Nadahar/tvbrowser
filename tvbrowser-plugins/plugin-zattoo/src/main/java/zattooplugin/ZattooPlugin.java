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
import java.util.Properties;

public class ZattooPlugin extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ZattooPlugin.class);

  private ImageIcon mIcon;
  protected static ZattooPlugin mInstance;
  private Properties mChannelMapping;

  public static Version getVersion() {
    return new Version(0, 1, false);
  }

  /**
   * Creates an instance of this plugin.
   */
  public ZattooPlugin() throws IOException {
    mInstance = this;
    mChannelMapping = new Properties();
    mChannelMapping.load(ZattooPlugin.class.getResourceAsStream("channelid.properties"));
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
    if (getChannelId(program.getChannel()) != null) {
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
    System.out.println("Channel : " + channel.getUniqueId() + " Mapping : " + mChannelMapping.getProperty(channel.getUniqueId()));
    return mChannelMapping.getProperty(channel.getUniqueId());
  }

  public static ZattooPlugin getInstance() {
    return mInstance;
  }

}

