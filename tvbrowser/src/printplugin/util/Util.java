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

package printplugin.util;

import java.util.Arrays;
import java.util.Comparator;

import tvbrowser.core.ChannelList;
import devplugin.Program;


public class Util {

  public static void sortProgramsByDateAndChannel(Program[] progArr) {
    Arrays.sort(progArr, new Comparator(){
      public int compare(Object o1, Object o2) {
        if (o1 instanceof Program && o2 instanceof Program) {
          Program p1 = (Program)o1;
          Program p2 = (Program)o2;
          int result = p1.getDate().compareTo(p2.getDate());
          if (result != 0) {
            return result;
          }

          int minutes1 = p1.getHours()*60+p1.getMinutes();
          int minutes2 = p2.getHours()*60+p2.getMinutes();
          if (minutes1<minutes2) {
            return -1;
          }
          else if (minutes1>minutes2) {
            return 1;
          }

          int pos1 = ChannelList.getPos(p1.getChannel());
          int pos2 = ChannelList.getPos(p2.getChannel());
          if (pos1 < pos2) {
            return -1;
          }
          else if (pos1 > pos2) {
            return +1;
          }
          else {
            return 0;
          }
        }
        return 0;
      }

    });
  }

}
