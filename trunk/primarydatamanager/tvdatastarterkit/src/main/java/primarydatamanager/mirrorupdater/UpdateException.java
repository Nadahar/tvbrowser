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
 *     $Date: 2003-10-09 11:57:31 +0200 (Do, 09 Okt 2003) $
 *   $Author: til132 $
 * $Revision: 230 $
 */
package primarydatamanager.mirrorupdater;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class UpdateException extends Exception {

  public UpdateException(String msg) {
    super(msg);
  }

  public UpdateException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
