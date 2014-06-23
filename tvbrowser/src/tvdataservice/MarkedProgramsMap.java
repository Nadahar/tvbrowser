/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import util.program.ProgramUtilities;
import devplugin.Marker;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * A class that contains all markings for programs.
 * 
 * @author René Mach
 * @since 3.3.4
 */
public class MarkedProgramsMap {
  private static MarkedProgramsMap mInstance;
  
  private final Map<String, MarkedHolder> mMarkedMap = Collections.synchronizedMap(new 
      HashMap<String, MarkedHolder>()); 
  
  private Thread mProgramTableRefreshThread;

  private int mProgramTableRefreshThreadWaitTime;
  
  private MarkedProgramsMap() {}
  
  /**
   * @return The instance of this class.
   */
  public static MarkedProgramsMap getInstance() {
    if(mInstance == null) {
      mInstance = new MarkedProgramsMap();
    }
    
    return mInstance;
  }
  
  synchronized void setMarkerForProgram(Program p, Marker[] markerArr) {
    synchronized (mMarkedMap) {
      MarkedHolder markedHolder = mMarkedMap.get(p.getUniqueID());
      
      if(markedHolder == null) {
        markedHolder = new MarkedHolder(p);
        
        mMarkedMap.put(p.getUniqueID(), markedHolder);
      }
      
      markedHolder.setMarkerArr(markerArr);
      
      handleFilterMarking(p);
    }
  }
  
  synchronized void addMarkerForProgram(Program p, Marker marker) {
    synchronized (mMarkedMap) {
      MarkedHolder markedHolder = mMarkedMap.get(p.getUniqueID());
      
      if(markedHolder == null) {
        markedHolder = new MarkedHolder(p);
        
        mMarkedMap.put(p.getUniqueID(), markedHolder);
      }
      
      markedHolder.addMarker(marker);
      
      handleFilterMarking(p);
    }
  }
  
  synchronized void removeMarkerForProgram(Program p, Marker marker) {
    synchronized (mMarkedMap) {
      MarkedHolder markedHolder = mMarkedMap.get(p.getUniqueID());
      
      if(markedHolder != null) {
        boolean removed = markedHolder.removeMarker(marker);
        
        if(removed) {
          mMarkedMap.remove(p.getUniqueID());
        }
        
        handleFilterMarking(p);
      }
    }
  }
  
  Marker[] getMarkerForProgram(Program p) {
    synchronized (mMarkedMap) {
      MarkedHolder markedHolder = mMarkedMap.get(p.getUniqueID());
      
      if(markedHolder != null) {
        return markedHolder.getMarkerArr();
      }
    }
    
    return MutableProgram.EMPTY_MARKER_ARR;
  }
  
  void setMarkPriorityForProgram(Program p, int markPriority) {
    synchronized (mMarkedMap) {
      MarkedHolder markedHolder = mMarkedMap.get(p.getUniqueID());
      
      if(markedHolder != null) {
        markedHolder.setMarkPriority(markPriority);
      }
    }    
  }
  
  int getMarkPriorityForProgram(Program p) {
    synchronized (mMarkedMap) {
      MarkedHolder markedHolder = mMarkedMap.get(p.getUniqueID());
      
      if(markedHolder != null) {
        return markedHolder.getMarkPriority();
      }
    }
    
    return Program.NO_MARK_PRIORITY;
  }
  
  boolean validateMarkingForProgram(Program p) {
    boolean validated = false;
    
    synchronized (mMarkedMap) {
      MarkedHolder markedHolder = mMarkedMap.get(p.getUniqueID());
      
      if(markedHolder != null) {
        markedHolder.validateMarking();
        validated = true;
      }
    }
    
    return validated;
  }
  
  public void revalidatePrograms() {
    synchronized(mMarkedMap) {
      Iterator<MarkedHolder> markedHolderIterator =  mMarkedMap.values().iterator();
      
      ArrayList<String> toRemoveKeys = new ArrayList<String>();
      
      while(markedHolderIterator.hasNext()) {
        MarkedHolder holder = markedHolderIterator.next();
        
        if(!holder.validate()) {
          toRemoveKeys.add(holder.getCurrentProgramInstance().getUniqueID());
        }
      }
      
      for(String key : toRemoveKeys) {
        mMarkedMap.remove(key);
      }
    }
  }
  
  public void validateMarkings() {
    synchronized (mMarkedMap) {
      for(String key : mMarkedMap.keySet()) {
        MarkedHolder marked = mMarkedMap.get(key);
        
        if(marked != null) {
          marked.validateMarking();
          
          if(marked.getCurrentProgramInstance() != null) {
            ((MutableProgram)marked.getCurrentProgramInstance()).fireStateChanged();
          }
        }
      }
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
  

  /**
   * @return All marked programs.
   */
  public Program[] getMarkedPrograms() {
    Program[] p = null;

    synchronized(mMarkedMap) {
      Iterator<MarkedHolder> markedHolderIterator =  mMarkedMap.values().iterator();
      
      ArrayList<Program> markedList = new ArrayList<Program>();
      
      while(markedHolderIterator.hasNext()) {
        markedList.add(markedHolderIterator.next().getCurrentProgramInstance());
      }
      
      p = new Program[markedList.size()];
      markedList.toArray(p);
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
    List<Program> programs = new ArrayList<Program>();
    
    synchronized(mMarkedMap) {
      Iterator<MarkedHolder> markedHolderIterator =  mMarkedMap.values().iterator();
              
      while(markedHolderIterator.hasNext()) {
        Program p = markedHolderIterator.next().getCurrentProgramInstance();
        
        boolean dontAccept = !filter.accept(p);

        if(dontAccept && useTrayFilterSettings) {
          dontAccept = !(Settings.propTrayFilterNot.getBoolean() || (Settings.propTrayFilterNotMarked.getBoolean() && p.getMarkerArr().length > 0));
        }

        if((p.isOnAir() && !includeOnAirPrograms) || p.isExpired() || dontAccept || p.getMarkPriority() < markPriority) {
          continue;
        }
        programs.add(p);
      }
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
  
  private static final class MarkedHolder {
    private byte mMarkPriority;
    private Program mCurrentProgramInstance;
    private Marker[] mMarkerArr;
    
    MarkedHolder(Program prog) {
      mCurrentProgramInstance = prog;
      mMarkPriority = Program.NO_MARK_PRIORITY;
      mMarkerArr = MutableProgram.EMPTY_MARKER_ARR;
    }
    
    synchronized void addMarker(Marker marker) {
      for(Marker test : mMarkerArr) {
        if(test != null && test.getId().equals(marker.getId())) {
          return;
        }
      }
      
      int oldCount = mMarkerArr.length;
      
      Marker[] newArr = new Marker[oldCount + 1];
      System.arraycopy(mMarkerArr, 0, newArr, 0, oldCount);
      newArr[oldCount] = marker;
      mMarkerArr = newArr;

      Arrays.sort(mMarkerArr,new Comparator<Marker>() {
        public int compare(Marker o1, Marker o2) {
          return o1.getId().compareTo(o2.getId());
        }
      });

      mMarkPriority = (byte) Math.max(mMarkPriority,marker.getMarkPriorityForProgram(mCurrentProgramInstance));
    //  
   //   Marker[] newArr = new Marker[mMarkerArr.length + 1];
   //   newArr[newArr.length - 1] = marker;
      
  //    System.arraycopy(mMarkerArr, 0, newArr, 0, mMarkerArr.length);
      
      // add program to artificial plugin tree
      if (marker instanceof PluginProxy) {
        PluginProxy proxy = (PluginProxy) marker;
        if (! proxy.canUseProgramTree() || proxy.hasArtificialPluginTree() ) {
          if (proxy.getArtificialRootNode() == null || proxy.getArtificialRootNode().size() < 100) {
            proxy.addToArtificialPluginTree((MutableProgram)mCurrentProgramInstance);
          }
        }
      }
      
    //  mMarkerArr = newArr;
      
  //    mMarkPriority = Math.max(mMarkPriority, marker.getMarkPriorityForProgram(mCurrentProgramInstance));
    }
    
    synchronized boolean removeMarker(Marker marker) {
      ArrayList<Marker> newArray = new ArrayList<Marker>();
      mMarkPriority = Program.NO_MARK_PRIORITY;
      
      for(Marker test : mMarkerArr) {
        if(test != null && !test.getId().equals(marker.getId())) {
          newArray.add(test);
          mMarkPriority = (byte)Math.max(mMarkPriority, test.getMarkPriorityForProgram(mCurrentProgramInstance));
          
          // remove from artificial plugin tree
          if (marker instanceof PluginProxy) {
            PluginProxy proxy = (PluginProxy) marker;
            if (proxy.hasArtificialPluginTree() && proxy.getArtificialRootNode().size() < 100) {
              proxy.getArtificialRootNode().removeProgram(mCurrentProgramInstance);
            }
          }
        }
      }
      
      mMarkerArr = newArray.toArray(new Marker[newArray.size()]);
      
      return isEmpty();
    }
    
    synchronized void setMarkerArr(Marker[] marker) {
      mMarkerArr = marker;
      
      mMarkPriority = Program.NO_MARK_PRIORITY;
      
      for(Marker test : marker) {
        if(test != null) {
          mMarkPriority = (byte)Math.max(mMarkPriority, test.getMarkPriorityForProgram(mCurrentProgramInstance));
        }
      }
    }
    
    synchronized void setMarkPriority(int markPriority) {
      mMarkPriority = (byte)markPriority;
    }
    
    int getMarkPriority() {
      return mMarkPriority;
    }
    
    Marker[] getMarkerArr() {
      return mMarkerArr;
    }
    
    boolean isEmpty() {
      return mMarkerArr == null || mMarkerArr.length == 0;
    }
    
    synchronized void validateMarking() {
      mMarkPriority = Program.NO_MARK_PRIORITY;

      for(Marker mark : mMarkerArr) {
        if(mark != null) {
          mMarkPriority = (byte) Math.max(mMarkPriority,mark.getMarkPriorityForProgram(mCurrentProgramInstance));
        }
      }
    }
    
    Program getCurrentProgramInstance() {
      return mCurrentProgramInstance;
    }
    
    synchronized boolean validate() {
      MutableProgram check = checkProgram((MutableProgram)mCurrentProgramInstance,PluginManagerImpl.getInstance().getPrograms(mCurrentProgramInstance.getDate(), mCurrentProgramInstance.getID()));
      
      if(check != null) {
        mCurrentProgramInstance = check;
        return true;
      }
      
      return false;
    }
    
    boolean isValid() {
      return mCurrentProgramInstance.getProgramState() == Program.IS_VALID_STATE;
    }
    
    private MutableProgram checkProgram(MutableProgram programInList, Program[] testProgs) {
      boolean titleWasChangedToMuch = false;
      MutableProgram testProg = null;
      
      if(testProgs != null && testProgs.length > 0) {
        testProg = (MutableProgram)testProgs[0];
        
        if(testProgs.length > 1) {
          for(Program prog : testProgs) {
            String[] titleParts = programInList.getTitle().toLowerCase().replaceAll("\\p{Punct}"," ").replaceAll("\\s+"," ").split(" ");
            String compareTitle = prog.getTitle().toLowerCase();
            
            boolean found = true;
            
            for(String titlePart : titleParts) {
              if(compareTitle.indexOf(titlePart) == -1) {
                found = false;
                break;
              }
            }
            
            if(found) {
              testProg = (MutableProgram)prog;
              break;
            }
          }
        }
      }

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
        //programInList.setMarkerArr(MutableProgram.EMPTY_MARKER_ARR);
        programInList.setProgramState(Program.WAS_DELETED_STATE);
      }
      else if(testProg != programInList) {
      /*  Marker[] testMarkerArr = testProg.getMarkerArr();
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
        programInList.setMarkPriority(-1);*/
        programInList.setProgramState(Program.WAS_UPDATED_STATE);
        return testProg;
      } else {
        return programInList;
      }
      
      return null;
    }
  }
}
