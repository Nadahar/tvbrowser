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

package tvbrowser.core.filters.filtercomponents;

import javax.swing.*;
import java.awt.*;
import java.io.*;

import tvbrowser.core.PluginLoader;
import tvbrowser.core.PluginManager;
import tvbrowser.core.filters.FilterComponent;
import devplugin.Plugin;
import devplugin.Program;

public class PluginFilterComponent implements FilterComponent {
    
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(PluginFilterComponent.class);
  private static java.util.logging.Logger mLog
     = java.util.logging.Logger.getLogger(PluginFilterComponent.class.getName());

    
  private JComboBox mBox;
  private devplugin.Plugin mPlugin;
  
  private String mDescription, mName;
  
  public PluginFilterComponent(String name, String desc) {
    mName = name;
    mDescription = desc;  
  }
    
  public PluginFilterComponent() {
    this("","");    
  }
    
  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
      
    String pluginClassName=(String)in.readObject();
    mPlugin = PluginLoader.getInstance().getActivePluginByClassName(pluginClassName);

  }
    
  public void write(ObjectOutputStream out) throws IOException {
      
    if (mPlugin==null) {
      out.writeObject("[invalid]"); 
    }
    else {
      out.writeObject(mPlugin.getClass().getName());
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
        
    JPanel content = new JPanel(new BorderLayout(0,7));
    JTextArea ta=new JTextArea(mLocalizer.msg("desc","Accept all programs marked by plugin:"));
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    ta.setOpaque(false);
    ta.setEditable(false);
    ta.setFocusable(false);
    content.add(ta,BorderLayout.NORTH);       
    Plugin[] plugins=PluginManager.getInstance().getInstalledPlugins();
    mBox=new JComboBox(plugins);
    content.add(mBox,BorderLayout.CENTER);
            
    if (mPlugin!=null) {
      for (int i=0;i<plugins.length;i++) {
        if (plugins[i].getClass().getName().equals(mPlugin.getClass().getName())) {
          mBox.setSelectedItem(plugins[i]);
          break;
        }                
      }
    }            
    return content;
    }
    
    public String toString() {
        return "plugin";
    }
        
    
        
    public void ok() {
        mPlugin=(devplugin.Plugin)mBox.getSelectedItem();
        
    }

		
		public int getVersion() {
			return 1;
		}

	
  public String getName() {
    return mName;
  }

  
  public String getDescription() {
    return mDescription;
  }

  public void setName(String name) {
    mName = name;
  }
  
  public void setDescription(String desc) {
    mDescription = desc;
  }
		
    
  
}
