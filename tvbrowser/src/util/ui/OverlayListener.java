package util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.TreePath;

/**
 *  A OverlayListener
 * 
 *  This Class shows partially visible JTree Nodes in a
 *  GlassPane.
 * 
 *  This Class was found at Santosh's Blog:
 *  http://www.jroller.com/page/santhosh/20050522#partially_visible_jtree_nodes
 */
public class OverlayListener extends MouseInputAdapter{
  private JTree mTree;
  private Component mOldGlassPane;
  private TreePath mPath;
  private int mRow;
  private Rectangle mBounds;

  public OverlayListener(JTree tree){
      this.mTree = tree;
      tree.addMouseListener(this);
      tree.addMouseMotionListener(this);
  }

  JComponent c = new JComponent(){
      public void paint(Graphics g){
          boolean selected = mTree.isRowSelected(mRow);
          Component renderer = mTree.getCellRenderer().getTreeCellRendererComponent(mTree, mPath.getLastPathComponent(),
                  mTree.isRowSelected(mRow), mTree.isExpanded(mRow), mTree.getModel().isLeaf(mPath.getLastPathComponent()), mRow,
                  selected);
          
          if(renderer instanceof JComponent) {
            ((JComponent)renderer).setOpaque(false);
          }
          
          c.setFont(mTree.getFont());
          Rectangle paintBounds = SwingUtilities.convertRectangle(mTree, mBounds, this);
          
          
          if(UIManager.getLookAndFeel().getClass().getCanonicalName().equals("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel")) {
            Color current = g.getColor();
            
            if(selected) {
              g.setColor(UIManager.getColor("Tree.selectionBackground"));
            }
            else {
              g.setColor(mTree.getBackground());
            }
            
            g.fillRect(paintBounds.x,paintBounds.y,paintBounds.width,paintBounds.height);
            
            g.setColor(current);
          }
          
          SwingUtilities.paintComponent(g, renderer, this, paintBounds);
          
          if(selected) {
            return;
          }

          g.setColor(Color.blue);
          ((Graphics2D)g).draw(paintBounds);
       }
  };

  public void mouseExited(MouseEvent e){
      resetGlassPane();
  }

  private void resetGlassPane(){
      if(mOldGlassPane!=null){
          c.setVisible(false);
          mTree.getRootPane().setGlassPane(mOldGlassPane);
          mOldGlassPane = null;
      }
  }

  public void mousePressed(MouseEvent e) {
    resetGlassPane();
    super.mousePressed(e);
  }
  
  public void mouseMoved(MouseEvent me){
      mPath = mTree.getPathForLocation(me.getX(), me.getY());
      if(mPath==null){
          resetGlassPane();
          return;
      }
      mRow = mTree.getRowForPath(mPath);
      mBounds = mTree.getPathBounds(mPath);
      if(!mTree.getVisibleRect().contains(mBounds)){
          if(mOldGlassPane==null){
              mOldGlassPane = mTree.getRootPane().getGlassPane();
              c.setOpaque(false);
              mTree.getRootPane().setGlassPane(c);
              c.setVisible(true);
          } else {
            mTree.getRootPane().repaint();
          }
      }else{
          resetGlassPane();
      }
  }
}