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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.ChannelFilter;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgressMonitor;
import tvbrowser.core.TvDataBase;
import tvbrowser.ui.mainframe.MainFrame;
import util.io.IOUtilities;
import util.program.ProgramUtilities;
import util.ui.ProgramPanel;

/**
 *
 * @author  Til
 */
public class DefaultProgramTableModel implements ProgramTableModel, ChangeListener {
  private static final int MAXIMUM_WAIT_TIME_FOR_THREAD_POOL_IN_SECONDS = 30;
  
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
  
  /**
   * the currently active channel filter
   */
  private ChannelFilter mChannelFilter = null;

  /**
   * Creates a new instance of DefaultProgramTableModel.
   * @param channelArr The array with the channels to show.
   * @param todayEarliestTime The start of the day.
   * @param tomorrowLatestTime THe end of the day.
   */
  public DefaultProgramTableModel(Channel[] channelArr,
    int todayEarliestTime, int tomorrowLatestTime)
  {
    mDateRangeForChannel = new HashMap<Channel, DateRange>();
    //mJointChannels = new HashMap<Channel, Channel>();

    mListenerList = new ArrayList<ProgramTableModelListener>();
    mTodayEarliestTime=todayEarliestTime;
    mTomorrowLatestTime=tomorrowLatestTime;


    mMainDay = new Date();

	  setChannels(channelArr);


    mTimer = new Timer(10000, e -> {
      handleTimerEvent();
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



  @SuppressWarnings("unchecked")
  public void setChannels(Channel[] channelArr) {
    if (channelArr == null) {
      throw new NullPointerException("shownChannelArr is null!");
    }
    checkThread();

    mChannelArr = channelArr;
    
    int joinedChannelCount = 0;
    
    for(Channel ch :mChannelArr) {
      if(ch.getJointChannel() != null) {
        joinedChannelCount++;
      }
    }
    
    mProgramColumn=new ArrayList[mChannelArr.length-joinedChannelCount];
    //mProgramColumn[i].toArray(a)
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
  
  public void setChannelFilter(ChannelFilter channelFilter) {
    mChannelFilter = channelFilter;
    updateTableContent();
    fireTableDataChanged(null);
  }


  public ProgramFilter getProgramFilter() {
    return mProgramFilter;
  }
  
  public ChannelFilter getChannelFilter() {
    return mChannelFilter;
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
    return (mChannelFilter == null || mChannelFilter.accept(program)) && (mProgramFilter==null || mProgramFilter.accept(program));
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

  @SuppressWarnings("unchecked")
  private synchronized void updateTableContent(final ProgressMonitor monitor, final Runnable callback)
  {
    // if this is the initial update, skip every UI related operation, just set
    // necessary members. the UI update will be forced when setting the initial
    // filter
    if (mProgramFilter == null || MainFrame.isStarting()) {
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

    final Date nextDay = mMainDay.addDays(1);
    int jointChannelCount = 0;
    
    ExecutorService threadPool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(),3));
    
    for (int i = 0; i < mChannelArr.length; i++) {
      mProgramColumn[i-jointChannelCount].clear();
      DateRange dateRange = mDateRangeForChannel.get(mChannelArr[i]);
      final ChannelDayProgram[] cdp = new ChannelDayProgram[dateRange.getCount()];

      for (int d = 0; d<cdp.length; d++) {
        cdp[d] = db.getDayProgram(mMainDay.addDays(dateRange.getBegin() + d), mChannelArr[i]);
      }
      
      ChannelDayProgram[] jointChannelDayProgram = null;
      
      Channel jointChannel = mChannelArr[i].getJointChannel();
      
      if(jointChannel != null) {
        jointChannelCount++;
        i++;
        
        dateRange = mDateRangeForChannel.get(jointChannel);
        jointChannelDayProgram = new ChannelDayProgram[dateRange.getCount()];
        
        for (int d = 0; d < jointChannelDayProgram.length; d++) {
          jointChannelDayProgram[d] = db.getDayProgram(mMainDay.addDays(dateRange.getBegin() + d), jointChannel);
        }
      }
      
      final int finalI = i;
      final int index = i-jointChannelCount;
      final ChannelDayProgram[] use = jointChannelDayProgram;
      
      threadPool.execute(new Thread("ADD CHANNEL DAY PROGRAM TO PROGRAM TABLE MODEL THREAD") {
        @Override
        public void run() {
          addChannelDayProgram(index, cdp, use, mMainDay, mTodayEarliestTime, nextDay, mTomorrowLatestTime);
          
          if (monitor != null) {
            monitor.setValue(finalI);
          }
        }
      });

    }

    threadPool.shutdown();
    
    try {
      threadPool.awaitTermination(MAXIMUM_WAIT_TIME_FOR_THREAD_POOL_IN_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {}
    
    boolean showEmptyColumns = (mProgramFilter instanceof tvbrowser.core.filters.ShowAllFilter) && (mChannelFilter == null);

    ArrayList<ArrayList<ProgramPanel>> newShownColumns = new ArrayList<ArrayList<ProgramPanel>>();
    ArrayList<Channel> newShownChannels = new ArrayList<Channel>();
    
    jointChannelCount = 0;
    
    for (int i = 0; i < mProgramColumn.length; i++) {
      if ((showEmptyColumns || mProgramColumn[i].size() > 0) && mChannelArr[i+jointChannelCount].getBaseChannel() == null) {
        newShownColumns.add(mProgramColumn[i]);
        newShownChannels.add(mChannelArr[i+jointChannelCount]);
      }
      
      if(mChannelArr[i+jointChannelCount].getJointChannel() != null) {
        jointChannelCount++;
      }
    }
    mShownProgramColumn = new ArrayList[newShownColumns.size()];
    mShownChannelArr = new Channel[newShownChannels.size()];

    newShownColumns.toArray(mShownProgramColumn);
    newShownChannels.toArray(mShownChannelArr);

    handleTimerEvent();
    registerAtPrograms(mProgramColumn);
    fireTableDataChanged(callback);
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
    return Math.min(mShownChannelArr.length, mProgramColumn.length);
  }

  public int getRowCount(int col) {
    return mShownProgramColumn[col].size();
  }

  public ProgramPanel getProgramPanel(int col, int row) {
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

  private void registerAtPrograms(final ArrayList<ProgramPanel>[] columns) {
    for (ArrayList<ProgramPanel> list : columns) {
      Iterator<ProgramPanel> it=list.iterator();
      while (it.hasNext()) {
        ProgramPanel panel = it.next();
        Program prog = panel.getProgram();
        prog.addChangeListener(DefaultProgramTableModel.this);
      }
    }
  }

  protected void fireTableDataChanged(Runnable callback) {
    for (int i = 0; i < mListenerList.size(); i++) {
      ProgramTableModelListener lst = mListenerList.get(i);
      lst.tableDataChanged(callback);
    }
  }

  
  
  protected void fireTableCellUpdated(final int col, final int row) {
    for (int i = 0; i < mListenerList.size(); i++) {
      ProgramTableModelListener lst = mListenerList.get(i);
      lst.tableCellUpdated(col, row);
    }
  }
  
  
  
  protected int getColumnOfChannel(Channel channel) {
    channel = Channel.getChannelForChannel(channel);
    
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

    ExecutorService threadPool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(),3));
    
    // Force a repaint of all programs on air
    // (so the progress background will be updated)
    if(mOnAirRows == null) {
      int columnCount = getColumnCount();
	  mOnAirRows = new int[columnCount];
      Arrays.fill(mOnAirRows, -1);
      for (int col1 = 0; col1 < columnCount; col1++) {
        final int col = col1;
        final int rowCount = getRowCount(col);
		for (int row1 = 0; row1 < rowCount; row1++) {
		  final int row = row1;
		  
		  threadPool.execute(new Thread("FORCE REPAINT ON AIR PROGRAMS THREAD NEW") {
		    @Override
		    public void run() {
		      ProgramPanel panel = getProgramPanel(col, row);
	          if (panel.getProgram().isOnAir()) {
	            mOnAirRows[col] = row;
	            fireTableCellUpdated(col, row);
	          }
		    }
		  });
        }
      }
    }
    else {
      for (int col1 = 0; col1 < mOnAirRows.length; col1++) {
        final int col = col1;
        
        threadPool.execute(new Thread("FORCE REPAINT ON AIR PROGRAMS THREAD CURRENT") {
          public void run() {
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
          };
        });
      }
    }
    
    threadPool.shutdown();
    
    try {
      threadPool.awaitTermination(MAXIMUM_WAIT_TIME_FOR_THREAD_POOL_IN_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {}
  }


  /**
   * Called when a program has changed.
   * 
   * @param evt The change event.
   */
  public void stateChanged(ChangeEvent evt) {
    // A program has changed -> fire the event
    final Program program = (Program) evt.getSource();
    SwingUtilities.invokeLater(() -> {
      fireProgramHasChanged(program);
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
        if(panel.pictureStateChanged()) {
          updateTableContent();
        }
        else {
          // Tell the panel that its program has changed
          panel.programHasChanged();
          
          // Fire the event
          fireTableCellUpdated(col, row);
        }
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
}
