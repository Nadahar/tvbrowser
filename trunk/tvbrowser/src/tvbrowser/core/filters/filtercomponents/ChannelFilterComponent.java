/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.core.filters.filtercomponents;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.OrderChooser;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Program;

public class ChannelFilterComponent extends AbstractFilterComponent {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ChannelFilterComponent.class);

  private OrderChooser mList;
  private Channel[] mSelectedChannels;
  private ArrayList<String> mSelectedChannelsIdList;

  public ChannelFilterComponent(String name, String desc) {
    super(name, desc);
    mSelectedChannels = new Channel[0];
    mSelectedChannelsIdList = new ArrayList<String>(0);
  }

  public ChannelFilterComponent() {
    this("", "");
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    ArrayList<Channel> channels = new ArrayList<Channel>();
    int channelCnt = in.readInt();

    for (int i = 0; i < channelCnt; i++) {
      if (version < 3) {
        String dataServiceClassName = (String) in.readObject();
        String groupId = null;

        if (version >= 2) {
          groupId = (String) in.readObject();
        }

        String channelId = (String) in.readObject();
        Channel ch = Channel.getChannel(dataServiceClassName, groupId, null,
            channelId);

        if (ch != null) {
          channels.add(ch);
        }
      } else if(version < 4) {
        Channel ch = Channel.readData(in, true);

        if (ch != null) {
          channels.add(ch);
          mSelectedChannelsIdList.add(ch.getUniqueId());
        }
      } else {
        mSelectedChannelsIdList.add(in.readUTF());
      }
    }
    
    if(version >= 4) {
      channelCnt = in.readInt();
      
      for(int i = 0; i < channelCnt; i++) {
        Channel ch = Channel.readData(in, true);

        if (ch != null) {
          channels.add(ch);
        }        
      }
    }
    
    mSelectedChannels = new Channel[channels.size()];
    channels.toArray(mSelectedChannels);
  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mSelectedChannelsIdList.size());
    
    for(String id : mSelectedChannelsIdList) {
      out.writeUTF(id);
    }
    
    out.writeInt(mSelectedChannels.length);

    for (Channel selectedChannel : mSelectedChannels) {
      selectedChannel.writeData(out);
    }
  }

  @Override
  public String toString() {
    return Localizer.getLocalization(Localizer.I18N_CHANNEL);
  }

  public void saveSettings() {
    Object[] o = mList.getOrder();
    mSelectedChannels = new Channel[o.length];
    mSelectedChannelsIdList.clear();
    
    for (int i = 0; i < o.length; i++) {
      mSelectedChannels[i] = (Channel) o[i];
      mSelectedChannelsIdList.add(mSelectedChannels[i].getUniqueId());
    }
  }
  
  public String getTypeDescription() {
    return mLocalizer.msg("description",
        "This filter accepts programs belonging to the following channels:");
  }

  public JPanel getSettingsPanel() {
    JPanel content = new JPanel(new FormLayout("50dlu:grow","fill:default:grow"));

    Channel[] channels = tvbrowser.core.ChannelList.getSubscribedChannels(false);
    mList = new OrderChooser(mSelectedChannels, channels);

    mList.getUpButton().setVisible(false);
    mList.getDownButton().setVisible(false);

    content.add(mList, CC.xy(1, 1));

    return content;
  }

  public boolean accept(final Program program) {
    for (Channel selectedChannel : mSelectedChannels) {
      if (selectedChannel.equals(program.getChannel())) {
        return true;
      }
    }
    return false;
  }

  public int getVersion() {
    return 4;
  }

  public Channel[] getChannels() {
    return mSelectedChannels;
  }
  
  public void updateAvailableChannels(Channel[] channels) {
    ArrayList<Channel> selectedAvailable = new ArrayList<Channel>();
    
    for(Channel ch : channels) {
      if(mSelectedChannelsIdList.contains(ch.getUniqueId())) {
        selectedAvailable.add(ch);
      }
    }
    
    mSelectedChannels = selectedAvailable.toArray(new Channel[selectedAvailable.size()]);
  }
  
  /**
   * @return <code>true</code> if at least one previously selected channel is
   * not available anymore. 
   */
  public boolean isBroken() {
    return mSelectedChannels.length != mSelectedChannelsIdList.size() || isEmpty();
  }
  
  public boolean isEmpty() {
    return mSelectedChannels.length == 0;
  }
}