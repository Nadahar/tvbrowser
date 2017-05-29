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
 *     $Date: 2017-03-04 19:20:18 +0100 (Sa, 04 Mär 2017) $
 *   $Author: ds10 $
 * $Revision: 8598 $
 */
package util.programmouseevent;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import compat.MenuCompat;
import compat.VersionCompat;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;
import tvbrowser.core.contextmenu.ContextMenuManager;

/**
 * A class that handles mouse interactions on programs.
 * Use this in your Plugin if you want to react to mouse events like the program table.
 * <p>
 * @author René Mach
 * @since 3.3.1
 */
public class ProgramMouseEventHandler extends MouseAdapter {
  private ProgramMouseAndContextMenuListener mContextMenuListener;
  private ContextMenuIf mOwner;
  
  private Thread mSingleClickThread;
  private boolean mPerformingSingleClick = false;
  
  /**
   * Create a new instance of this class to handle mouse events on programs.
   * <p>
   * @param listener The listener to react to.
   * @param owner The ContextMenuIf that wants to react on mouse events.
   */
  public ProgramMouseEventHandler(ProgramMouseAndContextMenuListener listener, ContextMenuIf owner) {
    mContextMenuListener = listener;
    mOwner = owner;
  }
  
  @Override
  public void mouseClicked(MouseEvent e) {
    if(mContextMenuListener instanceof ProgramMouseActionListener) {
      Program prog = mContextMenuListener.getProgramForMouseEvent(e);
      
      if (e.getClickCount() == 1) {
        performSingleClick(prog, e);
      }
      else if (e.getClickCount() == 2) {
        performDoubleClick(prog, e);
      }
    }
  }
  
  private void performSingleClick(final Program prog, final MouseEvent e) {
    mSingleClickThread = new Thread("Single click") {
      public void run() {
        try {
          mPerformingSingleClick = false;
          sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
          mPerformingSingleClick = true;
          handleProgramClick(prog, mOwner, true, e);
          mPerformingSingleClick = false;
        } catch (InterruptedException ex) { // ignore
        }
      }
    };
    
    mSingleClickThread.setPriority(Thread.MIN_PRIORITY);
    mSingleClickThread.start();
  }
    
  private void performDoubleClick(final Program prog, final MouseEvent e) {
    if(!mPerformingSingleClick && mSingleClickThread != null && mSingleClickThread.isAlive()) {
      mSingleClickThread.interrupt();
    }
    
    if(!mPerformingSingleClick) {
      handleProgramClick(prog, mOwner, false, e);
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    testAndHandleContextMenuEvent(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    testAndHandleContextMenuEvent(e);
  }
  
  private void testAndHandleContextMenuEvent(MouseEvent e) {
    if(mContextMenuListener instanceof ProgramContextMenuListener && e.isPopupTrigger()) {
      mContextMenuListener.showContextMenu(e);
    }
  }
  
  /**
   * Handles a program mouse click.
   * <p>
   * @param program The program to handle.
   * @param caller The ContextMenuIf that caused the event.
   * @param singleClick <code>true</code> if it was a single click, <code>false</code> otherwise.
   * @param e The event that was triggered with a mouse interaction.
   */
  public static void handleProgramClick(Program program, ContextMenuIf caller, boolean singleClick, MouseEvent e) {
    if (program == null || e == null) {
      // Nothing to do
      return;
    }
    
    ContextMenuIf menuIf = null;
    int actionId = MenuCompat.ID_ACTION_NONE;
    
    if(VersionCompat.isExtendedMouseActionSupported()) {
      try {
        Method m = ContextMenuManager.class.getDeclaredMethod(singleClick ? "getContextMenuForSingleClick" : "getContextMenuForDoubleClick", MouseEvent.class);
        Object contextMenu = m.invoke(ContextMenuManager.getInstance(), e);

        if (contextMenu == null) {
          return;
        }
        
        if(contextMenu instanceof ContextMenuIf) {
          menuIf = (ContextMenuIf)contextMenu;
        }
        else if(contextMenu.getClass().getCanonicalName().equals("tvbrowser.core.contextmenu.ContextMenuManager.ContextMenuAction")) {
          Method getContextMenuIf = contextMenu.getClass().getDeclaredMethod("getContextMenuIf");
          menuIf = (ContextMenuIf)getContextMenuIf.invoke(contextMenu);
          
          Method getContextMenuActionId = contextMenu.getClass().getDeclaredMethod("getContextMenuActionId");
          actionId = (Integer)getContextMenuActionId.invoke(contextMenu);
        }
      }catch(Exception e1) {
        e1.printStackTrace();
      }
    }
    else {
      if(SwingUtilities.isLeftMouseButton(e)) {
        menuIf = singleClick ? (((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) ? ContextMenuManager.getInstance().getLeftSingleCtrlClickIf() : ContextMenuManager.getInstance().getLeftSingleClickIf()) : ContextMenuManager.getInstance().getDefaultContextMenuIf();
      }
      else if(SwingUtilities.isMiddleMouseButton(e)) {
        menuIf = singleClick ? ContextMenuManager.getInstance().getMiddleClickIf() : ContextMenuManager.getInstance().getMiddleDoubleClickIf();
      }
    }

    if (menuIf == null || ((caller != null)  && (menuIf.getId().equals(caller.getId())))) {
      return;
    }
    
    ActionMenu actionMenu = menuIf.getContextMenuActions(program);
    
    handleAction(program, actionMenu, actionId);          
  }
  
  public static void handleAction(Program program, ActionMenu menu) {
    handleAction(program, menu, MenuCompat.ID_ACTION_NONE);
  }
  
  public static void handleAction(Program program, ActionMenu menu, int actionId) {
    Action action = null;
    
    Method loadActionMenu = null;
    
    if(actionId != MenuCompat.ID_ACTION_NONE && VersionCompat.isAtLeastTvBrowser4()) {
      try {
        loadActionMenu = ContextMenuManager.class.getDeclaredMethod("loadActionMenu", ActionMenu.class, int.class);
      }catch(Exception e) {}
    }
    
    if(actionId != MenuCompat.ID_ACTION_NONE && loadActionMenu != null) {
      ActionMenu actionMenu = null;
          
      try {
        actionMenu = (ActionMenu)loadActionMenu.invoke(null, menu, actionId);
      }catch(Exception e) {}
      
      if(actionMenu != null) {
        action = actionMenu.getAction();
      }
    } else {
      while (menu != null && menu.hasSubItems()) {
        ActionMenu[] subItems = menu.getSubItems();
        if (subItems.length>0) {
          menu = subItems[0];
        }
        else {
          menu = null;
        }
      }
      if (menu == null) {
        return;
      }
  
      action = menu.getAction();
    }
    
    if (action != null) {
      ActionEvent evt = new ActionEvent(program, 0, (String)action.
          getValue(Action.ACTION_COMMAND_KEY));
      action.actionPerformed(evt);
    }
  }
}
