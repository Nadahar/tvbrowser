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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import util.ui.BrowserLauncher;
import util.ui.ImageUtilities;
import util.ui.Localizer;


/**
 * This Action starts a Web-Browser and shows Details of an given Program-ID
 * @author bodo
 */
public class ShowDetailsAction extends AbstractAction {
    private static final Localizer mLocalizer = Localizer
            .getLocalizerFor(ShowDetailsAction.class);

    /** ID of Program in Database */
    private int _id;
    
    /**
     * Creates the Action
     * @param id ID of Program in Database
     */
    public ShowDetailsAction(int id) {
        _id = id;
        createGui(true, true);
    }

    
    /**
     * Creates the Action
     * @param id ID of Program in Database
     * @param icon show Icon?
     * @param name show Name?
     */
    public ShowDetailsAction(int id, boolean icon, boolean name) {
        _id = id;
        createGui(icon, name);
    }
    
    
    /**
     * Creates the GUI
     * @param icon show Icon?
     * @param name show Name?
     */
    private void createGui(boolean icon, boolean name) {
        // TODO: Translate this!
        if (_id < 0) {
            setEnabled(false);
        }
        
        if (name)
            putValue(Action.NAME, mLocalizer.msg("showDetailsOnWeb", "Show Details on the Web"));
        
        if (icon)
            putValue(Action.SMALL_ICON, ImageUtilities.createImageIconFromJar("tvraterplugin/imgs/details16.gif", getClass()));
    }    
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        BrowserLauncher.openURL("http://tvaddicted.wannawork.de/index.php?showId=" + _id);
    }

}
