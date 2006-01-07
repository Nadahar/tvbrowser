/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.extras.favoritesplugin;

import java.io.*;
import java.util.Properties;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import util.ui.UiUtilities;
import util.exc.*;

import devplugin.*;

import javax.swing.*;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.extras.common.DefaultMarker;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.extras.common.DataSerializer;
import tvbrowser.extras.common.DataDeserializer;

/**
 * Plugin for managing the favorite programs.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class FavoritesPlugin  {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
          .getLocalizerFor(FavoritesPlugin.class);

  private static FavoritesPlugin mInstance;
  private Favorite[] mFavoriteArr;

  /** The IDs of the plugins that should receive the favorites. */
  private String[] mClientPluginIdArr;

  private Properties mSettings;

  private static String DATAFILE_PREFIX = "favoritesplugin.FavoritesPlugin";

  private DefaultMarker mMarker = new DefaultMarker(DATAFILE_PREFIX, IconLoader.getInstance().getIconFromTheme("apps", "bookmark", 16));

  private ConfigurationHandler mConfigurationHandler;

  // private ArrayList mBlackList = new ArrayList();

  /**
   * Creates a new instance of FavoritesPlugin.
   */
  private FavoritesPlugin() {
    mFavoriteArr = new Favorite[0];
    mClientPluginIdArr = new String[0];
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    load();

  }

  public static FavoritesPlugin getInstance() {
    if (mInstance == null) {
      mInstance = new FavoritesPlugin();
    }
    return mInstance;
  }


  private void load() {
    try {
      mConfigurationHandler.loadData(new DataDeserializer(){
        public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
          readData(in);
        }
      });
    }catch(IOException e) {
      ErrorHandler.handle("Could not load favorites.", e);
    }

    try {
      Properties prop = mConfigurationHandler.loadSettings();
      loadSettings(prop);
    }catch(IOException e) {
      ErrorHandler.handle("Could not load favorite settings.", e);
    }

   /* String userDirectoryName = Settings.getUserDirectoryName();
    File userDirectory = new File(userDirectoryName);
    File datFile = new File(userDirectory, DATAFILE_PREFIX + ".dat");
    File propFile = new File(userDirectory, DATAFILE_PREFIX + ".prop");

    if (datFile.exists()) {
      ObjectInputStream in = null;
      try {
        in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(datFile), 0x4000));
        readData(in);
      }
      catch (Throwable thr) {
        ErrorHandler.handle("Could not load favorites.", thr);
      }
      finally {
        if (in != null) {
          try { in.close(); } catch (IOException exc) {
            // ignore
          }
        }
      }
    }


    // load plugin settings
    BufferedInputStream in = null;
    try {
      if (propFile.exists()) {
        Properties prop = new Properties();
        in = new BufferedInputStream(new FileInputStream(propFile), 0x4000);
        prop.load(in);
        in.close();
        loadSettings(prop);
      } else {
        loadSettings(new Properties());
      }
    }
    catch (Throwable thr) {
      ErrorHandler.handle("Could not load settings for favorites.", thr);
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (IOException exc) {
          // ignore
        }
      }
    }  */

  }

  public void store() {

    try {
      mConfigurationHandler.storeData(new DataSerializer(){
        public void write(ObjectOutputStream out) throws IOException {
          writeData(out);
        }
      });
    } catch (IOException e) {
      ErrorHandler.handle("Could not store favorites.", e);
    }

    try {
      mConfigurationHandler.storeSettings(mSettings);
    } catch (IOException e) {
      ErrorHandler.handle("Could not store favorite settings.", e);
    }

    /*
    String userDirectoryName = Settings.getUserDirectoryName();
    File userDirectory = new File(userDirectoryName);


    File tmpDatFile = new File(userDirectory, DATAFILE_PREFIX + ".dat.temp");
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(tmpDatFile));
      writeData(out);
      out.close();

      // Saving suceed -> Delete the old file and rename the temp file
      File datFile = new File(userDirectory, DATAFILE_PREFIX + ".dat");
      datFile.delete();
      tmpDatFile.renameTo(datFile);
    }
    catch(Throwable thr) {
      ErrorHandler.handle("Could not store favorites.", thr);
    }
    finally {
      if (out != null) {
        try { out.close(); } catch (IOException exc) {
          // ignore
        }
      }
    }

    // save the plugin settings in a temp file
    FileOutputStream fOut = null;
    File tmpPropFile = new File(userDirectory, DATAFILE_PREFIX + ".prop.temp");
    try {
      if (mSettings != null) {
        fOut = new FileOutputStream(tmpPropFile);
        mSettings.store(fOut, "Settings for Favorites");
        fOut.close();
      }

      // Saving suceed -> Delete the old file and rename the temp file
      File propFile = new File(userDirectory, DATAFILE_PREFIX + ".prop");
      propFile.delete();
      tmpPropFile.renameTo(propFile);
    }
    catch (Throwable thr) {
      ErrorHandler.handle("Could not store settings for favorites.", thr);
    }
    finally {
      if (fOut != null) {
        try { fOut.close(); } catch (IOException exc) {
          // ignore
        }
      }
    }     */


  }

  public ImageIcon getIconFromTheme(String category, String Icon, int size) {
    return IconLoader.getInstance().getIconFromTheme(category, Icon, size);
  }

  private void readData(ObjectInputStream in) throws IOException,
          ClassNotFoundException {
    int version = in.readInt();

    // get the favorites
    int size = in.readInt();
    Favorite[] newFavoriteArr = new Favorite[size];
    for (int i = 0; i < size; i++) {
      newFavoriteArr[i] = new Favorite(in);
    }
    mFavoriteArr = newFavoriteArr;

    // mark all the favorites
    for (int i = 0; i < mFavoriteArr.length; i++) {
      Program[] programArr = mFavoriteArr[i].getPrograms();
      for (int j = 0; j < programArr.length; j++) {
        programArr[j].mark(mMarker);
      }
    }

    // Get the client plugins
    size = in.readInt();
    mClientPluginIdArr = new String[size];
    for (int i = 0; i < size; i++) {
      if (version == 1) {
        // In older versions of TV-Browser, not the plugin ID was saved,
        // but its class name.
        // -> We have to translate the class name into an ID.
        String className = (String) in.readObject();
        mClientPluginIdArr[i] = "java." + className;
      } else {
        String pluginId = (String) in.readObject();
        mClientPluginIdArr[i] = pluginId;
      }
    }

//    updateTree();
  }

  private void deleteFavorite(Favorite favorite) {
    Program[] delFavPrograms = favorite.getPrograms();
    favorite.unmarkPrograms();
    ArrayList list = new ArrayList();

    for (int i = 0; i < mFavoriteArr.length; i++) {
      if(!mFavoriteArr[i].equals(favorite)) {
        mFavoriteArr[i].handleContainingPrograms(delFavPrograms);
        list.add(mFavoriteArr[i]);
      }
    }

    mFavoriteArr = new Favorite[list.size()];
    list.toArray(mFavoriteArr);
//    updateTree();
  }

  /* public void updateTree() {
PluginTreeNode node = getRootNode();
node.removeAllActions();
node.addAction(new CreateFavoriteAction());
node.removeAllChildren();
// ArrayList programs = new ArrayList();

for (int i = 0; i < mFavoriteArr.length; i++) {

//       * ArrayList blackList = mFavoriteArr[i].getBlackList();
//       * if(!blackList.isEmpty()) for (int j = 0; j < blackList.size(); j++)
//       * if(!programs.contains(blackList.get(j)))
//       * programs.add(blackList.get(j));

PluginTreeNode curNode = node.addNode(mFavoriteArr[i].getTitle());
curNode.addAction(new EditFavoriteAction(mFavoriteArr[i]));
curNode.addAction(new DeleteFavoriteAction(mFavoriteArr[i]));
curNode.addAction(null);
curNode.addAction(new RenameFavoriteAction(mFavoriteArr[i]));

Program[] progs = mFavoriteArr[i].getPrograms();
for (int j = 0; j < progs.length; j++) {
if (!mFavoriteArr[i].getBlackList().contains(progs[j]))
curNode.addProgram(progs[j]);
else
progs[j].unmark(this);
}
}


//     * if(!programs.isEmpty()) { PluginTreeNode curNode =
//     * node.addNode("Blacklist"); for(int i = 0; i < programs.size(); i++) {
//     * if(!((Program)programs.get(i)).isExpired()) {
//     * curNode.addProgram((Program)programs.get(i)); Program[] p =
//     * {((Program)programs.get(i))}; ((Program)programs.get(i)).unmark(this);
//     * for(int j = 0; j < mFavoriteArr.length; j++)
//     * mFavoriteArr[j].handleContainingPrograms(p); } } }


node.update();
}                */

  private void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version

    out.writeInt(mFavoriteArr.length);
    for (int i = 0; i < mFavoriteArr.length; i++) {
      mFavoriteArr[i].writeData(out);
    }

    out.writeInt(mClientPluginIdArr.length);
    for (int i = 0; i < mClientPluginIdArr.length; i++) {
      out.writeObject(mClientPluginIdArr[i]);
    }
  }

  /**
   * Called by the host-application during start-up. Implements this method to
   * load your plugins settings from the file system.
   */
  private void loadSettings(Properties settings) {
    mSettings = settings;
    if (settings == null) {
      throw new IllegalArgumentException("settings is null");
    }
  }



  private int getIntegerSetting(Properties prop, String key, int defaultValue) {
    int res = defaultValue;
    try {
      res = Integer.parseInt(prop.getProperty(key, "" + defaultValue));
    } catch (NumberFormatException e) {
      // ignore
    }
    return res;
  }

  public ActionMenu getButtonAction(final Frame parentFrame) {
    ButtonAction action = new ButtonAction();
    action.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showManageFavoritesDialog(parentFrame);
      }
    });

    action.setBigIcon(getIconFromTheme("apps", "bookmark", 22));
    action.setSmallIcon(getIconFromTheme("apps", "bookmark", 16));
    action.setShortDescription(mLocalizer.msg("favoritesManager",
            "Manage favorite programs"));
    action.setText(mLocalizer.msg("buttonText", "Manage Favorites"));

    return new ActionMenu(action);
  }

  public ActionMenu getContextMenuActions(final Frame parentFrame, final Program program) {
    ContextMenuAction menu = new ContextMenuAction();

    ArrayList favorites = new ArrayList();
    ArrayList blackFavorites = new ArrayList();

    for (int i = 0; i < mFavoriteArr.length; i++) {
      Program[] programs = mFavoriteArr[i].getPrograms();
      for (int j = 0; j < programs.length; j++)
        if (programs[j].equals(program)
                && !mFavoriteArr[i].getBlackList().contains(program)) {
          favorites.add(mFavoriteArr[i]);
          break;
        }
      if (mFavoriteArr[i].getBlackList().contains(program))
        blackFavorites.add(mFavoriteArr[i]);
    }

    if (favorites.isEmpty() && blackFavorites.isEmpty()) {
      menu.setText(mLocalizer
              .msg("contextMenuText", "Add to favorite programs"));
      menu.setActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          showEditFavoriteDialog(parentFrame, program);
        }
      });
    } else {
      menu.setText(mLocalizer.msg("manageFavorites", "Favorites"));

      ActionMenu[] submenu = new ActionMenu[2];

      ContextMenuAction add = new ContextMenuAction();
      add.setText(mLocalizer.msg("contextMenuRemove",
              "Remove program from favorite programs"));

      ContextMenuAction del = new ContextMenuAction();
      del.setText(mLocalizer.msg("contextMenuAdd", "Reactivate as favorite"));
      Action[] addAction = new AbstractAction[favorites.size()
              + ((favorites.size() > 1) ? 1 : 0)];
      Action[] delAction = new AbstractAction[blackFavorites.size()
              + ((blackFavorites.size() > 1) ? 1 : 0)];

      for (int i = 0; i < favorites.size(); i++) {
        Favorite fav = (Favorite) favorites.get(i);
        addAction[i] = new ContextMenuActionHandler(program, "add", fav, fav
                .getTitle());
      }

      if (favorites.size() > 1)
        addAction[favorites.size()] = new ContextMenuActionHandler(program,
                "addtoall", null, mLocalizer.msg("ActionFromAll", "From All"));

      for (int i = 0; i < blackFavorites.size(); i++) {
        Favorite fav = (Favorite) blackFavorites.get(i);
        delAction[i] = new ContextMenuActionHandler(program, "remove", fav, fav
                .getTitle());
      }

      if (blackFavorites.size() > 1)
        delAction[blackFavorites.size()] = new ContextMenuActionHandler(
                program, "removefromall", null, mLocalizer.msg("ActionInAll",
                "For all"));

      if (!favorites.isEmpty() && !blackFavorites.isEmpty()) {
        menu.setSmallIcon(getIconFromTheme("apps", "bookmark", 16));

        submenu[0] = new ActionMenu(add, addAction);
        submenu[1] = new ActionMenu(del, delAction);

        return new ActionMenu(menu, submenu);
      } else if (!favorites.isEmpty() && blackFavorites.isEmpty()) {
        add.setSmallIcon(getIconFromTheme("apps", "bookmark", 16));

        return new ActionMenu(add, addAction);
      } else if (favorites.isEmpty() && !blackFavorites.isEmpty()) {
        del.setSmallIcon(getIconFromTheme("apps", "bookmark", 16));

        return new ActionMenu(del, delAction);
      }
    }

    menu.setSmallIcon(getIconFromTheme("action", "bookmark-new", 16));
    return new ActionMenu(menu);
  }

  protected void addToBlackList(Program program, Favorite fav) {
    fav.getBlackList().add(program);
    Program[] p = { program };
      program.unmark(mMarker);

    for (int i = 0; i < mFavoriteArr.length; i++)
      mFavoriteArr[i].handleContainingPrograms(p);

    if (ManageFavoritesDialog.getInstance() != null)
      ManageFavoritesDialog.getInstance().favoriteSelectionChanged();

//    updateTree();
  }

  protected void addToAllBlackLists(Program program) {
    for (int i = 0; i < mFavoriteArr.length; i++)
      if (mFavoriteArr[i].contains(program))
        mFavoriteArr[i].getBlackList().add(program);
        program.unmark(mMarker);

    if (ManageFavoritesDialog.getInstance() != null)
      ManageFavoritesDialog.getInstance().favoriteSelectionChanged();

//    updateTree();
  }

  protected void removeFromBlackList(Program program, Favorite fav) {
    fav.getBlackList().remove(program);

    program.mark(mMarker);

    if (ManageFavoritesDialog.getInstance() != null)
      ManageFavoritesDialog.getInstance().favoriteSelectionChanged();

//    updateTree();
  }

  protected void removeFromAllBlackLists(Program program) {
    for (int i = 0; i < mFavoriteArr.length; i++) {
      if (mFavoriteArr[i].getBlackList().contains(program))
        mFavoriteArr[i].getBlackList().remove(program);
    }

    program.mark(mMarker);

    if (ManageFavoritesDialog.getInstance() != null)
      ManageFavoritesDialog.getInstance().favoriteSelectionChanged();

//    updateTree();
  }

  private void showManageFavoritesDialog(Frame parentFrame) {
    int splitPanePosition = getIntegerSetting(mSettings, "splitpanePosition",
            200);
    int width = getIntegerSetting(mSettings, "width", 500);
    int height = getIntegerSetting(mSettings, "height", 300);
    ManageFavoritesDialog dlg = new ManageFavoritesDialog(parentFrame, mFavoriteArr, splitPanePosition);
    dlg.setSize(new Dimension(width, height));
    UiUtilities.centerAndShow(dlg);

    if (dlg.getOkWasPressed()) {
      mFavoriteArr = dlg.getFavorites();
//      updateTree();
    }
    splitPanePosition = dlg.getSplitpanePosition();
    mSettings.setProperty("splitpanePosition", "" + splitPanePosition);
    mSettings.setProperty("width", "" + dlg.getWidth());
    mSettings.setProperty("height", "" + dlg.getHeight());
  }

  private void addFavorite(Favorite fav) {
    Favorite[] newFavoritesArr = new Favorite[mFavoriteArr.length + 1];
    System.arraycopy(mFavoriteArr, 0, newFavoritesArr, 0, mFavoriteArr.length);
    newFavoritesArr[mFavoriteArr.length] = fav;
    mFavoriteArr = newFavoritesArr;
  }

  public void showEditFavoriteDialog(Frame parentFrame, Program program) {
    Favorite favorite = new Favorite(program.getTitle());

    EditFavoriteDialog dlg = new EditFavoriteDialog(parentFrame, favorite);
    dlg.centerAndShow();

    if (dlg.getOkWasPressed()) {
      addFavorite(favorite);
      /*
       * Favorite[] newFavoritesArr = new Favorite[mFavoriteArr.length + 1];
       * System.arraycopy(mFavoriteArr, 0, newFavoritesArr, 0,
       * mFavoriteArr.length); newFavoritesArr[mFavoriteArr.length] = favorite;
       * mFavoriteArr = newFavoritesArr;
       */
//      updateTree();
    }
  }

  /*
  public PluginInfo getInfo() {
    String name = mLocalizer
        .msg("favoritesManager", "Manage favorite programs");
    String desc = mLocalizer
        .msg("description",
            "Automatically marks your favorite programs and passes them to other Plugins.");
    String author = "Til Schneider, www.murfman.de";

    return new PluginInfo(name, desc, author, new Version(1, 12));
  }      */

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "bookmark");
  }

  void unmark(Program[] programArr) {
    // unmark all programs with this plugin
    for (int i = 0; i < programArr.length; i++) {
        programArr[i].unmark(mMarker);
    }
  }


  void mark(Program[] programArr) {
    // mark all programs with this plugin
    for (int i = 0; i < programArr.length; i++) {
      programArr[i].mark(mMarker);
    }

    // Pass the program list to all client plugins
    for (int i = 0; i < mClientPluginIdArr.length; i++) {
      PluginAccess plugin = PluginManagerImpl.getInstance().getActivatedPluginForId(
          mClientPluginIdArr[i]);
      if (plugin != null) {
        plugin.receivePrograms(programArr);
      }
    }
  }

  public void handleTvDataUpdateFinished() {
    // Update all favorites
    for (int i = 0; i < mFavoriteArr.length; i++) {
      try {
        mFavoriteArr[i].updatePrograms();
      } catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }
//    updateTree();
  }

  /*
   * public ArrayList getBlackList() { return mBlackList; }
   */

  public String[] getClientPluginIds() {
    return mClientPluginIdArr;
  }

  public void setClientPluginIds(String[] clientPluginArr) {
    mClientPluginIdArr = clientPluginArr;
  }

  /**
   * Returns a new SettingsTab object, which is added to the settings-window.
   */
  public SettingsTab getSettingsTab() {
    return new FavoritesSettingTab();
  }

  public boolean canUseProgramTree() {
    return true;
  }

  class RenameFavoriteAction extends ButtonAction {
    private Favorite mFavorite;
    private Frame mParentFrame;

    public RenameFavoriteAction(Frame parentFrame, Favorite favorite) {
      mFavorite = favorite;
      mParentFrame = parentFrame;
      super.setText(mLocalizer.msg("rename", "rename"));
    }

    public void actionPerformed(ActionEvent e) {
      String newName = (String) JOptionPane.showInputDialog(mParentFrame,
              "Title:", "Rename favorite", JOptionPane.PLAIN_MESSAGE, null, null,
              mFavorite.getTitle());
      if (newName != null) {
        mFavorite.setTitle(newName);
//        updateTree();
      }
    }
  }

  class EditFavoriteAction extends ButtonAction {

    private Favorite mFavorite;
    private Frame mParentFrame;

    public EditFavoriteAction(Frame parentFrame, Favorite favorite) {
      mFavorite = favorite;
      mParentFrame = parentFrame;
      super.setText(mLocalizer.msg("edit", "edit"));
    }

    public void actionPerformed(ActionEvent e) {
      EditFavoriteDialog dlg = new EditFavoriteDialog(mParentFrame,
              mFavorite);
      dlg.centerAndShow();
      if (dlg.getOkWasPressed()) {
//        updateTree();
      }
    }
  }

  class DeleteFavoriteAction extends ButtonAction {

    private Favorite mFavorite;

    public DeleteFavoriteAction(Favorite favorite) {
      mFavorite = favorite;
      super.setText(mLocalizer.msg("delete", "delete"));
    }

    public void actionPerformed(ActionEvent e) {
      deleteFavorite(mFavorite);
    }
  }

  class CreateFavoriteAction extends ButtonAction {

    private Frame mParentFrame;

    public CreateFavoriteAction(Frame parentFrame) {
      mParentFrame = parentFrame;
      super.setSmallIcon(getIconFromTheme("actions", "document-new", 16));
      super.setText(mLocalizer.msg("new", "Create new favorite"));
    }

    public void actionPerformed(ActionEvent e) {
      Favorite favorite = new Favorite();
      EditFavoriteDialog dlg = new EditFavoriteDialog(mParentFrame,
              favorite);
      dlg.centerAndShow();
      if (dlg.getOkWasPressed()) {
        addFavorite(favorite);
//        updateTree();
      }
    }
  }

}
