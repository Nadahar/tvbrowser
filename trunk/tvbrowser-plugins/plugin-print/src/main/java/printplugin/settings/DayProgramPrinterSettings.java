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
 *     $Date: 2006-10-18 16:47:20 +0200 (Mi, 18 Okt 2006) $
 *   $Author: ds10 $
 * $Revision: 2755 $
 */

package printplugin.settings;

import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgramFilter;


/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 24.04.2005
 * Time: 14:20:26
 */

public class DayProgramPrinterSettings implements Settings {

  public static final int BREAK_TYPE_NEWPAGE = 1;
  public static final int BREAK_TYPE_HALFPAGE = 2;
  public static final int BREAK_TYPE_NONE = 3;

  private Date mFromDay;
  private int mNumberOfDays;
  private Channel[] mChannelList;
  private int mDayStartHour;
  private int mDayEndHour;
  private int mColCount;
  private int mChannelsPerColum;
  private ProgramIconSettings mProgramIconSettings;
  private ProgramFilter mProgramFilter;

  public DayProgramPrinterSettings(Date fromDay,
                                       int numberOfDays,
                                       Channel[] channelList,
                                       int dayStartHour,
                                       int dayEndHour,
                                       int colCount,
                                       int channelsPerColumn,
                                       ProgramIconSettings programIconSettings,
                                       ProgramFilter filter) {
    mFromDay = fromDay;
    mNumberOfDays = numberOfDays;
    mChannelList = channelList;
    mDayStartHour = dayStartHour;
    mDayEndHour = dayEndHour;
    mColCount = colCount;
    mChannelsPerColum = channelsPerColumn;
    mProgramIconSettings = programIconSettings;
    mProgramFilter = filter;
  }
  
  public Date getFromDay() {
    return mFromDay;
  }

  public int getNumberOfDays() {
    return mNumberOfDays;
  }

  public Channel[] getChannelList() {
    return mChannelList;
  }

  public int getDayStartHour() {
    return mDayStartHour;
  }

  public int getDayEndHour() {
    return mDayEndHour;
  }

  public int getColumnCount() {
    return mColCount;
  }

  public int getChannelsPerColumn() {
    return mChannelsPerColum;
  }
  
  
  public ProgramIconSettings getProgramIconSettings() {
    return mProgramIconSettings;
  }
  
  public ProgramFilter getProgramFilter() {
    return mProgramFilter;
  }
}
