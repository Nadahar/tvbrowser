package captureplugin.drivers.dreambox.connector.cs;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class ComboBoxCellRenderer extends JComboBox implements
		TableCellRenderer {

	/**
	 * Konstruktor
	 * 
	 * @param items
	 */
	public ComboBoxCellRenderer(Object[] items) {
		super(items);
	}

	/**
	 * @return renderer
	 */
	public JComponent getCellRenderer() {
		return this;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		// color
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		// Select the current value
		setSelectedItem(value);
		// Component
		return this;
	}
}
