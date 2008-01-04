/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 */
 
/**
 * TV-Browser
 * @author Martin Oberhauser
 */
 
package tvdataservice;

/**
 * This is a SettingsPanel for a Dataservice
 */
public abstract class SettingsPanel extends javax.swing.JPanel {

    /**
     *  Will be called if ok was pressed in the settings dialog
     */
    public abstract void ok();

    /**
     * Will be called if cancel was pressed
     * @since 2.7
     */
    public void cancel() {

    }
 }