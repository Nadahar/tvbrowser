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

package devplugin;

import java.util.Iterator;

import util.exc.TvBrowserException;

/**
 * The PluginManager provides some usefull methods for a plugin.
 * Currently, two methods are implemented. More methods may follow.
 *
 * @author Martin Oberhauser
 */
public interface PluginManager {

  /**
   * Gets a program.
   * <p>
   * Returns null, if the specified program could not be found.
   */
  public devplugin.Program getProgram(devplugin.Date date, String progID);
  
  /**
   * Returns a JComponent containing information about the given program.
   */
  public javax.swing.JComponent createProgramPanel(Program prog);

  /**
   * Gets the subscribed channels.
   */
  public Channel[] getSubscribedChannels();
  
  /**
   * Gets an iterator through all programs of the specified channel at the
   * specified date.
   * <p>
   * If the requested data is not available, null is returned.
   */
  public Iterator getChannelDayProgram(devplugin.Date date, Channel channel);

  /**
   * Searches the data for programs which match a regular expression.
   *
   * @param regex The regular expression programs must match to.
   * @param inTitle Should be searched in the title?
   * @param inText Should be searched in the desription?
   * @param caseSensitive Should the search be case sensitive?
   * @param channels The channels to search in.
   * @param startDate The date to start the search.
   * @param nrDays The number of days to include after the start date. If
   *        negative the days before the start date are used.
   * @throws TvBrowserException If there is a syntax error in the regular expression.
   * @return The matching programs.
   */
  public Program[] search(String regex, boolean inTitle, boolean inText,
    boolean caseSensitive, Channel[] channels, devplugin.Date startDate,
    int nrDays)
    throws TvBrowserException;
  
}