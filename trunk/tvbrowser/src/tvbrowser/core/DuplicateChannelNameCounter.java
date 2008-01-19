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

  Hashtable<String, Integer> mChannelNames = new Hashtable<String, Integer>();
  Hashtable<String, Integer> mChannelCountryNames = new Hashtable<String, Integer>();

  /**
   * Construct the Counter
   *
   * @param channels use channels in this List
   */
  public DuplicateChannelNameCounter(Channel[] channels) {
    mChannelNames = new Hashtable<String, Integer>();

    for (Channel ch:channels) {
      // names only
      String key = ch.getName().toLowerCase();
      Integer count = mChannelNames.get(key);

      if (count == null) {
        mChannelNames.put(key, 0);
      } else {
        count++;
        mChannelNames.put(key, count);
      }
      
      // names and country
      key = ch.getName().toLowerCase()+ch.getCountry().toLowerCase();
      count = mChannelCountryNames.get(key);

      if (count == null) {
        mChannelCountryNames.put(key, 0);
      } else {
        count++;
        mChannelCountryNames.put(key, count);
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
    Integer count = mChannelNames.get(channel.getName().toLowerCase());
    return (count != null) && (count != 0);
  }

  /**
   * Check if a channel is a duplicate, including the country name
   * @param channel Channel to check
   * @return true, if name is a duplicate
   */
  public boolean isDuplicateIncludingCountry(Channel channel) {
    if (channel == null) {
      return false;
    }
    Integer count = mChannelCountryNames.get(channel.getName().toLowerCase()+channel.getCountry().toLowerCase());
    return (count != null) && (count != 0);
  }

}
