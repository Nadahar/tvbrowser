/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin.drivers.defaultdriver.configpanels;

import javax.swing.table.AbstractTableModel;

import util.ui.Localizer;
import captureplugin.drivers.defaultdriver.DeviceConfig;
import captureplugin.drivers.defaultdriver.Variable;

/**
 * TableModel for VariableTable
 */
public class VariableTableModel extends AbstractTableModel {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(VariableTableModel.class);
    
    /** Data */
    private DeviceConfig mData;
    
    /**
     * creates a new VariableTableModel
     * @param data Configuration
     */
    public VariableTableModel(DeviceConfig data) {
        mData = data;
    }

    /**
     * return the Column-Names
     */
    public String getColumnName(int column) {
        if (column == 0) {
          return mLocalizer.msg("No", "No");
        } else if (column == 1) {
          return mLocalizer.msg("Description", "Description");
        } else {
          return mLocalizer.msg("Value", "Value");
        }
    }

    /**
     * returns 3
     */
    public int getColumnCount() {
        return 3;
    }

    /**
     * returns the count of rows in the table
     */
    public int getRowCount() {
        return mData.getVariables().size()+1;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

      Variable var;
      
      if (rowIndex >= mData.getVariables().size()) {
        var = new Variable();
        mData.getVariables().add(var);
        fireTableRowsInserted(mData.getVariables().size(), mData.getVariables().size());
      } else {
        var = (Variable) mData.getVariables().toArray()[rowIndex];
      }
      
      if (columnIndex == 1) {
        var.setDescription(aValue.toString());
      } else if (columnIndex == 2) {
        var.setValue(aValue.toString());
      }
      
      super.setValueAt(aValue, rowIndex, columnIndex);
    }

    /**
     * returns the value at Table-Position (row,col)
     */
    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return Integer.toString(row + 1);
        } else if (row < mData.getVariables().size()) {
            Variable var = (Variable) mData.getVariables().toArray()[row];
            if (col == 1) {
              return var.getDescription();
            } else if (col == 2) {
              return var.getValue();
            }
          
            return "";
        } else {
            return "";
        }
    }

}