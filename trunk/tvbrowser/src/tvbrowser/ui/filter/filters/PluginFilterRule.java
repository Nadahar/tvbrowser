
package tvbrowser.ui.filter.filters;

import javax.swing.*;
import java.awt.*;
import java.io.*;

import tvbrowser.core.PluginManager;
import devplugin.Plugin;
import devplugin.Program;

public class PluginFilterRule extends FilterRule {
    
    private JComboBox mBox;
    private devplugin.Plugin mPlugin;
    
    public PluginFilterRule(String name, String description) {
        super(name, description);
    }
    
    public PluginFilterRule(ObjectInputStream in) {
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
        /*Plugin plugin=(devplugin.Plugin)mBox.getSelectedItem();
        Plugin[] markedBy=program.getMarkedByPlugins();
        for (int i=0;i<markedBy.length;i++) {
            if (markedBy[i]==plugin) {
                return true;
            }
        }*/
        
        //Plugin plugin=PluginManager.getPlugin(mPluginClassName);
        
        System.out.println("accept program '"+program.getTitle()+"' ?");
        if (mPlugin==null) {
            System.out.println("uups! no plugin");
        }
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
            mPanel=new JPanel(new BorderLayout());        
            devplugin.Plugin[] plugins=PluginManager.getInstalledPlugins();
            mBox=new JComboBox(plugins);
            mPanel.add(mBox,BorderLayout.WEST);
            
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
