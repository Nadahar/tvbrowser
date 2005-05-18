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

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import devplugin.PluginAccess;
import devplugin.Program;

public class PluginFilterComponent implements FilterComponent {
    
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(PluginFilterComponent.class);
  private static java.util.logging.Logger mLog
     = java.util.logging.Logger.getLogger(PluginFilterComponent.class.getName());

    
  private JComboBox mBox;
  private PluginProxy mPlugin;
  
  private String mDescription, mName;
  
  public PluginFilterComponent(String name, String desc) {
    mName = name;
    mDescription = desc;  
  }
    
  public PluginFilterComponent() {
    this("","");    
  }
    
  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    String pluginId;
    if (version == 1) {
      String pluginClassName = (String) in.readObject();
      pluginId = "java." + pluginClassName;
    } else {
      pluginId = (String) in.readObject();
    }
    
    mPlugin = PluginProxyManager.getInstance().getPluginForId(pluginId);
  }
    
  public void write(ObjectOutputStream out) throws IOException {
    if (mPlugin==null) {
      out.writeObject("[invalid]"); 
    }
    else {
      out.writeObject(mPlugin.getId());
    }    
  }
    
  public boolean accept(Program program) {
         
    PluginAccess[] markedBy = program.getMarkedByPlugins();
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

    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    mBox=new JComboBox(plugins);
    if (mPlugin!=null) {
      mBox.setSelectedItem(mPlugin);
    }
    content.add(mBox,BorderLayout.CENTER);

    
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(content, BorderLayout.NORTH);
    return centerPanel;
  }


    public String toString() {
        return mLocalizer.msg("Plugin", "Plugin");
    }
        
    
        
  public void ok() {
    mPlugin = (PluginProxy) mBox.getSelectedItem();
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
