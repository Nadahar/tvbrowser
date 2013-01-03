package captureplugin.drivers.dreambox.connector.cs;

import java.awt.Component;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class DateCellEditor extends DefaultCellEditor {

	// Logger
	private static final Logger mLog = Logger.getLogger(DateCellEditor.class
			.getName());

	private static final DateFormat dateformat = new SimpleDateFormat();
	private final JTextField editor;
	private Object editorValue;

	/**
	 * Konstruktor
	 * 
	 * @param tf
	 */
	public DateCellEditor() {
		super(new JTextField());
		this.editor = (JTextField) getComponent();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		if (value instanceof Date) {
			Date date = (Date) value;
			editorValue = date;
			editor.setText(dateformat.format(date));
		} else {
			String text = (value == null) ? "" : value.toString();
			editor.setText(text);
			editorValue = text;
		}
		return editor;
	}

	@Override
	public Object getCellEditorValue() {
		try {
			editorValue = dateformat.parse(editor.getText());
		} catch (ParseException e) {
			mLog.warning(e.getLocalizedMessage() + " - Position: "
					+ e.getErrorOffset());
		}
		return editorValue;
	}
}
