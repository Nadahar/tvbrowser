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

import java.util.Calendar;

import util.io.IOUtilities;

public class Date implements java.io.Serializable, Comparable {

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
  
  
  
  private int date;  // days since 70-01-01

  /**
   * Constructs a new Date object, initialized with the current date.
   */
  public Date() {
    date = IOUtilities.getDaysSince1970();
  }

  public Date(int daysSince1970) {
    date=daysSince1970;
  }
  
  
  /**
   * A hash code implementation that returns the same code for equal Dates.
   */
  public int hashCode() {
    return date;
  }
  
  

  public boolean equals(Object obj) {
    if (obj instanceof Date) {
      return ((Date) obj).date == this.date;
    } else {
      return false;
    }
  }



  public java.util.Calendar getCalendar() {
    java.util.Calendar result=new java.util.GregorianCalendar();

    long l=(long)date*24*60*60*1000;

    java.util.Date d=new java.util.Date(l);
    result.setTime(d);

    return result;
  }

  
  
  public String toString() {
    java.util.Calendar cal = getCalendar();
    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
    int month = cal.get(Calendar.MONTH);

    return mLocalizer.msg("datePattern", "{0}, {1} {2}", DAY_MSG_ARR[dayOfWeek],
      MONTH_MSG_ARR[month], Integer.toString(dayOfMonth));
  }

  
  
  public long getDaysSince1900() {
    return date+25569;
  }

  public int getDaysSince1970() {
    return date;
  }

  public void addDays(int days) {
    date+=days;
  }

  public int compareTo(Object obj) {
    Date d=(Date)obj;
    if (date<d.getDaysSince1970()) {
      return -1;
    }else if (date>d.getDaysSince1970()) {
      return 1;
    }
    return 0;
  }

}