/**
 * Created on 23.06.2010
 */
package captureplugin.drivers.topfield;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;

/**
 * Cell editor for pre and post roll time.
 * 
 * @author Wolfgang Reh
 */
public class TopfieldRollTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
  private JSpinner timeSpinner = null;

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing
   * .JTable, java.lang.Object, boolean, int, int)
   */
  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    timeSpinner = new JSpinner();
    SpinnerNumberModel spinnerModel = new SpinnerNumberModel();
    spinnerModel.setMinimum(0);
    spinnerModel.setMaximum(90);
    timeSpinner.setModel(spinnerModel);
    timeSpinner.setValue(value);

    return timeSpinner;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.CellEditor#getCellEditorValue()
   */
  @Override
  public Object getCellEditorValue() {
    return timeSpinner.getValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    stopCellEditing();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.AbstractCellEditor#stopCellEditing()
   */
  @Override
  public boolean stopCellEditing() {
    try {
      timeSpinner.commitEdit();
    } catch (ParseException e) {
      return false;
    }
    return super.stopCellEditing();
  }
}
