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
package util.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateListener;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;

/**
 * This Class extends a JList for showing Programs
 */
public class ProgramList extends JList implements ChangeListener,
    ListDataListener, PluginStateListener {

  private Vector<Program> mPrograms = new Vector<Program>();

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   */
  public ProgramList(Vector<Program> programArr) {
    this(programArr, new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   */
  public ProgramList(Program[] programArr) {
    this(programArr, new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   */
  public ProgramList(ListModel programs) {
    this(programs, new PluginPictureSettings(
        PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE));
  }
  
  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programVector
   *          Array of Programs to show
   * @param settings
   *          The settings for the program panel.
   * 
   * @since 2.2.2
   */
  public ProgramList(Vector<Program> programVector,
      ProgramPanelSettings settings) {
    super(programVector);
    initialize(settings);
  }

  private void initialize(ProgramPanelSettings settings) {
    setCellRenderer(new ProgramListCellRenderer(settings));
    setToolTipText("");
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   * @param settings
   *          The settings for the program panel.
   * 
   * @since 2.2.2
   */
  public ProgramList(Program[] programArr, ProgramPanelSettings settings) {
    super(programArr);
    initialize(settings);
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   * @param settings
   *          The settings for the program panel.
   * 
   * @since 2.2.2
   */
  public ProgramList(ListModel programs, ProgramPanelSettings settings) {
    super(programs);
    programs.addListDataListener(this);
    initialize(settings);
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programVector
   *          Array of Programs to show
   * @param settings
   *          The plugin picture settings for the program panel.
   * 
   * @since 2.6
   */
  public ProgramList(Vector<Program> programVector,
      PluginPictureSettings settings) {
    this(programVector, new ProgramPanelSettings(settings, false));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   * @param settings
   *          The plugin picture settings for the program panel.
   * 
   * @since 2.6
   */
  public ProgramList(Program[] programArr, PluginPictureSettings settings) {
    this(programArr, new ProgramPanelSettings(settings, false));
  }
  
  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   * @param settings
   *          The plugin picture settings for the program panel.
   * 
   * @since 2.6
   */
  public ProgramList(ListModel programs, PluginPictureSettings settings) {
    this(programs, new ProgramPanelSettings(settings, false));
  }
  
  /**
   * Add a ChangeListener to all Programs for repainting
   */
  public void addNotify() {
    super.addNotify();
    removeFromPrograms();
    addToPrograms();
    PluginProxyManager.getInstance().addPluginStateListener(this);
  }

  /**
   * Remove a ChangeListener to all Programs
   */
  public void removeNotify() {
    super.removeNotify();
    removeFromPrograms();
    PluginProxyManager.getInstance().removePluginStateListener(this);
  }

  private void removeFromPrograms() {
    removeFromPrograms(0, mPrograms.size() - 1);
  }

  private void removeFromPrograms(int indexFirst, int indexLast) {
    if(indexFirst >= mPrograms.size()) {
      indexFirst = mPrograms.size() - 1;
    }
    if(indexLast >= mPrograms.size()) {
      indexLast = mPrograms.size() - 1;
    }
    if(indexFirst >= 0) {
      for (int i = indexLast; i >= indexFirst; i--) {
        mPrograms.remove(i).removeChangeListener(this);
      }
    }
  }

  private void addToPrograms() {
    ListModel list = getModel();
    addToPrograms(0, list.getSize() - 1);
  }

  private void addToPrograms(int indexFirst, int indexLast) {
    ListModel list = getModel();
    for (int i = indexFirst; i <= indexLast; i++) {
      Object element = list.getElementAt(i);
      if (element instanceof Program) {
        Program prg = (Program) element;
        prg.addChangeListener(this);
        mPrograms.add(prg);
      }
    }
  }

  /**
   * Add a Mouse-Listener for the Popup-Box
   * 
   * The caller ContextMenuIfs menus are not shown, if you want to have all
   * available menus just use <code>null</code> for caller.
   * 
   * @param caller
   *          The ContextMenuIf that called this.
   */
  public void addMouseListeners(final ContextMenuIf caller) {
    addMouseListener(new MouseAdapter() {
      private Thread mLeftSingleClickThread;
      private boolean mPerformingSingleClick = false;
      private static final byte LEFT_SINGLE_CLICK = 1;
      private static final byte LEFT_DOUBLE_CLICK = 2;
      private static final byte MIDDLE_CLICK = 3;
      
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopup(e, caller);
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showPopup(e, caller);
        }
      }

      public void mouseClicked(final MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1) && e.getModifiersEx() == 0) {
          mLeftSingleClickThread = new Thread("Single click") {
            public void run() {
              try {
                mPerformingSingleClick = false;
                Thread.sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
                mPerformingSingleClick = true;
                
                handleClick(LEFT_SINGLE_CLICK, e);
                
                mPerformingSingleClick = false;
              } catch (InterruptedException e) {
                // ignore
              }              
            }
          };
          mLeftSingleClickThread.setPriority(Thread.MIN_PRIORITY);
          mLeftSingleClickThread.start();
        }
        else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2) && e.getModifiersEx() == 0) {
          if(!mPerformingSingleClick && mLeftSingleClickThread != null && mLeftSingleClickThread.isAlive()) {
            mLeftSingleClickThread.interrupt();
          }
          
          if(!mPerformingSingleClick) {
            handleClick(LEFT_DOUBLE_CLICK, e);
          }
        }
        else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
          handleClick(MIDDLE_CLICK, e);
        }
      }
      
      private void handleClick(byte type, MouseEvent e) {
        int inx = locationToIndex(e.getPoint());
        if (inx >= 0) {
          Object prog = ProgramList.this.getModel()
          .getElementAt(inx);

          if(prog instanceof Program) {
            if(type == LEFT_SINGLE_CLICK) {
              Plugin.getPluginManager().handleProgramSingleClick((Program)prog, caller);
            }
            else if(type == LEFT_DOUBLE_CLICK) {
              Plugin.getPluginManager().handleProgramDoubleClick((Program)prog, caller);
            }
            else if(type == MIDDLE_CLICK) {
              Plugin.getPluginManager().handleProgramMiddleClick((Program)prog, caller);
            }
            // force recalculation of program panel sizes
            updateUI();
          }
        }
      }
    });
  }

  /**
   * Shows the Popup
   * 
   * @param e
   *          MouseEvent for X/Y Coordinates
   * @param caller
   *          The ContextMenuIf that called this
   */
  private void showPopup(MouseEvent e, ContextMenuIf caller) {
    PluginManager mng = Plugin.getPluginManager();

    int inx = locationToIndex(e.getPoint());
    setSelectedIndex(inx);

    if (getModel().getElementAt(inx) instanceof Program) {
      Program prog = (Program) getModel().getElementAt(inx);
      JPopupMenu menu = mng.createPluginContextMenu(prog, caller);
      menu.show(ProgramList.this, e.getX() - 15, e.getY() - 15);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  public void stateChanged(ChangeEvent e) {
    repaint();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
   */
  public void contentsChanged(ListDataEvent e) {
    removeFromPrograms();
    addToPrograms();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
   */
  public void intervalAdded(ListDataEvent e) {
    addToPrograms(e.getIndex0(), e.getIndex1());
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
   */
  public void intervalRemoved(ListDataEvent e) {
    removeFromPrograms(e.getIndex0(), e.getIndex1());
  }

  /**
   * @return The selected programs;
   * @since 2.2
   */
  public Program[] getSelectedPrograms() {
    Object[] o = getSelectedValues();

    if (o == null || o.length == 0) {
      return null;
    }

    Program[] p = new Program[o.length];
    for (int i = 0; i < o.length; i++) {
      p[i] = (Program) o[i];
    }

    return p;
  }
  
  public void pluginActivated(PluginProxy plugin) {
    if (plugin.getProgramTableIcons(Plugin.getPluginManager().getExampleProgram()) != null) {
      updatePrograms();
    }
  }

  public void pluginDeactivated(PluginProxy plugin) {
    updatePrograms();
  }

  private void updatePrograms() {
    repaint();
  }

  public void pluginLoaded(PluginProxy plugin) {
    // noop
  }

  public void pluginUnloaded(PluginProxy plugin) {
    // noop
  }
  
  /* Deprecated constructors from here */
  
  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs The program list.
   * @param settings The program panel settings.
   * @param axis The axis for the progress bar of the program table.
   * @deprecated Since 2.7
   */
  public ProgramList(ListModel programs, ProgramPanelSettings settings, int axis) {
    super(programs);
    programs.addListDataListener(this);
    setCellRenderer(new ProgramListCellRenderer(settings, axis));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   * @param settings
   *          The plugin picture settings for the program panel.
   * @param axis
   *          The orientation of the progress bar.
   * @deprecated Since 2.7 Use {@link #ProgramList(ListModel, ProgramPanelSettings)} instead.
   */
  public ProgramList(ListModel programs, PluginPictureSettings settings,
      int axis) {
    this(programs, new ProgramPanelSettings(settings, false, axis), axis);
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    final Point point = event.getPoint();
    int index = locationToIndex(point);
    if (index >= 0) {
      Rectangle bounds = getCellBounds(index, index);
      if (bounds != null) {
        int x = point.x - bounds.x;
        int y = point.y - bounds.y;
        Component component = getCellRenderer()
            .getListCellRendererComponent(this, mPrograms.elementAt(index),
                index, false, false);
        if (component != null && component instanceof Container) {
          Container container = (Container) component;
          component = container.getComponent(1);
          if (component != null && component instanceof ProgramPanel) {
            ProgramPanel panel = (ProgramPanel) component;
            x -= panel.getX();
            y -= panel.getY();
            return panel.getToolTipText(x, y);
          }
        }
      }
    }
    // mouse is over an empty part of the list
    return null;
  }

}