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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import mediathekplugin.parser.ARDParser;
import mediathekplugin.parser.IParser;
import mediathekplugin.parser.ZDFParser;
import util.io.IOUtilities;
import util.ui.UiUtilities;
import util.ui.html.HTMLTextHelper;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * @author Bananeweizen
 * 
 */
public class MediathekPlugin extends Plugin {

  private static final Version PLUGIN_VERSION = new Version(2, 70, false);

  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(MediathekPlugin.class);

  private HashMap<String, MediathekProgram> programs;

  // cached ordered copy of the programs map
  private MediathekProgram[] sortedPrograms;

  private boolean sorted = false;
  
  private Icon markIcon, contextIcon, pluginIconSmall,
      pluginIconLarge,
      mIconWeb;

  /** location of the dialog */
  private Point mLocation = null;

  /** size of Dialog */
  private Dimension mSize = null;

  private Properties settings;

  private PluginTreeNode rootNode = new PluginTreeNode(this, false);

  private static MediathekPlugin instance = null;
  
  /** The logger for this class */
  private static java.util.logging.Logger logger = Logger
      .getLogger(MediathekPlugin.class.getName());

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }
  
  private IParser[] mParsers;

  @Override
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("name", "Mediathek");
    String description = mLocalizer.msg("description",
        "Shows video information for the ZDF Mediathek.");
    return new PluginInfo(MediathekPlugin.class, name, description,
        "Michael Keppler", "GPL 3");
  }

  public MediathekPlugin() {
    instance = this;
    programs = new HashMap<String, MediathekProgram>();
    pluginIconSmall = createImageIcon("actions", "web-search", 16);
    pluginIconLarge = createImageIcon("actions", "web-search", 22);
    contextIcon = createImageIcon("actions", "web-search", 16);
    markIcon = contextIcon;
    mIconWeb = createImageIcon("apps", "internet-web-browser", 16);
    rootNode.setGroupingByDateEnabled(false);
  }

  @Override
  public ActionMenu getContextMenuActions(Program program) {
    // pseudo action for example program
    if (program.equals(getPluginManager().getExampleProgram())) {
      return new ActionMenu(new AbstractAction(mLocalizer.msg("name",
          "Mediathek"), getContextMenuIcon()) {
        public void actionPerformed(ActionEvent e) {
          // empty
        }
      });
    }
    // do we support the channel at all?
    if (!isSupportedChannel(program.getChannel())) {
      return null;
    }
    // get mediathek contents, if not yet loaded
    if (programs.isEmpty()) {
      return actionMenuReadMediathekContents();
    }
    // do we have any media?
    MediathekProgram mediaProgram = findProgram(program);
    if (mediaProgram == null) {
      return null;
    }
    // now create a menu
    if (mediaProgram.canReadEpisodes()) {
      if (mediaProgram.getItemCount() == -1) {
        return actionMenuReadEpisodes(mediaProgram);
      } else {
        return mediaProgram.actionMenuShowEpisodes();
      }
    }
    else {
      return new ActionMenu(new LaunchBrowserAction(mediaProgram.getUrl(),
          mLocalizer.msg("action.browseProgram", "Show Mediathek")));
    }
  }

  public boolean isSupportedChannel(Channel channel) {
    for (IParser parser : mParsers) {
      if (parser.isSupportedChannel(channel)) {
        return true;
      }
    }
    return false;
  }

  protected Icon getContextMenuIcon() {
    return contextIcon;
  }

  private ActionMenu actionMenuReadEpisodes(final MediathekProgram mediaProgram) {
    AbstractAction actionSeries = new AbstractAction(mLocalizer.msg(
        "action.readEpisodes", "Search items in the Mediathek"),
        getContextMenuIcon()) {

      public void actionPerformed(ActionEvent event) {
        mediaProgram.readEpisodes();
      }
    };
    return new ActionMenu(actionSeries);
  }

  private ActionMenu actionMenuReadMediathekContents() {
    AbstractAction searchMedia = new AbstractAction(mLocalizer.msg(
        "action.readContents", "Read all Mediathek programs"),
        getContextMenuIcon()) {

      public void actionPerformed(ActionEvent event) {
        readMediathekContents();
      }
    };
    return new ActionMenu(searchMedia);
  }

  public void addProgram(IParser parser, String title, String url) {
    addProgram(new MediathekProgram(parser, title, url));
  }

  private void addProgram(MediathekProgram program) {
    programs.put(program.getTitle(), program);
    sorted = false;
  }

  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "internet-web-browser", 16);
  }

  @Override
  public String getProgramTableIconText() {
    return mLocalizer.msg("programTableIconText", "Mediathek");
  }

  @Override
  public Icon[] getProgramTableIcons(Program program) {
    if (programs.isEmpty()) {
      return null;
    }
    if (!isSupportedChannel(program.getChannel())) {
      return null;
    }
    MediathekProgram mediaProgram = findProgram(program);
    if (mediaProgram != null) {
      mediaProgram.readEpisodes();
      return new Icon[] { markIcon };
    }
    return null;
  }

  /**
   * finds a program in the list of online programs which matches the given
   * TV-Browser program
   * 
   * @param program
   * @return
   */
  private MediathekProgram findProgram(Program program) {
    if (programs == null) {
      return null;
    }
    String title = program.getTitle();
    MediathekProgram mediathekProgram = programs.get(title);
    if (mediathekProgram == null) {
      if (title.endsWith(")") && title.contains("(")) {
        title = title.substring(0, title.lastIndexOf("(") - 1);
        mediathekProgram = programs.get(title);
      }
    }
    return mediathekProgram;
  }

  @Override
  public ActionMenu getButtonAction() {
    Action buttonAction = new AbstractAction("Mediathek",
        pluginIconSmall) {

      public void actionPerformed(ActionEvent e) {
        showDialog();
      }
    };
    buttonAction.putValue(Plugin.BIG_ICON, pluginIconLarge);
    return new ActionMenu(buttonAction);
  }

  private void showDialog() {
    final ProgramsDialog dlg = new ProgramsDialog(getParentFrame(), this);

    dlg.pack();
    dlg.addComponentListener(new java.awt.event.ComponentAdapter() {

      public void componentResized(ComponentEvent e) {
        mSize = e.getComponent().getSize();
      }

      public void componentMoved(ComponentEvent e) {
        e.getComponent().getLocation(mLocation);
      }
    });

    if ((mLocation != null) && (mSize != null)) {
      dlg.setLocation(mLocation);
      dlg.setSize(mSize);
      dlg.setVisible(true);
    } else {
      dlg.setSize(600, 600);
      UiUtilities.centerAndShow(dlg);
      mLocation = dlg.getLocation();
      mSize = dlg.getSize();
    }
  }

  public static MediathekPlugin getInstance() {
    return instance;
  }

  public Vector<MediathekProgram> getMediathekPrograms() {
    return new Vector<MediathekProgram>(programs.values());
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new MediathekSettingsTab(settings);
  }

  @Override
  public void handleTvBrowserStartFinished() {
    if (settings.getProperty(IMediathekProperties.readProgramsOnStart, "false")
        .equals("true")) {
      readMediathekContents();
    }
  }

  @Override
  public void loadSettings(Properties settings) {
    if (settings == null) {
      this.settings = new Properties();
    } else {
      this.settings = settings;
    }
  }

  @Override
  public Properties storeSettings() {
    return settings;
  }

  private void readMediathekContents() {
    for (IParser reader : mParsers) {
      reader.readContents();
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        updatePluginTree();
      }
    });
    // update programs of current day to force their icons to show
    Date date = getPluginManager().getCurrentDate();
    for (Channel channel : getPluginManager().getSubscribedChannels()) {
      if (isSupportedChannel(channel)) {
        for (int days = 0; days < 30; days++) {
          Iterator<Program> iter = Plugin.getPluginManager()
              .getChannelDayProgram(date, channel);
          if (iter != null) {
            while (iter.hasNext()) {
              Program program = iter.next();
              MediathekProgram mediaProgram = findProgram(program);
              if (mediaProgram != null) {
                mediaProgram.readEpisodes();
                program.validateMarking();
              }
            }
          }
        }
      }
    }
  }

  public String convertHTML(String html) {
    html = HTMLTextHelper.convertHtmlToText(html);
    html = IOUtilities.replace(html, "&amp;", "&");
    return html;
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    return rootNode;
  }

  /**
   * Updates the plugin tree.
   */
  private void updatePluginTree() {
    PluginTreeNode node = getRootNode();
    node.removeAllActions();
    node.removeAllChildren();
    node.getMutableTreeNode().setShowLeafCountEnabled(false);
    node.addAction(new AbstractAction(mLocalizer.msg("action.readAll",
        "Read all episodes")) {

      public void actionPerformed(ActionEvent e) {
        for (MediathekProgram program : getSortedPrograms()) {
          program.readEpisodes();
        }
      }
    });
    for (MediathekProgram program : getSortedPrograms()) {
      program.updatePluginTree(false);
    }
    node.update();
  }

  public MediathekProgram[] getSortedPrograms() {
    if (!sorted) {
      sortedPrograms = new MediathekProgram[programs.size()];
      programs.values().toArray(sortedPrograms);
      Arrays.sort(sortedPrograms);
      sorted = true;
    }
    return sortedPrograms;
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
  public void onActivation() {
    mParsers = new IParser[] { new ZDFParser(), new ARDParser() };
  }

}