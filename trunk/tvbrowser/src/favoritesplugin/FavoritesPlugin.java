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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package favoritesplugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import util.ui.UiUtilities;
import util.exc.*;

import devplugin.*;

/**
 * Plugin for managing the favorite programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FavoritesPlugin extends Plugin {

  /** The localizer for this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(FavoritesPlugin.class);

  private static FavoritesPlugin mInstance;
  
  private Favorite[] mFavoriteArr;
  
  private Plugin[] mClientPluginArr;

  
  
  /**
   * Creates a new instance of FavoritesPlugin.
   */
  public FavoritesPlugin() {
    mFavoriteArr = new Favorite[0];
    mClientPluginArr = new Plugin[0];
    
    mInstance = this;
  }

  
  
  public static FavoritesPlugin getInstance() {
    return mInstance;
  }
  
  
  
  public void readData(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
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
        programArr[j].mark(this);        
      }
    }

    // Get the client plugins
    Plugin[] installedPluginArr = Plugin.getPluginManager().getInstalledPlugins();
    ArrayList clientPluginList = new ArrayList();
    size = in.readInt();
    for (int i = 0; i < size; i++) {
      String className = (String) in.readObject();

      // Get the plugin with the right class name
      Plugin plugin = null;
      for (int j = 0; j < installedPluginArr.length; j++) {
        if (className.equals(installedPluginArr[j].getClass().getName())) {
          plugin = installedPluginArr[j];
          break;
        }
      }
      
      // If the plugin was found -> add it to the list
      if (plugin != null) {
        clientPluginList.add(plugin);
      }
    }

    // copy the list into the array
    mClientPluginArr = new Plugin[clientPluginList.size()];
    clientPluginList.toArray(mClientPluginArr);
  }



  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version

    out.writeInt(mFavoriteArr.length);
    for (int i = 0; i < mFavoriteArr.length; i++) {
      mFavoriteArr[i].writeData(out);
    }
    
    out.writeInt(mClientPluginArr.length);
    for (int i = 0; i < mClientPluginArr.length; i++) {
      out.writeObject(mClientPluginArr[i].getClass().getName());
    }
  }
  
  

  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the menu.
   */
  public void execute() {
    ManageFavoritesDialog dlg = new ManageFavoritesDialog(parent, mFavoriteArr);
    UiUtilities.centerAndShow(dlg);
    
    if (dlg.getOkWasPressed()) {
      mFavoriteArr = dlg.getFavorites();
    }
  }

  
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the context menu.
   */
  public void execute(Program program) {
    Favorite favorite = new Favorite();
    favorite.setTerm(program.getTitle());

    EditFavoriteDialog dlg = new EditFavoriteDialog(parent, favorite);
    dlg.centerAndShow();

    if (dlg.getOkWasPressed()) {
      Favorite[] newFavoritesArr = new Favorite[mFavoriteArr.length + 1];
      System.arraycopy(mFavoriteArr, 0, newFavoritesArr, 0, mFavoriteArr.length);
      newFavoritesArr[mFavoriteArr.length] = favorite;
      mFavoriteArr = newFavoritesArr;
    }
  }
  
  
  
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("favoritesManager", "Manage favorite programs");
    String desc = mLocalizer.msg("description",
      "Automatically marks your favorite programs and passes them to other Plugins." );
    String author = "Til Schneider, www.murfman.de" ;
    
    return new PluginInfo(name, desc, author, new Version(1, 0));
  }
  
  
  
  public String getMarkIconName() {
    return "favoritesplugin/ThumbUp16.gif";
  }

  
  
  public String getContextMenuItemText() {
	return mLocalizer.msg("contextMenuText", "Add to favorite programs");
  }

  
  
  public String getButtonIconName() {
    return "favoritesplugin/ThumbUp16.gif";
  }
  
  
  
  public String getButtonText() {
  	return mLocalizer.msg( "manageFavorites", "Manage Favorites" );
  }

  
  
  void unmark(Program[] programArr) {
    // unmark all programs with this plugin
    for (int i = 0; i < programArr.length; i++) {
      programArr[i].unmark(this);    
    }
  }

  
  
  void mark(Program[] programArr) {
    // mark all programs with this plugin
    for (int i = 0; i < programArr.length; i++) {
      programArr[i].mark(this);    
    }

    // Pass the program list to all client plugins
    for (int i = 0; i < mClientPluginArr.length; i++) {
      mClientPluginArr[i].execute(programArr);
    }
  }

  
  
  /**
   * This method is automatically called, when the TV data has changed.
   * (E.g. after an update).
   * <p>
   * Updates all favorites.
   */
  public void handleTvDataChanged() {
    // Update all favorites
    for (int i = 0; i < mFavoriteArr.length; i++) {
      try {
        mFavoriteArr[i].updatePrograms();
      }
      catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }
  }
  
  
  
  public Plugin[] getClientPlugins() {
    return mClientPluginArr;
  }
  
  
  
  public void setClientPlugins(Plugin[] clientPluginArr) {
    mClientPluginArr = clientPluginArr;
  }

  
  
  /**
   * Returns a new SettingsTab object, which is added to the settings-window.
   */
  public SettingsTab getSettingsTab() {
    return new FavoritesSettingTab();
  }
  
}
