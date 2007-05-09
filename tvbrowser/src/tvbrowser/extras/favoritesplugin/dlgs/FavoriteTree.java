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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import devplugin.Program;

import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import util.ui.UiUtilities;

/**
 * The tree for the ManageFavoritesDialog.
 * 
 * @author René Mach
 * @since 2.6
 */
public class FavoriteTree extends JTree implements MouseListener, DragGestureListener,
DropTargetListener, DragSourceListener  {
  
  private static FavoriteTree mInstance;
  
  private FavoriteNode mRootNode;
  private FavoriteNode mTransferNode;

  /**
   * @deprecated Only used to load data from an old plugin version.
   * @param favoriteArr
   */
  private FavoriteTree(Favorite[] favoriteArr) {
    mInstance = this;
    
    mRootNode = new FavoriteNode("FAVORITES_ROOT");
    setModel(new FavoriteTreeModel(mRootNode));
    setRootVisible(false);
    
    
    for(Favorite fav : favoriteArr)
      mRootNode.add(fav);
    
    getModel().reload(mRootNode);
    addMouseListener(this);
    
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    (new DragSource()).createDefaultDragGestureRecognizer(this,
        DnDConstants.ACTION_MOVE, this);
    
    new DropTarget(this, this);
  }
  
  private FavoriteTree(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    mInstance = this;
        
    mRootNode = new FavoriteNode(in, version);
    
    setModel(new FavoriteTreeModel(mRootNode));    
    setRootVisible(false);
    
    expand(mRootNode);
    
    addMouseListener(this);
    
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    (new DragSource()).createDefaultDragGestureRecognizer(this,
        DnDConstants.ACTION_MOVE, this);
    
    new DropTarget(this, this);
  }
  
  /**
   * @deprecated Used only for loading data from an old plugin version
   * @param favoriteArr
   */
  public static void create(Favorite[] favoriteArr) {
    new FavoriteTree(favoriteArr);
  }
  
  /**
   * Creates the instance of this class from the given ObjectInputStream.
   * 
   * @param in The stream to read the data from.
   * @param version The file version of the data file.
   * @throws IOException Thrown if something went wrong.
   * @throws ClassNotFoundException Thrown if something went wrong.
   */
  public static void create(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    new FavoriteTree(in, version);
  }
  
  /**
   * Gets the instance of this class.
   * 
   * @return The instance of this class.
   */
  public static FavoriteTree getInstance() {
    if(mInstance == null) {
      new FavoriteTree(new Favorite[0]);
    }
    
    return mInstance;
  }
  
  /**
   * Saves the data of this tree into the given stream.
   * 
   * @param out The stream to write the data to.
   * @throws IOException Thrown if something went wrong
   */
  public void storeData(ObjectOutputStream out) throws IOException {
    ((FavoriteNode)getModel().getRoot()).store(out);
  }  
  
  /**
   * Adds a favorite to this tree at the given target node.
   * 
   * @param fav The favorite to add.
   * @param target The target node to add the favorite to or
   * <code>null</code> if the root node should be used.
   */
  public void addFavorite(Favorite fav, FavoriteNode target) {
    if(target == null)
      target = mRootNode;
    
    ((DefaultTreeModel)getModel()).reload(target.add(fav));
    FavoritesPlugin.getInstance().updateRootNode(true);
  }
  
  private void showContextMenu(MouseEvent e) {
    if(e.isPopupTrigger()) {
      JPopupMenu menu = new JPopupMenu();
      
      JMenuItem item = new JMenuItem("Neu");
      
      final TreePath path = getPathForLocation(e.getX(), e.getY());
      setSelectionPath(path);
      
      Object[] o = path.getPath();
      
      final FavoriteNode last = (FavoriteNode)path.getLastPathComponent();
      
      final StringBuffer pathValue = new StringBuffer();
      
      for(Object p : o) {
        pathValue.append(p.toString()).append(";;");
      }
      
      if(pathValue.length() > 2) {
        pathValue.delete(pathValue.length() - 2, pathValue.length());
      }
      
      item.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent arg0) {
          
          ManageFavoritesDialog.getInstance().newFavorite(last.isDirectoryNode() ? last : (FavoriteNode)last.getParent());
        }
        
      });
      
      if(((FavoriteNode)path.getLastPathComponent()).isDirectoryNode()) {
        menu.add(item);
        
        item = new JMenuItem("Namen ändern:");
        
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String value = JOptionPane.showInputDialog(UiUtilities.getLastModalChildOf(ManageFavoritesDialog.getInstance()), "Name:", last.getUserObject());
            
            if(value != null) {
              last.setUserObject(value);
              updateUI();
            }
          }
        });
        
        menu.add(item);
        
      }

      item = new JMenuItem("Neuer Ordner");      
      
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String value = JOptionPane.showInputDialog(UiUtilities.getLastModalChildOf(ManageFavoritesDialog.getInstance()), "Name:", "Neuer Ordner");
          
          if(value != null) {
            ((FavoriteNode)((FavoriteNode)path.getLastPathComponent()).getParent()).add(new FavoriteNode(value));
            ((DefaultTreeModel)getModel()).reload(((FavoriteNode)path.getLastPathComponent()).getParent());
          }
          
        }
      });
      
      menu.add(item);
      
      item = new JMenuItem("Löschen");
      
      item.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent arg0) {
          if(last.isDirectoryNode()) {
            FavoriteNode parent = (FavoriteNode)last.getParent();
          
            parent.remove(last);
            ((DefaultTreeModel)getModel()).reload(parent);
          }
          else if(last.containsFavorite())
            ManageFavoritesDialog.getInstance().deleteSelectedFavorite();
        }
        
      });
      
      item.setEnabled(last.getChildCount() < 1);
      
      menu.add(item);
      
      
      
      menu.show(this, e.getX(), e.getY());
    }
  }
  
  private void expand(FavoriteNode node) {
    Enumeration e = node.children();
    
    while(e.hasMoreElements()) {
      FavoriteNode child = (FavoriteNode)e.nextElement();
      
      if(child.isDirectoryNode())
        expand(child);
    }
    
    if(node.wasExpanded())
      this.expandPath(new TreePath(((DefaultTreeModel)this.getModel()).getPathToRoot(node)));
  }
  
  public FavoriteTreeModel getModel() {
    return (FavoriteTreeModel)super.getModel();
  }
  
  /**
   * Gets all favorites in an array.
   * 
   * @return All favorites in an array.
   */
  public Favorite[] getFavoriteArr() {
    ArrayList<Favorite> favoriteList = new ArrayList<Favorite>();
    
    fillFavoriteList(mRootNode, favoriteList);
    
    return favoriteList.toArray(new Favorite[favoriteList.size()]);
  }
  
  private void fillFavoriteList(FavoriteNode node, ArrayList<Favorite> favoriteList) {
    if(node.isDirectoryNode()) {
      Enumeration e = node.children();
      
      while(e.hasMoreElements()) {
        FavoriteNode child = (FavoriteNode)e.nextElement();
        
        if(child.isDirectoryNode())
          fillFavoriteList(child, favoriteList);
        else if(child.containsFavorite())
          favoriteList.add(child.getFavorite());
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
    for (int i=0; i<delFavPrograms.length; i++) {
      delFavPrograms[i].unmark(FavoritesPluginProxy.getInstance());
    }

    deleteFavorite(mRootNode, favorite);
    
    String[] reminderServices = favorite.getReminderConfiguration().getReminderServices();    
    
    for (int i=0; i<reminderServices.length; i++)
      if (ReminderConfiguration.REMINDER_DEFAULT.equals(reminderServices[i])) 
        ReminderPlugin.getInstance().removePrograms(favorite.getPrograms());
    
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
    return isContainedByOtherFavorites(mRootNode,favorite,p);
  }
  
  private boolean isContainedByOtherFavorites(FavoriteNode node,Favorite favorite, Program p) {
    boolean value = false;
    
    if(node.isDirectoryNode()) {
      Enumeration e = node.children();
      
      while(e.hasMoreElements()) {
        FavoriteNode child = (FavoriteNode)e.nextElement();
        
        if(child.isDirectoryNode())
          value = value || isContainedByOtherFavorites(child, favorite, p);
        else if(child.containsFavorite()) {
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
      Enumeration e = node.children();
      
      while(e.hasMoreElements()) {
        FavoriteNode child = (FavoriteNode)e.nextElement();
        
        if(child.isDirectoryNode())
          deleteFavorite(child, fav);
        else if(child.containsFavorite()) {
          if(child.equals(fav)) {
            ((FavoriteNode)child.getParent()).remove(child);
          }
          else {
            child.getFavorite().handleContainingPrograms(fav.getPrograms());
          }
        }
      }
    }
    
    clearSelection();
    updateUI();
  }
  
  /**
   * Adds a favorite to this tree at the root node.
   * 
   * @param fav The favorite to add.
   */
  public void addFavorite(Favorite fav) {
    addFavorite(fav, mRootNode);
  }
  
  /**
   * Gets the root node of this tree.
   * 
   * @return The root node of this tree.
   */
  public FavoriteNode getRoot() {
    return mRootNode;
  }
  
  public void mouseClicked(MouseEvent e) {
    if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
      if(((FavoriteNode)getLastSelectedPathComponent()).containsFavorite()) {
        ManageFavoritesDialog.getInstance().editSelectedFavorite();
      }
    }
  }

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {
    showContextMenu(e);
  }

  public void mouseReleased(MouseEvent e) {
    showContextMenu(e);
  }

  public void dragGestureRecognized(DragGestureEvent e) {
    mTransferNode = (FavoriteNode)getLastSelectedPathComponent();
    
    if(mTransferNode != null) {
      e.startDrag(null,new FavoriteTransferNode());
    }
  }

  public void dragEnter(DropTargetDragEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  public void dragExit(DropTargetEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  public void dragOver(DropTargetDragEvent e) {
    
    
  }

  public void drop(DropTargetDropEvent e) {
    e.acceptDrop(e.getDropAction());
    
    Transferable transfer = e.getTransferable();
    
    if(transfer.isDataFlavorSupported(new DataFlavor(TreePath.class, "FavoriteNodeExport")))
    try {
      FavoriteNode node = mTransferNode;
      FavoriteNode parent = (FavoriteNode)node.getParent();
      
      TreePath path = getPathForLocation(e.getLocation().x, e.getLocation().y);
      
      if(path == null)
        path = new TreePath(mRootNode);
      
      if(!new TreePath(node.getPath()).isDescendant(path)) {
        setSelectionPath(null);
        
      
        
        parent.remove(node);
        
      
        FavoriteNode last = (FavoriteNode)path.getLastPathComponent();
        
        if(last.containsFavorite())
          last = (FavoriteNode)last.getParent();
        
        
        
        last.add(node);
        
        
      }
      
      updateUI();
    }catch(Exception ex) {ex.printStackTrace();}
    
    e.dropComplete(true);
  }

  public void dropActionChanged(DropTargetDragEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  public void dragDropEnd(DragSourceDropEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  public void dragEnter(DragSourceDragEvent e) {
    
    
  }

  public void dragExit(DragSourceEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  public void dragOver(DragSourceDragEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  public void dropActionChanged(DragSourceDragEvent arg0) {
    // TODO Auto-generated method stub
    
  }
  
  private class FavoriteTransferNode implements Transferable {
    private DataFlavor mDataFlavor;
    
    /**
     * Creates an instance of this class.
     */
    public FavoriteTransferNode() {
      mDataFlavor = new DataFlavor(TreePath.class, "FavoriteNodeExport");
    }
    
    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
      return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] {mDataFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor df) {
      return df.getMimeType().equals(mDataFlavor.getMimeType()) && df.getHumanPresentableName().equals(mDataFlavor.getHumanPresentableName());
    }
    
  }
}
