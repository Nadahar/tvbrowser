/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.*;

import tvbrowser.core.TvDataUpdateListener;
import tvbrowser.core.TvDataUpdater;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.extras.common.*;
import tvbrowser.extras.favoritesplugin.core.*;
import tvbrowser.extras.favoritesplugin.wizards.TypeWizardStep;
import tvbrowser.extras.favoritesplugin.wizards.WizardHandler;
import tvbrowser.extras.favoritesplugin.wizards.ExcludeWizardStep;
import tvbrowser.extras.favoritesplugin.dlgs.EditFavoriteDialog;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesDialog;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.UiUtilities;
import devplugin.*;

/**
 * Plugin for managing the favorite programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FavoritesPlugin implements ContextMenuIf{

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
          .getLocalizerFor(FavoritesPlugin.class);

  private static FavoritesPlugin mInstance;
  private Favorite[] mFavoriteArr;

  /** The IDs of the plugins that should receive the favorites. */
  private String[] mClientPluginIdArr;

  private Properties mSettings;

  private static String DATAFILE_PREFIX = "favoritesplugin.FavoritesPlugin";

  public static final Marker MARKER = new DefaultMarker(DATAFILE_PREFIX, IconLoader.getInstance().getIconFromTheme("apps", "bookmark", 16));

  private ConfigurationHandler mConfigurationHandler;

  private PluginTreeNode mRootNode;
  
  private boolean mShowInfoOnNewProgramsFound = true;

  /**
   * Creates a new instance of FavoritesPlugin.
   */
  private FavoritesPlugin() {
    mInstance = this;
    mFavoriteArr = new Favorite[0];
    mClientPluginIdArr = new String[0];
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    load();
    mRootNode = new PluginTreeNode("Favorites");
    updateRootNode();

    TvDataUpdater.getInstance().addTvDataUpdateListener(new TvDataUpdateListener() {
      public void tvDataUpdateStarted() {
      }

      public void tvDataUpdateFinished() {
        for (int i = 0; i < mFavoriteArr.length; i++) {
      try {
        mFavoriteArr[i].updatePrograms();
      } catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }

    ArrayList showInfoFavorites = new ArrayList();
    
    for (int i = 0; i < mFavoriteArr.length; i++) {
      if (mFavoriteArr[i].isRemindAfterDownload() && mFavoriteArr[i].getNewPrograms().length > 0)
        showInfoFavorites.add(mFavoriteArr[i]);
    }
    
    if(!showInfoFavorites.isEmpty()) {
      Favorite[] fav = new Favorite[showInfoFavorites.size()];
      showInfoFavorites.toArray(fav);
      
      showManageFavoritesDialog(true, fav);
    }
    
      }
    });
  }

  public static synchronized FavoritesPlugin getInstance() {
    if (mInstance == null)
      new FavoritesPlugin();
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
      ErrorHandler.handle(mLocalizer.msg("couldNotLoadFavorites","Could not load favorites"), e);
    }

    try {
      Properties prop = mConfigurationHandler.loadSettings();
      loadSettings(prop);
    }catch(IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotLoadFavoritesSettings","Could not load settings for favorites"), e);
    }

  }

  public void store() {

    try {
      mConfigurationHandler.storeData(new DataSerializer(){
        public void write(ObjectOutputStream out) throws IOException {
          writeData(out);
        }
      });
    } catch (IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotStoreFavorites","Could not store favorites"), e);
    }

    try {
      mConfigurationHandler.storeSettings(mSettings);
    } catch (IOException e) {
      ErrorHandler.handle(mLocalizer.msg("couldNotStoreFavoritesSettings","Could not store settings for favorites"), e);
    }

  }

  public ImageIcon getIconFromTheme(String category, String Icon, int size) {
    return IconLoader.getInstance().getIconFromTheme(category, Icon, size);
  }

  private void readData(ObjectInputStream in) throws IOException,
          ClassNotFoundException {
    int version = in.readInt();

    int size = in.readInt();

    Favorite[] newFavoriteArr = new Favorite[size];
    for (int i = 0; i < size; i++) {
      if (version <= 2) {
        newFavoriteArr[i] = new AdvancedFavorite(null, in);
      }
      else {
        String typeID = (String)in.readObject();
        if (TopicFavorite.TYPE_ID.equals(typeID)) {
          newFavoriteArr[i] = new TopicFavorite(in);
        }
        else if (TitleFavorite.TYPE_ID.equals(typeID)) {
          newFavoriteArr[i] = new TitleFavorite(in);
        }
        else if (ActorsFavorite.TYPE_ID.equals(typeID)) {
          newFavoriteArr[i] = new ActorsFavorite(in);
        }
        else if (AdvancedFavorite.TYPE_ID.equals(typeID)) {
          newFavoriteArr[i] = new AdvancedFavorite(in);
        }

      }


    }
    mFavoriteArr = newFavoriteArr;

    // mark all the favorites
    for (int i = 0; i < mFavoriteArr.length; i++) {
      Program[] programArr = mFavoriteArr[i].getPrograms();
      for (int j = 0; j < programArr.length; j++) {
        programArr[j].mark(MARKER);
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
        mClientPluginIdArr[i] = (String) in.readObject();
      }
    }
    
    if(version == 4)
      this.mShowInfoOnNewProgramsFound = in.readBoolean();

  }

  public void deleteFavorite(Favorite favorite) {
    Program[] delFavPrograms = favorite.getPrograms();
    for (int i=0; i<delFavPrograms.length; i++) {
      delFavPrograms[i].unmark(MARKER);
    }


    ArrayList list = new ArrayList();

    for (int i = 0; i < mFavoriteArr.length; i++) {
      if(!mFavoriteArr[i].equals(favorite)) {
        mFavoriteArr[i].handleContainingPrograms(delFavPrograms);
        list.add(mFavoriteArr[i]);
      }
    }

    mFavoriteArr = new Favorite[list.size()];
    list.toArray(mFavoriteArr);

    ReminderPlugin.getInstance().removePrograms(favorite.getPrograms());
    updateRootNode();

  }


  private void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(4); // version

    out.writeInt(mFavoriteArr.length);
    for (int i = 0; i < mFavoriteArr.length; i++) {
      out.writeObject(mFavoriteArr[i].getTypeID());
      mFavoriteArr[i].writeData(out);
    }

    out.writeInt(mClientPluginIdArr.length);
    for (int i = 0; i < mClientPluginIdArr.length; i++) {
      out.writeObject(mClientPluginIdArr[i]);
    }
    
    out.writeBoolean(mShowInfoOnNewProgramsFound);
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
        showManageFavoritesDialog(false, mFavoriteArr);
      }
    });

    action.setBigIcon(getIconFromTheme("apps", "bookmark", 22));
    action.setSmallIcon(getIconFromTheme("apps", "bookmark", 16));
    action.setShortDescription(mLocalizer.msg("favoritesManager",
            "Manage favorite programs"));
    action.setText(mLocalizer.msg("buttonText", "Manage Favorites"));

    return new ActionMenu(action);
  }


  public ActionMenu getContextMenuActions(final Program program) {

    ArrayList favorites = new ArrayList();
    for (int i = 0; i < mFavoriteArr.length; i++) {
      Program[] programs = mFavoriteArr[i].getPrograms();
      for (int j = 0; j < programs.length; j++) {
        if (programs[j].equals(program)) {
          favorites.add(mFavoriteArr[i]);
          break;
        }
      }
    }

    if (favorites.isEmpty()) {
      ContextMenuAction menu = new ContextMenuAction();
      menu.setSmallIcon(getIconFromTheme("action", "bookmark-new", 16));
      menu.setText(mLocalizer.msg("contextMenuText", "Add to favorite programs"));
      menu.setActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          showCreateFavoriteWizard(program);
        }
      });
      return new ActionMenu(menu);
    }
    else {
      ContextMenuAction menu = new ContextMenuAction();
      menu.setText(mLocalizer.msg("manageFavorites", "Favorites"));

      ArrayList actions = new ArrayList();
      for (int i=0; i<favorites.size(); i++) {
        final Favorite fav = (Favorite)favorites.get(i);
        ContextMenuAction action = new ContextMenuAction();
        action.setText(mLocalizer.msg("exclude","Exclude from '{0}'...", fav.getName()));
        action.setActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            showExcludeProgramsDialog(fav, program);
          }
        });
        actions.add(action);
      }

      for (int i=0; i<favorites.size(); i++) {
        final Favorite fav = (Favorite)favorites.get(i);
        ContextMenuAction editAction = new ContextMenuAction();
        editAction.setText(mLocalizer.msg("edit","Edit '{0}'...", fav.getName()));
        editAction.setActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
           editFavorite(fav);
          }
        });
        actions.add(editAction);
      }
      return new ActionMenu(menu, (Action[])actions.toArray(new Action[actions.size()]));
    }


  }


  private void editFavorite(Favorite favorite) {

    Component parent = UiUtilities.getBestDialogParent(null);
    EditFavoriteDialog dlg;
    if (parent instanceof Dialog) {
      dlg = new EditFavoriteDialog((Dialog)parent, favorite);
    }
    else {
      dlg = new EditFavoriteDialog((Frame)parent, favorite);
    }
    UiUtilities.centerAndShow(dlg);
    if (dlg.getOkWasPressed()) {
      updateRootNode();
    }

  }

  private void showManageFavoritesDialog(final boolean showNew, Favorite[] favoriteArr) {
    int splitPanePosition = getIntegerSetting(mSettings, "splitpanePosition",
            200);
    int width = getIntegerSetting(mSettings, "width", 500);
    int height = getIntegerSetting(mSettings, "height", 300);
    ManageFavoritesDialog dlg;
    Container parent = UiUtilities.getBestDialogParent(null);
    if (parent instanceof Frame) {
      dlg = new ManageFavoritesDialog((Frame)parent, favoriteArr, splitPanePosition, showNew);
    }
    else {
      dlg = new ManageFavoritesDialog((Dialog)parent, favoriteArr, splitPanePosition, showNew);
    }
    dlg.setSize(new Dimension(width, height));
    
    if(mShowInfoOnNewProgramsFound) {
      final ManageFavoritesDialog dialog = dlg;    
    
      new Thread() {
        public void run() {
          while(!dialog.isVisible())
            try {
              Thread.sleep(100);
            }catch(Exception e){};
          if(showNew) {
            JCheckBox chb = new JCheckBox(mLocalizer.msg("dontShow","Don't show this description again."));
            Object[] o = {mLocalizer.msg("description","Favorites that contains new programs will be shown in this dialog.\nWhen you click on a Favorite you can see the new programs in the right list.\n\n"),
                chb
            };
            
            JOptionPane.showMessageDialog(dialog,o);
          
            if(chb.isSelected())
              mShowInfoOnNewProgramsFound = false;
        }
      }
    }.start();
    }
    UiUtilities.centerAndShow(dlg);

    if (!showNew) {
      mFavoriteArr = dlg.getFavorites();
      updateRootNode();
    }
    splitPanePosition = dlg.getSplitpanePosition();
    mSettings.setProperty("splitpanePosition", "" + splitPanePosition);
    mSettings.setProperty("width", "" + dlg.getWidth());
    mSettings.setProperty("height", "" + dlg.getHeight());
  }
  
  public boolean isUsingExpertMode() {
    return mSettings.getProperty("expertMode","false").compareTo("true") == 0;
  }
  
  public void setIsUsingExpertMode(boolean value) {
    mSettings.setProperty("expertMode",String.valueOf(value));
  }

  public void addFavorite(Favorite fav) {
    Favorite[] newFavoritesArr = new Favorite[mFavoriteArr.length + 1];
    System.arraycopy(mFavoriteArr, 0, newFavoritesArr, 0, mFavoriteArr.length);
    newFavoritesArr[mFavoriteArr.length] = fav;
    mFavoriteArr = newFavoritesArr;

    updateRootNode();

  }

   private void showCreateFavoriteWizard(Program program) {

    WizardHandler handler = new WizardHandler(UiUtilities.getBestDialogParent(null), new TypeWizardStep(program));
    tvbrowser.extras.favoritesplugin.core.Favorite fav = (tvbrowser.extras.favoritesplugin.core.Favorite)handler.show();
    if (fav != null) {
      try {
        fav.updatePrograms();
        addFavorite(fav);
      } catch (TvBrowserException exc) {
        ErrorHandler.handle(mLocalizer.msg("couldNotUpdateFavorites","Could not update favorites."), exc);
      }

    }
  }


  private void showExcludeProgramsDialog(Favorite fav, Program program) {
    WizardHandler handler = new WizardHandler(UiUtilities.getBestDialogParent(null), new ExcludeWizardStep(fav, program));
    Exclusion exclusion = (Exclusion) handler.show();
    if (exclusion != null) {
      fav.addExclusion(exclusion);
      try {
        fav.updatePrograms();
        updateRootNode();
      } catch (TvBrowserException exc) {
        ErrorHandler.handle(mLocalizer.msg("couldNotUpdateFavorites","Could not update favorites."), exc);
      }
    }
  }


  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "bookmark", 16);
  }


  public PluginTreeNode getRootNode() {
    return mRootNode;
  }

  public void updateRootNode() {

    mRootNode.removeAllChildren();

    for (int i=0; i<mFavoriteArr.length; i++) {
      PluginTreeNode n = new PluginTreeNode(mFavoriteArr[i].getName());
      Program[] progArr = mFavoriteArr[i].getPrograms();
      for (int j=0; j<progArr.length; j++) {
        n.addProgram(progArr[j]);
      }
      mRootNode.add(n);
    }

    mRootNode.update();
    ReminderPlugin.getInstance().updateRootNode();
  }
  
  


  public String[] getClientPluginIds() {
    return mClientPluginIdArr;
  }

  public void setClientPluginIds(String[] clientPluginArr) {
    mClientPluginIdArr = clientPluginArr;
  }

  public PluginAccess[] getDefaultClientPlugins() {
    ArrayList list = new ArrayList();
    for (int i=0; i<mClientPluginIdArr.length; i++) {
      PluginAccess plugin = Plugin.getPluginManager().getActivatedPluginForId(mClientPluginIdArr[i]);
      if (plugin != null && plugin.canReceivePrograms()) {
        list.add(plugin);
      }
    }
    return (PluginAccess[])list.toArray(new PluginAccess[list.size()]);
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




  public String getId() {
    return DATAFILE_PREFIX;
  }
}
