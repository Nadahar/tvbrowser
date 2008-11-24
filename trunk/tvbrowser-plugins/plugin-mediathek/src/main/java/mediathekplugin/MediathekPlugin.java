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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import util.io.IOUtilities;
import util.ui.UiUtilities;
import util.ui.html.HTMLTextHelper;
import devplugin.ActionMenu;
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
    // only support ZDF
    if (!isSupportedChannel(program)) {
      return null;
    }
    // get mediathek contents, if not yet loaded
    if (programs.isEmpty()) {
      return actionMenuReadMediathekContents();
    }
    // this is the interesting part
    MediathekProgram mediaProgram = programs.get(program.getTitle());
    if (mediaProgram != null) {
      if (mediaProgram.getItemCount() == 0) {
        return actionMenuReadEpisodes(mediaProgram);
      } else {
        return mediaProgram.actionMenuShowEpisodes();
      }
    }
    return null;
  }

  private boolean isSupportedChannel(Program program) {
    return program.getChannel().getName().contains("ZDF");
  }

  protected Icon getContextMenuIcon() {
    return contextIcon;
  }

  private ActionMenu actionMenuReadEpisodes(final MediathekProgram mediaProgram) {
    AbstractAction actionSeries = new AbstractAction(mLocalizer.msg(
        "context.readEpisodes", "Search items in the Mediathek"),
        getContextMenuIcon()) {

      public void actionPerformed(ActionEvent event) {
        mediaProgram.readEpisodes();
      }
    };
    return new ActionMenu(actionSeries);
  }

  private ActionMenu actionMenuReadMediathekContents() {
    AbstractAction searchMedia = new AbstractAction(mLocalizer.msg(
        "context.readContents", "Read all Mediathek programs"),
        getContextMenuIcon()) {

      public void actionPerformed(ActionEvent event) {
        readMediathekContents();
      }
    };
    return new ActionMenu(searchMedia);
  }

  private void addProgram(String title, String relativeUrl) {
    addProgram(new MediathekProgram(title, relativeUrl));
  }

  private void addProgram(MediathekProgram program) {
    programs.put(program.getTitle(), program);
    sorted = false;
  }

  protected String readUrl(String urlString) {
    URL url;
    if (!urlString.startsWith("http")) {
      urlString = "http://www.zdf.de" + urlString;
    }
    try {
      url = new URL(urlString);
      BufferedReader reader = new BufferedReader(new InputStreamReader(url
          .openStream(), "utf-8"));
      String inputLine;
      StringBuffer buffer = new StringBuffer();
      while ((inputLine = reader.readLine()) != null) {
        buffer.append(inputLine);
      }
      reader.close();
      return buffer.toString();
    } catch (MalformedURLException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "";
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
    if (!isSupportedChannel(program)) {
      return null;
    }
    MediathekProgram mediaProgram = programs.get(program.getTitle());
    if (mediaProgram != null) {
      if (mediaProgram.getItemCount() == 0) {
        UpdateThread.getInstance().addProgram(mediaProgram);
      }
      return new Icon[] { markIcon };
    }
    return null;
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
    String startPage = readUrl("http://www.zdf.de/ZDFmediathek/inhalt?inPopup=true");
    Pattern pattern = Pattern.compile(Pattern
        .quote("<a href=\"/ZDFmediathek/content/")
        + "([^?]+)"
        + Pattern.quote("?reset=true\">")
        + "([^<]+)"
        + Pattern.quote("</a>"));
    Matcher matcher = pattern.matcher(startPage);
    int count = 0;
    while (matcher.find()) {
      String relativeUrl = matcher.group(1);
      String title = convertHTML(matcher.group(2));
      addProgram(title, relativeUrl);
      count++;
    }
    final String msg = "Read " + count + " programs from the ZDF Mediathek";
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        logger.info(msg);
        updatePluginTree();
      }
    });
  }

  protected String convertHTML(String html) {
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
}