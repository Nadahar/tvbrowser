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

import devplugin.Program;
import devplugin.ProgramSearcher;
import devplugin.Plugin;
import devplugin.Date;

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import util.exc.TvBrowserException;
import util.ui.SearchFormSettings;
import util.program.ProgramUtilities;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.extras.favoritesplugin.FavoriteConfigurator;
import tvbrowser.extras.common.ReminderConfiguration;
import tvbrowser.extras.common.LimitationConfiguration;
import tvbrowser.extras.reminderplugin.ReminderPlugin;

public abstract class Favorite {

  private Program[] mPrograms;
  private String mName;
  private ReminderConfiguration mReminderConfiguration;
  private LimitationConfiguration mLimitationConfiguration;
  private boolean mRemindAfterDownload;
  private ArrayList mExclusionList;

  public Favorite() {
    mReminderConfiguration = new ReminderConfiguration();
    mLimitationConfiguration = new LimitationConfiguration();
    mPrograms = new Program[]{};
    mExclusionList = new ArrayList();
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


  public Program[] getPrograms() {
    return mPrograms;
  }



  public abstract SearchFormSettings getSearchFormSettings();


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

    // Don't save the programs but only their date and id
    out.writeInt(mPrograms.length);
    for (int i = 0; i < mPrograms.length; i++) {
      mPrograms[i].getDate().writeData(out);
      out.writeObject(mPrograms[i].getID());
    }

    _writeData(out);
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

  /**
   * Performs a new search, and refreshes the program marks
   * @throws TvBrowserException
   */
  public void updatePrograms() throws TvBrowserException {

    SearchFormSettings searchForm = getSearchFormSettings();

    ProgramSearcher searcher = searchForm.createSearcher();
    Program[] progs = searcher.search(searchForm.getFieldTypes(),
                                                new devplugin.Date(),
                                                1000,
                                                Plugin.getPluginManager().getSubscribedChannels(),
                                                true
                                                );

    Exclusion[] exclusions = getExclusions();
    ArrayList list = new ArrayList();

    for (int i=0; i<progs.length; i++) {
      boolean isExcluded = false;
      for (int j=0; j<exclusions.length; j++) {
        if (exclusions[j].isProgramExcluded(progs[i])) {
          isExcluded = true;
          break;
        }
      }
      if (!isExcluded) {
        list.add(progs[i]);
      }
    }

    /* Now we have to lists:
         - mPrograms:   previous favorite programs
         - newProgList: new favorite programs

       We walk through the lists and remove or add the programs:
       For all programs from mPrograms, that don't exist in newProgList, we REMOVE it from new ProgList
       For all programs from newProgList, that don't exist in mPrograms, we ADD it to mPrograms

     */

    Comparator comparator = ProgramUtilities.getProgramComparator();
    Program[] newProgList = (Program[])list.toArray(new Program[list.size()]);
    Arrays.sort(newProgList, comparator);
    Arrays.sort(mPrograms, comparator);

    Program[] p1 = mPrograms;
    Program[] p2 = newProgList;

    ArrayList resultList = new ArrayList();

    int inx1 = 0;
    int inx2 = 0;
    while (inx1 < p1.length && inx2 < p2.length) {
      if (comparator.compare(p1[inx1], p2[inx2]) < 0) {
        // remove p1[inx1]
        p1[inx1].unmark(FavoritesPlugin.MARKER);
        ReminderPlugin.getInstance().removeProgram(p1[inx1]);
        inx1++;
      }
      else if (comparator.compare(p1[inx1], p2[inx2]) > 0) {
        // add (p2[inx2]
        p2[inx2].mark(FavoritesPlugin.MARKER);
        ReminderPlugin.getInstance().addProgram(p2[inx2]);
        resultList.add(p2[inx2]);
        inx2++;
      }
      else {
        // leave
        resultList.add(p2[inx2]);
        inx1++;
        inx2++;
      }
    }

    if (inx2 < p2.length) {
      // add (p2[inx2]..p2[p2.length-1])
      for (int i=inx2; i<p2.length; i++) {
        p2[i].mark(FavoritesPlugin.MARKER);
        ReminderPlugin.getInstance().addProgram(p2[i]);
        resultList.add(p2[i]);
      }
    }
    if (inx1 < p1.length) {
      // remove (p1[inx1]..p1[p1.length-1])
      for (int i=inx1; i<p1.length; i++) {
        p1[i].unmark(FavoritesPlugin.MARKER);
        ReminderPlugin.getInstance().removeProgram(p1[i]);
      }
    }

    mPrograms = (Program[])resultList.toArray(new Program[list.size()]);
  }

  public abstract FavoriteConfigurator createConfigurator();

  protected abstract void _writeData(ObjectOutputStream out) throws IOException;



}
