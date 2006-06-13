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
 *     $Date: 2005-08-20 17:18:20 +0200 (Sa, 20 Aug 2005) $
 *   $Author: darras $
 * $Revision: 1443 $
 */
package devplugin;

/**
 * A interface for a object that supports receiving programs.
 * 
 * @author René Mach
 *
 */
public interface ProgramReceiveIf {
  
  /**
   * Gets whether the ProgramReceiveIf supports receiving programs from other plugins.
   * 
   * @return Whether the ProgramReceiveIf supports receiving programs from other plugins.
   * 
   * @see #receivePrograms(Program[])
   */
  public boolean canReceivePrograms();
  
  /**
   * Receives a list of programs from another plugin.
   * 
   * @param programArr The programs passed from the other plugin.
   * 
   * @see #canReceivePrograms()
   */
  public void receivePrograms(Program[] programArr);
  
  public String getId();
}
