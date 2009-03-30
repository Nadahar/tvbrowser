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
import java.awt.Component;
import java.awt.Font;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import util.ui.Localizer;
import util.ui.OverlayListener;
import util.ui.UiUtilities;

/**
 * The tree for the ManageFavoritesDialog.
 * 
 * @author René Mach
 * @since 2.6
 */
public class FavoriteTree extends JTree implements DragGestureListener, DropTargetListener {
  private static Localizer mLocalizer = Localizer.getLocalizerFor(FavoriteTree.class);
  
  private FavoriteNode mTransferNode;
  private Rectangle2D mCueLine = new Rectangle2D.Float();

  private FavoriteNode mRootNode;
  private FavoriteNode mTargetNode;
  private int mTarget;
  private long mDragOverStart;
  private boolean mExpandListenerIsEnabled;
  
  protected static final DataFlavor FAVORITE_FLAVOR = new DataFlavor(TreePath.class, "FavoriteNodeExport");
  
  /**
   * Creates an instance of this class.
   */
  public FavoriteTree() {
    init();
  }
    
  public void updateUI() {
    setUI(new FavoriteTreeUI());
    invalidate();
  }
  
  private void init() {
    setModel(FavoriteTreeModel.getInstance());
    setRootVisible(false);
    setShowsRootHandles(true);

    mRootNode = (FavoriteNode) FavoriteTreeModel.getInstance().getRoot();

    FavoriteTreeCellRenderer renderer = new FavoriteTreeCellRenderer();
    renderer.setLeafIcon(null);
    setCellRenderer(renderer);
    
    mExpandListenerIsEnabled = false;
    expand(mRootNode);
    mExpandListenerIsEnabled = true;
    
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_DELETE) {
          if(getSelectionPath() != null) {
            FavoriteNode node = (FavoriteNode)getSelectionPath().getLastPathComponent();
            FavoriteNode parent = (FavoriteNode)node.getParent();
            
            int n = parent.getIndex(node);
                        
            delete((FavoriteNode)getSelectionPath().getLastPathComponent());
            
            if (n >= parent.getChildCount()) {
              n--;
            }
            if(n > 0) {
              setSelectionPath(new TreePath(((FavoriteNode)parent.getChildAt(n)).getPath()));
            } else {
              setSelectionPath(new TreePath(parent.getPath()));
            }
          }
        }
        else if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
          if(getSelectionPath() != null) {
            Rectangle pathBounds = getRowBounds(getRowForPath(getSelectionPath()));
            
            showContextMenu(new Point(pathBounds.x + pathBounds.width - 10, pathBounds.y + pathBounds.height - 5));
          }
        }
        else if(e.getKeyCode() == KeyEvent.VK_F2) {
          if(getSelectionPath() != null) {
            FavoriteNode node = (FavoriteNode)getSelectionPath().getLastPathComponent();
            
            if(node.isDirectoryNode()) {
              renameFolder(node);
            } else {
              ManageFavoritesDialog.getInstance().editSelectedFavorite();
            }
          }
        }
      }
    });
    
    mExpandListenerIsEnabled = true;
    
    addTreeExpansionListener(new TreeExpansionListener() {
      public void treeCollapsed(TreeExpansionEvent e) {
        if(e.getPath() != null && mExpandListenerIsEnabled) {
          ((FavoriteNode)e.getPath().getLastPathComponent()).setWasExpanded(false);
        }
      }

      public void treeExpanded(TreeExpansionEvent e) {
        if(e.getPath() != null && mExpandListenerIsEnabled) {
          ((FavoriteNode)e.getPath().getLastPathComponent()).setWasExpanded(true);
        }
      }
    });
        
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    new OverlayListener(this);
    (new DragSource()).createDefaultDragGestureRecognizer(this,
        DnDConstants.ACTION_MOVE, this);
    
    new DropTarget(this, this);
  }

  protected void reload(FavoriteNode node) {
    mExpandListenerIsEnabled = false;
    getModel().reload(this, node);
    mExpandListenerIsEnabled = true;
  }
  
  private void showContextMenu(Point p) {
    
      JPopupMenu menu = new JPopupMenu();      
      TreePath path1 = getPathForLocation(p.x, p.y);
      
      if(path1 == null) {
        path1 = new TreePath(mRootNode);
      }
      
      final TreePath path = path1;
      final FavoriteNode last = (FavoriteNode)path.getLastPathComponent();

      setSelectionPath(path);
      
      JMenuItem item;
      
      if(last.isDirectoryNode() && last.getChildCount() > 0) {
        item = new JMenuItem(isExpanded(path) ? mLocalizer.msg("collapse", "Collapse") : mLocalizer.msg("expand", "Expand"));
        item.setFont(item.getFont().deriveFont(Font.BOLD));
        
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if(isExpanded(path)) {
              collapsePath(path);
            } else {
              expandPath(path);
            }
          }
        });
        
        if(!last.equals(mRootNode)) {
          menu.add(item);
        }
        
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
      
      item = new JMenuItem(mLocalizer.msg("newFolder", "New folder"),
          IconLoader.getInstance().getIconFromTheme("actions", "folder-new", 16));      
      
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          newFolder(last);
        }
      });
      
      menu.add(item);
      menu.addSeparator();
      
      item = new JMenuItem(mLocalizer.msg("newFavorite", "New Favorite"),
          FavoritesPlugin.getInstance().getIconFromTheme("actions", "document-new", 16));
      
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ManageFavoritesDialog.getInstance().newFavorite(last.isDirectoryNode() ? last : (FavoriteNode)last.getParent());
        }
      });
      
      menu.add(item);
      
      if(last.isDirectoryNode()) {
        if(!last.equals(mRootNode)) {
          item = new JMenuItem(mLocalizer.msg("renameFolder", "Rename folder"),
              IconLoader.getInstance().getIconFromTheme("actions", "document-edit", 16));
          
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              renameFolder(last);
            }
          });
          
          menu.add(item);
        }
      }
      
      FavoriteNode parentSort = null;
      
      if(!last.isDirectoryNode() && last.getParent().equals(mRootNode) && mRootNode.getChildCount() > 1) {
        parentSort = (FavoriteNode)last.getParent();
      }
      
      final FavoriteNode sortNode = parentSort == null ? last : parentSort;
        
      if(last.getChildCount() > 1 || (last.getParent().equals(mRootNode) && mRootNode.getChildCount() > 1 )) {
        menu.addSeparator();
        
        item = new JMenuItem(mLocalizer.msg("sort", "Sort alphabetically"),
            IconLoader.getInstance().getIconFromTheme("actions", "sort-list", 16));
        final String titleAlpha = item.getText();
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              FavoriteTreeModel.getInstance().sort(sortNode,true, FavoriteNodeComparator.getInstance(), titleAlpha);
              reload(sortNode);
          }
        });
        menu.add(item);

        item = new JMenuItem(mLocalizer.msg("sortCount", "Sort by number of programs"),
            IconLoader.getInstance().getIconFromTheme("actions", "sort-list-numerical", 16));
        final String titleCount = item.getText();
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              FavoriteTreeModel.getInstance().sort(sortNode,true, FavoriteNodeCountComparator.getInstance(),titleCount);
              reload(sortNode);
          }
        });
        menu.add(item);
      }
      
      if(parentSort != null) {
        menu.addSeparator();
      }
      
      
      if (!last.isDirectoryNode()) {
        item = new JMenuItem(mLocalizer.msg("editFavorite", "Edit favorite"),
            IconLoader.getInstance().getIconFromTheme("actions", "document-edit", 16));
        item.setFont(item.getFont().deriveFont(Font.BOLD));
        
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ManageFavoritesDialog.getInstance().editSelectedFavorite();
          }
        });
        
        menu.add(item);
      }
      
      item = new JMenuItem(Localizer.getLocalization(Localizer.I18N_DELETE),
          IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16));
      
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          delete(last);
        }
      });
      
      if(!last.equals(mRootNode) && last.getChildCount() < 1) {
        menu.add(item);
      }
      
      if(!ManageFavoritesDialog.getInstance().programListIsEmpty()) {
        menu.addSeparator();
        
        item = new JMenuItem(ManageFavoritesDialog.mLocalizer.msg("send", "Send Programs to another Plugin"), FavoritesPlugin.getInstance().getIconFromTheme("actions", "edit-copy", 16));
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
             ManageFavoritesDialog.getInstance().showSendDialog();
          }
        });
        
        menu.add(item);
      }
      
      menu.show(this, p.x, p.y);
  }
  
  protected void delete(FavoriteNode node) {
    if(node.isDirectoryNode() && node.getChildCount() < 1) {
      FavoriteNode parent = (FavoriteNode)node.getParent();
      parent.remove(node);
      getModel().reload(parent);
    }
    else if(node.containsFavorite()) {
      ManageFavoritesDialog.getInstance().deleteSelectedFavorite();
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
      
      if(!node.equals(mRootNode)) {
        collapsePath(new TreePath(node.getPath()));
      }
    }
  }
  
  private void expand(FavoriteNode node) {
    @SuppressWarnings("unchecked")
    Enumeration<FavoriteNode> e = node.children();
    
    while(e.hasMoreElements()) {
      FavoriteNode child = e.nextElement();
      
      if(child.isDirectoryNode()) {
        expand(child);
      }
    }
    
    if(node.wasExpanded()) {
      expandPath(new TreePath((this.getModel()).getPathToRoot(node)));
    } else {
      collapsePath(new TreePath((this.getModel()).getPathToRoot(node)));
    }
  }
  
  public FavoriteTreeModel getModel() {
    return (FavoriteTreeModel)super.getModel();
  }
  

  /**
   * Gets the root node of this tree.
   * 
   * @return The root node of this tree.
   */
  public FavoriteNode getRoot() {
    return mRootNode;
  }

  public void dragGestureRecognized(DragGestureEvent e) {
    mTransferNode = (FavoriteNode)getLastSelectedPathComponent();
    
    if(mTransferNode != null) {
      e.startDrag(null,new FavoriteTransferNode());
    }
  }

  public void dragEnter(DropTargetDragEvent e) {
    if(e.getCurrentDataFlavors().length > 1 || e.getCurrentDataFlavors().length < 1 || !e.getCurrentDataFlavors()[0].equals(FAVORITE_FLAVOR)) {
      e.rejectDrag();
    } else if(calculateCueLine(e.getLocation())) {
      e.acceptDrag(e.getDropAction());
    } else {
      e.rejectDrag();
    }
  }

  public void dragExit(DropTargetEvent e) {
    paintImmediately(mCueLine.getBounds());
    mCueLine.setRect(0,0,0,0);
    mTarget = -2;
  }
  
  private int getTargetFor(FavoriteNode node, Point p, int row) {
    Rectangle location = getRowBounds(getClosestRowForLocation(p.x, p.y));
    
    if(node.isDirectoryNode()) {
      if(row != getRowCount() && p.y - location.y <= (location.height / 4)) {
        return -1;
      }
      else if(row != getRowCount() && p.y - location.y <= (location.height - (location.height / 4))) {
        return 0;
      } else {
        return 1;
      }
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
  
  
  private boolean calculateCueLine(Point p) {
    int row = getClosestRowForLocation(p.x, p.y);
    
    Rectangle rowBounds = getRowBounds(row);
    
    if(rowBounds.y + rowBounds.height < p.y) {
      row = this.getRowCount();
    }
    
    TreePath path = getPathForRow(row);
    
    if(path == null) {
      path = new TreePath(mRootNode);
    }
    
    if(mTransferNode != null && !new TreePath(mTransferNode.getPath()).isDescendant(path)) {      
      FavoriteNode last = (FavoriteNode)path.getLastPathComponent();
      FavoriteNode pointed = last;
      
      int target = getTargetFor(pointed, p, row);
      
      if(target == -1 || (target == 1 && !isExpanded(new TreePath(pointed.getPath())))) {
        if(!last.isRoot()) {        
          last = (FavoriteNode)last.getParent();
        }
      }
      
      if(mTarget == 0 && (System.currentTimeMillis() - mDragOverStart) > 1000 && isCollapsed(new TreePath(last.getPath())) && last.getChildCount() > 0) {
        expandPath(new TreePath(last.getPath()));

        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            mTarget = -2;
          }
        });
      }
      
      if(mTargetNode != last || mTarget != target) {
        mTargetNode = last;
        mTarget = target;
        mDragOverStart = System.currentTimeMillis();
        
        this.paintImmediately(mCueLine.getBounds());
        
        int y = row != getRowCount() ? rowBounds.y : rowBounds.y + rowBounds.height;
              
        if(target == -1) {
          Rectangle rect = new Rectangle(rowBounds.x,y-1,rowBounds.width,2);
          
          mCueLine.setRect(rect);
        }
        else if(target == 0) {        
          Rectangle rect = new Rectangle(rowBounds.x,rowBounds.y,rowBounds.width,rowBounds.height);
            
          mCueLine.setRect(rect);
        }
        else if(target == 1) {
          Rectangle rect= new Rectangle(rowBounds.x,y + rowBounds.height-1,rowBounds.width,2);
        
          if(row == getRowCount()) {
           rect = new Rectangle(0,y-1,getWidth(),2);
          } 
          mCueLine.setRect(rect);
        }
        
        Graphics2D g2 = (Graphics2D) getGraphics();
        Color c = new Color(255, 0, 0, mCueLine.getHeight() > 2 ? 40 : 180);
        g2.setColor(c);
        g2.fill(mCueLine);
      }
      
      return true;
    }
    
    return false;
  }
  
  public void dragOver(DropTargetDragEvent e) {
    if(e.getCurrentDataFlavors().length > 1 || e.getCurrentDataFlavors().length < 1 || !e.getCurrentDataFlavors()[0].equals(FAVORITE_FLAVOR)) {
      paintImmediately(mCueLine.getBounds());
      e.rejectDrag();
    }
    else if(calculateCueLine(e.getLocation())) {
      e.acceptDrag(e.getDropAction());
    } else {
      e.rejectDrag();
    }
    
    if (this.getVisibleRect().width < this.getSize().width
        || this.getVisibleRect().height < this.getSize().height) {
      int scroll = 20;
      if (e.getLocation().y + scroll + 5 > getVisibleRect().height) {
        scrollRectToVisible(new Rectangle(e.getLocation().x, e.getLocation().y
            + scroll + 5, 1, 1));
      }
      if (e.getLocation().y - scroll < getVisibleRect().y) {
        scrollRectToVisible(new Rectangle(e.getLocation().x, e.getLocation().y
            - scroll, 1, 1));
      }
      if (e.getLocation().x - scroll < getVisibleRect().x) {
        scrollRectToVisible(new Rectangle(e.getLocation().x - scroll, e
            .getLocation().y, 1, 1));
      }
      if (e.getLocation().x + scroll + 5 > getVisibleRect().width) {
        scrollRectToVisible(new Rectangle(e.getLocation().x + scroll + 5, e
            .getLocation().y, 1, 1));
      }
    }
  }

  public void drop(DropTargetDropEvent e) {
    e.acceptDrop(e.getDropAction());
    this.paintImmediately(mCueLine.getBounds());
    Transferable transfer = e.getTransferable();
    
    if(transfer.isDataFlavorSupported(new DataFlavor(TreePath.class, "FavoriteNodeExport"))) {
      try {
        FavoriteNode node = mTransferNode;
        FavoriteNode parent = (FavoriteNode)node.getParent();
        
        int row = getClosestRowForLocation(e.getLocation().x, e.getLocation().y);
        
        TreePath path = new TreePath(mRootNode);
        
        if(getRowBounds(row).y + getRowBounds(row).height < e.getLocation().y) {
          row = this.getRowCount();
        } else {
          path = getPathForRow(row);
        }

        if(path == null) {
          path = new TreePath(mRootNode);
        }

        FavoriteNode last = (FavoriteNode)path.getLastPathComponent();
        FavoriteNode pointed = last;   
        int target = getTargetFor(pointed, e.getLocation(), row);
        
        if(!new TreePath(node.getPath()).isDescendant(path)) {
          setSelectionPath(null);
          
          parent.remove(node);
          
          int n = -1;
          
          if(target == -1 || (target == 1 && !isExpanded(new TreePath(pointed.getPath())))) {
            if(last.isRoot()) {
              n = 0;
            } else {
              n = last.getParent().getIndex(last);
              last = (FavoriteNode)last.getParent();
            }
          }
          
          if(target == -1) {
            last.insert(node, n);
          } else if(target == 0) {
            if(isExpanded(new TreePath(last))) {
              last.insert(node, 0);
            } else {
              last.add(node);
            }
          }
          else if(row != getRowCount()) {
            last.insert(node, n + 1);
          } else {
            last.add(node);
          }
          
          expandPath(new TreePath(last.getPath()));
          
          mExpandListenerIsEnabled = false;
          expand(last);
          mExpandListenerIsEnabled = true;
        }
        
        updateUI();
      }catch(Exception ex) {ex.printStackTrace();}
    }
    
    e.dropComplete(true);
  }

  public void dropActionChanged(DropTargetDragEvent e) {}
  
  private static class FavoriteTransferNode implements Transferable {    
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
  
  protected void newFolder(FavoriteNode parent) {
    String value = JOptionPane.showInputDialog(UiUtilities.getLastModalChildOf(ManageFavoritesDialog.getInstance()), mLocalizer.msg("folderName","Folder name:"), mLocalizer.msg("newFolder","New folder"));
    
    if(value != null && value.length() > 0) {
      FavoriteNode node = new FavoriteNode(value);
      
      if(parent.equals(mRootNode) || parent.isDirectoryNode()) {
        parent.add(node);
        expandPath(new TreePath(parent.getPath()));
      } else {
        ((FavoriteNode)parent.getParent()).insert(node,parent.getParent().getIndex(parent));
      }
      
      reload((FavoriteNode)node.getParent());
    }
  }

  public String convertValueToText(Object value, boolean selected,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    StringBuffer text = new StringBuffer(value.toString());

    if(value instanceof FavoriteNode) {
      int[] count = FavoriteTreeModel.getProgramsCount((FavoriteNode)value);

      if(count[0] > 0) {
        if(count[1] < 1) {
          text.append(" [").append(count[0]).append(']');
        } else {
          text.append(" [").append(count[0]).append(", ").append(
              Localizer.getLocalization(Localizer.I18N_TODAY)).append(": ")
              .append(count[1]).append(']');
        }
      }
    }

    return text.toString();
  }

  private class FavoriteTreeUI extends javax.swing.plaf.basic.BasicTreeUI implements MouseListener {
    private static final int CLICK_WAIT_TIME = 150;
    private Thread mClickedThread;
    private TreePath mLastSelectionPath;
    private long mMousePressedTime;
    private boolean mWasExpanded;
    
    protected MouseListener createMouseListener() {
      return this;
    }
    
    public void mousePressed(MouseEvent e) {
      if(!e.isConsumed()) {
        if(!tree.isFocusOwner()) {
          tree.requestFocusInWindow();
        }
        
        TreePath path = getClosestPathForLocation(tree, e.getX(), e.getY());
        
        if(path != null && getPathBounds(tree,path).contains(e.getPoint())) {
          setSelectionPath(path);
        }
        
        if(e.isPopupTrigger()) {
          showContextMenu(e.getPoint());
        }
        
        mMousePressedTime = e.getWhen();
        
        checkForClickInExpandControl(getClosestPathForLocation(tree, e.getX(), e.getY()),e.getX(),e.getY());
        e.consume();
      }
    }
    
    public void mouseReleased(MouseEvent e) {
      if(!e.isConsumed()) {
        if(e.isPopupTrigger()) {
          showContextMenu(e.getPoint());
        }
        
        if(SwingUtilities.isLeftMouseButton(e)) {
          final TreePath path = getClosestPathForLocation(tree, e.getX(), e.getY());
          
          if(path != null && ((FavoriteNode)path.getLastPathComponent()).containsFavorite()) {
            mLastSelectionPath = path;
            if(e.getClickCount() >= 2) {
              ManageFavoritesDialog.getInstance().editSelectedFavorite();
            }
          }
          else if(path != null && ((FavoriteNode)path.getLastPathComponent()).isDirectoryNode() && (e.getWhen() - mMousePressedTime) < CLICK_WAIT_TIME && getPathBounds(tree,path).contains(e.getPoint())) {
            if(mClickedThread == null || !mClickedThread.isAlive()) {
              mClickedThread = new Thread("Double click favorite") {
                public void run() {
                  if(!isExpanded(path)) {
                    expandPath(path);
                    mWasExpanded = true;
                  }
                  else if(mLastSelectionPath != null && tree.getSelectionPath().equals(mLastSelectionPath)){
                    collapsePath(path);
                    mWasExpanded = false;
                  }
                  setSelectionPath(path);
                  mLastSelectionPath = path;
                  
                  try {
                    Thread.sleep(CLICK_WAIT_TIME*2);
                  }catch(Exception e) {
                    e.printStackTrace();
                  }
                  
                  mWasExpanded = false;
                }
              };
              mClickedThread.start();
            }
            else if(!mWasExpanded && mLastSelectionPath != null && tree.getSelectionPath().equals(mLastSelectionPath)){
              collapsePath(path);
            }
          }
          else {
            mLastSelectionPath = path;
          }
        }
        e.consume();
      }
    }

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}
  }
  
  private static class FavoriteTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel,
        boolean expanded,
        boolean leaf, int row,
        boolean hasFocus) {
      JLabel label = (JLabel)super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
      
      if(label != null) {
        if(UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
          label.setBackground(tree.getBackground());
          label.setOpaque(!sel && !hasFocus);
        }
      }
      
      if(leaf && value instanceof FavoriteNode && ((FavoriteNode)value).isDirectoryNode()) {
        label.setIcon(getClosedIcon());
      }
      
      if(UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel")) {
        if(sel) {
          label.setOpaque(true);
          label.setBackground(UIManager.getColor("Tree.selectionBackground"));
        }
        else {
          label.setOpaque(false);
        }
      }
      
      return label;
    }
  }
  
  protected void moveSelectedFavorite(int rowCount) {
    FavoriteNode src = (FavoriteNode)getSelectionPath().getLastPathComponent();
    
    if(src.isDirectoryNode()) {
      collapseAll(src);
    }
    
    int row = getRowForPath(getSelectionPath());
    
    if(row != -1) {
      TreePath path = getPathForRow(row+rowCount);
      
      if(path != null) {
        FavoriteNode srcParent = (FavoriteNode)src.getParent();
        
        
        FavoriteNode target = (FavoriteNode)path.getLastPathComponent();
        FavoriteNode tarParent = target.equals(mRootNode) ? mRootNode : ((FavoriteNode)target.getParent());
        
        int n = tarParent.getIndex(target);
        
        srcParent.remove(src);
        
        if(n > -1) {
          tarParent.insert(src,n);
        } else {
          tarParent.add(src);
        }
        
        reload(srcParent);
        reload(tarParent);
        
        setSelectionPath(new TreePath(src.getPath()));
        
        expandPath(new TreePath(tarParent.getPath()));
        
        mExpandListenerIsEnabled = false;
        expand(tarParent);
        mExpandListenerIsEnabled = true;
      }
    }
  }
  
  protected void renameFolder(FavoriteNode node) {    
    if(node != null && node.isDirectoryNode()) {
      String value = JOptionPane.showInputDialog(UiUtilities.getLastModalChildOf(ManageFavoritesDialog.getInstance()), mLocalizer.msg("folderName","Folder name:"), node.getUserObject());
    
      if(value != null) {
        node.setUserObject(value);
        updateUI();
      }
    }
  }
}
