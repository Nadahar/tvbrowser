package tvbrowser.ui.programtable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A class for keyboard selection of programs in the ProgramTable.
 * 
 * @author René Mach
 *
 */
public class KeyboardAction implements ActionListener {
  
  private ProgramTableScrollPane mScrollPane;
  private boolean mUp,mRight,mDown,mLeft,mContextMenu,mDeselect,mMiddleClick,mDoubleClick;
  
  /**
   * 
   * @param pane The ProgramTableScrollPane to start the action on.
   * @param up Action to go up?
   * @param right Action to go right?
   * @param down Action to go down?
   * @param left Action to go left?
   * @param context Action to open context menu?
   * @param deselect Action to deselect the selected program?
   * @param middleClick Action to start the middle click Plugin?
   * @param doubleClick Action to start the double click Plugin?
   */  
  public KeyboardAction(ProgramTableScrollPane pane, boolean up, boolean right, boolean down, boolean left, boolean context, boolean deselect , boolean middleClick, boolean doubleClick) {
    mScrollPane = pane;
    mUp = up;
    mRight = right;
    mDown = down;
    mLeft = left;
    mContextMenu = context;
    mDeselect = deselect;
    mMiddleClick = middleClick;
    mDoubleClick = doubleClick;    
  }
  
  public void actionPerformed(ActionEvent e) {
    if(mUp)
      mScrollPane.up();
    if(mRight)
      mScrollPane.right();
    if(mDown)
      mScrollPane.down();
    if(mLeft)
      mScrollPane.left();
    if(mContextMenu)
      mScrollPane.showPopupMenu();
    if(mDeselect)
      mScrollPane.deSelectItem();
    if(mMiddleClick)
      mScrollPane.handleMiddleClick();
    if(mDoubleClick)
      mScrollPane.handleDoubleClick();
  }
}
