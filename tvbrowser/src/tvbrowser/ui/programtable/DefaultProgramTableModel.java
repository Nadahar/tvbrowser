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
import java.util.Iterator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.TvDataBase;
import util.io.IOUtilities;
import util.ui.ProgramPanel;

import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Program;
import devplugin.Date;
import devplugin.ProgressMonitor;
import devplugin.ProgramFilter;

/**
 *
 * @author  Til
 */
public class DefaultProgramTableModel implements ProgramTableModel, ChangeListener {
  
  private int mTomorrowLatestTime;
  private int mTodayEarliestTime;

  private ArrayList mListenerList;
  
  private Channel[] mChannelArr, mShownChannelArr;
  private Date mMainDay, mNextDay;
  
  private ArrayList[] mProgramColumn, mShownProgramColumn;
  
  private int mLastTimerMinutesAfterMidnight;
  private Timer mTimer;
  
  private ProgramFilter mProgramFilter=null;



  /**
   * Creates a new instance of DefaultProgramTableModel.
   */
  public DefaultProgramTableModel(Channel[] channelArr,
    int todayEarliestTime, int tomorrowLatestTime)
  {
    mListenerList = new ArrayList();
    mTodayEarliestTime=todayEarliestTime;
    mTomorrowLatestTime=tomorrowLatestTime;

    mMainDay = new Date();
    mNextDay = mMainDay.addDays(1);
    
	  setChannels(channelArr);
    
    mTimer = new Timer(10000, new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        handleTimerEvent();
      }
    });
    mTimer.start();
  }

  public void setTimeRange(int todayEarliestTime, int tomorrowLatestTime) {
    mTodayEarliestTime=todayEarliestTime;
    mTomorrowLatestTime=tomorrowLatestTime;
    fireTableDataChanged();
  }
  
  
  
  
  public void setChannels(Channel[] channelArr) {
    if (channelArr == null) {
      throw new NullPointerException("shownChannelArr is null!");
    }
    mChannelArr = channelArr;
    
	  mProgramColumn=new ArrayList[mChannelArr.length];
	  for (int i=0;i<mProgramColumn.length;i++) {
		  mProgramColumn[i]=new ArrayList();
	  }
    
    updateTableContent();
  }
  
  
  public void setProgramFilter(ProgramFilter filter) {
    mProgramFilter=filter;
    fireTableDataChanged();
    updateTableContent();
  }
  
  private void addChannelDayProgram(int col, ChannelDayProgram cdp, int startMinutes, Date startDate, int endMinutes, Date endDate ) {
    if (cdp==null) return;
    Iterator it=cdp.getPrograms();  
    if (it!=null) {
      while (it.hasNext()) {
        Program prog=(Program)it.next();
        int time=prog.getHours()*60+prog.getMinutes();
        if (time>=startMinutes && time<=endMinutes && prog.getDate().compareTo(startDate)>=0 && prog.getDate().compareTo(endDate)<=0) {
          if (mProgramFilter==null || mProgramFilter.accept(prog)) {
            ProgramPanel panel = new ProgramPanel(prog);
            mProgramColumn[col].add(panel);
          }
        }
      }
    }
  }
  
 

  public void setDate(Date date, ProgressMonitor monitor, Runnable callback) {
    mMainDay = date;
    mNextDay = date.addDays(1);
    
    updateTableContent(monitor, callback);
  }
  
  
  public Date getDate() {
    return mMainDay;
  }
  
  
  private void updateTableContent() {
    updateTableContent(null, null);
  }
  
  private void updateTableContent(ProgressMonitor monitor, final Runnable callback) {
    
    deregisterFromPrograms(mProgramColumn);
    
    TvDataBase db = TvDataBase.getInstance();
    
    if (monitor!=null) {    
      monitor.setMaximum(mChannelArr.length-1);
      monitor.setValue(0);
    }
    
    for (int i = 0; i < mChannelArr.length; i++) {
      
      mProgramColumn[i].clear();
      ChannelDayProgram cdp = db.getDayProgram(mMainDay, mChannelArr[i]);
      if (cdp != null) {
        addChannelDayProgram(i, cdp, mTodayEarliestTime, cdp.getDate(),
                             24 * 60, cdp.getDate());
      }

      cdp = db.getDayProgram(mNextDay, mChannelArr[i]);
      if (cdp != null) {
        addChannelDayProgram(i, cdp, 0, cdp.getDate(), mTomorrowLatestTime,
                             cdp.getDate());
      }
      
      if (monitor!=null) {
        monitor.setValue(i);
      }       
      
    }
    boolean showEmptyColumns = mProgramFilter instanceof tvbrowser.core.filters.ShowAllFilter;
    
      ArrayList newShownColumns = new ArrayList();
      ArrayList newShownChannels = new ArrayList();
      for (int i=0; i<mProgramColumn.length; i++) {
        if (showEmptyColumns || mProgramColumn[i].size()>0) {
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
    
                    fireTableDataChanged();
                
                    if (callback != null) {
                      callback.run();
                    }
                
              }
        
            });
    
    
    
    
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
    return mShownProgramColumn[col].size();   
  }



  public ProgramPanel getProgramPanel(int col, int row) {
      
    //  ArrayList list=mProgramColumn[col];
    //  if (list.size()<=row) return null;
    //  return (ProgramPanel)list.get(row);
      
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
    //for (int col = 0; col < mChannelArr.length; col++) {
      //if (channel.equals(mChannelArr[col])) {
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
  
  
  private synchronized void updateProgramsOnAir() {
    TvDataBase db = TvDataBase.getInstance();
    for (int i = 0; i < mChannelArr.length; i++) {
      Channel channel = mChannelArr[i];

      ChannelDayProgram dayProg = db.getDayProgram(mMainDay, channel);
      if (dayProg != null) {
        dayProg.markProgramOnAir();
      }

      dayProg = db.getDayProgram(mNextDay, channel);
      if (dayProg != null) {
        dayProg.markProgramOnAir();
      }
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
    for (int row = 0; row < getRowCount(col); row++) {
      ProgramPanel panel = getProgramPanel(col, row);
      if (program == panel.getProgram()) {
        fireTableCellUpdated(col, row);
        return;
      }
    }
  }

}
