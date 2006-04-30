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
 *     $Date: 2006-04-30 16:47:46 +0200 (Sun, 30 Apr 2006) $
 *   $Author: darras $
 * $Revision: 2296 $
 */

package tvbrowser.extras.favoritesplugin;

import devplugin.ActionMenu;
import devplugin.Program;
import devplugin.ContextMenuAction;

import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import tvbrowser.extras.favoritesplugin.core.Favorite;

import javax.swing.*;

public class ContextMenuProvider {

  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer
          .getLocalizerFor(ContextMenuProvider.class);

  private Favorite[] mFavoriteArr;

  public ContextMenuProvider(Favorite[] favoriteArr) {
    mFavoriteArr = favoriteArr;

  }

  public ActionMenu getContextMenuActions(Program program) {

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
        return createAddToFavoritesActionMenu(program);
      }
      else {
        Favorite[] favArr = (Favorite[])favorites.toArray(new Favorite[favorites.size()]);
        ContextMenuAction menu = new ContextMenuAction();
        menu.setText(mLocalizer.msg("favorites", "Favorites"));
        menu.setSmallIcon(FavoritesPlugin.getInstance().getIconFromTheme("apps", "bookmark", 16));

        return new ActionMenu(menu, new ActionMenu[]{
          createExcludeFromFavoritesMenuAction(favArr, program),
          createEditFavoriteMenuAction(favArr),
          createDeleteFavoriteMenuAction(favArr)
        });
      }

    }

  public ImageIcon getIconFromTheme(String category, String icon, int size) {
    return FavoritesPlugin.getInstance().getIconFromTheme(category, icon, size);
  }


  private ActionMenu createAddToFavoritesActionMenu(final Program program) {
    ContextMenuAction menu = new ContextMenuAction();
      menu.setSmallIcon(getIconFromTheme("action", "bookmark-new", 16));
      menu.setText(mLocalizer.msg("addToFavorites", "Add to favorite programs"));
      menu.setActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          FavoritesPlugin.getInstance().showCreateFavoriteWizard(program);
        }
      });
      return new ActionMenu(menu);
  }


  private ActionMenu createExcludeFromFavoritesMenuAction(final Favorite[] favArr, final Program program) {

    if (favArr.length == 1) {
      ContextMenuAction action = new ContextMenuAction();
      action.setSmallIcon(getIconFromTheme("apps", "bookmark", 16));
      action.setText(mLocalizer.msg("excludeFromFavorite","Exclude from '{0}'...", favArr[0].getName()));
      action.setActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          FavoritesPlugin.getInstance().showExcludeProgramsDialog(favArr[0], program);
        }
      });
      return new ActionMenu(action);
    }
    else {
      ContextMenuAction menu = new ContextMenuAction();
      menu.setText(mLocalizer.msg("excludeFrom","Exclude from"));
      menu.setSmallIcon(getIconFromTheme("apps", "bookmark", 16));
      ContextMenuAction[] subItems = new ContextMenuAction[favArr.length];
      for (int i=0; i<subItems.length; i++) {
        final Favorite fav = favArr[i];
        subItems[i] = new ContextMenuAction(favArr[i].getName());
        subItems[i].setSmallIcon(getIconFromTheme("apps", "bookmark", 16));
        subItems[i].setActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            FavoritesPlugin.getInstance().showExcludeProgramsDialog(fav, program);
          }
        });
      }

      return new ActionMenu(menu, subItems);
    }
  }


  private ActionMenu createEditFavoriteMenuAction(final Favorite[] favArr) {
    if (favArr.length == 1) {
      ContextMenuAction action = new ContextMenuAction();
      action.setSmallIcon(getIconFromTheme("action", "document-edit", 16));
      action.setText(mLocalizer.msg("editFavorite","Edit favorite '{0}'...", favArr[0].getName()));
      action.setActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          FavoritesPlugin.getInstance().editFavorite(favArr[0]);
        }
      });
      return new ActionMenu(action);
    }
    else {
      ContextMenuAction menu = new ContextMenuAction();
      menu.setSmallIcon(getIconFromTheme("action", "document-edit", 16));
      menu.setText(mLocalizer.msg("edit","Edit Favorite"));
      ContextMenuAction[] subItems = new ContextMenuAction[favArr.length];
      for (int i=0; i<subItems.length; i++) {
        final Favorite fav = favArr[i];
        subItems[i] = new ContextMenuAction(favArr[i].getName());
        subItems[i].setSmallIcon(getIconFromTheme("apps", "bookmark", 16));
        subItems[i].setActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            FavoritesPlugin.getInstance().editFavorite(fav);
          }
        });
      }

      return new ActionMenu(menu, subItems);
    }
  }


  private ActionMenu createDeleteFavoriteMenuAction(final Favorite[] favArr) {
      if (favArr.length == 1) {
        ContextMenuAction action = new ContextMenuAction();
        action.setSmallIcon(getIconFromTheme("actions", "edit-delete", 16));
        action.setText(mLocalizer.msg("deleteFavorite","Delete Favorite '{0}'...", favArr[0].getName()));
        action.setActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            FavoritesPlugin.getInstance().askAndDeleteFavorite(favArr[0]);
          }
        });
        return new ActionMenu(action);
      }
      else {
        ContextMenuAction menu = new ContextMenuAction();
        menu.setText(mLocalizer.msg("delete","Delete Favorite"));
        menu.setSmallIcon(getIconFromTheme("actions", "edit-delete", 16));
        ContextMenuAction[] subItems = new ContextMenuAction[favArr.length];
        for (int i=0; i<subItems.length; i++) {
          final Favorite fav = favArr[i];
          subItems[i] = new ContextMenuAction(favArr[i].getName());
          subItems[i].setSmallIcon(getIconFromTheme("apps", "bookmark", 16));
          subItems[i].setActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
              FavoritesPlugin.getInstance().askAndDeleteFavorite(fav);
            }
          });
        }

        return new ActionMenu(menu, subItems);
      }
    }



}
