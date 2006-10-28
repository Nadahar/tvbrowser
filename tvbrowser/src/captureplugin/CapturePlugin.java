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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.DeviceIf;
import devplugin.ActionMenu;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
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

    /** Root-Node for the Program-Tree */
    private PluginTreeNode mRootNode = new PluginTreeNode(this, false);
    
    /**
     * Creates the Plugin
     */
    public CapturePlugin() {
        mInstance = this;
    }

    /**
     * Returns this Instance
     * 
     * @return Instance
     */
    public static CapturePlugin getInstance() {
        return mInstance;
    }

    /**
     * Called by the host-application during start-up. Implement this method to
     * load any objects from the file system.
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

        return new PluginInfo(name, desc, author, new Version(2, 06));
    }

    /**
     * Returns a new SettingsTab object, which is added to the settings-window.
     */
    public SettingsTab getSettingsTab() {
        return new CapturePluginSettingsTab((JFrame) getParentFrame(), this);
    }

    /**
     * Return true if execute(program[]) is supported
     */
    public boolean canReceivePrograms() {
        return true;
    }

    /**
     * Receives a list of programs from another plugin.
     * <p>
     * Override this method to receive programs from other plugins.
     * 
     * @param programArr The programs passed from the other plugin.
     * @see #canReceivePrograms()
     */
    public void receivePrograms(Program[] programArr) {
        showExecuteDialog(programArr);
    }

    private void showExecuteDialog(Program[] program) {      
        Window comp = UiUtilities.getLastModalChildOf(getParentFrame());

        if (comp instanceof JDialog) {
            comp = (Window) comp.getParent();
        }

        if (mConfig.getDevices().size() <= 0) {
            JOptionPane.showMessageDialog(comp, mLocalizer.msg("CreateDevice", "Please create Device first!"));

            CapturePluginDialog dialog;

            if (comp instanceof JFrame) {
                dialog = new CapturePluginDialog((JFrame) comp, mConfig);
            } else {
                dialog = new CapturePluginDialog((JDialog) comp, mConfig);
            }

            dialog.show(CapturePluginPanel.TAB_DEVICELIST);
            return;
        }

        DeviceSelector select;

        if (comp instanceof JDialog)
            select = new DeviceSelector((JDialog) comp, mConfig.getDeviceArray(), program);
        else
            select = new DeviceSelector((JFrame) comp, mConfig.getDeviceArray(), program);

        int x = comp.getWidth() / 2;
        int y = comp.getHeight() / 2;

        select.setVisible(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
     */
    public ActionMenu getContextMenuActions(final Program program) {

        final DeviceIf[] devices = mConfig.getDeviceArray();

        final Window parent = UiUtilities.getLastModalChildOf(getParentFrame());
        
        Action mainaction = new devplugin.ContextMenuAction();
        mainaction.putValue(Action.NAME, mLocalizer.msg("record", "record Program"));
        mainaction.putValue(Action.SMALL_ICON, createImageIcon("mimetypes", "video-x-generic", 16));

        ArrayList actionList = new ArrayList();

        for (int i = 0; i < devices.length; i++) {
            final DeviceIf dev = devices[i];

            Action action = new devplugin.ContextMenuAction();
            action.putValue(Action.NAME, devices[i].getName());

            ArrayList commandList = new ArrayList();

            if (dev.isAbleToAddAndRemovePrograms()) {
                
                if (dev.isInList(program)) {
                    AbstractAction caction = new AbstractAction() {
                        public void actionPerformed(ActionEvent evt) {
                            dev.remove(parent, program);
                            updateMarkedPrograms();
                        }
                    };
                    caction.putValue(Action.NAME, Localizer.getLocalization(Localizer.I18N_DELETE));
                    commandList.add(caction);
                } else {
                    AbstractAction caction = new AbstractAction() {
                        public void actionPerformed(ActionEvent evt) {
                            dev.add(parent, program);
                            updateMarkedPrograms();
                        }
                    };
                    caction.putValue(Action.NAME, mLocalizer.msg("doRecord", "record"));
                    commandList.add(caction);
                }
                
            }
            
            String[] commands = devices[i].getAdditionalCommands();

            if (commands != null)
              for (int y = 0; y < commands.length; y++) {
                
                final int num = y;
                
                AbstractAction caction = new AbstractAction() {

                    public void actionPerformed(ActionEvent evt) {
                        dev.executeAdditionalCommand(parent, num, program);
                    }
                };
                caction.putValue(Action.NAME, commands[y]);
                commandList.add(caction);
              }

            Action[] commandActions = new Action[commandList.size()];
            commandList.toArray(commandActions);

            actionList.add(new ActionMenu(action, commandActions));
        }

        if (actionList.size() == 1) {
          ActionMenu menu = (ActionMenu) actionList.get(0);
          
          if (menu.getSubItems().length==0) {
            return null;
          }
          
          if (menu.getSubItems().length == 1) {
            Action action = menu.getSubItems()[0].getAction();
            action.putValue(Action.SMALL_ICON, createImageIcon("mimetypes", "video-x-generic", 16));
            return new ActionMenu(action);
          } else {
            mainaction.putValue(Action.NAME, menu.getTitle());
            return new ActionMenu(mainaction, menu.getSubItems());
          }
          
        }
        
        ActionMenu[] actions = new ActionMenu[actionList.size()];
        actionList.toArray(actions);

        if (actions.length == 0) {
          return null;
        }
        
        return new ActionMenu(mainaction, actions);
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

            ((Program) list.get(i)).mark(this);
        }

        for (int i = 0; i < mMarkedPrograms.size(); i++) {
            ((Program) mMarkedPrograms.get(i)).unmark(this);
        }

        mMarkedPrograms = list;
        
        updateTreeNode();
    }

    /**
     * This Function Iterates over all Devices and collects the list of Programs
     * to mark...
     * 
     * @return List with all Programs to mark
     */
    private Vector getMarkedByDevices() {
        Vector v = new Vector();

        Iterator devIt = mConfig.getDevices().iterator();

        while (devIt.hasNext()) {
            DeviceIf device = (DeviceIf) devIt.next();

            Program[] programs = device.getProgramList();

            if (programs != null) {
              for (int i = 0; i < programs.length; i++) {
                if (!v.contains(programs[i])) {
                    v.add(programs[i]);
                }
              }
            }
        }

        return v;
    }

    /*
     * (non-Javadoc)
     * 
     * @see devplugin.Plugin#getButtonAction()
     */
    public ActionMenu getButtonAction() {
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                showDialog();
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("CapturePlugin", "Capture Plugin"));
        action.putValue(Action.SMALL_ICON, createImageIcon("mimetypes", "video-x-generic", 16));
        action.putValue(BIG_ICON, createImageIcon("mimetypes", "video-x-generic", 22));

        return new ActionMenu(action);
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

    public ThemeIcon getMarkIconFromTheme() {
      return new ThemeIcon("mimetypes", "video-x-generic", 16);
    }
    
    /**
     * Sets the CaputePluginData
     * 
     * @param data CapturePluginData
     */
    public void setCapturePluginData(CapturePluginData data) {
        mConfig = data;
    }

    /**
     * Returns the CapturePluginData
     * 
     * @return The CapaturePluginData
     */
    public CapturePluginData getCapturePluginData() {
        return mConfig;
    }

    /**
     * Update the TreeNode
     */
    private void updateTreeNode() {
      mRootNode.removeAllChildren();

      Iterator devIt = mConfig.getDevices().iterator();

      while (devIt.hasNext()) {
          DeviceIf device = (DeviceIf) devIt.next();

          PluginTreeNode node = new PluginTreeNode(device.getName());
          
          Program[] programs = device.getProgramList();

          if (programs != null)
            for (int i = 0; i < programs.length; i++) {
              node.addProgram(programs[i]);
            }
          
          mRootNode.add(node);
      }
      
      mRootNode.update();
    }
    
    /**
     * Get the Root-Node. 
     * The CapturePlugin handles all Programs for itself. Some 
     * Devices can remove Programs externaly
     */
    public PluginTreeNode getRootNode() {
      return mRootNode;
    }

    /* (non-Javadoc)
     * @see devplugin.Plugin#canUseProgramTree()
     */
    public boolean canUseProgramTree() {
      return true;
    }
    
}