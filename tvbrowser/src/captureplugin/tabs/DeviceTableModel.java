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
package captureplugin.tabs;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import util.ui.Localizer;
import captureplugin.drivers.DeviceIf;
import devplugin.Program;


/**
 * The TableModel for the Device-Program Table
 * 
 * @author bodum
 */
public class DeviceTableModel extends AbstractTableModel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DeviceTableModel.class);
   
    
    /** List of Devices */
    private ArrayList<DeviceIf> mDevices = new ArrayList<DeviceIf>();
    /** List of Programs */
    private ArrayList<Program> mPrograms = new ArrayList<Program>();
    
    /**
     * Creates the Model
     */
    public DeviceTableModel() {
        mDevices = new ArrayList<DeviceIf>();
        mPrograms = new ArrayList<Program>();
    }

    /**
     * Add a Device-Program to the Model
     * @param dev Device
     * @param prog Program in Device
     */
    public void addProgram(DeviceIf dev,  Program prog) {
        mDevices.add(dev);
        mPrograms.add(prog);
        fireTableRowsInserted(mDevices.size(), mDevices.size());
    }
    
    /**
     * Remove a row
     * @param row row to remove
     */
    public void removeRow(int row) {
        if (row > mDevices.size()) {
            return;
        }
        
        mDevices.remove(row);
        mPrograms.remove(row);
        
        fireTableRowsDeleted(row, row);
    }
    

    /**
     * Clears the Table
     */
    public void clearTable() {
        mDevices.clear();
        mPrograms.clear();
    }
    
    public int getColumnCount() {
        return 2;
    }

    /**
     * Returns the Names for the Columns
     */
    public String getColumnName(int col) {
        if (col == 0) {
            return mLocalizer.msg("Device", "Device");
        } else {
            return Localizer.getLocalization(Localizer.I18N_PROGRAM);
        }
    }
    
    public int getRowCount() {
        return mDevices.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        
        if (rowIndex > mDevices.size()) {
            return null;
        }
        
        if (columnIndex == 0) {
            return mDevices.get(rowIndex);
        }
        
        return mPrograms.get(rowIndex);
    }
}
