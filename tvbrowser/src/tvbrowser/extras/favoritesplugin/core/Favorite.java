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

import devplugin.*;

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Calendar;

import util.exc.TvBrowserException;
import util.program.ProgramUtilities;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.reminderplugin.ReminderPlugin;

public abstract class Favorite {

  private Program[] mPrograms;
  private Program[] mNewProgramsArr;
  private String mName;
  private ReminderConfiguration mReminderConfiguration;
  private LimitationConfiguration mLimitationConfiguration;
  private boolean mRemindAfterDownload;
  private ArrayList mExclusionList;
  private PluginAccess[] mForwardPluginArr;

  public Favorite() {
    mReminderConfiguration = new ReminderConfiguration();
    mLimitationConfiguration = new LimitationConfiguration();
    mPrograms = new Program[]{};
    mNewProgramsArr = new Program[]{};
    mExclusionList = new ArrayList();

    mForwardPluginArr = FavoritesPlugin.getInstance().getDefaultClientPlugins();
  }

  public Favorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // version
    mName = (String)in.readObject();
    mReminderConfiguration = new ReminderConfiguration(in);
    mLimitationConfiguration = new LimitationConfiguration(in);
    mRemindAfterDownload = in.readBoolean();

    mExclusionList = new ArrayList();
    int exclSize = in.readInt();
    for (int i=0; i<exclSize; i++) {
      mExclusionList.add(new Exclusion(in));
    }

    ArrayList list = new ArrayList();
    int cnt = in.readInt();
    for (int i=0; i<cnt; i++) {
      String id = (String)in.readObject();
      PluginAccess plugin = Plugin.getPluginManager().getActivatedPluginForId(id);
      if (plugin != null) {
        list.add(plugin);
      }
    }
    mForwardPluginArr = (PluginAccess[])list.toArray(new PluginAccess[list.size()]);

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

    mPrograms = new Program[programList.size()];
    programList.toArray(mPrograms);
    mNewProgramsArr = new Program[]{};
  }


  public abstract String getTypeID();

  public String getName() {
    return mName;
  }

  public void setName(String name) {
    mName = name;
  }

  public LimitationConfiguration getLimitationConfiguration() {
    return mLimitationConfiguration;
  }

  public ReminderConfiguration getReminderConfiguration() {
    return mReminderConfiguration;
  }


  public void setRemindAfterDownload(boolean b) {
    mRemindAfterDownload = b;
  }

  public boolean isRemindAfterDownload() {
    return mRemindAfterDownload;
  }


  public void setForwardPlugins(PluginAccess[] pluginArr) {
    mForwardPluginArr = pluginArr;
  }

  public PluginAccess[] getForwardPlugins() {
    return mForwardPluginArr;
  }

  public Program[] getPrograms() {
    return mPrograms;
  }

  public Program[] getNewPrograms() {
    return mNewProgramsArr;
  }


  public void handleContainingPrograms(Program[] progs) {
    for(int i = 0; i < mPrograms.length; i++) {
      for(int j = 0; j < progs.length; j++) {
        if(mPrograms[i].equals(progs[j])) {
          progs[j].mark(FavoritesPlugin.MARKER);
        }
      }
    }
  }




  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);  // version
    out.writeObject(mName);
    mReminderConfiguration.store(out);
    mLimitationConfiguration.store(out);
    out.writeBoolean(mRemindAfterDownload);

    out.writeInt(mExclusionList.size());
    for (int i=0; i<mExclusionList.size(); i++) {
      ((Exclusion)mExclusionList.get(i)).writeData(out);
    }

    out.writeInt(mForwardPluginArr.length);
    for (int i=0; i<mForwardPluginArr.length; i++) {
      out.writeObject(mForwardPluginArr[i].getId());
    }

    // Don't save the programs but only their date and id
    out.writeInt(mPrograms.length);
    for (int i = 0; i < mPrograms.length; i++) {
      mPrograms[i].getDate().writeData(out);
      out.writeObject(mPrograms[i].getID());
    }

    internalWriteData(out);
  }



  public Exclusion[] getExclusions() {
    return (Exclusion[])mExclusionList.toArray(new Exclusion[mExclusionList.size()]);
  }

  public void addExclusion(Exclusion exclusion) {
    mExclusionList.add(exclusion);
  }

  public void removeExclusion(Exclusion exclusion) {
    mExclusionList.remove(exclusion);
  }

  public void setExclusions(Exclusion[] exclusionArr) {
    mExclusionList.clear();
    for (int i=0; i<exclusionArr.length; i++) {
      mExclusionList.add(exclusionArr[i]);
    }
  }


  public boolean contains(Program prog) {
    for(int i = 0; i < mPrograms.length; i++) {
      if(mPrograms[i].equals(prog)) {
        return true;
      }
    }

    return false;
  }


  private Program[] filterByLimitations(Program[] progArr) {

    Exclusion[] exclusions = getExclusions();
    ArrayList list = new ArrayList();
    boolean isLimitedByTime = getLimitationConfiguration().isLimitedByTime();
    int timeFrom = getLimitationConfiguration().getTimeFrom();
    int timeFromParsed = timeFrom;
    int timeTo = getLimitationConfiguration().getTimeTo();

    if(timeFrom > timeTo)
      timeFromParsed -= 60*24;
    
    int allowedDayOfWeek = getLimitationConfiguration().getDayLimit();
    for (int i=0; i<progArr.length; i++) {
      boolean isExcluded = false;
      for (int j=0; j<exclusions.length; j++) {
        if (exclusions[j].isProgramExcluded(progArr[i])) {
          isExcluded = true;
          break;
        }
      }
      if (!isExcluded && isLimitedByTime) {
        int startTime = progArr[i].getHours() * 60 + progArr[i].getMinutes(); 
          
        if(timeFrom > timeTo && startTime >= timeFrom)
          startTime -= 60*24;
        
        if (startTime < timeFromParsed || startTime > timeTo) {
          isExcluded = true;
        }
        else {
          if (allowedDayOfWeek != LimitationConfiguration.DAYLIMIT_DAILY) {
            Calendar cal = progArr[i].getDate().getCalendar();
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (allowedDayOfWeek == LimitationConfiguration.DAYLIMIT_WEEKEND) {
              if (dayOfWeek != 1 && dayOfWeek != 7) {
                isExcluded = true;
              }
            }
            else if (allowedDayOfWeek == LimitationConfiguration.DAYLIMIT_WEEKDAY) {
              if (dayOfWeek == 1 || dayOfWeek == 7) {
                isExcluded = true;
              }
            }
            else if (allowedDayOfWeek != dayOfWeek) {
              isExcluded = true;
            }
          }
        }
      }

      if (!isExcluded) {
        list.add(progArr[i]);
      }
    }

    return (Program[])list.toArray(new Program[list.size()]);

  }

  /**
   * Performs a new search, and refreshes the program marks
   * @throws TvBrowserException
   */
  public void updatePrograms() throws TvBrowserException {

    Channel[] channelArr;
    if (getLimitationConfiguration().isLimitedByChannel()) {
      channelArr = getLimitationConfiguration().getChannels();
    }
    else {
      channelArr = Plugin.getPluginManager().getSubscribedChannels();
    }

    updatePrograms(internalSearchForPrograms(channelArr));
  }
  
  /**
   * Checks all current programs if the are not excluded,
   * and refreshes the program marks.
   * @throws TvBrowserException
   */
  public void refreshPrograms() throws TvBrowserException {
    updatePrograms(mPrograms);
  }
  
  private void updatePrograms(Program[] progs) throws TvBrowserException {
    Program[] newProgList = filterByLimitations(progs);


    /* Now we have to lists:
         - mPrograms:   previous favorite programs
         - newProgList: new favorite programs

       We walk through the lists and remove or add the programs:
       For all programs from mPrograms, that don't exist in newProgList, we REMOVE it from new ProgList
       For all programs from newProgList, that don't exist in mPrograms, we ADD it to mPrograms

     */
    Comparator comparator = ProgramUtilities.getProgramComparator();
    Arrays.sort(newProgList, comparator);
    Arrays.sort(mPrograms, comparator);

    Program[] p1 = mPrograms;
    Program[] p2 = newProgList;

    ArrayList resultList = new ArrayList();
    ArrayList newPrograms = new ArrayList();

    int inx1 = 0;
    int inx2 = 0;
    while (inx1 < p1.length && inx2 < p2.length) {
      if (comparator.compare(p1[inx1], p2[inx2]) < 0) {
        // remove p1[inx1]
        unmarkProgram(p1[inx1]);
        inx1++;
      }
      else if (comparator.compare(p1[inx1], p2[inx2]) > 0) {
        // add (p2[inx2]
        markProgram(p2[inx2]);
        newPrograms.add(p2[inx2]);
        resultList.add(p2[inx2]);
        inx2++;
      }
      else {
        // leave
        // unmark the old instance of the program        
        p1[inx1].unmark(FavoritesPlugin.MARKER);
        // mark the new instance of the program
        p2[inx2].mark(FavoritesPlugin.MARKER);
        resultList.add(p2[inx2]);
        inx1++;
        inx2++;
      }
    }

    if (inx2 < p2.length) {
      // add (p2[inx2]..p2[p2.length-1])
      for (int i=inx2; i<p2.length; i++) {
        markProgram(p2[i]);
        newPrograms.add(p2[i]);
        resultList.add(p2[i]);
      }
    }
    if (inx1 < p1.length) {
      // remove (p1[inx1]..p1[p1.length-1])
      for (int i=inx1; i<p1.length; i++) {
        unmarkProgram(p1[i]);
      }
    }

    // pass programs to plugins
    mNewProgramsArr = (Program[])newPrograms.toArray(new Program[newPrograms.size()]);
    PluginAccess[] pluginArr = getForwardPlugins();
    for (int i=0; i<pluginArr.length; i++) {
      pluginArr[i].receivePrograms(mNewProgramsArr);
    }


    mPrograms = (Program[])resultList.toArray(new Program[resultList.size()]);
  }


  private void markProgram(Program p) {
    p.mark(FavoritesPlugin.MARKER);
    String[] reminderServices = getReminderConfiguration().getReminderServices();
    for (int i=0; i<reminderServices.length; i++) {
      if (ReminderConfiguration.REMINDER_DEFAULT.equals(reminderServices[i])) {
        ReminderPlugin.getInstance().addProgram(p);
      }
    }


  }

  private void unmarkProgram(Program p) {
    if(!FavoritesPlugin.getInstance().isContainedByOtherFavorites(this,p)) {
      p.unmark(FavoritesPlugin.MARKER);
    }
      
    String[] reminderServices = getReminderConfiguration().getReminderServices();
    for (int i=0; i<reminderServices.length; i++) {
      if (ReminderConfiguration.REMINDER_DEFAULT.equals(reminderServices[i])) {
        ReminderPlugin.getInstance().removeProgram(p);
      }
    }

  }

  public abstract FavoriteConfigurator createConfigurator();

  protected abstract void internalWriteData(ObjectOutputStream out) throws IOException;

  protected abstract Program[] internalSearchForPrograms(Channel[] channelArr) throws TvBrowserException;

}
