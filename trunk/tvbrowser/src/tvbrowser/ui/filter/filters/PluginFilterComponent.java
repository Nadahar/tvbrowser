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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.filter.filters;

import javax.swing.*;
import java.awt.*;
import java.io.*;

import tvbrowser.core.PluginManager;
import devplugin.Plugin;
import devplugin.Program;

public class PluginFilterComponent extends FilterComponent {
    
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(PluginFilterComponent.class);
 
    
    private JComboBox mBox;
    private devplugin.Plugin mPlugin;
    
    public PluginFilterComponent(String name, String description) {
        super(name, description);
    }
    
    public PluginFilterComponent(ObjectInputStream in) {
        try {
            int version=in.readInt();
            mName=(String)in.readObject();
            mDescription=(String)in.readObject();
            String pluginClassName=(String)in.readObject();
            mPlugin=PluginManager.getPlugin(pluginClassName);
            
        }catch (IOException e) {
            util.exc.ErrorHandler.handle("Could not read filter rule from file", e);
        }catch (ClassNotFoundException e) {
            util.exc.ErrorHandler.handle("Could not read filter rule from file", e);
        }
    }
    
    public void store(ObjectOutputStream out) {
        try {
            out.writeInt(1);
            out.writeObject(mName);
            out.writeObject(mDescription); 
            out.writeObject(mPlugin.getClass().getName());       
        }catch (IOException e) {
            util.exc.ErrorHandler.handle("Could not write keyword filter to file", e); 
        }
    }
    
    public boolean accept(Program program) {
         
        Plugin[] markedBy=program.getMarkedByPlugins();
        for (int i=0;i<markedBy.length;i++) {
            if (markedBy[i]==mPlugin) {
                return true;
            }
        }        
        
        return false;
    }
    
    public JPanel getPanel() {
        
        if (mPanel==null) {
            mPanel=new JPanel(new BorderLayout(0,7));
            JTextArea ta=new JTextArea(mLocalizer.msg("desc","Accept all programs marked by plugin:"));
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setOpaque(false);
            ta.setEditable(false);
            ta.setFocusable(false);
            mPanel.add(ta,BorderLayout.NORTH);       
            devplugin.Plugin[] plugins=PluginManager.getInstalledPlugins();
            mBox=new JComboBox(plugins);
            mPanel.add(mBox,BorderLayout.CENTER);
            
            if (mPlugin!=null) {
                for (int i=0;i<plugins.length;i++) {
                    if (plugins[i].getClass().getName().equals(mPlugin.getClass().getName())) {
                        mBox.setSelectedItem(plugins[i]);
                        break;
                    }                
                }
            }            
        }
        
        return mPanel;
    }
    
    public String toString() {
        return "plugin";
    }
        
    
        
    public void ok() {
        mPlugin=(devplugin.Plugin)mBox.getSelectedItem();
        
    }
    
  
}
