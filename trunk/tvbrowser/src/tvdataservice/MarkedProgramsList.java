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
package tvdataservice;

import java.util.ArrayList;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import util.program.ProgramUtilities;

import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * A class that contains all marked programs.
 * 
 * @author René Mach
 * @since 2.2
 */
public class MarkedProgramsList {

  private static MarkedProgramsList mInstance;
  private ArrayList<MutableProgram> mList;

  private MarkedProgramsList() {
    mList = new ArrayList<MutableProgram>();
    mInstance = this;
  }

  /**
   * @return The instance of this class.
   */
  public static synchronized MarkedProgramsList getInstance() {
    if(mInstance == null)
      new MarkedProgramsList();
    return mInstance;
  }

  protected void addProgram(MutableProgram p) {
    if(p!= null && !mList.contains(p) && p.getMarkerArr().length > 0)
      mList.add(p);
  }

  protected void removeProgram(MutableProgram p) {
    if(p!= null && p.getMarkerArr().length < 1)
      mList.remove(p);
  }

  /**
   * @return The marked programs.
   */
  public Program[] getMarkedPrograms() {
    Program[] p = new Program[mList.size()];
    mList.toArray(p);

    return p;
  }

  /**
   * @param filter The filter to use for program filtering
   * @return The time sorted programs for the tray.
   */
  public Program[] getTimeSortedProgramsForTray(ProgramFilter filter) {
    int n = mList.size() > Settings.propTrayImportantProgramsSize.getInt() ? Settings.propTrayImportantProgramsSize.getInt() : mList.size();

    ArrayList<Program> programs = new ArrayList<Program>();

    int k = 0;
    int i = 0;

    while(i < n) {
      if(k >= mList.size())
        break;

      MutableProgram p = mList.get(k);
      if(ProgramUtilities.isOnAir(p) || p.isExpired() || !filter.accept(p)) {
        k++;
        continue;
      }
      long value1 = (p.getDate().getValue() - Date.getCurrentDate().getValue()) * 24 * 60 + p.getStartTime();
      boolean found = false;

      for(int j = 0; j < programs.size(); j++) {
        Program p1 = programs.get(j);
        long value2 = (p1.getDate().getValue() - Date.getCurrentDate().getValue()) * 24 * 60 + p1.getStartTime();

        if(value2 > value1) {
          programs.add(j,p);
          found = true;
          break;
        }
      }

      if(!found && filter.accept(p))
        programs.add(p);

      k++;
      i++;
    }

    for(i = k; i < mList.size(); i++) {
      Program p = mList.get(i);

      if(ProgramUtilities.isOnAir(p) || p.isExpired())
        continue;

      long valueNew = (p.getDate().getValue() - Date.getCurrentDate().getValue()) * 24 * 60 + p.getStartTime();
      Program p1 = programs.get(programs.size() - 1);

      long valueOld = (p1.getDate().getValue() - Date.getCurrentDate().getValue()) * 24 * 60 + p1.getStartTime();
      if(valueOld > valueNew)
        for(int j = 0; j < programs.size(); j++) {
          p1 = programs.get(j);
          valueOld = (p1.getDate().getValue() - Date.getCurrentDate().getValue()) * 24 * 60 + p1.getStartTime();

          if(valueOld > valueNew) {
            programs.add(j,p);
            break;
          }
        }

      if(programs.size() > Settings.propTrayImportantProgramsSize.getInt())
        programs.remove(Settings.propTrayImportantProgramsSize.getInt());
    }

    Program[] trayPrograms = new Program[programs.size()];
    programs.toArray(trayPrograms);


    return trayPrograms;
  }

  /**
   * Revalidate program markings
   */
  public void revalidatePrograms() {
    synchronized(mList) {
      MutableProgram[] programs = mList.toArray(new MutableProgram[mList.size()]);
      mList.clear();

      for(MutableProgram programInList : programs) {
        MutableProgram testProg = (MutableProgram)PluginManagerImpl.getInstance().getProgram(programInList.getDate(), programInList.getID());

        if(testProg == null || programInList.getTitle().toLowerCase().compareTo(testProg.getTitle().toLowerCase()) != 0) {
          programInList.setMarkerArr(MutableProgram.EMPTY_MARKER_ARR);
          programInList.setProgramState(Program.WAS_DELETED_STATE);
        }
        else if(testProg != programInList) {
          testProg.setMarkerArr(programInList.getMarkerArr());
          testProg.setMarkPriority(programInList.getMarkPriority());
          programInList.setMarkerArr(MutableProgram.EMPTY_MARKER_ARR);
          programInList.setMarkPriority(-1);
          programInList.setProgramState(Program.WAS_UPDATED_STATE);
          mList.add(testProg);
        }
        else
          mList.add(programInList);
      }
    }
  }
}
