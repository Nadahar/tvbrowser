/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 */

package tvraterplugin;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import util.io.IOUtilities;
import util.ui.ImageUtilities;
import util.ui.Localizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This Plugin gives the User the possibility to rate a Movie
 * 
 * TODO: Get Personal Ratings from Server
 * TODO: Send Original-Titles to Server 
 * 
 * @author Bodo Tasche
 */
public class TVRaterPlugin extends devplugin.Plugin {
  private static final Version mVersion = new Version(2, 70);

  protected final static int MINLENGTH = 15;

  private Properties _settings;

  /**
   * Root-Node for the Program-Tree
   */
  private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(TVRaterPlugin.class);

  /**
   * ID of the favorites plugin for recognition of unrated favorites
   */
  private static final String FAVORITES_PLUGIN_ID = "favoritesplugin.FavoritesPlugin";

  private Database _tvraterDB = new Database();

  private boolean hasRightToDownload = false;

  /** Instance of this Plugin */
  private static TVRaterPlugin _tvRaterInstance;

  private PluginInfo mPluginInfo;

  /**
   * flag indicating that the host program has started
   */
  private boolean mStartFinished;

  public TVRaterPlugin() {
    _tvRaterInstance = this;
    mRootNode.getMutableTreeNode().setIcon(
        new ImageIcon(ImageUtilities.createImageFromJar(
            "tvraterplugin/imgs/missingrating.png", TVRaterPlugin.class)));
  }

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      String name = mLocalizer.msg("pluginName", "TV Rater");
      String desc = mLocalizer
          .msg(
              "description",
              "Gives the User the possibility to rate a Show/Movie and get ratings from other Users");
      String author = "Bodo Tasche";

      mPluginInfo = new PluginInfo(TVRaterPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  /*
   *  (non-Javadoc)
   * @see devplugin.Plugin#getButtonAction()
   */
  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {

      public void actionPerformed(ActionEvent evt) {
        showDialog();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("pluginName", "TV Rater"));
    action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities
        .createImageFromJar("tvraterplugin/imgs/tvrater.png",
            TVRaterPlugin.class)));
    action.putValue(BIG_ICON, new ImageIcon(ImageUtilities.createImageFromJar(
        "tvraterplugin/imgs/tvrater22.png", TVRaterPlugin.class)));

    return new ActionMenu(action);
  }

  /**
   * This method is invoked by the host-application if the user has choosen
   * your plugin from the menu.
   */
  public void showDialog() {
    if ((_settings.getProperty("name", "").length() == 0)
        || (_settings.getProperty("password", "").length() == 0)) {
      showNotConfigured();
    } else {
      DialogOverview dlg = new DialogOverview(getParentFrame(), this);

      layoutWindow("dialogOverview", dlg);

      dlg.setVisible(true);
    }
  }

  /*
   *  (non-Javadoc)
   * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
   */
  public ActionMenu getContextMenuActions(final Program program) {
    AbstractAction action = new AbstractAction() {

      public void actionPerformed(ActionEvent evt) {
        showRatingDialog(program);
      }
    };
    if (getRating(program) != null) {
      action.putValue(Action.NAME, mLocalizer.msg("contextMenuText",
          "View rating"));
    } else {
      action.putValue(Action.NAME, mLocalizer.msg("contextNoRating",
          "Rate program"));
    }
    action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities
        .createImageFromJar("tvraterplugin/imgs/tvrater.png",
            TVRaterPlugin.class)));

    return new ActionMenu(action);
  }

  public void showRatingDialog(Program program) {
    if ((_settings.getProperty("name", "").length() == 0)
        || (_settings.getProperty("password", "").length() == 0)) {
      showNotConfigured();
    } else {
      DialogRating dlg = new DialogRating(getParentFrame(), this, program);

      layoutWindow("dialogRatin", dlg);

      dlg.setVisible(true);
    }

  }

  /**
   * Show a Information-Dialog if the Plugin was not configured yet
   */
  private void showNotConfigured() {
    int ret = JOptionPane.showConfirmDialog(getParentFrame(), mLocalizer.msg(
        "noUserText", "No User specified. Do you want to do this now?"),
        mLocalizer.msg("noUserTitle", "No User specified"),
        JOptionPane.YES_NO_OPTION);

    if (ret == JOptionPane.YES_OPTION) {
      getPluginManager().showSettings(this);
    }
  }

  public Properties storeSettings() {
    return _settings;
  }

  public void loadSettings(Properties settings) {
    if (settings == null) {
      settings = new Properties();
    }

    this._settings = settings;
  }

  public SettingsTab getSettingsTab() {
    return new TVRaterSettingsTab(_settings);
  }

  public String getMarkIconName() {
    return "tvraterplugin/imgs/missingrating.png";
  }

  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @return The description text for the program table icons.
   * @see #getProgramTableIcons(Program)
   */
  public String getProgramTableIconText() {
    return mLocalizer.msg("icon", "Rating");
  }

  /**
   * Gets the icons this Plugin provides for the given program. These icons
   * will be shown in the program table under the start time.
   * <p>
   * Return <code>null</code> if your plugin does not provide this feature.
   * 
   * @param program
   *            The programs to get the icons for.
   * @return The icons for the given program or <code>null</code>.
   * @see #getProgramTableIconText()
   */
  public Icon[] getProgramTableIcons(Program program) {
    Rating rating = getRating(program);

    if (rating != null) {
      return new Icon[] { RatingIconTextFactory.getImageIconForRating(rating
          .getOverallRating()) };
    }

    return null;
  }

  /**
   * Returns the Rating for a program.
   * This Function returns the personal rating if the settings say these ratings
   * are preferred
   *
   * @param program Get rating for this program
   * @return Rating
   */
  public Rating getRating(Program program) {
    Rating rating;

    if (_settings.getProperty("ownRating", "").equalsIgnoreCase("true")) {
      rating = getPersonalRating(program);
      if (rating != null) {
        return rating;
      }
    }

    return _tvraterDB.getServerRating(program);
  }

  /**
   * Get the personal rating for the given program
   * 
   * @param program
   * @return personal rating or <code>null</code> if no personal rating is available
   * @since 2.6 
   */
  private Rating getPersonalRating(Program program) {
    return _tvraterDB.getPersonalRating(program);
  }

  /**
   * Returns the Database for the Ratings
   * 
   * @return Rating-Database
   */
  public Database getDatabase() {
    return _tvraterDB;
  }

  /**
   * Returns the Settings for this Plugin
   * 
   * @return Settings
   */
  public Properties getSettings() {
    return _settings;
  }

  /**
   * Called by the host-application during start-up.
   * 
   * @see #writeData(ObjectOutputStream)
   */
  public void readData(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    _tvraterDB.readData(in);
  }

  /**
   * Counterpart to loadData. Called when the application shuts down.
   * 
   * @see #readData(ObjectInputStream)
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    _tvraterDB.writeData(out);
  }

  /**
   * Gets the parent frame.
   * <p>
   * The parent frame may be used for showing dialogs.
   * 
   * @return The parent frame.
   */
  public java.awt.Frame getParentFrameForTVRater() {

    return getParentFrame();
  }

  public void handleTvBrowserStartFinished() {
    hasRightToDownload = true;
    mStartFinished = true;
    updateRootNode();
    if (Integer.parseInt(_settings.getProperty("updateIntervall", "0")) == 2) {
      updateDB();
    }
  }

  /*
   *  (non-Javadoc)
   * @see devplugin.Plugin#handleTvDataUpdateFinished()
   */
  public void handleTvDataUpdateFinished() {
    if (!((_settings.getProperty("name", "").length() == 0) || (_settings
        .getProperty("password", "").length() == 0))
        && hasRightToDownload && IOUtilities.getMinutesAfterMidnight() > 1) {
      if (Integer.parseInt(_settings.getProperty("updateIntervall", "0")) < 3) {
        updateDB();
      }
    }
    updateRootNode();
  }

  /**
   * Updates the Database
   */
  private void updateDB() {
    final TVRaterPlugin tvrater = this;

    Thread updateThread = new Thread("TV Rater update") {
      public void run() {
        Updater up = new Updater(tvrater);
        up.run();
      }
    };
    updateThread.setPriority(Thread.MIN_PRIORITY);
    updateThread.start();
  }

  /**
   * Returns an Instance of this Plugin
   * 
   * @return Instance of this Plugin
   */
  public static TVRaterPlugin getInstance() {
    return _tvRaterInstance;
  }

  /**
   * Returns true if Program is rateable (Length > MINLENGTH or last Program of Day) 
   * @param program Program to check
   * @return true if program is rateable
   */
  public boolean isProgramRateable(Program program) {
    if ((program.getTitle() != null)
        && (program.getLength() >= TVRaterPlugin.MINLENGTH)) {
      return true;
    }

    if ((program.getTitle() != null) && (program.getLength() <= 0)) {
      program.getChannel();

      Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(
          program.getDate(), program.getChannel());

      Program last = null;

      while ((it != null) && (it.hasNext())) {
        last = it.next();
      }

      if (program == last) {
        return true;
      }

    }

    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    //manually cast to avoid unsafe compiler cast
    return (Class<? extends PluginsFilterComponent>[]) new Class[] { TVRaterFilter.class };
  }

  @Override
  public PluginsProgramFilter[] getAvailableFilter() {
    return new PluginsProgramFilter[] { new TVRaterProgramFilter(this) };
  }

  /**
   * Force an update of the currently shown programs in the program table
   * where we need to add/update a TV rating.
   * 
   * Internally called after a successful update of the TV ratings database.
   * 
   * @since 2.6
   */
  public void updateCurrentDate() {
    // dont update the UI if the rating updater runs on TV-Browser start
    if (!mStartFinished) {
      return;
    }
    Date currentDate = getPluginManager().getCurrentDate();
    final Channel[] channels = getPluginManager().getSubscribedChannels();
    for (int i = 0; i < channels.length; ++i) {
      final Iterator<Program> iter = getPluginManager().getChannelDayProgram(
          currentDate, channels[i]);
      if (null == iter) {
        continue;
      }
      while (iter.hasNext()) {
        Program prog = iter.next();
        if (getRating(prog) != null) {
          prog.validateMarking();
        }
      }
    }
  }

  @Override
  public boolean canUseProgramTree() {
    return true;
  }

  @Override
  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  /**
   * collect all expired favorites without rating for the plugin tree
   * 
   * @since 2.6
   */
  protected void updateRootNode() {
    if (!mStartFinished) {
      return;
    }
    mRootNode.removeAllChildren();
    mRootNode.getMutableTreeNode().setShowLeafCountEnabled(false);

    // add top ratings for each category
    HashMap<String, HashSet<PluginTreeNode>> titles = new HashMap<String, HashSet<PluginTreeNode>>();
    
    // sort ratings by title, so the nodes in the tree are sorted
    Collection<Rating> ratings = getDatabase().getServerRatings();
    Rating[] ratingsArr = new Rating[ratings.size()];
    ratings.toArray(ratingsArr);
    Arrays.sort(ratingsArr, new Comparator<Rating>() {
      public int compare(Rating arg0, Rating arg1) {
        return arg0.getTitle().compareTo(arg1.getTitle());
      }});
    
    // find all ratings with high values
    ArrayList<PluginTreeNode> listOverall = new ArrayList<PluginTreeNode>();
    ArrayList<PluginTreeNode> listAction = new ArrayList<PluginTreeNode>();
    ArrayList<PluginTreeNode> listFun = new ArrayList<PluginTreeNode>();
    ArrayList<PluginTreeNode> listErotic = new ArrayList<PluginTreeNode>();
    ArrayList<PluginTreeNode> listTension = new ArrayList<PluginTreeNode>();
    ArrayList<PluginTreeNode> listEntitlement = new ArrayList<PluginTreeNode>();
    for (int i=0; i < ratingsArr.length; i++) {
      Rating rating = ratingsArr[i];
      addTitle(titles, listOverall, rating, rating.getOverallRating());
      addTitle(titles, listAction, rating, rating.getActionRating());
      addTitle(titles, listFun, rating, rating.getFunRating());
      addTitle(titles, listErotic, rating, rating.getEroticRating());
      addTitle(titles, listTension, rating, rating.getTensionRating());
      addTitle(titles, listEntitlement, rating, rating.getEntitlementRating());
    }

    // now find the programs which match the previously collected titles
    Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
    Date date = Date.getCurrentDate();
    for (int d = 0; d < 31; d++) {
      for (int i = 0; i < channels.length; i++) {
        Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(
            date, channels[i]);
        while ((it != null) && (it.hasNext())) {
          Program program = it.next();
          if (program != null) {
            String title = program.getTitle();
            if (title != null) {
              HashSet<PluginTreeNode> nodes = titles.get(title);
              if (nodes != null) {
                for (Iterator<PluginTreeNode> iterator = nodes.iterator(); iterator
                    .hasNext();) {
                  PluginTreeNode titleNode = iterator.next();
                  titleNode.addProgramWithoutCheck(program);
                }
              }
            }
          }
        }
      }
      date = date.addDays(1);
    }

    // now add the title nodes to the plugin tree
    PluginTreeNode topOverall = mRootNode.addNode(mLocalizer.msg("topOverall", "Top programs"));
    PluginTreeNode topAction = mRootNode.addNode(mLocalizer.msg("topAction", "Top action"));
    PluginTreeNode topFun = mRootNode.addNode(mLocalizer.msg("topFun", "Top fun"));
    PluginTreeNode topErotic = mRootNode.addNode(mLocalizer.msg("topErotic", "Top erotic"));
    PluginTreeNode topTension = mRootNode.addNode(mLocalizer.msg("topTension", "Top tension"));
    PluginTreeNode topEntitlement = mRootNode.addNode(mLocalizer.msg("topEntitlement", "Top entitlement"));
    addList(topOverall, listOverall);
    addList(topAction, listAction);
    addList(topFun, listFun);
    addList(topErotic, listErotic);
    addList(topTension, listTension);
    addList(topEntitlement, listEntitlement);
    
    // add unrated favorites
    PluginTreeNode favoritesNode = mRootNode.addNode(mLocalizer.msg(
        "unratedFavorites", "Unrated favorites"));
    favoritesNode.setGroupingByDateEnabled(false);
    Program[] programs = getPluginManager().getMarkedPrograms();

    // search all unrated favorites
    List<Program> unratedFavs = new ArrayList<Program>();
    for (int progIndex = 0; progIndex < programs.length; progIndex++) {
      Program program = programs[progIndex];
      if (program.isExpired()) {
        Marker[] markers = program.getMarkerArr();
        for (int markerIndex = 0; markerIndex < markers.length; markerIndex++) {
          if (markers[markerIndex].getId()
              .equalsIgnoreCase(FAVORITES_PLUGIN_ID)) {
            if (getPersonalRating(program) == null) {
              unratedFavs.add(program);
            }
            break;
          }
        }
      }
    }
    favoritesNode.addPrograms(unratedFavs);
    mRootNode.update();
  }

  private void addList(PluginTreeNode topNode, ArrayList<PluginTreeNode> titleList) {
    for (PluginTreeNode titleNode : titleList) {
      if (!titleNode.isEmpty()) {
        topNode.add(titleNode);
      }
    }
  }

  private void addTitle(HashMap<String, HashSet<PluginTreeNode>> titles,
      ArrayList<PluginTreeNode> list, Rating rating, int ratingValue) {
    if (ratingValue >= 4) {
      PluginTreeNode titleNode = new PluginTreeNode(rating.getTitle());
      list.add(titleNode);
      titleNode.getMutableTreeNode().setIcon(RatingIconTextFactory.getImageIconForRating(ratingValue));
      HashSet<PluginTreeNode> nodes = titles.get(rating.getTitle());
      if (nodes == null) {
        nodes = new HashSet<PluginTreeNode>();
        titles.put(rating.getTitle(), nodes);
      }
      nodes.add(titleNode);
    }
  }

  @Override
  public void onActivation() {
    // the root node will only be update after the start-finished event
    updateRootNode();
  }
}