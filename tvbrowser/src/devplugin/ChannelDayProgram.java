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

package devplugin;

/**
 * A list of the programs of one channel and one day.
 *
 * @author Til Schneider, www.murfman.de
 */
public interface ChannelDayProgram {
  
  /**
   * Returns the channel of this day program.
   *
   * @return  the channel of this day program.
   */
  public Channel getChannel();

  /**
   * Returns the date of this day program.
   *
   * @return  the date of this day program.
   */
  public Date getDate();

  /**
   * Returns the first program object having the specified ID.
   *
   * @param progID The ID of the wanted program.
   * @return  the program object having the specified ID.
   */
  public Program getProgram(String progID);
  
  /**
   * Returns all program objects having the specified ID.
   * (Well this is stupid but if programs start at the same
   * time on the same channel on the same date they have the same id.)
   * 
   * @param progID The ID of the wanted programs.
   * @return The array with the program objects having the specified ID.
   * @since 3.3.3
   */
  public Program[] getPrograms(String progID);

  /**
   * Gets the number of programs in this list.
   *
   * @return the number of programs.
   */
  public int getProgramCount();

  /**
   * Returns the program at the specified index.
   *
   * @param index The index of the wanted program.
   * @return The program at the specified index.
   */
  public Program getProgramAt(int index);

  /**
   * Returns an iterator containing all programms. Each iterator item is a
   * devplugin.Program object.
   *
   * @return An iterator through the program list.
   */
  public java.util.Iterator<Program> getPrograms();

  /**
   * Returns whether this channel day program is complete.
   * <p>
   * @return true if the last program ends afer midnight. Future implementations
   * may check for gaps too.
   */
  public boolean isComplete();
  
  /**
   * Gets the last program end time state on data update.
   * 
   * @return If the last program had ent time on data update
   * @since 2.2
   */
  public boolean getLastProgramHadEndOnUpdate();
  
}
