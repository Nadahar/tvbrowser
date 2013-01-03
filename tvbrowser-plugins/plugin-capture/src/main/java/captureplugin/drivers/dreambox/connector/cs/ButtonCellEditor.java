package captureplugin.drivers.dreambox.connector.cs;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class ButtonCellEditor extends DefaultCellEditor {

	private final JButton editButton;

	private Object value;

	/**
	 * Konstruktor
	 * 
	 * @param listener
	 */
	public ButtonCellEditor(ActionListener listener) {
		super(new JCheckBox());
		editButton = new JButton();
		editButton.setOpaque(true);
		editButton.addActionListener(listener);
		editButton.setFocusable(false);
	}

	/**
	 * @return editor
	 */
	public JComponent getCellEditor() {
		return editButton;
	}

	@Override
	public Object getCellEditorValue() {
		return value;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		// value merken
		this.value = value;
		if (value instanceof JButton) {
			JButton button = (JButton) value;
			editButton.setText(button.getText());
			editButton.setActionCommand(button.getActionCommand());
			editButton.setToolTipText(button.getToolTipText());
			editButton.setIcon(button.getIcon());
		} else {
			editButton.setText((value == null) ? "" : value.toString());
		}
		// Component
		return editButton;
	}

}
