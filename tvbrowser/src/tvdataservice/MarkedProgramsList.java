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
package tvdataservice;

import java.util.ArrayList;

import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * A class that contains all marked programs.
 *
 * @author René Mach
 * @since 2.2
 * @deprecated since 3.3.4 use {@link MarkedProgramsMap} instead.
 */
public class MarkedProgramsList {

  private static MarkedProgramsList mInstance;

  private MarkedProgramsList() {
    mInstance = this;
  }

  /**
   * @return The instance of this class.
   * @deprecated since 3.3.4 use {@link MarkedProgramsMap#getInstance()} instead.
   */
  public static synchronized MarkedProgramsList getInstance() {
    if(mInstance == null) {
      new MarkedProgramsList();
    }
    return mInstance;
  }

  /**
   * @return The marked programs.
   * @deprecated since 3.3.4 use {@link MarkedProgramsMap#getMarkedPrograms()} instead.
   */
  public Program[] getMarkedPrograms() {
    return MarkedProgramsMap.getInstance().getMarkedPrograms();
  }

  /**
   * @param filter The filter to use for program filtering
   * @param markPriority The minimum mark priority of programs to find.
   * @param numberOfPrograms The number of programs to show. Use a value of 0 or below for all important programs.
   * @param includeOnAirPrograms If the marked programs array should contain running programs.
   * @return The time sorted programs for the tray.
   * @deprecated since 3.3.4 use {@link MarkedProgramsMap#getTimeSortedProgramsForTray(ProgramFilter, int, int, boolean)}
   */
  public Program[] getTimeSortedProgramsForTray(ProgramFilter filter, int markPriority, int numberOfPrograms, boolean includeOnAirPrograms) {
    return MarkedProgramsMap.getInstance().getTimeSortedProgramsForTray(filter, markPriority, numberOfPrograms, includeOnAirPrograms);
  }

  /**
   * @param filter The filter to use for program filtering
   * @param markPriority The minimum mark priority of programs to find.
   * @param numberOfPrograms The number of programs to show. Use a value of 0 or below for all important programs.
   * @param includeOnAirPrograms If the marked programs array should contain running programs.If the tray filter settings should be used for filtering.
   * @param useTrayFilterSettings If the tray filter settings should be used for filtering.
   * @param excludePrograms The list with excluded programs.
   * @return The time sorted programs for the tray.
   * @deprecated since 3.3.4 use {@link MarkedProgramsMap#getTimeSortedProgramsForTray(ProgramFilter, int, int, boolean, boolean, ArrayList)} instead.
   */
  public Program[] getTimeSortedProgramsForTray(ProgramFilter filter, int markPriority, int numberOfPrograms, boolean includeOnAirPrograms, boolean useTrayFilterSettings, ArrayList<Program> excludePrograms) {
    return MarkedProgramsMap.getInstance().getTimeSortedProgramsForTray(filter, markPriority, numberOfPrograms, includeOnAirPrograms, useTrayFilterSettings, excludePrograms);
  }

  /**
   * Does nothing anymore.
   * @deprecated since 3.3.4 use {@link MarkedProgramsMap#revalidatePrograms()} instead.
   */
  public void revalidatePrograms() {}
}
