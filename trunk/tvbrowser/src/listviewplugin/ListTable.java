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
package listviewplugin;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import devplugin.Program;

/**
 * This Table repaints the Programs if their state changes
 */
public class ListTable extends JTable implements ChangeListener {
    /** List of all Programs */
    private Vector<Program> mPrograms = new Vector<Program>();;

    /**
     * Creates the ListTable
     * @param model Model to use
     */
    public ListTable(ListTableModel model) {
        super(model);
        addToAllPrograms();
    }

    /**
     * Sets the Model 
     * @param model Model to use
     */
    public void setModel(ListTableModel model) {
        super.setModel(model);
        removeFromAllPrograms();
        addToAllPrograms();
    }

    /**
     * Add a ChangeListener to all Programs for repainting
     */
    public void addNotify() {
        super.addNotify();
        removeFromAllPrograms();
        addToAllPrograms();
    }

    /**
     * Remove a ChangeListener to all Programs
     */
    public void removeNotify() {
        super.removeNotify();
        removeFromAllPrograms();
    }

    /**
     * Add a Listener to all Programs
     */
    public void addToAllPrograms() {
        ListTableModel model = (ListTableModel) getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            Program prg = model.getProgram(i);
            if (prg != null) {
                prg.addChangeListener(this);
                mPrograms.add(prg);
            }

            prg = model.getNextProgram(i);
            if (prg != null) {
                prg.addChangeListener(this);
                mPrograms.add(prg);
            }
        }
    }

    /**
     * Remove Listeners form all Programs  
     */
    private void removeFromAllPrograms() {
        for (int i = 0; i < mPrograms.size(); i++) {
            (mPrograms.get(i)).removeChangeListener(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
        updateUI();
    }
}