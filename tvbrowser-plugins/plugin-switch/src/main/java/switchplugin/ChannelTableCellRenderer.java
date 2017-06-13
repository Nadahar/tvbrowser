package switchplugin;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
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
  
  private ChannelLabel mLabel;
  
  public ChannelTableCellRenderer() {
    mLabel = new ChannelLabel();
    mLabel.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value, isSelected,
        hasFocus, row, column);
    
    if(value instanceof Channel) {
      mLabel.setChannel((Channel)value);
      mLabel.setOpaque(isSelected || row%2==1);
      
      if(isSelected) {
        mLabel.setForeground(table.getSelectionForeground());
        mLabel.setBackground(table.getSelectionBackground());
      }
      else {
        mLabel.setForeground(table.getForeground());
        mLabel.setBackground(table.getBackground());
      }
      
      c = mLabel;
    }
    
    return c;
  }

}
