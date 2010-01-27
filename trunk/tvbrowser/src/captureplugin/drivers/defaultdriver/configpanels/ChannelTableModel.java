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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

import util.ui.Localizer;
import captureplugin.drivers.defaultdriver.DeviceConfig;
import devplugin.Channel;
import devplugin.Plugin;
import devplugin.PluginManager;

/**
 * TableModel for ChannelTable
 */
public class ChannelTableModel extends AbstractTableModel {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ChannelTableModel.class);
    
    /** Data */
    private DeviceConfig mData;

    /** The Rows in the TableModel */
    private ArrayList<Channel> mChannelRows;
    
    /**
     * creates a new ChannelTableModel
     * @param data Configuration
     */
    public ChannelTableModel(DeviceConfig data) {
        mData = data;
        updateChannels();
    }

    /**
     * Updates the List of Channels
     */
    private void updateChannels() {
        PluginManager pl = Plugin.getPluginManager();
        if (pl == null) {
            return;
        }
        TreeMap<Channel, String> channels = mData.getChannels();

        final Channel[] channelArray = pl.getSubscribedChannels();
        for (Channel channel : channelArray) {
            if (!channels.containsKey(channel)) {
                channels.put(channel, "");
            }
        }
        mData.setChannels(channels);
        
        mChannelRows = new ArrayList<Channel>();
        
        Iterator<Channel> it = mData.getChannels().keySet().iterator();
        Channel key;

        while (it.hasNext()) {
            key = it.next();
            mChannelRows.add(key);
        }
        
        Collections.sort(mChannelRows, new Comparator<Channel>() {
          public int compare(Channel a, Channel b) {
            int aPos = 0;
            int bPos = 0;
            
            int counter = 0;
            
            while(counter < channelArray.length && !channelArray[counter].equals(a)) {
              aPos = counter++;
            }
            
            counter = 0;
            
            while(counter < channelArray.length && !channelArray[counter].equals(b)) {
              bPos = counter++;
            }
            
            return aPos - bPos;
          }
        });
    }
    
    /**
     * return the "Internal Name" for col 0, "External Name" for col 1
     */
    public String getColumnName(int column) {
        if (column == 0) {
          return mLocalizer.msg("IntName", "Internal Name");
        } else {
          return mLocalizer.msg("ExtName", "External Name");
        }
    }

    /**
     * returns 2
     */
    public int getColumnCount() {
        return 2;
    }

    /**
     * returns the count of rows in the table
     */
    public int getRowCount() {
        return mData.getChannels().size();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      
      Channel key = getKeyForRow(rowIndex);
      
      if (key != null) {
        mData.getChannels().put(key, (String)aValue);
      }
      
      super.setValueAt(aValue, rowIndex, columnIndex);
    }

    /**
     * returns the value at Table-Position (row,col)
     */
    public Object getValueAt(int row, int col) {
        Channel key = getKeyForRow(row);

        if (col == 0) {
          return key;
        } else {
          return mData.getChannels().get(key);
        }
    }
    
    /**
     * Get the Channel for a specific Row
     * @param row Row to get Channel for
     * @return Channel for Row
     */
    private Channel getKeyForRow(int row){
      return mChannelRows.get(row);
    }
}