/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
package captureplugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.DeviceIf;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * Plugin to send the TV-Data to an external program
 * 
 * @author Andreas Hessel, Bodo Tasche
 */
public class CapturePlugin extends devplugin.Plugin {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePlugin.class);
    
    /** mData that stores the Settings */
    private CapturePluginData mConfig = new CapturePluginData();

    /** Current Marked Programs */
    private Vector mMarkedPrograms = new Vector();
    
    /** The Singelton */
    private static CapturePlugin mInstance = null;
    
    /**
     * Creates the Plugin
     */
    public CapturePlugin() {
        mInstance = this;
    }

    /**
     * Returns this Instance 
     * @return Instance
     */
    public static CapturePlugin getInstance() {
        return mInstance;
    }
    
    /**
     * Called by the host-application during start-up.
     * Implement this method to load any objects from the file system.
     * 
     * @see #writeData(ObjectOutputStream)
     */
    public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
        mConfig = new CapturePluginData();
        mConfig.readData(in, this);
        updateMarkedPrograms();
    }

    /**
     * Counterpart to loadData. Called when the application shuts down.
     * Implement this method to store any objects to the file system.
     * 
     * @see #readData(ObjectInputStream)
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        mConfig.writeData(out);
    }

    /**
     * Called by the host-application during start-up. Implements this method to
     * load your plugins settings from the file system.
     */
    public void loadSettings(Properties settings) {
    }

    /**
     * Called by the host-application during shut-down. Implements this method
     * to store your plugins settings to the file system.
     */
    public Properties storeSettings() {
        return null;
    }

    /**
     * Implement this function to provide information about your plugin.
     */
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("CapturePlugin", "Capture Plugin");
        String desc = mLocalizer.msg("Desc", "Starts a external Program with configurable Parameters");
        String author = "Bodo Tasche, Andreas Hessel";

        return new PluginInfo(name, desc, author, new Version(2, 0));
    }

    /**
     * Returns a new SettingsTab object, which is added to the settings-window.
     */
    public SettingsTab getSettingsTab() {
        return new CapturePluginSettingsTab((JFrame)getParentFrame(), this);
    }


    /**
     * Return tur if execute(program[]) is supported
     */
    public boolean canReceivePrograms() {
        return false;
    }
    
    /**
     * This method is invoked by the host-application if the user has choosen
     * your plugin from the context menu.
     */
    public void executeProgram(Program program) {
        
        if (mConfig.getDevices().size() <= 0) {
            JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("CreateDevice","Please create Device first!"));
            
            CapturePluginDialog dialog = new CapturePluginDialog(getParentFrame(), mConfig);
            dialog.show(CapturePluginPanel.TAB_DEVICELIST);
            return;
        }
        
        DeviceSelector select = new DeviceSelector(UiUtilities.getLastModalChildOf(getParentFrame()), mConfig.getDeviceArray(), program);

        Component comp = UiUtilities.getLastModalChildOf(getParentFrame()); 
        
        int x = comp.getWidth() / 2;
        int y = comp.getHeight() / 2;

        select.show(comp, x, y);
    }

    /*
     *  (non-Javadoc)
     * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
     */
    public Action[] getContextMenuActions(final Program program) {
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                executeProgram(program);
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("record", "record Program"));
        action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("captureplugin/capturePlugin.png", CapturePlugin.class)));
        
        return new Action[] {action};
    }

    /**
     * Updates the marked Programs.
     */
    public void updateMarkedPrograms() {
        Vector list = getMarkedByDevices();
        
        for (int i = 0; i < list.size(); i++) {
            
            if (mMarkedPrograms.contains(list.get(i))) {
                mMarkedPrograms.remove(list.get(i));
            }
            
            ((Program)list.get(i)).mark(this);
        }
        
        for (int i = 0;i < mMarkedPrograms.size(); i++) {
            ((Program)mMarkedPrograms.get(i)).unmark(this);
        }
        
        mMarkedPrograms = list;
    }

    /**
     * This Function Iterates over all Devices and 
     * collects the list of Programs to mark... 
     * @return List with all Programs to mark
     */
    private Vector getMarkedByDevices() {
        Vector v = new Vector();
        
        Iterator devIt = mConfig.getDevices().iterator();
        
        while (devIt.hasNext()) {
            DeviceIf device = (DeviceIf) devIt.next();
            
            Program[] programs = device.getProgramList();
            
            for (int i = 0; i < programs.length; i++) {
                if (!v.contains(programs[i])) {
                    v.add(programs[i]);
                }
                
            }
        }        
        
        return v;
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
        action.putValue(Action.NAME, mLocalizer.msg("CapturePlugin", "Capture Plugin"));
        action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("captureplugin/capturePlugin.png", CapturePlugin.class)));
        action.putValue(BIG_ICON, new ImageIcon(ImageUtilities.createImageFromJar("captureplugin/capturePlugin24.png", CapturePlugin.class)));
        
        return action;
    }        
    
    /**
     * This method is invoked by the host-application if the user has choosen
     * your plugin from the menu.
     */
    public void showDialog() {
        CapturePluginDialog dialog = new CapturePluginDialog(getParentFrame(), mConfig);
        dialog.show(CapturePluginPanel.TAB_PROGRAMLIST);
        updateMarkedPrograms();
    }

    /**
     * Returns the name of the file, containing your plugin icon (in the
     * jar-File).
     */
    public String getMarkIconName() {
        return "captureplugin/capturePlugin.png";
    }

    /** 
     * Sets the CaputePluginData
     * @param data CapturePluginData
     */
    public void setCapturePluginData(CapturePluginData data) {
        mConfig = data;
    }
    
    /**
     * Returns the CapturePluginData
     * @return The CapaturePluginData
     */
    public CapturePluginData getCapturePluginData() {
        return mConfig;
    }
    

}