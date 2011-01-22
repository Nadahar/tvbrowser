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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.program.ProgramUtilities;
import devplugin.Marker;
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
  private HashSet<MutableProgram> mMarkedPrograms;
  private Thread mProgramTableRefreshThread;
  private int mProgramTableRefreshThreadWaitTime;

  private MarkedProgramsList() {
    mMarkedPrograms = new HashSet<MutableProgram>();
    mInstance = this;
  }

  /**
   * @return The instance of this class.
   */
  public static synchronized MarkedProgramsList getInstance() {
    if(mInstance == null) {
      new MarkedProgramsList();
    }
    return mInstance;
  }

  private Thread getProgramTableRefreshThread() {
    mProgramTableRefreshThreadWaitTime = 500;
    return new Thread("Program table refresh") {
      public void run() {
        while(mProgramTableRefreshThreadWaitTime > 0) {
          try {
            sleep(100);
            mProgramTableRefreshThreadWaitTime -= 100;
          }catch(Exception e) {}
        }

        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            MainFrame.getInstance().getProgramTableModel().updateTableContent();
          }
        });
      }
    };
  }

  protected void addProgram(MutableProgram p) {
    if(p!= null && !contains(p) && p.getMarkerArr().length > 0) {
      synchronized(mMarkedPrograms) {
        mMarkedPrograms.add(p);
      }

      handleFilterMarking(p);
    }
  }

  private boolean contains(MutableProgram p) {
    if(p != null) {
      synchronized(mMarkedPrograms) {
        for(MutableProgram prog : mMarkedPrograms) {
          if(p.getDate().equals(prog.getDate()) && p.getID().equals(prog.getID())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  protected void removeProgram(MutableProgram p) {
    if(p!= null && p.getMarkerArr().length < 1) {
      synchronized(mMarkedPrograms) {
        mMarkedPrograms.remove(p);
      }

      handleFilterMarking(p);
    }
  }

  private void handleFilterMarking(Program p) {
    if(!MainFrame.isStarting() && !MainFrame.isShuttingDown() && PluginManagerImpl.getInstance().getFilterManager() != null && !PluginManagerImpl.getInstance().getFilterManager().getCurrentFilter().equals(PluginManagerImpl.getInstance().getFilterManager().getDefaultFilter())) {
      try {
        boolean contained = MainFrame.getInstance().getProgramTableModel().contains(p);
        boolean accepted = PluginManagerImpl.getInstance().getFilterManager().getCurrentFilter().accept(p);

        if((contained && !accepted) || (!contained && accepted)) {
          if(mProgramTableRefreshThread == null || !mProgramTableRefreshThread.isAlive()) {
            mProgramTableRefreshThread = getProgramTableRefreshThread();
            mProgramTableRefreshThread.start();
          }
          else {
            mProgramTableRefreshThreadWaitTime = 500;
          }
        }
      }catch(Exception e) {
        // ignore
      }
    }
  }

  /**
   * @return The marked programs.
   */
  public Program[] getMarkedPrograms() {
    Program[] p = null;

    synchronized(mMarkedPrograms) {
      p = new Program[mMarkedPrograms.size()];
      mMarkedPrograms.toArray(p);
    }

    return p;
  }

  /**
   * @param filter The filter to use for program filtering
   * @param markPriority The minimum mark priority of programs to find.
   * @param numberOfPrograms The number of programs to show. Use a value of 0 or below for all important programs.
   * @param includeOnAirPrograms If the marked programs array should contain running programs.
   * @return The time sorted programs for the tray.
   */
  public Program[] getTimeSortedProgramsForTray(ProgramFilter filter, int markPriority, int numberOfPrograms, boolean includeOnAirPrograms) {
    return getTimeSortedProgramsForTray(filter, markPriority, numberOfPrograms, includeOnAirPrograms, false, null);
  }

  /**
   * @param filter The filter to use for program filtering
   * @param markPriority The minimum mark priority of programs to find.
   * @param numberOfPrograms The number of programs to show. Use a value of 0 or below for all important programs.
   * @param includeOnAirPrograms If the marked programs array should contain running programs.If the tray filter settings should be used for filtering.
   * @param useTrayFilterSettings If the tray filter settings should be used for filtering.
   * @param excludePrograms
   * @return The time sorted programs for the tray.
   */
  public Program[] getTimeSortedProgramsForTray(ProgramFilter filter, int markPriority, int numberOfPrograms, boolean includeOnAirPrograms, boolean useTrayFilterSettings, ArrayList<Program> excludePrograms) {

    synchronized(mMarkedPrograms) {
      List<Program> programs = new ArrayList<Program>();

      Iterator<MutableProgram> it = mMarkedPrograms.iterator();
      while (it.hasNext()) {
        MutableProgram p = it.next();
        boolean dontAccept = !filter.accept(p);

        if(dontAccept && useTrayFilterSettings) {
          dontAccept = !(Settings.propTrayFilterNot.getBoolean() || (Settings.propTrayFilterNotMarked.getBoolean() && p.getMarkerArr().length > 0));
        }

        if((p.isOnAir() && !includeOnAirPrograms) || p.isExpired() || dontAccept || p.getMarkPriority() < markPriority) {
          continue;
        }
        programs.add(p);
      }

      if (excludePrograms != null) {
        programs.removeAll(excludePrograms);
      }

      Collections.sort(programs, ProgramUtilities.getProgramComparator());

      int maxCount = Math.min(programs.size(), Settings.propTrayImportantProgramsSize.getInt());
      if (numberOfPrograms > 0) {
        maxCount = Math.min(maxCount, numberOfPrograms);
      }
      programs = programs.subList(0, maxCount);
      Collections.sort(programs, ProgramUtilities.getProgramComparator()); // needed twice due to sublist not guaranteeing the order

      return programs.toArray(new Program[programs.size()]);
    }

  }

  /**
   * Revalidate program markings
   */
  public void revalidatePrograms() {
    synchronized(mMarkedPrograms) {
      MutableProgram[] programs = mMarkedPrograms.toArray(new MutableProgram[mMarkedPrograms.size()]);
      mMarkedPrograms.clear();

      for(MutableProgram programInList : programs) {
        MutableProgram testProg = (MutableProgram)PluginManagerImpl.getInstance().getProgram(programInList.getDate(), programInList.getID());

        boolean titleWasChangedToMuch = false;

        if(testProg != null && testProg.getTitle() != null && programInList.getTitle() != null
            && programInList.getTitle().toLowerCase().compareTo(testProg.getTitle().toLowerCase()) != 0) {
          String[] titleParts = programInList.getTitle().toLowerCase().replaceAll("\\p{Punct}"," ").replaceAll("\\s+"," ").split(" ");
          String compareTitle = testProg.getTitle().toLowerCase();

          for(String titlePart : titleParts) {
            if(compareTitle.indexOf(titlePart) == -1) {
              titleWasChangedToMuch = true;
              break;
            }
          }
        }

        if(testProg == null || titleWasChangedToMuch) {
          programInList.setMarkerArr(MutableProgram.EMPTY_MARKER_ARR);
          programInList.setProgramState(Program.WAS_DELETED_STATE);
        }
        else if(testProg != programInList) {
          Marker[] testMarkerArr = testProg.getMarkerArr();
          Marker[] currentMarkerArr = programInList.getMarkerArr();

          if(testMarkerArr == MutableProgram.EMPTY_MARKER_ARR) {
            testProg.setMarkerArr(currentMarkerArr);
            testProg.setMarkPriority(programInList.getMarkPriority());
          }
          else if(currentMarkerArr != MutableProgram.EMPTY_MARKER_ARR) {
            ArrayList<Marker> newMarkerList = new ArrayList<Marker>();

            for(Marker marker : testMarkerArr) {
              newMarkerList.add(marker);
            }

            for(Marker marker : currentMarkerArr) {
              if(!newMarkerList.contains(marker)) {
                newMarkerList.add(marker);
              }
            }

            java.util.Collections.sort(newMarkerList,new Comparator<Marker>() {
              public int compare(Marker o1, Marker o2) {
                return o1.getId().compareTo(o2.getId());
              }
            });

            testProg.setMarkerArr(newMarkerList.toArray(new Marker[newMarkerList.size()]));
            testProg.setMarkPriority(Math.max(testProg.getMarkPriority(),programInList.getMarkPriority()));
          }

          programInList.setMarkerArr(MutableProgram.EMPTY_MARKER_ARR);
          programInList.setMarkPriority(-1);
          programInList.setProgramState(Program.WAS_UPDATED_STATE);
          mMarkedPrograms.add(testProg);
        } else {
          mMarkedPrograms.add(programInList);
        }
      }
    }
  }
}
