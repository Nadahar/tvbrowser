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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;

import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.ContextMenuIf;
import devplugin.Date;
import devplugin.NodeFormatter;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramItem;
import devplugin.ProgramReceiveTarget;

/**
 * The model for the favorite tree.
 *
 * @author René Mach
 * @since 2.6
 */
public class FavoriteTreeModel extends DefaultTreeModel {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(FavoriteTreeModel.class);

  private static FavoriteTreeModel mInstance;

  /**
   * Creates an instance of this class.
   *
   * @param root The root node for this model.
   */
  private FavoriteTreeModel(TreeNode root) {
    super(root, true);
  }

  public static FavoriteTreeModel initInstance(Favorite[] favoriteArr) {
    FavoriteNode rootNode = new FavoriteNode("");
    fixRootNode(rootNode);
    for(Favorite fav : favoriteArr) {
      rootNode.add(fav);
    }

    mInstance = new FavoriteTreeModel(rootNode);
    return mInstance;
  }

  public static FavoriteTreeModel initInstance(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    FavoriteNode rootNode = new FavoriteNode(in, version);
    fixRootNode(rootNode);
    mInstance = new FavoriteTreeModel(rootNode);
    return mInstance;
  }

  /**
   * change the label of the root node after it has been red from disk
   * @param rootNode
   */
  private static void fixRootNode(final FavoriteNode rootNode) {
    String rootLabel = mLocalizer.msg("rootLabel", "All favorites");
    if (StringUtils.isEmpty(rootLabel)) {
      rootLabel = "FAVORITES_ROOT";
    }
    rootNode.setUserObject(rootLabel);
  }

  public static FavoriteTreeModel getInstance() {
    if (mInstance == null) {
      mInstance = initInstance(new Favorite[0]);
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
   * @param fav
   *          The favorite to add.
   * @param parent
   *          The parent node to add the favorite to or <code>null</code> if the
   *          root node should be used.
   * @return the newly created node for the favorite
   */
  public FavoriteNode addFavorite(Favorite fav, FavoriteNode parent) {
    if (parent == null) {
      parent = (FavoriteNode) getRoot();
    }
    FavoriteNode newNode = parent.add(fav);
    reload(parent);
    FavoritesPlugin.getInstance().updateRootNode(true);
    return newNode;
  }

  public static String getFavoriteLabel(Favorite favorite, Program program) {
    return getFavoriteLabel(favorite, program, null);
  }

  public static String getFavoriteLabel(Favorite favorite, Program p, Channel currentChannel) {
    Date d = p.getDate();
    String progdate;

    Date currentDate = Date.getCurrentDate();

    if (d.equals(currentDate.addDays(-1))) {
      progdate = Localizer.getLocalization(Localizer.I18N_YESTERDAY);
    } else if (d.equals(currentDate)) {
      progdate = Localizer.getLocalization(Localizer.I18N_TODAY);
    } else if (d.equals(currentDate.addDays(1))) {
      progdate = Localizer.getLocalization(Localizer.I18N_TOMORROW);
    } else {
      progdate = p.getDateString();
    }

    String description = progdate + "  " + p.getTimeString();
    if(favorite.getName().compareTo(p.getTitle()) != 0) {
      description = description + "  " + p.getTitle();
    }
    String episode = p.getTextField(ProgramFieldType.EPISODE_TYPE);
    if (StringUtils.isNotBlank(episode)) {
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

  public void updatePluginTree(final PluginTreeNode node, final PluginTreeNode dateNode, FavoriteNode parentFavorite) {
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
          updatePluginTree(newNode, dateNode,child);
          if (!newNode.isEmpty()) {
            node.add(newNode);
          }
        } else {
          newNode.getMutableTreeNode().setIcon(FavoritesPlugin.getFavoritesIcon(16));

          Program[] progArr = child.getFavorite().getWhiteListPrograms();
          if (progArr.length > 0) {
            node.add(newNode);
            Action editFavorite = new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                FavoritesPlugin.getInstance().editFavorite(child.getFavorite());
              }
            };
            editFavorite.putValue(Action.NAME, mLocalizer.ellipsisMsg("editTree","Edit"));
            editFavorite.putValue(Action.SMALL_ICON, TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));

            Action deleteFavorite = new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                FavoritesPlugin.getInstance().askAndDeleteFavorite(child.getFavorite());
              }
            };
            deleteFavorite.putValue(Action.NAME, mLocalizer.ellipsisMsg("deleteTree","Delete"));
            deleteFavorite.putValue(Action.SMALL_ICON, TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
            deleteFavorite.putValue(ContextMenuIf.ACTIONKEY_KEYBOARD_EVENT,
                KeyEvent.VK_DELETE);

            newNode.addAction(editFavorite);
            newNode.addAction(deleteFavorite);


            if(progArr.length <= 10) {
              newNode.setGroupingByDateEnabled(false);
            }
            boolean episodeOnly = progArr.length > 1;
            for (Program program : progArr) {
              String episode = program.getTextField(ProgramFieldType.EPISODE_TYPE);
              if (StringUtils.isBlank(episode)) {
                episodeOnly = false;
                break;
              }
            }

            for (Program program : progArr) {
              PluginTreeNode pNode = newNode.addProgramWithoutCheck(program);
              dateNode.addProgram(program);
              if (episodeOnly || progArr.length <= 10) {
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
   * @param comp Comparator for sorting
   * @param title Title of confirmation message dialog
   */
  public void sort(FavoriteNode node, Comparator<FavoriteNode> comp, String title) {
    String msg = mLocalizer.msg("reallySort",
        "Do you really want to sort '{0}'?\n\nThe current order will get lost.", node.toString());
    int result = JOptionPane.showConfirmDialog(UiUtilities
        .getLastModalChildOf(MainFrame.getInstance()), msg, title,
        JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
      sortNodeInternal(node, comp);
    }

    ManageFavoritesDialog.getInstance().favoriteSelectionChanged();
  }

  /**
   * sort favorite nodes (dialog handling must be done by caller)
   * @param node
   * @param comp
   */
  private void sortNodeInternal(FavoriteNode node,
      Comparator<FavoriteNode> comp) {
    ArrayList<FavoriteNode> childNodes = Collections.list(node.children());
    Collections.sort(childNodes, comp);

    node.removeAllChildren();

    for(FavoriteNode child : childNodes) {
      node.add(child);
      if(child.isDirectoryNode()) {
        sortNodeInternal(child, comp);
      }
    }
  }

  /**
   * Gets the Favorites containing the given receive target in an array.
   *
   * @param target The target to check.
   * @return The Favorites that contains the given receive target in an array.
   */
  public Favorite[] getFavoritesContainingReceiveTarget(ProgramReceiveTarget target) {
    Favorite[] favorites = getFavoriteArr();

    ProgramReceiveTarget[] defaultTargets = FavoritesPlugin.getInstance().getDefaultClientPluginsTargets();

    for(ProgramReceiveTarget defaultTarget : defaultTargets) {
      if(defaultTarget.equals(target)) {
        return favorites;
      }
    }

    ArrayList<Favorite> receiveFavorites = new ArrayList<Favorite>();

    for(Favorite fav : favorites) {
      if(fav.containsReceiveTarget(target)) {
        receiveFavorites.add(fav);
      }
    }

    return receiveFavorites.toArray(new Favorite[receiveFavorites.size()]);
  }

  public void updatePluginTree(final PluginTreeNode topicNode, final PluginTreeNode dateNode) {
    updatePluginTree(topicNode, dateNode, null);
  }

  /**
   * get an array of all favorites containing the given program
   * @param program program to search for
   * @return array of favorites
   * @since 2.7
   */
  public Favorite[] getFavoritesContainingProgram(Program program) {
    ArrayList<Favorite> containing = new ArrayList<Favorite>();

    for (Favorite favorite : getFavoriteArr()) {
      for (Program favProgram : favorite.getPrograms()) {
        if (favProgram.equals(program)) {
          containing.add(favorite);
          break;
        }
      }
    }
    return containing.toArray(new Favorite[containing.size()]);
  }

  public boolean isInMultipleFavorites(final Program program) {
    int found = 0;
    for (Favorite favorite : getFavoriteArr()) {
      for (Program favProgram : favorite.getPrograms()) {
        if (favProgram.equals(program)) {
          found++;
          break;
        }
      }
      if (found >= 2) {
        return true;
      }
    }
    return false;
  }
}
