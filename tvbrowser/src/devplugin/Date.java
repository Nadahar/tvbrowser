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
import java.util.Calendar;

public class Date implements Comparable {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(Date.class);

  private static final String[] DAY_MSG_ARR = { mLocalizer.msg("day.0", "So"), mLocalizer.msg("day.1", "Mo"),
      mLocalizer.msg("day.2", "Tu"), mLocalizer.msg("day.3", "We"), mLocalizer.msg("day.4", "Th"),
      mLocalizer.msg("day.5", "Fr"), mLocalizer.msg("day.6", "Sa") };

  private static final String[] MONTH_MSG_ARR = { mLocalizer.msg("month.1", "Jan"), mLocalizer.msg("month.2", "Feb"),
      mLocalizer.msg("month.3", "Mar"), mLocalizer.msg("month.4", "Apr"), mLocalizer.msg("month.5", "May"),
      mLocalizer.msg("month.6", "Jun"), mLocalizer.msg("month.7", "Jul"), mLocalizer.msg("month.8", "Aug"),
      mLocalizer.msg("month.9", "Sep"), mLocalizer.msg("month.10", "Oct"), mLocalizer.msg("month.11", "Nov"),
      mLocalizer.msg("month.12", "Dec") };

  private static final String[] LONG_DAY_MSG_ARR = { mLocalizer.msg("day.0_long", "So"), mLocalizer.msg("day.1_long", "Mo"),
      mLocalizer.msg("day.2_long", "Tu"), mLocalizer.msg("day.3_long", "We"), mLocalizer.msg("day.4_long", "Th"),
      mLocalizer.msg("day.5_long", "Fr"), mLocalizer.msg("day.6_long", "Sa") };

  private static final String[] LONG_MONTH_MSG_ARR = { mLocalizer.msg("month.1_long", "Jan"), mLocalizer.msg("month.2_long", "Feb"),
      mLocalizer.msg("month.3_long", "Mar"), mLocalizer.msg("month.4_long", "Apr"), mLocalizer.msg("month.5_long", "May"),
      mLocalizer.msg("month.6_long", "Jun"), mLocalizer.msg("month.7_long", "Jul"), mLocalizer.msg("month.8_long", "Aug"),
      mLocalizer.msg("month.9_long", "Sep"), mLocalizer.msg("month.10_long", "Oct"), mLocalizer.msg("month.11_long", "Nov"),
      mLocalizer.msg("month.12_long", "Dec") };

  private final int mYear;

  private final int mMonth;

  private final int mDay;

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
   * Attention: DO NOT USE THIS!
   * Under Os/2 it has some problems with calculating the real Date!
   * 
   * @deprecated
   */
  public Date(int daysSince1970) {

    long l = (long) daysSince1970 * 24 * 60 * 60 * 1000;
    java.util.Date d = new java.util.Date(l);
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    mYear = cal.get(Calendar.YEAR);
    mMonth = cal.get(Calendar.MONTH) + 1;
    mDay = cal.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * @param d The date to check for days since.
   * @return The days since the given date.
   * @since 0.9.7.4 This method may not return the exactly number of days since
   *        the calculation is confounded by daylight savings time
   *        switchovers... Around midnight the result may not be correct.
   */

  public int getNumberOfDaysSince(Date d) {
    Calendar cal_1 = d.getCalendar();
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

  public static Date getCurrentDate() {
    return new Date();
  }

  /**
   * Creates a new instance from a RandomAccessFile.
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

  public String getDateString() {
    return String.valueOf(getValue());
  }

  public String getLongDateString() {
    Calendar cal = getCalendar();
    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
    int month = cal.get(Calendar.MONTH);

    return mLocalizer.msg("datePattern", "{0}, {1} {2}", LONG_DAY_MSG_ARR[dayOfWeek], LONG_MONTH_MSG_ARR[month], Integer
        .toString(dayOfMonth));
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
    Calendar cal = getCalendar();
    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
    int month = cal.get(Calendar.MONTH);

    return mLocalizer.msg("datePattern", "{0}, {1} {2}", DAY_MSG_ARR[dayOfWeek], MONTH_MSG_ARR[month], Integer
        .toString(dayOfMonth));
  }

  /**
   * @deprecated
   */
  public int getDaysSince1970() {
    Calendar cal = getCalendar();
    int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
    int daylight = cal.get(Calendar.DST_OFFSET);
    java.util.Date utilDate = cal.getTime();
    long millis = utilDate.getTime() + zoneOffset + daylight;
    return (int) (millis / 1000L / 60L / 60L / 24L);
  }

  public Date addDays(int days) {
    Calendar cal = getCalendar();
    cal.add(Calendar.DAY_OF_MONTH, days);
    return new Date(cal);
  }

  public int compareTo(Object obj) {
    Date d = (Date) obj;
    if (this.mYear < d.mYear) {
    	return -1;
    }
    else if (this.mYear > d.mYear) {
    	return 1;
    }
    else if (this.mMonth < d.mMonth) {
    	return -1;
    }
    else if (this.mMonth > d.mMonth) {
    	return 1;
    }
    else if (this.mDay < d.mDay) {
    	return -1;
    }
    else if (this.mDay > d.mDay) {
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
   * @return
   * @since 2.6
   */
  public boolean isFirstDayOfWeek() {
    return getDayOfWeek() == Calendar.MONDAY;
  }

}