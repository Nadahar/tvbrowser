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

import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

import captureplugin.CapturePlugin;
import captureplugin.drivers.defaultdriver.DeviceConfig;

import util.ui.Localizer;
import devplugin.Channel;
import devplugin.PluginManager;

/**
 * TableModel for ChannelTable
 */
public class ChannelTableModel extends AbstractTableModel {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ChannelTableModel.class);
    
    /** Data */
    private DeviceConfig mData;

    /**
     * creates a new ChannelTableModel
     */
    public ChannelTableModel(DeviceConfig data) {
        mData = data;
        updateChannels();
    }

    /**
     * Updates the List of Channels
     */
    private void updateChannels() {
        PluginManager pl = CapturePlugin.getPluginManager();
        if (pl == null) {
            return;
        }
        Channel[] c = pl.getSubscribedChannels();
        TreeMap channels = mData.getChannels();
        for (int i = 0; i < c.length; i++) {
            if (!channels.containsKey(c[i].getName())) {
                channels.put(c[i].getName(), "");
            }
        }
        mData.setChannels(channels);
    }
    
    /**
     * return the "Internal Name" for col 0, "External Name" for col 1
     */
    public String getColumnName(int column) {
        if (column == 0)
            return mLocalizer.msg("IntName", "Internal Name");
        else
            return mLocalizer.msg("ExtName", "External Name");
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

    /**
     * returns the value at Table-Position (row,col)
     */
    public Object getValueAt(int row, int col) {
        Iterator it = mData.getChannels().keySet().iterator();
        String key = "";

        int i = 0;
        while (it.hasNext()) {
            key = (String) it.next();
            if (i == row) break;
            i++;
        }

        if (col == 0)
            return key;
        else
            return (String) mData.getChannels().get(key);
    }
}