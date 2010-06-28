package dataviewerplugin;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * DataViewer for TV-Browser.
 * 
 * @author René Mach
 * 
 */
public class DataTableCellRenderer extends DefaultTableCellRenderer {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected static final Color COMPLETE = new Color(128, 255, 128);

  protected static final Color UNCOMPLETE = new Color(255, 210, 0);

  protected static final Color NODATA = new Color(255, 128, 128);

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    
    String bool = (String) value;

    if (bool.startsWith("true")) {
      if(bool.endsWith("pict")) {
        label.setForeground(COMPLETE.darker());
        label.setText("pict");
      } else {
        label.setForeground(COMPLETE);
      }
      label.setBackground(COMPLETE);
    } else if (bool.compareTo("false") == 0) {
      label.setForeground(NODATA);
      label.setBackground(NODATA);
    } else {
      if(bool.endsWith("pict")) {
        label.setForeground(UNCOMPLETE.darker());
        label.setText("pict");
      } else {
        label.setForeground(UNCOMPLETE);
      }
      
      label.setBackground(UNCOMPLETE);
    }

    return label;
  }
}
