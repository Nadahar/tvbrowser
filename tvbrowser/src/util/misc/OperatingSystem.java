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
package util.misc;

import util.browserlauncher.Launch;

/**
 * Checks which OS is used.
 * 
 * Uses the BrowserLauncher-Detection-Code
 * 
 * @author bodum
 * @since 2.2
 */
public class OperatingSystem {
  /**
   * @return true, if the Operating System is Windows
   */
  public static boolean isWindows() {
    return Launch.getOs() == Launch.OS_WINDOWS;
  }
  
  /**
   * @return true, if the Operating System is MacOS
   */
  public static boolean isMacOs() {
    return Launch.getOs() == Launch.OS_MAC;
  }
  
  /**
   * @return <code>True</code>, if the Operation System is Linux.
   * @since 2.2.4/2.6
   */
  public static boolean isLinux() {
    return Launch.getOs() == Launch.OS_LINUX;
  }
  
  /**
   * @return true, if the Operating System is not Windows or MacOS
   */
  public static boolean isOther() {
    return Launch.getOs() == Launch.OS_OTHER;
  }
  
}