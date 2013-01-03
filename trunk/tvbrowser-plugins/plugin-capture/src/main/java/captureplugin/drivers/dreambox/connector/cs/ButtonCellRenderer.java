package captureplugin.drivers.dreambox.connector.cs;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class ButtonCellRenderer extends DefaultTableCellRenderer implements
		TableCellRenderer {

	private final JButton renderButton;

	/**
	 * Konstruktor
	 */
	public ButtonCellRenderer() {
		renderButton = new JButton();
		renderButton.setOpaque(true);
	}

	/**
	 * @return renderer
	 */
	public JComponent getCellRenderer() {
		return renderButton;
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
		if (value instanceof JButton) {
			JButton button = (JButton) value;
			renderButton.setText(button.getText());
			renderButton.setToolTipText(button.getToolTipText());
			renderButton.setIcon(button.getIcon());
		} else {
			renderButton.setText((value == null) ? "" : value.toString());
		}
		return renderButton;
	}
}
