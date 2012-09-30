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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.TvDataBase;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import util.io.IOUtilities;
import util.program.ProgramUtilities;
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

  private ArrayList<ProgramTableModelListener> mListenerList;
  
  private Channel[] mChannelArr, mShownChannelArr;
  private Date mMainDay;
  
  private ArrayList<ProgramPanel>[] mProgramColumn, mShownProgramColumn;
  
  private int mLastTimerMinutesAfterMidnight;
  private Timer mTimer;
  
  private ProgramFilter mProgramFilter=null;

  private HashMap<Channel, DateRange> mDateRangeForChannel;
  
  private int[] mOnAirRows;
  
  private HashMap<Channel, Channel> mJointChannels;
  
  /**
   * the currently active channel group
   */
  private ChannelFilterComponent mChannelGroupFilter = null;

  /**
   * Creates a new instance of DefaultProgramTableModel.
   */
  public DefaultProgramTableModel(Channel[] channelArr,
    int todayEarliestTime, int tomorrowLatestTime)
  {
    mDateRangeForChannel = new HashMap<Channel, DateRange>();
    mJointChannels = new HashMap<Channel, Channel>();

    mListenerList = new ArrayList<ProgramTableModelListener>();
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

    int channelTime = ch.getTimeZone().getRawOffset()/60000;
    if (ch.getTimeZone().useDaylightTime()) {
      channelTime+=60;
    }
    
    int localTime = TimeZone.getDefault().getRawOffset()/60000;
    if (ch.getTimeZone().useDaylightTime()) {
      localTime+=60;
    }
    int timeDiff = channelTime - localTime;  // e.g -240min
    
    int startTimeForChannelLocale = mTodayEarliestTime+timeDiff;
    int endTimeForChannelLocale = mTomorrowLatestTime+59+timeDiff+1440;
    
    int fromDate;
     if (startTimeForChannelLocale>=0) {
      fromDate = startTimeForChannelLocale/24/60;
    }
    else {
      fromDate = (startTimeForChannelLocale/24/60)-1;
    }

    int toDate;
    if (endTimeForChannelLocale>0) {
      toDate = endTimeForChannelLocale/24/60 +1;
    }
    else {
      toDate = (endTimeForChannelLocale/24/60)-1+1;
    }

    if(!TvDataBase.getInstance().isDayProgramAvailable(mMainDay.addDays(fromDate),ch)) {
      fromDate++;
    }
    
    return new DateRange(fromDate, toDate-fromDate);

  }


  public void setTimeRange(int todayEarliestTime, int tomorrowLatestTime) {
    mTodayEarliestTime=todayEarliestTime;
    mTomorrowLatestTime=tomorrowLatestTime;
    fireTableDataChanged(null);
  }


  private void updateDateRange() {
    mDateRangeForChannel.clear();
    for (Channel channel : mChannelArr) {
      DateRange dateRange = getDateRangeForChannel(channel);
      mDateRangeForChannel.put(channel, dateRange);
    }
  }



  public void setChannels(Channel[] channelArr) {
    if (channelArr == null) {
      throw new NullPointerException("shownChannelArr is null!");
    }
    checkThread();

    mChannelArr = channelArr;
    mJointChannels.clear();
    
    for(int i = 1; i < channelArr.length; i++) {
      if(channelArr[i-1].isTimeLimited() && channelArr[i].isTimeLimited()) {
        if((channelArr[i-1].getStartTimeLimit() == channelArr[i].getEndTimeLimit())
          && (channelArr[i-1].getEndTimeLimit() == channelArr[i].getStartTimeLimit())) {
          mJointChannels.put(channelArr[i-1], channelArr[i]);
          channelArr[i-1].setJointChannel(channelArr[i]);
        }
        else {
          channelArr[i-1].setJointChannel(null);
        }
      }
      else {
        channelArr[i-1].setJointChannel(null);
      }
    }
    
    mProgramColumn=new ArrayList[mChannelArr.length-mJointChannels.size()];
    for (int i=0;i<mProgramColumn.length;i++) {
      mProgramColumn[i]=new ArrayList<ProgramPanel>();
    }
    
    updateDateRange();
    
    updateTableContent();
  }
  
  
  public void setProgramFilter(ProgramFilter filter) {
    mProgramFilter=filter;
    updateTableContent();
    fireTableDataChanged(null);
  }
  
  public void setChannelGroup(ChannelFilterComponent channelFilter) {
    mChannelGroupFilter = channelFilter;
    updateTableContent();
    fireTableDataChanged(null);
  }


  public ProgramFilter getProgramFilter() {
    return mProgramFilter;
  }
  
  public ChannelFilterComponent getChannelGroup() {
    return mChannelGroupFilter;
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



  private void addChannelDayProgram(int col, ChannelDayProgram[] cdpArr, ChannelDayProgram[] jointChannelProgArr, Date fromDate, int fromMinutes, Date toDate, int toMinutes)
  {
    if (cdpArr == null) {
      return;
    }
    checkThread();
    
    ArrayList<Iterator<Program>> programIteratorArray = new ArrayList<Iterator<Program>>();
    
    if(jointChannelProgArr == null) {
      jointChannelProgArr = new ChannelDayProgram[0];
    }
    
    int minLength = Math.min(cdpArr.length, jointChannelProgArr.length);
    
    for(int i = 0; i < minLength; i++) {
      ArrayList<Program> tempList = new ArrayList<Program>();
      
      if(cdpArr[i] != null) {
        for(int progCount = 0; progCount < cdpArr[i].getProgramCount(); progCount++) {
          tempList.add(cdpArr[i].getProgramAt(progCount));
        }
      }
      if(jointChannelProgArr[i] != null) {
        for(int progCount = 0; progCount < jointChannelProgArr[i].getProgramCount(); progCount++) {
          tempList.add(jointChannelProgArr[i].getProgramAt(progCount));
        }
      }
      
      Collections.sort(tempList,ProgramUtilities.getProgramComparator());
      programIteratorArray.add(tempList.iterator());
    }
    
    ChannelDayProgram[] longerDayRange = cdpArr;
    
    if(jointChannelProgArr.length > cdpArr.length) {
      longerDayRange = jointChannelProgArr;
    }
    
    for(int i = minLength; i < longerDayRange.length; i++) {
      if(longerDayRange[i] != null) {
        programIteratorArray.add(longerDayRange[i].getPrograms());
      }
    }
    
    for (Iterator<Program> it : programIteratorArray) {
      /*if (cdp==null) {
        break;
      }
      Iterator<Program> it=cdp.getPrograms();*/
      if (it!=null) {
        while (it.hasNext()) {
          Program prog=it.next();
          int progTime=prog.getStartTime();
	        Date progDate = prog.getDate();
	        // program starts before given end time
	        if (compareDateTime(progDate, progTime, toDate, toMinutes) <= 0) {
            // program starts after or at given end time
            if (compareDateTime(progDate, progTime, fromDate, fromMinutes) >= 0)  {
  		        if (filterAccepts(prog)) {
                ProgramPanel panel = new ProgramPanel(prog);
                mProgramColumn[col].add(panel);
              }
            }
  	        else {
              // add the last program _before_ the day start time which is still running afterwards
  	          if (mProgramColumn[col].isEmpty()) {
  	            if (compareDateTime(progDate, progTime + prog.getLength(), fromDate, fromMinutes) > 0) {
                  if (filterAccepts(prog)) {
                    ProgramPanel panel = new ProgramPanel(prog);
                    mProgramColumn[col].add(panel);
                  }
                }
  	          }
  	        }
	        }
        }
      }
    }
  }


  private boolean filterAccepts(Program program) {
    return (mChannelGroupFilter == null || mChannelGroupFilter.accept(program)) && (mProgramFilter==null || mProgramFilter.accept(program));
  }

  public void setDate(Date date, ProgressMonitor monitor, Runnable callback)
  {
    mMainDay = date;
    updateDateRange();
    updateTableContent(monitor, callback);
  }
  
  public Date getDate() {
    return mMainDay;
  }
  
  public void updateTableContent() {
    updateTableContent(null, null);
  }

  private void updateTableContent(ProgressMonitor monitor, final Runnable callback)
  {
    // if this is the initial update, skip every UI related operation, just set
    // necessary members. the UI update will be forced when setting the initial
    // filter
    if (mProgramFilter == null) {
      mShownProgramColumn = mProgramColumn.clone();
      mShownChannelArr = mChannelArr.clone();
      return;
    }
    
    checkThread();
    mOnAirRows = null;
    deregisterFromPrograms(mProgramColumn);
    
    TvDataBase db = TvDataBase.getInstance();

    if (monitor != null) {
      monitor.setMaximum(mProgramColumn.length - 1);
      monitor.setValue(0);
    }

    Date nextDay = mMainDay.addDays(1);
    int jointChannelCount = 0;
    
    for (int i = 0; i < mChannelArr.length; i++) {
      mProgramColumn[i-jointChannelCount].clear();
      DateRange dateRange = mDateRangeForChannel.get(mChannelArr[i]);
      ChannelDayProgram[] cdp = new ChannelDayProgram[dateRange.getCount()];

      for (int d = 0; d<cdp.length; d++) {
        cdp[d] = db.getDayProgram(mMainDay.addDays(dateRange.getBegin() + d), mChannelArr[i]);
      }
      
      ChannelDayProgram[] jointChannelDayProgram = null;
      
      Channel jointChannel = mJointChannels.get(mChannelArr[i]);
      
      if(jointChannel != null) {
        jointChannelCount++;
        i++;
        
        dateRange = mDateRangeForChannel.get(jointChannel);
        jointChannelDayProgram = new ChannelDayProgram[dateRange.getCount()];
        
        for (int d = 0; d < jointChannelDayProgram.length; d++) {
          jointChannelDayProgram[d] = db.getDayProgram(mMainDay.addDays(dateRange.getBegin() + d), jointChannel);
        }
      }
      
      addChannelDayProgram(i-jointChannelCount, cdp, jointChannelDayProgram, mMainDay, mTodayEarliestTime, nextDay, mTomorrowLatestTime);

      if (monitor != null) {
        monitor.setValue(i);
      }
    }

    boolean showEmptyColumns = (mProgramFilter instanceof tvbrowser.core.filters.ShowAllFilter) && (mChannelGroupFilter == null);

    ArrayList<ArrayList<ProgramPanel>> newShownColumns = new ArrayList<ArrayList<ProgramPanel>>();
    ArrayList<Channel> newShownChannels = new ArrayList<Channel>();
    
    jointChannelCount = 0;
    
    for (int i = 0; i < mProgramColumn.length; i++) {
      if (showEmptyColumns || mProgramColumn[i].size() > 0) {
        newShownColumns.add(mProgramColumn[i]);
        newShownChannels.add(mChannelArr[i+jointChannelCount]);
        
        if(mJointChannels.get(mChannelArr[i+jointChannelCount]) != null) {
          jointChannelCount++;
        }
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
        fireTableDataChanged(callback);
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
  
  /**
   * @return The number of available channels.
   */
  public int getAvailableChannelCount() {
    checkThread();
    return mChannelArr.length;
  }

  public int getColumnCount() {
    checkThread();
    return Math.min(mShownChannelArr.length, mProgramColumn.length);
  }

  public int getRowCount(int col) {
    checkThread();
    return mShownProgramColumn[col].size();
  }

  public ProgramPanel getProgramPanel(int col, int row) {
    checkThread();

    ArrayList<ProgramPanel> list=mShownProgramColumn[col];
    if (list.size()<=row) {
      return null;
    }
    return list.get(row);
  }

  private void deregisterFromPrograms(ArrayList<ProgramPanel>[] columns) {
    for (ArrayList<ProgramPanel> list : columns) {
      Iterator<ProgramPanel> it=list.iterator();
      while (it.hasNext()) {
        ProgramPanel panel = it.next();
        Program prog = panel.getProgram();
        prog.removeChangeListener(this);
      }
    }
  }

  private void registerAtPrograms(ArrayList<ProgramPanel>[] columns) {
    for (ArrayList<ProgramPanel> list : columns) {
      Iterator<ProgramPanel> it=list.iterator();
      while (it.hasNext()) {
        ProgramPanel panel = it.next();
        Program prog = panel.getProgram();
        prog.addChangeListener(this);
      }
    }
  }

  protected void fireTableDataChanged(Runnable callback) {
    for (int i = 0; i < mListenerList.size(); i++) {
      ProgramTableModelListener lst = mListenerList.get(i);
      lst.tableDataChanged(callback);
    }
  }

  
  
  protected void fireTableCellUpdated(int col, int row) {
    for (int i = 0; i < mListenerList.size(); i++) {
      ProgramTableModelListener lst = mListenerList.get(i);
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
  
  /**
   * Checks if this model contains a program.
   * 
   * @param p The program to check.
   * @return True if the program is contained.
   * @throws Exception Thrown if something goes wrong.
   * @since 2.5.3
   */
  public boolean contains(Program p) throws Exception {
    checkThread();
      
    for(int i = 0; i < mShownChannelArr.length; i++) {
      if(p.getChannel().equals(mShownChannelArr[i])) {
        for(ProgramPanel panel : mShownProgramColumn[i]) {
          if(panel.getProgram().equals(p)) {
            return true;
          }
        }
      }
    }
    
    return false;
  }


  private void handleTimerEvent() {
    checkThread();

    // Avoid a repaint 6 times a minute (Once a minute is enough)
    int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
    if (minutesAfterMidnight == mLastTimerMinutesAfterMidnight) {
      return;
    }

    mLastTimerMinutesAfterMidnight = minutesAfterMidnight;

    // Force a repaint of all programs on air
    // (so the progress background will be updated)
    if(mOnAirRows == null) {
      int columnCount = getColumnCount();
	  mOnAirRows = new int[columnCount];
      Arrays.fill(mOnAirRows, -1);
      for (int col = 0; col < columnCount; col++) {
        int rowCount = getRowCount(col);
		for (int row = 0; row < rowCount; row++) {
          ProgramPanel panel = getProgramPanel(col, row);
          if (panel.getProgram().isOnAir()) {
            mOnAirRows[col] = row;
            fireTableCellUpdated(col, row);
          }
        }
      }
    }
    else {
      for (int col = 0; col < mOnAirRows.length; col++) {
        if(mOnAirRows[col] != -1) {
          ProgramPanel panel = getProgramPanel(col, mOnAirRows[col]);
          
          if(panel.getProgram().isOnAir()) {
            fireTableCellUpdated(col, mOnAirRows[col]);
          }
          else if(panel.getProgram().isExpired()){
            fireTableCellUpdated(col, mOnAirRows[col]);
            
            panel = getProgramPanel(col, mOnAirRows[col]+1);
            
            if(panel == null) {
              mOnAirRows[col] = -1;
            } else {
              mOnAirRows[col] = mOnAirRows[col]+1;
              fireTableCellUpdated(col, mOnAirRows[col]);
            }
          }
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
    
    int rowCount = getRowCount(col);
	// Get the row of this program
    for (int row = 0; row < rowCount; row++) {
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



  private static class DateRange {

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
  
/*  public Channel getJointChannelFor(Channel ch) {
    return mJointChannels.get(ch);
  }
  
  @Override
  public Channel getChannelForChannel(Channel ch) {
    Set<Channel> keys = mJointChannels.keySet();
    
    Channel joined = ch;
    
    for(Channel key : keys) {
      if(mJointChannels.get(key).equals(ch)) {
        joined = key;
        break;
      }
    }
    
    return joined;
  }*/

}
