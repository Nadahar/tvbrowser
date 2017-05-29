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
package util.programkeyevent;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import compat.MenuCompat;
import compat.VersionCompat;
import devplugin.ContextMenuIf;
import devplugin.Program;
import tvbrowser.core.Settings;
import util.programmouseevent.ProgramMouseEventHandler;
import util.settings.ContextMenuMouseActionSetting;

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
          ProgramMouseEventHandler.handleAction(program, setting.getContextMenuIf().getContextMenuActions(program), setting.getContextMenuActionId());
        }
        break;
      }
    }
  }
  
  private static ContextMenuMouseActionSetting[] getSettingFor(final String property) {
    ContextMenuMouseActionSetting[] result = new ContextMenuMouseActionSetting[0];
        
    try {
      Class<?> clazz = Settings.class;
      Field prop = clazz.getDeclaredField(property);
      Object propObj = prop.get(clazz);
      Method m = propObj.getClass().getDeclaredMethod("getContextMenuMouseActionArray");
      result = (ContextMenuMouseActionSetting[])m.invoke(propObj);
    }catch(Exception e1) {}
    
    return result;
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
            ContextMenuMouseActionSetting[] actionSetting = null;
            
            if(VersionCompat.isExtendedMouseActionSupported()) {
              actionSetting = getSettingFor("propLeftSingleClickIfArray");
            }
            else {
              if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {                
                actionSetting = new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(KeyEvent.CTRL_DOWN_MASK, Settings.propLeftSingleCtrlClickIf.getString(), MenuCompat.ID_ACTION_NONE)};
              }
              else {
                actionSetting = new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(0, Settings.propLeftSingleClickIf.getString(), MenuCompat.ID_ACTION_NONE)};
              }
            }
            
            handleKeyEventFor(actionSetting, e, program);
          }
          else if(e.getKeyCode() == MIDDLE_SINGLE_KEY) {
            ContextMenuMouseActionSetting[] actionSetting = null;
            
            if(VersionCompat.isExtendedMouseActionSupported()) {
              actionSetting = getSettingFor("propMiddleSingleClickIfArray");
            }
            else {
              actionSetting = new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(0, Settings.propMiddleClickIf.getString(), MenuCompat.ID_ACTION_NONE)};
            }
            
            handleKeyEventFor(actionSetting, e, program);
          }
          else if(e.getKeyCode() == LEFT_DOUBLE_KEY) {
            ContextMenuMouseActionSetting[] actionSetting = null;
            
            if(VersionCompat.isExtendedMouseActionSupported()) {
              actionSetting = getSettingFor("propLeftDoubleClickIfArray");
            }
            else {
              actionSetting = new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(0, Settings.propDoubleClickIf.getString(), MenuCompat.ID_ACTION_NONE)};
            }
            
            handleKeyEventFor(actionSetting, e, program);
          }
          else if(e.getKeyCode() == MIDDLE_DOUBLE_KEY) {
            ContextMenuMouseActionSetting[] actionSetting = null;
            
            if(VersionCompat.isExtendedMouseActionSupported()) {
              actionSetting = getSettingFor("propMiddleDoubleClickIfArray");
            }
            else {
              actionSetting = new ContextMenuMouseActionSetting[] {new ContextMenuMouseActionSetting(0, Settings.propMiddleDoubleClickIf.getString(), MenuCompat.ID_ACTION_NONE)};
            }
            
            handleKeyEventFor(actionSetting, e, program);
          }
        }
      }
      
      mContextMenuListener.keyEventActionFinished();
      e.consume();
    }
  }
}
