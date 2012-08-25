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
public class ChannelTableCellRenderer extends DefaultTableCellRenderer {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);

   
      //label.setForeground(Color.black);
      //label.setBackground(new Color(255, 221, 221));
      label.setHorizontalAlignment(SwingConstants.RIGHT);
   return label;
  }
}
