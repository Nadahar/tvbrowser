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
package mediacenterplugin;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * This Class is the KeyBoard Adapter. It maps Keyboard-Events to
 * MediaPanel-Functions
 * 
 * @author bodum
 */
public class KeyMediaAdapter extends KeyAdapter {

  /** MediaPanel to use */
  private MediaPanel mMediaPanel;

  /**
   * Creates the Adapter
   * @param mediaPanel Use this MediaPanel
   */
  public KeyMediaAdapter(MediaPanel mediaPanel) {
    mMediaPanel = mediaPanel;
  }

  /**
   * This Function calls the MediaPanel-Functions 
   */
  public void keyPressed(KeyEvent e) {
    if (e.getKeyChar() == '+') {
      mMediaPanel.nextDay();
    } else if (e.getKeyChar() == '-') {
      mMediaPanel.lastDay();
    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
      mMediaPanel.close();
    } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
      mMediaPanel.lastLineInDescription();
    } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
      mMediaPanel.nextLineInDescription();
    }
 }

}
