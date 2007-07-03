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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.elgatodriver.configdialog;

import captureplugin.CapturePlugin;
import captureplugin.drivers.elgatodriver.ElgatoChannel;
import captureplugin.drivers.elgatodriver.ElgatoConfig;
import devplugin.Channel;
import util.ui.Localizer;

import javax.swing.table.AbstractTableModel;

/**
 * The Table-Model for the mapping of the Channels
 * 
 * @author bodum
 */
public class ConfigTableModel extends AbstractTableModel {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ConfigTableModel.class);

  private Channel[] mSubscribedChannels = CapturePlugin.getPluginManager().getSubscribedChannels();
  
  private ElgatoConfig mConfig;
  
  public ConfigTableModel(ElgatoConfig config) {
    mConfig = config;
  }

  public String getColumnName(int column) {
    if (column == 0) {
      return mLocalizer.msg("channel","Channel");
    } else if (column == 1) {
      return mLocalizer.msg("eyeTV","EyeTV Channel");
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
    } else if (columnIndex == 1){
      return mConfig.getElgatoChannel(mSubscribedChannels[rowIndex]);
    }
    
    return null;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == 1;
  }
  
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex == 1) {
      mConfig.setElgatoChannel(mSubscribedChannels[rowIndex], (ElgatoChannel) aValue);
    }
  }
  
  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == 0) {
      return Channel.class;
    }

    return ElgatoChannel.class;
  }
  
}