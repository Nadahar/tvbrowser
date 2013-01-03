package captureplugin.drivers.dreambox.connector.cs;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class LabelCellEditor extends DefaultCellEditor {
	private final JTextField textField;

	/**
	 * @param tf
	 */
	public LabelCellEditor(final JTextField tf) {
		super(tf);
		textField = tf;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (value instanceof JLabel) {
			JLabel label = (JLabel) value;
			textField.setText(label.getText());
			textField.setToolTipText(label.getToolTipText());
			textField.setHorizontalAlignment(JLabel.LEFT);
		} else {
			textField.setText((value == null) ? "" : value.toString());
		}
		return textField;
	}

	@Override
	public Object getCellEditorValue() {
		return textField.getText();
	}
}
