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

import devplugin.Channel;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

import tvbrowser.extras.common.LimitationConfiguration;

public class Exclusion {

  public static final int DAYLIMIT_DAILY = LimitationConfiguration.DAYLIMIT_DAILY;
  public static final int DAYLIMIT_WEEKEND = LimitationConfiguration.DAYLIMIT_WEEKEND;
  public static final int DAYLIMIT_WEEKDAY = LimitationConfiguration.DAYLIMIT_WEEKDAY;
  public static final int DAYLIMIT_SUNDAY = LimitationConfiguration.DAYLIMIT_SUNDAY;
  public static final int DAYLIMIT_MONDAY = LimitationConfiguration.DAYLIMIT_MONDAY;
  public static final int DAYLIMIT_TUESDAY = LimitationConfiguration.DAYLIMIT_TUESDAY;
  public static final int DAYLIMIT_WEDNESDAY = LimitationConfiguration.DAYLIMIT_WEDNESDAY;
  public static final int DAYLIMIT_THURSDAY = LimitationConfiguration.DAYLIMIT_THURSDAY;
  public static final int DAYLIMIT_FRIDAY = LimitationConfiguration.DAYLIMIT_FRIDAY;
  public static final int DAYLIMIT_SATURDAY = LimitationConfiguration.DAYLIMIT_SATURDAY;

  private Channel mChannel;
  private String mTopic;
  private String mTitle;
  private int mTimeFrom;
  private int mTimeTo;
  private int mDayOfWeek;

  /**
   * Creates a new exclusion criteria.
   * @param title null, if any title is allowed
   * @param topic null, if any keyword is allowed
   * @param channel null, if any channel is allowed
   * @param timeFrom lower time limit (or -1, if no lower limit exists)
   * @param timeTo upper time limit (or -1, if no upper limit exists)
   */
  public Exclusion(String title, String topic, Channel channel, int timeFrom, int timeTo, int dayOfWeek) {
    mTitle = title;
    mTopic = topic;
    mChannel =channel;
    mTimeFrom = timeFrom;
    mTimeTo = timeTo;
    mDayOfWeek = dayOfWeek;
  }

  public Exclusion(ObjectInputStream in) throws ClassNotFoundException, IOException {
    int version = in.readInt();  // version
    
    boolean hasChannel = in.readBoolean();
    if (hasChannel) {
      if(version < 3) {
        String channelServiceClassName = (String) in.readObject();
        String channelGroupId = null;
      
        if(version >= 2)
          channelGroupId = (String) in.readObject();
      
        String channelId=(String)in.readObject();
        mChannel = Channel.getChannel(channelServiceClassName, channelGroupId, null, channelId);
      }
      else {
        mChannel = Channel.readData(in, true);
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

    mTimeFrom = in.readInt();
    mTimeTo = in.readInt();
    mDayOfWeek = in.readInt();

  }


  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(3);  // version
    out.writeBoolean(mChannel != null);
    if (mChannel != null) {
      mChannel.writeData(out);
    }

    out.writeBoolean(mTitle != null);
    if (mTitle != null) {
      out.writeObject(mTitle);
    }

    out.writeBoolean(mTopic != null);
    if (mTopic != null) {
      out.writeObject(mTopic);
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

  public Channel getChannel() {
    return mChannel;
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
    
    if(isInvalid())
      return false;
    
    if (mChannel != null) {
      Channel ch = prog.getChannel();
      if (ch.equals(mChannel)) {
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
      Iterator types = ProgramFieldType.getTypeIterator();
      StringBuffer value = new StringBuffer();
      
      while(types.hasNext()) {
        ProgramFieldType type = (ProgramFieldType)types.next();
        
        if(type.getFormat() == ProgramFieldType.TEXT_FORMAT)
          value.append(prog.getTextField(type)).append(" ");
      }
            
      if (value.toString() != null && value.toString().toLowerCase().indexOf(mTopic.toLowerCase()) >=0)
        topicExcl = true;
    }
    else
      topicExcl = true;

    int timeFromParsed = mTimeFrom;
    int progTime = prog.getHours()*60 + prog.getMinutes();

    if(mTimeFrom > mTimeTo) {
      timeFromParsed -= 60*24;
      
      if(progTime > mTimeTo)
        progTime -= 24*60;
    }
        
    if (mTimeFrom >=0 && mTimeTo >=0) {
      if (progTime >= timeFromParsed && progTime <= mTimeTo)
        timeExcl = true;
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

    return channelExcl && titleExcl && topicExcl && timeExcl && dayExcl;
  }
  
  /**
   * Gets if this Exclusion is invalid.
   * 
   * @return <code>True</code> if this Exclusion is invalid, <code>false</code> otherwise.
   */
  public boolean isInvalid() {
    return mTitle == null && mTopic == null && mChannel == null && mTimeFrom == -1 &&mTimeTo == -1 && mDayOfWeek == Exclusion.DAYLIMIT_DAILY;
  }
}
