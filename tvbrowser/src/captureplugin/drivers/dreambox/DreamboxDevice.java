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
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */
package captureplugin.drivers.dreambox;

import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;
import captureplugin.drivers.dreambox.configdialog.DreamboxConfigDialog;
import captureplugin.drivers.dreambox.connector.DreamboxChannel;
import captureplugin.drivers.dreambox.connector.DreamboxConnector;
import devplugin.Program;
import util.ui.Localizer;
import util.ui.UiUtilities;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The Dreambox-Device
 */
public class DreamboxDevice implements DeviceIf {
    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DreamboxDevice.class);

    /** Driver */
    private DreamboxDriver mDriver;
    /** Name for this Device */
    private String mName;
    /** Configuration for this Device */
    private DreamboxConfig mConfig;
    /** List of Recordings */
    private ArrayList<Program> _programList = new ArrayList<Program>();

    /**
     * Creates this Device
     * @param dreamboxDriver Driver for the Dreambox
     * @param name Name for this Device
     */
    public DreamboxDevice(DreamboxDriver dreamboxDriver, String name) {
        mDriver = dreamboxDriver;
        mName = name;
        mConfig = new DreamboxConfig();
    }

    /**
     * Clones another dreambox device
     * @param dreamboxDevice Device to clone
     */
    public DreamboxDevice(DreamboxDevice dreamboxDevice) {
        mDriver = (DreamboxDriver) dreamboxDevice.getDriver();
        mName = dreamboxDevice.getName();
        mConfig = dreamboxDevice.getConfig().clone();
    }

    /**
     * @return Configuration for this device
     */
    private DreamboxConfig getConfig() {
        return mConfig;
    }

    /**
     * @return ID for this Device
     */
    public String getId() {
        return mConfig.getId();
    }

    /**
     * @return Name for this Device
     */
    public String getName() {
        return mName;
    }

    /**
     * Set the new Name for this Device
     * @param name new Name
     * @return new Name
     */
    public String setName(String name) {
        mName = name;
        return mName;
    }

    /**
     * @return Driver for this Device
     */
    public DriverIf getDriver() {
        return mDriver;
    }

    /**
     * Opens a configure dialog for this device
     *
     * @param parent Parent for the dialog
     */
    public void configDevice(Window parent) {
        DreamboxConfigDialog dialog;

        if (parent instanceof JDialog) {
            dialog = new DreamboxConfigDialog((JDialog) parent, this, mConfig);
        } else {
            dialog = new DreamboxConfigDialog((JFrame) parent, this, mConfig);
        }

        UiUtilities.centerAndShow(dialog);

        if (dialog.wasOkPressed()) {
            mName = dialog.getDeviceName();
            mConfig = dialog.getConfig();
        }
    }

    /**
     * @see captureplugin.drivers.DeviceIf#isInList(devplugin.Program)
     */
    public boolean isInList(Program program) {
        return _programList.contains(program);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#isAbleToAddAndRemovePrograms()
     */
    public boolean isAbleToAddAndRemovePrograms() {
        return true;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#add(java.awt.Window, devplugin.Program)
     */
    public boolean add(Window parent, Program program) {
        return false;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#remove(java.awt.Window, devplugin.Program)
     */
    public boolean remove(Window parent, Program program) {
        return false;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#getProgramList()
     */
    public Program[] getProgramList() {
        DreamboxConnector con = new DreamboxConnector(mConfig.getDreamboxAddress());

        Program[] ret = con.getRecordings(mConfig);
        _programList = new ArrayList<Program>(Arrays.asList(ret));

        return ret;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#getAdditionalCommands()
     */
    public String[] getAdditionalCommands() {
        return new String[]{mLocalizer.msg("switch", "Switch channel")};
    }

    /**
     * @see captureplugin.drivers.DeviceIf#executeAdditionalCommand(java.awt.Window, int, devplugin.Program)
     */
    public boolean executeAdditionalCommand(Window parent, int num, Program program) {
        if (num == 0) {
            final DreamboxChannel channel =  mConfig.getDreamboxChannel(program.getChannel());

            if (channel != null) {
                new Thread(new Runnable() {
                    public void run() {
                        DreamboxConnector connect = new DreamboxConnector(mConfig.getDreamboxAddress());
                        connect.switchToChannel(channel);
                    }
                }).start();
            } else {
                int ret = JOptionPane.showConfirmDialog(parent,
                        mLocalizer.msg("notConfiguredText","Channel not configured, do\nyou want to do this now?"),
                        mLocalizer.msg("notConfiguredTitle","Configure"), JOptionPane.YES_NO_OPTION);

                if (ret == JOptionPane.YES_OPTION) {
                    configDevice(parent);
                }
            }
            return true;
        }
        return false;
    }

    public Object clone() {
        return new DreamboxDevice(this);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#writeData(java.io.ObjectOutputStream)
     */
    public void writeData(ObjectOutputStream stream) throws IOException {
        mConfig.writeData(stream);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#readData(java.io.ObjectInputStream)
     */
    public void readData(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        mConfig = new DreamboxConfig(stream);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#checkProgramsAfterDataUpdateAndGetDeleted()
     */
    public Program[] checkProgramsAfterDataUpdateAndGetDeleted() {
        return new Program[0];
    }

    /**
     * @see captureplugin.drivers.DeviceIf#getDeleteRemovedProgramsAutomatically()
     */
    public boolean getDeleteRemovedProgramsAutomatically() {
        return true;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#removeProgramWithoutExecution(devplugin.Program)
     */
    public void removeProgramWithoutExecution(Program p) {
    }
}