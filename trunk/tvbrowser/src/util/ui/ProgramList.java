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
    ListDataListener {

  private Vector<Program> mPrograms = new Vector<Program>();

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   */
  public ProgramList(Vector<Program> programArr) {
    super(programArr);
    setCellRenderer(new ProgramListCellRenderer(new ProgramPanelSettings(
        new PluginPictureSettings(
            PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false)));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   */
  public ProgramList(Program[] programArr) {
    super(programArr);
    setCellRenderer(new ProgramListCellRenderer(new ProgramPanelSettings(
        new PluginPictureSettings(
            PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false)));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   */
  public ProgramList(ListModel programs) {
    super(programs);
    programs.addListDataListener(this);
    setCellRenderer(new ProgramListCellRenderer(new ProgramPanelSettings(
        new PluginPictureSettings(
            PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false)));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   * @param showOnlyDateAndTitle
   *          If this panel should only show date time and title.
   * 
   * @since 2.2.1
   * @deprecated Since 2.2.2 Use
   *             {@link #ProgramList(Vector, ProgramPanelSettings)} instead.
   */
  public ProgramList(Vector<Program> programArr, boolean showOnlyDateAndTitle) {
    this(programArr, new ProgramPanelSettings(
        ProgramPanelSettings.SHOW_PICTURES_NEVER, -1, -1, showOnlyDateAndTitle,
        true, 10));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programArr
   *          Array of Programs to show
   * @param showOnlyDateAndTitle
   *          If this panel should only show date time and title.
   * 
   * @since 2.2.1
   * @deprecated Since 2.2.2 Use
   *             {@link #ProgramList(Program[], ProgramPanelSettings)} instead.
   */
  public ProgramList(Program[] programArr, boolean showOnlyDateAndTitle) {
    this(programArr, new ProgramPanelSettings(
        ProgramPanelSettings.SHOW_PICTURES_NEVER, -1, -1, showOnlyDateAndTitle,
        true, 10));
  }

  /**
   * Creates the JList and adds the default MouseListeners (PopUpBox)
   * 
   * @param programs
   *          Model with Programs to show
   * @param showOnlyDateAndTitle
   *          If this panel should only show date time and title.
   * 
   * @since 2.2.1
   * @deprecated Since 2.2.2 Use
   *             {@link #ProgramList(ListModel, ProgramPanelSettings)} instead.
   */
  public ProgramList(ListModel programs, boolean showOnlyDateAndTitle) {
    this(programs, new ProgramPanelSettings(
        ProgramPanelSettings.SHOW_PICTURES_NEVER, -1, -1, showOnlyDateAndTitle,
        true, 10));
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
    setCellRenderer(new ProgramListCellRenderer(settings));
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
    setCellRenderer(new ProgramListCellRenderer(settings));
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
    setCellRenderer(new ProgramListCellRenderer(settings));
  }

  public ProgramList(ListModel programs, ProgramPanelSettings settings, int axis) {
    super(programs);
    programs.addListDataListener(this);
    setCellRenderer(new ProgramListCellRenderer(settings, axis));
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
    super(programVector);
    setCellRenderer(new ProgramListCellRenderer(new ProgramPanelSettings(
        settings, false)));
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
    super(programArr);
    setCellRenderer(new ProgramListCellRenderer(new ProgramPanelSettings(
        settings, false)));
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
    super(programs);
    programs.addListDataListener(this);
    setCellRenderer(new ProgramListCellRenderer(new ProgramPanelSettings(
        settings, false)));
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
   */
  public ProgramList(ListModel programs, PluginPictureSettings settings,
      int axis) {
    super(programs);
    programs.addListDataListener(this);
    setCellRenderer(new ProgramListCellRenderer(new ProgramPanelSettings(
        settings, false), axis));
  }

  /**
   * Add a ChangeListener to all Programs for repainting
   */
  public void addNotify() {
    super.addNotify();
    removeFromPrograms();
    addToPrograms();
  }

  /**
   * Remove a ChangeListener to all Programs
   */
  public void removeNotify() {
    super.removeNotify();
    removeFromPrograms();
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
        final PluginManager mng = Plugin.getPluginManager();
        
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1) && e.getModifiersEx() == 0) {
          mLeftSingleClickThread = new Thread() {
            public void run() {
              try {
                Thread.sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
                
                int inx = locationToIndex(e.getPoint());
                Program prog = (Program) ProgramList.this.getModel()
                    .getElementAt(inx);

                mng.handleProgramSingleClick(prog, caller);                
              } catch (InterruptedException e) {
                // ignore
              }              
            }
          };
          mLeftSingleClickThread.setPriority(Thread.MIN_PRIORITY);
          mLeftSingleClickThread.start();
        }
        else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2) && e.getModifiersEx() == 0) {
          if(mLeftSingleClickThread != null && mLeftSingleClickThread.isAlive()) {
            mLeftSingleClickThread.interrupt();
          }
          
          int inx = locationToIndex(e.getPoint());
          Program prog = (Program) ProgramList.this.getModel()
              .getElementAt(inx);

          mng.handleProgramDoubleClick(prog, caller);
        }
        else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
          int inx = locationToIndex(e.getPoint());
          Program prog = (Program) ProgramList.this.getModel()
              .getElementAt(inx);

          mng.handleProgramMiddleClick(prog, caller);
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
}