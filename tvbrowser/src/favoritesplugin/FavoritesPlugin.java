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

import java.io.ObjectInputStream;
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
  
  private Favorite[] mFavoritesArr;

  
  
  /**
   * Creates a new instance of FavoritesPlugin.
   */
  public FavoritesPlugin() {
    mFavoritesArr = new Favorite[0];
    
    mInstance = this;
  }

  
  
  public static FavoritesPlugin getInstance() {
    return mInstance;
  }
  
  
  
  public void loadData(ObjectInputStream in) {
    try {
      mFavoritesArr = (Favorite[]) in.readObject();
      
      for (int i = 0; i < mFavoritesArr.length; i++) {
        Iterator progIter = mFavoritesArr[i].getProgramIterator();
        while (progIter.hasNext()) {
          Program program = (Program) progIter.next();
          mark(program);
        }
      }
    } catch (Exception exc) {
      String msg = mLocalizer.msg( "error.1", "Error loading favorite list!\n({0})" , exc);
      ErrorHandler.handle(msg, exc);
    }
  }

  
  
  public Object storeData() {
    return mFavoritesArr;
  }
  
  

  /**
   * This method is invoked by the host-application if the user has choosen your
   * plugin from the menu.
   */
  public void execute() {
    ManageFavoritesDialog dlg = new ManageFavoritesDialog(parent, mFavoritesArr);
    UiUtilities.centerAndShow(dlg);
    
    if (dlg.getOkWasPressed()) {
      mFavoritesArr = dlg.getFavorites();
    }
  }

  
  
  public void execute(devplugin.Program[] programArr) {
    if (programArr.length == 1) {
      Favorite favorite = new Favorite();
      favorite.setTerm(programArr[0].getTitle());
      
      EditFavoriteDialog dlg = new EditFavoriteDialog(parent, favorite);
      dlg.centerAndShow();
      
      if (dlg.getOkWasPressed()) {
        Favorite[] newFavoritesArr = new Favorite[mFavoritesArr.length + 1];
        System.arraycopy(mFavoritesArr, 0, newFavoritesArr, 0, mFavoritesArr.length);
        newFavoritesArr[mFavoritesArr.length] = favorite;
        mFavoritesArr = newFavoritesArr;
      }
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

  
  
  void unmark(Program program) {
    if (program != null) {
      program.unmark(this);
    }
  }

  
  
  void mark(Program program) {
    if (program != null) {
      program.mark(this);
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
    for (int i = 0; i < mFavoritesArr.length; i++) {
      try {
        mFavoritesArr[i].updatePrograms();
      }
      catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }
    }
  }

}
