/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.extras.favoritesplugin.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Iterator;

import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.extras.common.ChannelItem;
import tvbrowser.extras.common.LimitationConfiguration;
import devplugin.Channel;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;

public class Exclusion {

  public static final int DAYLIMIT_DAILY = LimitationConfiguration.DAYLIMIT_DAILY;
  private static final int DAYLIMIT_WEEKEND = LimitationConfiguration.DAYLIMIT_WEEKEND;
  private static final int DAYLIMIT_SUNDAY = LimitationConfiguration.DAYLIMIT_SUNDAY;
  private static final int DAYLIMIT_SATURDAY = LimitationConfiguration.DAYLIMIT_SATURDAY;

  private ChannelItem mChannel;
  private String mTopic;
  private String mTitle;
  private int mTimeFrom;
  private int mTimeTo;
  private int mDayOfWeek;
  private String mFilterName;

  /**
   * Creates a new exclusion criteria.
   * @param title null, if any title is allowed
   * @param topic null, if any keyword is allowed
   * @param channel null, if any channel is allowed
   * @param timeFrom lower time limit (or -1, if no lower limit exists)
   * @param timeTo upper time limit (or -1, if no upper limit exists)
   * @param dayOfWeek The day of week to use.
   * @param filterName The name of the filter to use;
   */
  public Exclusion(String title, String topic, Channel channel, int timeFrom, int timeTo, int dayOfWeek, String filterName) {
    mTitle = title;
    mTopic = topic;
    mChannel = new ChannelItem(channel);
    mTimeFrom = timeFrom;
    mTimeTo = timeTo;
    mDayOfWeek = dayOfWeek;
    mFilterName = filterName;
  }

  public Exclusion(ObjectInputStream in) throws ClassNotFoundException, IOException {
    int version = in.readInt();  // version
    
    boolean hasChannel = in.readBoolean();
    if (hasChannel) {
      if(version < 3) {
        String channelServiceClassName = (String) in.readObject();
        String channelGroupId = null;
      
        if(version >= 2) {
          channelGroupId = (String) in.readObject();
        }
      
        String channelId=(String)in.readObject();
        Channel ch = Channel.getChannel(channelServiceClassName, channelGroupId, null, channelId);
        
        mChannel = new ChannelItem(ch);
      }
      else if (version < 5) {
        Channel ch = Channel.readData(in, true);
        mChannel = new ChannelItem(ch);
      }
      else {
        mChannel = new ChannelItem(in,3);
      }
    }

    boolean hasTitle = in.readBoolean();
    if (hasTitle) {
      mTitle = (String)in.readObject();
    }

    boolean hasTopic = in.readBoolean();
    if (hasTopic) {
      mTopic = (String)in.readObject();
    }
    
    if(version > 3) {
      if(in.readBoolean()) {
        mFilterName = (String)in.readObject();
      }
    }

    mTimeFrom = in.readInt();
    mTimeTo = in.readInt();
    mDayOfWeek = in.readInt();
    
    if(mChannel == null) {
      mChannel = new ChannelItem(null);
    }
  }


  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(5);  // version
    out.writeBoolean(mChannel != null);
    if (mChannel != null) {
      mChannel.saveItem(out);
    }

    out.writeBoolean(mTitle != null);
    if (mTitle != null) {
      out.writeObject(mTitle);
    }

    out.writeBoolean(mTopic != null);
    if (mTopic != null) {
      out.writeObject(mTopic);
    }
    
    out.writeBoolean(mFilterName != null);
    if(mFilterName != null) {
      out.writeObject(mFilterName);
    }

    out.writeInt(mTimeFrom);
    out.writeInt(mTimeTo);
    out.writeInt(mDayOfWeek);
  }


  public String getTitle() {
    return mTitle;
  }

  public String getTopic() {
    return mTopic;
  }
  
  public ProgramFilter getFilter() {
    ProgramFilter[] filters = FilterManagerImpl.getInstance().getAvailableFilters();
    
    for(ProgramFilter filter : filters) {
      if(filter.getName().equals(mFilterName)) {
        return filter;
      }
    }
    
    return null;
  }

  public Channel getChannel() {
    return mChannel.getChannel();
  }

  public int getTimeLowerBound() {
    return mTimeFrom;
  }

  public int getTimeUpperBound() {
    return mTimeTo;
  }

  public int getDayOfWeek() {
    return mDayOfWeek;
  }


  public boolean isProgramExcluded(Program prog) {
    boolean channelExcl = false;
    boolean titleExcl = false;
    boolean topicExcl = false;
    boolean timeExcl = false;
    boolean dayExcl = false;
    boolean filterExclusion = false;
    
    if(isInvalid()) {
      return false;
    }
    
    if (mChannel != null && !mChannel.isNullChannel()) {
      Channel ch = prog.getChannel();
      if (ch.equals(mChannel.getChannel())) {
        channelExcl = true;
      }
    }
    else {
      channelExcl = true;
    }
    
    if (mTitle != null) {
      if (mTitle.equalsIgnoreCase(prog.getTitle())) {
        titleExcl = true;
      }
    }
    else {
      titleExcl = true;
    }

    if (mTopic != null) {
      Iterator<ProgramFieldType> types = ProgramFieldType.getTypeIterator();
      StringBuilder value = new StringBuilder();
      
      while(types.hasNext()) {
        ProgramFieldType type = types.next();
        
        if(type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
          value.append(prog.getTextField(type)).append(' ');
        }
      }
            
      if (value.toString() != null && value.toString().toLowerCase().indexOf(mTopic.toLowerCase()) >=0) {
        topicExcl = true;
      }
    } else {
      topicExcl = true;
    }
    
    if(mFilterName != null) {
      ProgramFilter filter = getFilter();
      
      if(filter != null) {
        filterExclusion = filter.accept(prog);
      }
      else {
        filterExclusion = true;
      }
    }
    else {
      filterExclusion = true;
    }

    int timeFromParsed = mTimeFrom;
    int progTime = prog.getHours()*60 + prog.getMinutes();

    if(mTimeFrom > mTimeTo) {
      timeFromParsed -= 60*24;
      
      if(progTime > mTimeTo) {
        progTime -= 24*60;
      }
    }
        
    if (mTimeFrom >=0 && mTimeTo >=0) {
      if (progTime >= timeFromParsed && progTime <= mTimeTo) {
        timeExcl = true;
      }
    }
    else if (mTimeFrom >=0) {
      if (progTime >= timeFromParsed) {
        timeExcl = true;
      }
    }
    else if (mTimeTo >=0) {
      if (progTime <= mTimeTo) {
        timeExcl = true;
      }
    }
    else {
      timeExcl = true;
    }

    if (mDayOfWeek != DAYLIMIT_DAILY) {
      int dayOfWeek = prog.getDate().getCalendar().get(Calendar.DAY_OF_WEEK);
      if (mDayOfWeek >=1 && mDayOfWeek <=7) {
        if (dayOfWeek == mDayOfWeek) {
          dayExcl = true;
        }
      } else if (mDayOfWeek == DAYLIMIT_WEEKEND) {
        dayExcl = dayOfWeek == DAYLIMIT_SUNDAY || dayOfWeek == DAYLIMIT_SATURDAY;
      } else /* (mDayOfWeek == DAYLIMIT_WEEKDAY) */ {
        dayExcl = dayOfWeek > 1 && dayOfWeek <7;
      }
    }
    else {
      dayExcl = true;
    }
    
    return channelExcl && titleExcl && topicExcl && timeExcl && dayExcl && filterExclusion;
  }
  
  /**
   * Gets if this Exclusion is invalid.
   * 
   * @return <code>True</code> if this Exclusion is invalid, <code>false</code> otherwise.
   */
  public boolean isInvalid() {
    return (mTitle == null && mTopic == null && !mChannel.isAvailableOrNullChannel() && mFilterName == null && mTimeFrom == -1 &&mTimeTo == -1 && mDayOfWeek == Exclusion.DAYLIMIT_DAILY) || !mChannel.isAvailableOrNullChannel();
  }
}
