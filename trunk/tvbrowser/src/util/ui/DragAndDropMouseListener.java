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

  /**
   * 
   * @param source The source list of DnD.
   * @param target The target list of DnD.
   * @param action The action of DnD.
   * @param listener The GestureListener of DnD.
   */
  public DragAndDropMouseListener(JList source, JList target,
      ListDropAction action, DragGestureListener listener) {
    mSource = source;
    mTarget = target;
    mAction = action;
    mListener = listener;

    restore();
  }

  /**
   * Rebuilds the correct MouseListener behavior.
   */
  public void restore() {
    MouseListener[] listeners = mSource.getMouseListeners();
    for (int i = 0; i < listeners.length; i++)
      mSource.removeMouseListener(listeners[i]);

    MouseMotionListener[] mlisteners = mSource.getMouseMotionListeners();
    for (int i = 0; i < mlisteners.length; i++)
      mSource.removeMouseMotionListener(mlisteners[i]);

    (new DragSource()).createDefaultDragGestureRecognizer(mSource,
        DnDConstants.ACTION_MOVE, mListener);

    mSource.addMouseListener(this);
  }

  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && mSource != mTarget) {
      int index = mSource.locationToIndex(e.getPoint());
      mSource.setSelectedIndex(index);
      mAction.drop(mSource, mTarget, 0, true);
      mLastSelectedIndex = index;
    }
  }

  // Rebuild the selection of the JList with the needed
  // functions.
  public void mousePressed(MouseEvent e) {
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

  // Rebuild the selection of the JList with the needed
  // functions.
  public void mouseReleased(MouseEvent e) {
    int index = mSource.locationToIndex(e.getPoint());

    if (e.isControlDown() && SwingUtilities.isLeftMouseButton(e)) {
      DefaultListSelectionModel model = (DefaultListSelectionModel) mSource
          .getSelectionModel();

      if (model.isSelectedIndex(index))
        model.removeSelectionInterval(index, index);
      else
        model.addSelectionInterval(index, index);

      mLastSelectedIndex = index;
    } else if (!e.isShiftDown() && !e.isControlDown()
        && SwingUtilities.isLeftMouseButton(e)) {
      mSource.setSelectedIndex(index);
      mLastSelectedIndex = index;
    }
  }
}
