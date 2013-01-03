package captureplugin.drivers.dreambox.connector.cs;

import java.awt.Component;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class ComboBoxCellEditor extends DefaultCellEditor {

	// Logger
	private static final Logger mLog = Logger
			.getLogger(ComboBoxCellEditor.class.getName());
	private final JComboBox comboBoxEditor;
	private final DefaultComboBoxModel modelRenderer;

	/**
	 * Konstruktor
	 * 
	 * @param comboBox
	 */
	public ComboBoxCellEditor(final JComboBox comboBox) {
		this(comboBox, null);
	}

	/**
	 * Konstruktor
	 * 
	 * @param comboBox
	 * @param model
	 */
	public ComboBoxCellEditor(final JComboBox comboBox,
			final ComboBoxModel model) {
		super(comboBox);
    mLog.setLevel(Level.INFO);    
		modelRenderer = (DefaultComboBoxModel) model;
		comboBoxEditor = comboBox;
		comboBoxEditor.setEditable(true);
	}

	@Override
	public Object getCellEditorValue() {
		return comboBoxEditor.getSelectedItem();
	}

	/**
	 * @return editor
	 */
	public JComponent getCellEditor() {
		return comboBoxEditor;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		comboBoxEditor.setSelectedItem(value);
		return comboBoxEditor;
	}

	@Override
	public boolean stopCellEditing() {
		String selectedItem = (String) comboBoxEditor.getSelectedItem();
		boolean found = false;
		Set<String> items = new TreeSet<String>();
		for (int i = 0; i < comboBoxEditor.getItemCount(); i++) {
			items.add((String) comboBoxEditor.getItemAt(i));
			if (selectedItem.equals(comboBoxEditor.getItemAt(i))) {
				found = true;
			}
		}
		if (!found) {
			items.add(selectedItem);
			comboBoxEditor.removeAllItems();
			for (String item : items) {
				comboBoxEditor.addItem(item);
			}
			comboBoxEditor.setSelectedItem(selectedItem);
			// Renderer Model anpasssen
			if (modelRenderer != null) {
				modelRenderer.addElement(selectedItem);
			}
			mLog.info("LISTE erweitert: " + selectedItem);
		}
		fireEditingStopped();
		return true;
	}
}
