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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.pluginview;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.OverlayListener;
import util.ui.SingleAndDoubleClickTreeUI;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramItem;
import devplugin.ProgramReceiveTarget;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org) Date: 01.01.2005 Time:
 * 20:25:18
 */
public class PluginTree extends JTree implements DragGestureListener,
    DropTargetListener, DragSourceListener {
  
  private Rectangle2D mCueLine = new Rectangle2D.Float();
  private Object mPlugin = null;
  private Thread mDropThread = null;
  private String mDragNode = null;
  private Point mCurrentPoint = null;
  private Rectangle2D mGhostRect = new Rectangle2D.Float();
  private BufferedImage mGhostImage, mTreeImage;
  private boolean rejected = false;
  private static PluginTree mInstance;
  private static boolean mUpdateAllowed = true;

  public PluginTree(TreeModel model) {
    super(model);
    
    setRootVisible(false);
    setShowsRootHandles(true);
    setRowHeight(17);
    
    expandPath(new TreePath(model.getRoot()));
    
    mInstance = this;
    /* remove the F2 key from the keyboard bindings of the JTree */
    InputMap inputMap = getInputMap();
    KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
    inputMap.put(keyStroke, "none");
    
    new OverlayListener(this);
    (new DragSource()).createDefaultDragGestureRecognizer(this,
        DnDConstants.ACTION_MOVE, this);

    new DropTarget(this, this);
  }

  public static PluginTree getInstance() {
    return mInstance;
  }

  public String convertValueToText(Object value, boolean selected,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    if (value instanceof Node) {
      Node node = (Node) value;
      Object o = node.getUserObject();
      if (o instanceof ProgramItem) {
        ProgramItem programItem = (ProgramItem) o;
        return node.getNodeFormatter().format(programItem);
      } else if (o != null) {
        if (!leaf) {
          int leafs = getLeafCount(node);
          node.setLeafCount(leafs);
          if (node.isShowLeafCount()) {
            StringBuilder buf = new StringBuilder(o.toString());
            if (leafs > 0) {
              buf.append(" [").append(leafs).append(']');
            }
            return buf.toString();
          } else {
            return o.toString();
          }
        }
        return o.toString();
      } else {
        return "null";
      }
    } else {
      return value.toString();
    }
  }

  /**
   * Calculates the number of Childs
   * 
   * @param node
   *          use this Node
   * @return Number of Child-Nodes
   */
  private int getLeafCount(TreeNode node) {
    int count = 0;
    for (Enumeration children = node.children(); children.hasMoreElements();) {
      TreeNode child = (TreeNode) children.nextElement();
      if (child.isLeaf()) {
        count++;
      }
      else {
        count += getLeafCount(child); 
      }
    }
    return count;
  }

  public void expandAll(TreePath path) {
    expandPath(path);
    TreeModel model = getModel();
    if (path != null && model != null) {
      Object comp = path.getLastPathComponent();
      int cnt = model.getChildCount(comp);
      for (int i = 0; i < cnt; i++) {
        Object node = model.getChild(comp, i);
        expandAll(path.pathByAddingChild(node));
      }
    }
  }

  public void collapseAll(TreePath path) {
		TreeModel model = getModel();
		if (path != null && model != null) {
			Object comp = path.getLastPathComponent();
			int cnt = model.getChildCount(comp);
			for (int i = 0; i < cnt; i++) {
				Object node = model.getChild(comp, i);
				collapseAll(path.pathByAddingChild(node));
			}
	    if (!path.getLastPathComponent().equals(model.getRoot())) {
        collapsePath(path);
      }
		}
	}

  private static class TransferNode implements Transferable {
    private DataFlavor mDF;

    public TransferNode() {
      mDF = new DataFlavor(TreePath.class, "NodeExport");
    }

    public DataFlavor[] getTransferDataFlavors() {
      DataFlavor[] f = { mDF };
      return f;
    }

    public boolean isDataFlavorSupported(DataFlavor e) {
      if (e.equals(mDF)) {
        return true;
      }
      return false;
    }

    public Object getTransferData(DataFlavor e)
        throws UnsupportedFlavorException, IOException {
      return null;
    }

  }

  public void dragGestureRecognized(DragGestureEvent e) {
    try {
      if (mDropThread != null && mDropThread.isAlive()) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        return;
      }
      PluginTree tree = (PluginTree) e.getComponent();
      if (tree.getLastSelectedPathComponent() == null) {
        return;
      }

      Node node = (Node) tree.getSelectionPath().getLastPathComponent();
      if (node.getType() != Node.ROOT
          && (node.getLeafCount() > 0 || node.isLeaf())) {

        ((PluginTreeModel) this.getModel()).setDisableUpdate(true);

        mUpdateAllowed = false;
        Vector<Program> vec = this.getLeafElements(node, new Vector<Program>());

        if (vec.size() == 1) {
          mDragNode = (vec.firstElement()).getTitle();
        } else if (node.isLeaf()) {
          mDragNode = ((ProgramItem) node.getUserObject()).getProgram()
              .getTitle();
        } else {
          mDragNode = vec.size() + " " + Localizer.getLocalization(Localizer.I18N_PROGRAMS);
        }

        JLabel lbl2 = new JLabel(mDragNode);
        lbl2.setForeground(Color.white);
        lbl2.setVisible(true);
        lbl2.setFont(this.getFont());

        Point ptDragOrigin = e.getDragOrigin();

        Point ptOffset = new Point();

        TreePath path = getPathForLocation(ptDragOrigin.x, ptDragOrigin.y);
        Point raPath = getPathBounds(path).getLocation();

        ptOffset.setLocation(ptDragOrigin.x - raPath.x, ptDragOrigin.y
            - raPath.y);
        mGhostRect.setRect(ptOffset.x, ptOffset.y,
            lbl2.getPreferredSize().width, lbl2.getPreferredSize().height);

        lbl2.setSize(lbl2.getPreferredSize());
        mGhostImage = new BufferedImage(lbl2.getWidth(), lbl2
            .getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

        mTreeImage = new BufferedImage(this.getWidth(), this.getHeight(),
            BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2t = mTreeImage.createGraphics();
        this.paint(g2t);
        g2t.dispose();

        // Get a graphics context for this image
        Graphics2D g2 = mGhostImage.createGraphics();

        // Ask the cell renderer to paint itself into the BufferedImage
        lbl2.paint(g2);

        // Use DST_OVER to cause under-painting to occur
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER,
            0.5f));

        Color c1 = new Color(UIManager.getDefaults().getColor(
            "Tree.selectionBackground").getRGB());
        c1 = c1.darker();

        g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), 0, new Color(255,
            255, 255, 0)));

        // Paint under the JLabel's text
        g2.fillRect(0, 0, getWidth(), mGhostImage.getHeight());

        // Finished with the graphics context now
        g2.dispose();

        mCurrentPoint = e.getDragOrigin();

        e.startDrag(null, mGhostImage, new Point(15, 5),
            new TransferNode(), this);
      }
    } catch (Exception ee) {

      ((PluginTreeModel) this.getModel()).setDisableUpdate(false);
      mUpdateAllowed = true;
    }
  }

  public void dragEnter(DropTargetDragEvent e) {
    checkAndPaintTarget(e);
  }
  
  private void checkAndPaintTarget(DropTargetDragEvent e) {
    boolean reject = true;
    boolean changed = false;
    Object temp = null;

    try {
      DataFlavor[] flavors = e.getCurrentDataFlavors();

      if (flavors != null && flavors.length == 1) {

        try {
          TreePath targetPath = ((PluginTree) ((DropTarget) e.getSource())
              .getComponent()).getPathForLocation(e.getLocation().x, e
              .getLocation().y);

          if (targetPath != null) {
            Node target = (Node)targetPath.getLastPathComponent();
            
            if(target.getProgramReceiveTarget() == null && targetPath.getPathCount() <= 2) {
              target = (Node) targetPath.getPathComponent(1);
            }
  
            if (flavors[0].getHumanPresentableName().equals("NodeExport")) {
              TreePath sourcePath = ((PluginTree) ((DropTarget) e.getSource())
                  .getComponent()).getSelectionPath();
              Node plugin = (Node) sourcePath.getPathComponent(1);
              
              if (!target.equals(plugin) && !targetPath.isDescendant(sourcePath) &&
                  !sourcePath.isDescendant(targetPath)) {
                if (target.equals(ReminderPlugin.getInstance().getRootNode()
                    .getMutableTreeNode())) {
                  e.acceptDrag(e.getDropAction());
                  reject = false;
                  temp = ReminderPlugin.getInstance();
                  rejected = false;
                } else if(target.getProgramReceiveTarget() == null) {
                  PluginProxy[] pluginAccessArray = PluginProxyManager.getInstance().getActivatedPlugins();
  
                  for (PluginProxy pluginAccess : pluginAccessArray) {
                    if (pluginAccess.getRootNode() != null) {
                      if (pluginAccess.getRootNode().getMutableTreeNode().equals(target)) {
                        if (pluginAccess.canReceiveProgramsWithTarget()) {
                          e.acceptDrag(e.getDropAction());
                          reject = false;
                          temp = pluginAccess;
                          rejected = false;
                        } else {
                          mPlugin = null;
                          break;
                        }
                      }
                    }
                  }
                }
                else if(!target.equals(sourcePath.getLastPathComponent())){
                  e.acceptDrag(e.getDropAction());
                  reject = rejected = false;
                  temp = target.getProgramReceiveTarget();
                }
                else {
                  mPlugin = null;
                }
              }
            } else if (flavors[0].getHumanPresentableName().equals("Program")) {
              if (targetPath.getPathCount() <= 2) {
                if (FavoritesPlugin.getInstance().getRootNode()
                    .getMutableTreeNode().equals(target)) {
                  e.acceptDrag(e.getDropAction());
                  rejected = false;
                  reject = false;
                  temp = FavoritesPlugin.getInstance();
                } else if (ReminderPlugin.getInstance().getRootNode()
                    .getMutableTreeNode().equals(target)) {
                  e.acceptDrag(e.getDropAction());
                  rejected = false;
                  reject = false;
                  temp = ReminderPlugin.getInstance();
                } else {
                  PluginProxy[] pa = PluginProxyManager.getInstance().getActivatedPlugins();
  
                  for (PluginProxy pluginAccess : pa) {
                    if (pluginAccess.getRootNode() != null) {
                      if (pluginAccess.getRootNode().getMutableTreeNode().equals(target)) {
    
                        /*
                         * This would only work with Java 1.5
                         * 
                         * Transferable tr = e.getTransferable(); Program program =
                         * (Program) tr.getTransferData(flavors[0]);
                         */
    
                        if (getAction(pluginAccess.getContextMenuActions(Plugin
                            .getPluginManager().getExampleProgram())) == null) {
                          mPlugin = null;
                          break;
                        }
    
                        e.acceptDrag(e.getDropAction());
                        reject = false;
                        rejected = false;
                        temp = pluginAccess;
                      }
                    }
                  }
                }
              }
            }
  
            if (!reject && (mPlugin == null || temp != mPlugin)) {
              changed = true;
              this.paintImmediately(mCueLine.getBounds());
              
              mCueLine.setRect(((PluginTree) ((DropTarget) e.getSource())
                  .getComponent()).getPathBounds(targetPath));
              
              Graphics2D g2 = (Graphics2D) getGraphics();
              Color c = new Color(255, 0, 0, 40);
              g2.setColor(c);
              g2.fill(mCueLine);
              mPlugin = temp;
            }
          }
        } catch (Exception e2) {
          e.rejectDrag();
        }

        if (reject && !rejected) {
          e.rejectDrag();
          this.paintImmediately(mCueLine.getBounds());
          mPlugin = null;
          rejected = true;
        }

        Point location = (Point) e.getLocation().clone();

        if (flavors[0].getHumanPresentableName().equals("NodeExport")
            && ((mCurrentPoint.x != location.x) || (mCurrentPoint.y != location.y))) {

          if (!DragSource.isDragImageSupported()) {
            Graphics2D g2 = (Graphics2D) getGraphics();

            Rectangle2D old = (Rectangle2D) mGhostRect.clone();

            // Remember where you are about to draw the new ghost image
            mGhostRect.setRect(location.x + 20, location.y, mGhostImage
                .getWidth(), mGhostImage.getHeight());

            Rectangle2D target = (Rectangle2D) old.clone();
            target.add(mGhostRect);

            BufferedImage bf = new BufferedImage((int) target.getWidth(),
                (int) target.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g2b = bf.createGraphics();

            int width = ((int) (mTreeImage.getWidth() - (target.getX() + target
                .getWidth()))) > 0 ? (int) target.getWidth()
                : (int) (mTreeImage.getWidth() - (target.getX()));
            int height = ((int) (mTreeImage.getHeight() - (target.getY() + target
                .getHeight()))) > 0 ? (int) target.getHeight()
                : (int) (mTreeImage.getHeight() - (target.getY()));

            if (width > 0 && height > 0) {
              g2b.drawImage(mTreeImage.getSubimage((int) target.getX(),
                  (int) target.getY(), width, height), 0, 0, null);
            }

            if (mCueLine.contains(location) && !reject) {
              Rectangle2D temp1 = mCueLine.createIntersection(target);
              double y = 0;

              if (changed) {
                y = mCueLine.getY() - target.getY();
                changed = false;
              }
              temp1.setRect(0, y, temp1.getWidth(), temp1.getHeight());

              Color c = new Color(255, 0, 0, 40);
              g2b.setColor(c);
              if (mCueLine.contains(mCueLine.createIntersection(target))) {
                g2b.fill(temp1);
              }
            }

            g2b.drawImage(mGhostImage, AffineTransform.getTranslateInstance(
                mGhostRect.getX() - target.getX(), mGhostRect.getY()
                    - target.getY()), null);
            g2b.dispose();

            g2.drawImage(bf, AffineTransform.getTranslateInstance(
                target.getX(), target.getY()), null);
          }
          mCurrentPoint = location;
        }
      }
    } catch (Exception ee) {}

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

  public void dragOver(DropTargetDragEvent e) {
    checkAndPaintTarget(e);
  }

  public void dropActionChanged(DropTargetDragEvent e) {}

  public void dragExit(DropTargetEvent e) {
    paintImmediately(mCueLine.getBounds());
    paintImmediately(mGhostRect.getBounds());
    mPlugin = null;
  }

  public void drop(DropTargetDropEvent e) {
    e.acceptDrop(e.getDropAction());
    final Transferable tr = e.getTransferable();
    final Object src = e.getSource();
    final Point loc = e.getLocation();
    final PluginTree tree = this;
    e.dropComplete(true);
    mDropThread = new Thread("Plugin view drop") {
      public void run() {
        tree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        DataFlavor[] flavors = tr.getTransferDataFlavors();

        if (flavors != null && flavors.length == 1) {
          try {
            TreePath targetPath = ((PluginTree) ((DropTarget) src)
                .getComponent()).getPathForLocation(loc.x, loc.y);
            Node target = (Node) targetPath.getLastPathComponent();
            
            if(target.getProgramReceiveTarget() == null && targetPath.getPathCount() <= 2) {
              target = (Node) targetPath.getPathComponent(1);
            }

            if (flavors[0].getHumanPresentableName().equals("NodeExport")) {

              TreePath sourcePath = ((PluginTree) ((DropTarget) src)
                  .getComponent()).getSelectionPath();
              Node plugin = (Node) sourcePath.getPathComponent(1);
              Node source = (Node) sourcePath.getLastPathComponent();

              if (target.equals(plugin) || targetPath.isDescendant(sourcePath) || sourcePath.isDescendant(targetPath)) {
                return;
              } else {
                Vector<Program> vec;
                if (source.isLeaf()) {
                  vec = new Vector<Program>();
                  if (source.getUserObject() instanceof ProgramItem) {
                    vec.addElement(((ProgramItem) source.getUserObject())
                        .getProgram());
                  }
                } else {
                  vec = getLeafElements(source, new Vector<Program>());
                }
                Program[] p = vec.toArray(new Program[vec.size()]);
                
                if(p.length > 0) {
                  if (target.equals(ReminderPlugin.getInstance().getRootNode()
                      .getMutableTreeNode())) {
                      ReminderPlugin.getInstance().addPrograms(p);
                  } else if(target.getProgramReceiveTarget() == null) {
                    PluginProxy[] pa = PluginProxyManager.getInstance().getActivatedPlugins();
  
                    for (PluginProxy pluginAccess : pa) {
                      if (pluginAccess.getRootNode() != null) {
                        if (pluginAccess.getRootNode().getMutableTreeNode().equals(target)) {
                          if (pluginAccess.canReceiveProgramsWithTarget()
                              && pluginAccess.getProgramReceiveTargets() != null
                              && pluginAccess.getProgramReceiveTargets().length > 0) {
                            pluginAccess.receivePrograms(p,pluginAccess.getProgramReceiveTargets()[0]);
                          } else {
                            break;
                          }
                        }
                      }
                    }
                  }
                  else {
                    ProgramReceiveTarget receiveTarget = target.getProgramReceiveTarget();
                    receiveTarget.getReceifeIfForIdOfTarget().receivePrograms(p,receiveTarget);
                  }
                }
              }
            } else if (flavors[0].getHumanPresentableName().equals("Program")) {
              PluginProxy[] pa = PluginProxyManager.getInstance().getActivatedPlugins();

              boolean found = false;
              Program program = (Program) tr.getTransferData(flavors[0]);

              for (PluginProxy pluginAccess : pa) {
                if (pluginAccess.getRootNode().getMutableTreeNode().equals(target)) {
                  Action action = getAction(pluginAccess
                      .getContextMenuActions(program));

                  if (action != null) {
                    found = true;
                    ActionEvent evt = new ActionEvent(program, 0,
                        (String) action.getValue(Action.ACTION_COMMAND_KEY));
                    action.actionPerformed(evt);
                  }
                }
              }

              if (!found) {
                Action action = null;

                if (FavoritesPlugin.getInstance().getRootNode()
                    .getMutableTreeNode().equals(target)) {
                  action = getAction(FavoritesPluginProxy.getInstance()
                      .getContextMenuActions(program));
                } else if (ReminderPlugin.getInstance().getRootNode()
                    .getMutableTreeNode().equals(target)) {
                  action = getAction(ReminderPluginProxy.getInstance()
                      .getContextMenuActions(program));
                }

                if (action != null) {
                  found = true;
                  ActionEvent evt = new ActionEvent(program, 0, (String) action
                      .getValue(Action.ACTION_COMMAND_KEY));
                  action.actionPerformed(evt);
                }
              }
            }
          } catch (Exception ee) {}
        }
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            paintImmediately(mCueLine.getBounds());
            paintImmediately(mGhostRect.getBounds());
          };
        });

        mPlugin = null;
        tree.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    };
    mDropThread.setPriority(Thread.MIN_PRIORITY);
    mDropThread.start();
  }

  private Action getAction(ActionMenu menu) {
    while (menu != null && menu.hasSubItems()) {
      ActionMenu[] subItems = menu.getSubItems();
      if (subItems.length > 0) {
        menu = subItems[0];
      } else {
        menu = null;
      }
    }
    if (menu == null) {
      mPlugin = null;
      return null;
    }

    return (menu.getAction());
  }

  /**
   * Get the Programs of a node and it's child nodes
   * 
   * @param node
   *          Node to start the search
   * @param entries
   *          A Vector to store the Programs in
   * @return A vector with the Programs
   */
  private Vector<Program> getLeafElements(Node node, Vector<Program> entries) {
    for (int i = 0; i < node.getChildCount(); i++) {
      if (node.getChildAt(i).isLeaf()) {
        Node childnode = (Node) node.getChildAt(i);
        if (childnode.getUserObject() instanceof ProgramItem) {
          if (!entries.contains(((ProgramItem) childnode.getUserObject())
              .getProgram())) {
            entries.addElement(((ProgramItem) childnode.getUserObject())
                .getProgram());
          }
        }
      } else {
        entries = getLeafElements((Node) node.getChildAt(i), entries);
      }
    }
    return entries;
  }

  public void dragEnter(DragSourceDragEvent dsde) {}

  public void dragOver(DragSourceDragEvent dsde) {}

  public void dropActionChanged(DragSourceDragEvent dsde) {}

  public void dragDropEnd(DragSourceDropEvent dsde) {
    ((PluginTreeModel) this.getModel()).setDisableUpdate(false);
    mUpdateAllowed = true;
  }

  public void dragExit(DragSourceEvent dse) {}
  
  public void updateUI() {
    if(mUpdateAllowed) {
      setUI(new PluginTreeUI(SingleAndDoubleClickTreeUI.EXPAND_AND_COLLAPSE, getSelectionPath()));
      invalidate();
    }
  }
  
  private static class PluginTreeUI extends SingleAndDoubleClickTreeUI {
    private GraphicsConfiguration mGC;
    private JLabel mProgramLabel = new JLabel();

    protected PluginTreeUI(int type, TreePath selectionPath) {
      super(type, selectionPath);
    }

    protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf)  {      
      if(path.getLastPathComponent() instanceof Node && (tree.getSelectionPath() == null || !tree.getSelectionPath().equals(path))) {
        Node node = (Node)path.getLastPathComponent();
        
        if(node.getType() == Node.PROGRAM) {
          Program program = ((ProgramItem)node.getUserObject()).getProgram();
          
          if(UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel")) {
            bounds.setBounds(bounds.x,bounds.y+1,bounds.width,bounds.height);
          }
          
          boolean cleaned = false;
          
          /*if(program.getMarkerArr().length > 0 && program.getMarkPriority() >= Program.MIN_MARK_PRIORITY) {
            cleaned = true;
            
            if(!program.isExpired()) {
              g.setColor(Color.white);
              g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            
            switch(program.getMarkPriority()) {
              case Program.MIN_MARK_PRIORITY: g.setColor(Settings.propProgramPanelMarkedMinPriorityColor.getColor());break;
              case Program.LOWER_MEDIUM_MARK_PRIORITY: g.setColor(Settings.propProgramPanelMarkedLowerMediumPriorityColor.getColor());break;
              case Program.MEDIUM_MARK_PRIORITY: g.setColor(Settings.propProgramPanelMarkedMediumPriorityColor.getColor());break;
              case Program.HIGHER_MEDIUM_MARK_PRIORITY: g.setColor(Settings.propProgramPanelMarkedHigherMediumPriorityColor.getColor());break;
              case Program.MAX_MARK_PRIORITY: g.setColor(Settings.propProgramPanelMarkedMaxPriorityColor.getColor());break;
            }
            
            if(program.isExpired()) {
              g.setColor(new Color(g.getColor().getRed(), g.getColor().getGreen(), g.getColor().getBlue(), (int)(g.getColor().getAlpha()*6/10.)));
            }
            
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
          }*/
          
          if(program.isOnAir()) {
            if(!cleaned) {
              g.setColor(Color.white);
              g.fillRect(bounds.x, bounds.y+1, bounds.width, bounds.height-2);
            }
            
            int runTime = IOUtilities.getMinutesAfterMidnight() - program.getStartTime();
            if (runTime < 0) {
              runTime += 24 * 60;
            }
            int progressX = (int)((bounds.width)/(double)program.getLength() * runTime);
            
            g.setColor(Settings.propProgramTableColorOnAirDark.getColor());
            g.fillRect(bounds.x,bounds.y+1,progressX,bounds.height-2);            
            
            g.setColor(Settings.propProgramTableColorOnAirLight.getColor());
            g.fillRect(bounds.x + progressX,bounds.y+1,bounds.width-progressX,bounds.height-2);
          }
          
          if(program.isExpired()) {
            g.setColor(UIManager.getColor("ComboBox.disabledForeground"));            
          }
          else if(FilterManagerImpl.getInstance().getCurrentFilter().accept(program)) {
            g.setColor(UIManager.getColor("Label.foreground"));
          }
          else {
            g.setColor(Color.red);
          }
          
          String text = node.getNodeFormatter().format((ProgramItem)node.getUserObject());          
          
          // use an image to be able to display HTML content on the node
          BufferedImage textImage = getImage(bounds);
          Graphics lg = textImage.getGraphics();
          
          mProgramLabel.setFont(tree.getFont());
          mProgramLabel.setForeground(g.getColor());
          mProgramLabel.setText(text);
          mProgramLabel.setOpaque(false);
          mProgramLabel.setBounds(0, 0, bounds.width, bounds.height);
          mProgramLabel.paint(lg);
          
          g.drawImage(textImage, bounds.x, bounds.y, mProgramLabel);
          lg.dispose();
        }
        else {
          super.paintRow(g,clipBounds,insets,bounds,path,row,isExpanded,hasBeenExpanded,isLeaf);
        }
      }
      else {
        super.paintRow(g,clipBounds,insets,bounds,path,row,isExpanded,hasBeenExpanded,isLeaf);
      }
    }

    /**
     * Returns a new image with size of the defined bounds.<p>
     * This is used as target image to render the HTML label.
     * 
     * @param bounds
     * @return BufferedImage
     */
    private BufferedImage getImage(Rectangle bounds) {
      GraphicsConfiguration gc = getGraphicsConfiguration();
      BufferedImage textImage = gc.createCompatibleImage(bounds.width, bounds.height, BufferedImage.TRANSLUCENT);
      return textImage;
    }

    /**
     * Returns the default GraphicsConfiguration.
     * 
     * @return
     */
    private GraphicsConfiguration getGraphicsConfiguration() {
      if (mGC == null) {
        // Caching of the GraphicsConfiguration
        mGC = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice().getDefaultConfiguration();
      }
      return mGC;
    }
  }
}


