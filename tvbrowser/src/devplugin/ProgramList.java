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
package devplugin;

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

import searchplugin.SearchPlugin;
import util.ui.ProgramListCellRenderer;


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
    public ProgramList(Program[] programArr) {
        super(programArr);
        setCellRenderer(new ProgramListCellRenderer());
        addMouseListeners();
    }

    /**
     * Creates the JList and if wanted adds the default MouseListeners
     * 
     * @param programArr Array of Programs to show
     * @param mouseListeners If true adds PopUp-MouseListeners
     */
    public ProgramList(Program[] programArr, boolean mouseListeners) {
        super(programArr);
        setCellRenderer(new ProgramListCellRenderer());
        if (mouseListeners) {
            addMouseListeners();
        }
    }
    

    /**
     * Creates the JList and adds the default MouseListeners (PopUpBox)
     * 
     * @param programArr Array of Programs to show
     */
    public ProgramList(ListModel programs) {
        super(programs);
        programs.addListDataListener(this);
        setCellRenderer(new ProgramListCellRenderer());
        addMouseListeners();
    }

    /**
     * Creates the JList and if wanted adds the default MouseListeners
     * 
     * @param programArr Array of Programs to show
     * @param mouseListeners If true adds PopUp-MouseListeners
     */
    public ProgramList(ListModel programs, boolean mouseListeners) {
        super(programs);
        programs.addListDataListener(this);
        setCellRenderer(new ProgramListCellRenderer());
        if (mouseListeners) {
            addMouseListeners();
        }
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
            Program prg = (Program)list.getElementAt(i);
            prg.addChangeListener(this);
            mPrograms.add(prg);
        }
    }
    
    /**
     * Add a Mouse-Listener for the Popup-Box
     */
    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
    		public void mouseClicked(MouseEvent e) {
        		if (SwingUtilities.isRightMouseButton(e)) {
    				int inx= locationToIndex(e.getPoint());
    				JPopupMenu menu=devplugin.Plugin.getPluginManager().createPluginContextMenu((Program) ProgramList.this.getModel().getElementAt(inx),SearchPlugin.getInstance());
    				
    				menu.show(ProgramList.this, e.getX() - 15, e.getY() - 15);
    			} else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
    				int inx=locationToIndex(e.getPoint());
    				Program p= (Program) ProgramList.this.getModel().getElementAt(inx);

    				Plugin plugin = devplugin.Plugin.getPluginManager().getDefaultContextMenuPlugin();
    	            if (plugin != null) {
    	                plugin.execute(p);
    	            }
    	        }
        	}
        	
        });
        
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