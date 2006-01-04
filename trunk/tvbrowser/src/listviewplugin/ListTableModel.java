/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package listviewplugin;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import devplugin.Channel;
import devplugin.Program;


/**
 * This Class contains the TableModel for the Table
 */
public class ListTableModel extends AbstractTableModel {

    /** Translator */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ListTableModel.class);

    /** Data in this Model */
    private Vector mData = new Vector();
    
    /**
     * Returns the Column-Name
     */
    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return mLocalizer.msg("Channel", "Channel");
        case 1:
            return mLocalizer.msg("Program", "Program");
        case 2:
            return mLocalizer.msg("NextProgram","Next Program");
        default:
            break;
        } 
        return super.getColumnName(column);
    }
    
    /**
     * Adds one Row to the Table
     * @param channel Channel
     * @param first First Program
     * @param second Second Program
     */
    public void addRow(Channel channel, Program first, Program second) {
        ListTableModelData data= new ListTableModelData();
        data.mChannel = channel;
        data.mFirst = first;
        data.mSecond = second;
        
        mData.add(data);
    }
    
    /**
     * Updates a Row in the Table
     * @param channel Channel in Row
     * @param first First Program
     * @param second Second Program
     */
    public void updateRow(Channel channel, Program first, Program second) {
      ListTableModelData data= new ListTableModelData();
      data.mChannel = channel;
      data.mFirst = first;
      data.mSecond = second;
      
      int pos = findRowWithChannel(channel);
      if (pos > -1) {
        ListTableModelData olddata = (ListTableModelData)mData.get(pos); 
        if (!olddata.equals(data)) {
          mData.remove(pos);
          mData.add(pos, data);
          fireTableRowsUpdated(pos, pos);
        }
      } else {
        mData.add(data);
        fireTableRowsInserted(mData.indexOf(data), mData.indexOf(data));
      }
    }
    
    /**
     * Trys to find a Row with a specific Channel and returns it's Index
     * @param channel Find Row with this Channel
     * @return Row with Channel, -1 if not found
     */
    private int findRowWithChannel(Channel channel) {
      int size = mData.size();
      for (int i = 0;i < size;i++) {
        ListTableModelData data = (ListTableModelData)mData.get(i);
        if (data.mChannel.equals(channel)) {
          return i;
        }
      }
      return -1;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 3;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return mData.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        if (rowIndex > mData.size()) {
            return null;
        }
        
        ListTableModelData data = (ListTableModelData) mData.get(rowIndex);

        switch (columnIndex) {
        case 0:
            return data.mChannel;
        case 1:
            return data.mFirst;
        case 2:
            return data.mSecond;

        default:
            break;
        }
        
        
        return null;
    }

    /**
     * Gets the first Program in the Row
     * @param row Row
     * @return First Program in Row
     */
    public Program getProgram(int row) {
        ListTableModelData data = getListTableModelData(row);
        
        if (data != null) {
            return data.mFirst;
        }
        return null;
    }

    /**
     * Gets the second Program in the Row
     * @param row Row
     * @return Second Program in the Row
     */
    public Program getNextProgram(int row) {
        ListTableModelData data = getListTableModelData(row);
        
        if (data != null) {
            return data.mSecond;
        }
        return null;
    }
    
    /**
     * Returns the ListTableModelData for a specific Row
     * @param row Get Data from this Row
     * @return ListTableModelData in row
     */
    private ListTableModelData getListTableModelData(int row) {
        if ((row < 0) || (row >= mData.size())) {
            return null;
        }
        
        return (ListTableModelData)mData.get(row);
    }
    
    /**
     * The Data in a rows
     */
    private class ListTableModelData {
        Channel mChannel;
        Program mFirst, mSecond;
        
        public boolean equals(Object obj) {
          if (obj instanceof ListTableModelData) {
            ListTableModelData data = (ListTableModelData) obj;
            
            if (mFirst == null && data.mFirst != null) {
              return false;
            }
            
            if (mSecond == null && data.mSecond != null) {
              return false;
            }
            
            return mFirst.equals(data.mFirst) && mSecond.equals(data.mSecond);
          }
          
          return super.equals(obj);
        }
    }

}