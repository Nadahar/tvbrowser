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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.favoritesplugin.dlgs;

import devplugin.Channel;
import devplugin.Date;
import devplugin.NodeFormatter;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramItem;
import devplugin.ProgramReceiveTarget;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.UiUtilities;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * The model for the favorite tree.
 * 
 * @author Ren� Mach
 * @since 2.6
 */
public class FavoriteTreeModel extends DefaultTreeModel {
  private static Localizer mLocalizer = Localizer.getLocalizerFor(FavoriteTreeModel.class);

  private static FavoriteTreeModel mInstance;

  /**
   * private cache for current date, used during plugin tree update
   */
  private Date mCurrentDate;

  /**
   * Creates an instance of this class.
   * 
   * @param root The root node for this model. 
   */
  private FavoriteTreeModel(TreeNode root) {
    super(root, true);
  }

  public static FavoriteTreeModel initInstance(Favorite[] favoriteArr) {
    FavoriteNode rootNode = new FavoriteNode("FAVORITES_ROOT");

    for(Favorite fav : favoriteArr) {
      rootNode.add(fav);
    }

    mInstance = new FavoriteTreeModel(rootNode);
    return mInstance;
  }

  public static FavoriteTreeModel initInstance(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    FavoriteNode rootNode = new FavoriteNode(in, version);

    mInstance = new FavoriteTreeModel(rootNode);
    return mInstance;
  }

  public static FavoriteTreeModel getInstance() {
    if (mInstance == null) {
      FavoriteNode rootNode = new FavoriteNode("FAVORITES_ROOT");
      mInstance = new FavoriteTreeModel(rootNode);
    }

    return mInstance;
  }

  public void reload(TreeNode node) {
    super.reload(node);
    @SuppressWarnings("unchecked")
    Enumeration<FavoriteNode> e = node.children();
    
    while(e.hasMoreElements()) {
      FavoriteNode child = e.nextElement();
      
      if(child.isDirectoryNode()) {
        reload(child);
      }
    }
  }

  public void reload(FavoriteTree tree, TreeNode node) {
    super.reload(node);
    @SuppressWarnings("unchecked")
    Enumeration<FavoriteNode> e = node.children();

    while(e.hasMoreElements()) {
      FavoriteNode child = e.nextElement();

      if(child.isDirectoryNode()) {
        reload(tree, child);
      }
    }

    FavoriteNode parent = (FavoriteNode)node;

    if(parent.wasExpanded()) {
      tree.expandPath(new TreePath((tree.getModel()).getPathToRoot(node)));
    } else {
      tree.collapsePath(new TreePath((tree.getModel()).getPathToRoot(node)));
    }
  }


  public void reload() {
    reload(root);
  }
  
  public boolean isLeaf(Object nodeObject) {
    if (nodeObject instanceof FavoriteNode) {
      FavoriteNode node = (FavoriteNode) nodeObject;
      return node.getChildCount() == 0;
    }
    return super.isLeaf(nodeObject);
  }

  /**
   * Gets all favorites in an array.
   *
   * @return All favorites in an array.
   */
  public Favorite[] getFavoriteArr() {
    ArrayList<Favorite> favoriteList = new ArrayList<Favorite>();

    fillFavoriteList((FavoriteNode) getRoot(), favoriteList);

    return favoriteList.toArray(new Favorite[favoriteList.size()]);
  }

  private void fillFavoriteList(FavoriteNode node, ArrayList<Favorite> favoriteList) {
    if(node.isDirectoryNode()) {
      @SuppressWarnings("unchecked")
      Enumeration<FavoriteNode> e = node.children();

      while(e.hasMoreElements()) {
        FavoriteNode child = e.nextElement();

        if(child.isDirectoryNode()) {
          fillFavoriteList(child, favoriteList);
        } else if(child.containsFavorite()) {
          favoriteList.add(child.getFavorite());
        }
      }
    }
  }

  /**
   * Deletes a favorite.
   *
   * @param favorite The favorite to delete.
   */
  public void deleteFavorite(Favorite favorite) {
    Program[] delFavPrograms = favorite.getPrograms();
    for (Program program : delFavPrograms) {
      program.unmark(FavoritesPluginProxy.getInstance());
    }

    deleteFavorite((FavoriteNode) getRoot(), favorite);

    String[] reminderServices = favorite.getReminderConfiguration().getReminderServices();

    for (String reminderService : reminderServices) {
      if (ReminderConfiguration.REMINDER_DEFAULT.equals(reminderService)) {
        ReminderPlugin.getInstance().removePrograms(favorite.getPrograms());
      }
    }

    FavoritesPlugin.getInstance().updateRootNode(true);
  }

  /**
   * Check if a program is marked by other Favorites to.
   *
   * @param favorite The Favorite that wants to check this.
   * @param p The program to check.
   * @return True if the program was found in other Favorites than the given one.
   */
  public boolean isContainedByOtherFavorites(Favorite favorite, Program p) {
    return isContainedByOtherFavorites((FavoriteNode) getRoot(),favorite,p);
  }

  private boolean isContainedByOtherFavorites(FavoriteNode node,Favorite favorite, Program p) {
    boolean value = false;

    if(node.isDirectoryNode()) {
      @SuppressWarnings("unchecked")
      Enumeration<FavoriteNode> e = node.children();

      while(e.hasMoreElements()) {
        FavoriteNode child = e.nextElement();

        if(child.isDirectoryNode()) {
          value = value || isContainedByOtherFavorites(child, favorite, p);
        } else if(child.containsFavorite()) {
          if(!child.equals(favorite)) {
            value = value || child.getFavorite().contains(p);
          }
        }
      }
    }

    return value;
  }

  private void deleteFavorite(FavoriteNode node, Favorite fav) {
    if(node.isDirectoryNode()) {
      @SuppressWarnings("unchecked")
      Enumeration<FavoriteNode> e = node.children();

      while(e.hasMoreElements()) {
        FavoriteNode child = e.nextElement();

        if(child.isDirectoryNode()) {
          deleteFavorite(child, fav);
        } else if(child.containsFavorite()) {
          if(child.equals(fav)) {
            node.remove(child);
          }
          else {
            child.getFavorite().handleContainingPrograms(fav.getPrograms());
          }
        }
      }
    }

  }

  /**
   * Adds a favorite to this tree at the root node.
   *
   * @param fav The favorite to add.
   */
  public void addFavorite(Favorite fav) {
    addFavorite(fav, (FavoriteNode) getRoot());
  }

  /**
   * Adds a favorite to this tree at the given target node.
   *
   * @param fav The favorite to add.
   * @param target The target node to add the favorite to or
   * <code>null</code> if the root node should be used.
   */
  public void addFavorite(Favorite fav, FavoriteNode target) {
    if(target == null) {
      target = (FavoriteNode) getRoot();
    }

    reload(target.add(fav));
    FavoritesPlugin.getInstance().updateRootNode(true);
  }

  public static String getFavoriteLabel(Favorite favorite, Program program) {
    return getFavoriteLabel(favorite, program, null);
  }

  public static String getFavoriteLabel(Favorite favorite, Program p, Channel currentChannel) {
    Date d = p.getDate();
    String progdate;

    Date currentDate = Date.getCurrentDate();
    if (d.equals(currentDate)) {
      progdate = mLocalizer.msg("today", "today");
    } else if (d.equals(currentDate.addDays(1))) {
      progdate = mLocalizer.msg("tomorrow", "tomorrow");
    } else {
      progdate = p.getDateString();
    }

    String description = progdate + "  " + p.getTimeString();
    if(favorite.getName().compareTo(p.getTitle()) != 0) {
      description = description + "  " + p.getTitle();
    }
    String episode = p.getTextField(ProgramFieldType.EPISODE_TYPE);
    if (episode != null && (! episode.trim().equalsIgnoreCase(""))) {
      if (episode.length()<=3) {
        episode = ProgramFieldType.EPISODE_TYPE.getLocalizedName() + " " + episode;
      }
      description = description + ": " + episode ;
    }
    if (null == currentChannel || currentChannel != p.getChannel()) {
      description = description + "  (" + p.getChannel() + ")";
    }
    return description;
  }

  /**
   * Saves the data of this tree into the given stream.
   *
   * @param out The stream to write the data to.
   * @throws IOException Thrown if something went wrong
   */
  public void storeData(ObjectOutputStream out) throws IOException {
    ((FavoriteNode)getRoot()).store(out);
  }

  public void updatePluginTree(PluginTreeNode node, FavoriteNode parentFavorite) {
    if(parentFavorite == null) {
      parentFavorite = (FavoriteNode) getRoot();
    }

    if(parentFavorite.isDirectoryNode()) {
      @SuppressWarnings("unchecked")
      Enumeration<FavoriteNode> e = parentFavorite.children();

      while(e.hasMoreElements()) {
        final FavoriteNode child = e.nextElement();

        PluginTreeNode newNode = new PluginTreeNode(child.toString());
        newNode.setGroupingByWeekEnabled(true);

        if(child.isDirectoryNode()) {
          updatePluginTree(newNode,child);
          if (!newNode.isEmpty()) {
            node.add(newNode);
          }
        } else {
          newNode.getMutableTreeNode().setIcon(IconLoader.getInstance().getIconFromTheme("apps","bookmark",16));
          
          Program[] progArr = child.getFavorite().getWhiteListPrograms();
          if (progArr.length > 0) {
            node.add(newNode);
            Action editFavorite = new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                FavoritesPlugin.getInstance().editFavorite(child.getFavorite());
              }
            };
            editFavorite.putValue(Action.NAME, mLocalizer.msg("editTree","Edit..."));
            editFavorite.putValue(Action.SMALL_ICON, FavoritesPlugin.getInstance().getIconFromTheme("actions", "document-edit", 16));

            Action deleteFavorite = new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                FavoritesPlugin.getInstance().askAndDeleteFavorite(child.getFavorite());
              }
            };
            deleteFavorite.putValue(Action.NAME, mLocalizer.msg("deleteTree","Delete..."));
            deleteFavorite.putValue(Action.SMALL_ICON, FavoritesPlugin.getInstance().getIconFromTheme("actions", "edit-delete", 16));

            newNode.addAction(editFavorite);
            newNode.addAction(deleteFavorite);


            if(progArr.length <= 10) {
              newNode.setGroupingByDateEnabled(false);
            }

            for (Program program : progArr) {
              PluginTreeNode pNode = newNode.addProgramWithoutCheck(program);

              int numberOfDays = program.getDate().getNumberOfDaysSince(mCurrentDate);
              if ((progArr.length <= 10) || (numberOfDays > 1)) {
                pNode.setNodeFormatter(new NodeFormatter() {
                  public String format(ProgramItem pitem) {
                    Program p = pitem.getProgram();
                    return FavoriteTreeModel.getFavoriteLabel(child.getFavorite(), p);
                  }
                });
              }
            }
          }
        }
      }
    }
  }

   /** Calculates the number of programs contained in the children
   *
   * @param node
   *          use this Node
   * @return Number of Child-Nodes
   */
  public static int[] getProgramsCount(FavoriteNode node) {
    int[] count = new int[2];

    Date currentDate = Date.getCurrentDate();
    if(node.containsFavorite()) {
      Program[] whiteListPrograms = node.getFavorite().getWhiteListPrograms();
      count[0] = whiteListPrograms.length;
      for(Program p : whiteListPrograms) {
        if(p.getDate().equals(currentDate)) {
          count[1]++;
        }
      }
    }

    for (int i = 0; i < node.getChildCount(); i++) {
      FavoriteNode child = (FavoriteNode)node.getChildAt(i);
      if (child.containsFavorite()) {
        Program[] whiteListPrograms = child.getFavorite().getWhiteListPrograms();
        count[0] += whiteListPrograms.length;

        for(Program p : whiteListPrograms) {
          if(p.getDate().equals(currentDate)) {
            count[1]++;
          }
        }
      } else {
        int[] countReturned = getProgramsCount(child);
        count[0] += countReturned[0];
        count[1] += countReturned[1];
      }
    }
    return count;
  }

  /**
   * Sorts the path from the given node to all leafs alphabetically.
   *
   * @param node The node to sort from.
   * @param start If this is called with the root sort node.
   * @param comp Comparator for sorting
   * @param title Title of confirmation message dialog
   */
  public void sort(FavoriteNode node, boolean start, Comparator<FavoriteNode> comp, String title) {
    int result = JOptionPane.YES_OPTION;

    if(start) {
      String msg = mLocalizer.msg("reallySort", "Do you really want to sort your " +
      "favorites?\n\nThe current order will get lost.");
      result = JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), msg, title, JOptionPane.YES_NO_OPTION);
    }

    if (result == JOptionPane.YES_OPTION) {
      FavoriteNode[] nodes = new FavoriteNode[node.getChildCount()];

      for(int i = 0; i < nodes.length; i++) {
        nodes[i] = (FavoriteNode)node.getChildAt(i);
      }

      node.removeAllChildren();

      Arrays.sort(nodes, comp);

      for(FavoriteNode child : nodes) {
        node.add(child);

        if(child.isDirectoryNode()) {
          sort(child, false, comp, title);
        }
      }
    }

    ManageFavoritesDialog.getInstance().favoriteSelectionChanged();
  }
  
  /**
   * Gets the Favorites containing the given receive target in an array.
   * 
   * @param target The target to check.
   * @return The Favorites that contains the given receive target in an array.
   */
  public Favorite[] getFavoritesContainingReceiveTarget(ProgramReceiveTarget target) {
    Favorite[] favorites = getFavoriteArr();
    ArrayList<Favorite> receiveFavorites = new ArrayList<Favorite>();
    
    for(Favorite fav : favorites) {
      if(fav.containsReceiveTarget(target)) {
        receiveFavorites.add(fav);
      }
    }
    
    return receiveFavorites.toArray(new Favorite[receiveFavorites.size()]);
  }

  public void updatePluginTree(PluginTreeNode rootNode) {
    mCurrentDate = Date.getCurrentDate();
    updatePluginTree(rootNode,null);
  }
    
}
