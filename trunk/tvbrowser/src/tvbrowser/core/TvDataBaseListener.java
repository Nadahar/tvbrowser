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
package tvbrowser.core;

import tvdataservice.MutableChannelDayProgram;
import devplugin.ChannelDayProgram;

/**
 * A listener that listens for TV data events
 * 
 * @author Til Schneider, www.murfman.de
 */
public interface TvDataBaseListener {

  /**
   * Is called when a day program has been added to the
   * TV data base but not has been saved, so changes are
   * currently possible. Use this if your plugin wants
   * to change/add some data.
   * <p>
   * ATTENTION: Don't use it to mark or do something other
   * than data changing to the program.
   * @param prog The added day program.
   * @since 2.7
   */
  public void dayProgramAdded(MutableChannelDayProgram prog);

  /**
   * Is called when a day program has been added to the TV data base.
   * Use this if you want to mark programs or something else than
   * data chaging.
   * <p>
   * ATTENTION: If you change data of programs in here the changes
   * wont be kept because they will not be saved.
   * @param prog The added day program.
   */
  public void dayProgramAdded(ChannelDayProgram prog);

  /**
   * Is called when a day program has been deleted from the TV data base.
   * 
   * @param prog The deleted day program.
   */
  public void dayProgramDeleted(ChannelDayProgram prog);

  /**
   * Is called if something was done with the day program.
   * Maybe it was removed, added or changed. What actually happend
   * depends on the given parameter. This method is handy if you
   * want to use threads for the handling of the day program changes
   * and have to keep the order of the processing of removed and added
   * programs.
   * <p>
   * @param removedDayProgram The day program that was removed.
   * If it's <code>null</code> that means it didn't exist a day program
   * for the day and channel before (or short a new day program was added).
   * @param addedDayProgram The day program that was added.
   * If it's <code>null</code> that means the day program was deleted if
   * the removedDayProgram is not <code>null</code>.
   * <p>
   * @since 2.7.3
   */
  public void dayProgramTouched(ChannelDayProgram removedDayProgram, ChannelDayProgram addedDayProgram);
}
