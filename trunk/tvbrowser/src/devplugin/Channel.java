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

package devplugin;

import java.io.*;
import java.util.TimeZone;

import tvdataservice.TvDataService;

public class Channel {

  private TvDataService mDataService;
  private String mName;
  private String mId;
  private TimeZone mTimeZone;
  private String mCountry;
  private int mDayLightSavingTimeCorrection;



  public Channel(TvDataService dataService, String name, String id,
    TimeZone timeZone, String country)
  {
    if (country.length() != 2) {
      throw new IllegalArgumentException("country must be a two character "
        + "ISO country code (as used in top level domains, e.g. 'de' or 'us')");
    }
    
    mDataService = dataService;
    mName = name;
    mId = id;
    mTimeZone=timeZone;
    mCountry = country;
    mDayLightSavingTimeCorrection=0;
  }

  public Channel(TvDataService dataService, String name, TimeZone timeZone,
    String country)
  {
    this(dataService, name, name, timeZone, country);
  }
  
  /**
   * @deprecated
   */
  public Channel(TvDataService dataService, String name, String id,
    TimeZone timeZone)
  {
    this(dataService, name, id, timeZone, "de");
  }
  
  /**
   * @deprecated
   */
  public Channel(TvDataService dataService, String name, TimeZone timeZone) {
    this(dataService, name, name, timeZone);
  }
  
  /**
   * @deprecated
   */
  public Channel(TvDataService dataService, String name, String id) {
    this(dataService, name, id, TimeZone.getDefault());
  }

  /**
   * @deprecated
   */
  public Channel(TvDataService dataService, String name) {
    this(dataService, name, name);
  }



  public static Channel readData(ObjectInputStream in, boolean allowNull)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    
    String dataServiceClassName = (String) in.readObject();
    
    String channelId;
    
    
    if (version==1) {
    	channelId=""+in.readInt();
    }
    else {
    	channelId=(String)in.readObject();
    }
        
    Channel channel = getChannel(dataServiceClassName, channelId);
    if ((channel == null) && (! allowNull)) {
      throw new IOException("Channel with id " + channelId + " of data service "
        + dataServiceClassName + " not found!");
    }
    return channel;
  }
  


  /**
   * Serialized this object.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version

    out.writeObject(mDataService.getClass().getName());
    out.writeObject(mId);
  }

  
  
  public static Channel getChannel(String dataServiceClassName, String channelId) {
    if (dataServiceClassName == null) {
      // Fast return
      return null;
    }
    
    Channel[] channelArr = Plugin.getPluginManager().getSubscribedChannels();
    for (int i = 0; i < channelArr.length; i++) {
      if (dataServiceClassName.equals(channelArr[i].getDataService().getClass().getName())
        && (channelArr[i].getId().equals(channelId)))
      {
        return channelArr[i];
      }      
    }
    
    return null;
  }
  
  public TimeZone getTimeZone() {
    return mTimeZone;
  }
  
  public String getCountry() {
    return mCountry;
  }
  
  public void setDayLightSavingTimeCorrection(int correction) {
    mDayLightSavingTimeCorrection=correction;
  }
  
  public int getDayLightSavingTimeCorrection() {
    return mDayLightSavingTimeCorrection;
  }

  public TvDataService getDataService() {
    return mDataService;
  }



  public String toString() {
    if (mDataService!=null) {
      return mName + " (" + mDataService.getInfo().getName() + ")";
    }
    return mName;
  }



  public String getName() {
    return mName;
  }



  public String getId() {
    return mId;
  }



  public boolean equals(Object obj) {
    if (obj instanceof Channel) {
      Channel cmp = (Channel) obj;
      return (mDataService == cmp.mDataService) && (mId == cmp.mId);
    }

    return false;
  }

}
