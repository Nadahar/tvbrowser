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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package devplugin;

import java.io.*;

import java.util.Calendar;

//import com.sun.rsasign.o;

//import util.io.IOUtilities;

public class Date implements Comparable {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(Date.class);
  
  private static final String[] DAY_MSG_ARR = {
    mLocalizer.msg("day.0", "So"),
    mLocalizer.msg("day.1", "Mo"),
    mLocalizer.msg("day.2", "Tu"),
    mLocalizer.msg("day.3", "We"),
    mLocalizer.msg("day.4", "Th"),
    mLocalizer.msg("day.5", "Fr"),
    mLocalizer.msg("day.6", "Sa")
  };

  private static final String[] MONTH_MSG_ARR = {
    mLocalizer.msg("month.1", "Jan"),
    mLocalizer.msg("month.2", "Feb"),
    mLocalizer.msg("month.3", "Mar"),
    mLocalizer.msg("month.4", "Apr"),
    mLocalizer.msg("month.5", "May"),
    mLocalizer.msg("month.6", "Jun"),
    mLocalizer.msg("month.7", "Jul"),
    mLocalizer.msg("month.8", "Aug"),
    mLocalizer.msg("month.9", "Sep"),
    mLocalizer.msg("month.10", "Oct"),
    mLocalizer.msg("month.11", "Nov"),
    mLocalizer.msg("month.12", "Dec")
  };
  
  
  
  //private int date;  // days since 70-01-01
  private int mYear;
  private int mMonth;
  private int mDay;
  private Calendar mCalendar;
  
  private static Date DATE=new Date();

  /**
   * Constructs a new Date object, initialized with the current date.
   */
  public Date() {
    mCalendar=Calendar.getInstance();
    mYear=mCalendar.get(Calendar.YEAR);
    mMonth=mCalendar.get(Calendar.MONTH)+1;
    mDay=mCalendar.get(Calendar.DAY_OF_MONTH);
  }

/**
 * 
 * @deprecated
 */
  public Date(int daysSince1970) {
    
    long l=(long)daysSince1970*24*60*60*1000;
    java.util.Date d=new java.util.Date(l);
    mCalendar=Calendar.getInstance();
    mCalendar.setTime(d);
    mYear=mCalendar.get(Calendar.YEAR);
    mMonth=mCalendar.get(Calendar.MONTH)+1;
    mDay=mCalendar.get(Calendar.DAY_OF_MONTH);
  }


public Date(Calendar cal) {
  mCalendar=(Calendar)cal.clone();
  mYear=mCalendar.get(Calendar.YEAR);
  mMonth=mCalendar.get(Calendar.MONTH)+1;
  mDay=mCalendar.get(Calendar.DAY_OF_MONTH);
}

public Date(Date d) {
  mYear=d.mYear;
  mMonth=d.mMonth;
  mDay=d.mDay;
  mCalendar=Calendar.getInstance();
  mCalendar.set(Calendar.YEAR,mYear);
  mCalendar.set(Calendar.MONTH,mMonth-1);
  mCalendar.set(Calendar.DAY_OF_MONTH,mDay);
  
}
  
  

  
  
  public static Date getCurrentDate() {
    return DATE;
  }
  
  /**
   * Creates a new instance from a stream.
   */
  public Date(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    int version = in.readInt();
    if (version==1) {
      int date=in.readInt();
        // TODO:
      long l=(long)date*24*60*60*1000;
      java.util.Date d=new java.util.Date(l);
      mCalendar=Calendar.getInstance();
      mCalendar.setTime(d);
      mYear=mCalendar.get(Calendar.YEAR);
      mMonth=mCalendar.get(Calendar.MONTH)+1;
      mDay=mCalendar.get(Calendar.DAY_OF_MONTH);
    }
    else {
      mYear=in.readInt();
      mMonth=in.readInt();
      mDay=in.readInt();
      mCalendar=Calendar.getInstance();  
      mCalendar.set(Calendar.YEAR,mYear);
      mCalendar.set(Calendar.MONTH,mMonth-1);
      mCalendar.set(Calendar.DAY_OF_MONTH,mDay);
    }
    
  }

  
  
  /**
   * Writes this instance to a stream.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version
    out.writeInt(mYear);
    out.writeInt(mMonth);
    out.writeInt(mDay);
  }
  
  
  
  /**
   * A hash code implementation that returns the same code for equal Dates.
   */
  
  public int hashCode() {
    return mCalendar.hashCode(); 
  }
  
  public String getDateString() {
    
   // return ""+mYear+ (mMonth<10?"0":"") + mMonth + (mDay<10?"0":"") + mDay;
    return ""+getValue();
    
  }
  
  
  public long getValue() {
    return mYear*10000 + mMonth*100 + mDay;
  }

  public boolean equals(Object obj) {
    
    if (obj instanceof Date) {
        Date d=(Date)obj;
        return d.getValue()==getValue();
     // return (d.mMonth==mMonth && d.mYear==mYear && d.mDay==mDay);
    } else {
      
      return false;
      
    }
    
  }


  public java.util.Calendar getCalendar() {
    return mCalendar;

  }

  
  
  public String toString() {
    
    //return mDay+".";
    
    
    //java.util.Calendar cal = getCalendar();
    int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK) - 1;
    int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
    int month = mCalendar.get(Calendar.MONTH);  

    return mLocalizer.msg("datePattern", "{0}, {1} {2}", DAY_MSG_ARR[dayOfWeek],
      MONTH_MSG_ARR[month], Integer.toString(dayOfMonth));
  }

  /**
   * @deprecated
   */   
  
  public int getDaysSince1970() {
    int zoneOffset = mCalendar.get(Calendar.ZONE_OFFSET);
    int daylight = mCalendar.get(Calendar.DST_OFFSET);
    long millis = System.currentTimeMillis() + zoneOffset + daylight;
    return (int) (millis / 1000L / 60L / 60L / 24L);     
  }
  
  
       
       
  
  public void setMonth(int month) {
    mMonth=month;
    mCalendar.set(Calendar.MONTH,mMonth-1);
  }
  
  public void setYear(int year) {
    mYear=year;
    mCalendar.set(Calendar.YEAR,mYear);
  }
  
  public void setDay(int day) {
    mDay=day;
    mCalendar.set(Calendar.DAY_OF_MONTH,mDay);
  }


  public void addDays(int days) {
     addDaysToDate(days);     
   }
   
   
   
/*
  public Date addDays(int days) {
    Date result=new Date(this);
    result.addDaysToDate(days);
    return result;
  }
*/
  private void addDaysToDate(int days) {    
    mCalendar.add(Calendar.DAY_OF_MONTH, days);
    mYear=mCalendar.get(Calendar.YEAR);
    mMonth=mCalendar.get(Calendar.MONTH)+1;
    mDay=mCalendar.get(Calendar.DAY_OF_MONTH);
  }

  public int compareTo(Object obj) {
    Date d=(Date)obj;
    
    long val=d.getValue();
    long thisVal=getValue();
    
    if (thisVal<val) {
      return -1;
    }
    else if (thisVal>val) {
      return 1;
    }
    return 0;
    
   
  }

}