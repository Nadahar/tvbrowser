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
  
  /** Holds the heights of the cells. The dimensions are: column and then row. */
  private int[][] mCellHeightArr;
  
  
  
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
  
  
  
  public int getCellHeight(int col, int row) {
    if (mCellHeightArr == null) {
      mLog.warning("Cell height are not yet initialized!");
      return 0;
    }
    else if (mCellHeightArr[col] == null) {
      mLog.warning("Cell height are not yet initialized for column " + col + "!");
      return 0;
    }
    else if ((col < 0) || (col >= mCellHeightArr.length)) {
      mLog.warning("Column out of bounds " + col + "! Bounds: [0.." + (mCellHeightArr.length - 1) + "]");
      return 0;
    }
    else if ((row < 0) || (row >= mCellHeightArr[col].length)) {
     // mLog.warning("Row out of bounds " + row + "! Bounds: [0.." + (mCellHeightArr[col].length - 1) + "]");
      return 0;
    }
    else {
      return mCellHeightArr[col][row];
    }
  }
  
  
  
  protected int[][] createRawCellHeights(ProgramTableModel model) {
    int[][] cellHeightArr = new int[model.getColumnCount()][];
    for (int col = 0; col < model.getColumnCount(); col++) {
      cellHeightArr[col] = new int[model.getRowCount(col)];
    }
    
    return cellHeightArr;
  }
  
  
  
  protected void setCellHeights(int[][] cellHeightArr) {
    mCellHeightArr = cellHeightArr;
  }

  
  
  protected void setColumnStarts(int[] columnStartArr) {
    mColumnStartArr = columnStartArr;
  }

}
