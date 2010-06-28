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
 *     $Date: 2006-03-25 12:16:57 +0100 (Sa, 25 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 2029 $
 */
package util.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import tvbrowser.core.Settings;

/**
 * Formats a Time to a String.
 * 
 * @author bodum
 */
public class TimeFormatter {
  private SimpleDateFormat mFormat;
  private Calendar mCal = Calendar.getInstance();

  /**
   * Use Format from the Settings
   */
  public TimeFormatter() {
    this(Settings.getTimePattern());
  }

  /**
   * @param format Format to use. See {@link SimpleDateFormat}.
   */
  public TimeFormatter(String format) {
    mFormat = new SimpleDateFormat(format);
    
    mCal.set(Calendar.HOUR_OF_DAY, 2);
    
    if(mCal.get(Calendar.HOUR_OF_DAY) == 3) {
      mCal.add(Calendar.DAY_OF_MONTH, 1);
    }
  }

  /**
   * Converts a Time into a String.
   * @param hours Hour (24-Hour-Format!)
   * @param minutes Minutes
   * @return String representation
   */
  public String formatTime(int hours, int minutes) {
    mCal.set(Calendar.HOUR_OF_DAY, hours);
    mCal.set(Calendar.MINUTE, minutes);
    return mFormat.format(mCal.getTime());
  }
}
