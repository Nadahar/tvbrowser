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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import devplugin.Program;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * The tree for the ManageFavoritesDialog.
 * 
 * @author René Mach
 * @since 2.6
 */
public class FavoriteTree extends JTree implements MouseListener, DragGestureListener,
DropTargetListener {
  private static Localizer mLocalizer = Localizer.getLocalizerFor(FavoriteTree.class);
  
  private static FavoriteTree mInstance;
  
  private FavoriteNode mRootNode;
  private FavoriteNode mTransferNode;
  private Rectangle2D mCueLine = new Rectangle2D.Float();
  
  private FavoriteNode mTargetNode;
  private int mTarget;
  private long mMousePressedTime;

  protected static final DataFlavor FAVORITE_FLAVOR = new DataFlavor(TreePath.class, "FavoriteNodeExport");
  
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
  public void addFavorite(Favorite fav, FavoriteNode target) {System.out.println("hier");
    if(target == null)
      target = mRootNode;
    
    ((DefaultTreeModel)getModel()).reload(target.add(fav));
    FavoritesPlugin.getInstance().updateRootNode(true);
  }
  
  private void showContextMenu(MouseEvent e) {
    if(e.isPopupTrigger()) {
      JPopupMenu menu = new JPopupMenu();      
      TreePath path1 = getPathForLocation(e.getX(), e.getY());
      
      if(path1 == null)
        path1 = new TreePath(mRootNode);
      
      final TreePath path = path1;
      final FavoriteNode last = (FavoriteNode)path.getLastPathComponent();

      setSelectionPath(path);
      
      JMenuItem item;
      
      if(last.isDirectoryNode()  && !last.equals(mRootNode)) {
        item = new JMenuItem(isExpanded(path) ? mLocalizer.msg("collapse", "Collapse") : mLocalizer.msg("expand", "Expand"));
        
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if(isExpanded(path))
              collapsePath(path);
            else
              expandPath(path);
          }
        });
        
        menu.add(item);
        
        item = new JMenuItem(mLocalizer.msg("expandAll", "Expand all"));
        
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            expandAll(last);
          }
        });
        
        menu.add(item);

        item = new JMenuItem(mLocalizer.msg("collapseAll", "Collapse all"));
        
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            collapseAll(last);
          }
        });
        
        menu.add(item);

        menu.addSeparator();
      }
      
      
      
      
      item = new JMenuItem(mLocalizer.msg("newFavorite", "New Favorite"),
          FavoritesPlugin.getInstance().getIconFromTheme("actions", "document-new", 16));
      
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ManageFavoritesDialog.getInstance().newFavorite(last.isDirectoryNode() ? last : (FavoriteNode)last.getParent());
        }
      });
      
      menu.add(item);
      
      if(last.isDirectoryNode() && !last.equals(mRootNode)) {
        item = new JMenuItem(mLocalizer.msg("renameFolder", "Rename folder"),
            IconLoader.getInstance().getIconFromTheme("actions", "document-edit", 16));
        
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
        
        if(last.getChildCount() > 0) {
          item = new JMenuItem(mLocalizer.msg("sort", "Sort"),
              IconLoader.getInstance().getIconFromTheme("actions", "sort-list", 16));
          
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sort(last,true);
                getModel().reload(last);
            }
          });
          
          menu.add(item);
        }
      }
      else {
        item = new JMenuItem(mLocalizer.msg("editFavorite", "Edit favorite"),
            IconLoader.getInstance().getIconFromTheme("actions", "document-edit", 16));
        
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ManageFavoritesDialog.getInstance().editSelectedFavorite();
          }
        });
        
        menu.add(item);
      }

      item = new JMenuItem(mLocalizer.msg("newFolder", "New folder"),
          IconLoader.getInstance().getIconFromTheme("actions", "folder-new", 16));      
      
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          newFolder(last);
        }
      });
      
      menu.add(item);
      
      item = new JMenuItem(Localizer.getLocalization(Localizer.I18N_DELETE),
          IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16));
      
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
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
      
      if(!last.equals(mRootNode))
        menu.add(item);
      
      menu.show(this, e.getX(), e.getY());
    }
  }
  
  private void expandAll(FavoriteNode node) {
    if(node.isDirectoryNode()) {
      expandPath(new TreePath(node.getPath()));
      
      for(int i = 0; i < node.getChildCount(); i++) {
        FavoriteNode child = (FavoriteNode)node.getChildAt(i);
        
        if(child.isDirectoryNode()) {
          expandPath(new TreePath(child.getPath()));
          expandAll(child);
        }
      }
    }
  }
  
  private void collapseAll(FavoriteNode node) {
    if(node.isDirectoryNode()) {
      for(int i = 0; i < node.getChildCount(); i++) {
        FavoriteNode child = (FavoriteNode)node.getChildAt(i);
        
        if(child.isDirectoryNode()) {
          collapseAll(child);
          collapsePath(new TreePath(child.getPath()));
        }
      }
      collapsePath(new TreePath(node.getPath()));
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
  
  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {
    showContextMenu(e);
    mMousePressedTime = e.getWhen();
  }

  public void mouseReleased(MouseEvent e) {
    showContextMenu(e);
    
    if(SwingUtilities.isLeftMouseButton(e)) {
      TreePath path = getPathForLocation(e.getX(), e.getY());
      
      if(((FavoriteNode)path.getLastPathComponent()).containsFavorite()) {
        if(e.getClickCount() >= 2)
          ManageFavoritesDialog.getInstance().editSelectedFavorite();
      }
      else if(((FavoriteNode)path.getLastPathComponent()).isDirectoryNode() && (e.getWhen() - mMousePressedTime) < 500) {
        if(!isExpanded(path)) {
          expandPath(path);
        }
        else {
          collapsePath(path);
        }
        this.setSelectionPath(path);
      }
    }

  }

  public void dragGestureRecognized(DragGestureEvent e) {
    mTransferNode = (FavoriteNode)getLastSelectedPathComponent();
    
    if(mTransferNode != null) {
      e.startDrag(null,new FavoriteTransferNode());
    }
  }

  public void dragEnter(DropTargetDragEvent e) {
    if(e.getCurrentDataFlavors().length > 1 || e.getCurrentDataFlavors().length < 1 || !e.getCurrentDataFlavors()[0].equals(FAVORITE_FLAVOR))
      e.rejectDrag();
    else {
      Graphics2D g2 = (Graphics2D) getGraphics();
      Color c = new Color(255, 0, 0, 40);
      g2.setColor(c);
      g2.fill(mCueLine);
    }
  }

  public void dragExit(DropTargetEvent e) {
    this.paintImmediately(mCueLine.getBounds());
  }
  
  private int getTargetFor(FavoriteNode node, Point p, int row) {
    Rectangle location = getRowBounds(getClosestRowForLocation(p.x, p.y));
    
    if(node.isDirectoryNode()) {
      if(row != getRowCount() && p.y - location.y <= (location.height / 4)) {
        return -1;
      }
      else if(row != getRowCount() && p.y - location.y <= (location.height - (location.height / 4))) {
        return 0;
      }
      else
        return 1;
    }
    else {
      if(p.y - location.y <= (location.height / 2)) {
        return -1;
      }
      else {
        return  1;
      }
    }    
  }
  
  public void dragOver(DropTargetDragEvent e) {
    if(e.getCurrentDataFlavors().length > 1 || e.getCurrentDataFlavors().length < 1 || !e.getCurrentDataFlavors()[0].equals(FAVORITE_FLAVOR)) {
      paintImmediately(mCueLine.getBounds());
      e.rejectDrag();
      return;
    }

    int row = getClosestRowForLocation(e.getLocation().x, e.getLocation().y);;
    
    Rectangle rowBounds = getRowBounds(row);
    
    if(rowBounds.y + rowBounds.height < e.getLocation().y)
      row = this.getRowCount();
    
    TreePath path = getPathForRow(row);
    
    if(path == null)
      path = new TreePath(mRootNode);
    
    if(mTransferNode != null && !new TreePath(mTransferNode.getPath()).isDescendant(path)) {
      e.acceptDrag(e.getDropAction());
      
      FavoriteNode last = (FavoriteNode)path.getLastPathComponent();
      FavoriteNode pointed = last;
      
      int target = getTargetFor(pointed, e.getLocation(), row);
      
      
      if(target == -1 || (target == 1 && !isExpanded(new TreePath(pointed.getPath())))) {
        if(!last.isRoot()) {        
          last = (FavoriteNode)last.getParent();
        }
      }      
      
      if(mTargetNode != last || mTarget != target) {
      
      mTargetNode = last;
      mTarget = target;
      
      this.paintImmediately(mCueLine.getBounds());
      
      int y = row != getRowCount() ? rowBounds.y : rowBounds.y + rowBounds.height;
            
      if(target == -1) {
        Rectangle rect = new Rectangle(0 + 20 * (last.getPath().length-1),y-1,getWidth(),2);
        
        mCueLine.setRect(rect);
      }
      else if(target == 0) {        
        Rectangle rect = new Rectangle(0,y,getWidth(),rowBounds.height);
          
        mCueLine.setRect(rect);
      }
      else if(target == 1) {
        Rectangle rect= new Rectangle(0 + 20 * (last.getPath().length-1),y + rowBounds.height-1,getWidth(),2);;
      
        if(row == getRowCount()) {
         rect = new Rectangle(0,y-1,getWidth(),2);
        } 
        mCueLine.setRect(rect);
      }
      
      Graphics2D g2 = (Graphics2D) getGraphics();
      Color c = new Color(255, 0, 0, 40);
      g2.setColor(c);
      g2.fill(mCueLine);
      }
    }
    else
      e.rejectDrag();
    
  }

  public void drop(DropTargetDropEvent e) {
    e.acceptDrop(e.getDropAction());
    this.paintImmediately(mCueLine.getBounds());
    Transferable transfer = e.getTransferable();
    
    if(transfer.isDataFlavorSupported(new DataFlavor(TreePath.class, "FavoriteNodeExport")))
    try {
      FavoriteNode node = mTransferNode;
      FavoriteNode parent = (FavoriteNode)node.getParent();
      
      int row = getClosestRowForLocation(e.getLocation().x, e.getLocation().y);;
      
      TreePath path = new TreePath(mRootNode);
      
      if(getRowBounds(row).y + getRowBounds(row).height < e.getLocation().y)
        row = this.getRowCount();
      else
        path = getPathForRow(row);

      FavoriteNode last = (FavoriteNode)path.getLastPathComponent();
      FavoriteNode pointed = last;   
      int target = getTargetFor(pointed, e.getLocation(), row);;


      
      
      
      if(path == null)
        path = new TreePath(mRootNode);
      
      if(!new TreePath(node.getPath()).isDescendant(path)) {
        setSelectionPath(null);
        
      
        
        parent.remove(node);
        
      
        
        
        int n = -1;
        
        if(target == -1 || (target == 1 && !isExpanded(new TreePath(pointed.getPath())))) {
          if(last.isRoot())
            n = 0;
          else {
            n = last.getParent().getIndex(last);
            last = (FavoriteNode)last.getParent();
          }
        }
        
        if(target == -1)
          last.insert(node, n);
        else if(target == 0) {
          if(isExpanded(new TreePath(last)))
            last.insert(node, 0);
          else
            last.add(node);
        }
        else if(row != getRowCount())
          last.insert(node, n + 1);
        else
          last.add(node);
        
        expand(last);
      }
      
      updateUI();
    }catch(Exception ex) {ex.printStackTrace();}
    
    e.dropComplete(true);
  }

  public void dropActionChanged(DropTargetDragEvent e) {}
  
  private class FavoriteTransferNode implements Transferable {    
    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
      return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] {FAVORITE_FLAVOR};
    }

    public boolean isDataFlavorSupported(DataFlavor df) {
      return df.getMimeType().equals(FAVORITE_FLAVOR.getMimeType()) && df.getHumanPresentableName().equals(FAVORITE_FLAVOR.getHumanPresentableName());
    }
  }
  
  public void expandPath(TreePath path) {
    super.expandPath(path);
    
    handleExpandedState((FavoriteNode)path.getLastPathComponent(),true);
  }

  public void collapsePath(TreePath path) {
    super.collapsePath(path);
    
    handleExpandedState((FavoriteNode)path.getLastPathComponent(),false);
  }

  private void handleExpandedState(FavoriteNode node, boolean expanded) {
    node.setWasExpanded(expanded);
  }
  
  protected void newFolder(FavoriteNode last) {
    String value = JOptionPane.showInputDialog(UiUtilities.getLastModalChildOf(ManageFavoritesDialog.getInstance()), "Name:", "Neuer Ordner");
    
    if(value != null && value.length() > 0) {
      FavoriteNode node = new FavoriteNode(value);
      
      if(last.equals(mRootNode))
        last.add(node);
      else
        ((FavoriteNode)last.getParent()).add(node);
      
      getModel().reload(node.getParent());
    }
  }
  
  public String convertValueToText(Object value, boolean selected,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    StringBuffer text = new StringBuffer(value.toString());
    
    if(value instanceof FavoriteNode) {
      text.append(" [").append(getProgramsCount((FavoriteNode)value)).append("]");
    }
    
    return text.toString();
  }
  
   /** Calculates the number of programs containded in the childs
   * 
   * @param node
   *          use this Node
   * @return Number of Child-Nodes
   */
  private int getProgramsCount(FavoriteNode node) {
    int count = node.containsFavorite() ? node.getFavorite().getWhiteListPrograms().length : 0;
    for (int i = 0; i < node.getChildCount(); i++) {
      FavoriteNode child = (FavoriteNode)node.getChildAt(i);
      if (child.containsFavorite()) {
        count += child.getFavorite().getWhiteListPrograms().length;
      } else {
        count += getProgramsCount(child);
      }
    }
    return count;
  }
  
  public void sort(FavoriteNode node, boolean start) {
    int result = JOptionPane.YES_OPTION;
    
    if(start) {
      String msg = mLocalizer.msg("reallySort", "Do you really want to sort your " +
      "favorites?\n\nThe current order will get lost.");
      String title = UIManager.getString("OptionPane.titleText");
      result = JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), msg, title, JOptionPane.YES_NO_OPTION);
    }
    
    if (result == JOptionPane.YES_OPTION) {
      FavoriteNode[] nodes = new FavoriteNode[node.getChildCount()];
      
      for(int i = 0; i < nodes.length; i++) {
        nodes[i] = (FavoriteNode)node.getChildAt(i);
      }
      
      node.removeAllChildren();
      
      Arrays.sort(nodes);
      
      for(FavoriteNode child : nodes) {
        node.add(child);
        
        if(child.isDirectoryNode())
          sort(child, false);
      }
    }
    
    ManageFavoritesDialog.getInstance().favoriteSelectionChanged();
  }
}
