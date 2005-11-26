package onlinereminder;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MinutesCellRenderer extends DefaultTableCellRenderer {
  JLabel label;
  
  public MinutesCellRenderer() {
    label = new JLabel();
  }
  
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component def = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
    if (value instanceof Integer) {
      Integer minutes = (Integer) value;
      
      label.setText(ReminderValues.getStringForMinutes(minutes.intValue()));
      
      label.setOpaque(def.isOpaque());
      label.setForeground(def.getForeground());
      label.setBackground(def.getBackground());
      
      return label;
    }
    
    return def;
  }
  
}
