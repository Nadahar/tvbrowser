/*
 * TV-Browser Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package tvraterplugin;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import util.ui.ImageUtilities;
import util.ui.UiUtilities;


/**
 * This Action creates a ProgramListDialog
 * 
 * @author bodum
 */
public class ListAction extends AbstractAction {

    /** Title of Program to show */
    private String _title;
    /** Parent */
    private Frame _fparent;
    /** Parent */
    private Dialog _dparent;
    
    /**
     * Creates the Action 
     * @param parent Parent
     * @param title Title of Program to display in List
     */
    public ListAction(Frame parent, String title) {
        _fparent = parent;
        _title = title;
        createGui(true, true);
    }

    /**
     * Creates the Action 
     * @param parent Parent
     * @param title Title of Program to display in List
     * @paran icon show Icon?
     * @paran name show Name ?
     */
    public ListAction(Frame parent, String title, boolean icon, boolean name) {
        _fparent = parent;
        _title = title;
        createGui(icon, name);
    }

    /**
     * Creates the Action 
     * @param parent Parent
     * @param title Title of Program to display in List
     */
    public ListAction(Dialog parent, String title) {
        _title = title;
        _dparent = parent;
        createGui(true, true);
    }

    /**
     * Creates the Action 
     * @param parent Parent
     * @param title Title of Program to display in List
     * @paran icon show Icon?
     * @paran name show Name ?
     */
    public ListAction(Dialog parent, String title, boolean icon, boolean name) {
        _title = title;
        _dparent = parent;
        createGui(icon, name);
    }
    
    
    /**
     * Creates the GUI
     * @param icon show Icon?
     * @param name show Name?
     */
    private void createGui(boolean icon, boolean name) {
        // TODO: Translate this!
        if (name)
            putValue(Action.NAME, "Show List");
        
        if (icon)
            putValue(Action.SMALL_ICON, ImageUtilities.createImageIconFromJar("tvraterplugin/imgs/listview16.gif", getClass()));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        ProgramListDialog dialog;
        
        if (_dparent != null)
            dialog = new ProgramListDialog(_dparent, _title);
        else 
            dialog = new ProgramListDialog(_fparent, _title);
        
        Dimension dimensionDialog = dialog.getSize();
        if (dimensionDialog.width < 300) {
            dimensionDialog.width = 300;
        } else if (dimensionDialog.width > 500) {
            dimensionDialog.width = 500;
        }
        
        if (dimensionDialog.height > 300) {
            dimensionDialog.height = 300;
        }
        dialog.setSize(dimensionDialog);
        
        UiUtilities.centerAndShow(dialog);        
    }

}