/**
 * Created on 12.02.2011
 */
package captureplugin.drivers.topfield;

import util.ui.Localizer;

/**
 * Key which is used to sort the device channels when retrieving the external
 * channel list.
 * 
 * @author Wolfgang
 */
public enum TopfieldChannelSortKey {
  CHANNEL_NAME {
    @Override
    public String toString() {
      return localizer.msg(CHANNEL_NAME_TEXT, DEFAULT_CHANNEL_NAME_TEXT);
    }
  },
  CHANNEL_NUMBER {
    @Override
    public String toString() {
      return localizer.msg(CHANNEL_NUMBER_TEXT, DEFAULT_CHANNEL_NUMBER_TEXT);
    }
  };

  private static final String CHANNEL_NAME_TEXT = "channelNameText";
  private static final String DEFAULT_CHANNEL_NAME_TEXT = "Channel name";
  private static final String CHANNEL_NUMBER_TEXT = "channelNumberText";
  private static final String DEFAULT_CHANNEL_NUMBER_TEXT = "Channel number";
  private static final Localizer localizer = Localizer.getLocalizerFor(TopfieldChannelSortKey.class);
}
