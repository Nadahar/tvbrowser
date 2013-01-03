package captureplugin.drivers.dreambox.connector.cs;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class DateCellRenderer extends DefaultTableCellRenderer implements
		TableCellRenderer {

	private final DateFormat dateFormat;

	DateCellRenderer() {
		this.dateFormat = new SimpleDateFormat();
	}

	DateCellRenderer(String fmt) {
		this.dateFormat = new SimpleDateFormat(fmt);
	}

	@Override
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
		// text
		if (value instanceof Date) {
			Date date = (Date) value;
			setText(dateFormat.format(date));
			setToolTipText(SimpleDateFormat.getDateTimeInstance().format(date));
		} else {
			setText((value == null) ? "" : value.toString());
		}
		setHorizontalAlignment(JLabel.CENTER);
		return this;
	}
}
