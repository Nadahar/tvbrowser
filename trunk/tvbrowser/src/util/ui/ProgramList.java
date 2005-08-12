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

import devplugin.Plugin;
import devplugin.PluginManager;
import devplugin.Program;


/**
 * This Class extends a JList for showing Programs
 */
public class ProgramList extends JList implements ChangeListener, ListDataListener {

    private Vector mPrograms = new Vector();


    /**
     * Creates the JList and adds the default MouseListeners (PopUpBox)
     *
     * @param programArr Array of Programs to show
     */
    public ProgramList(Vector programArr) {
        super(programArr);
        setCellRenderer(new ProgramListCellRenderer());
    }

    /**
     * Creates the JList and adds the default MouseListeners (PopUpBox)
     *
     * @param programArr Array of Programs to show
     */
    public ProgramList(Program[] programArr) {
        super(programArr);
        setCellRenderer(new ProgramListCellRenderer());
    }

    /**
     * Creates the JList and adds the default MouseListeners (PopUpBox)
     *
     * @param programs Model with Programs to show
     */
    public ProgramList(ListModel programs) {
        super(programs);
        programs.addListDataListener(this);
        setCellRenderer(new ProgramListCellRenderer());
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
        for (int i=0; i < mPrograms.size(); i++) {
            ((Program)mPrograms.get(i)).removeChangeListener(this);
        }
    }

    private void addToPrograms() {
        ListModel list = getModel();

        for (int i=0; i < list.getSize(); i++) {
            if (list.getElementAt(i) instanceof Program) {
                Program prg = (Program)list.getElementAt(i);
                prg.addChangeListener(this);
                mPrograms.add(prg);
            }
        }
    }

    /**
     * Add a Mouse-Listener for the Popup-Box
     */
    public void addMouseListeners(final Plugin caller) {
      addMouseListener(new MouseAdapter() {

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

        public void mouseClicked(MouseEvent e) {
          PluginManager mng = Plugin.getPluginManager();

          if (SwingUtilities.isLeftMouseButton(e)
              && (e.getClickCount() == 2)) {
            int inx = locationToIndex(e.getPoint());
            Program prog = (Program) ProgramList.this.getModel()
                .getElementAt(inx);

            mng.handleProgramDoubleClick(prog, caller);
          }
          if (SwingUtilities.isMiddleMouseButton(e)
              && (e.getClickCount() == 1)) {
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
     * @param e MouseEvent for X/Y Coordinates
     * @param caller Plugin that called this
     */
    private void showPopup(MouseEvent e, Plugin caller) {
      PluginManager mng = Plugin.getPluginManager();

      int inx = locationToIndex(e.getPoint());
      setSelectedIndex(inx);
      Program prog = (Program) getModel().getElementAt(inx);
      JPopupMenu menu = mng.createPluginContextMenu(prog, caller);

      menu.show(ProgramList.this, e.getX() - 15, e.getY() - 15);

    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
        updateUI();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
     */
    public void contentsChanged(ListDataEvent e) {
        removeFromPrograms();
        addToPrograms();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
     */
    public void intervalAdded(ListDataEvent e) {
        removeFromPrograms();
        addToPrograms();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
     */
    public void intervalRemoved(ListDataEvent e) {
        removeFromPrograms();
        addToPrograms();
    }
}