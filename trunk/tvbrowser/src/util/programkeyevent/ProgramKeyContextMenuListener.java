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
 *     $Date: 2013-06-17 04:02:08 +0200 (Mo, 17 Jun 2013) $
 *   $Author: ds10 $
 * $Revision: 7853 $
 */
package util.programkeyevent;

import devplugin.Program;

/**
 * An interface that is used to track popup key events.
 * Use this for {@link ProgramKeyEventHandler} if you want to be informed about a popup menu event. 
 * <p>
 * @author René Mach
 * @since 3.3.1
 */
public interface ProgramKeyContextMenuListener {
  /**
   * Show a context menu on the triggered program.
   * <p>
   * @param program The program that has triggered the popup event.
   */
  public void showContextMenu(Program program);
}
