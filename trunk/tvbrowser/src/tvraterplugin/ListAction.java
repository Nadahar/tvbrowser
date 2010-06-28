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

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;


/**
 * This Action creates a ProgramListDialog
 * 
 * @author bodum
 */
public class ListAction extends AbstractAction {
    private static final Localizer mLocalizer = Localizer
    	.getLocalizerFor(ListAction.class);

    /** Title of Program to show */
    private String mTitle;
    /** Parent */
    private Window mParent;
    
    /**
     * Creates the Action
     * @param parent Parent
     * @param title Title of Program to display in List
     */
    public ListAction(Window parent, String title) {
        mParent = parent;
        mTitle = title;
        createGui(true, true);
    }

    /**
     * Creates the Action
     * @param parent Parent
     * @param title Title of Program to display in List
     * @paran icon show Icon?
     * @paran name show Name ?
     */
    public ListAction(Window parent, String title, boolean icon, boolean name) {
        mParent = parent;
        mTitle = title;
        createGui(icon, name);
    }

    /**
     * Creates the GUI
     * @param icon show Icon?
     * @param name show Name?
     */
    private void createGui(boolean icon, boolean name) {
        putValue(Action.SHORT_DESCRIPTION, mLocalizer.msg("showList", "Show List"));

        if (name) {
          putValue(Action.NAME, mLocalizer.msg("showList", "Show List"));
        }
        
        if (icon) {
          putValue(Action.SMALL_ICON, TVBrowserIcons.search(TVBrowserIcons.SIZE_SMALL));
        }
    }

    public void actionPerformed(ActionEvent e) {
        ProgramListDialog dialog = new ProgramListDialog(mParent, mTitle);
        
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