/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package util.ui;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

/**
 * This class implements a MouseListener for DnD in JLists.
 * 
 * @author René Mach
 * 
 */
public class DragAndDropMouseListener extends MouseAdapter {
  private int mLastSelectedIndex = 0;
  private JList mSource, mTarget;
  private ListDropAction mAction;
  private DragGestureListener mListener;
  private boolean mMoveWithDoubleClick;

  /**
   * 
   * @param source
   *          The source list of DnD.
   * @param target
   *          The target list of DnD.
   * @param action
   *          The action of DnD.
   * @param listener
   *          The GestureListener of DnD.
   */
  public DragAndDropMouseListener(JList source, JList target,
      ListDropAction action, DragGestureListener listener) {
    this(source,target,action,listener,true);
  }
  
  /**
   * 
   * @param source
   *          The source list of DnD.
   * @param target
   *          The target list of DnD.
   * @param action
   *          The action of DnD.
   * @param listener
   *          The GestureListener of DnD.
   * @param moveWithDoubleClick
   *          If the items should be moved on double click.           
   */
  public DragAndDropMouseListener(JList source, JList target,
      ListDropAction action, DragGestureListener listener, boolean moveWithDoubleClick) {
    mSource = source;
    mTarget = target;
    mAction = action;
    mListener = listener;
    mMoveWithDoubleClick = moveWithDoubleClick;
    
    restore();
  }

  /**
   * Rebuilds the correct MouseListener behavior.
   */
  public void restore() {
    MouseListener[] listeners = mSource.getMouseListeners();
    for (int i = 0; i < listeners.length; i++) {
      if(!(listeners[i] instanceof ToolTipManager)) {
        mSource.removeMouseListener(listeners[i]);
      }
    }
    
    MouseMotionListener[] mlisteners = mSource.getMouseMotionListeners();
    for (int i = 0; i < mlisteners.length; i++) {
      if(!(mlisteners[i] instanceof ToolTipManager)) {
        mSource.removeMouseMotionListener(mlisteners[i]);
      }
    }
    
    (new DragSource()).createDefaultDragGestureRecognizer(mSource,
        DnDConstants.ACTION_MOVE, mListener);
    
    mSource.addMouseListener(this);
  }

  public void mouseClicked(MouseEvent e) {
    if (mSource.isEnabled() && !mSource.hasFocus()) {
      mSource.requestFocusInWindow();
    }
    
    if (mSource.isEnabled() && SwingUtilities.isLeftMouseButton(e)
        && e.getClickCount() == 2 && mSource != mTarget && mMoveWithDoubleClick) {
      int index = mSource.locationToIndex(e.getPoint());
      mSource.setSelectedIndex(index);
      mAction.drop(mSource, mTarget, 0, true);
      mLastSelectedIndex = index;
    }
  }

  // Rebuild the selection of the JList with the needed
  // functions.
  public void mousePressed(MouseEvent e) {
    if (mSource.isEnabled()) {
      if (e.isShiftDown() && SwingUtilities.isLeftMouseButton(e)) {
        int index = mSource.locationToIndex(e.getPoint());
        mSource.setSelectionInterval(index, mLastSelectedIndex);
      } else if (!e.isControlDown() && !e.isShiftDown()
          && SwingUtilities.isLeftMouseButton(e)) {
        DefaultListSelectionModel model = (DefaultListSelectionModel) mSource
            .getSelectionModel();
        int index = mSource.locationToIndex(e.getPoint());
        if (!model.isSelectedIndex(index)) {
          mSource.setSelectedIndex(index);
          mLastSelectedIndex = index;
        }
      }
    }
  }

  // Rebuild the selection of the JList with the needed
  // functions.
  public void mouseReleased(MouseEvent e) {
    int index = mSource.locationToIndex(e.getPoint());

    if (mSource.isEnabled()) {
      if (e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {
        DefaultListSelectionModel model = (DefaultListSelectionModel) mSource
            .getSelectionModel();

        if (model.isSelectedIndex(index)) {
          model.removeSelectionInterval(index, index);
        } else {
          model.addSelectionInterval(index, index);
        }

        mLastSelectedIndex = index;
      } else if (!e.isShiftDown() && !e.isControlDown()
          && SwingUtilities.isLeftMouseButton(e)) {
        mSource.setSelectedIndex(index);
        mLastSelectedIndex = index;
      }
    }
  }
}
