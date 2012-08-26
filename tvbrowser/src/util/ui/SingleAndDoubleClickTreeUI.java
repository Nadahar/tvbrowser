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
package util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * A tree UI that expands paths for single and double click,
 * but collapse paths only if current path is selected for
 * single click or always for double click.
 * 
 * @author René Mach
 * @since 2.6
 */
public class SingleAndDoubleClickTreeUI extends javax.swing.plaf.basic.BasicTreeUI implements MouseListener {
  /** Type for expand and collapse only on user clicks */
  public static final int EXPAND_AND_COLLAPSE = 0;
  /** Type for expand and automatically collapse other paths when expand other paths */
  public static final int AUTO_COLLAPSE_EXPAND = 1;
  
  private static final int CLICK_WAIT_TIME = 150;
  private Thread mClickedThread;
  private long mMousePressedTime;
  private boolean mWasExpanded;
  private TreePath mLastSelectionPath;
  
  private int mType;
  
  private Icon mCollapsedIcon;
  private Icon mExpandedIcon;
  
  /**
   * Creates an instance of this plugin.
   * 
   * @param type The type of this UI.
   * @param selectedPath The currently selected path.
   */
  public SingleAndDoubleClickTreeUI(int type, TreePath selectedPath) {
    mType = type;
    mLastSelectionPath = selectedPath;
    
    if(UiUtilities.isGTKLookAndFeel()) {
      mCollapsedIcon = new Icon() {
        @Override
        public int getIconHeight() {
          return 6;
        }
  
        @Override
        public int getIconWidth() {
          return 6;
        }
  
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
          int xPoints[] = {x+1,x+1,x+6};
          int yPoints[] = {y,y+6,y+3};
          
          ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
          g.setColor(Color.gray);
          g.fillPolygon(xPoints, yPoints, 3);
          g.setColor(Color.darkGray);
          g.drawPolygon(xPoints, yPoints, 3);
        }        
      };
      
      mExpandedIcon = new Icon() {
        @Override
        public int getIconHeight() {
          return 5;
        }
  
        @Override
        public int getIconWidth() {
          return 7;
        }
  
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
          int xPoints[] = {x+1,x+4,x+7};
          int yPoints[] = {y,y+5,y};
          
          ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
          g.setColor(Color.gray);
          g.fillPolygon(xPoints, yPoints, 3);
          g.setColor(Color.darkGray);
          g.drawPolygon(xPoints, yPoints, 3);
        }        
      };
    }
  }
  
  protected MouseListener createMouseListener() {
    return this;
  }
  
  /**
   * Sets the last selected path to the given one.
   * 
   * @param path The path to set.
   */
  public void setLastSelectedPath(TreePath path) {
    mLastSelectionPath = path;
  }
  
  public void mousePressed(MouseEvent e) {
    if(!e.isConsumed()) {
      if(!tree.isFocusOwner()) {
        tree.requestFocusInWindow();
      }
      
      TreePath path = getClosestPathForLocation(tree, e.getX(), e.getY());
      
      if(path != null && getPathBounds(tree,path).contains(e.getPoint())) {
        tree.setSelectionPath(path);
      }
      /*else {
       tree.setSelectionPath(new TreePath(getModel().getRoot()));
      }*/
      
      mMousePressedTime = e.getWhen();
      
      checkForClickInExpandControl(getClosestPathForLocation(tree, e.getX(), e.getY()),e.getX(),e.getY());
      e.consume();
    }
  }
  
  public void mouseReleased(MouseEvent e) {
    if(!e.isConsumed()) {
      if(SwingUtilities.isLeftMouseButton(e)) {
        final TreePath path = getClosestPathForLocation(tree, e.getX(), e.getY());
        
        if(path != null && !((DefaultMutableTreeNode)path.getLastPathComponent()).isLeaf() && ((mType == EXPAND_AND_COLLAPSE && (e.getWhen() - mMousePressedTime) < CLICK_WAIT_TIME) || mType != EXPAND_AND_COLLAPSE) && getPathBounds(tree,path).contains(e.getPoint())) {
          if(mClickedThread == null || !mClickedThread.isAlive()) {
            mClickedThread = new Thread("Single click tree UI double click") {
              public void run() {
                if(!tree.isExpanded(path)) {
                  tree.expandPath(path);
                  mWasExpanded = true;
                  
                  if(mType == AUTO_COLLAPSE_EXPAND && mLastSelectionPath != null && !mLastSelectionPath.isDescendant(path) && !path.isDescendant(mLastSelectionPath)) {
                    tree.collapsePath(mLastSelectionPath);
                  }
                }
                else if(mLastSelectionPath != null && tree.getSelectionPath().equals(mLastSelectionPath)){
                  tree.collapsePath(path);
                  mWasExpanded = false;
                }
                tree.setSelectionPath(path);
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
            tree.collapsePath(path);
          }
        }
        else {
          mLastSelectionPath = path;
        }
      }
      e.consume();
    }
  }
  
  protected void paintHorizontalLine(Graphics g,JComponent c,int y,int left,int right) {
    if(!UiUtilities.isGTKLookAndFeel()) {
      super.paintHorizontalLine(g, c, y, left, right);
    }
  }
  
  protected void paintVerticalLine(Graphics g,JComponent c,int x,int top,int bottom) {
    if(!UiUtilities.isGTKLookAndFeel()) {
      super.paintVerticalLine(g, c, x, top, bottom);
    }
  }
  
  public Icon getCollapsedIcon() {
    return mCollapsedIcon != null ? mCollapsedIcon : super.getCollapsedIcon();
  }
  
  public Icon getExpandedIcon() {
    return mExpandedIcon != null ? mExpandedIcon : super.getExpandedIcon();
  }

  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}
}