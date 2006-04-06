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
import devplugin.Program;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class Exclusion {

  private Channel mChannel;
  private String mTopic;
  private String mTitle;
  private int mTimeFrom;
  private int mTimeTo;

  /**
   * Creates a new exclusion criteria.
   * @param title null, if any title is allowed
   * @param topic null, if any keyword is allowed
   * @param channel null, if any channel is allowed
   * @param timeFrom lower time limit (or -1, if no lower limit exists)
   * @param timeTo upper time limit (or -1, if no upper limit exists)
   */
  public Exclusion(String title, String topic, Channel channel, int timeFrom, int timeTo) {
    mTitle = title;
    mTopic = topic;
    mChannel =channel;
    mTimeFrom = timeFrom;
    mTimeTo = timeTo;
  }

  public Exclusion(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.readInt();  // version

    boolean hasChannel = in.readBoolean();
    if (hasChannel) {
      String channelServiceClassName = (String) in.readObject();
      String channelId=(String)in.readObject();
      mChannel = Channel.getChannel(channelServiceClassName, channelId);
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

  }


  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);  // version
    out.writeBoolean(mChannel != null);
    if (mChannel != null) {
      out.writeObject(mChannel.getDataServiceProxy().getId());
      out.writeObject(mChannel.getId());
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


  public boolean isProgramExcluded(Program prog) {
    boolean channelExcl = false;
    boolean titleExcl = false;
    boolean topicExcl = false;
    boolean timeExcl = false;

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
      String description = prog.getDescription()+" "+prog.getShortInfo();
      if (description != null && description.indexOf(mTopic) >=0) {
        topicExcl = true;
      }
    }
    else {
      topicExcl = true;
    }

    int progTime = prog.getHours()*60 + prog.getMinutes();
    if (mTimeFrom >=0 && mTimeTo >=0) {
      if (progTime >= mTimeFrom && progTime <=mTimeTo) {
        timeExcl = true;
      }
    }
    else if (mTimeFrom >=0) {
      if (progTime >= mTimeFrom) {
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

    return channelExcl && titleExcl && topicExcl && timeExcl;
  }

}
