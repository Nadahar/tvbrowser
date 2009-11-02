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

/**
 * TV-Browser
 * @author Martin Oberhauser
 */

package devplugin;

import java.io.DataInput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Date implements Comparable<Date> {

  public static util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(Date.class);

  private final int mYear;

  private final int mMonth;

  private final int mDay;
  
  private static final HashMap<Integer, String> LONG_DATE_MAP = new HashMap<Integer, String>(28);

  /**
   * Constructs a new Date object, initialized with the current date.
   */
  public Date() {
    Calendar mCalendar = Calendar.getInstance();
    mYear = mCalendar.get(Calendar.YEAR);
    mMonth = mCalendar.get(Calendar.MONTH) + 1;
    mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
  }

  public Date(int year, int month, int dayOfMonth) {
    mYear = year;
    mMonth = month;
    mDay = dayOfMonth;
  }

  public static Date createYYYYMMDD(String date, String separator) {
    if (date == null)
      return null;
    String[] s = date.split(separator);
    if (s.length != 3)
      return null;
    int year = Integer.parseInt(s[0]);
    int month = Integer.parseInt(s[1]);
    int day = Integer.parseInt(s[2]);
    return new Date(year, month, day);

  }

  public static Date createDDMMYYYY(String date, String separator) {
    if (date == null)
      return null;
    String[] s = date.split(separator);
    if (s.length != 3)
      return null;
    int day = Integer.parseInt(s[0]);
    int month = Integer.parseInt(s[1]);
    int year = Integer.parseInt(s[2]);
    return new Date(year, month, day);

  }

  public static Date createYYMMDD(String date, String separator) {
    return createYYYYMMDD("20" + date, separator);
  }

  /**
   * @param date
   *          The date to check for days since.
   * @return The days since the given date.
   * @since 0.9.7.4 This method may not return the exactly number of days since
   *        the calculation is confounded by daylight savings time
   *        switchovers... Around midnight the result may not be correct.
   */

  public int getNumberOfDaysSince(Date date) {
    Calendar cal_1 = date.getCalendar();
    java.util.Date utilDate_1 = cal_1.getTime();
    long millis_1 = utilDate_1.getTime();

    Calendar cal_2 = getCalendar();
    java.util.Date utilDate_2 = cal_2.getTime();
    long millis_2 = utilDate_2.getTime();
    
    int hours = (int)((millis_2 - millis_1) / 1000L / 60L / 60L);    
    
    if(hours % 24 > 1)
      return (int)(hours / 24. + 0.99);

    return (int) ((millis_2 - millis_1) / 1000L / 60L / 60L / 24L);

  }

  public int getYear() {
    return mYear;
  }

  public int getMonth() {
    return mMonth;
  }

  public int getDayOfMonth() {
    return mDay;
  }
  
  /**
   * Returns the week number within the current year.
   * @return The week number.
   *
   * @since 2.5.1
   */
  public int getWeekOfYear() {
    Calendar cal = getCalendar();
    return cal.get(Calendar.WEEK_OF_YEAR);
  } 

  public Date(Calendar cal) {
    mYear = cal.get(Calendar.YEAR);
    mMonth = cal.get(Calendar.MONTH) + 1;
    mDay = cal.get(Calendar.DAY_OF_MONTH);
  }

  public Date(Date d) {
    mYear = d.mYear;
    mMonth = d.mMonth;
    mDay = d.mDay;

  }

  /**
   * do not call this method repeatedly! always cache this value because this is
   * a very slow function!
   * 
   * @return
   */
  public static Date getCurrentDate() {
    return new Date();
  }

  /**
   * Creates a new instance from a RandomAccessFile.
   * @throws IOException 
   * @throws ClassNotFoundException 
   * 
   * @since 2.2
   */
  public Date(DataInput in) throws IOException, ClassNotFoundException {
    int version = in.readInt();
    if (version == 1) { // currently, version==2 is used
      int date = in.readInt();
      long l = (long) date * 24 * 60 * 60 * 1000;
      java.util.Date d = new java.util.Date(l);
      Calendar mCalendar = Calendar.getInstance();
      mCalendar.setTime(d);
      mYear = mCalendar.get(Calendar.YEAR);
      mMonth = mCalendar.get(Calendar.MONTH) + 1;
      mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
    } else {
      mYear = in.readInt();
      mMonth = in.readInt();
      mDay = in.readInt();
    }

  }
  
  /**
   * Creates a new instance from a stream.
   * @throws IOException 
   * @throws ClassNotFoundException 
   */
  public Date(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();
    if (version == 1) { // currently, version==2 is used
      int date = in.readInt();
      long l = (long) date * 24 * 60 * 60 * 1000;
      java.util.Date d = new java.util.Date(l);
      Calendar mCalendar = Calendar.getInstance();
      mCalendar.setTime(d);
      mYear = mCalendar.get(Calendar.YEAR);
      mMonth = mCalendar.get(Calendar.MONTH) + 1;
      mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
    } else {
      mYear = in.readInt();
      mMonth = in.readInt();
      mDay = in.readInt();
    }

  }

  /**
   * Writes this instance to a RandomAccessFile.
   * @param out 
   * @throws IOException 
   * 
   * @since 2.2
   */
  public void writeToDataFile(RandomAccessFile out) throws IOException {
    out.writeInt(2); // version
    out.writeInt(mYear);
    out.writeInt(mMonth);
    out.writeInt(mDay);
  }

  /**
   * Writes this instance to a stream.
   * @throws IOException 
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
    return mYear * 10000 + mMonth * 100 + mDay;

  }

  /**
   * return the textual representation of this date with abbreviated day of week
   * and abbreviated month name
   * 
   * @return date string
   */
  public String getDateString() {
    return String.valueOf(getValue());
  }
  
  private String getFormattedString(boolean longDay, boolean longMonth) {
    Calendar cal = getCalendar();
    
    SimpleDateFormat day = new SimpleDateFormat(longDay ? "EEEEEE" : "E");
    SimpleDateFormat month = new SimpleDateFormat(longMonth ? "MMMMMM" : "MMM");    
    
    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

    java.util.Date javaDate = new java.util.Date(getCalendar().getTimeInMillis());
    
    return mLocalizer.msg("datePattern", "{0}, {1} {2}", day.format(javaDate), month.format(javaDate), Integer
        .toString(dayOfMonth));
  }

  /**
   * return the textual representation of this date with full day of week
   * and full month name (neither abbreviated)
   * 
   * @return date string
   */
  public String getLongDateString() {
    int hashCode = hashCode();
    String result = LONG_DATE_MAP.get(hashCode);
    if (result == null) {
      result = getFormattedString(true,true);
      LONG_DATE_MAP.put(hashCode, result);
    }
    return result;
  }

  /**
   * returns the textual representation of this date with abbreviated day of week, but full month
   * @return date as string
   */
  public String getShortDayLongMonthString() {
    return getFormattedString(false,true);
  }

  public long getValue() {
    return mYear * 10000 + mMonth * 100 + mDay;
  }

  public static Date createDateFromValue(long value) {
    int year = (int) (value / 10000L);
    int month = (int) (value % 10000L / 100L);
    int day = (int) (value % 100L);

    return new Date(year, month, day);
  }

  public boolean equals(Object obj) {

    if (obj instanceof Date) {
      Date d = (Date) obj;
      return d.getDayOfMonth()==getDayOfMonth() && d.getMonth()==getMonth() && d.getYear()==getYear();
    }
    return false;

  }

  public java.util.Calendar getCalendar() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH, mMonth - 1);
    cal.set(Calendar.YEAR, mYear);
    cal.set(Calendar.DAY_OF_MONTH, mDay);

    return cal;
  }

  public String toString() {
    return getFormattedString(false,false);
  }

  public Date addDays(int days) {
    Calendar cal = getCalendar();
    cal.add(Calendar.DAY_OF_MONTH, days);
    return new Date(cal);
  }

  public int compareTo(Date otherDate) {
    if (this.mYear < otherDate.mYear) {
    	return -1;
    }
    else if (this.mYear > otherDate.mYear) {
    	return 1;
    }
    else if (this.mMonth < otherDate.mMonth) {
    	return -1;
    }
    else if (this.mMonth > otherDate.mMonth) {
    	return 1;
    }
    else if (this.mDay < otherDate.mDay) {
    	return -1;
    }
    else if (this.mDay > otherDate.mDay) {
    	return 1;
    }
    return 0;
  }
  
  /**
   * get the day of the week for this date
   * @return day of week, as Calendar.MONDAY and so on
   * @since 2.6
   */
  public int getDayOfWeek() {
    Calendar cal = getCalendar();
    return cal.get(Calendar.DAY_OF_WEEK);
  }

  /**
   * is this a Monday?
   * @return <code>true</code>, if this is the first day of the week
   * @since 2.6
   */
  public boolean isFirstDayOfWeek() {
    return getDayOfWeek() == Calendar.MONDAY;
  }

}