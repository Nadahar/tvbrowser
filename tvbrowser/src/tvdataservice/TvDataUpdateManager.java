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
package tvdataservice;

import devplugin.Channel;
import devplugin.Date;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public interface TvDataUpdateManager {

  /**
   * Updates the given day program.
   * 
   * @param program The day program to update.
   */
  public void updateDayProgram(MutableChannelDayProgram program);
  
  /**
   * Gets whether a certain day program is available.
   * 
   * @param date The date of the day program to check.
   * @param channel The channel of the day program to check.
   * @return Whether the day program is available.
   */
  public boolean isDayProgramAvailable(Date date, Channel channel);

  /**
   * Returns whether the download should be canceled.
   * 
   * @return Whether the download should be canceled.
   */  
  public boolean cancelDownload();

}
