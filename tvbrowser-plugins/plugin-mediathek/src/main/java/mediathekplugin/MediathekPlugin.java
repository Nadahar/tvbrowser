/*
 * Copyright Michael Keppler
 *
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
package mediathekplugin;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import util.browserlauncher.Launch;
import util.io.IOUtilities;
import util.ui.html.HTMLTextHelper;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * @author Bananeweizen
 *
 */
public class MediathekPlugin extends Plugin {

  private static final boolean IS_STABLE = false;

  private static final Version PLUGIN_VERSION = new Version(2, 80, 1, IS_STABLE);

  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(MediathekPlugin.class);

  private Icon markIcon, contextIcon, pluginIconSmall, pluginIconLarge, mIconWeb;

  private MediathekSettings mSettings;

  private PluginTreeNode rootNode = new PluginTreeNode(this, false);

  private static MediathekPlugin instance = null;

  /** The logger for this class */
  private static final Logger logger = Logger.getLogger(MediathekPlugin.class.getName());

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }

  private static final Icon[] EMPTY_ICON_LIST = {};

  private ArrayList<WebMediathek> mMediatheks;

  private Database mDatabase;

  @Override
  public PluginInfo getInfo() {
    final String name = mLocalizer.msg("name", "Mediathek");
    final String description = mLocalizer.msg("description", "Shows video information for several mediatheks.");
    return new PluginInfo(MediathekPlugin.class, name, description, "Michael Keppler", "GPL 3");
  }

  public MediathekPlugin() {
    rememberInstance(this);
    pluginIconSmall = createImageIcon("actions", "web-search", 16);
    pluginIconLarge = createImageIcon("actions", "web-search", 22);
    contextIcon = createImageIcon("actions", "web-search", 16);
    markIcon = contextIcon;
    mIconWeb = createImageIcon("apps", "internet-web-browser", 16);
    rootNode.setGroupingByDateEnabled(false);
  }

  private static void rememberInstance(final MediathekPlugin plugin) {
    instance = plugin;
  }

  @Override
  public ActionMenu getContextMenuActions(final Program program) {
    // pseudo action for example program
    if (program.equals(getPluginManager().getExampleProgram())) {
      return new ActionMenu(new AbstractAction(mLocalizer.msg("name", "Mediathek"), getContextMenuIcon()) {
        public void actionPerformed(final ActionEvent e) {
          // empty
        }
      });
    }
    if (mDatabase != null) {
      ArrayList<MediathekProgramItem> programs = mDatabase.getMediathekPrograms(program);
      return getContextMenu(programs);
    }
    return null;
  }

  private ActionMenu getContextMenu(ArrayList<MediathekProgramItem> programs) {
    if (programs.isEmpty()) {
      return null;
    }
    final Action mainAction = new ContextMenuAction(mLocalizer.msg("context", "Episodes in the Mediathek {0}", programs.size()),
        getContextMenuIcon());
    final ArrayList<Action> actionList = new ArrayList<Action>();
    for (final MediathekProgramItem episode : programs) {
      actionList.add(new AbstractAction(episode.getTitle(), episode.getIcon()) {

        public void actionPerformed(final ActionEvent e) {
          Launch.openURL(episode.getUrl());
        }
      });
    }
    return new ActionMenu(mainAction, actionList.toArray(new Action[actionList.size()]));
  }

  protected Icon getContextMenuIcon() {
    return contextIcon;
  }

  @Override
  public ActionMenu getButtonAction() {
    final ContextMenuAction menuAction = new ContextMenuAction("Mediathek", pluginIconSmall);
    final ArrayList<Object> subscribedList = new ArrayList<Object>(50);
    final ArrayList<Action> remainingList = new ArrayList<Action>(50);

    for (WebMediathek mediathek : getMediatheks()) {
      boolean subscribed = false;
      for (Channel channel : getPluginManager().getSubscribedChannels()) {
        if (mediathek.acceptsChannel(channel)) {
          subscribedList.add(setActionDescription(mediathek.getAction(true)));
          subscribed = true;
          break;
        }
      }
      if (!subscribed) {
        remainingList.add(setActionDescription(mediathek.getAction(true)));
      }
    }
    if (!remainingList.isEmpty()) {
      subscribedList.add(ContextMenuSeparatorAction.getInstance());
      subscribedList.add(new ActionMenu(new ContextMenuAction(mLocalizer.msg("notSubscribed", "Not subscribed channels")),remainingList.toArray()));
    }
    return new ActionMenu(menuAction, subscribedList.toArray());
  }

  private Action setActionDescription(AbstractAction action) {
    action.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.NAME));
    action.putValue(Action.LONG_DESCRIPTION, action.getValue(Action.NAME));
    return action;
  }

  private ArrayList<WebMediathek> getMediatheks() {
    if (mMediatheks == null) {
      mMediatheks = new ArrayList<WebMediathek>();
      try {
        final InputStream stream = getClass().getResourceAsStream("urls.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String separator = "";
        while (separator != null) {
          String title = reader.readLine();
          String regex = reader.readLine();
          String url = reader.readLine();
          separator = reader.readLine();
          mMediatheks.add(new WebMediathek(title, regex, url));
        }
        reader.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return mMediatheks;
  }

  public static MediathekPlugin getInstance() {
    return instance;
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new MediathekSettingsTab(mSettings);
  }

  @Override
  public void handleTvBrowserStartFinished() {
    readMediathekContents();
  }

  @Override
  public void loadSettings(final Properties properties) {
    mSettings = new MediathekSettings(properties);
  }

  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  void readMediathekContents() {
    final Thread contentThread = new Thread("Read Mediathek contents") {
      @Override
      public void run() {
        mDatabase = new Database(mSettings.getMediathekPath());
        updatePluginTree();
        // // update programs of current day to force their icons to show
        // final ArrayList<Program> validationPrograms = new
        // ArrayList<Program>(128);
        // final ProgramFilter currentFilter =
        // getPluginManager().getFilterManager().getCurrentFilter();
        // // have outer loop iterate over days so that all programs of today
        // are
        // // loaded first
        // for (int days = 0; days < 30; days++) {
        // final Date date = getPluginManager().getCurrentDate().addDays(days);
        // for (Channel channel : getPluginManager().getSubscribedChannels()) {
        // if (isSupportedChannel(channel)) {
        // final Iterator<Program> iter =
        // Plugin.getPluginManager().getChannelDayProgram(date, channel);
        // if (iter != null) {
        // while (iter.hasNext()) {
        // final Program program = iter.next();
        // // first search mediathek, then filter -> typically better
        // // performance
        // final MediathekProgram mediaProgram = findProgram(program);
        // if (mediaProgram != null && currentFilter.accept(program)) {
        // mediaProgram.readEpisodes();
        // validationPrograms.add(program);
        // }
        // }
        // }
        // }
        // }
        // }
        // SwingUtilities.invokeLater(new Runnable() {
        // public void run() {
        // for (Program program : validationPrograms) {
        // program.validateMarking();
        // }
        // }
        // });
      }
    };
    contentThread.setPriority(Thread.MIN_PRIORITY);
    contentThread.start();
  }

  public String convertHTML(final String html) {
    String result = HTMLTextHelper.convertHtmlToText(html);
    result = IOUtilities.replace(result, "&amp;", "&");
    return result;
  }

  @Override
  public boolean canUseProgramTree() {
    return false;
  }

  @Override
  public PluginTreeNode getRootNode() {
    return rootNode;
  }

  /**
   * Updates the plugin tree.
   */
  private void updatePluginTree() {
    final PluginTreeNode node = getRootNode();
    node.removeAllActions();
    node.removeAllChildren();
    node.getMutableTreeNode().setShowLeafCountEnabled(false);
    // for (MediathekProgram program : getSortedPrograms()) {
    // program.updatePluginTree(false);
    // }
    node.update();
  }

  public Logger getLogger() {
    return logger;
  }

  public Icon getPluginIcon() {
    return pluginIconSmall;
  }

  public Icon getWebIcon() {
    return mIconWeb;
  }

  protected Frame getFrame() {
    return this.getParentFrame();
  }

  @Override
  public ActionMenu getContextMenuActions(final Channel channel) {
    ArrayList<Action> actions = new ArrayList<Action>();
    for (WebMediathek mediathek : getMediatheks()) {
      if (mediathek.acceptsChannel(channel)) {
        actions.add(mediathek.getAction(false));
      }
    }
    if (actions.isEmpty()) {
      return null;
    }
    if (actions.size() == 1) {
      return new ActionMenu(actions.get(0));
    }
    final ContextMenuAction menuAction = new ContextMenuAction("Mediathek", pluginIconSmall);
    return new ActionMenu(menuAction, actions.toArray());
  }

  public Icon getImageIcon(String fileName) {
    return createImageIcon(fileName);
  }
}