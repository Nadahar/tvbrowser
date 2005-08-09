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

package favoritesplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import util.exc.TvBrowserException;
import util.ui.SearchFormSettings;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;
import devplugin.ProgramSearcher;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class Favorite {
  
  private SearchFormSettings mSearchFormSettings;
  
  private boolean mUseCertainChannel;
  private Channel[] mCertainChannels;
  private boolean mUseCertainTimeOfDay;
  private int mCertainFromTime, mCertainToTime;
  
  private boolean mUseFilter;
  private ProgramFilter mFilter;

  private String mTitle;

  private Program[] mProgramArr;
  
  
  
  /**
   * Creates a new instance of Favorite.
   */
  public Favorite() {
    mSearchFormSettings = new SearchFormSettings("");
    
    mUseCertainChannel = false;
    mCertainChannels = null;
    mUseCertainTimeOfDay = false;
    mCertainFromTime = 0;
    mCertainToTime = 23 * 60 + 59; // 23:59
    
    mProgramArr = new Program[0];
    
    mUseFilter = false;
    mFilter = null;
  }


  /**
   * Creates a new instance of Favorite.
   */
  public Favorite(String searchText) {
    this();
    
    mSearchFormSettings.setSearchText(searchText);
  }
  
  
  /**
   * Deserializes this Object.
   */
  public Favorite(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    
    if (version <= 2) {
      String term = (String) in.readObject();
      in.readBoolean(); // searchInTitle
      boolean searchInText = in.readBoolean();
      int searchMode = in.readInt();
  
      mSearchFormSettings = new SearchFormSettings(term);
      if (searchInText) {
        mSearchFormSettings.setSearchIn(SearchFormSettings.SEARCH_IN_ALL);
      } else {
        mSearchFormSettings.setSearchIn(SearchFormSettings.SEARCH_IN_TITLE);
      }
      
      switch (searchMode) {
        case 1: mSearchFormSettings.setSearcherType(PluginManager.SEARCHER_TYPE_EXACTLY); break;
        case 2: mSearchFormSettings.setSearcherType(PluginManager.SEARCHER_TYPE_KEYWORD); break;
        case 3: mSearchFormSettings.setSearcherType(PluginManager.SEARCHER_TYPE_REGULAR_EXPRESSION); break;
      }
    } else {
      mSearchFormSettings = new SearchFormSettings(in);
    }

    if (version >=5) {
      mTitle = (String)in.readObject();
    }
    else {
      mTitle = mSearchFormSettings.getSearchText();
    }
    mUseCertainChannel = in.readBoolean();

    if (version < 6) {
      String certainChannelServiceClassName = (String) in.readObject();
      String certainChannelId;
      if (version==1) {
        certainChannelId=""+in.readInt();
      }else{
        certainChannelId=(String)in.readObject();
      }
      Channel ch = Channel.getChannel(certainChannelServiceClassName, certainChannelId);
      if (ch != null) {
        mCertainChannels = new Channel[]{ch};
      } else {
        mUseCertainChannel = false;
      }
    }
    else {
      if (mUseCertainChannel) {
        int cnt = in.readInt();
        ArrayList list = new ArrayList();
        for (int i=0; i<cnt; i++) {
          String certainChannelServiceClassName = (String) in.readObject();
          String certainChannelId;
          if (version==1) {
            certainChannelId=""+in.readInt();
          }else{
            certainChannelId=(String)in.readObject();
          }

          Channel channel = Channel.getChannel(certainChannelServiceClassName, certainChannelId);
          if (channel != null) {
            list.add(channel);
          }
        }
        mCertainChannels = new Channel[list.size()];
        list.toArray(mCertainChannels);
      }
    }
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
    
    if (version >=4) {
        mUseFilter = in.readBoolean();
        
        mFilter = getFilterByName((String)in.readObject());
    } else {
        mUseFilter = false;
    }
  }

  /**
   * Returns a specific Filter 
   * @param name Name of the Filter
   * @return The Filter if found, otherwise null 
   */
  private ProgramFilter getFilterByName(String name ){
    ProgramFilter[] flist = Plugin.getPluginManager().getAvailableFilters();
    
    for (int i=0; i<flist.length;i++) {
      if (flist[i].getName().equals(name)) {
        return flist[i];
      }
    }
    
    return null;
  }
  
  /**
   * Serializes this Object.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(6); // version: 6

    mSearchFormSettings.writeData(out);

    out.writeObject(mTitle);

    out.writeBoolean(mUseCertainChannel);

    String certainChannelServiceClassName;

    if (mUseCertainChannel) {
      String certainChannelId;
      out.writeInt(mCertainChannels.length);
      for (int i=0; i<mCertainChannels.length; i++) {
        certainChannelServiceClassName = mCertainChannels[i].getDataService().getClass().getName();
        certainChannelId = mCertainChannels[i].getId();
        out.writeObject(certainChannelServiceClassName);
        out.writeObject(certainChannelId);
      }
    }
    
    out.writeBoolean(mUseCertainTimeOfDay);
    out.writeInt(mCertainFromTime);
    out.writeInt(mCertainToTime);

    // Don't save the programs but only their date and id
    out.writeInt(mProgramArr.length);
    for (int i = 0; i < mProgramArr.length; i++) {
      mProgramArr[i].getDate().writeData(out);
      out.writeObject(mProgramArr[i].getID());
    }
    
    if (mFilter != null) {
        out.writeBoolean(mUseFilter);
        out.writeObject(mFilter.getName());
    } else {
        out.writeBoolean(false);
        out.writeObject("NULL");
    }
  }


  public SearchFormSettings getSearchFormSettings() {
    return mSearchFormSettings;
  }

  public void setSearchFormSettings(SearchFormSettings settings) {
    mSearchFormSettings = settings;
  }
  
  
  public boolean getUseCertainChannel() {
    return mUseCertainChannel;
  }
  
  public void setUseCertainChannel(boolean useCertainChannel) {
    mUseCertainChannel = useCertainChannel;
  }

  
  
  public Channel[] getCertainChannels() {
    return mCertainChannels;
  }
  
  public void setCertainChannels(Channel[] certainChannel) {
    mCertainChannels = certainChannel;
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


  public boolean getUseFilter() {
      return mUseFilter;
  }
  
  public void setUseFilter(boolean usefilter) {
      mUseFilter = usefilter;
  }
  
  public ProgramFilter getFilter() {
      return mFilter;
  }
  
  public void setFilter(ProgramFilter filter) {
      mFilter = filter;
  }
  
  public Program[] getPrograms() {
    return mProgramArr;
  }

  
  
  public void unmarkPrograms() {
    FavoritesPlugin.getInstance().unmark(mProgramArr);
  }
  

  
  public void updatePrograms() throws TvBrowserException {
    // Unmark all programs in the old list
    unmarkPrograms();
    
    // Search for matching programs
    ProgramFieldType[] fieldArr = mSearchFormSettings.getFieldTypes();
    devplugin.Date startDate = new devplugin.Date();
    int nrDays = 1000;
    Channel[] channels;
    if (mUseCertainChannel) {
      if ((mCertainChannels == null) || (mCertainChannels[0] == null)) {
        channels = new Channel[0];
      } else {
        channels = new Channel[] { mCertainChannels[0] };
      }
    } else {
      channels = Plugin.getPluginManager().getSubscribedChannels();
    }

    ProgramSearcher searcher = mSearchFormSettings.createSearcher();
    Program[] matchingProgArr = searcher.search(fieldArr, startDate, nrDays,
                                                channels, true);
    
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
    
    if (mUseFilter && (mFilter != null)) {
        ArrayList passedList = new ArrayList();
        for (int i = 0; i < mProgramArr.length; i++) {
            if (mFilter.accept(mProgramArr[i])) {
                passedList.add(mProgramArr[i]);
            }
        }
        
        mProgramArr = new Program[passedList.size()];
        passedList.toArray(mProgramArr);
    }
    
    // mark these programs
    FavoritesPlugin.getInstance().mark(mProgramArr);
  }
  
  public void setTitle(String title) {
    mTitle = title;  
  }
  
  public String getTitle() {
    if (mTitle == null) {
      return mSearchFormSettings.getSearchText();
    }
    return mTitle;
  }
  
}
