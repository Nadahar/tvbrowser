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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */


package tvbrowser.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.*;

/**
 * The context menu is shown by clicking the right mouse button at the program table.
 */


public class ContextMenu extends JPopupMenu implements ActionListener {

    private devplugin.Program program;
    private Frame parent;

    private HashMap plugins;

    public ContextMenu(Frame parent) {
        super();
        this.parent=parent;
        plugins=new HashMap();
    }


    /**
     * Adds the specified plugin to the context menu, if the plugin supports the
     * context menu mechanism.
     */
    public void addPlugin(devplugin.Plugin plugin) {
        String itemText=plugin.getContextMenuItemText();
        if (itemText==null) {
            return;
        }

        JMenuItem item=new JMenuItem(itemText);
        add(item);
        item.addActionListener(this);

        int i=1;
        String txt=itemText;
        while (plugins.get(txt)!=null) {
            txt=itemText+"("+i+")";
            i++;
        }
        plugins.put(txt,plugin);
    }

    /**
     * Assigns the specified program to the context menu. If the user selects
     * a menu item, te execute method of this program will be performed.
     */
    public void setProgram(devplugin.Program prog) {
        program=prog;
    }


    public void actionPerformed(ActionEvent e) {
        Object obj=e.getSource();
        if (obj instanceof JMenuItem) {
            String itemText=((JMenuItem)obj).getText();
            devplugin.Plugin plugin=(devplugin.Plugin)plugins.get(itemText);
            plugin.execute(program);
        }
    }
}