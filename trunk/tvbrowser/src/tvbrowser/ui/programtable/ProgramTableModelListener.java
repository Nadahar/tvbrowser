/*
 * ProgramTableModelListener.java
 *
 * Created on 24. Mai 2003, 13:58
 */

package tvbrowser.ui.programtable;

/**
 *
 * @author  Til
 */
public interface ProgramTableModelListener {
  
  public void tableDataChanged(Runnable callback);
  
  public void tableCellUpdated(int col, int row);
}
