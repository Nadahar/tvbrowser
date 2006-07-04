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

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.filters.FilterComponent;
import util.ui.OrderChooser;
import devplugin.Channel;
import devplugin.Program;

public class ChannelFilterComponent implements FilterComponent {

  private static final util.ui.Localizer mLocalizer
          = util.ui.Localizer.getLocalizerFor(ChannelFilterComponent.class);
  

  private OrderChooser mList;
  private Channel[] mSelectedChannels;
  private String mName, mDescription;

	public ChannelFilterComponent(String name, String desc) {
    mSelectedChannels = new Channel[]{};
    mName = name;
    mDescription = desc;
	}
  
  public ChannelFilterComponent() {
    this("", "");
  }
  
  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {    
    ArrayList channels=new ArrayList();
    int channelCnt = in.readInt();    
    
    for (int i=0; i<channelCnt; i++) {
      if(version < 3) {
        String dataServiceClassName = (String)in.readObject();
        String groupId = null;
      
        if(version >= 2)
          groupId = (String)in.readObject();
      
        String channelId = (String)in.readObject();
        Channel ch = Channel.getChannel(dataServiceClassName, groupId, null, channelId); 
        
        if (ch!=null)
          channels.add(ch);  
      } 
      else {
        Channel ch = Channel.readData(in, true); 
        
        if (ch!=null)
          channels.add(ch);        
      }
    }
    mSelectedChannels = new Channel[channels.size()];
    channels.toArray(mSelectedChannels);        
  }
    

  public void write(ObjectOutputStream out) throws IOException {    
    out.writeInt(mSelectedChannels.length);
    
    for (int i=0; i<mSelectedChannels.length;i++)
      mSelectedChannels[i].writeData(out);
  } 
  
	public String toString() {
		return mLocalizer.msg("channel","Channel");
	}


	public void ok() {
    Object[] o = mList.getOrder();
    mSelectedChannels = new Channel[o.length];
    for (int i=0; i<o.length; i++) {
      mSelectedChannels[i] = (Channel)o[i];
    }
	}

	

	
	public JPanel getPanel() {
    JPanel content = new JPanel(new BorderLayout());
    content.add(new JLabel(mLocalizer.msg("description","This filter accepts programs belonging to the following channels:")),BorderLayout.NORTH);
    Channel[] channels = tvbrowser.core.ChannelList.getSubscribedChannels();
    mList = new OrderChooser(mSelectedChannels, channels);
    
    mList.getUpButton().setVisible(false);
    mList.getDownButton().setVisible(false);
    
    content.add(mList,BorderLayout.WEST);
    
		return content;
	}

	
	public boolean accept(Program program) {
    for (int i=0; i<mSelectedChannels.length; i++) {
      if (mSelectedChannels[i].equals(program.getChannel())) {
        return true;
      }
    }    
		return false;
	}


	public int getVersion() {
		return 3;
	}

	
	public String getName() {
		return mName;
	}

	
	public String getDescription() {
		return mDescription;
	}

	public void setName(String name) {
    mName = name;
	}
  
  public void setDescription(String desc) {
    mDescription = desc;
  }
	

  
  
  
}