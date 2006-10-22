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
import java.awt.Graphics2D;
import java.awt.Image;
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

import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import util.ui.OverlayListener;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;
import devplugin.ProgramItem;
import devplugin.ProgramReceiveTarget;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org) Date: 01.01.2005 Time:
 * 20:25:18
 */
public class PluginTree extends JTree implements DragGestureListener,
    DropTargetListener, DragSourceListener {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(PluginTree.class);

  private Rectangle2D mCueLine = new Rectangle2D.Float();
  private Object mPlugin = null;
  private Thread mDropThread = null;
  private String mDragNode = null;
  private Point mCurrentPoint = null;
  private Rectangle2D mGhostRect = new Rectangle2D.Float();
  private BufferedImage mGhostImage, mTreeImage;
  private boolean rejected = false;
  private static PluginTree mInstance;

  public PluginTree(TreeModel model) {
    super(model);
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
            StringBuffer buf = new StringBuffer(o.toString());
            if (leafs > 0) {
              buf.append(" [").append(leafs).append("]");
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
    for (int i = 0; i < node.getChildCount(); i++) {
      if (node.getChildAt(i).isLeaf()) {
        count++;
      } else {
        count += getLeafCount(node.getChildAt(i));
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

  class TransferNode implements Transferable {
    private DataFlavor mDF;

    public TransferNode() {
      mDF = new DataFlavor(TreePath.class, "NodeExport");
    }

    public DataFlavor[] getTransferDataFlavors() {
      DataFlavor[] f = { mDF };
      return f;
    }

    public boolean isDataFlavorSupported(DataFlavor e) {
      if (e.equals(mDF))
        return true;
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
      if (tree.getLastSelectedPathComponent() == null)
        return;

      Node node = (Node) tree.getSelectionPath().getLastPathComponent();
      if (node.getType() != Node.ROOT
          && (node.getLeafCount() > 0 || node.isLeaf())) {

        ((PluginTreeModel) this.getModel()).setDisableUpdate(true);

        Vector<Program> vec = this.getLeafElements(node, new Vector<Program>());

        if (vec.size() == 1) {
          mDragNode = ((Program) vec.firstElement()).getTitle();
        } else if (node.isLeaf())
          mDragNode = ((ProgramItem) node.getUserObject()).getProgram()
              .getTitle();
        else
          mDragNode = vec.size() + " " + mLocalizer.msg("programs", "Programs");

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
        mGhostImage = new BufferedImage((int) lbl2.getWidth(), (int) lbl2
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

        mCurrentPoint = (Point) e.getDragOrigin();

        e.startDrag(null, (Image) mGhostImage, new Point(15, 5),
            new TransferNode(), this);
      }
    } catch (Exception ee) {

      ((PluginTreeModel) this.getModel()).setDisableUpdate(false);
    }
  }

  public void dragEnter(DropTargetDragEvent e) {}

  public void dragOver(DropTargetDragEvent e) {
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

          Node target = (Node) targetPath.getPathComponent(1);

          if (flavors[0].getHumanPresentableName().equals("NodeExport")) {
            TreePath sourcePath = ((PluginTree) ((DropTarget) e.getSource())
                .getComponent()).getSelectionPath();
            Node plugin = (Node) sourcePath.getPathComponent(1);

            if (!target.equals(plugin) && targetPath.getPathCount() <= 2) {
              if (target.equals(ReminderPlugin.getInstance().getRootNode()
                  .getMutableTreeNode())) {
                e.acceptDrag(e.getDropAction());
                reject = false;
                temp = ReminderPlugin.getInstance();
                rejected = false;
              } else {
                PluginAccess[] pa = Plugin.getPluginManager()
                    .getActivatedPlugins();

                for (int i = 0; i < pa.length; i++) {
                  if (pa[i].getRootNode().getMutableTreeNode().equals(target)) {
                    if ((pa[i].canReceivePrograms() || pa[i].canReceiveProgramsWithTarget())) {
                      e.acceptDrag(e.getDropAction());
                      reject = false;
                      temp = pa[i];
                      rejected = false;
                    } else {
                      mPlugin = null;
                      break;
                    }
                  }
                }
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
                PluginAccess[] pa = Plugin.getPluginManager()
                    .getActivatedPlugins();

                for (int i = 0; i < pa.length; i++) {
                  if (pa[i].getRootNode().getMutableTreeNode().equals(target)) {

                    /*
                     * This would only work with Java 1.5
                     * 
                     * Transferable tr = e.getTransferable(); Program program =
                     * (Program) tr.getTransferData(flavors[0]);
                     */

                    if (getAction(pa[i].getContextMenuActions(Plugin
                        .getPluginManager().getExampleProgram())) == null) {
                      mPlugin = null;
                      break;
                    }

                    e.acceptDrag(e.getDropAction());
                    reject = false;
                    rejected = false;
                    temp = pa[i];
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
            Graphics2D g2b = (Graphics2D) bf.createGraphics();

            int width = ((int) (mTreeImage.getWidth() - (target.getX() + target
                .getWidth()))) > 0 ? (int) target.getWidth()
                : (int) (mTreeImage.getWidth() - (target.getX()));
            int height = ((int) (mTreeImage.getHeight() - (target.getY() + target
                .getHeight()))) > 0 ? (int) target.getHeight()
                : (int) (mTreeImage.getHeight() - (target.getY()));

            if (width > 0 && height > 0)
              g2b.drawImage(mTreeImage.getSubimage((int) target.getX(),
                  (int) target.getY(), width, height), 0, 0, null);

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
              if (mCueLine.contains(mCueLine.createIntersection(target)))
                g2b.fill(temp1);
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
      if (e.getLocation().y + scroll + 5 > getVisibleRect().height)
        scrollRectToVisible(new Rectangle(e.getLocation().x, e.getLocation().y
            + scroll + 5, 1, 1));
      if (e.getLocation().y - scroll < getVisibleRect().y)
        scrollRectToVisible(new Rectangle(e.getLocation().x, e.getLocation().y
            - scroll, 1, 1));
      if (e.getLocation().x - scroll < getVisibleRect().x)
        scrollRectToVisible(new Rectangle(e.getLocation().x - scroll, e
            .getLocation().y, 1, 1));
      if (e.getLocation().x + scroll + 5 > getVisibleRect().width)
        scrollRectToVisible(new Rectangle(e.getLocation().x + scroll + 5, e
            .getLocation().y, 1, 1));
    }
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
    mDropThread = new Thread() {
      public void run() {
        tree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        DataFlavor[] flavors = tr.getTransferDataFlavors();

        if (flavors != null && flavors.length == 1) {
          try {
            TreePath targetPath = ((PluginTree) ((DropTarget) src)
                .getComponent()).getPathForLocation(loc.x, loc.y);
            Node target = (Node) targetPath.getPathComponent(1);

            if (flavors[0].getHumanPresentableName().equals("NodeExport")) {

              TreePath sourcePath = ((PluginTree) ((DropTarget) src)
                  .getComponent()).getSelectionPath();
              Node plugin = (Node) sourcePath.getPathComponent(1);
              Node source = (Node) sourcePath.getLastPathComponent();

              if (target.equals(plugin) || targetPath.getPathCount() > 2) {
                return;
              } else {
                if (target.equals(ReminderPlugin.getInstance().getRootNode()
                    .getMutableTreeNode())) {
                  Vector<Program> vec;
                  if (source.isLeaf()) {
                    vec = new Vector<Program>();
                    if (source.getUserObject() instanceof ProgramItem)
                      vec.addElement(((ProgramItem) source.getUserObject())
                          .getProgram());
                  } else
                    vec = getLeafElements(source, new Vector<Program>());
                  Program[] p = new Program[vec.size()];
                  if (p.length > 0) {
                    vec.toArray(p);
                    ReminderPlugin.getInstance().addPrograms(p);
                  }
                } else {
                  PluginAccess[] pa = Plugin.getPluginManager()
                      .getActivatedPlugins();

                  for (int i = 0; i < pa.length; i++) {
                    if (pa[i].getRootNode().getMutableTreeNode().equals(target)) {
                      if ((pa[i].canReceivePrograms() || pa[i].canReceiveProgramsWithTarget())) {
                        Vector<Program> vec;
                        if (source.isLeaf()) {
                          vec = new Vector<Program>();
                          if (source.getUserObject() instanceof ProgramItem)
                            vec.addElement(((ProgramItem) source
                                .getUserObject()).getProgram());
                        } else
                          vec = getLeafElements(source, new Vector<Program>());
                        Program[] p = new Program[vec.size()];
                        if (p.length > 0) {
                          vec.toArray(p);
                          pa[i].receivePrograms(p,ProgramReceiveTarget.createDefaultTargetForProgramReceiveIfId(pa[i].getId()));
                        }
                      } else {
                        break;
                      }
                    }
                  }
                }
              }
            } else if (flavors[0].getHumanPresentableName().equals("Program")) {
              PluginAccess[] pa = Plugin.getPluginManager()
                  .getActivatedPlugins();

              boolean found = false;
              Program program = (Program) tr.getTransferData(flavors[0]);

              for (int i = 0; i < pa.length; i++) {
                if (pa[i].getRootNode().getMutableTreeNode().equals(target)) {
                  Action action = getAction(pa[i]
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
                    .getMutableTreeNode().equals(target))
                  action = getAction(FavoritesPlugin.getInstance()
                      .getContextMenuActions(program));
                else if (ReminderPlugin.getInstance().getRootNode()
                    .getMutableTreeNode().equals(target))
                  action = getAction(ReminderPlugin.getInstance()
                      .getContextMenuActions(program));

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
              .getProgram()))
            entries.addElement(((ProgramItem) childnode.getUserObject())
                .getProgram());
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
  }

  public void dragExit(DragSourceEvent dse) {}
}
