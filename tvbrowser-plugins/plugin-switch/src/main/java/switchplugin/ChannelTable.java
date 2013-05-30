package switchplugin;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import switchplugin.ChannelTableCellRenderer;

/**
 * The Table for the Channel setup.
 * 
 * @author René Mach
 *
 */
public class ChannelTable extends JTable{
    private static final long serialVersionUID = 1L;

    /**
     * @param values The values of the table.
     * @param head The column headers of the table.
     */
    public ChannelTable(Object[][] values, Object[] head) {
      super(values,head);
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {
      return new ChannelTableCellRenderer();
    }
    
    public boolean isCellEditable(int row, int column) {
      return column != 0;
    }
    
    public int getRowHeight() {
      return 27;
    }
}
