package captureplugin.drivers.dreambox.connector.cs;

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class DreamboxTimerTableModel extends AbstractTableModel {

	private final List<String> columnNames;
	private final List<List<Object>> data;

	DreamboxTimerTableModel(List<List<Object>> data, List<String> columnNames) {
		this.data = data;
		this.columnNames = columnNames;
	}

	@Override
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public int getColumnCount() {
		return columnNames.size();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames.get(col);
	}

	public int getRowCount() {
		return data.size();
	}

	public Object getValueAt(int row, int col) {
		return (data.get(row)).get(col);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if ((col == DreamboxTimerListPanel.COL_ACTION_DISABLE)
				|| (col == DreamboxTimerListPanel.COL_ACTION_EDIT)
				|| (col == DreamboxTimerListPanel.COL_ACTION_DELETE)) {
			return true;
		}
		return false;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		Object oldValue = getValueAt(row, col);
		if (oldValue != value) {
			List<Object> rowVector = data.get(row);
			rowVector.set(col, value);
			fireTableCellUpdated(row, col);
		}
	}

	/**
	 * Remove row from model
	 * 
	 * @param row
	 */
	public void removeRow(int row) {
		data.remove(row);
	}
}
