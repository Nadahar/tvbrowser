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
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */
package captureplugin.utils;

import javax.swing.table.AbstractTableModel;

import util.ui.Localizer;
import devplugin.Channel;
import devplugin.Plugin;

/**
 * The Table-Model for the mapping of the Channels
 *
 * @author bodum
 */
public class ConfigTableModel extends AbstractTableModel {
    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ConfigTableModel.class);

    private Channel[] mSubscribedChannels = Plugin.getPluginManager().getSubscribedChannels();

    private ConfigIf mConfig;

    private String mExternalName;

    public ConfigTableModel(ConfigIf config, String externalName) {
        mConfig = config;
        mExternalName = externalName;
    }

    public String getColumnName(int column) {
        if (column == 0) {
            return Localizer.getLocalization(Localizer.I18N_CHANNEL);
        } else if (column == 1) {
            return mExternalName;
        }
        return null;
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return mSubscribedChannels.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return mSubscribedChannels[rowIndex];
        } else if (columnIndex == 1) {
            return mConfig.getExternalChannel(mSubscribedChannels[rowIndex]);
        }

        return null;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            mConfig.setExternalChannel(mSubscribedChannels[rowIndex], (ExternalChannelIf) aValue);
        }
    }

    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Channel.class;
        }

        return ExternalChannelIf.class;
    }

}
