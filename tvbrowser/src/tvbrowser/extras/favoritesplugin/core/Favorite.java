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
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesDialog;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import util.exc.TvBrowserException;
import util.program.ProgramUtilities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

public abstract class Favorite {

  private Program[] mPrograms;
  private Program[] mNewProgramsArr;
  private String mName;
  private ReminderConfiguration mReminderConfiguration;
  private LimitationConfiguration mLimitationConfiguration;
  private boolean mRemindAfterDownload;
  private ArrayList<Exclusion> mExclusionList;
  private ProgramReceiveTarget[] mForwardPluginArr;

  private ArrayList<Program> mBlackList;
  
  public Favorite() {
    mReminderConfiguration = new ReminderConfiguration(FavoritesPlugin.getInstance().isAutoSelectingRemider() ? new String[] {ReminderConfiguration.REMINDER_DEFAULT} : new String[0]);
    mLimitationConfiguration = new LimitationConfiguration();
    mPrograms = new Program[]{};
    mNewProgramsArr = new Program[]{};
    mExclusionList = new ArrayList<Exclusion>();
    mBlackList = new ArrayList<Program>();

    mForwardPluginArr = FavoritesPlugin.getInstance().getDefaultClientPluginsTargets();
  }

  public Favorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();  // version
    mName = (String)in.readObject();
    mReminderConfiguration = new ReminderConfiguration(in);
    mLimitationConfiguration = new LimitationConfiguration(in);
    mRemindAfterDownload = in.readBoolean();

    mExclusionList = new ArrayList<Exclusion>();
    int exclSize = in.readInt();
    for (int i=0; i<exclSize; i++) {
      Exclusion exclusion = new Exclusion(in);
      
      if(!exclusion.isInvalid())
        mExclusionList.add(exclusion);
    }

    int cnt = in.readInt();
    mForwardPluginArr = new ProgramReceiveTarget[cnt];

    for (int i=0; i<cnt; i++) {
      if(version <=2) {
        String id = (String)in.readObject();
        mForwardPluginArr[i] = ProgramReceiveTarget.createDefaultTargetForProgramReceiveIfId(id);
      }
      else
        mForwardPluginArr[i] = new ProgramReceiveTarget(in);
    }
        
    // Don't save the programs but only their date and id
    int size = in.readInt();
    ArrayList<Program> programList = new ArrayList<Program>(size);
    readProgramsToList(programList, size, in);
    
    if(version >= 2) {
      size = in.readInt();
      mBlackList = new ArrayList<Program>(size);
      readProgramsToList(mBlackList, size, in);
    }
    else
      mBlackList = new ArrayList<Program>();
    
    mPrograms = programList.toArray(new Program[programList.size()]);
    
    mNewProgramsArr = new Program[]{};
  }

  private void readProgramsToList(ArrayList<Program> list, int size, ObjectInputStream in) throws IOException, ClassNotFoundException {
    for (int i = 0; i < size; i++) {
      Date date = new Date(in);
      String progID = (String) in.readObject();
      Program program = Plugin.getPluginManager().getProgram(date, progID);
      if (program != null) {
        list.add(program);
      }
    }
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


  public void setForwardPlugins(ProgramReceiveTarget[] pluginArr) {
    mForwardPluginArr = pluginArr;
  }

  public ProgramReceiveTarget[] getForwardPlugins() {
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
          progs[j].mark(FavoritesPluginProxy.getInstance());
        }
      }
    }
  }




  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(3);  // version
    out.writeObject(mName);
    mReminderConfiguration.store(out);
    mLimitationConfiguration.store(out);
    out.writeBoolean(mRemindAfterDownload);

    out.writeInt(mExclusionList.size());
    for (int i=0; i<mExclusionList.size(); i++) {
      (mExclusionList.get(i)).writeData(out);
    }

    out.writeInt(mForwardPluginArr.length);
    for (int i=0; i<mForwardPluginArr.length; i++) {
      mForwardPluginArr[i].writeData(out);
    }

    // Don't save the programs but only their date and id
    out.writeInt(mPrograms.length);
    for (int i = 0; i < mPrograms.length; i++) {
      mPrograms[i].getDate().writeData(out);
      out.writeObject(mPrograms[i].getID());
    }
    
    // Save the programs on BlackList
    out.writeInt(mBlackList.size());
    for(int i = 0; i < mBlackList.size(); i++) {
      Program p = mBlackList.get(i);
      p.getDate().writeData(out);
      out.writeObject(p.getID());
    }

    internalWriteData(out);
  }



  public Exclusion[] getExclusions() {
    return mExclusionList.toArray(new Exclusion[mExclusionList.size()]);
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
    if(mBlackList.contains(prog))
      return false;
    
    for(int i = 0; i < mPrograms.length; i++) {
      if(mPrograms[i].equals(prog)) {
        return true;
      }
    }

    return false;
  }


  private Program[] filterByLimitations(Program[] progArr) {

    Exclusion[] exclusions = getExclusions();
    ArrayList<Program> list = new ArrayList<Program>();
    
    int allowedDayOfWeek = getLimitationConfiguration().getDayLimit();
    for (int i=0; i<progArr.length; i++) {
      boolean isExcluded = false;
      for (int j=0; j<exclusions.length; j++) {
        if (exclusions[j].isProgramExcluded(progArr[i])) {
          isExcluded = true;
          break;
        }
      }
      if (!isExcluded && getLimitationConfiguration().isLimitedByTime()) {        
        if (ProgramUtilities.isNotInTimeRange(getLimitationConfiguration().getTimeFrom(),getLimitationConfiguration().getTimeTo(),progArr[i]))
          isExcluded = true;
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

    return list.toArray(new Program[list.size()]);

  }

  /**
   * Performs a new search, and refreshes the program marks
   * @throws TvBrowserException
   */
  public void updatePrograms() throws TvBrowserException {
    updatePrograms(false, true);
  }
  
  /**
   * Performs a new search, and refreshes the program marks
   * @param send If the new found programs should be send to plugins.
   * @throws TvBrowserException
   */
  public void updatePrograms(boolean send) throws TvBrowserException {
    updatePrograms(false, send);
  }

  /**
   * Performs a new search, and refreshes the program marks
   * @param dataUpdate The update was started after a data update.
   * @param send If the new found programs should be send to plugins.
   * @throws TvBrowserException
   */
  public void updatePrograms(boolean dataUpdate, boolean send) throws TvBrowserException {
    Channel[] channelArr;
    if (getLimitationConfiguration().isLimitedByChannel()) {
      channelArr = getLimitationConfiguration().getChannels();
    }
    else {
      channelArr = Plugin.getPluginManager().getSubscribedChannels();
    }

    updatePrograms(internalSearchForPrograms(channelArr), dataUpdate, send);
  }
  
  /**
   * Checks all current programs if the are not excluded,
   * and refreshes the program marks.
   * @throws TvBrowserException
   */
  public void refreshPrograms() throws TvBrowserException {
    updatePrograms(mPrograms, false, false);
  }
  
  private void updatePrograms(Program[] progs, boolean dataUpdate, boolean send) throws TvBrowserException {
    Program[] newProgList = filterByLimitations(progs);


    /* Now we have to lists:
         - mPrograms:   previous favorite programs
         - newProgList: new favorite programs

       We walk through the lists and remove or add the programs:
       For all programs from mPrograms, that don't exist in newProgList, we REMOVE it from new ProgList
       For all programs from newProgList, that don't exist in mPrograms, we ADD it to mPrograms

     */
    Comparator<Program> comparator = ProgramUtilities.getProgramComparator();
    Arrays.sort(newProgList, comparator);
    Arrays.sort(mPrograms, comparator);

    Program[] p1 = mPrograms;
    Program[] p2 = newProgList;

    ArrayList<Program> resultList = new ArrayList<Program>();
    ArrayList<Program> newPrograms = new ArrayList<Program>();

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
        /* leave
         *         
         * check if the new found program is a new instance 
         * of the program an mark it if it is so. */
        if(p1[inx1].getProgramState() == Program.WAS_DELETED_STATE)
          markProgram(p2[inx2]);
          
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
    mNewProgramsArr = newPrograms.toArray(new Program[newPrograms.size()]);
    ProgramReceiveTarget[] pluginArr = getForwardPlugins();
    
    if(mNewProgramsArr.length > 0 && send) {
      if(!dataUpdate) {
        for (int i=0; i<pluginArr.length; i++) {
          if(pluginArr[i] != null && pluginArr[i].getReceifeIfForIdOfTarget() != null)
            pluginArr[i].getReceifeIfForIdOfTarget().receivePrograms(mNewProgramsArr,pluginArr[i]);
        }
      }
      else
        FavoritesPlugin.getInstance().addProgramsForSending(pluginArr, mNewProgramsArr);
    }

    mPrograms = resultList.toArray(new Program[resultList.size()]);
  }


  private void markProgram(Program p) {
    if(!mBlackList.contains(p)) {
      p.mark(FavoritesPluginProxy.getInstance());
      String[] reminderServices = getReminderConfiguration().getReminderServices();
      for (int i=0; i<reminderServices.length; i++) {
        if (ReminderConfiguration.REMINDER_DEFAULT.equals(reminderServices[i])) {
          ReminderPlugin.getInstance().addProgram(p);
        }
      }
    }
  }

  private void unmarkProgram(Program p) {
    if(!FavoriteTreeModel.getInstance().isContainedByOtherFavorites(this,p)) {
      p.unmark(FavoritesPluginProxy.getInstance());
    }
      
    String[] reminderServices = getReminderConfiguration().getReminderServices();
    for (int i=0; i<reminderServices.length; i++) {
      if (ReminderConfiguration.REMINDER_DEFAULT.equals(reminderServices[i])) {
        ReminderPlugin.getInstance().removeProgram(p);
      }
    }

  }
  
  /**
   * Checks if all programs on the black are valid.
   */
  public void refreshBlackList() {
    for(int i = mBlackList.size() - 1; i >= 0 ; i--) {
      Program programInList = mBlackList.remove(i);
      Program testProg = PluginManagerImpl.getInstance().getProgram(programInList.getDate(), programInList.getID());
    
      if(testProg != null && programInList.getTitle().toLowerCase().compareTo(testProg.getTitle().toLowerCase()) == 0) 
        mBlackList.add(testProg);
    }
  }
  
  /**
   * Checks if a program is on the black list.
   * 
   * @param program The program to check.
   * @return If the program in on the black list.
   */
  public boolean isOnBlackList(Program program) {
    return mBlackList.contains(program);
  }
  
  /**
   * Add a program to the black list
   * 
   * @param program The program to put on the black list.
   */
  public void addToBlackList(Program program) {
    if(!mBlackList.contains(program)) {
      mBlackList.add(program);
      unmarkProgram(program);
      FavoritesPlugin.getInstance().updateRootNode(true);
      
      if(ManageFavoritesDialog.getInstance() != null)
        ManageFavoritesDialog.getInstance().favoriteSelectionChanged();
    }
  }
  
  /**
   * Removes the program from the black list,
   * if it is in it.
   * 
   * @param program The program to remove from the black list.
   */
  public void removeFromBlackList(Program program) {
    if(mBlackList.remove(program)) {
      markProgram(program);
      FavoritesPlugin.getInstance().updateRootNode(true);
      
      if(ManageFavoritesDialog.getInstance() != null)
        ManageFavoritesDialog.getInstance().favoriteSelectionChanged();
    }
  }
  
  /**
   * @return The programs that are not on the black list.
   */
  public Program[] getWhiteListPrograms() {
    ArrayList<Program> tempProgramArr = new ArrayList<Program>();
    
    for(int i = 0; i < mPrograms.length; i++) {
      if(!mBlackList.contains(mPrograms[i]))
        tempProgramArr.add(mPrograms[i]);
    }
        
    return tempProgramArr.toArray(new Program[tempProgramArr.size()]);
  }
  
  /**
   * 
   * @return The programs that are on the black list.
   */
  public Program[] getBlackListPrograms() {
    return mBlackList.toArray(new Program[mBlackList.size()]);
  }

  public abstract FavoriteConfigurator createConfigurator();

  protected abstract void internalWriteData(ObjectOutputStream out) throws IOException;

  protected abstract Program[] internalSearchForPrograms(Channel[] channelArr) throws TvBrowserException;

}
