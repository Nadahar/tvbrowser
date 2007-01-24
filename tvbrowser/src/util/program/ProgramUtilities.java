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
 */
package util.program;

import util.io.IOUtilities;
import devplugin.Date;
import devplugin.Program;

import java.util.Comparator;

import tvbrowser.core.ChannelList;

/**
 * Provides utilities for program stuff.
 * 
 * @author René Mach
 *
 */
public class ProgramUtilities {

  /**
   * Helper method to check if a program runs.
   * 
   * @param p
   *          The program to check.
   * @return True if the program runs.
   */
  public static boolean isOnAir(Program p) {
    int time = IOUtilities.getMinutesAfterMidnight();
    Date currentDate = Date.getCurrentDate();
	if (currentDate.addDays(-1).compareTo(p.getDate()) == 0)
      time += 24 * 60;
    if (currentDate.compareTo(p.getDate()) < 0)
      return false;

    if (p.getStartTime() <= time && (p.getStartTime() + p.getLength()) > time)
      return true;
    return false;
  }

  public static Comparator<Program> getProgramComparator() {
    return sProgramComparator;
  }

  /**
   * Comparator to sort programs by date, time and channel 
   */
  private static Comparator<Program> sProgramComparator = new Comparator<Program>(){
    public int compare(Program p1, Program p2) {
      int res=p1.getDate().compareTo(p2.getDate());
      if (res!=0) return res;

      int minThis=p1.getStartTime();
      int minOther=p2.getStartTime();

      if (minThis<minOther) {
        return -1;
      }else if (minThis>minOther) {
        return 1;
      }

      int pos1 = ChannelList.getPos(p1.getChannel());
      int pos2 = ChannelList.getPos(p2.getChannel());
      if (pos1 < pos2) {
        return -1;
      }
      else if (pos1 > pos2) {
        return 1;
      }

      return 0;

    }
  };

  /**
   * A helper method to get if a program is not in a time range.
   * 
   * @param timeFrom The beginning of the time range to check
   * @param timeTo The ending of the time range
   * @param p The program to check
   * @return If the program is not in the given time range.
   * @since 2.2.2
   */
  public static boolean isNotInTimeRange(int timeFrom, int timeTo, Program p) {
    int timeFromParsed = timeFrom;

    if(timeFrom > timeTo)
      timeFromParsed -= 60*24;
    
    int startTime = p.getStartTime(); 
    
    if(timeFrom > timeTo && startTime >= timeFrom)
      startTime -= 60*24;
    
    return (startTime < timeFromParsed || startTime > timeTo);
  }
}
