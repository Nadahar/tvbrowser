/**
 * Created on 23.06.2010
 */
package captureplugin.drivers.topfield;

import javax.swing.table.AbstractTableModel;

import util.ui.Localizer;
import captureplugin.utils.ExternalChannelIf;
import devplugin.Channel;
import devplugin.Plugin;

/**
 * 
 * @author Wolfgang Reh
 */
public class TopfieldChannelTableModel extends AbstractTableModel {
  private static final String DEVICE_CHANNEL_LABEL = "topfieldChannel";
  private static final String DEFAULT_DEVICE_CHANNEL_LABEL = "Topfield Channel";
  private static final String CHANNEL_PREROLL = "channelPreroll";
  private static final String DEFAULT_CHANNEL_PREROLL = "Preroll";
  private static final String CHANNEL_POSTROLL = "channelPostroll";
  private static final String DEFAULT_CHANNEL_POSTROLL = "Posroll";

  private static final Localizer localizer = Localizer.getLocalizerFor(TopfieldChannelTableModel.class);

  private final Channel[] subscribedChannels = Plugin.getPluginManager().getSubscribedChannels();
  private final TopfieldConfiguration configuration;

  /**
   * Create a table model with the current configuration.
   * 
   * @param config The configuration
   */
  public TopfieldChannelTableModel(TopfieldConfiguration config) {
    configuration = config;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    return 4;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int column) {
    switch (column) {
    case 0:
      return Localizer.getLocalization(Localizer.I18N_CHANNEL);
    case 1:
      return localizer.msg(DEVICE_CHANNEL_LABEL, DEFAULT_DEVICE_CHANNEL_LABEL);
    case 2:
      return localizer.msg(CHANNEL_PREROLL, DEFAULT_CHANNEL_PREROLL);
    case 3:
      return localizer.msg(CHANNEL_POSTROLL, DEFAULT_CHANNEL_POSTROLL);
    default:
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getRowCount()
   */
  @Override
  public int getRowCount() {
    return subscribedChannels.length;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
    case 0:
      return subscribedChannels[rowIndex];
    case 1:
      return configuration.getExternalChannel(subscribedChannels[rowIndex]);
    case 2:
      return configuration.getChannelPreroll(subscribedChannels[rowIndex]);
    case 3:
      return configuration.getChannelPostroll(subscribedChannels[rowIndex]);
    default:
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return !(columnIndex == 0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int,
   * int)
   */
  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    TopfieldServiceInfo serviceInfo;

    switch (columnIndex) {
    case 1:
      configuration.setExternalChannel(subscribedChannels[rowIndex], (ExternalChannelIf) aValue);
      break;
    case 2:
      serviceInfo = (TopfieldServiceInfo) configuration.getExternalChannel(subscribedChannels[rowIndex]);
      if (serviceInfo != null) {
        serviceInfo.setPreroll((Integer) aValue);
      }
      break;
    case 3:
      serviceInfo = (TopfieldServiceInfo) configuration.getExternalChannel(subscribedChannels[rowIndex]);
      if (serviceInfo != null) {
        serviceInfo.setPostroll((Integer) aValue);
      }
      break;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class<?> getColumnClass(int columnIndex) {
    switch (columnIndex) {
    case 0:
      return Channel.class;
    case 1:
      return ExternalChannelIf.class;
    case 2:
    case 3:
      return Integer.class;
    default:
      return null;
    }
  }
}
