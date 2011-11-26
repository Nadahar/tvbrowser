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
 *     $Date: 2010-07-09 19:22:09 +0200 (Fr, 09 Jul 2010) $
 *   $Author: jo_mormes $
 * $Revision: 6676 $
 */

package primarydatamanager.primarydataservice;

import java.io.PrintStream;
import java.util.Properties;

/**
 * Gets the raw TV data for one or more TV channels.
 * 
 * @author Martin Oberhauser
 */
public interface PrimaryDataService {

  /**
   * Gets the raw TV data and writes it to a directory
   * 
   * @param dir The directory to write the raw TV data to.
   * @param err The stream to print error messages to.
   * @return Whether there were errors.
   */
  public boolean execute(String dir, PrintStream err);
  
  /**
   * Sets parameters that might be read by the PDS.
   * 
   * @param parameters
   */
  public void setParameters(Properties parameters);

  /**
   * Gets the number of bytes read (= downloaded) by this data service.
   * 
   * @return The number of bytes read.
   */
  public int getReadBytesCount();


}