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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesDialog;
import tvbrowser.extras.programinfo.ProgramInfo;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.TVBrowserIcons;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.ContextMenuSeparatorAction;
import devplugin.Program;

public class ContextMenuProvider {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
          .getLocalizerFor(ContextMenuProvider.class);

  private Favorite[] mFavoriteArr;

  public ContextMenuProvider(Favorite[] favoriteArr) {
    mFavoriteArr = favoriteArr;

  }

  public ActionMenu getContextMenuActions(Program program) {
      ArrayList<Favorite> favorites = new ArrayList<Favorite>();
      for (Favorite favorite : mFavoriteArr) {
        Program[] programs = favorite.getPrograms();
        for (Program favProgram : programs) {
          if (favProgram.equals(program)) {
            favorites.add(favorite);
            break;
          }
        }
      }

      Favorite[] favArr = favorites.toArray(new Favorite[favorites.size()]);

      if(ManageFavoritesDialog.getInstance() != null && ManageFavoritesDialog.getInstance().isVisible()) {
        if(!favorites.isEmpty()) {         
          ActionMenu blackListAction = createBlackListFavoriteMenuAction(favArr, program);
          
          ArrayList<Object> subItems = new ArrayList<Object>(2);
          subItems.add(createExcludeFromFavoritesMenuAction(favArr, program));
          
          if(blackListAction != null) {
            subItems.add(0,blackListAction);
          }
          
          return new ActionMenu(mLocalizer.msg("favorites", "Favorites"),FavoritesPlugin.getFavoritesIcon(16),subItems.toArray());
        }
        else {
          return null;
        }
      }
      else {
        if (favorites.isEmpty()) {
          return new ActionMenu(mLocalizer.msg("favorites", "Favorites"), FavoritesPlugin.getFavoritesIcon(16), new ActionMenu[] {
            createAddToFavoritesActionMenu(program),
              createGlobalExclusionMenu(program)
              });
        }
        else {
          ActionMenu blackListAction = createBlackListFavoriteMenuAction(favArr, program);
          ActionMenu repetitions = FavoritesPlugin.getInstance().isShowingRepetitions() ? createRepetitionsMenuAction(favArr, program) : null;
  
          ArrayList<Object> subItems = new ArrayList<Object>(8);
          subItems.add(createManageFavoriteMenuAction(favArr));
          subItems.add(createEditFavoriteMenuAction(favArr));
          subItems.add(createExcludeFromFavoritesMenuAction(favArr, program));
          subItems.add(createDeleteFavoriteMenuAction(favArr));
          subItems.add(ContextMenuSeparatorAction.getInstance());
          subItems.add(createGlobalExclusionMenu(program));
          subItems.add(createAddToFavoritesActionMenu(program));
          if (repetitions != null) {
            subItems.add(3, repetitions);
          }
          if(blackListAction != null) {
            subItems.add(1, blackListAction);
          }
          return new ActionMenu(mLocalizer.msg("favorites", "Favorites"), FavoritesPlugin.getFavoritesIcon(16), subItems.toArray());
        }
      }
    }

  public ImageIcon getIconFromTheme(String category, String icon, int size) {
    return FavoritesPlugin.getIconFromTheme(category, icon, size);
  }

  private ActionMenu createGlobalExclusionMenu(final Program program) {
    ContextMenuAction menu = new ContextMenuAction();
    menu.setSmallIcon(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    menu.setText(mLocalizer.ellipsisMsg("createGlobalExclusion", "Create global exclusion"));
    menu.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        FavoritesPlugin.getInstance().showExcludeProgramsDialog(null,program);
      }
    });
    return new ActionMenu(menu);
  }

  private ActionMenu createAddToFavoritesActionMenu(final Program program) {
    ContextMenuAction menu = new ContextMenuAction();
      menu.setSmallIcon(FavoritesPlugin.getFavoritesIcon(16));
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
      action.setSmallIcon(TVBrowserIcons.filter(TVBrowserIcons.SIZE_SMALL));
      action.setText(mLocalizer.ellipsisMsg("excludeFromFavorite","Exclude from '{0}'", favArr[0].getName()));
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
      menu.setSmallIcon(TVBrowserIcons.filter(TVBrowserIcons.SIZE_SMALL));
      ContextMenuAction[] subItems = new ContextMenuAction[favArr.length];
      for (int i=0; i<subItems.length; i++) {
        final Favorite fav = favArr[i];
        subItems[i] = new ContextMenuAction(favArr[i].getName());
        subItems[i].setSmallIcon(FavoritesPlugin.getFavoritesIcon(16));
        subItems[i].setActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            FavoritesPlugin.getInstance().showExcludeProgramsDialog(fav, program);
          }
        });
      }

      return new ActionMenu(menu, subItems);
    }
  }

  private ActionMenu createManageFavoriteMenuAction(final Favorite[] favArr) {
    if (favArr.length == 1) {
      ContextMenuAction action = new ContextMenuAction();
      action.setSmallIcon(FavoritesPlugin.getFavoritesIcon(16));
      action.setText(mLocalizer.ellipsisMsg("manageFavorite","Manage favorite '{0}'", favArr[0].getName()));
      action.setActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          FavoritesPlugin.getInstance().showManageFavoritesDialog(favArr[0]);
        }
      });
      return new ActionMenu(action);
    }
    else {
      ContextMenuAction menu = new ContextMenuAction();
      menu.setSmallIcon(FavoritesPlugin.getFavoritesIcon(16));
      menu.setText(mLocalizer.msg("manage","Manage Favorite"));
      ContextMenuAction[] subItems = new ContextMenuAction[favArr.length];
      for (int i=0; i<subItems.length; i++) {
        final Favorite fav = favArr[i];
        subItems[i] = new ContextMenuAction(favArr[i].getName());
        subItems[i].setSmallIcon(FavoritesPlugin.getFavoritesIcon(16));
        subItems[i].setActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            FavoritesPlugin.getInstance().showManageFavoritesDialog(fav);
          }
        });
      }

      return new ActionMenu(menu, subItems);
    }
  }

  private ActionMenu createEditFavoriteMenuAction(final Favorite[] favArr) {
    if (favArr.length == 1) {
      ContextMenuAction action = new ContextMenuAction();
      action.setSmallIcon(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
      action.setText(mLocalizer.ellipsisMsg("editFavorite","Edit favorite '{0}'", favArr[0].getName()));
      action.setActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          FavoritesPlugin.getInstance().editFavorite(favArr[0]);
        }
      });
      return new ActionMenu(action);
    }
    else {
      ContextMenuAction menu = new ContextMenuAction();
      menu.setSmallIcon(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
      menu.setText(mLocalizer.msg("edit","Edit Favorite"));
      ContextMenuAction[] subItems = new ContextMenuAction[favArr.length];
      for (int i=0; i<subItems.length; i++) {
        final Favorite fav = favArr[i];
        subItems[i] = new ContextMenuAction(favArr[i].getName());
        subItems[i].setSmallIcon(FavoritesPlugin.getFavoritesIcon(16));
        subItems[i].setActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            FavoritesPlugin.getInstance().editFavorite(fav);
          }
        });
      }

      return new ActionMenu(menu, subItems);
    }
  }

  private ActionMenu createRepetitionsMenuAction(final Favorite[] favorites, Program p) {
    ContextMenuAction topMenu = new ContextMenuAction();
    topMenu.setSmallIcon(TVBrowserIcons.search(TVBrowserIcons.SIZE_SMALL));
    topMenu.setText(mLocalizer.msg("repetitions", "More programs"));

    if (favorites.length==1) {
      return createFavoriteRepetitionMenu(topMenu,favorites[0], p);
    }
    else {
      ArrayList<ActionMenu> menus = new ArrayList<ActionMenu>();

      for (Favorite favorite : favorites) {
        ContextMenuAction subItem = new ContextMenuAction(favorite.getName());
        ActionMenu menu = createFavoriteRepetitionMenu(subItem,favorite, p);

        if(menu != null) {
          menus.add(menu);
        }
      }

      return menus.isEmpty() ? null : new ActionMenu(topMenu, menus.toArray(new ActionMenu[menus.size()]));
    }
  }

  private ActionMenu createFavoriteRepetitionMenu(ContextMenuAction parent, Favorite favorite, Program p) {
    Program[] programs = favorite.getPrograms();

    if(programs == null || (programs.length == 1 && programs[0].equals(p))) {
      return null;
    }

    ArrayList<ContextMenuAction> subItems = new ArrayList<ContextMenuAction>();

    for (final Program program : programs) {
      if(!program.isExpired() && !program.equals(p)) {
        ContextMenuAction subItem = new ContextMenuAction(FavoriteTreeModel.getFavoriteLabel(favorite, program, p.getChannel()));
        subItem.setActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            MainFrame.getInstance().scrollToProgram(program);
            if (ProgramInfo.isShowing()) {
              ProgramInfo.getInstance().showProgramInformation(program);
            }
          }
        });

        subItems.add(subItem);
        if (subItems.size() >= 30) {
          break;
        }
      }
    }

    // maybe all other repetitions were already expired?
    if (subItems.size() == 0) {
      return null;
    }

    return new ActionMenu(parent, subItems.toArray(new ContextMenuAction[subItems.size()]));
  }

  private ActionMenu createDeleteFavoriteMenuAction(final Favorite[] favArr) {
      if (favArr.length == 1) {
        ContextMenuAction action = new ContextMenuAction();
        action.setSmallIcon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
        action.setText(mLocalizer.ellipsisMsg("deleteFavorite","Delete Favorite '{0}'", favArr[0].getName()));
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
        menu.setSmallIcon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
        ContextMenuAction[] subItems = new ContextMenuAction[favArr.length];
        for (int i=0; i<subItems.length; i++) {
          final Favorite fav = favArr[i];
          subItems[i] = new ContextMenuAction(favArr[i].getName());
          subItems[i].setSmallIcon(FavoritesPlugin.getFavoritesIcon(16));
          subItems[i].setActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
              FavoritesPlugin.getInstance().askAndDeleteFavorite(fav);
            }
          });
        }

        return new ActionMenu(menu, subItems);
      }
    }


  private ActionMenu createBlackListFavoriteMenuAction(final Favorite[] favArr, final Program program) {
    if (favArr.length == 1) {
      ContextMenuAction action = new ContextMenuAction();

      if(favArr[0].isOnBlackList(program)) {
        action.setSmallIcon(TVBrowserIcons.refresh(TVBrowserIcons.SIZE_SMALL));
        action.setText(mLocalizer.msg("removeFavoriteFromBlackList","Put this program back into '{0}'", favArr[0].getName()));
        action.setActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            favArr[0].removeFromBlackList(program);
          }
        });

        return (new ActionMenu(action));
      } else {
        return null;
      }
    }
    else {
      ArrayList<Favorite> fromList = new ArrayList<Favorite>();

      for (Favorite favorite : favArr) {
        if(favorite.isOnBlackList(program)) {
          fromList.add(favorite);
        }
      }

      ContextMenuAction reactivate = new ContextMenuAction(mLocalizer.ellipsisMsg("removeFromBlackList",
          "Put this program back into"));
      reactivate.setSmallIcon(TVBrowserIcons.refresh(TVBrowserIcons.SIZE_SMALL));

      ContextMenuAction[] reactivateAction = new ContextMenuAction[fromList.size()];

      for(int i = 0; i < fromList.size(); i++) {
        final Favorite fav = fromList.get(i);
        reactivateAction[i] = new ContextMenuAction(fav.getName());
        reactivateAction[i].setSmallIcon(FavoritesPlugin.getFavoritesIcon(16));
        reactivateAction[i].setActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            fav.removeFromBlackList(program);
          }
        });
      }

      if(!fromList.isEmpty()) {
        return new ActionMenu(reactivate,reactivateAction);
      } else {
        return null;
      }
    }
  }

}
