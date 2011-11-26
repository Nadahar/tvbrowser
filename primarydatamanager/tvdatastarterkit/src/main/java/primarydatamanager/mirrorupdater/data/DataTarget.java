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
 *     $Date: 2006-11-29 11:53:20 +0100 (Mi, 29 Nov 2006) $
 *   $Author: ds10 $
 * $Revision: 2914 $
 */
package primarydatamanager.mirrorupdater.data;

import primarydatamanager.mirrorupdater.UpdateException;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public interface DataTarget {
 
  public String[] listFiles() throws UpdateException;
  
  public void deleteFile(String fileName) throws UpdateException;
  
  public void writeFile(String fileName, byte[] data) throws UpdateException;
  
  public void close() throws UpdateException;
}
