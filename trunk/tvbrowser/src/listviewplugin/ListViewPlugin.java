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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import util.ui.ImageUtilities;
import util.ui.UiUtilities;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Version;

/**
 * This Plugin shows a List of current running Programs
 * 
 * @author bodo
 */
public class ListViewPlugin extends Plugin {

    /** Translator */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ListViewPlugin.class);

    /**
     * Creates the Plugin
     */
    public ListViewPlugin() {
    }

    /**
     * Returns Informations about this Plugin
     */
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "View List Plugin");
        String desc = mLocalizer.msg("description", "Shows a List of current running Programs");
        String author = "Bodo Tasche";
        return new PluginInfo(name, desc, author, new Version(1, 30));
    }

    /**
     * Creates the Dialog
     */
    public void showDialog() {
        final ListViewDialog dlg = new ListViewDialog(getParentFrame(), this);

        dlg.pack();
        dlg.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                _dimensionListDialog = e.getComponent().getSize();
            }

            public void componentMoved(ComponentEvent e) {
                e.getComponent().getLocation(_locationListDialog);
            }
        });

        if ((_locationListDialog != null) && (_dimensionListDialog != null)) {
            dlg.setLocation(_locationListDialog);
            dlg.setSize(_dimensionListDialog);
            dlg.show();
        } else {
            dlg.setSize(600, 600);
            UiUtilities.centerAndShow(dlg);
            _locationListDialog = dlg.getLocation();
            _dimensionListDialog = dlg.getSize();
        }

    }

    /**
     * Icon to show for a marked program
     */
    public String getMarkIconName() {
        return "listviewplugin/listview16.gif";
    }


    /*
     *  (non-Javadoc)
     * @see devplugin.Plugin#getButtonAction()
     */
    public Action getButtonAction() {
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                showDialog();
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("buttonName", "View Liste"));
        action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("listviewplugin/listview16.gif", ListViewPlugin.class)));
        action.putValue(BIG_ICON, new ImageIcon(ImageUtilities.createImageFromJar("listviewplugin/listview24.gif", ListViewPlugin.class)));
        
        
        return action;
    }
    
    
    /** Needed for Position */
    private Point _locationListDialog = null;

    /** Needed for Position */
    private Dimension _dimensionListDialog = null;
    
}