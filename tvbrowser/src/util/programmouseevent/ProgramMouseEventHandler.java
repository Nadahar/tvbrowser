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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.programmouseevent;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;

import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.ContextMenuManager.ContextMenuAction;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;

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
    
    ContextMenuAction contextMenuAction = singleClick ? ContextMenuManager.getInstance().getContextMenuForSingleClick(e) : ContextMenuManager.getInstance().getContextMenuForDoubleClick(e);

    if (contextMenuAction == null) {
      return;
    }

    if ((caller != null)  && (contextMenuAction.getContextMenuIf().getId().equals(caller.getId()))) {
      return;
    }

    handleAction(program, contextMenuAction.getContextMenuIf().getContextMenuActions(program), contextMenuAction.getContextMenuActionId());
  }
  
  public static void handleAction(Program program, ActionMenu menu) {
    handleAction(program, menu, ActionMenu.ID_ACTION_NONE);
  }
  
  public static void handleAction(Program program, ActionMenu menu, int actionId) {
    Action action = null;
    
    if(actionId != ActionMenu.ID_ACTION_NONE) {
     ActionMenu actionMenu = ContextMenuManager.loadActionMenu(menu, actionId);
     
     if(actionMenu != null) {
       action = actionMenu.getAction();
     }
    
    }else {
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
