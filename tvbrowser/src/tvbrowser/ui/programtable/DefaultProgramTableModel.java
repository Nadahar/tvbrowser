/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

/*
 * DefaultProgramTableModel.java
 *
 * Created on 24. Mai 2003, 13:28
 */

package tvbrowser.ui.programtable;

import java.util.ArrayList;
import java.util.Iterator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.io.IOUtilities;

import tvbrowser.core.DayProgram;

import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Program;

/**
 *
 * @author  Til
 */
public class DefaultProgramTableModel implements ProgramTableModel, ChangeListener {
  
  private static final int TOMORROW_LATEST_TIME = 5 * 60; // Until 5:00 am
  private static final int TODAY_EARLIEST_TIME = 5 * 60; // Until 5:00 am


  private ArrayList mListenerList;
  
  private Channel[] mShownChannelArr;
  private DayProgram mMainDay, mNextDay;
  
  private ArrayList[] mProgramColumn;
  
  //private devplugin.Date mDate;
  /** Holds the number of programs for a column. */
  //private int[] mMainDayProgramCount, mNextDayProgramCount;
  
  private int mLastTimerMinutesAfterMidnight;
  private Timer mTimer;
  
  private ProgramFilter mProgramFilter=null;



  /**
   * Creates a new instance of DefaultProgramTableModel.
   */
  public DefaultProgramTableModel(Channel[] shownChannelArr) {
    mListenerList = new ArrayList();
    
    
	setShownChannels(shownChannelArr);
    
    mTimer = new Timer(10000, new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        handleTimerEvent();
      }
    });
  }

  
  
  public void setShownChannels(Channel[] shownChannelArr) {
    if (shownChannelArr == null) {
      throw new NullPointerException("shownChannelArr is null!");
    }
    
    mShownChannelArr = shownChannelArr;
    
	mProgramColumn=new ArrayList[mShownChannelArr.length];
	for (int i=0;i<mProgramColumn.length;i++) {
		mProgramColumn[i]=new ArrayList();
	}
    
 //   updateProgramCount();
 //   fireTableDataChanged();
 	setDayPrograms(mMainDay,mNextDay);
  }
  
  
  public void setProgramFilter(ProgramFilter filter) {
  	mProgramFilter=filter;
    fireTableDataChanged();
    setDayPrograms(mMainDay, mNextDay);
  }
  
  private void addChannelDayProgram(int col, ChannelDayProgram cdp, int start, int end) {
    if (cdp==null) return;
    Iterator it=cdp.getPrograms();  
    while (it.hasNext()) {
      Program prog=(Program)it.next();
      int time=prog.getHours()*60+prog.getMinutes();
      if (time>=start && time<=end) {
        if (mProgramFilter==null || mProgramFilter.accept(prog)) {
          mProgramColumn[col].add(prog);
        }
      }
    }
  }
  
  public void setDayPrograms(DayProgram mainDay, DayProgram nextDay) {
    
    deregisterFromPrograms(mProgramColumn);
    /*
  	for (int i=0;i<mShownChannelArr.length;i++) {
      ChannelDayProgram cdp=mainDay.getChannelDayProgram(mShownChannelArr[i]);
      mProgramColumn[i].clear();
      if (cdp!=null) {
        Iterator it=cdp.getPrograms();
        while (it.hasNext()) {
          Program prog=(Program)it.next();
          if (mProgramFilter==null || mProgramFilter.accept(prog)) {
            mProgramColumn[i].add(prog);
          }
        }
      }    
    }
   */
   
  for (int i=0;i<mShownChannelArr.length;i++) {
    mProgramColumn[i].clear();
    ChannelDayProgram cdp;
    if (mainDay!=null) {
      cdp=mainDay.getChannelDayProgram(mShownChannelArr[i]);     
      addChannelDayProgram(i,cdp,TODAY_EARLIEST_TIME,24*60);
    }
    
    if (nextDay!=null) { 
      cdp=nextDay.getChannelDayProgram(mShownChannelArr[i]);
      addChannelDayProgram(i,cdp,0,TOMORROW_LATEST_TIME);
    }
   }
    
  	
    
  	
  //  deregisterFromPrograms(mMainDay, mMainDayProgramCount);
  //  deregisterFromPrograms(mNextDay, mNextDayProgramCount);
    
    mMainDay = mainDay;
    mNextDay = nextDay;

if (mTimer!=null) {

    // Start the timer if one of the day 
    int todayDaysSince1970 = IOUtilities.getDaysSince1970();
    if (((mMainDay != null) && (mMainDay.getDate().getDaysSince1970() == todayDaysSince1970))
      || ((mNextDay != null) && (mNextDay.getDate().getDaysSince1970() == todayDaysSince1970)))

   
    
    {
      mTimer.start();
      handleTimerEvent();
    } else {
      mTimer.stop();
    }
}   
 //   updateProgramCount();

   // registerAtPrograms(mMainDay, mMainDayProgramCount);
   // registerAtPrograms(mNextDay, mNextDayProgramCount);
    
    registerAtPrograms(mProgramColumn);
    
    fireTableDataChanged();
  }

  
  
  public void addProgramTableModelListener(ProgramTableModelListener listener) {
    mListenerList.add(listener);
  }

  
  
  public Channel[] getShownChannels() {
    return mShownChannelArr;
  }
  
  

  public int getColumnCount() {
    return mShownChannelArr.length;
  }



  public int getRowCount(int col) {
    //return mMainDayProgramCount[col] + mNextDayProgramCount[col];
    
    return mProgramColumn[col].size();
    
  }



  public Program getProgram(int col, int row) {
      
      ArrayList list=mProgramColumn[col];
      if (list.size()<=row) return null;
      return (Program)list.get(row);
     // if (mProgramColumn[col].size()<=row) return null;
     // return (Program)mProgramColumn[col].get(row);
      
      
      /*
    if (row < mMainDayProgramCount[col]) {
      // This program is today
      return getProgram(mMainDay, col, row);
    }
    else if (row < mMainDayProgramCount[col] + mNextDayProgramCount[col]) {
      // This program is tomorrow
      return getProgram(mNextDay, col, row - mMainDayProgramCount[col]);
    }
    else {
      // Illegal row index
      return null;
    }
    */
  }

  
  
  private Program getProgram(DayProgram dayProgram, int col, int index) {
    if (dayProgram == null) {
      return null;
    }
      
    ChannelDayProgram prg = dayProgram.getChannelDayProgram(mShownChannelArr[col]);
    if (prg == null) {
      return null;
    }
    
    return prg.getProgramAt(index);
  }
  
  
  /*
  private void updateProgramCount() {
    mMainDayProgramCount = new int[mShownChannelArr.length];
    mNextDayProgramCount = new int[mShownChannelArr.length];
    
    if (mMainDay != null) {
      for (int col = 0; col < mMainDayProgramCount.length; col++) {
        ChannelDayProgram prg = mMainDay.getChannelDayProgram(mShownChannelArr[col]); 
        if (prg != null) {
          mMainDayProgramCount[col] = prg.getProgramCount();
        }
      }
    }

    if (mNextDay != null) {
      for (int col = 0; col < mNextDayProgramCount.length; col++) {
        ChannelDayProgram prg = mNextDay.getChannelDayProgram(mShownChannelArr[col]); 
        if (prg != null) {
          // Count the programs until the latest time is reached
          Iterator iter = prg.getPrograms();
          while (iter.hasNext()) {
            Program program = (Program) iter.next();
            int startTime = program.getHours() * 60 + program.getMinutes();
            if (startTime > TOMORROW_LATEST_TIME) {
              break;
            }
            mNextDayProgramCount[col]++;
          }
        }
      }
    }
  }
*/

  private void deregisterFromPrograms(ArrayList[] columns) {
      
    for (int i=0;i<columns.length;i++) {
      Iterator it=columns[i].iterator();
      while (it.hasNext()) {
        Program prog=(Program)it.next();
        prog.removeChangeListener(this);
      }
    }          
  }

/*
  private void deregisterFromPrograms(DayProgram dayProgram, int[] programCountArr) {
    if (dayProgram != null) {
      for (int col = 0; col < mShownChannelArr.length; col++) {
        ChannelDayProgram prg = dayProgram.getChannelDayProgram(mShownChannelArr[col]);
        for (int row = 0; row < programCountArr[col]; row++) {
          Program program = prg.getProgramAt(row);
          program.removeChangeListener(this);
        }
      }
    }
  }
*/

  private void registerAtPrograms(ArrayList[] columns) {
  
    for (int i=0;i<columns.length;i++) {
      Iterator it=columns[i].iterator();
      while (it.hasNext()) {
        Program prog=(Program)it.next();
        prog.addChangeListener(this);
      }
    }          
     
         
     
        
  }
  
  
  /*
  private void registerAtPrograms(DayProgram dayProgram, int[] programCountArr) {
    if (dayProgram != null) {
      for (int col = 0; col < mShownChannelArr.length; col++) {
        ChannelDayProgram prg = dayProgram.getChannelDayProgram(mShownChannelArr[col]);
        for (int row = 0; row < programCountArr[col]; row++) {
          Program program = prg.getProgramAt(row);
          program.addChangeListener(this);
        }
      }
    }
  }

*/

  protected void fireTableDataChanged() {
    for (int i = 0; i < mListenerList.size(); i++) {
      ProgramTableModelListener lst = (ProgramTableModelListener) mListenerList.get(i);
      lst.tableDataChanged();
    }
  }

  
  
  protected void fireTableCellUpdated(int col, int row) {
    for (int i = 0; i < mListenerList.size(); i++) {
      ProgramTableModelListener lst = (ProgramTableModelListener) mListenerList.get(i);
      lst.tableCellUpdated(col, row);
    }
  }
  
  
  
  protected int getColumnOfChannel(Channel channel) {
    for (int col = 0; col < mShownChannelArr.length; col++) {
      if (channel.equals(mShownChannelArr[col])) {
        return col;
      }
    }
    
    // No such column found
    return -1;
  }



  private void handleTimerEvent() {
    // Avoid a repaint 6 times a minute (Once a minute is enough)
    int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
    if (minutesAfterMidnight == mLastTimerMinutesAfterMidnight) {
      return;
    }
    
    mLastTimerMinutesAfterMidnight = minutesAfterMidnight;
    
    int todayDaysSince1970 = IOUtilities.getDaysSince1970();
    if (mMainDay.getDate().getDaysSince1970() == todayDaysSince1970) {
      mMainDay.markProgramsOnAir();
    }
    else if (mNextDay.getDate().getDaysSince1970() == todayDaysSince1970) {
      mNextDay.markProgramsOnAir();
    }
  }



  public void stateChanged(ChangeEvent evt) {
    // A program has changed
    Program program = (Program) evt.getSource();
    
    // Get the column of this program
    int col = getColumnOfChannel(program.getChannel());
    if (col == -1) {
      // This program is not shown in this table
      return;
    }
    
    // Get the row of this program
    int row = 0;
    for (; row < getRowCount(col); row++) {
      Program prg = getProgram(col, row);
      if (program == prg) {
        break;
      }
    }

    fireTableCellUpdated(col, row);
  }

}
