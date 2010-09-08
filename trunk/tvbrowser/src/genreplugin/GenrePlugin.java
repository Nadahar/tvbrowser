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
package genreplugin;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.Action;

import util.ui.Localizer;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public class GenrePlugin extends Plugin {

  private static final String[] GENRE_SUFFIX = new String[] {"film", "serie"};

  /**
   * plugin version
   */
  private static final Version PLUGIN_VERSION = new Version(3,0);

  /**
   * localizer for this class
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GenrePlugin.class);

  /**
   * minimum length in characters for a genre to be accepted
   */
  private static final int MIN_GENRE_LENGTH = 4;

  /**
   * root node of the plugin tree
   */
  private PluginTreeNode mRootNode;

  private static GenrePlugin instance;

  private GenreSettings mSettings;

  private ArrayList<String> hiddenGenres = new ArrayList<String>();

  private List<String> currentGenres;

  private boolean mStartFinished = false;

  /**
   * constructor, called from core
   */
  public GenrePlugin() {
    super();
    instance = this;
  }

  protected static GenrePlugin getInstance() {
    return instance;
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }

  @Override
  public PluginInfo getInfo() {
    final String name = mLocalizer.msg( "pluginName" ,"Genres" );
    final String desc = mLocalizer.msg( "pluginDescription" ,"Shows the available programs sorted by genre" );
    return new PluginInfo(GenrePlugin.class, name, desc, "Michael Keppler",
        "GPL 3");
  }

  @Override
  public PluginTreeNode getRootNode() {
    if (mRootNode == null) {
      mRootNode = new PluginTreeNode(this);
    }
    return mRootNode;
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new GenreSettingsTab(this, hiddenGenres, mSettings);
  }

  protected void updateRootNode() {
    updateRootNode(false);
  }

  synchronized private void updateRootNode(final boolean force) {
    if (!mStartFinished && !force) {
      return;
    }
    final PluginTreeNode root = getRootNode();
    root.removeAllActions();
    root.removeAllChildren();

    int progCount = 0;
    final HashMap<String, PluginTreeNode> genreNodes = new HashMap<String, PluginTreeNode>();
    currentGenres = new ArrayList<String>();
    final Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();
    Date date = Date.getCurrentDate();
    final int maxDays = mSettings.getDays();
    for (int days = 0; days < maxDays; days++) {
      for (int i = 0; i < channels.length; ++i) {
        for (Iterator<Program> iter = devplugin.Plugin.getPluginManager()
            .getChannelDayProgram(date, channels[i]); iter.hasNext();) {
          final Program prog = iter.next();
          String genreField = prog.getTextField(ProgramFieldType.GENRE_TYPE);
          if (genreField != null) {
            // some programs have buggy fields with brackets
            if (genreField.startsWith("(") && genreField.endsWith(")")) {
              genreField = genreField.substring(1, genreField.length() - 1);
            }
            // remove sub genres in braces
            if (mSettings.getUnifyBraceGenres()) {
              int brace = genreField.indexOf('(');
              if (brace > 0) {
                genreField = genreField.substring(0, brace).trim();
              }
            }
            // some programs have multiple genres in the field
            final String[] genres = genreField.split(",|/");
            for (int genreIndex = 0; genreIndex < genres.length; genreIndex++) {
              String genre = genres[genreIndex].trim();
              // fix genres ending in dash
              if (genre.endsWith("-")) {
                if (genreIndex < genres.length - 1) {
                  for (String suffix : GENRE_SUFFIX) {
                    if (genres[genreIndex + 1].endsWith(suffix)) {
                      genre = genre.substring(0, genre.length() - 1).trim() + suffix;
                      break;
                    }
                  }
                }
              }
              if (genre.length() >= MIN_GENRE_LENGTH && !hiddenGenres.contains(genre)) {
                PluginTreeNode node = genreNodes.get(genre);
                if (node == null) {
                  node = new PluginTreeNode(genre);
                  formatGenreNode(maxDays, node);
                  genreNodes.put(genre, node);
                  currentGenres.add(genre);
                }
                node.addProgramWithoutCheck(prog);
                progCount++;
              }
            }
          }
        }
      }
      date = date.addDays(1);
      // stop if there are to many nodes for the tree
      if (progCount > 10000) {
        break;
      }
    }
    Collections.sort(currentGenres, String.CASE_INSENSITIVE_ORDER);
    mergeSimilarGenres(genreNodes);
    for (String genre : currentGenres) {
      final PluginTreeNode genreNode = genreNodes.get(genre);
      // the node may be deleted because of merging
      if (genreNode != null) {
        root.add(genreNode);
      }
    }
    root.update();
  }

  private void formatGenreNode(final int maxDays, final PluginTreeNode node) {
    node.setGroupingByDateEnabled(maxDays > 1);
    final Action hideCategory = new HideGenreAction(node.getUserObject()
        .toString());
    node.addAction(hideCategory);
  }

  private void mergeSimilarGenres(
      final HashMap<String, PluginTreeNode> genreNodes) {
    for (String genre : currentGenres) {
      final int index = genre.indexOf('-');
      if (index > 0 && index + 2 <= genre.length()) {
        String shortened = genre.replace("-", "");
        if (Character.isUpperCase(shortened.charAt(index))) {
          shortened = shortened.substring(0, index) + shortened.substring(index, index + 1).toLowerCase() + shortened.substring(index + 1);
          mergeTwoGenres(genre, shortened, genreNodes);
        }
        final String genreWithBlanks = genre.replace("-", " ");
        mergeTwoGenres(genre, genreWithBlanks, genreNodes);
      }
      final String umlauts = genre.replace("oe", "ö").replace("ae", "ä")
          .replace("ue", "ü");
      mergeTwoGenres(umlauts, genre, genreNodes);
    }
  }

  private void mergeTwoGenres(final String finalGenre, final String removedGenre, final HashMap<String,PluginTreeNode> genreNodes) {
    if (!currentGenres.contains(removedGenre) || !currentGenres.contains(finalGenre)) {
      return;
    }
    if (finalGenre.equals(removedGenre)) {
      return;
    }
    final PluginTreeNode firstNode = genreNodes.get(finalGenre);
    final PluginTreeNode secondNode = genreNodes.get(removedGenre);
    for (Program program : secondNode.getPrograms()) {
      firstNode.addProgram(program);
    }
    // delete the node to mark the genre deleted
    genreNodes.remove(removedGenre);
  }

  @Override
  public void handleTvBrowserStartFinished() {
    mStartFinished  = true;
    loadRootNode(getRootNode());
    mRootNode.getMutableTreeNode().setShowLeafCountEnabled(true);
    if (mRootNode.isEmpty()) {
      updateRootNode(true);
    }
    else {
      mRootNode.update();
    }
  }

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "system-file-manager", 16);
  }

  @Override
  public void handleTvDataUpdateFinished() {
    if (mRootNode != null) {
      updateRootNode();
    }
  }

  @Override
  public void loadSettings(final Properties properties) {
    mSettings = new GenreSettings(properties);
    getFilterFromSettings();
  }

  @Override
  public Properties storeSettings() {
    mSettings.setHiddenGenres(hiddenGenres.toArray());
    return mSettings.storeSettings();
  }

  protected void hideGenre(final String genre) {
    if (!hiddenGenres.contains(genre)) {
      hiddenGenres.add(genre);
    }
  }

  protected void getFilterFromSettings() {
    hiddenGenres = new ArrayList<String>();
    for (String genre : mSettings.getHiddenGenres()) {
      hideGenre(genre);
    }
  }

  @Override
  public void onActivation() {
    // only run node update for manual activation
    if (mRootNode != null) {
      updateRootNode();
    }
  }

  @Override
  public void writeData(final ObjectOutputStream out) throws IOException {
    // save the tree
    if (mRootNode != null && !mRootNode.isEmpty()) {
      storeRootNode(mRootNode);
    }
  }

  @Override
  public int getMarkPriorityForProgram(final Program p) {
    return Program.NO_MARK_PRIORITY;
  }

  @Override
  protected void loadRootNode(final PluginTreeNode node) {
    super.loadRootNode(node);
    // add the context menu actions after loading the tree
    final int maxDays = mSettings.getDays();
    final PluginTreeNode[] children = node.getChildren();
    for (PluginTreeNode child : children) {
      formatGenreNode(maxDays, child);
    }
  }

}
