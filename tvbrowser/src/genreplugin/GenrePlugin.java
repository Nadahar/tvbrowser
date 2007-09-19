/*
 * GenrePlugin by Michael Keppler
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * VCS information:
 *     $Date: 2007-09-15 19:13:12 +0200 (Sa, 15 Sep 2007) $
 *   $Author: bananeweizen $
 * $Revision: 1 $
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
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public class GenrePlugin extends Plugin {

  private static final int MAX_DAYS = 1;

  /**
   * plugin version
   */
  private static final Version PLUGIN_VERSION = new Version(2,60);

  /**
   * localizer for this class
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GenrePlugin.class);

  /**
   * root node of the plugin tree
   */
  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private static GenrePlugin instance;

  private Properties mSettings;
  
  private ArrayList<String> hiddenGenres = new ArrayList<String>();

  private static final String FILTERED_GENRE = "filteredGenre";
  private static final String FILTERED_GENRES_COUNT = "filteredGenresCount";

  private List<String> currentGenres;

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
    return mRootNode;
  }
  
  @Override
  public SettingsTab getSettingsTab() {
    return new GenreSettingsTab(this, hiddenGenres);
  }

  protected void updateRootNode() {
    PluginTreeNode root = getRootNode();
    root.removeAllActions();
    root.removeAllChildren();
    root.getMutableTreeNode().setShowLeafCountEnabled(true);
    
    HashMap<String, PluginTreeNode> genreNodes = new HashMap<String, PluginTreeNode>();
    currentGenres = new ArrayList<String>();
    Channel[] channels = devplugin.Plugin.getPluginManager().getSubscribedChannels();
    Date date = Date.getCurrentDate();
    for (int days = 0; days < MAX_DAYS; days++) {
      for (int i = 0; i < channels.length; ++i) {
        Iterator<Program> iter = devplugin.Plugin.getPluginManager()
            .getChannelDayProgram(date, channels[i]);
        if (iter != null) {
          while (iter.hasNext()) {
            Program prog = iter.next();
            String genre = prog.getTextField(ProgramFieldType.GENRE_TYPE);
            if (genre != null) {
              genre = genre.trim();
              if (!hiddenGenres.contains(genre)) {
                PluginTreeNode node = genreNodes.get(genre);
                if (node == null) {
                  node = new PluginTreeNode(genre);
                  node.setGroupingByDateEnabled(MAX_DAYS > 1);
                  Action hideCategory = new HideGenreAction(genre);
                  node.addAction(hideCategory );
                  genreNodes.put(genre, node);
                  currentGenres.add(genre);
                }
                node.addProgram(prog);
              }
            }
          }
        }
      }
      date = date.addDays(1);
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
    updateRootNode();
  }

  @Override
  protected String getMarkIconName() {
    return "genreplugin/genreplugin.png";
  }

  @Override
  public void handleTvDataUpdateFinished() {
    updateRootNode();
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
    return mSettings;
  }

  public void hideGenre(String genre) {
    hiddenGenres.add(genre);
  }

  private void getFilterFromSettings() {
    int filterCount = Integer.parseInt(mSettings.getProperty(FILTERED_GENRES_COUNT, "0"));
    for (int i = 0; i<filterCount; i++) {
      hiddenGenres.add(mSettings.getProperty(FILTERED_GENRE+String.valueOf(i),""));
    }
  }

  public void saveSettings(Object[] hidden) {
    mSettings.setProperty(FILTERED_GENRES_COUNT, String.valueOf(hidden.length));
    for (int i = 0; i < hidden.length; i++) {
      mSettings.setProperty(FILTERED_GENRE+String.valueOf(i), (String) hidden[i]);
    }
  }

}
