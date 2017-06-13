/*
 * TV-Browser Compat
 * Copyright (C) 2017 TV-Browser team (dev@tvbrowser.org)
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
 * SVN information:
 *     $Date: 2014-06-17 15:59:09 +0200 (Di, 17 Jun 2014) $
 *   $Author: ds10 $
 * $Revision: 8152 $
 */
package compat;

import devplugin.Version;
import tvbrowser.TVBrowser;

/**
 * Compatibility class for TV-Browser version infos.
 * 
 * @author René Mach
 * @since 0.1
 */
public final class VersionCompat {
  
  /**
   * @return <code>true</code> if the used TV-Browser is at least a 
   * TV-Browser 4, <code>false</code> otherwise.
   */
  public static boolean isAtLeastTvBrowser4() {
    return TVBrowser.VERSION.compareTo(new Version(3,44,96,false)) >= 0;
  }
  
  /**
   * @return <code>true</code> since TV-Browser 3.2, <code>false</code> otherwise.
   */
  public static boolean isCenterPanelSupported() {
    return TVBrowser.VERSION.compareTo(new Version(3,20,true)) >= 0;
  }

  /**
   * @return <code>true</code> since TV-Browser 3.2.1, <code>false</code> otherwise.
   */
  public static boolean isJointChannelSupported() {
    return TVBrowser.VERSION.compareTo(new Version(3,21,true)) >= 0;
  }
  
  /**
   * @return <code>true</code> since TV-Browser 3.3.4, <code>false</code> otherwise.
   */
  public static boolean isSortNumberSupported() {
    return TVBrowser.VERSION.compareTo(new Version(3,34,true)) >= 0;
  }
  
  /**
   * @return <code>true</code> since TV-Browser 3.3.1, <code>false</code> otherwise.
   */
  public static boolean isExtendedMouseActionSupported() {
    return TVBrowser.VERSION.compareTo(new Version(3, 31, true)) >= 0;
  }
}
