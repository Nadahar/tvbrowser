package captureplugin.drivers.dreambox.connector.cs;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTable.PrintMode;

import util.ui.Localizer;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public abstract class JPanelRefreshAbstract extends JPanel implements
		ActionListener {

	/**
	 * PopupListener
	 */
	class PopupListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			showPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}

		private void showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				mPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				// aktuelle Zeile selektieren
				mTable.changeSelection(mTable.rowAtPoint(e.getPoint()), mTable
						.columnAtPoint(e.getPoint()), false, false);
			}
		}
	}

	// Konstanten
	private static final String QUOTE = "\"";
	private static final String TAB = "\t";
	// Translator
	private static final Localizer mLocalizer = Localizer
			.getLocalizerFor(JPanelRefreshAbstract.class);
	// Logger
	private static final Logger mLog = Logger
			.getLogger(JPanelRefreshAbstract.class.getName());
	// Aktionen
	private static final String CMD_COPY = "copy";
	private static final String CMD_PRINT = "print";
	// Member
	final JPopupMenu mPopupMenu;
	final JTable mTable;

	/**
	 * Konstruktor
	 */
	JPanelRefreshAbstract() {
		mTable = new JTable();
		mPopupMenu = new JPopupMenu();

		// Popup aufbauen
		JMenuItem menuItemPrint = new JMenuItem(mLocalizer.msg("print",
				"Print..."));
		menuItemPrint.setActionCommand(CMD_PRINT);
		menuItemPrint.addActionListener(this);
		menuItemPrint.setIcon(new ImageIcon(getClass().getResource(
				"images/print.gif")));
		mPopupMenu.add(menuItemPrint);

		JMenuItem menuItemCopy = new JMenuItem(mLocalizer.msg("copy", "Copy"));
		menuItemCopy.setActionCommand(CMD_COPY);
		menuItemCopy.addActionListener(this);
		menuItemCopy.setIcon(new ImageIcon(getClass().getResource(
				"images/copy.gif")));
		mPopupMenu.add(menuItemCopy);

		// add the listener to the table
		MouseListener popupListener = new PopupListener();
		mTable.addMouseListener(popupListener);
	}

	/**
	 * Action
	 */
	public void actionPerformed(ActionEvent ae) {
		String actionCommand = ae.getActionCommand();

		if (actionCommand.equals(CMD_COPY)) {
			cmdCopyToClipboard(ae);
		} else if (actionCommand.equals(CMD_PRINT)) {
			cmdPrint(ae);
		} else {
			actionPerformedDelegate(ae);
		}
	}

	/**
	 * actionPerformed weiterleiten
	 * 
	 * @param ae
	 */
	public void actionPerformedDelegate(ActionEvent ae) {
		mLog.info(ae.getActionCommand());
	}

	/**
	 * copy to clipboard
	 * 
	 * @param ae
	 */
	private void cmdCopyToClipboard(ActionEvent ae) {
		String s = getModelRowValues(-1) + "\n";
		for (int tableRow = 0; tableRow < mTable.getRowCount(); tableRow++) {
			int modelRow = mTable.convertRowIndexToModel(tableRow);
			s += getModelRowValues(modelRow) + "\n";
		}
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				new StringSelection(s), null);
	}

	/**
	 * print table
	 * 
	 * @param ae
	 */
	private void cmdPrint(ActionEvent ae) {
		try {
			PrintMode printMode = PrintMode.FIT_WIDTH;
			MessageFormat footerFormat = new MessageFormat("Seite: {0}");
			boolean interactive = false;
			boolean showPrintDialog = true;
			PrintRequestAttributeSet attrSet = new HashPrintRequestAttributeSet();
			attrSet.add(new JobName("CapturePlugin", null));
			attrSet.add(OrientationRequested.PORTRAIT);
			attrSet.add(MediaSizeName.ISO_A4);
			MediaSize mediaSize = MediaSize
					.getMediaSizeForName(MediaSizeName.ISO_A4);
			float[] size = mediaSize.getSize(MediaSize.MM);
			float leftMargin = 10.0f;
			float topMargin = 5.0f;
			attrSet.add(new MediaPrintableArea(leftMargin, topMargin, size[0]
					- (leftMargin * 1.5f), size[1] - (topMargin * 2),
					MediaPrintableArea.MM));
			PrintService pservice = PrintServiceLookup
					.lookupDefaultPrintService();
			mTable.print(printMode, null, footerFormat, showPrintDialog,
					attrSet, interactive, pservice);
		} catch (PrinterException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get tab seperated row as text
	 * 
	 * @param modelRow
	 * @return s
	 */
	String getModelRowValues(int modelRow) {
		String rv = "";
		for (int tableCol = 0; tableCol < mTable.getColumnModel()
				.getColumnCount(); tableCol++) {
			Object value;
			int modelCol = mTable.convertColumnIndexToModel(tableCol);
			if (modelRow == -1) {
				value = mTable.getModel().getColumnName(modelCol);
			} else {
				value = mTable.getModel().getValueAt(modelRow, modelCol);
			}
			String s;
			if (value instanceof JLabel) {
				JLabel label = (JLabel) value;
				s = label.getText();
			} else if (value instanceof JButton) {
				JButton button = (JButton) value;
				String text = button.getText();
				s = (text.length() == 0 ? button.getActionCommand() : text);
			} else if (value instanceof Date) {
				Date date = (Date) value;
				s = SimpleDateFormat.getDateTimeInstance().format(date);
			} else if (value != null) {
				s = value.toString();
			} else {
				s = "";
			}
			s = s.replaceAll(QUOTE, QUOTE + QUOTE);
			rv += QUOTE + s + QUOTE + TAB;
		}
		if (rv.length() >= TAB.length()) {
			rv = rv.substring(0, rv.length() - 1);
		}
		return rv;
	}

	/**
	 * panel reload
	 */
	public abstract void refresh();

}
