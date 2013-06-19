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

import java.awt.event.KeyEvent;

import devplugin.Program;

public interface ProgramKeyActionListener {
  /**
   * Gets the program to use for a key action.
   * <p>
   * @param e The key event that was triggered for a user keyboard interaction.
   * @return The program that was affected by the user keyboard interaction or <code>null</code> if no program was affected. 
   */
  public Program getProgramForKeyEvent(KeyEvent e);
  
  /**
   * Called when KeyEventAction was finished.
   */
  public void keyEventActionFinished();
}
