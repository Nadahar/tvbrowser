package captureplugin.drivers.dreambox.connector.cs;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class LabelCellRenderer extends DefaultTableCellRenderer {

	private final int horizontalAlignment;

	LabelCellRenderer() {
		horizontalAlignment = JLabel.LEFT;
	}

	LabelCellRenderer(int alignment) {
		this.horizontalAlignment = alignment;
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
		if (value instanceof JLabel) {
			JLabel label = (JLabel) value;
			setText(label.getText());
			String toolTipText = label.getToolTipText();
			if ((toolTipText == null) || (toolTipText.length() == 0)) {
				toolTipText = label.getText();
			}
			setToolTipText(toolTipText);
			setIcon(label.getIcon());
		} else {
			String text = (value == null) ? "" : value.toString();
			setText(text);
			setToolTipText(text);
			setIcon(null);
		}
		setHorizontalAlignment(horizontalAlignment);
		return this;
	}
}
