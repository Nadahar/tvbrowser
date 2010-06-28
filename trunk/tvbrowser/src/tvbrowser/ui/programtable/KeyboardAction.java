package tvbrowser.ui.programtable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;

/**
 * A class for keyboard selection of programs in the ProgramTable.
 * 
 * @author René Mach
 * 
 */
public class KeyboardAction implements ActionListener {

  public static final int KEY_UP = 0;
  public static final int KEY_DOWN = 1;
  public static final int KEY_RIGHT = 2;
  public static final int KEY_LEFT = 3;
  public static final int KEY_CONTEXTMENU = 4;
  public static final int KEY_DESELECT = 5;
  public static final int KEY_MIDDLECLICK = 6;
  public static final int KEY_DOUBLECLICK = 7;
  public static final int KEY_SINGLECLICK = 8;
  public static final int KEY_MIDDLE_DOUBLE_CLICK = 8;

  private ProgramTableScrollPane mScrollPane;

  private int mType;

  /**
   * 
   * @param pane The ProgramTableScrollPane to start the action on.
   * @param type The Type of Action ( KEY_UP, KEY_DOWN, ...)
   */
  public KeyboardAction(ProgramTableScrollPane pane, int type) {
    mScrollPane = pane;
    mType = type;
  }

  public void actionPerformed(ActionEvent e) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (mType == KEY_UP) {
          mScrollPane.up();
        }
        if (mType == KEY_DOWN) {
          mScrollPane.down();
        }
        if (mType == KEY_LEFT) {
          mScrollPane.left();
        }
        if (mType == KEY_RIGHT) {
          mScrollPane.right();
        }
        if (mType == KEY_CONTEXTMENU) {
          mScrollPane.showPopupMenu();
        }
        if (mType == KEY_DESELECT) {
          mScrollPane.deSelectItem();
        }
        if (mType == KEY_MIDDLECLICK) {
          mScrollPane.handleMiddleClick();
        }
        if (mType == KEY_DOUBLECLICK) {
          mScrollPane.handleDoubleClick();
        }
        if (mType == KEY_SINGLECLICK) {
          mScrollPane.handleLeftSingleClick();
        }
        if (mType == KEY_MIDDLE_DOUBLE_CLICK) {
          mScrollPane.handleMiddleDoubleClick();
        }
      };
    });
  }
}