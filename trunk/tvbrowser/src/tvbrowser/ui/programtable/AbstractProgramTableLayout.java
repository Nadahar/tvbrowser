/*
 * AbstractProgramTableLayout.java
 *
 * Created on 24. Mai 2003, 12:35
 */

package tvbrowser.ui.programtable;

/**
 *
 * @author  Til
 */
public abstract class AbstractProgramTableLayout implements ProgramTableLayout {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(AbstractProgramTableLayout.class.getName());
  
  /** Holds the y position of the first program of a column. */
  private int[] mColumnStartArr;
  
  
  
  /**
   * Creates a new instance of AbstractProgramTableLayout
   */
  public AbstractProgramTableLayout() {
  }

  
  
  public int getColumnStart(int col) {
    if (mColumnStartArr == null) {
      mLog.warning("Cell starts are not yet initialized!");
      return 0;
    }
    else if ((col < 0) || (col >= mColumnStartArr.length)) {
      mLog.warning("Column out of bounds " + col + "! Bounds: [0.." + (mColumnStartArr.length - 1) + "]");
      return 0;
    }
    else {
      return mColumnStartArr[col];
    }
  }

  
  
  protected void setColumnStarts(int[] columnStartArr) {
    mColumnStartArr = columnStartArr;
  }

}
