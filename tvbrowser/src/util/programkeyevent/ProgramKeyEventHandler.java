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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import tvbrowser.core.Settings;
import util.programmouseevent.ProgramMouseEventHandler;
import util.settings.ContextMenuMouseActionSetting;

import devplugin.ContextMenuIf;
import devplugin.Program;

/**
 * A class that handles keyboard interactions on programs.
 * Use this in your Plugin if you want to react to keyboard events like the program table.
 * <p>
 * @author René Mach
 * @since 3.3.1
 */
public class ProgramKeyEventHandler extends KeyAdapter {
  public static final int LEFT_SINGLE_KEY = KeyEvent.VK_L;
  public static final int LEFT_DOUBLE_KEY = KeyEvent.VK_D;
  public static final int MIDDLE_SINGLE_KEY = KeyEvent.VK_M;
  public static final int MIDDLE_DOUBLE_KEY = KeyEvent.VK_U;
  
  private ProgramKeyAndContextMenuListener mContextMenuListener;
  private ContextMenuIf mOwner;
  /**
   * Create a new instance of this class to handle mouse events on programs.
   * <p>
   * @param listener The listener to react to.
   * @param owner The ContextMenuIf that wants to react on mouse events.
   */
  public ProgramKeyEventHandler(ProgramKeyAndContextMenuListener listener, ContextMenuIf owner) {
    mContextMenuListener = listener;
    mOwner = owner;
  }
  
  private void handleKeyEventFor(ContextMenuMouseActionSetting[] mouseActionSettings, KeyEvent e, Program program) {
    for(ContextMenuMouseActionSetting setting : mouseActionSettings) {
      if(setting.getModifiersEx() == e.getModifiersEx()) {
        if(setting.getContextMenuIf() != mOwner) {
          ProgramMouseEventHandler.handleAction(program, setting.getContextMenuIf().getContextMenuActions(program));
        }
        break;
      }
    }
  }
  
  @Override
  public void keyPressed(KeyEvent e) {
    if(e.getKeyCode() == LEFT_SINGLE_KEY || e.getKeyCode() == MIDDLE_SINGLE_KEY || e.getKeyCode() == LEFT_DOUBLE_KEY
      || e.getKeyCode() == MIDDLE_DOUBLE_KEY || e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU || e.getKeyCode() == KeyEvent.VK_R) {
      Program program = mContextMenuListener.getProgramForKeyEvent(e);
      
      if(program != null) {
        if(e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU || e.getKeyCode() == KeyEvent.VK_R) {
          mContextMenuListener.showContextMenu(program);
        }
        else {
          if(e.getKeyCode() == LEFT_SINGLE_KEY) {
            handleKeyEventFor(Settings.propLeftSingleClickIfArray.getContextMenuMouseActionArray(), e, program);
          }
          else if(e.getKeyCode() == MIDDLE_SINGLE_KEY) {
            handleKeyEventFor(Settings.propMiddleSingleClickIfArray.getContextMenuMouseActionArray(), e, program);
          }
          else if(e.getKeyCode() == LEFT_DOUBLE_KEY) {
            handleKeyEventFor(Settings.propLeftDoubleClickIfArray.getContextMenuMouseActionArray(), e, program);
          }
          else if(e.getKeyCode() == MIDDLE_DOUBLE_KEY) {
            handleKeyEventFor(Settings.propMiddleDoubleClickIfArray.getContextMenuMouseActionArray(), e, program);
          }
        }
      }
      
      mContextMenuListener.keyEventActionFinished();
      e.consume();
    }
  }
}
