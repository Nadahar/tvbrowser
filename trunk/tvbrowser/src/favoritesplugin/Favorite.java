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

package favoritesplugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import util.exc.TvBrowserException;

import devplugin.*;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class Favorite {
  
  public static final int MODE_MATCH_EXACTLY = 1;
  public static final int MODE_TERM_IS_KEYWORD = 2;
  public static final int MODE_TERM_IS_REGEX = 3;
  
  private String mTerm;
  private boolean mSearchInTitle, mSearchInText;
  private int mSearchMode;
  private boolean mUseCertainChannel;
  private Channel mCertainChannel;
  private boolean mUseCertainTimeOfDay;
  private int mCertainFromTime, mCertainToTime;
  
  private Program[] mProgramArr;
  
  
  
  /**
   * Creates a new instance of Favorite.
   */
  public Favorite() {
    mTerm = "";
    mSearchInTitle = true;
    mSearchInText = false;
    mSearchMode = MODE_MATCH_EXACTLY;
    mUseCertainChannel = false;
    mCertainChannel = null;
    mUseCertainTimeOfDay = false;
    mCertainFromTime = 0;
    mCertainToTime = 23 * 60 + 59; // 23:59
    
    mProgramArr = new Program[0];
  }

  
  
  /**
   * Deserializes this Object.
   */
  public Favorite(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    
    mTerm = (String) in.readObject();
    mSearchInTitle = in.readBoolean();
    mSearchInText = in.readBoolean();
    mSearchMode = in.readInt();
    mUseCertainChannel = in.readBoolean();

    String certainChannelServiceClassName = (String) in.readObject();
    int certainChannelId = in.readInt();
    mCertainChannel = Channel.getChannel(certainChannelServiceClassName, certainChannelId);

    mUseCertainTimeOfDay = in.readBoolean();
    mCertainFromTime = in.readInt();
    mCertainToTime = in.readInt();

    // Don't save the programs but only their date and id
    int size = in.readInt();
    ArrayList programList = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      Date date = new Date(in);
      String progID = (String) in.readObject();
      Program program = Plugin.getPluginManager().getProgram(date, progID);
      if (program != null) {
        programList.add(program);
      }
    }
    mProgramArr = new Program[programList.size()];
    programList.toArray(mProgramArr);
  }
  
  
  
  /**
   * Serializes this Object.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    
    out.writeObject(mTerm);
    out.writeBoolean(mSearchInTitle);
    out.writeBoolean(mSearchInText);
    out.writeInt(mSearchMode);
    out.writeBoolean(mUseCertainChannel);

    String certainChannelServiceClassName = null;
    int certainChannelId = -1;
    if (mCertainChannel != null) {
      certainChannelServiceClassName = mCertainChannel.getDataService().getClass().getName();
      certainChannelId = mCertainChannel.getId();
    }
    out.writeObject(certainChannelServiceClassName);
    out.writeInt(certainChannelId);
    
    out.writeBoolean(mUseCertainTimeOfDay);
    out.writeInt(mCertainFromTime);
    out.writeInt(mCertainToTime);

    // Don't save the programs but only their date and id
    out.writeInt(mProgramArr.length);
    for (int i = 0; i < mProgramArr.length; i++) {
      mProgramArr[i].getDate().writeData(out);
      out.writeObject(mProgramArr[i].getID());
    }
  }

  
  
  public String getTerm() {
    return mTerm;
  }

  public void setTerm(String term) {
    mTerm = term;
  }

  
  
  public boolean getSearchInTitle() {
    return mSearchInTitle;
  }
  
  public void setSearchInTitle(boolean searchInTitle) {
    mSearchInTitle = searchInTitle;
  }

  
  
  public boolean getSearchInText() {
    return mSearchInText;
  }
  
  public void setSearchInText(boolean searchInText) {
    mSearchInText = searchInText;
  }

  
  
  public int getSearchMode() {
    return mSearchMode;
  }
  
  public void setSearchMode(int searchMode) {
    mSearchMode = searchMode;
  }

  
  
  public boolean getUseCertainChannel() {
    return mUseCertainChannel;
  }
  
  public void setUseCertainChannel(boolean useCertainChannel) {
    mUseCertainChannel = useCertainChannel;
  }

  
  
  public Channel getCertainChannel() {
    return mCertainChannel;
  }
  
  public void setCertainChannel(Channel certainChannel) {
    mCertainChannel = certainChannel;
  }

  
  
  public boolean getUseCertainTimeOfDay() {
    return mUseCertainTimeOfDay;
  }
  
  public void setUseCertainTimeOfDay(boolean useCertainTimeOfDay) {
    mUseCertainTimeOfDay = useCertainTimeOfDay;
  }

  
  
  public int getCertainFromTime() {
    return mCertainFromTime;
  }
  
  public void setCertainFromTime(int certainFromTime) {
    mCertainFromTime = certainFromTime;
  }

  
  
  public int getCertainToTime() {
    return mCertainToTime;
  }
  
  public void setCertainToTime(int certainToTime) {
    mCertainToTime = certainToTime;
  }

  
  
  public Program[] getPrograms() {
    return mProgramArr;
  }
  

  
  public void updatePrograms() throws TvBrowserException {
    // Unmark all programs in the old list
    FavoritesPlugin.getInstance().unmark(mProgramArr);
    
    // Search for matching programs
    String regex;
    switch (mSearchMode) {
      case MODE_MATCH_EXACTLY:   regex = "\\Q" + mTerm + "\\E"; break;
      case MODE_TERM_IS_KEYWORD: regex = ".*\\Q" + mTerm + "\\E.*"; break;
      default:                   regex = mTerm; break;
    }
    boolean inTitle = mSearchInTitle;
    boolean inText = mSearchInText;
    boolean caseSensitive = false;
    Channel[] channels;
    if (mUseCertainChannel) {
      if (mCertainChannel == null) {
        channels = new Channel[0];
      } else {
        channels = new Channel[] { mCertainChannel };
      }
    } else {
      channels = Plugin.getPluginManager().getSubscribedChannels();
    }
    devplugin.Date startDate = new devplugin.Date();
    int nrDays = 1000;
    
    Program[] matchingProgArr = Plugin.getPluginManager().search(regex, inTitle,
      inText, caseSensitive, channels, startDate, nrDays);
    
    // Check whether the program is within the specified time of day
    if (mUseCertainTimeOfDay) {
      ArrayList passedList = new ArrayList();
      for (int i = 0; i < matchingProgArr.length; i++) {
        Program program = matchingProgArr[i];
        int startTime = program.getHours() * 60 + program.getMinutes();
        if ((startTime >= mCertainFromTime) && (startTime <= mCertainToTime)) {
          passedList.add(program);
        }
      }
      mProgramArr = new Program[passedList.size()];
      passedList.toArray(mProgramArr);
    } else {
      mProgramArr = matchingProgArr;
    }
    
    // mark these programs
    FavoritesPlugin.getInstance().mark(mProgramArr);
  }
  
}
