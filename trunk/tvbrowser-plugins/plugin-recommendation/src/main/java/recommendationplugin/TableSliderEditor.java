package recommendationplugin;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * Created by IntelliJ IDEA.
 * User: bodo
 * Date: 24.12.2008
 * Time: 12:02:38
 */
public class TableSliderEditor extends AbstractCellEditor implements TableCellEditor {
  private TableSlider slider;

  public TableSliderEditor() {
    slider = new TableSlider();
    slider.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent event) {
        stopCellEditing();
      }
    });
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
    slider.setBounds(table.getCellRect(row, column, false));
    slider.updateUI();
    slider.setValue((Integer) value);
    return slider;
  }

  public Object getCellEditorValue() {
    return slider.getValue();
  }
}


