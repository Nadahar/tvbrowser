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

package tvbrowser.core;

import java.util.*;
import java.io.*;


/**
 * ChannelList contains a list of all available channels in the system.
 * Use this class to subscribe channels.
 * The available channels are listed in the file CHANNEL_FILE.
 */

public class ChannelList {

    private static Vector channels;
    public static final String CHANNEL_FILE="channels.prop";
    
    private static ArrayList subscribedChannels=new ArrayList();
    

    /**
     * If the file CHANNEL_FILE does not exist, a default channel list is created by calling this
     * method.
     */
    public static void createDefaultChannelList() {
        channels=new Vector();
        channels.add(new Channel("ARD",1));
        channels.add(new Channel("ZDF",2));
        channels.add(new Channel("RTL",3));
        channels.add(new Channel("Sat.1",4));
        channels.add(new Channel("Pro 7",5));
        channels.add(new Channel("VOX",8));
        
        
		channels.add(new Channel("TV 5", 201));
		channels.add(new Channel("Kabel 1", 202));
		channels.add(new Channel("Premiere", 203));
		channels.add(new Channel("Bayern", 204));
		channels.add(new Channel("TRT", 205));
		channels.add(new Channel("SWR", 206));
		channels.add(new Channel("n-tv", 207));
		channels.add(new Channel("arte", 208));
		channels.add(new Channel("VIVA", 209));
		channels.add(new Channel("MTV", 210));
		channels.add(new Channel("MTV2", 211));
		channels.add(new Channel("mdr", 212));
		channels.add(new Channel("Neunlive", 213));
		channels.add(new Channel("3Sat", 214));
		channels.add(new Channel("EuroSport", 215));
		channels.add(new Channel("RTL 2", 216));
		channels.add(new Channel("ORF 2", 217));
		channels.add(new Channel("Kinder Kanal", 218));
		channels.add(new Channel("NBC", 219));
		channels.add(new Channel("Nord 3", 220));
		channels.add(new Channel("Hessen", 221));
		channels.add(new Channel("Phoenix", 222));
		channels.add(new Channel("CNN", 223));
		channels.add(new Channel("ORB", 224));
		channels.add(new Channel("SF1", 225));
		channels.add(new Channel("SUPER RTL", 226));
		channels.add(new Channel("DSF", 227));
		channels.add(new Channel("EuroNews", 228));
		channels.add(new Channel("ORF 1", 229));
    }

    /**
     * Opens the file CHANNEL_FILE to extract the available channels.
     */
    public static void readChannelList() throws IOException {
        BufferedReader in=new BufferedReader(new FileReader(CHANNEL_FILE));
        String line, a, b;
        int pos, id;
        for(;;) {
            line=in.readLine();
            if (line==null) break;
            pos=line.indexOf(' ');
            a=line.substring(0,pos);
            b=line.substring(pos+1);

            id=Integer.parseInt(a);
            Channel ch=new Channel(b,id);
            channels.add(ch);

        }
        in.close();
    }

    /**
     * Stores the channel file CHANNEL_FILE.
     */
    public static void writeChannelList() throws IOException {
        PrintWriter out=new PrintWriter(new FileWriter(CHANNEL_FILE));
        Enumeration enum=channels.elements();
        while (enum.hasMoreElements()) {
            Channel ch=(Channel)enum.nextElement();
            out.println(ch.getId()+" "+ch.getName());
        }
        out.close();
    }
    
    /**
     * Subscribes a channel
     * @param id the channel's ID
     */
    public static void subscribeChannel(int id) {
    	
    	Channel ch=getChannel(id);
    	subscribedChannels.add(ch);
    }

    /**
     * Marks the specified channels as 'subscribed'. All other channels become
     * 'unsubscribed'
     *
     * @param channelNames the names of the subscribed channels (array of String)
     */
    public static void setSubscribeChannels(Object[] channelNames) {
		Channel curChannel;
    	subscribedChannels=new ArrayList();
    	for (int i=0;i<channelNames.length;i++) {
			curChannel=getChannel((String)channelNames[i]);
			if (curChannel==null) {
			 	throw new RuntimeException("curChannel is null");
			}
			subscribedChannels.add(curChannel);		   
    	}
    	
    	
     
    }

    /**
     * Returns a new Channel object with the specified ID or null, if the
     * given ID does not exist.
     */
    public static Channel getChannel(int id) {
      Channel result;
      Enumeration enum=channels.elements();
      while (enum.hasMoreElements()) {
          result=(Channel)enum.nextElement();
          if (result.getId()==id) {
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
    public static boolean isSubscribedChannel(int id) {
      
      for (int i=0;i<subscribedChannels.size();i++) {
      	
      	Channel ch=(Channel)subscribedChannels.get(i);
      	int curId=ch.getId();
      	if (curId==id) {
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