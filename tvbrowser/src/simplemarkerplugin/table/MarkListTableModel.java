package simplemarkerplugin.table;

import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.io.Serializable;
import java.util.ArrayList;

public class MarkListTableModel extends DefaultTableModel implements Serializable {
  private static final long serialVersionUID = 1L;
  private ArrayList<MarkList> mLists;

  public MarkListTableModel(ArrayList<MarkList> lists) {
    mLists = lists;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex != 1;
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public int getRowCount() {
    if (mLists == null) {
      return 0;
    }
    return mLists.size();
  }

  @Override
  public Object getValueAt(int row, int column) {
    if (row < 0 || row > mLists.size()) {
      return null;
    }

    MarkList item = mLists.get(row);
    return item;
  }

  @Override
  public String getColumnName(int column) {
    switch(column){
      case 0: return SimpleMarkerPlugin.getLocalizer().msg("settings.list", "Additional Mark List");
      case 1: return "Icon";
      case 2: return SimpleMarkerPlugin.getLocalizer().msg("settings.markPriority", "Mark priority");
    }

    return null;
  }

  @Override
  public void setValueAt(Object aValue, int row, int column) {
    switch(column) {
      case 0: mLists.get(row).setName((String)aValue);break;
      case 1: mLists.get(row).setMarkIconFileName((String)aValue);break;
    }

    fireTableChanged(new TableModelEvent(this));
  }

  public void addRow(MarkList item) {
    mLists.add(item);
    fireTableRowsInserted(mLists.size()-1, mLists.size()-1);
  }

  @Override
  public void removeRow(int row) {
    mLists.remove(row);
    fireTableRowsDeleted(row, row);
  }
}