/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package printplugin.settings;

import devplugin.Date;
import devplugin.Channel;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.awt.print.PageFormat;

public class DayProgramScheme extends Scheme {

 /* private int mFromDay;
  private int mDayCount;
  private Channel[] mChannels;
  private int mDayStartHour;
  private int mDayEndHour;

  private PageFormat mPageFormat;  */
  private DayProgramPrinterSettings mSettings;

  public DayProgramScheme(String name) {
    super(name);
  }

  public void store(ObjectOutputStream out) throws IOException {

  }

  public void read(ObjectInputStream in) throws IOException {

  }
      /*
  public void setFromDay(int from) {
    mFromDay = from;
  }

  public void setDayCount(int days) {
    mDayCount = days;
  }

  public void setChannels(Channel[] channels) {
    mChannels = channels;
  }

  public void setDayStartHour(int dayStartHour) {
    mDayStartHour = dayStartHour;
  }

  public void setDayEndHour(int dayEndHour) {
    mDayEndHour = dayEndHour;
  }

  public void setPageFormat(PageFormat format) {
    mPageFormat = format;
  }

  public PageFormat getPageFormat() {
    return mPageFormat;
  }   */

  public void setSettings(DayProgramPrinterSettings settings) {
    mSettings = settings;
  }

  public DayProgramPrinterSettings getSettings() {
    return mSettings;
  }
                     /*
  public DayProgramPrinterSettings getSettings() {
    return new DayProgramPrinterSettings(){
      public Date getFromDay() {
        return new Date().addDays(mFromDay);
      }

      public int getNumberOfDays() {
        return mDayCount;
      }

      public Channel[] getChannelList() {
        return mChannels;
      }

      public int getDayStartHour() {
        return mDayStartHour;
      }

      public int getDayEndHour() {
        return mDayEndHour;
      }

      public int getColumnCount() {
        return 5;
      }

      public int getChannelsPerColumn() {
        return 2;
      }

      public PageFormat getPageFormat() {
        return mPageFormat;
      }

    };
  }   */
}
