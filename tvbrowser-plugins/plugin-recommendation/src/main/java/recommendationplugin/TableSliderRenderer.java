package recommendationplugin;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class TableSliderRenderer extends TableSlider implements TableCellRenderer {

  Border unselectedBorder = null;
  Border selectedBorder = null;

  public Component getTableCellRendererComponent(JTable table, Object val, boolean isSelected, boolean hasFocus, int row, int column) {
    if (table.getRowHeight() < getHeight()) {
      table.setRowHeight(getHeight());
    }
/*
    if (isSelected) {
      if (selectedBorder == null) {
        selectedBorder = BorderFactory.createMatteBorder(1, 3, 1, 3, table.getSelectionBackground());
      }
      setBorder(selectedBorder);
    } else {
      if (unselectedBorder == null) {
        unselectedBorder = BorderFactory.createMatteBorder(1, 3, 1, 3, table.getBackground());
      }
      setBorder(unselectedBorder);
    }
*/

    if (isSelected) {
      setBackground(table.getSelectionBackground());
    } else {
      setBackground(table.getBackground());
    }

    int value = (Integer) val;
    setValue(value);

    return this;
  }
}
