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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.InputMap;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import util.ui.OverlayListener;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;
import devplugin.ProgramItem;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 01.01.2005
 * Time: 20:25:18
 */
public class PluginTree extends JTree implements DragGestureListener,DropTargetListener{

  private Rectangle2D mCueLine = new Rectangle2D.Float();
  private PluginAccess mPlugin = null;
  
  public PluginTree(TreeModel model) {
    super(model);

    /* remove the F2 key from the keyboard bindings of the JTree */
    InputMap inputMap = getInputMap();
    KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
    inputMap.put(keyStroke,"none");
    
    new OverlayListener(this);
    (new DragSource()).createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_MOVE,this);
    new DropTarget(this,this);
  }

  public String convertValueToText(Object value, boolean selected,
                                     boolean expanded, boolean leaf, int row,
                                     boolean hasFocus) {
    if (value instanceof Node) {
      Node node = (Node)value;
      Object o = node.getUserObject();
      if (o instanceof ProgramItem) {
        ProgramItem programItem = (ProgramItem)o;
        return node.getNodeFormatter().format(programItem);
      }
      else if (o != null) {
        if(!leaf) {
          int leafs = getLeafCount(node);
          String leafString = leafs > 0 ? " [" + leafs + "]" : "";
          if(node.isShowLeafCount())
            return o.toString() + leafString;
          else
            return o.toString();
        }
        return o.toString(); 
      }
      else {
        return "null";
      }
    }
    else {
      return value.toString();
    }
  }

  /**
   * Calculates the number of Childs
   * @param node use this Node
   * @return Number of Child-Nodes
   */
  private int getLeafCount(TreeNode node) {
    int count = 0;
    for(int i=0; i<node.getChildCount(); i++) {
        if(node.getChildAt(i).isLeaf()) {
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
      for (int i=0; i<cnt; i++) {
        Object node = model.getChild(comp, i);
        expandAll(path.pathByAddingChild(node));
      }
    }
  }

  class TransferNode implements Transferable {
    private DataFlavor mDF;
    
    public TransferNode() {
      mDF = new DataFlavor(TreePath.class,"NodeExport");
    }
    
    public DataFlavor[] getTransferDataFlavors() {
      DataFlavor[] f = {mDF};
      return f;
    }

    public boolean isDataFlavorSupported(DataFlavor e) {
      if(e.equals(mDF))
        return true;
      return false;
    }

    public Object getTransferData(DataFlavor e) throws UnsupportedFlavorException, IOException {
      return null;
    }
    
  }
  
  public void dragGestureRecognized(DragGestureEvent e) {
    PluginTree tree = (PluginTree)e.getComponent();
    if(tree.getLastSelectedPathComponent() == null)
      return;
    
    Node node = (Node)tree.getSelectionPath().getLastPathComponent();
    if(node.getType() != Node.ROOT && (getLeafCount(node) > 0 || node.isLeaf()))
      e.startDrag(null,new TransferNode());
  }
  

  public void dragEnter(DropTargetDragEvent e) {

    
  }

  public void dragOver(DropTargetDragEvent e) {
    boolean reject = true;
    PluginAccess temp = null;
    try {      
      DataFlavor[] flavors = e.getCurrentDataFlavors(); 
      
      if(flavors != null && flavors.length == 1 && 
          flavors[0].getHumanPresentableName().equals("NodeExport")) {
        
        TreePath targetPath = ((PluginTree)((DropTarget)e.getSource()).getComponent()).getPathForLocation(e.getLocation().x,e.getLocation().y);        
        Node target = (Node)targetPath.getPathComponent(1);
        
        TreePath sourcePath = ((PluginTree)((DropTarget)e.getSource()).getComponent()).getSelectionPath();
        Node plugin = (Node)sourcePath.getPathComponent(1);    

        if(!target.equals(plugin) && targetPath.getPathCount() <= 2) {
          PluginAccess[] pa = Plugin.getPluginManager().getActivatedPlugins();
          
          for(int i = 0; i < pa.length; i++) {
            if(pa[i].getRootNode().getMutableTreeNode().equals(target)) {
              if(pa[i].canReceivePrograms()) {
                e.acceptDrag(e.getDropAction());
                reject = false;
                temp = pa[i];
              }
              else {
                mPlugin = null;
                break;
              }
            }
          }
        }
        if(!reject && (mPlugin == null || temp != mPlugin)) {
          this.paintImmediately(mCueLine.getBounds());
          mCueLine.setRect(((PluginTree)((DropTarget)e.getSource()).getComponent()).getPathBounds(targetPath));
          Graphics2D g2 = (Graphics2D) this.getGraphics();
          Color c = new Color(255,0,0,40);
          g2.setColor(c);
          g2.fill(mCueLine);
          mPlugin = temp;
        }
      }
    }catch(Exception ee){}
    
    if(reject) {
      e.rejectDrag();
      this.paintImmediately(mCueLine.getBounds());
      mPlugin = null;
    }
  }

  public void dropActionChanged(DropTargetDragEvent e) {

    
  }

  public void dragExit(DropTargetEvent e) {
    this.paintImmediately(mCueLine.getBounds());
    mPlugin = null;
  }

  public void drop(DropTargetDropEvent e) {
    e.acceptDrop(e.getDropAction());
    Transferable tr = e.getTransferable();
    
    DataFlavor[] flavors = tr.getTransferDataFlavors(); 
    
    if(flavors != null && flavors.length == 1 && 
        flavors[0].getHumanPresentableName().equals("NodeExport")) {
      try {
      TreePath targetPath = ((PluginTree)((DropTarget)e.getSource()).getComponent()).getPathForLocation(e.getLocation().x,e.getLocation().y);
      System.out.println(targetPath);
      Node target = (Node)targetPath.getPathComponent(1);
      
      TreePath sourcePath = ((PluginTree)((DropTarget)e.getSource()).getComponent()).getSelectionPath();
      Node plugin = (Node)sourcePath.getPathComponent(1);
      Node source = (Node)sourcePath.getLastPathComponent();
      
      if(target.equals(plugin) || targetPath.getPathCount() > 2) {
        return;
      }
      else {
        PluginAccess[] pa = Plugin.getPluginManager().getActivatedPlugins();
        
        for(int i = 0; i < pa.length; i++) {
          if(pa[i].getRootNode().getMutableTreeNode().equals(target)) {
            if(pa[i].canReceivePrograms()) {
              Vector vec;
              if(source.isLeaf()) {
                vec = new Vector();
                if(source.getUserObject() instanceof ProgramItem)
                  vec.addElement(((ProgramItem)source.getUserObject()).getProgram());
              }
              else
                vec = this.getLeafElements(source,new Vector());
              Program[] p = new Program[vec.size()];
              if(p.length > 0) {
                vec.toArray(p);
                pa[i].receivePrograms(p);
              }
            }
            else {
              break;
            }
          }
        }
      }
      }catch(Exception ee){}
    }
    this.paintImmediately(mCueLine.getBounds());
  }
  
  /**
   * Get the Programs of a node and it's child nodes
   * @param node Node to start the search
   * @param entries A Vector to store the Programs in
   * @return A vector with the Programs
   */
  private Vector getLeafElements(Node node, Vector entries) {    
    for(int i=0; i<node.getChildCount(); i++) {
        if(node.getChildAt(i).isLeaf()) {
          Node childnode = (Node)node.getChildAt(i);
          if(childnode.getUserObject() instanceof ProgramItem) {            
            if(!entries.contains(((ProgramItem)childnode.getUserObject()).getProgram()))
              entries.addElement(((ProgramItem)childnode.getUserObject()).getProgram());
          }
        } else {
            entries = getLeafElements((Node)node.getChildAt(i),entries);
        }
    }   
    return entries;
  }
}

