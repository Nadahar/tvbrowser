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

package tvbrowser.ui.settings;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import devplugin.Channel;
import tvbrowser.ui.customizableitems.CustomizableItemsPanel;
import tvbrowser.core.*;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ChannelsSettingsTab extends devplugin.SettingsTab {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(ChannelsSettingsTab.class.getName());
  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ChannelsSettingsTab.class);
  
  private CustomizableItemsPanel panel;
  public String getName() {
    return mLocalizer.msg("channels", "Channels");
  }

  
  
  public ChannelsSettingsTab() {
    super();
    
    String msg;
    
    setLayout(new BorderLayout());
	setBorder(BorderFactory.createEmptyBorder(20,30,20,30));
	
    String leftText = mLocalizer.msg("availableChannels", "Available channels");
    String rightText = mLocalizer.msg("subscribedChannels", "Subscribed channels");
    panel = CustomizableItemsPanel.createCustomizableItemsPanel(leftText, rightText);

    JTextArea textArea=new JTextArea(2,40);
    textArea.setOpaque(false);
    textArea.setEditable(false);
    textArea.setDisabledTextColor(Color.black);
    textArea.setEnabled(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    msg = mLocalizer.msg("infoText", "Use the left and right buttons to "
      + "subscribe or unsubscribe channels and the up and down buttons to "
      + "create your personal ordering of your favourite channels.");
    textArea.setText(msg);
    textArea.setBorder(BorderFactory.createEmptyBorder(10,5,5,0));


    add(panel,BorderLayout.CENTER);
    add(textArea,BorderLayout.SOUTH);

    Channel ch;
    Iterator iter = ChannelList.getChannels();

    Channel[] subscribedChannels=new Channel[ChannelList.getNumberOfSubscribedChannels()];
    mLog.fine("ok till now");
    while (iter.hasNext()) {
      ch = (Channel) iter.next();
      mLog.fine("channel "+ch.getName());
      //if (ch.isSubscribed()) {
      if (ChannelList.isSubscribedChannel(ch)) {
      	mLog.fine("is subscribed");
      //  subscribedChannels[ch.getPos()]=ch;
     	int pos=ChannelList.getPos(ch);
      	subscribedChannels[pos]=ch;
      }else{
        panel.addElementLeft(ch);
        mLog.fine("is NOT subscribed");
      }
    }
    mLog.fine("done");
    
    mLog.fine("subscribedChannel.length: "+subscribedChannels.length);

    for (int i=0;i<subscribedChannels.length;i++) {
      panel.addElementRight(subscribedChannels[i]);
    }

    mLog.fine("DONE!");
  }

  
  
  public void ok() {
    Object[] list = panel.getElementsRight();
    
    // Convert the list into a Channel[] and fill channels
    Channel[] channelArr = new Channel[list.length];
    for (int i = 0; i < list.length; i++) {
      channelArr[i] = (Channel) list[i];      
    }
    
    ChannelList.setSubscribeChannels(channelArr);
    Settings.setSubscribedChannels(channelArr);
  }
  
}