package simplemarkerplugin.table;

import devplugin.ProgramReceiveTarget;
import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

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
    return 4;
  }

  @Override
  public int getRowCount() {
    if (mLists == null) {
      return 0;
    }
    return mLists.size();
  }

  @Override
  public String getColumnName(int column) {
    switch(column){
      case 0: return SimpleMarkerPlugin.getLocalizer().msg("settings.list", "Additional Mark List");
      case 1: return SimpleMarkerPlugin.getLocalizer().msg("settings.icon", "icon");
      case 2: return SimpleMarkerPlugin.getLocalizer().msg("settings.markPriority", "Mark priority");
      case 3: return SimpleMarkerPlugin.getLocalizer().msg("settings.sendToPlugin", "Send to plugin");
    }

    return null;
  }

  @Override
  public Object getValueAt(int row, int column) {
    if (row < 0 || row > mLists.size()) {
      return null;
    }

    return mLists.get(row);
  }

  @Override
  public void setValueAt(Object aValue, int row, int column) {
    switch(column) {
      case 0: mLists.get(row).setName((String)aValue);break;
      case 1: mLists.get(row).setMarkIconFileName((String)aValue);break;
      case 2: mLists.get(row).setMarkPriority((Integer)aValue); break;
      case 3: mLists.get(row).setPluginTargets((Collection<ProgramReceiveTarget>) aValue); break;
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