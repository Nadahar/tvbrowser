package util.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JList;

/**
 * A class for DnD in JLists and between two of them.
 * 
 * @author René Mach
 *
 */
public class ListDragAndDropHandler implements DropTargetListener,
    DragGestureListener {
  
  private JList mList1, mList2;
  private JList mSource, mTarget;
  private JList mCue = null;
  private ListDropAction mAction;
  
  private Rectangle2D mCueLine = new Rectangle2D.Float();
  private int mOldIndex = -1;
  private boolean mSwitched = false;
  
  private boolean mPaint1, mPaint2;

  /**
   * Cunstructor of this class.
   * 
   * @param list1 The first list.
   * @param list2 The second list (can be the same like the first).
   * @param action The Interface for the drop action.
   */
  public ListDragAndDropHandler(JList list1, JList list2, ListDropAction action) {
    mList1 = list1;
    mList2 = list2;
    mPaint1 = true;
    mPaint2 = true;
    new DropTarget(mList1, this);
    if(list2 != null && list1 != list2) {
      new DropTarget(mList2, this);
    }
    mAction = action;
  }
  
  /**
   * Enable printing of cueLines in the lists on DnD.
   * 
   * @param list1 Set this false to disables cueLine for the first list.
   * @param list2 Set this false to disables cueLine for the second list.
   */
  public void setPaintCueLine(boolean list1, boolean list2) {
    mPaint1 = list1;
    mPaint2 = list2;
  }
  
  public void dragGestureRecognized(DragGestureEvent e) {
    if(e.getComponent().equals(mList1)) {
      mSource = mList1;
      mTarget = mList2;
    }
    if(e.getComponent().equals(mList2)) {
      mSource = mList2;
      mTarget = mList1;
    }
    if(mSource.isEnabled()) {
      e.startDrag(null,new TransferEntries(mSource.getSelectedIndices(),"JList","Indices"));
    }
  }
  
  public void dragEnter(DropTargetDragEvent e) {

    
  }

  public void dragOver(DropTargetDragEvent e) {
    DataFlavor[] flavors = e.getCurrentDataFlavors();
    if(flavors != null && flavors.length == 2 &&
        flavors[0].getHumanPresentableName().equals("Indices") &&
        flavors[1].getHumanPresentableName().equals("Source")) {
      e.acceptDrag(e.getDropAction());
    }
    else {
      e.rejectDrag();
      return;
    }
    mCue = null;
    if(((DropTarget)e.getSource()).getComponent().equals(mSource)) {
      mCue = mSource;
    } else if (((DropTarget)e.getSource()).getComponent().equals(mTarget)) {
      mCue = mTarget;
    }
    
    if((!mPaint1 && mCue.equals(mList1)) || (!mPaint2 && mCue.equals(mList2))) {
      mCue = null;
    }
    
    if (mCue != null) {
      Point p = e.getLocation();
      Rectangle rect = mCue.getVisibleRect();
      int i = mCue.locationToIndex(p);
      
      if(i != -1) {
        Rectangle listRect = mCue.getCellBounds(mCue.locationToIndex(p),
            mCue.locationToIndex(p));
        Graphics2D g2 = (Graphics2D) mCue.getGraphics();
        boolean paint = false;
        
        if(listRect != null) {
          listRect.setSize(listRect.width,listRect.height/2);
          if(!listRect.contains(e.getLocation()) && !mSwitched && i == mOldIndex) {
            mCue.paintImmediately(mCueLine.getBounds());
            mCueLine.setRect(0,listRect.y + (listRect.height * 2) - 1,listRect.width,2);
            mSwitched = true;
            paint = true;
          }
          else if(listRect.contains(e.getLocation()) && i == mOldIndex && mSwitched) {
            mCue.paintImmediately(mCueLine.getBounds());
            mCueLine.setRect(0,listRect.y - 1,listRect.width,2);
            mSwitched = false;
            paint = true;
          }
          else if(i != mOldIndex && listRect.contains(e.getLocation())) {
            mCue.paintImmediately(mCueLine.getBounds());
            mCueLine.setRect(0,listRect.y - 1,listRect.width,2);
            mSwitched = false;
            mOldIndex = i;
            paint = true;
          }
          else if(i != mOldIndex && !listRect.contains(e.getLocation())) {
            mCue.paintImmediately(mCueLine.getBounds());
            mCueLine.setRect(0,listRect.y + (listRect.height * 2) - 1,listRect.width,2);
            mSwitched = true;
            mOldIndex = i;
            paint = true;
          }
          if(paint) {
            Color c = new Color(255,0,0,180);
            g2.setColor(c);
            g2.fill(mCueLine);
          }
        }
      }
      else {
        mOldIndex = -1;
        mCue.paintImmediately(mCueLine.getBounds());
      }
      
      if(p.y + 20 > rect.y + rect.height) {
        mCue.scrollRectToVisible(new Rectangle(p.x,p.y + 15,1,1));
      }
      if(p.y - 20 < rect.y) {
        mCue.scrollRectToVisible(new Rectangle(p.x,p.y - 15,1,1));
      }
    }
  }

  public void dropActionChanged(DropTargetDragEvent e) {
   
    
  }

  public void dragExit(DropTargetEvent e) {
    if(((DropTarget)e.getSource()).getComponent().equals(mTarget)) {
      mOldIndex = -1;
      mTarget.paintImmediately(mCueLine.getBounds());
    }
    else if(((DropTarget)e.getSource()).getComponent().equals(mSource)) {
      mOldIndex = -1;
      mTarget.paintImmediately(mCueLine.getBounds());
    }
  }

  public void drop(DropTargetDropEvent e) {
    e.acceptDrop(e.getDropAction());
    Transferable tr = e.getTransferable();
      
    DataFlavor[] flavors = tr.getTransferDataFlavors();
    
    // If theTransferable is a TransferEntries drop it
    if(flavors != null && flavors.length == 2 &&
        flavors[0].getHumanPresentableName().equals("Indices") &&
        flavors[1].getHumanPresentableName().equals("Source")) {
      try {
        JList target = (JList)((DropTarget)e.getSource()).getComponent();
        int x = target.locationToIndex(e.getLocation());
        
        Rectangle rect = target.getCellBounds(x,x);
        if(rect != null) {
          rect.setSize(rect.width,rect.height/2);
        
          if(!rect.contains(e.getLocation())) {
            x++;
          }
        } else {
          x = 0;
        }
                
        if(target.equals(mTarget)) {
          mAction.drop(mSource,mTarget,x,false);
        }
        else if(target.equals(mSource)) {
          mAction.drop(mSource,mSource,x,false);
        }
        
      }catch(Exception ee) {ee.printStackTrace();}
    }
    e.dropComplete(true);
  }


}
