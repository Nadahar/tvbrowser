package dataviewerplugin;

import javax.swing.table.DefaultTableModel;

/**
 * DataViewer for TV-Browser.
 * 
 * @author René Mach
 * 
 */
public class DataTableModel extends DefaultTableModel {

  /**
   * @param data
   *          The table data.
   * @param header
   *          The header values of the columns.
   */
  public DataTableModel(Object[][] data, Object[] header) {
    super(data, header);
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public boolean isCellEditable(int x, int y) {
    return false;
  }

}
