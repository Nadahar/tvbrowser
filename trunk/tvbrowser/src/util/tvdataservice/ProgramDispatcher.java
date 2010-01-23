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

package util.tvdataservice;

import java.util.HashMap;

import tvdataservice.MutableChannelDayProgram;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;

/**
 * You can pass Program objects to this dispatcher and it will put it in the
 * right AbstractChannelDayProgram.
 * <p>
 * After passing all programs to the dispatcher you can request the
 * AbstractChannelDayPrograms.
 *
 * @author  Til Schneider, www.murfman.de
 */
public class ProgramDispatcher {

  /**
   * Contains for a Date (key) a hash containing for a Channel (key)
   * an MutableChannelDayProgram (value).
   */
  private HashMap<Date, HashMap<Channel, MutableChannelDayProgram>> mDayProgramHash;



  /**
   * Creates a new instance of ProgramDispatcher.
   */
  public ProgramDispatcher() {
    mDayProgramHash = new HashMap<Date, HashMap<Channel, MutableChannelDayProgram>>();
  }



  /**
   * Dispatches the specified program to the right ChannelDayProgram.
   * <p>
   * If there is no such ChannelDayProgram, it will be created.
   *
   * @param program The program to dispatch
   */
  public void dispatch(Program program) {
    
    Date date = program.getDate();
    Channel channel = program.getChannel();

    MutableChannelDayProgram channelDayProg
      = getChannelDayProgram(date, channel, true);

    channelDayProg.addProgram(program);
  }



  /**
   * Gets the ChannelDayProgram of the specified date and channel.
   *
   * @param date The date of the wanted ChannelDayProgram.
   * @param channel The channel of the wanted ChannelDayProgram.
   * @return The ChannelDayProgram of the specified date and channel.
   */
  public MutableChannelDayProgram getChannelDayProgram(Date date, Channel channel) {
    MutableChannelDayProgram channelDayProgram
      = getChannelDayProgram(date, channel, false);

    /*
    if (channelDayProgram != null) {
      mLog.info("Found " + channelDayProgram.getProgramCount()
        + " programs for " + channel.getName() + " on " + date);
    }
    */

    return channelDayProgram;
  }



  /**
   * Gets the ChannelDayProgram of the specified date and channel.
   *
   * @param date The date of the wanted ChannelDayProgram.
   * @param channel The channel of the wanted ChannelDayProgram.
   * @param createIfNotExisting Specifies whether the ChannelDayProgram should
   *        be created if there was no matching ChannelDayProgram found. If
   *        false null is returned in this case.
   * @return The ChannelDayProgram of the specified date and channel.
   */
  private MutableChannelDayProgram getChannelDayProgram(Date date,
    Channel channel, boolean createIfNotExisting)
  {
    // get the hash containg for the channel (key)
    // the MutableChannelDayProgram (value)
    HashMap<Channel, MutableChannelDayProgram> channelDayProgramHash = mDayProgramHash.get(date);
    if (channelDayProgramHash == null) {
      if (createIfNotExisting) {
        channelDayProgramHash = new HashMap<Channel, MutableChannelDayProgram>();
        mDayProgramHash.put(date, channelDayProgramHash);
      } else {
        return null;
      }
    }

    // get the MutableChannelDayProgram
    MutableChannelDayProgram channelDayProg
      = channelDayProgramHash.get(channel);
    if (channelDayProg == null) {
      if (createIfNotExisting) {
        channelDayProg = new MutableChannelDayProgram(date, channel);
        channelDayProgramHash.put(channel, channelDayProg);
      } else {
        return null;
      }
    }

    return channelDayProg;
  }

}
