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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.simpledevice;

import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;
import captureplugin.drivers.utils.ProgramTime;
import captureplugin.drivers.utils.ProgramTimeDialog;
import devplugin.Channel;
import devplugin.Program;

/**
 * The Applescript-Device
 *
 * @author bodum
 */
public final class SimpleDevice implements DeviceIf {
    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer
            .getLocalizerFor(SimpleDevice.class);

    /**
     * Driver
     */
    private DriverIf mDriver;

    /**
     * Connection
     */
    private SimpleConnectionIf mConnection;

    /**
     * Name of Device
     */
    private String mName;

    /**
     * Configuration
     */
    private SimpleConfig mConfig = new SimpleConfig();

    /**
     * List of Recordings
     */
    private Program[] mListOfRecordings;

    public SimpleDevice(SimpleConnectionIf connection, DriverIf driver, String name) {
        mDriver = driver;
        mName = name;
        mConnection = connection;
    }

    public SimpleDevice(SimpleDevice device) {
        mDriver = device.getDriver();
        mName = device.getName();
        mConfig = (SimpleConfig) device.getConfig().clone();
        mConnection = device.getConnection();
    }

    private SimpleConfig getConfig() {
        return mConfig;
    }

    public DriverIf getDriver() {
        return mDriver;
    }

    public String getId() {
        return mConfig.getId();
    }

    public String getName() {
        return mName;
    }

    public String setName(String name) {
        mName = name;
        return mName;
    }

    public void configDevice(Window parent) {
        SimpleConfigDialog dialog = new SimpleConfigDialog(parent, this,
        mConnection, mConfig);
        UiUtilities.centerAndShow(dialog);

        if (dialog.wasOkPressed()) {
            mName = dialog.getName();
            mConfig = dialog.getConfig();
        }
    }

    public String[] getAdditionalCommands() {
        return new String[]{mLocalizer.msg("switchChannel",
                "Switch to Channel"),};
    }

    public boolean executeAdditionalCommand(Window parent, int num,
                                            Program program) {
        if (num == 0) {
            if (testConfig(parent, program.getChannel())) {
                mConnection.switchToChannel(mConfig, program.getChannel());
            }
        }

        return false;
    }

    public boolean isAbleToAddAndRemovePrograms() {
        return true;
    }

    public Program[] getProgramList() {
        mListOfRecordings = mConnection.getAllRecordings(mConfig);
        return mListOfRecordings.clone();
    }

    public boolean isInList(Program program) {
        if (mListOfRecordings == null) {
            mListOfRecordings = mConnection.getAllRecordings(mConfig);
        }
        return Arrays.asList(mListOfRecordings).contains(program);
    }

    public boolean add(Window parent, Program program) {
        if (testConfig(parent, program.getChannel())) {
            ProgramTime time = new ProgramTime(program);
            ProgramTimeDialog dialog = new ProgramTimeDialog(parent, time, false);
            UiUtilities.centerAndShow(dialog);

            if (dialog.getPrgTime() != null) {
                return mConnection.addToRecording(mConfig, dialog.getPrgTime());
            }
        }
        return false;
    }

    public boolean remove(Window parent, Program program) {
        mConnection.removeRecording(mConfig, program);
        return true;
    }

    /**
     * Test if the Channel is in the Configuration. If not a
     * Dialog is shown
     *
     * @param parent Parent Dialog
     * @param ch     Channel to check
     * @return true if Channel is in Config
     */
    private boolean testConfig(Window parent, Channel ch) {
        if (mConfig.getExternalChannel(ch) == null) {
            int ret = JOptionPane.showConfirmDialog(parent, mLocalizer.msg("channelAssign", "Please assign Channel first"), mLocalizer.msg("channelAssignTitle", "Assign Channel"), JOptionPane.YES_NO_OPTION);

            if (ret == JOptionPane.YES_OPTION) {
                SimpleConfigDialog dialog = new SimpleConfigDialog(parent, this,
            mConnection, mConfig);
                UiUtilities.centerAndShow(dialog);

                if (dialog.wasOkPressed()) {
                    mConfig = dialog.getConfig();
                    mName = dialog.getName();
                }
            }
            return false;
        }

        return true;
    }

    public void readData(ObjectInputStream stream, boolean importDevice) throws IOException,
            ClassNotFoundException {
        mConfig = new SimpleConfig(stream);
    }

    public void writeData(ObjectOutputStream stream) throws IOException {
        mConfig.writeData(stream);
    }

    public Object clone() {
        return new SimpleDevice(this);
    }

    public Program[] checkProgramsAfterDataUpdateAndGetDeleted() {
        ArrayList<Program> deletedPrograms = new ArrayList<Program>();

        for (Program p : mListOfRecordings) {
            if (p.getProgramState() == Program.WAS_DELETED_STATE) {
              deletedPrograms.add(p);
            }
        }

        return deletedPrograms.toArray(new Program[deletedPrograms.size()]);
    }

    /**
     * Gets if programs that were removed during a data
     * update should be deleted automatically.
     *
     * @return If the programs should be deleted.
     * @since 2.11
     */
    public boolean getDeleteRemovedProgramsAutomatically() {
        return true;
    }

    /**
     * Removes programs that were deleted during a data update
     *
     * @param p The program to remove from this device.
     * @since 2.11
     */
    public void removeProgramWithoutExecution(Program p) {
        mConnection.removeRecording(mConfig, p);
    }

    /**
     * @return ApplescriptConnection that is used for this device
     * @since 2.6
     */
    public SimpleConnectionIf getConnection() {
        return mConnection;
    }
    
    @Override
    public Program getProgramForProgramInList(Program p) {
      if (mListOfRecordings == null) {
        mListOfRecordings = mConnection.getAllRecordings(mConfig);
      }
      
      for(Program prog : mListOfRecordings) {
        if(prog.equals(p)) {
          return prog;
        }
      }
      
      return null;
    }

    @Override
    public void sendProgramsToReceiveTargets(Program[] progs) {
      // TODO Auto-generated method stub
      
    }
}