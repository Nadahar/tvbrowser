package captureplugin.drivers.dreambox.connector.cs;

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class DreamboxMovieTableModel extends AbstractTableModel {

	private final List<String> columnNames;
	private final List<List<Object>> data;

	DreamboxMovieTableModel(List<List<Object>> data, List<String> columnNames) {
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
		if ((col == DreamboxMovieListPanel.COL_TAGS)
				|| (col == DreamboxMovieListPanel.COL_TITLE)) {
			return true;
		}
		return false;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		List<Object> rowVector = data.get(row);
		rowVector.set(col, value);
		fireTableCellUpdated(row, col);
	}
}
