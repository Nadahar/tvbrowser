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

import java.awt.Dimension;
import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.CapturePlugin;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;
import captureplugin.drivers.dreambox.connector.DreamboxChannel;
import captureplugin.drivers.dreambox.connector.DreamboxConnector;
import captureplugin.drivers.utils.ProgramTime;
import captureplugin.drivers.utils.ProgramTimeDialog;
import captureplugin.utils.ExternalChannelIf;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;

/**
 * The Dreambox-Device
 */
public final class DreamboxDevice implements DeviceIf {

    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DreamboxDevice.class);

    /**
     * Driver
     */
    private DreamboxDriver mDriver;
    /**
     * Name for this Device
     */
    private String mName;
    /**
     * Configuration for this Device
     */
    private DreamboxConfig mConfig;
    /**
     * List of Recordings
     */
    private ArrayList<ProgramTime> mProgramTimeList = new ArrayList<ProgramTime>();
    /**
     * List of Recordings
     */
    private ArrayList<Program> mProgramList = new ArrayList<Program>();

    /**
     * Creates this Device
     *
     * @param dreamboxDriver Driver for the Dreambox
     * @param name           Name for this Device
     */
    public DreamboxDevice(DreamboxDriver dreamboxDriver, String name) {
        mDriver = dreamboxDriver;
        mName = name;
        mConfig = new DreamboxConfig();
    }

    /**
     * Clones another dreambox device
     *
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
    @Override
    public String getId() {
        return mConfig.getId();
    }

    /**
     * @return Name for this Device
     */
    @Override
    public String getName() {
        return mName;
    }

    /**
     * Set the new Name for this Device
     *
     * @param name new Name
     * @return new Name
     */
    @Override
    public String setName(String name) {
        mName = name;
        return mName;
    }

    /**
     * @return Driver for this Device
     */
    @Override
    public DriverIf getDriver() {
        return mDriver;
    }

    /**
     * Opens a configure dialog for this device
     *
     * @param parent Parent for the dialog
     */
    @Override
    public void configDevice(Window parent) {
        DreamboxConfigDialog dialog = new DreamboxConfigDialog(parent, this,
        mConfig);

        CapturePlugin.getInstance().layoutWindow("dreamboxConfig", dialog, new Dimension(400,500));
        dialog.setVisible(true);

        if (dialog.wasOkPressed()) {
            mName = dialog.getDeviceName();
            mConfig = dialog.getConfig();
        }
    }

    /**
     * @see captureplugin.drivers.DeviceIf#isInList(devplugin.Program)
     */
    @Override
    public boolean isInList(Program program) {
        return mProgramList.contains(program);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#isAbleToAddAndRemovePrograms()
     */
    @Override
    public boolean isAbleToAddAndRemovePrograms() {
        return true;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#add(java.awt.Window,devplugin.Program)
     */
    @Override
    public boolean add(Window parent, Program program) {

        if (program.isExpired()) {
            JOptionPane.showMessageDialog(parent,
                    mLocalizer.msg("expiredText","This program has expired. It's not possible to record it.\nWell, unless you have a time-machine."),
                    mLocalizer.msg("expiredTitle","Expired"),
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        final DreamboxChannel channel = (DreamboxChannel) mConfig.getExternalChannel(program.getChannel());

        if (channel == null) {
            int ret = JOptionPane.showConfirmDialog(parent,
                    mLocalizer.msg("notConfiguredText", "Channel not configured, do\nyou want to do this now?"),
                    mLocalizer.msg("notConfiguredTitle", "Configure"), JOptionPane.YES_NO_OPTION);

            if (ret == JOptionPane.YES_OPTION) {
                configDevice(parent);
            }
        } else {
            ProgramTimeDialog dialog;

            ProgramTime time = new ProgramTime(program);

            Calendar start = time.getStartAsCalendar();
            start.add(Calendar.MINUTE, mConfig.getPreTime()*-1);
            time.setStart(start.getTime());

            Calendar end = time.getEndAsCalendar();
            end.add(Calendar.MINUTE, mConfig.getAfterTime());
            time.setEnd(end.getTime());

            JComboBox box = new JComboBox(new String[] {
                    mLocalizer.msg("afterEventNothing", "Nothing"),
                    mLocalizer.msg("afterEventStandby", "Standby"),
                    mLocalizer.msg("afterEventDeepstandby", "Deepstandby"),
                    mLocalizer.msg("afterEventAuto", "Auto")});

            dialog = new ProgramTimeDialog(parent, time, false, mLocalizer.msg(
          "afterEventTitle", "After recording"), box);

            UiUtilities.centerAndShow(dialog);

            if (dialog.getPrgTime() != null) {
                DreamboxConnector connector = new DreamboxConnector(mConfig);

                // try to add a new timer to the timer list
                boolean successful =
                	connector.addRecording(channel, dialog.getPrgTime(),
										   box.getSelectedIndex(), mConfig.getTimeZone());

                if (!successful) {

                	JOptionPane.showMessageDialog(parent,
                		mLocalizer.msg("failedText", "Programming operation failed:\n{0}", connector.getLastStateText()),
                		mLocalizer.msg("failedTitle", "Failed"),
                		JOptionPane.INFORMATION_MESSAGE);

                	return false;
                }

                return true;
            }
        }

        return false;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#remove(java.awt.Window,devplugin.Program)
     */
    @Override
    public boolean remove(Window parent, Program program) {

        for (ProgramTime time : mProgramTimeList) {

            if (time.getProgram().equals(program)) {

                ExternalChannelIf channel = mConfig.getExternalChannel(program.getChannel());

                if (channel != null) {
                    DreamboxConnector connector = new DreamboxConnector(mConfig);

                    // try to remove a new timer from the timer list
                    boolean successful = connector.removeRecording((DreamboxChannel) channel, time, mConfig.getTimeZone());

                    if (!successful) {

                    	JOptionPane.showMessageDialog(parent,
                    		mLocalizer.msg("failedText", "Programming operation failed:\n{0}", connector.getLastStateText()),
                    		mLocalizer.msg("failedTitle", "Failed"),
                    		JOptionPane.INFORMATION_MESSAGE);

                    	return false;
                    }

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#getProgramList()
     */
    @Override
    public Program[] getProgramList() {
        DreamboxConnector con = new DreamboxConnector(mConfig);
        if (mConfig.hasValidAddress()) {
            ProgramTime[] times = con.getRecordings(mConfig);
            mProgramTimeList = new ArrayList<ProgramTime>(Arrays.asList(times));

            mProgramList = new ArrayList<Program>();

            for (ProgramTime time : times) {
                mProgramList.add(time.getProgram());
            }

            return mProgramList.toArray(new Program[mProgramList.size()]);
        }

        return null;
    }

   /**
     * @see captureplugin.drivers.DeviceIf#getAdditionalCommands()
     */
    @Override
    public String[] getAdditionalCommands() {
       return new String[]{mLocalizer.msg("switch", "Switch channel"), mLocalizer.msg("sendMessage", "Send as Message"), mLocalizer.msg("streamChannel", "Open channel with mediaplayer")};
    }

    /**
     * @see captureplugin.drivers.DeviceIf#executeAdditionalCommand(java.awt.Window,int,devplugin.Program)
     */
    @Override
    public boolean executeAdditionalCommand(Window parent, int num, Program program) {
        if (num == 0) {
            final DreamboxChannel channel = (DreamboxChannel) mConfig.getExternalChannel(program.getChannel());

            if (channel != null) {
                new Thread(new Runnable() {
                    public void run() {
                        DreamboxConnector connect = new DreamboxConnector(mConfig);
                        connect.switchToChannel(channel);
                    }
                }).start();
            } else {
                int ret = JOptionPane.showConfirmDialog(parent,
                        mLocalizer.msg("notConfiguredText", "Channel not configured, do\nyou want to do this now?"),
                        mLocalizer.msg("notConfiguredTitle", "Configure"), JOptionPane.YES_NO_OPTION);

                if (ret == JOptionPane.YES_OPTION) {
                    configDevice(parent);
                }
            }
            return true;
        } else if (num == 1) {
            DreamboxConnector connect = new DreamboxConnector(mConfig);

            ParamParser parser = new ParamParser();

            connect.sendMessage(parser.analyse("{channel_name} - {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n{title}", program));
        } else if (num == 2) {
            final DreamboxChannel channel = (DreamboxChannel) mConfig.getExternalChannel(program.getChannel());

            if (channel != null) {
                DreamboxConnector connect = new DreamboxConnector(mConfig);
                if (!connect.streamChannel(channel)) {
                    int ret = JOptionPane.showConfirmDialog(parent,
                        mLocalizer.msg("mediaplayerNotConfiguredText", "Unfortunately, a problem occurred during executing the mediaplayer,\ndo you want to correct the configuration now?"),
                        mLocalizer.msg("mediaplayerNotConfiguredTitle", "Configure"), JOptionPane.YES_NO_OPTION);
                    if (ret == JOptionPane.YES_OPTION) {
                        configDevice(parent);
                    }
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        return new DreamboxDevice(this);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#writeData(java.io.ObjectOutputStream)
     */
    @Override
    public void writeData(ObjectOutputStream stream) throws IOException {
        mConfig.writeData(stream);
    }

    /**
     * @see captureplugin.drivers.DeviceIf#readData(java.io.ObjectInputStream, boolean)
     */
    @Override
    public void readData(ObjectInputStream stream, boolean importDevice) throws IOException, ClassNotFoundException {
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
    @Override
    public boolean getDeleteRemovedProgramsAutomatically() {
        return true;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#removeProgramWithoutExecution(devplugin.Program)
     */
    @Override
    public void removeProgramWithoutExecution(Program p) {
        for (ProgramTime time : mProgramTimeList) {
            if (time.getProgram().equals(p)) {
                DreamboxChannel channel = (DreamboxChannel) mConfig.getExternalChannel(p.getChannel());
                if (channel != null) {
                    DreamboxConnector connector = new DreamboxConnector(mConfig);
                    connector.removeRecording(channel, time, mConfig.getTimeZone());
                }
            }
        }
    }

    /**
     * @see captureplugin.drivers.DeviceIf#getProgramForProgramInList(devplugin.Program)
     */
    @Override
    public Program getProgramForProgramInList(Program p) {
      for(ProgramTime time : mProgramTimeList) {
        for(Program prog : time.getAllPrograms()) {
          if(prog.equals(p)) {
            return time.getProgram();
          }
        }
      }

      return null;
    }

    /**
     * @see captureplugin.drivers.DeviceIf#sendProgramsToReceiveTargets(devplugin.Program[])
     */
    @Override
    public void sendProgramsToReceiveTargets(Program[] progs) {
      ProgramReceiveTarget[] targets = mConfig.getProgramReceiveTargets();

      for(ProgramReceiveTarget target : targets) {
        target.receivePrograms(progs);
      }
    }
}