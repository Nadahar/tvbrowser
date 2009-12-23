package simplemarkerplugin.table;

import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;
import util.ui.MarkPriorityComboBoxRenderer;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class MarkListPriorityCellEditor extends AbstractCellEditor implements
    TableCellEditor {

  private static final long serialVersionUID = 1L;

  private final String[] prioValues = {
      SimpleMarkerPlugin.getLocalizer().msg("settings.noPriority","None"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.min","Minimum"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.lowerMedium","Lower Medium"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.medium","Medium"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.higherMedium","Higher Medium"),
      SimpleMarkerPlugin.getLocalizer().msg("settings.max","Maximum")};

  private JComboBox mComboBox;
  private MarkList mItem;

  public MarkListPriorityCellEditor() {
    mComboBox = new JComboBox(prioValues);
    mComboBox.setRenderer(new MarkPriorityComboBoxRenderer());
  }

  public boolean isCellEditable(EventObject evt) {
    return !(evt instanceof MouseEvent) || ((MouseEvent) evt).getClickCount() >= 2;
  }

  public Object getCellEditorValue() {
    mItem.setMarkPriority(mComboBox.getSelectedIndex()-1);
    return mComboBox.getSelectedIndex() - 1;
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
    boolean isSelected, int row, int column) {
    mItem = (MarkList)table.getValueAt(table.getSelectedRow(),0);
    mComboBox.setSelectedIndex(mItem.getMarkPriority());
    return mComboBox;
  }
}
