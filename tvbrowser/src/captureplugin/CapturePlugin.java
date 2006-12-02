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

import captureplugin.drivers.DeviceIf;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;
import util.ui.Localizer;
import util.ui.UiUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

/**
 * Plugin to send the TV-Data to an external program
 *
 * @author Andreas Hessel, Bodo Tasche
 */
public class CapturePlugin extends devplugin.Plugin {

    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePlugin.class);

    /**
     * mData that stores the Settings
     */
    private CapturePluginData mConfig = new CapturePluginData();

    /**
     * Current Marked Programs
     */
    private Vector<Program> mMarkedPrograms = new Vector<Program>();

    /**
     * The Singelton
     */
    private static CapturePlugin mInstance = null;

    /**
     * Root-Node for the Program-Tree
     */
    private PluginTreeNode mRootNode = new PluginTreeNode(this, false);
    private static final String RECORD = "##record";
    private static final String REMOVE = "##remove";

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

        return new PluginInfo(name, desc, author, new Version(2, 10));
    }

    /**
     * Returns a new SettingsTab object, which is added to the settings-window.
     */
    public SettingsTab getSettingsTab() {
        return new CapturePluginSettingsTab((JFrame) getParentFrame(), this);
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

        ArrayList<ActionMenu> actionList = new ArrayList<ActionMenu>();

        for (final DeviceIf dev : devices) {
            Action action = new ContextMenuAction();
            action.putValue(Action.NAME, dev.getName());

            ArrayList<AbstractAction> commandList = new ArrayList<AbstractAction>();

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
                    caction.putValue(Action.NAME, mLocalizer.msg("record", "record"));
                    commandList.add(caction);
                }

            }

            String[] commands = dev.getAdditionalCommands();

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
            ActionMenu menu = actionList.get(0);

            if (menu.getSubItems().length == 0) {
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
        Vector<Program> list = getMarkedByDevices();

        for (Program aList : list) {

            if (mMarkedPrograms.contains(aList)) {
                mMarkedPrograms.remove(aList);
            }

            aList.mark(this);
        }

        for (Program mMarkedProgram : mMarkedPrograms) {
            mMarkedProgram.unmark(this);
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
    private Vector<Program> getMarkedByDevices() {
        Vector<Program> v = new Vector<Program>();

        for (Object o : mConfig.getDevices()) {
            DeviceIf device = (DeviceIf) o;

            Program[] programs = device.getProgramList();

            if (programs != null) {
                for (Program program : programs) {
                    if (!v.contains(program)) {
                        v.add(program);
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

        for (Object o : mConfig.getDevices()) {
            DeviceIf device = (DeviceIf) o;

            PluginTreeNode node = new PluginTreeNode(device.getName());

            Program[] programs = device.getProgramList();

            if (programs != null)
                for (Program program : programs) {
                    node.addProgram(program);
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

    @Override
    public boolean canReceiveProgramsWithTarget() {
        return mConfig.getDevices().size() >0;
    }

    @Override
    public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
        if (receiveTarget == null || receiveTarget.getTargetId() == null ||
            receiveTarget.getTargetId().indexOf("#") == -1)
            return false;

        String id = receiveTarget.getTargetId();
        String deviceid = id.substring(0, id.indexOf("#"));
        String command= id.substring(id.indexOf("#"));

        for (DeviceIf device : mConfig.getDevices()) {
            if (device.getId().equals(deviceid)) {
                if (command.equals(REMOVE)) {
                    for (Program program:programArr) {
                        device.remove(getParentFrame(), program);
                    }
                    updateMarkedPrograms();
                    return true;
                } else if (command.equals(RECORD)) {
                    for (Program program:programArr) {
                        device.add(getParentFrame(), program);
                    }
                    updateMarkedPrograms();
                    return true;
                }

                if (command.startsWith("#_")) {
                    command = command.substring(2);

                    String[] cmdstr = device.getAdditionalCommands();

                    for (int i = 0;i < cmdstr.length;i++) {
                        if (cmdstr[i].equals(command)) {
                            for (Program program:programArr) {
                                device.executeAdditionalCommand(getParentFrame(), i, program);
                            }
                            return true;
                        }
                    }

                }
            }
        }

        return false;
    }

    @Override
    public ProgramReceiveTarget[] getProgramReceiveTargets() {
        ArrayList<ProgramReceiveTarget> targets = new ArrayList<ProgramReceiveTarget>();

        for (DeviceIf device : mConfig.getDevices()) {
            if (device.isAbleToAddAndRemovePrograms()) {
                targets.add(new ProgramReceiveTarget(this, device.getName() + " - " + mLocalizer.msg("record", "record"), device.getId() + RECORD));
                targets.add(new ProgramReceiveTarget(this, device.getName() + " - " + mLocalizer.msg("remove", "remove"), device.getId() + REMOVE));
            }

            for (String command : device.getAdditionalCommands()) {
                targets.add(new ProgramReceiveTarget(this, device.getName() + " - " + command, device.getId() + "#_" + command));
            }
        }

        return targets.toArray(new ProgramReceiveTarget[0]);
    }
}