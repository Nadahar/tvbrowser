/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package tvbrowser.ui.settings;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

import tvbrowser.ui.customizableitems.CustomizableItemsPanel;
import tvbrowser.core.*;


public class ChannelsSettingsTab extends devplugin.SettingsTab {

  private CustomizableItemsPanel panel;
  public String getName() { return "Channels"; }

  public ChannelsSettingsTab() {
    super();
    setLayout(new BorderLayout());
    panel=CustomizableItemsPanel.createCustomizableItemsPanel("Available channels:","Subscribed channels:");

    JTextArea textArea=new JTextArea(2,40);
    textArea.setOpaque(false);
    textArea.setEditable(false);
    textArea.setDisabledTextColor(Color.black);
    textArea.setEnabled(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setText("Use the arrow buttons to subscribe or unsubscribe channels and the up and down buttons to create your personal ordering of your favourite channels.");
    textArea.setBorder(BorderFactory.createEmptyBorder(10,5,5,0));


    add(panel,BorderLayout.CENTER);
    add(textArea,BorderLayout.SOUTH);

    Channel ch;
    Enumeration enum=ChannelList.getChannels();

    Channel[] subscribedChannels=new Channel[ChannelList.getNumberOfSubscribedChannels()];

    while (enum.hasMoreElements()) {
      ch=(Channel)enum.nextElement();
      if (ch.isSubscribed()) {
        subscribedChannels[ch.getPos()]=ch;
      }else{
        panel.addElementLeft(ch.getName());
      }
    }

    for (int i=0;i<subscribedChannels.length;i++) {
      panel.addElementRight(subscribedChannels[i].getName());
    }



  }

  public void ok() {

    StringBuffer channels=new StringBuffer();
    Object[] list=panel.getElementsRight();
    ChannelList.setSubscribeChannels(list);
    Channel ch;
    for (int i=0;i<list.length;i++) {
      ch=ChannelList.getChannel((String)list[i]);
      if (ch==null) {
        continue;
      }

      channels.append(ch.getId());
      if (i<list.length-1) {
        channels.append(",");
      }
    }

    Settings.setSubscribedChannels(channels.toString());


  }
}