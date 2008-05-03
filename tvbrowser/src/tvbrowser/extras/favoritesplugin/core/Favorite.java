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
import devplugin.ProgramSearcher;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesDialog;
import tvbrowser.extras.reminderplugin.ReminderPlugin;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.program.ProgramUtilities;
import util.ui.SearchFormSettings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gdata.client.http.GoogleGDataRequest;

public abstract class Favorite {

  private ArrayList<Program> mPrograms;
  private ArrayList<Program> mNewPrograms;
  private String mName;
  private ReminderConfiguration mReminderConfiguration;
  private LimitationConfiguration mLimitationConfiguration;
  private boolean mRemindAfterDownload;
  private ArrayList<Exclusion> mExclusionList;
  private ProgramReceiveTarget[] mForwardPluginArr;
  protected SearchFormSettings mSearchFormSettings;

  /**
   * unsorted list of blacklisted (non-favorite) programs
   */
  private ArrayList<Program> mBlackList;
  
  public Favorite() {
    mReminderConfiguration = new ReminderConfiguration(FavoritesPlugin.getInstance().isAutoSelectingRemider() ? new String[] {ReminderConfiguration.REMINDER_DEFAULT} : new String[0]);
    mLimitationConfiguration = new LimitationConfiguration();
    mPrograms = new ArrayList<Program>();
    mNewPrograms = new ArrayList<Program>();
    mExclusionList = null; // defer initialisation until needed, save memory
    mBlackList = null; // defer initialization until needed

    mForwardPluginArr = FavoritesPlugin.getInstance().getDefaultClientPluginsTargets();
  }

  public Favorite(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();  // version
    mName = (String)in.readObject();
    mReminderConfiguration = new ReminderConfiguration(in);
    mLimitationConfiguration = new LimitationConfiguration(in);
    mRemindAfterDownload = in.readBoolean();

    int exclSize = in.readInt();
    if (exclSize > 0) {
      mExclusionList = new ArrayList<Exclusion>();
    }
    for (int i=0; i<exclSize; i++) {
      Exclusion exclusion = new Exclusion(in);
      
      if(!exclusion.isInvalid()) {
        mExclusionList.add(exclusion);
      }
    }

    int cnt = in.readInt();
    mForwardPluginArr = new ProgramReceiveTarget[cnt];

    for (int i=0; i<cnt; i++) {
      if(version <=2) {
        String id = (String)in.readObject();
        mForwardPluginArr[i] = ProgramReceiveTarget.createDefaultTargetForProgramReceiveIfId(id);
      } else {
        mForwardPluginArr[i] = new ProgramReceiveTarget(in);
      }
    }
        
    // Don't save the programs but only their date and id
    int size = in.readInt();
    ArrayList<Program> programList = new ArrayList<Program>(size);
    readProgramsToList(programList, size, in);
    
    if(version >= 2) {
      size = in.readInt();
      if (size > 0) {
        mBlackList = new ArrayList<Program>(size);
        readProgramsToList(mBlackList, size, in);
      }
    }
    else {
      mBlackList = null;
    }
    if (mBlackList != null) {
      Collections.sort(mBlackList, ProgramUtilities.getProgramComparator());
    }
    
    mPrograms = programList;
    
    mNewPrograms = new ArrayList<Program>();
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
    return mPrograms.toArray(new Program[mPrograms.size()]);
  }

  public Program[] getNewPrograms() {
    return mNewPrograms.toArray(new Program[mNewPrograms.size()]);
  }


  public void handleContainingPrograms(Program[] progs) {
    for (Program p : progs) {
      if (mPrograms.contains(p)) {
        p.mark(FavoritesPluginProxy.getInstance());
      }
    }
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(3);  // version
    out.writeObject(mName);
    mReminderConfiguration.store(out);
    mLimitationConfiguration.store(out);
    out.writeBoolean(mRemindAfterDownload);

    if (mExclusionList == null) {
      out.writeInt(0);
    }
    else {
      out.writeInt(mExclusionList.size());
      for (int i=0; i<mExclusionList.size(); i++) {
        (mExclusionList.get(i)).writeData(out);
      }
    }

    out.writeInt(mForwardPluginArr.length);
    for (int i=0; i<mForwardPluginArr.length; i++) {
      mForwardPluginArr[i].writeData(out);
    }

    // Don't save the programs but only their date and id
    out.writeInt(mPrograms.size());
    for (Program p : mPrograms) {
      p.getDate().writeData(out);
      out.writeObject(p.getID());
    }

    // Save the programs on BlackList
    if (mBlackList == null) {
      out.writeInt(0);
    }
    else {
      out.writeInt(mBlackList.size());
      for(int i = 0; i < mBlackList.size(); i++) {
        Program p = mBlackList.get(i);
        p.getDate().writeData(out);
        out.writeObject(p.getID());
      }
    }

    internalWriteData(out);
  }



  public Exclusion[] getExclusions() {
    if (mExclusionList == null) {
      return new Exclusion[0];
    }
    return mExclusionList.toArray(new Exclusion[mExclusionList.size()]);
  }

  public void addExclusion(Exclusion exclusion) {
    if (mExclusionList == null) {
      mExclusionList = new ArrayList<Exclusion>(1);
    }
    mExclusionList.add(exclusion);
    try {
      refreshPrograms();
      FavoritesPlugin.getInstance().updateRootNode(true);
    } catch (TvBrowserException exc) {
      ErrorHandler.handle("Could not update favorites.", exc);
    }
    updateManageDialog();
  }

  public void removeExclusion(Exclusion exclusion) {
    if (mExclusionList == null) {
      return;
    }
    mExclusionList.remove(exclusion);
  }

  public void setExclusions(Exclusion[] exclusionArr) {
    if (mExclusionList == null) {
      mExclusionList = new ArrayList<Exclusion>(exclusionArr.length);
    }
    else {
      mExclusionList.clear();
    }
    for (int i=0; i<exclusionArr.length; i++) {
      mExclusionList.add(exclusionArr[i]);
    }
  }


  public boolean contains(Program prog) {
    if(mBlackList != null && mBlackList.contains(prog)) {
      return false;
    }
    return mPrograms.contains(prog);
  }


  private Program[] filterByLimitations(Program[] progArr) {
    Exclusion[] globalExclusions = FavoritesPlugin.getInstance().getGlobalExclusions();
    Exclusion[] exclusions = new Exclusion[getExclusions().length + globalExclusions.length];
    
    System.arraycopy(globalExclusions,0,exclusions,0,globalExclusions.length);
    System.arraycopy(getExclusions(),0,exclusions,globalExclusions.length,exclusions.length - globalExclusions.length);    
    
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
        if (ProgramUtilities.isNotInTimeRange(getLimitationConfiguration().getTimeFrom(),getLimitationConfiguration().getTimeTo(),progArr[i])) {
          isExcluded = true;
        } else {
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
   * Search in a list of Programs to find new Items
   *
   * @param added list of programs to check
   * @param removed list of removed programs
   * @param dataUpdate is a data update running at the moment
   * @param sendToPlugins send data to otheer plugins
   * @throws util.exc.TvBrowserException Problems during search
   * @since 2.7
   */
  public void searchNewPrograms(Program[] added, Program[] removed, boolean dataUpdate, boolean sendToPlugins) throws TvBrowserException {
    SearchFormSettings searchForm = mSearchFormSettings;

    final ArrayList<Program> currentList = new ArrayList<Program>(mPrograms);

    final ProgramSearcher searcher = searchForm.createSearcher();

    for (Program p:added) {
      if (searcher.matches(p, searchForm.getFieldTypes()) && !currentList.contains(p)) {
        currentList.add(p);
      }
    }

    for (Program p:removed) {
      if (currentList.contains(p)) {
        currentList.remove(p);
      }
    }


    updatePrograms(currentList.toArray(new Program[currentList.size()]), dataUpdate, sendToPlugins);
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
   * @param sendToPlugins If the new found programs should be send to plugins.
   * @throws TvBrowserException
   */
  public void updatePrograms(boolean dataUpdate, boolean sendToPlugins) throws TvBrowserException {
    updatePrograms(internalSearchForPrograms(), dataUpdate, sendToPlugins);
  }
  
  /**
   * Checks all current programs if they are not excluded,
   * and refreshes the program marks.
   * @throws TvBrowserException
   */
  public void refreshPrograms() throws TvBrowserException {
    updatePrograms(mPrograms.toArray(new Program[mPrograms.size()]), false, false);
  }
  
  private void updatePrograms(Program[] progs, boolean dataUpdate, boolean send) throws TvBrowserException {
    Program[] newProgList = filterByLimitations(progs);


    /* Now we have two lists:
         - mPrograms:   previous favorite programs
         - newProgList: new favorite programs

       We walk through the lists and remove or add the programs:
       For all programs from mPrograms, that don't exist in newProgList, we REMOVE it from new ProgList
       For all programs from newProgList, that don't exist in mPrograms, we ADD it to mPrograms

     */
    Comparator<Program> comparator = ProgramUtilities.getProgramComparator();
    Arrays.sort(newProgList, comparator);

    Program[] p1 = mPrograms.toArray(new Program[mPrograms.size()]);
    Arrays.sort(p1, comparator);

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
         * of the program and mark it if it is so. */
        if(p1[inx1].getProgramState() == Program.WAS_DELETED_STATE) {
          markProgram(p2[inx2]);
        }
          
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
    mNewPrograms = newPrograms;
    ProgramReceiveTarget[] pluginArr = getForwardPlugins();
    
    if(mNewPrograms.size() > 0 && send) {
      if(!dataUpdate) {
        for (int i=0; i<pluginArr.length; i++) {
          if(pluginArr[i] != null && pluginArr[i].getReceifeIfForIdOfTarget() != null) {
            pluginArr[i].getReceifeIfForIdOfTarget().receivePrograms(mNewPrograms.toArray(new Program[mNewPrograms.size()]),pluginArr[i]);
          }
        }
      } else {
        FavoritesPlugin.getInstance().addProgramsForSending(pluginArr, mNewPrograms.toArray(new Program[mNewPrograms.size()]));
      }
    }

    mPrograms = resultList;
  }


  private void markProgram(Program p) {
    if(mBlackList == null || !mBlackList.contains(p)) {
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
   * Checks if all programs on the blacklist are valid.
   */
  public void refreshBlackList() {
    if (mBlackList == null) {
      return;
    }
    for(int i = mBlackList.size() - 1; i >= 0 ; i--) {
      Program programInList = mBlackList.remove(i);
      Program testProg = PluginManagerImpl.getInstance().getProgram(programInList.getDate(), programInList.getID());
    
      if(testProg != null && programInList.getTitle().toLowerCase().compareTo(testProg.getTitle().toLowerCase()) == 0) {
        mBlackList.add(testProg);
      }
    }
  }
  
  /**
   * Checks if a program is on the blacklist.
   * 
   * @param program The program to check.
   * @return If the program in on the blacklist.
   */
  public boolean isOnBlackList(Program program) {
    if (mBlackList == null) {
      return false;
    }
    return mBlackList.contains(program);
  }
  
  /**
   * Add a program to the blacklist
   * 
   * @param program The program to put on the blacklist.
   */
  public void addToBlackList(Program program) {
    if (mBlackList == null) {
      mBlackList = new ArrayList<Program>(1);
    }
    if(!mBlackList.contains(program)) {
      mBlackList.add(program);
      Collections.sort(mBlackList, ProgramUtilities.getProgramComparator());
      unmarkProgram(program);
      FavoritesPlugin.getInstance().updateRootNode(true);
      updateManageDialog();
    }
  }
  
  /**
   * Removes the program from the blacklist,
   * if it is in it.
   * 
   * @param program The program to remove from the blacklist.
   */
  public void removeFromBlackList(Program program) {
    if (mBlackList == null) {
      return;
    }
    if(mBlackList.remove(program)) {
      markProgram(program);
      FavoritesPlugin.getInstance().updateRootNode(true);
      updateManageDialog();
    }
  }

  private void updateManageDialog() {
    if(ManageFavoritesDialog.getInstance() != null) {
      ManageFavoritesDialog.getInstance().favoriteSelectionChanged();
    }
  }
  
  /**
  * @return The programs that are not on the blacklist.
  */
  public Program[] getWhiteListPrograms() {
    return getWhiteListPrograms(false);
  }
  
  /**
   * @param onlyNotExpiredPrograms <code>true</code> if only not expired
   * programs should be returned, <code>false</code> otherwise.
   * @return The programs that are not on the blacklist.
   */
  public Program[] getWhiteListPrograms(boolean onlyNotExpiredPrograms) {
    ArrayList<Program> tempProgramArr = new ArrayList<Program>();

    for (final Program p : mPrograms) {
      if((mBlackList == null || !mBlackList.contains(p)) && (!onlyNotExpiredPrograms || !p.isExpired())) {
        tempProgramArr.add(p);
      }
    }

    return tempProgramArr.toArray(new Program[tempProgramArr.size()]);
  }
  
  /**
   * 
   * @return The programs that are on the blacklist.
   */
  public Program[] getBlackListPrograms() {
    if (mBlackList == null) {
      return new Program[0];
    }
    return mBlackList.toArray(new Program[mBlackList.size()]);
  }

  public abstract FavoriteConfigurator createConfigurator();

  protected abstract void internalWriteData(ObjectOutputStream out) throws IOException;

  protected Program[] internalSearchForPrograms() throws TvBrowserException {

    SearchFormSettings searchForm = mSearchFormSettings;

    ProgramSearcher searcher = searchForm.createSearcher();
    return searcher.search(searchForm.getFieldTypes(),
                                                new devplugin.Date().addDays(-1),
                                                1000,
                                                getChannels(),
                                                false
                                                );
  }

  /**
   * Gets if this Favorite contains the given receive target.
   * <p>
   * @param target The target to check for.
   * @return <code>True</code> if this Favorite contains
   * the given target, <code>false</code> otherwise.
   */
  public boolean containsReceiveTarget(ProgramReceiveTarget target) {
    if(mForwardPluginArr != null && target != null) {
      for(ProgramReceiveTarget tar : mForwardPluginArr) {
        if(tar.getReceiveIfId().equals(target.getReceiveIfId())&& tar.getTargetId().equals(target.getTargetId())) {
          return true;
        }
      }
    }
    
    return false;
  }

  /**
   * get the channels this favorite is searched on
   * @return channel array
   */
  protected Channel[] getChannels() {
    Channel[] channelArr;
    if (getLimitationConfiguration().isLimitedByChannel()) {
      channelArr = getLimitationConfiguration().getChannels();
    }
    else {
      channelArr = Plugin.getPluginManager().getSubscribedChannels();
    }
    return channelArr;
  }

  /**
   * Checks if this program matches the favorite.
   * if it does, it will be added to the favorite.
   *
   * @param p new Program
   * @param dataUpdate is this method called during a data update?
   * @param send should the program be sended to other plugins
   * @since 2.7
   */
  public void tryToMatch(Program p, boolean dataUpdate, boolean send) throws TvBrowserException {
    if (matches(p)) {

      // ToDo: Filter By Limitations
      mPrograms.add(p);
      mNewPrograms.add(p);
      markProgram(p);
      if (send) {
        ProgramReceiveTarget[] pluginArr = getForwardPlugins();
        if(!dataUpdate) {
          for (ProgramReceiveTarget receiveTarget : pluginArr) {
            if (receiveTarget != null && receiveTarget.getReceifeIfForIdOfTarget() != null) {
              receiveTarget.getReceifeIfForIdOfTarget().receivePrograms(mNewPrograms.toArray(new Program[mNewPrograms.size()]), receiveTarget);
            }
          }
        } else {
          FavoritesPlugin.getInstance().addProgramsForSending(pluginArr, mNewPrograms.toArray(new Program[mNewPrograms.size()]));
        }
      }
    }
  }

  /**
   * This function tries to match the given program with the favorite
   *
   * @param p check if this program matches
   * @return <code>true</code> if the program matches
   *
   * @throws TvBrowserException exception during matching
   */
  public boolean matches(Program p) throws TvBrowserException {
    SearchFormSettings searchForm = mSearchFormSettings;
    ProgramSearcher searcher = searchForm.createSearcher();
    return mSearchFormSettings.createSearcher().matches(p, searchForm.getFieldTypes());
  }

  /**
   * Checks if the program is marked by this favorite and remove it if it does.
   * @param p Program to remove
   * @since 2.7
   */
  public void removeProgram(Program p) {
    unmarkProgram(p);
  }

  /**
   * Clears the list of new programs
   * @since 2.7
   */
  public void clearNewPrograms() {
    mNewPrograms = new ArrayList<Program>();
  }

}
