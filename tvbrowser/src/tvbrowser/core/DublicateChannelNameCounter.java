package tvbrowser.core;

import devplugin.Channel;

import java.util.Hashtable;
import javax.swing.ListModel;

/**
 * This class returns the number of similar channels in
 * a List of Channels.
 *
 * @since 2.6
 */
public class DublicateChannelNameCounter {

  Hashtable<String, Integer> mChannelnames = new Hashtable<String, Integer>();

  /**
   * Construct the Counter
   *
   * @param model use channels in this List
   */
  public DublicateChannelNameCounter(ListModel model) {
    mChannelnames = new Hashtable<String, Integer>();

    for (int i = 0; i < model.getSize(); i++) {
      Channel ch = (Channel) model.getElementAt(i);

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
   * Check if a channel is a dublicate
   * @param channel Channel to check
   * @return true, if name is a dublicate
   */
  public boolean isDublicate(Channel channel) {
    return mChannelnames.get(channel.getName()) != 0;
  }
}
