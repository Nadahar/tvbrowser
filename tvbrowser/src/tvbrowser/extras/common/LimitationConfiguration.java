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

package tvbrowser.extras.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import devplugin.Channel;


public class LimitationConfiguration {

  public static final int DAYLIMIT_DAILY = -1;
  public static final int DAYLIMIT_WEEKEND = -2;
  public static final int DAYLIMIT_WEEKDAY = -3;
  public static final int DAYLIMIT_SUNDAY = Calendar.SUNDAY;  // 1
  public static final int DAYLIMIT_MONDAY = Calendar.MONDAY;   // 2
  public static final int DAYLIMIT_TUESDAY = Calendar.TUESDAY;
  public static final int DAYLIMIT_WEDNESDAY = Calendar.WEDNESDAY;
  public static final int DAYLIMIT_THURSDAY = Calendar.THURSDAY;
  public static final int DAYLIMIT_FRIDAY = Calendar.FRIDAY;
  public static final int DAYLIMIT_SATURDAY = Calendar.SATURDAY;



  private int mFrom, mTo;
  private Channel[] mChannelArr;
  private ArrayList<ChannelItem> mChannelItemList = new ArrayList<ChannelItem>();
  private boolean mIsLimitedByChannel;
  private boolean mIsLimitedByTime;
  private int mDayLimit;



  public LimitationConfiguration(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();  // version

    mIsLimitedByTime = in.readBoolean();
    if (mIsLimitedByTime) {
      mFrom = in.readInt();
      mTo = in.readInt();
    }

    mIsLimitedByChannel = in.readBoolean();
    if (mIsLimitedByChannel) {
      int cnt = in.readInt();
      ArrayList<Channel> list = new ArrayList<Channel>();
      for (int i=0; i<cnt; i++) {
        ChannelItem item = new ChannelItem(in, version);
        
        if(item.isValid()) {
          mChannelItemList.add(item);
        }

        if (item.getChannel() != null) {
          list.add(item.getChannel());
        }
      }
      
      mChannelArr = list.toArray(new Channel[list.size()]);
    }

    mDayLimit = in.readInt();
  }

  public LimitationConfiguration() {
    mDayLimit = DAYLIMIT_DAILY;
  }

  public void store(ObjectOutputStream out) throws IOException {
    out.writeInt(3); // version

    out.writeBoolean(mIsLimitedByTime);
    if (mIsLimitedByTime) {
      out.writeInt(mFrom);
      out.writeInt(mTo);
    }

    out.writeBoolean(mIsLimitedByChannel);
    if (mIsLimitedByChannel) {
      out.writeInt(mChannelItemList.size());
      for (int i=0; i<mChannelItemList.size(); i++) {
        (mChannelItemList.get(i)).saveItem(out);
      }
    }

    out.writeInt(mDayLimit);
  }

  public void setTime(int from, int to) {
    mFrom = from;
    mTo = to;
    mIsLimitedByTime = true;
  }

  public int getTimeFrom() {
    return mFrom;
  }

  public int getTimeTo() {
    return mTo;
  }

  public void setChannels(Channel[] ch) {
    mChannelItemList.clear();
    
    for (Channel element : ch) {
      mChannelItemList.add(new ChannelItem(element));
    }
    
    mChannelArr = ch;
    mIsLimitedByChannel = true;
  }

  public Channel[] getChannels() {
    return mChannelArr;
  }

  public boolean isLimitedByChannel() {
    return mIsLimitedByChannel;
  }

  public boolean isLimitedByTime() {
    return mIsLimitedByTime;
  }

  public void setIsLimitedByChannel(boolean b) {
    mIsLimitedByChannel = b;
  }

  public void setIsLimitedByTime(boolean b) {
    mIsLimitedByTime = b;
  }

  public void setDayLimit(int daylimit) {
    mDayLimit = daylimit;
  }

  public int getDayLimit() {
    return mDayLimit;
  }
}
