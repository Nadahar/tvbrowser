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
package tvbrowser.ui.programtable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.TvDataBase;
import util.io.IOUtilities;
import util.ui.ProgramPanel;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgressMonitor;

/**
 *
 * @author  Til
 */
public class DefaultProgramTableModel implements ProgramTableModel, ChangeListener {
  
  private int mTomorrowLatestTime;
  private int mTodayEarliestTime;

  private ArrayList mListenerList;
  
  private Channel[] mChannelArr, mShownChannelArr;
  private Date mMainDay;
  
  private ArrayList[] mProgramColumn, mShownProgramColumn;
  
  private int mLastTimerMinutesAfterMidnight;
  private Timer mTimer;
  
  private ProgramFilter mProgramFilter=null;

  private HashMap mDateRangeForChannel;

  /**
   * Creates a new instance of DefaultProgramTableModel.
   */
  public DefaultProgramTableModel(Channel[] channelArr,
    int todayEarliestTime, int tomorrowLatestTime)
  {
    mDateRangeForChannel = new HashMap();

    mListenerList = new ArrayList();
    mTodayEarliestTime=todayEarliestTime;
    mTomorrowLatestTime=tomorrowLatestTime;


    mMainDay = new Date();

	  setChannels(channelArr);


    mTimer = new Timer(10000, new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        handleTimerEvent();
      }
    });
    mTimer.start();
  }




  private DateRange getDateRangeForChannel(Channel ch) {

    int channelTime = ch.getTimeZone().getRawOffset()/1000/60/60;

    if (ch.getTimeZone().useDaylightTime()) {
      channelTime+=1;
    }
    int localTime = TimeZone.getDefault().getRawOffset()/1000/60/60;
    if (ch.getTimeZone().useDaylightTime()) {
      localTime+=1;
    }
    int timeDiff = channelTime - localTime;  // e.g -4h

    int startTimeForChannelLocale = mTodayEarliestTime/60+timeDiff;
    int endTimeForChannelLocale = (mTomorrowLatestTime+59)/60+timeDiff+24;


    int fromDate;
     if (startTimeForChannelLocale>=0) {
      fromDate = startTimeForChannelLocale/24;
    }
    else {
      fromDate = (startTimeForChannelLocale/24)-1;
    }

    int toDate;
    if (endTimeForChannelLocale>0) {
      toDate = endTimeForChannelLocale/24 +1;
    }
    else {
      toDate = (endTimeForChannelLocale/24)-1+1;
    }

    if(!TvDataBase.getInstance().isDayProgramAvailable(mMainDay.addDays(fromDate),ch))
      fromDate++;
    
    return new DateRange(fromDate, toDate-fromDate);

  }


  public void setTimeRange(int todayEarliestTime, int tomorrowLatestTime) {
    mTodayEarliestTime=todayEarliestTime;
    mTomorrowLatestTime=tomorrowLatestTime;
    fireTableDataChanged(null);
  }


  private void updateDateRange() {
    mDateRangeForChannel.clear();
    for (int i=0; i<mChannelArr.length; i++) {
      DateRange dateRange = getDateRangeForChannel(mChannelArr[i]);
      mDateRangeForChannel.put(mChannelArr[i], dateRange);
    }
  }



  public void setChannels(Channel[] channelArr) {
    if (channelArr == null) {
      throw new NullPointerException("shownChannelArr is null!");
    }
    checkThread();

    mChannelArr = channelArr;
    
    mProgramColumn=new ArrayList[mChannelArr.length];
    for (int i=0;i<mProgramColumn.length;i++) {
      mProgramColumn[i]=new ArrayList();
    }

    updateDateRange();

    updateTableContent();
  }
  
  
  public void setProgramFilter(ProgramFilter filter) {
    mProgramFilter=filter;
    fireTableDataChanged(null);
    updateTableContent();
  }


  public ProgramFilter getProgramFilter() {
    return mProgramFilter;
  }


  private static int compareDateTime(Date d1, int m1, Date d2, int m2) {
    if (d1.compareTo(d2)<0) { // (d1<d2)
      return -1;
    }
    else if (d1.compareTo(d2)>0) { //(d1>d2)
      return 1;
    }
    else { // d1 == d2
      if (m1<m2) {
        return -1;
      }
      else if (m1>m2) {
        return 1;
      }
      else {
          return 0;
      }
    }
  }



  private void addChannelDayProgram(int col, ChannelDayProgram[] cdpArr, Date fromDate, int fromMinutes, Date toDate, int toMinutes)
  {
    if (cdpArr == null) {
      return;
    }
    checkThread();

    for (int i = 0; i<cdpArr.length; i++) {
      ChannelDayProgram cdp = cdpArr[i];
      if (cdp==null) {
        break;
      }
      Iterator it=cdp.getPrograms();
      if (it!=null) {
        while (it.hasNext()) {
          Program prog=(Program)it.next();
          int time=prog.getHours()*60+prog.getMinutes();          
	        if (compareDateTime(prog.getDate(), time, fromDate, fromMinutes) >=0 && compareDateTime(prog.getDate(), time, toDate, toMinutes)<=0) {
		        if (mProgramFilter==null || mProgramFilter.accept(prog)) {
              ProgramPanel panel = new ProgramPanel(prog);
              mProgramColumn[col].add(panel);
            }
          }
        }
      }
    }
  }




  public void setDate(Date date, ProgressMonitor monitor,
    Runnable callback)
  {
    mMainDay = date;
    updateDateRange();
    updateTableContent(monitor, callback);
  }
  
  
  public Date getDate() {
    return mMainDay;
  }
  
  
  private void updateTableContent() {
    updateTableContent(null, null);
  }


  private void updateTableContent(ProgressMonitor monitor,
    final Runnable callback)
  {
    checkThread();
    deregisterFromPrograms(mProgramColumn);

    TvDataBase db = TvDataBase.getInstance();

    if (monitor != null) {
      monitor.setMaximum(mChannelArr.length - 1);
      monitor.setValue(0);
    }

    Date nextDay = mMainDay.addDays(1);
    for (int i = 0; i < mChannelArr.length; i++) {
      mProgramColumn[i].clear();
      DateRange dateRange = (DateRange)mDateRangeForChannel.get(mChannelArr[i]);
      ChannelDayProgram[] cdp = new ChannelDayProgram[dateRange.getCount()];

      for (int d = 0; d<cdp.length; d++) {
        cdp[d] = db.getDayProgram(mMainDay.addDays(dateRange.getBegin() + d), mChannelArr[i]);
      }
      addChannelDayProgram(i, cdp, mMainDay, mTodayEarliestTime, nextDay, mTomorrowLatestTime);

      if (monitor != null) {
        monitor.setValue(i);
      }
    }

    boolean showEmptyColumns = mProgramFilter instanceof tvbrowser.core.filters.ShowAllFilter;

    ArrayList newShownColumns = new ArrayList();
    ArrayList newShownChannels = new ArrayList();
    for (int i = 0; i < mProgramColumn.length; i++) {
      if (showEmptyColumns || mProgramColumn[i].size() > 0) {
        newShownColumns.add(mProgramColumn[i]);
        newShownChannels.add(mChannelArr[i]);
      }
    }
    mShownProgramColumn = new ArrayList[newShownColumns.size()];
    mShownChannelArr = new Channel[newShownChannels.size()];

    newShownColumns.toArray(mShownProgramColumn);
    newShownChannels.toArray(mShownChannelArr);

    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        handleTimerEvent();

        registerAtPrograms(mProgramColumn);

        // Update the programs on air
        updateProgramsOnAir();

        fireTableDataChanged(callback);

        /*if (callback != null) {
          callback.run();
        }*/
      }
    });
  }

  
  
  public void addProgramTableModelListener(ProgramTableModelListener listener) {
    mListenerList.add(listener);
  }

  
  
  public Channel[] getShownChannels() {
    checkThread();
    return mShownChannelArr;
  }
  
  

  public int getColumnCount() {
    checkThread();
    return mShownChannelArr.length;
  }



  public int getRowCount(int col) {
    checkThread();
    return mShownProgramColumn[col].size();   
  }



  public ProgramPanel getProgramPanel(int col, int row) {
    checkThread();

    ArrayList list=mShownProgramColumn[col];
    if (list.size()<=row) return null;
    return (ProgramPanel)list.get(row);
 
  }

  
 

  private void deregisterFromPrograms(ArrayList[] columns) {
    for (int i=0;i<columns.length;i++) {
      Iterator it=columns[i].iterator();
      while (it.hasNext()) {
        ProgramPanel panel = (ProgramPanel) it.next();
        Program prog = panel.getProgram();
        prog.removeChangeListener(this);
      }
    }          
  }


  private void registerAtPrograms(ArrayList[] columns) {
    for (int i=0;i<columns.length;i++) {
      Iterator it=columns[i].iterator();
      while (it.hasNext()) {
        ProgramPanel panel = (ProgramPanel) it.next();
        Program prog = panel.getProgram();
        prog.addChangeListener(this);
      }
    }
  }

 

  protected void fireTableDataChanged(Runnable callback) {
    for (int i = 0; i < mListenerList.size(); i++) {
      ProgramTableModelListener lst = (ProgramTableModelListener) mListenerList.get(i);
      lst.tableDataChanged(callback);
    }
  }

  
  
  protected void fireTableCellUpdated(int col, int row) {
    for (int i = 0; i < mListenerList.size(); i++) {
      ProgramTableModelListener lst = (ProgramTableModelListener) mListenerList.get(i);
      lst.tableCellUpdated(col, row);
    }
  }
  
  
  
  protected int getColumnOfChannel(Channel channel) {
    checkThread();
    for (int col = 0; col < mShownChannelArr.length; col++) {
      if (channel.equals(mShownChannelArr[col])) {
        return col;
      }
    }
    
    // No such column found
    return -1;
  }



  private void handleTimerEvent() {
    checkThread();

    // Avoid a repaint 6 times a minute (Once a minute is enough)
    int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
    if (minutesAfterMidnight == mLastTimerMinutesAfterMidnight) {
      return;
    }

    mLastTimerMinutesAfterMidnight = minutesAfterMidnight;
        
    // Update the programs on air
    updateProgramsOnAir();
    
    // Force a repaint of all programs on air
    // (so the progress background will be updated)
    for (int col = 0; col < getColumnCount(); col++) {
      for (int row = 0; row < getRowCount(col); row++) {
        ProgramPanel panel = getProgramPanel(col, row);
        if (panel.getProgram().isOnAir()) {
          fireTableCellUpdated(col, row);
        }
      }
    }
  }
  
  
  private void updateProgramsOnAir() {
    checkThread();
    
    TvDataBase db = TvDataBase.getInstance();
    for (int i = 0; i < mChannelArr.length; i++) {
      Channel channel = mChannelArr[i];

      DateRange dateRange = (DateRange)mDateRangeForChannel.get(channel);
      int cnt = dateRange.getCount();
      for (int j=0; j<cnt; j++) {
        ChannelDayProgram dayProg = db.getDayProgram(mMainDay.addDays(j), channel);
        if (dayProg != null) {
          dayProg.markProgramOnAir();
        }
      }
    }
  }


  /**
   * Called when a program has changed.
   * 
   * @param evt The change event.
   */
  public void stateChanged(ChangeEvent evt) {
    // A program has changed -> fire the event
    final Program program = (Program) evt.getSource();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        fireProgramHasChanged(program);
      }
    });
  }


  protected void fireProgramHasChanged(Program program) {
    // Get the column of this program
    int col = getColumnOfChannel(program.getChannel());
    if (col == -1) {
      // This program is not shown in this table
      return;
    }
    
    // Get the row of this program
    for (int row = 0; row < getRowCount(col); row++) {
      ProgramPanel panel = getProgramPanel(col, row);
      if (program == panel.getProgram()) {
        // Tell the panel that its program has changed
        panel.programHasChanged();
        
        // Fire the event
        fireTableCellUpdated(col, row);
        return;
      }
    }
  }
  
  
  private void checkThread() {
    if (! SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("The table model must be used in the "
          + "Swing event thread (use SwingUtilities.invokeLater())");
    }
  }



  class DateRange {

    private int mCnt;
    private int mBegin;

    public DateRange(int begin, int cnt) {
      mBegin = begin;
      mCnt = cnt;
    }

    public int getBegin() {
      return mBegin;
    }

    public int getCount() {
      return mCnt;
    }

  }

}
