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
 */

package tvbrowser.core;

import java.util.*;
import java.io.*;

import util.exc.TvBrowserException;

/**
 * ChannelList contains a list of all available channels in the system.
 * Use this class to subscribe channels.
 * The available channels are listed in the file CHANNEL_FILE.
 *
 * @author Martin Oberhauser
 */
public class ChannelList {
  
  private static Vector channels=new Vector();
  public static final String CHANNEL_FILE="channels.prop";
  
  private static ArrayList subscribedChannels=new ArrayList();
  
   
  
	public static void addDataLoaderChannels(String dataloaderName) {
		
		tvdataloader.TVDataServiceInterface dataloader=DataLoaderManager.getDataLoader(dataloaderName);
		int num=dataloader.getNumberOfAvailableChannels();
		Channel[] channelList=new Channel[num];
		for (int i=0;i<num;i++) {
			channelList[i]=new Channel(dataloaderName);
		}
		dataloader.initializeAvailableChannels(channelList);
		for (int i=0;i<num;i++) {
			channels.add(channelList[i]);
		}
	}
  
  /**
   * Stores the channel file CHANNEL_FILE.
   */
  public static void writeChannelList() throws TvBrowserException {
	PrintWriter out = null;
	try {
	  new PrintWriter(new FileWriter(CHANNEL_FILE));
	  Enumeration enum=channels.elements();
	  while (enum.hasMoreElements()) {
		Channel ch=(Channel)enum.nextElement();
		out.println(ch.getId()+" "+ch.getName());
	  }
	}
	catch (IOException exc) {
	  throw new TvBrowserException(ChannelList.class, "error.2", 
		"Can't write channel list!\n({0})", CHANNEL_FILE, exc);
	}
	finally {
	  if (out != null) {
		out.close();
	  }
	}
  }
  
  
  
  /**
   * Subscribes a channel
   * @param id the channel's ID
   */
  public static void subscribeChannel(String loader, int id) {  
	Channel ch=getChannel(loader,id);
	subscribedChannels.add(ch);
  }
  
  
  
  
  /**
   * Marks the specified channels as 'subscribed'. All other channels become
   * 'unsubscribed'
   *
   * @param channels the subscribed channels (array of String)
   */
  public static void setSubscribeChannels(Object[] channels) {
	subscribedChannels=new ArrayList();
	for (int i=0;i<channels.length;i++) {
		subscribedChannels.add(channels[i]);
	}
  }

  
  /**
   * Returns a new Channel object with the specified ID or null, if the
   * given ID does not exist.
   */
  public static Channel getChannel(String loader, int id) {
	Channel result;
	Enumeration enum=channels.elements();
	while (enum.hasMoreElements()) {
	  result=(Channel)enum.nextElement();
	  if (result.getDataServiceName().equals(loader) && result.getId()==id) {
		return result;
	  }
	}
	return null;
  }
  
  
  
  public static int getPos(int id) {
	for (int i=0;i<subscribedChannels.size();i++) {    
	  Channel ch=(Channel)subscribedChannels.get(i);
	  int curId=ch.getId();
	  if (curId==id) {
		return i;
	  }
	}
	return -1;
  }
  
  
  
  /**
   * Returns a new Channel object with the specified Name or null, if
   * the given  channel name does not exist.
   */
  public static Channel getChannel(String name) {
	Channel result;
	Enumeration enum=channels.elements();
	while (enum.hasMoreElements()) {
	  result=(Channel)enum.nextElement();
	  if (result.getName().equals(name)) {
		return result;
	  }
	}
	return null;
  }
  
  
  
  /**
   * Returns an Enumeration of all available Channel objects.
   */
  public static Enumeration getChannels() {
	return channels.elements();
  }
  
  
  
  /**
   * Returns true, if the specified channel is currently subscribed.
   */
  public static boolean isSubscribedChannel(Channel channel) {
	if (channel==null) return false;
	for (int i=0;i<subscribedChannels.size();i++) {    
	  Channel ch=(Channel)subscribedChannels.get(i);
	  if (ch!=null && ch.getId()==channel.getId() && ch.getDataServiceName().equals(channel.getDataServiceName())) {
		return true;
	  }
	}
	return false;
  }
  
  
  
  /**
   * Returns the number of subscribed channels.
   */
  public static int getNumberOfSubscribedChannels() {  
	return subscribedChannels.size();
  }
  
  
  
  /**
   * Returns all subscribed channels.
   */
  public static Channel[] getSubscribedChannels() {
	Channel[] result=new Channel[subscribedChannels.size()];
	for (int i=0;i<subscribedChannels.size();i++) {
	  result[i]=(Channel)subscribedChannels.get(i);
	}
	return result;
  }
  

}