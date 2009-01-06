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
package devplugin;

import javax.swing.DefaultListModel;


/**
 * A searcher that searches for a programs.
 * 
 * @author Til Schneider, www.murfman.de
 */
public interface ProgramSearcher {

  /**
   * Checks whether a field of a program matches to the criteria of this
   * searcher.
   * 
   * @param prog The program to check.
   * @param typeArr The fields to check.
   * @return Whether at least one field of the program matches.
   */
  public boolean matches(Program prog, ProgramFieldType[] typeArr);

  /**
   * Searches the TV data base for programs that match the criteria of this
   * searcher.
   * 
   * @param fieldArr
   *          The fields to search in
   * @param startDate
   *          The date to start the search.
   * @param nrDays
   *          The number of days to include after the start date. If this value
   *          is negative, then all days are searched (beginning with yesterday)
   *          and the startDate parameter is ignored.
   * @param channels
   *          The channels to search in. If this is <code>null</code>, then all
   *          subscribed channels are searched.
   * @param sortByStartTime
   *          Should the results be sorted by the start time? If not, the
   *          results will be grouped by date and channel and the search will be
   *          faster.
   * @return The matching programs.
   */
  public Program[] search(ProgramFieldType[] fieldArr, Date startDate,
      int nrDays, Channel[] channels, boolean sortByStartTime);

  /**
   * Searches the TV data base for programs that match the criteria of this
   * searcher.
   * 
   * @param fieldArr
   *          The fields to search in
   * @param startDate
   *          The date to start the search.
   * @param nrDays
   *          The number of days to include after the start date. If this value
   *          is negative, then all days are searched (beginning with yesterday)
   *          and the startDate parameter is ignored.
   * @param channels
   *          The channels to search in. If this is <code>null</code>, then all
   *          subscribed channels are searched.
   * @param sortByStartTime
   *          Should the results be sorted by the start time? If not, the
   *          results will be grouped by date and channel and the search will be
   *          faster.
   * @param progress
   *          progressMonitor for showing the search progress
   * @return The matching programs.
   */
  public Program[] search(ProgramFieldType[] fieldArr, Date startDate,
      int nrDays, Channel[] channels, boolean sortByStartTime, ProgressMonitor progress);

  /**
   * Searches the TV data base for programs that match the criteria of this
   * searcher.
   * 
   * @param fieldArr
   *          The fields to search in
   * @param startDate
   *          The date to start the search.
   * @param nrDays
   *          The number of days to include after the start date. If this value
   *          is negative, then all days are searched (beginning with yesterday)
   *          and the startDate parameter is ignored.
   * @param channels
   *          The channels to search in. If this is <code>null</code>, then all
   *          subscribed channels are searched.
   * @param sortByStartTime
   *          Should the results be sorted by the start time? If not, the
   *          results will be grouped by date and channel and the search will be
   *          faster.
   * @param progress
   *          progressMonitor for showing the search progress
   * @param listModel
   *          The list model the found programs should be stored in.
   * @return The matching programs.
   * @since 2.7
   */
  public Program[] search(ProgramFieldType[] fieldArr, Date startDate,
      int nrDays, Channel[] channels, boolean sortByStartTime, ProgressMonitor progress, DefaultListModel listModel);

}

