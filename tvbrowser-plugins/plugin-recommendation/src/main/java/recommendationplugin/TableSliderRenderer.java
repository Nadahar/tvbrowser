package recommendationplugin;

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

public class TableSliderRenderer extends JSlider implements TableCellRenderer {

  Border unselectedBorder = null;
  Border selectedBorder = null;

  public TableSliderRenderer() {
    setMinimum(0);
    setMaximum(100);
    setBounds(0, 0, 100, 10);
    setOpaque(true);
  }

  public Component getTableCellRendererComponent(JTable table, Object val, boolean isSelected, boolean hasFocus, int row, int column) {
    if (table.getRowHeight() < getHeight() + 14) {
      table.setRowHeight(getHeight() + 14);
    }

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
