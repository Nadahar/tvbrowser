/*
 * SimpleMarkerPlugin by René Mach
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package simplemarkerplugin.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import simplemarkerplugin.MarkList;
import simplemarkerplugin.SimpleMarkerPlugin;
import devplugin.ProgramReceiveTarget;

/**
 * The table model for the mark list settings table.
 * 
 * @author René Mach
 *
 */
public class MarkListTableModel extends DefaultTableModel implements Serializable {
  private static final long serialVersionUID = 1L;
  private ArrayList<MarkList> mLists;

  /**
   * Creates an instance of this class.
   * <p>
   * @param lists The list with the MarkLists.
   */
  public MarkListTableModel(ArrayList<MarkList> lists) {
    mLists = lists;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex != 1;
  }

  @Override
  public int getColumnCount() {
    return 5;
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
      case 2: return SimpleMarkerPlugin.getLocalizer().msg("settings.markPriority", "Highlighting priority");
      case 3: return SimpleMarkerPlugin.getLocalizer().msg("settings.programImportance", "Program importance");
      case 4: return SimpleMarkerPlugin.getLocalizer().msg("settings.sendToPlugin", "Send to plugin");
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
      case 3: mLists.get(row).setProgramImportance((Byte)aValue); break;
      case 4: mLists.get(row).setPluginTargets((Collection<ProgramReceiveTarget>) aValue); break;
    }

    fireTableChanged(new TableModelEvent(this));
  }

  /**
   * Adds a row to thit table.
   * <p>
   * @param item The item to add.
   */
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