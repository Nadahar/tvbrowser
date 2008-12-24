package recommendationplugin;

import javax.swing.AbstractCellEditor;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: bodo
 * Date: 24.12.2008
 * Time: 12:02:38
 */
public class TableSliderEditor extends AbstractCellEditor implements TableCellEditor {
  public TableSliderEditor() {
    slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
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

  private JSlider slider;
  private boolean firstTime = true;
}


