/*
 * TV-Browser
 * Copyright (C) 04-2003 TV-Browser-Team (dev@tvbrowser.org)
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
package tvbrowser.ui.filter.dlgs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
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

import javax.swing.Icon;
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

import devplugin.ProgramFilter;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterList;
import tvbrowser.core.filters.ShowAllFilter;
import tvbrowser.core.filters.UserFilter;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;

import util.ui.Localizer;
import util.ui.OverlayListener;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

public class FilterTree extends JTree implements DragGestureListener, DropTargetListener {
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(FilterTree.class);

  private FilterNode mTransferNode;
  private Rectangle2D mCueLine = new Rectangle2D.Float();

  private FilterNode mRootNode;
  private FilterNode mTargetNode;
  private int mTarget;
  private long mDragOverStart;
  private boolean mExpandListenerIsEnabled;

  private static final DataFlavor FILTER_FLAVOR = new DataFlavor(
      TreePath.class, "FilterNodeExport");
  
  FilterTree() {
    init();
  }
  

  public void updateUI() {
    setUI(new FilterTreeUI());
    invalidate();
  }
  
  private void init() {
    setModel(FilterList.getInstance().getFilterTreeModel());
    
    setRootVisible(true);
    setShowsRootHandles(true);

    mRootNode = (FilterNode) FilterList.getInstance().getFilterTreeModel().getRoot();

    FilterTreeCellRenderer renderer = new FilterTreeCellRenderer();
    renderer.setLeafIcon(null);
    setCellRenderer(renderer);

    mExpandListenerIsEnabled = false;
    expand(mRootNode);
    mExpandListenerIsEnabled = true;

    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        FilterNode node = getSelectedFilterNode();
        
        if(e.getKeyCode() == KeyEvent.VK_DELETE) {
          SelectFilterDlg.getInstance().deleteSelectedItem(node);
        }
        else if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
          if(getSelectionPath() != null) {
            Rectangle pathBounds = getRowBounds(getRowForPath(getSelectionPath()));

            showContextMenu(new Point(pathBounds.x + pathBounds.width - 10, pathBounds.y + pathBounds.height - 5));
          }
        }
        else if(e.getKeyCode() == KeyEvent.VK_F2) {
          SelectFilterDlg.getInstance().editSelectedFilter(node);
        }
      }
    });

    mExpandListenerIsEnabled = true;

    addTreeExpansionListener(new TreeExpansionListener() {
      public void treeCollapsed(TreeExpansionEvent e) {
        if(e.getPath() != null && mExpandListenerIsEnabled) {
          ((FilterNode)e.getPath().getLastPathComponent()).setWasExpanded(false);
        }
      }

      public void treeExpanded(TreeExpansionEvent e) {
        if(e.getPath() != null && mExpandListenerIsEnabled) {
          ((FilterNode)e.getPath().getLastPathComponent()).setWasExpanded(true);
        }
      }
    });

    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    new OverlayListener(this);
    (new DragSource()).createDefaultDragGestureRecognizer(this,
        DnDConstants.ACTION_MOVE, this);

    new DropTarget(this, this);
  }
  

  @Override
  public void dragGestureRecognized(DragGestureEvent dge) {
    mTransferNode = (FilterNode)getLastSelectedPathComponent();

    if(mTransferNode != null) {
      dge.startDrag(null,new FilterTransferNode());
    }

  }

  private int getTargetFor(FilterNode node, Point p, int row) {
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
      FilterNode last = (FilterNode)path.getLastPathComponent();
      FilterNode pointed = last;

      int target = getTargetFor(pointed, p, row);

      if(target == -1 || (target == 1 && !isExpanded(new TreePath(pointed.getPath())))) {
        if(!last.isRoot()) {
          last = (FilterNode)last.getParent();
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

  @Override
  public void dragEnter(DropTargetDragEvent dtde) {
    if(dtde.getCurrentDataFlavors().length > 1 || dtde.getCurrentDataFlavors().length < 1 || !dtde.getCurrentDataFlavors()[0].equals(FILTER_FLAVOR)) {
      dtde.rejectDrag();
    } else if(calculateCueLine(dtde.getLocation())) {
      dtde.acceptDrag(dtde.getDropAction());
    } else {
      dtde.rejectDrag();
    }

  }

  @Override
  public void dragExit(DropTargetEvent dte) {
    paintImmediately(mCueLine.getBounds());
    mCueLine.setRect(0,0,0,0);
    mTarget = -2;
  }

  @Override
  public void dragOver(DropTargetDragEvent dtde) {
    if(dtde.getCurrentDataFlavors().length > 1 || dtde.getCurrentDataFlavors().length < 1 || !dtde.getCurrentDataFlavors()[0].equals(FILTER_FLAVOR)) {
      paintImmediately(mCueLine.getBounds());
      dtde.rejectDrag();
    }
    else if(calculateCueLine(dtde.getLocation())) {
      dtde.acceptDrag(dtde.getDropAction());
    } else {
      dtde.rejectDrag();
    }

    if (this.getVisibleRect().width < this.getSize().width
        || this.getVisibleRect().height < this.getSize().height) {
      int scroll = 20;
      if (dtde.getLocation().y + scroll + 5 > getVisibleRect().height) {
        scrollRectToVisible(new Rectangle(dtde.getLocation().x, dtde.getLocation().y + scroll + 5, 1, 1));
      }
      if (dtde.getLocation().y - scroll < getVisibleRect().y) {
        scrollRectToVisible(new Rectangle(dtde.getLocation().x, dtde.getLocation().y - scroll, 1, 1));
      }
      if (dtde.getLocation().x - scroll < getVisibleRect().x) {
        scrollRectToVisible(new Rectangle(dtde.getLocation().x - scroll, dtde.getLocation().y, 1, 1));
      }
      if (dtde.getLocation().x + scroll + 5 > getVisibleRect().width) {
        scrollRectToVisible(new Rectangle(dtde.getLocation().x + scroll + 5, dtde.getLocation().y, 1, 1));
      }
    }
  }

  @Override
  public void drop(DropTargetDropEvent dtde) {
    dtde.acceptDrop(dtde.getDropAction());
    this.paintImmediately(mCueLine.getBounds());
    Transferable transfer = dtde.getTransferable();

    if(transfer.isDataFlavorSupported(new DataFlavor(TreePath.class, "FilterNodeExport"))) {
      try {
        FilterNode node = mTransferNode;
        FilterNode parent = (FilterNode)node.getParent();

        int row = getClosestRowForLocation(dtde.getLocation().x, dtde.getLocation().y);

        TreePath path = new TreePath(mRootNode);

        if(getRowBounds(row).y + getRowBounds(row).height < dtde.getLocation().y) {
          row = this.getRowCount();
        } else {
          path = getPathForRow(row);
        }

        if(path == null) {
          path = new TreePath(mRootNode);
        }

        FilterNode last = (FilterNode)path.getLastPathComponent();
        FilterNode pointed = last;
        int target = getTargetFor(pointed, dtde.getLocation(), row);

        if(!new TreePath(node.getPath()).isDescendant(path)) {
          setSelectionPath(null);

          parent.remove(node);

          int n = -1;

          if(target == -1 || (target == 1 && !isExpanded(new TreePath(pointed.getPath())))) {
            if(last.isRoot()) {
              n = 0;
            } else {
              n = last.getParent().getIndex(last);
              last = (FilterNode)last.getParent();
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

    dtde.dropComplete(true);
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {
    // TODO Auto-generated method stub

  }
  
  /**
   * Gets the root node of this tree.
   *
   * @return The root node of this tree.
   */
  public FilterNode getRoot() {
    return mRootNode;
  }
  

  private void expandAll(FilterNode node) {
    if(node.isDirectoryNode()) {
      expandPath(new TreePath(node.getPath()));

      for(int i = 0; i < node.getChildCount(); i++) {
        FilterNode child = (FilterNode)node.getChildAt(i);

        if(child.isDirectoryNode()) {
          expandPath(new TreePath(child.getPath()));
          expandAll(child);
        }
      }
    }
  }

  private void collapseAll(FilterNode node) {
    if(node.isDirectoryNode()) {
      for(int i = 0; i < node.getChildCount(); i++) {
        FilterNode child = (FilterNode)node.getChildAt(i);

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

  private void expand(FilterNode node) {
    @SuppressWarnings("unchecked")
    Enumeration<FilterNode> e = node.children();

    while(e.hasMoreElements()) {
      FilterNode child = e.nextElement();

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

  public FilterTreeModel getModel() {
    return (FilterTreeModel)super.getModel();
  }
  
  private void showContextMenu(Point p) {
    JPopupMenu menu = new JPopupMenu();
    int row = getClosestRowForLocation(p.x, p.y);
    if (row >= 0 && row < getRowCount()) {
      setSelectionRow(row);
    }
    
    final FilterNode last = getSelectedFilterNode();
    final TreePath path = new TreePath(last.getPath());
    
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

    if (!last.isDirectoryNode() && last.getFilter() instanceof UserFilter) {
      item = new JMenuItem(mLocalizer.ellipsisMsg("editFilter", "Edit filter '{0}'", last.getFilter().getName()),
          TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
      item.setFont(item.getFont().deriveFont(Font.BOLD));

      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          SelectFilterDlg.getInstance().editSelectedFilter(last);
        }
      });
      menu.add(item);
      menu.addSeparator();
    }

    item = new JMenuItem(mLocalizer.msg("newFolder", "New folder"),
        IconLoader.getInstance().getIconFromTheme("actions", "folder-new", 16));

    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SelectFilterDlg.getInstance().createNewFolder(last);
      }
    });
    menu.add(item);

    item = new JMenuItem(mLocalizer.msg("newFilter", "New Filter"), TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));

    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SelectFilterDlg.getInstance().createNewFilter(last);
      }
    });
    menu.add(item);

    if(last.isDirectoryNode()) {
      if(!last.equals(mRootNode)) {
        item = new JMenuItem(mLocalizer.msg("renameFolder", "Rename folder"),
            TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));

        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            renameFolder(last);
          }
        });

        menu.add(item);
      }
    }

    item = new JMenuItem(mLocalizer.msg("newSeparator", "Add separator"),IconLoader.getInstance().getIconFromTheme("emblems", "separator", 16));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SelectFilterDlg.getInstance().addSeparator(last);
      }
    });
    
    menu.add(item);
    
    if(last.isDeletingAllowed()) {
      item = new JMenuItem(Localizer.getLocalization(Localizer.I18N_DELETE),
          TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
  
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          SelectFilterDlg.getInstance().deleteSelectedItem(last);
        }
      });
      
  
      if(!last.equals(mRootNode) && last.getChildCount() < 1) {
        menu.add(item);
      }
    }
    
    if(last.containsFilter()) {
      String id = last.getFilter().getClass().getName();
      String name = last.getFilter().getName();
      
      if(!((Settings.propDefaultFilter.getString().equals(id + "###" + name)) ||
          (Settings.propDefaultFilter.getString().trim().length() < 1 && last.getFilter() instanceof ShowAllFilter))) {
        item = new JMenuItem(mLocalizer.msg("setDefault","Set as default"),IconLoader.getInstance().getIconFromTheme("actions", "view-filter", 16));
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            SelectFilterDlg.getInstance().setDefaultFilter(last);
          }
        });
        
        menu.addSeparator();
        menu.add(item);
      }
    }

    menu.show(this, p.x, p.y);
  }

  protected void reload(FilterNode node) {
    mExpandListenerIsEnabled = false;
    getModel().reload(this, node);
    mExpandListenerIsEnabled = true;
  }
  

  
  private class FilterTreeUI extends javax.swing.plaf.basic.BasicTreeUI implements MouseListener {
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
        
        if(SelectFilterDlg.getInstance() != null) {
          SelectFilterDlg.getInstance().updateBtns();
        }
        
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

          if(path != null && ((FilterNode)path.getLastPathComponent()).containsFilter()) {
            mLastSelectionPath = path;
            if(e.getClickCount() >= 2) {
              SelectFilterDlg.getInstance().editSelectedFilter((FilterNode)path.getLastPathComponent());
            }
          }
          else if(path != null && ((FilterNode)path.getLastPathComponent()).isDirectoryNode() && (e.getWhen() - mMousePressedTime) < CLICK_WAIT_TIME && getPathBounds(tree,path).contains(e.getPoint())) {
            if(mClickedThread == null || !mClickedThread.isAlive()) {
              mClickedThread = new Thread("Double click filter") {
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

  private static class FilterTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        final boolean sel,
        boolean expanded,
        boolean leaf, int row,
        boolean cellHasFocus) {
      JLabel label = (JLabel)super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,cellHasFocus);

      if(label != null) {
        if(UiUtilities.isGTKLookAndFeel()) {
          label.setBackground(tree.getBackground());
          label.setOpaque(!sel && !cellHasFocus);
        }
      }

      if(leaf && value instanceof FilterNode && ((FilterNode)value).isDirectoryNode()) {
        label.setIcon(getClosedIcon());
      }
      
      if(value instanceof FilterNode) {
        FilterNode test = (FilterNode)value;
        
        if(test.containsFilter()) {
          String id = test.getFilter().getClass().getName();
          String name = test.getFilter().getName();
          
          if((Settings.propDefaultFilter.getString().equals(id + "###" + name)) ||
              (Settings.propDefaultFilter.getString().trim().length() < 1 && test.getFilter() instanceof ShowAllFilter)) {
            label = new JLabel(label.getText());
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            
            
            if(sel) {
              label.setOpaque(true);
              label.setBackground(UIManager.getColor("Tree.selectionBackground"));
              label.setForeground(UIManager.getColor("Tree.selectionForeground"));
            }
          }
        }
        else if(test.containsSeparator()) {
          label.setText("");
          label.setIcon(new Icon() {
            public int getIconHeight() {
              return 16;
            }
            
            public int getIconWidth() {
              return 64;
            }
            
            public void paintIcon(Component c, Graphics g, int x, int y) {
              if(sel) {
                g.setColor(UIManager.getColor("Tree.selectionBackground"));
                g.fillRect(0,0,getIconWidth(),getIconHeight());
              }
              
              g.setColor(c.getForeground());
              g.drawLine(4,getIconHeight()/2-1,getIconWidth()-4,getIconHeight()/2-1);
              g.drawLine(4,getIconHeight()/2,getIconWidth()-4,getIconHeight()/2);
            }
          });
        }
      }

      if(UiUtilities.isNimbusLookAndFeel()) {
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

  protected void moveSelectedFilter(int rowCount) {
    FilterNode src = (FilterNode)getSelectionPath().getLastPathComponent();

    if(src.isDirectoryNode()) {
      collapseAll(src);
    }

    int row = getRowForPath(getSelectionPath());

    if(row != -1) {
      TreePath path = getPathForRow(row+rowCount);

      if(path != null) {
        FilterNode srcParent = (FilterNode)src.getParent();


        FilterNode target = (FilterNode)path.getLastPathComponent();
        FilterNode tarParent = target.equals(mRootNode) ? mRootNode : ((FilterNode)target.getParent());

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

  protected void renameFolder(FilterNode node) {
    if(node != null && node.isDirectoryNode()) {
      String value = JOptionPane.showInputDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), mLocalizer.msg("folderName","Folder name:"), node.getUserObject());

      if(value != null) {
        node.setUserObject(value);
        updateUI();
      }
    }
  }

  protected FilterNode findFilter(final ProgramFilter filter) {
    return findFilter(filter, getRoot());
  }

  private FilterNode findFilter(final ProgramFilter filter, final FilterNode root) {
    if (root.isDirectoryNode()) {
      Enumeration<FilterNode> e = root.children();

      while(e.hasMoreElements()) {
        FilterNode child = e.nextElement();
        if(child.isDirectoryNode()) {
          FilterNode result = findFilter(filter, child);
          if (result != null) {
            return result;
          }
        }
        else {
          if (child.getFilter().equals(filter)) {
            return child;
          }
        }
      }
    }
    return null;
  }
  
  private static class FilterTransferNode implements Transferable {
    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
      return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] {FILTER_FLAVOR};
    }

    public boolean isDataFlavorSupported(DataFlavor df) {
      return df.getMimeType().equals(FILTER_FLAVOR.getMimeType()) && df.getHumanPresentableName().equals(FILTER_FLAVOR.getHumanPresentableName());
    }
  }
  
  private FilterNode getSelectedFilterNode() {
    TreePath path1 = null;
    int[] selectionRows = getSelectionRows();
    if (selectionRows != null && selectionRows.length > 0) {
      path1 = getPathForRow(selectionRows[0]);
    }

    if(path1 == null) {
      path1 = new TreePath(mRootNode);
    }

    final TreePath path = path1;
    final FilterNode last = (FilterNode)path.getLastPathComponent();

    setSelectionPath(path);
    
    return last;
  }
}
