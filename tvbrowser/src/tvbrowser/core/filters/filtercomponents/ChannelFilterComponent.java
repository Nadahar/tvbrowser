package tvbrowser.core.filters.filtercomponents;

import java.io.*;
import java.util.ArrayList;

import javax.swing.*;

import tvbrowser.core.filters.FilterComponent;
import util.ui.*;


import java.awt.*;

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
      String dataServiceClassName = (String)in.readObject();
      String channelId = (String)in.readObject();
      Channel ch = Channel.getChannel(dataServiceClassName, channelId); 
      if (ch!=null) {
        channels.add(ch);  
      }
    }
    mSelectedChannels = new Channel[channels.size()];
    channels.toArray(mSelectedChannels);        
  }
    

  public void write(ObjectOutputStream out) throws IOException {    
    out.writeInt(mSelectedChannels.length);
    for (int i=0; i<mSelectedChannels.length;i++) {
      Channel ch = mSelectedChannels[i];   
      out.writeObject(ch.getDataService().getClass().getName());
      out.writeObject(ch.getId());   
    }    
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
		return 1;
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