package switchplugin;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import util.ui.ChannelLabel;

import devplugin.Channel;

/**
 * The TableCellRenderer for the ChannelTable.
 * 
 * @author René Mach
 *
 */
public class ChannelTableCellRenderer extends DefaultTableCellRenderer {


  private static final long serialVersionUID = 1L;

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value, isSelected,
        hasFocus, row, column);
    
    if(value instanceof Channel) {
      ChannelLabel label = new ChannelLabel((Channel)value);
      label.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
      
      label.setOpaque(isSelected);
      
      if(isSelected) {
        label.setForeground(table.getSelectionForeground());
        label.setBackground(table.getSelectionBackground());
      }
      
      c = label;
    }
    
    return c;
  }

}
