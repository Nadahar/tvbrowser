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

package favoritesplugin;

import java.io.*;
import java.util.Properties;
import java.awt.Dimension;

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
  
  /** The IDs of the plugins that should receive the favorites. */
  private String[] mClientPluginIdArr;
  
  private Properties mSettings;

  
  
  /**
   * Creates a new instance of FavoritesPlugin.
   */
  public FavoritesPlugin() {
    mFavoriteArr = new Favorite[0];
    mClientPluginIdArr = new String[0];
    
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
  }



  public void writeData(ObjectOutputStream out) throws IOException {
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
   public void loadSettings(Properties settings) {
     mSettings=settings;     
     if (settings==null) {
       throw new IllegalArgumentException("settings is null"); 
     }
   }
  
  
  
   /**
    * Called by the host-application during shut-down. Implements this method to
    * store your plugins settings to the file system.
    */
   public Properties storeSettings() {
     return mSettings;
   }
   
   
   private int getIntegerSetting(Properties prop, String key, int defaultValue) {
     int res=defaultValue;
     try {
       res=Integer.parseInt(mSettings.getProperty(key,""+defaultValue));
     }catch (NumberFormatException e) {       
     }
      return res;
   }

  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the menu.
   */
  public void execute() {
    int splitPanePosition=getIntegerSetting(mSettings,"splitpanePosition",200);
    int width=getIntegerSetting(mSettings,"width",500);
    int height=getIntegerSetting(mSettings,"height",300);
    ManageFavoritesDialog dlg = new ManageFavoritesDialog(getParentFrame(), mFavoriteArr,splitPanePosition);
    dlg.setSize(new Dimension(width,height));
    UiUtilities.centerAndShow(dlg);
    
    if (dlg.getOkWasPressed()) {
      mFavoriteArr = dlg.getFavorites();
    }
    splitPanePosition=dlg.getSplitpanePosition();
    mSettings.setProperty("splitpanePosition",""+splitPanePosition);
    mSettings.setProperty("width",""+dlg.getWidth());
    mSettings.setProperty("height",""+dlg.getHeight());
  }

  
  
  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the context menu.
   */
  public void execute(Program program) {
    Favorite favorite = new Favorite(program.getTitle());

    EditFavoriteDialog dlg = new EditFavoriteDialog(getParentFrame(), favorite);
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
    
    return new PluginInfo(name, desc, author, new Version(1, 6));
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
    for (int i = 0; i < mClientPluginIdArr.length; i++) {
      PluginAccess plugin = getPluginManager().getActivatedPluginForId(mClientPluginIdArr[i]);
      if (plugin != null) {
        plugin.receivePrograms(programArr);
      }
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
  
}
