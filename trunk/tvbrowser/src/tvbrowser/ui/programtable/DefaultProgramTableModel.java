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

  private ArrayList mListenerList;
  
  private Channel[] mShownChannelArr;
  private DayProgram mMainDay, mNextDay;
  
  /** Holds the number of programs for a column. */
  private int[] mMainDayProgramCount, mNextDayProgramCount;
  
  private int mLastTimerMinutesAfterMidnight;
  private Timer mTimer;



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
    
    updateProgramCount();
    fireTableDataChanged();
  }
  
  
  
  public void setDayPrograms(DayProgram mainDay, DayProgram nextDay) {
    deregisterFromPrograms(mMainDay, mMainDayProgramCount);
    deregisterFromPrograms(mNextDay, mNextDayProgramCount);
    
    mMainDay = mainDay;
    mNextDay = nextDay;

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
    
    updateProgramCount();

    registerAtPrograms(mMainDay, mMainDayProgramCount);
    registerAtPrograms(mNextDay, mNextDayProgramCount);
    
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
    return mMainDayProgramCount[col] + mNextDayProgramCount[col];
  }



  public Program getProgram(int col, int row) {
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
