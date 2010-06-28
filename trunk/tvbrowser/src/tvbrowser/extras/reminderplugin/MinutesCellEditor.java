package tvbrowser.extras.reminderplugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class MinutesCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
  private JComboBox mComboBox;
  
  public MinutesCellEditor() {
    mComboBox = new JComboBox(ReminderDialog.SMALL_REMIND_MSG_ARR);
    mComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
  }

  private void setValue(ReminderListItem item) {
    mComboBox.setSelectedIndex(ReminderFrame.getValueForMinutes(item
        .getMinutes()));
  }
  
  public boolean stopCellEditing() {
    if (mComboBox.isEditable()) {
      // Commit edited value.
      mComboBox.actionPerformed(new ActionEvent(this, 0, ""));
    }
    return super.stopCellEditing();
  }

  public void actionPerformed(ActionEvent e) {
    stopCellEditing();
  }

  // Implement the one CellEditor method that AbstractCellEditor doesn't.
  public Object getCellEditorValue() {
    return ReminderFrame.getMinutesForValue(mComboBox.getSelectedIndex());
  }

  public boolean isCellEditable(EventObject evt) {
    if (evt instanceof MouseEvent) {
      return ((MouseEvent)evt).getClickCount() >= 2;
    }
    return true;
  }
  
  JComboBox getComboBox() {
    return mComboBox;
  }
  
  // Implement the one method defined by TableCellEditor.
  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected, int row, int column) {
    setValue((ReminderListItem) value);
    return mComboBox;
  }
}