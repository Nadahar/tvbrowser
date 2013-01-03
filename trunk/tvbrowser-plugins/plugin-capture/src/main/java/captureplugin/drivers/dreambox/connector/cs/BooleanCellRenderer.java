package captureplugin.drivers.dreambox.connector.cs;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class BooleanCellRenderer extends JCheckBox implements TableCellRenderer {

	private final String mToolTip;

	/**
	 * Konstruktor
	 * 
	 * @param toolTip
	 */
	public BooleanCellRenderer(String toolTip) {
		mToolTip = toolTip;
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
		if (value instanceof Boolean) {
			Boolean b = (Boolean) value;
			setSelected(b);
			setToolTipText(mToolTip);
		} else {
			setText(value == null ? "" : value.toString());
		}
		// Alignment
		setHorizontalAlignment(SwingConstants.CENTER);
		// Component
		return this;
	}
}
