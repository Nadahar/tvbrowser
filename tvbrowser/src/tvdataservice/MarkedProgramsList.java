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

import devplugin.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
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
  private HashSet<MutableProgram> mMarkedPrograms;
  private Thread mProgramTableRefreshThread;
  private int mProgramTableRefreshThreadWaitTime;
  private MarkerHashMap<String,Marker[]> mProgramMarkingsMap;
  private HashMap<String,Vector<ChangeListener>> mProgramChangeListenerMap;

  private MarkedProgramsList() {
    mMarkedPrograms = new HashSet<MutableProgram>();
    mProgramMarkingsMap = new MarkerHashMap<String,Marker[]>();
    mProgramChangeListenerMap = new HashMap<String,Vector<ChangeListener>>();
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

  protected synchronized void addProgram(MutableProgram p, Marker marker) {
    if(p!= null && !contains(p) &&  mProgramMarkingsMap.get(getIdFor(p)).length > 0) {
      mMarkedPrograms.add(p);
      mark(marker,p);
      handleFilterMarking(p);
    }
    else {
      mark(marker,p);
    }
  }
  
  private void mark(Marker marker, MutableProgram p) {
    boolean alreadyMarked = getMarkedByPluginIndex(marker,p) != -1;
    
    int oldCount = mProgramMarkingsMap.get(getIdFor(p)).length;

    if (! alreadyMarked) {
      // Append the new plugin
      Marker[] newArr = new Marker[oldCount + 1];
      System.arraycopy(mProgramMarkingsMap.get(getIdFor(p)), 0, newArr, 0, oldCount);
      newArr[oldCount] = marker;
      
      Arrays.sort(newArr,new Comparator<Marker>() {
        public int compare(Marker o1, Marker o2) {
          return o1.getId().compareTo(o2.getId());
        }
      });
      
      mProgramMarkingsMap.put(getIdFor(p),newArr);
      
      p.setMarkPriority(Math.max(p.getMarkPriority(),marker.getMarkPriorityForProgram(p)));
        
      // add program to artificial plugin tree
      if (marker instanceof PluginProxy) {
        PluginProxy proxy = (PluginProxy) marker;
        if (! proxy.canUseProgramTree() || proxy.hasArtificialPluginTree() ) {
          if (proxy.getArtificialRootNode() == null || proxy.getArtificialRootNode().size() < 100) {
            proxy.addToArtificialPluginTree(p);
          }
        }
      }
      
      p.fireStateChanged();
    }

    if(oldCount < 1) {
      p.cacheTitle();
    }
  }
  
  private void unmark(Marker marker, MutableProgram p) {
    int idx = getMarkedByPluginIndex(marker,p);
    if (idx != -1) {
      Marker[] markerArr = mProgramMarkingsMap.get(getIdFor(p));
      
      if (markerArr.length == 1) {
        // This was the only plugin
        mProgramMarkingsMap.remove(getIdFor(p));
        p.setMarkPriority(Program.NO_MARK_PRIORITY);
      }
      else {
        int oldCount = markerArr.length;
        Marker[] newArr = new Marker[oldCount - 1];
        System.arraycopy(markerArr, 0, newArr, 0, idx);
        System.arraycopy(markerArr, idx + 1, newArr, idx, oldCount - idx - 1);
        
        p.setMarkPriority(Program.NO_MARK_PRIORITY);
        
        for(Marker mark : newArr) {
          p.setMarkPriority(Math.max(p.getMarkPriority(),mark.getMarkPriorityForProgram(p)));
        }
        
        mProgramMarkingsMap.put(getIdFor(p), newArr);
      }

      // remove from artificial plugin tree
      if (marker instanceof PluginProxy) {
        PluginProxy proxy = (PluginProxy) marker;
        if (proxy.hasArtificialPluginTree() && proxy.getArtificialRootNode().size() < 100) {
          proxy.getArtificialRootNode().removeProgram(p);
        }
      }

      p.fireStateChanged();
      
      if(mProgramMarkingsMap.get(getIdFor(p)).length < 1) {
        p.clearTitleCache();
        removeProgram(p,marker);
      }
    }
  }
  
  private int getMarkedByPluginIndex(Marker plugin, Program p) {
    Marker[] markerArr = mProgramMarkingsMap.get(getIdFor(p));
    
    for (int i = 0; i < markerArr.length; i++) {
      if (markerArr[i].getId().compareTo(plugin.getId()) == 0) {
        return i;
      }
    }

    return -1;
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

  protected void removeProgram(MutableProgram p, Marker marker) {
    if(p!= null && mProgramMarkingsMap.get(getIdFor(p)).length < 1) {
      mMarkedPrograms.remove(p);
      mProgramMarkingsMap.remove(getIdFor(p));
      handleFilterMarking(p);
    }
    else {
      unmark(marker,p);
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
    Program[] p = new Program[mMarkedPrograms.size()];
    mMarkedPrograms.toArray(p);

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
    int n = (mMarkedPrograms.size() > numberOfPrograms && numberOfPrograms > 0) ? numberOfPrograms : mMarkedPrograms.size();

    ArrayList<Program> programs = new ArrayList<Program>();

    int k = 0;
    int i = 0;
    
    Iterator<MutableProgram> it = mMarkedPrograms.iterator();

    long currentDateValue = Date.getCurrentDate().getValue();
    while(i < n) {
      if(k >= mMarkedPrograms.size()) {
        break;
      }

      MutableProgram p = it.next();
      if((p.isOnAir() && !includeOnAirPrograms) || p.isExpired() || !filter.accept(p) || p.getMarkPriority() < markPriority) {
        k++;
        continue;
      }
      long value1 = (p.getDate().getValue() - currentDateValue) * 24 * 60 + p.getStartTime();
      boolean found = false;

      for(int j = 0; j < programs.size(); j++) {
        Program p1 = programs.get(j);
        long value2 = (p1.getDate().getValue() - currentDateValue) * 24 * 60 + p1.getStartTime();

        if(value2 > value1) {
          programs.add(j,p);
          found = true;
          break;
        }
      }

      if(!found && filter.accept(p)) {
        programs.add(p);
      }

      k++;
      i++;
    }

    for(i = k; i < mMarkedPrograms.size(); i++) {
      Program p = it.next();

      if((p.isOnAir() && !includeOnAirPrograms) || p.isExpired() || p.getMarkPriority() < markPriority) {
        continue;
      }

      long valueNew = (p.getDate().getValue() - currentDateValue) * 24 * 60 + p.getStartTime();
      Program p1 = programs.get(programs.size() - 1);

      long valueOld = (p1.getDate().getValue() - currentDateValue) * 24 * 60 + p1.getStartTime();
      if(valueOld > valueNew) {
        for(int j = 0; j < programs.size(); j++) {
          p1 = programs.get(j);
          valueOld = (p1.getDate().getValue() - currentDateValue) * 24 * 60 + p1.getStartTime();

          if(valueOld > valueNew) {
            programs.add(j,p);
            break;
          }
        }
      }

      if(programs.size() > Settings.propTrayImportantProgramsSize.getInt()) {
        programs.remove(Settings.propTrayImportantProgramsSize.getInt());
      }
    }

    Program[] trayPrograms = new Program[programs.size()];
    programs.toArray(trayPrograms);


    return trayPrograms;
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
        
        if(testProg != null && programInList.getTitle().toLowerCase().compareTo(testProg.getTitle().toLowerCase()) != 0) {
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
          mProgramMarkingsMap.remove(programInList.getID());
          programInList.setProgramState(Program.WAS_DELETED_STATE);
        }
        else if(testProg != programInList) {
          programInList.setProgramState(Program.WAS_UPDATED_STATE);
          mMarkedPrograms.add(testProg);
        } else {
          mMarkedPrograms.add(programInList);
        }
      }
    }
  }
  
  private static class MarkerHashMap<K,V> extends HashMap<K,V> {
    public V get(Object o) {
      V value = super.get(o);
      
      if(value != null) {
        return value;
      }
      
      return (V)MutableProgram.EMPTY_MARKER_ARR;
    }
  }
  
  protected Marker[] getMarkerForProgram(Program p) {
    return mProgramMarkingsMap.get(getIdFor(p));
  }
  
  private String getIdFor(Program p) {
    if(p == null || p.getDate() == null) {
      return null;
    }
    else if(p.getID() == null) {
      return "exampleProgramId";
    }
    else {
      return p.getDate().getValue() + " " + ProgramUtilities.getTimeZoneCorrectedProgramId(p.getID());
    }
  }
  
  protected void addChangeListener(ChangeListener listener, Program p) {
    Vector<ChangeListener> listenerVec = mProgramChangeListenerMap.get(getIdFor(p));
    
    if(listenerVec == null) {
      listenerVec = new Vector<ChangeListener>();
      listenerVec.add(listener);
      
      mProgramChangeListenerMap.put(getIdFor(p), listenerVec);
    }
    else {
      listenerVec.add(listener);
    }
  }
  
  protected void removeChangeListener(ChangeListener listener, Program p) {
    Vector<ChangeListener> listenerVec = mProgramChangeListenerMap.get(getIdFor(p));
    
    if(listenerVec != null) {
      listenerVec.remove(listener);
      
      if(listenerVec.isEmpty()) {
        mProgramChangeListenerMap.remove(getIdFor(p));
      }
    }
  }
  
  protected Vector<ChangeListener> getListenerFor(Program p) {
    return mProgramChangeListenerMap.get(getIdFor(p));
  }
}
