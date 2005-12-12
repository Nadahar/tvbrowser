package util.ui;

import javax.swing.JList;

/**
 * A interface for the drop action of JLists.
 * 
 * @author René Mach
 *
 */
public interface ListDropAction {
  /**
   * The method that is called by the drop event.
   * 
   * @param source The source JList.
   * @param target The target JList.
   * @param row The number of the row to move the entries to.
   * @param move Only move one entry from the source to the target list.
   */
  public void drop(JList source, JList target, int row, boolean move);
}
