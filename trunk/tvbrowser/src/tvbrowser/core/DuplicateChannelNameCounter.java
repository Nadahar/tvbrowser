package tvbrowser.core;

import devplugin.Channel;

import java.util.Hashtable;

/**
 * This class returns the number of similar channels in
 * a List of Channels.
 *
 * @since 2.6
 */
public class DuplicateChannelNameCounter {

  Hashtable<String, Integer> mChannelnames = new Hashtable<String, Integer>();

  /**
   * Construct the Counter
   *
   * @param channels use channels in this List
   */
  public DuplicateChannelNameCounter(Channel[] channels) {
    mChannelnames = new Hashtable<String, Integer>();

    for (Channel ch:channels) {
      Integer count = mChannelnames.get(ch.getName());

      if (count == null) {
        mChannelnames.put(ch.getName(), 0);
      } else {
        count++;
        mChannelnames.put(ch.getName(), count);
      }
    }
  }

  /**
   * Check if a channel is a duplicate
   * @param channel Channel to check
   * @return true, if name is a duplicate
   */
  public boolean isDuplicate(Channel channel) {
    if (channel == null) {
      return false;
    }
    Integer count = mChannelnames.get(channel.getName());
    return (count != null) && (count != 0);
  }
}
