/*
 * TV-Browser
 * Copyright (C) 2013 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date: 2017-03-04 23:44:53 +0100 (Sa, 04 Mär 2017) $
 *   $Author: ds10 $
 * $Revision: 8599 $
 */
package devplugin;

import java.awt.Component;

/**
 * Listener for tab events of TV-Browser main window.
 * <p>
 * @author René Mach
 * @since 3.4.5
 */
public interface TabListener {
  /**
   * Called whenever the tab was chosen by user to show.
   * <p>
   * @since 3.4.5
   */
  public void tabShown();
  
  /**
   * Called whenever the tab was currently the visible one
   * but the user had chosen another tab.
   * <p>
   * @param mostRecentFocusOwner The component that had the focus until most recently.
   * @since 3.4.5
   */
  public void tabHidden(Component mostRecentFocusOwner);
}
