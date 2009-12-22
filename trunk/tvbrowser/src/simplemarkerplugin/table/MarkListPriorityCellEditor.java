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
      SimpleMarkerPlugin.mLocalizer.msg("settings.noPriority","None"),
      SimpleMarkerPlugin.mLocalizer.msg("settings.min","Minimum"),
      SimpleMarkerPlugin.mLocalizer.msg("settings.lowerMedium","Lower Medium"),
      SimpleMarkerPlugin.mLocalizer.msg("settings.medium","Medium"),
      SimpleMarkerPlugin.mLocalizer.msg("settings.higherMedium","Higher Medium"),
      SimpleMarkerPlugin.mLocalizer.msg("settings.max","Maximum")};

  private JComboBox mComboBox;
  private MarkList mItem;

  public MarkListPriorityCellEditor() {
    mComboBox = new JComboBox(prioValues);
    mComboBox.setRenderer(new MarkPriorityComboBoxRenderer());
  }

  public boolean isCellEditable(EventObject evt) {
    if (evt instanceof MouseEvent) {
      return ((MouseEvent) evt).getClickCount() >= 2;
    }
    return true;
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
