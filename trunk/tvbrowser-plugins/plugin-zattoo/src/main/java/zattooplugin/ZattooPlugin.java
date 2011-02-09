package zattooplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import util.browserlauncher.Launch;
import util.exc.ErrorHandler;
import util.io.ExecutionHandler;
import util.io.IOUtilities;
import util.misc.OperatingSystem;
import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

public final class ZattooPlugin extends Plugin {

  private static final String ICON_NAME = "zattoo";
  private static final String ICON_CATEGORY = "apps";
  private static final boolean PLUGIN_IS_STABLE = true;
  private static final Version PLUGIN_VERSION = new Version(1, 0, 0, PLUGIN_IS_STABLE);

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ZattooPlugin.class);
  private static final Logger mLog = Logger.getLogger(ZattooPlugin.class.getName());

  private ImageIcon mIcon;
  private static ZattooPlugin mInstance;
  private ZattooSettings mSettings;
  private PluginsProgramFilter mFilters;
  private ZattooChannelProperties mChannelIds;
  private HashSet<Program> mSwitchPrograms = new HashSet<Program>();
  private PluginTreeNode mRootNode;
  private ThemeIcon mThemeIcon;
  private Timer mTimer;
	private ProgramReceiveTarget mProgramReceiveTarget = new ProgramReceiveTarget(this, mLocalizer.msg("receiveTarget", "Show on Zattoo"), "ZATTOO_TARGET");

  /**
   * Creates an instance of this plugin.
   */
  public ZattooPlugin() {
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
      mChannelIds = new ZattooChannelProperties("channels_" + country, mSettings);
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
      mIcon = createImageIcon(ICON_CATEGORY, ICON_NAME, 16);
    }
    return mIcon;
  }

  public ActionMenu getContextMenuActions(final Program program) {
    if (getPluginManager().getExampleProgram().equals(program)) {
    	return getRememberActionMenu(program);
    }
    if (isProgramSupported(program)) {
      return getRememberActionMenu(program);
    }
    return null;
  }

  private ActionMenu getRememberActionMenu(final Program program) {
    final AbstractAction action = new AbstractAction() {
      public void actionPerformed(final ActionEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {

          public void run() {
            mSwitchPrograms.add(program);
            program.mark(ZattooPlugin.this);
            updateRootNode();
          }
        });
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("contextMenuRemember", "Switch channel when program starts"));
    action.putValue(Action.SMALL_ICON, getPluginIcon());
    return new ActionMenu(action);
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
    action.putValue(Action.NAME, mLocalizer.msg("contextMenuSwitch", "Switch channel"));
    action.putValue(Action.SMALL_ICON, getPluginIcon());
    return new ActionMenu(action);
  }

  private void openChannel(final Channel channel) {
    final String id = getChannelId(channel);
    if (id == null) {
      return;
    }

    String url = "http://zattoo.com/view/" + id;
    ExecutionHandler executionHandler = null;
    if (mSettings.getUseWebPlayer()) {
      Launch.openURL(url);
    } else if (mSettings.getUsePrismPlayer()) {
      executionHandler = new ExecutionHandler("-uri " + url + " -name tvbrowser-zattoo", "prism");
    } else {
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
        // TODO: use getOutput() after 3.0
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
    if (!mSettings.getUseLocalPlayer() && comma >= 0) {
      return id.substring(comma + 1).trim();
    } else if (!mSettings.getUseLocalPlayer() && comma == -1) {
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
          return isProgramSupported(program);
        }
      };
    }
    return new PluginsProgramFilter[] { mFilters };
  }

  @Override
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return new Class[] { ZattooFilterComponent.class };
  }

  public boolean isChannelSupported(final Channel channel) {
    return getChannelId(channel) != null;
  }

  public static boolean canUseLocalPlayer() {
    return OperatingSystem.isMacOs() || OperatingSystem.isWindows();
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    if (mRootNode == null) {
      mRootNode = new PluginTreeNode(this);
      mRootNode.getMutableTreeNode().setIcon(getPluginIcon());
    }
    return mRootNode;
  }

  private void updateRootNode() {
    getRootNode().clear();
    for (Program program : mSwitchPrograms) {
      mRootNode.addProgramWithoutCheck(program);
    }
    mRootNode.update();
  }

  @Override
  public ThemeIcon getMarkIconFromTheme() {
    if (mThemeIcon == null) {
      mThemeIcon = new ThemeIcon(ICON_CATEGORY, ICON_NAME, 16);
    }
    return mThemeIcon;
  }

  @Override
  public void handleTvBrowserStartFinished() {
    mTimer = new Timer(60 * 1000, new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        Date today = new Date();
        int now = IOUtilities.getMinutesAfterMidnight();
        int delay = 2;
        Program startProgram = null;
        synchronized (mSwitchPrograms) {
          for (Program program : mSwitchPrograms) {
            if (program.getDate().compareTo(today) == 0) {
              int startTime = program.getStartTime();
              if (now >= startTime - delay) {
                startProgram = program;
                break;
              }
            }
          }
          // starts only 1 program per invocation. multiple calls to zattoo not supported
          if (startProgram != null) {
            mSwitchPrograms.remove(startProgram);
            startProgram.unmark(ZattooPlugin.this);
            updateRootNode();
            openChannel(startProgram.getChannel());
          }
        }
      }
    });
    mTimer.start();
  }

  @Override
  public boolean canReceiveProgramsWithTarget() {
  	return true;
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
  	return new ProgramReceiveTarget[] {mProgramReceiveTarget };
  }

  @Override
  public boolean receivePrograms(Program[] programArr,
  		ProgramReceiveTarget receiveTarget) {
  	for (Program program : programArr) {
			if (isProgramSupported(program)) {
        mSwitchPrograms.add(program);
        program.mark(ZattooPlugin.this);
			}
		}
    updateRootNode();
    return true;
  }

	private boolean isProgramSupported(Program program) {
		return isChannelSupported(program.getChannel()) && !program.isExpired() && !program.isOnAir();
	}
}
