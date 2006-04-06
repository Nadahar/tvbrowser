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

import devplugin.Channel;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class LimitationConfiguration {

  private int mFrom, mTo;
  private Channel[] mChannelArr;
  private boolean mIsLimitedByChannel;
  private boolean mIsLimitedByTime;



  public LimitationConfiguration(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // version

    mIsLimitedByTime = in.readBoolean();
    if (mIsLimitedByTime) {
      mFrom = in.readInt();
      mTo = in.readInt();
    }

    mIsLimitedByChannel = in.readBoolean();
    if (mIsLimitedByChannel) {
      int cnt = in.readInt();
      ArrayList list = new ArrayList();
      for (int i=0; i<cnt; i++) {
        String channelServiceClassName = (String) in.readObject();
        String certainChannelId=(String)in.readObject();
        Channel channel = Channel.getChannel(channelServiceClassName, certainChannelId);
        if (channel != null) {
          list.add(channel);
        }
      }
      mChannelArr = (Channel[])list.toArray(new Channel[list.size()]);
    }
  }

  public LimitationConfiguration() {

  }

  public void store(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version

    out.writeBoolean(mIsLimitedByTime);
    if (mIsLimitedByTime) {
      out.writeInt(mFrom);
      out.writeInt(mTo);
    }

    out.writeBoolean(mIsLimitedByChannel);
    if (mIsLimitedByChannel) {
      out.writeInt(mChannelArr.length);
      for (int i=0; i<mChannelArr.length; i++) {
        out.writeObject(mChannelArr[i].getDataServiceProxy().getId());
        out.writeObject(mChannelArr[i].getId());
      }
    }

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




}
