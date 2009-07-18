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
package captureplugin.drivers.defaultdriver;

import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;
import captureplugin.drivers.utils.ProgramTime;
import captureplugin.drivers.utils.ProgramTimeDialog;
import devplugin.Program;

/**
 * The Default-Device
 */
public class DefaultDevice implements DeviceIf {
    /** Driver */
    private DriverIf mDriver;
    /** Config */
    private DeviceConfig mConfig;
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DefaultDevice.class);

    /**
     * Create the Device
     * @param driver Driver
     * @param name Name of Device
     */
    public DefaultDevice(DriverIf driver, String name) {
        mDriver = driver;
        mConfig = new DeviceConfig();
        mConfig.setName(name);
    }
    
    
    /**
     * Creates a Clone of a DefaultDevice
     * @param device Clone this device
     */
    public DefaultDevice(DefaultDevice device) {
        mDriver = device.getDriver();
        mConfig = (DeviceConfig) device.getConfig().clone();
    }

    /**
     * Returns the Name of this Device
     * @return Name
     */
    public String toString() {
        return mConfig.getName();
    }

    /**
     * Returns the Config
     * @return Config
     */
    public DeviceConfig getConfig() {
        return mConfig;
    }
    
    public DriverIf getDriver() {
        return mDriver;
    }

    public String getId() {
        return mConfig.getId();
    }

    public String getName() {
        return mConfig.getName();
    }

    public String setName(String name) {
        mConfig.setName(name);
        return mConfig.getName();
    }

    public void configDevice(Window owner) {
        
        DefaultKonfigurator config = new DefaultKonfigurator(owner,
        (DeviceConfig) mConfig.clone());
        UiUtilities.centerAndShow(config);
        
        if (config.okWasPressed()) {
            mConfig = config.getConfig();
        }
        
    }

    public boolean isInList(Program program) {
        return mConfig.getMarkedPrograms().contains(program);
    }

    public boolean add(Window parent, Program program) {
        
        if (isInList(program)) {
            return false;
        }
        
        
        if (mConfig.getOnlyFuturePrograms() && (program.isExpired() || program.isOnAir())) {
            
            JOptionPane.showMessageDialog(parent,
                    mLocalizer.msg("OnlyFuture", "Sorry, you are not able to record Programs that are on Air or are expired!\n(See the Settings to enable this)"),
                    Localizer.getLocalization(Localizer.I18N_ERROR),
                    JOptionPane.ERROR_MESSAGE);
            
            return false;
        }
        
        ProgramTime prgTime = new ProgramTime(program);
        
        prgTime.addMinutesToStart(mConfig.getPreTime() * -1);
        prgTime.addMinutesToEnd(mConfig.getPostTime());

        if (mConfig.getMarkedPrograms().getMaxProgramsInTime(prgTime) >= mConfig.getMaxSimultanious()) {
            
            JOptionPane.showMessageDialog(parent,
                    mLocalizer.msg("MaxRecordings", "Sorry, the maximum of simultanious recodings is reached!\n(See the Settings to enable this)"),
                    mLocalizer.msg("Error","Error"),
                    JOptionPane.ERROR_MESSAGE);
            
            return false;            
        }
        
        ProgramTimeDialog cdialog = new ProgramTimeDialog(parent, prgTime, true);
        
        if(mConfig.getShowTitleAndTimeDialog())
          UiUtilities.centerAndShow(cdialog);
        
        prgTime = cdialog.getPrgTime();
        
        if (prgTime == null) {
            return false;
        }
        
        if (mConfig.getMarkedPrograms().getMaxProgramsInTime(prgTime) >= mConfig.getMaxSimultanious()) {
            JOptionPane.showMessageDialog(parent,
                    mLocalizer.msg("MaxRecordings", "Sorry, the maximum of simultanious recodings is reached!\n(See the Settings to enable this)"),
                    mLocalizer.msg("Error","Error"),
                    JOptionPane.ERROR_MESSAGE);
            return false;            
        }
        
        CaptureExecute exec = CaptureExecute.getInstance(parent, mConfig);
        
        if (exec.addProgram(prgTime)) {
            mConfig.getMarkedPrograms().add(prgTime);
            return true;
        }
        
        return false;
    }

    public boolean remove(Window parent, Program program) {
        CaptureExecute exec = CaptureExecute.getInstance(parent, mConfig);
        
        ProgramTime prgTime = mConfig.getMarkedPrograms().getProgamTimeForProgram(program);
        
        if (exec.removeProgram(prgTime)) {
            mConfig.getMarkedPrograms().remove(prgTime);
            return true;
        } else {
            
            int ret = JOptionPane.showConfirmDialog(parent, 
                    mLocalizer.msg("DeleteConfirmOnError", "There was an error while deleting the Program.\nShould I remove it from the List?"), 
                    mLocalizer.msg("Error","Error"),
                    JOptionPane.YES_NO_OPTION);
            
            if (ret == JOptionPane.YES_OPTION) {
                mConfig.getMarkedPrograms().remove(prgTime);
                return true;
            }
        }
        
        
        
        return false;

    }

    public Program[] getProgramList() {
        ProgramTimeList marked = mConfig.getMarkedPrograms();
        
        return marked.getPrograms();
    }

    public Object clone() {
        return new DefaultDevice(this);
    }


    public void writeData(ObjectOutputStream stream) throws IOException {
        mConfig.writeData(stream);
    }


    public void readData(ObjectInputStream stream, boolean importDevice) throws IOException, ClassNotFoundException {
        mConfig.readData(stream, importDevice);
    }


    public boolean isAbleToAddAndRemovePrograms() {
        if (mConfig.getParameterFormatAdd().trim().length()+mConfig.getParameterFormatRem().trim().length() == 0) {
            return false;
        }
        
        return true;
    }


    public String[] getAdditionalCommands() {
        Collection<ParamEntry> commands = mConfig.getEnabledParamList();
        String[] values = new String[commands.size()];
        
        Iterator<ParamEntry> it = commands.iterator();

        int i = 0;
        while (it.hasNext()) {
            values[i] = it.next().toString();
            i++;
        }
        
        return values;
    }


    public boolean executeAdditionalCommand(Window parent,int num, Program program) {
        
        CaptureExecute exec = CaptureExecute.getInstance(parent, mConfig);
        
        ArrayList<ParamEntry> list = new ArrayList<ParamEntry>(mConfig.getEnabledParamList());
        
        if (num <= list.size()) {
            
            ProgramTime time = new ProgramTime(program);
            
            return exec.execute(time, list.get(num).getParam());
        }
        
        return false;
    }
    
    /**
     * Checks the programs marked by this device if there
     * were updates or deletings of programs and returns
     * the deleted programs as array.
     * 
     * @return The deleted programs as array.
     */
    public Program[] checkProgramsAfterDataUpdateAndGetDeleted() {
      ProgramTime[] programTimes = mConfig.getMarkedPrograms().getProgramTimes();
      ArrayList<Program> deleted = new ArrayList<Program>();      
      
      for(ProgramTime pTime : programTimes)
        if(pTime.checkIfRemovedOrUpdateInstead())
          deleted.add(pTime.getProgram());
      
      return deleted.toArray(new Program[deleted.size()]);
    }
    
    /**
     * Gets if programs that were removed during a data
     * update should be deleted automatically.
     * 
     * @return If the programs should be deleted.
     * @since 2.11
     */
    public boolean getDeleteRemovedProgramsAutomatically() {
      return mConfig.getDeleteRemovedPrograms();
    }
    
    /**
     * Removes programs that were deleted during a data update
     * 
     * @param p The program to remove from this device. 
     * @since 2.11
     */
    public void removeProgramWithoutExecution(Program p) {
      ProgramTime prgTime = mConfig.getMarkedPrograms().getProgamTimeForProgram(p);
      mConfig.getMarkedPrograms().remove(prgTime);
    }
}