/*
 * GenrePlugin Copyright Michael Keppler
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
public class GenrePlugin extends Plugin implements IGenreSettings {

  /**
   * plugin version
   */
  private static final Version PLUGIN_VERSION = new Version(2,70);

  /**
   * localizer for this class
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GenrePlugin.class);

  /**
   * root node of the plugin tree
   */
  private PluginTreeNode mRootNode;

  private static GenrePlugin instance;

  private Properties mSettings;
  
  private ArrayList<String> hiddenGenres = new ArrayList<String>();

  private List<String> currentGenres;

  private boolean mStartFinished = false;

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
    String name = mLocalizer.msg( "pluginName" ,"Genres" );
    String desc = mLocalizer.msg( "pluginDescription" ,"Shows the available programs sorted by genre" );
    return new PluginInfo(GenrePlugin.class, name, desc, "Michael Keppler");
  }

  @Override
  public PluginTreeNode getRootNode() {
    if (mRootNode == null) {
      mRootNode = new PluginTreeNode(this, false);
      if (mStartFinished) {
        // update the tree as the plugin view has been switched on for the first
        // time after start
        updateRootNode();
      }
    }
    return mRootNode;
  }
  
  @Override
  public SettingsTab getSettingsTab() {
    return new GenreSettingsTab(this, hiddenGenres, mSettings);
  }

  protected void updateRootNode() {
    if (!mStartFinished) {
      return;
    }
    PluginTreeNode root = getRootNode();
    root.removeAllActions();
    root.removeAllChildren();
    root.getMutableTreeNode().setShowLeafCountEnabled(true);
    
    int progCount = 0;
    HashMap<String, PluginTreeNode> genreNodes = new HashMap<String, PluginTreeNode>();
    currentGenres = new ArrayList<String>();
    Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();
    Date date = Date.getCurrentDate();
    int maxDays = Integer.valueOf(mSettings.getProperty(SETTINGS_DAYS, "7"));
    for (int days = 0; days < maxDays; days++) {
      for (int i = 0; i < channels.length; ++i) {
        Iterator<Program> iter = devplugin.Plugin.getPluginManager()
            .getChannelDayProgram(date, channels[i]);
        if (iter != null) {
          while (iter.hasNext()) {
            Program prog = iter.next();
            String genreField = prog.getTextField(ProgramFieldType.GENRE_TYPE);
            if (genreField != null) {
              // some programs have buggy fields with brackets
              if (genreField.startsWith("(") && genreField.endsWith(")")) {
                genreField = genreField.substring(1, genreField.length() - 1);
              }
              // some programs have multiple genres in the field
              String[] genres = genreField.split(",");
              for (String g : genres) {
                String genre = g.trim();
                if (genre.length() > 3) {
                  if (!hiddenGenres.contains(genre)) {
                    PluginTreeNode node = genreNodes.get(genre);
                    if (node == null) {
                      node = new PluginTreeNode(genre);
                      node.setGroupingByDateEnabled(maxDays > 1);
                      Action hideCategory = new HideGenreAction(genre);
                      node.addAction(hideCategory);
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
      PluginTreeNode genreNode = genreNodes.get(genre);
      // the node may be deleted because of merging
      if (genreNode != null) {
        root.add(genreNode);
      }
    }
    root.update();
  }

  private void mergeSimilarGenres(HashMap<String, PluginTreeNode> genreNodes) {
    for (String genre : currentGenres) {
      int index = genre.indexOf('-');
      if (index > 0) {
        String shortened = genre.replace("-", "");
        if (Character.isUpperCase(shortened.charAt(index))) {
          shortened = shortened.substring(0, index) + shortened.substring(index, index + 1).toLowerCase() + shortened.substring(index + 1);
          mergeTwoGenres(genre, shortened, genreNodes);
        }
        String genreWithBlanks = genre.replace("-", " ");
        mergeTwoGenres(genre, genreWithBlanks, genreNodes);
      }
      String umlauts = genre.replace("oe", "ö").replace("ae", "ä").replace("ue", "ü");
      mergeTwoGenres(umlauts, genre, genreNodes);
    }
  }

  private void mergeTwoGenres(String finalGenre, String removedGenre, HashMap<String,PluginTreeNode> genreNodes) {
    if (!currentGenres.contains(removedGenre) || !currentGenres.contains(finalGenre)) {
      return;
    }
    if (finalGenre.equals(removedGenre)) {
      return;
    }
    PluginTreeNode firstNode = genreNodes.get(finalGenre);
    PluginTreeNode secondNode = genreNodes.get(removedGenre);
    for (Program program : secondNode.getPrograms()) {
      firstNode.addProgram(program);
    }
    // delete the node to mark the genre deleted
    genreNodes.remove(removedGenre);
  }

  @Override
  public void handleTvBrowserStartFinished() {
    mStartFinished  = true;
    // update tree, but only if it is shown at all
    if (mRootNode != null) {
      updateRootNode();
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
  public void loadSettings(Properties settings) {
    mSettings = settings;
    if (mSettings == null) {
      mSettings = new Properties();
    }
    getFilterFromSettings();
  }

  @Override
  public Properties storeSettings() {
    saveSettings(hiddenGenres.toArray());
    return mSettings;
  }

  public void hideGenre(String genre) {
    if (!hiddenGenres.contains(genre)) {
      hiddenGenres.add(genre);
    }
  }

  protected void getFilterFromSettings() {
    hiddenGenres.clear();
    int filterCount = Integer.parseInt(mSettings.getProperty(FILTERED_GENRES_COUNT, "0"));
    for (int i = 0; i<filterCount; i++) {
      hideGenre(mSettings.getProperty(FILTERED_GENRE+String.valueOf(i),""));
    }
  }

  public void saveSettings(Object[] hidden) {
    mSettings.setProperty(FILTERED_GENRES_COUNT, String.valueOf(hidden.length));
    for (int i = 0; i < hidden.length; i++) {
      mSettings.setProperty(FILTERED_GENRE+String.valueOf(i), (String) hidden[i]);
    }
  }

  @Override
  public void onActivation() {
    if (mRootNode != null) {
      updateRootNode();
    }
  }

}
