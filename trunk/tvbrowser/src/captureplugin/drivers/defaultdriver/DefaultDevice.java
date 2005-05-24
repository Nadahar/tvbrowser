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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;
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
     * @param device
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
    
    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#getDriver()
     */
    public DriverIf getDriver() {
        return mDriver;
    }

    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#getName()
     */
    public String getName() {
        return mConfig.getName();
    }

    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#setName(java.lang.String)
     */
    public String setName(String name) {
        mConfig.setName(name);
        return mConfig.getName();
    }

    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#configDevice(javax.swing.JFrame)
     */
    public void configDevice(Window owner) {
        
        
        DefaultKonfigurator config;
        
        
        if (owner instanceof JDialog) {
            config = new DefaultKonfigurator((JDialog)owner, (DeviceConfig) mConfig.clone());
        } else {
            config = new DefaultKonfigurator((JFrame) owner, (DeviceConfig) mConfig.clone());
        }
        UiUtilities.centerAndShow(config);
        
        if (config.okWasPressed()) {
            mConfig = config.getConfig();
        }
        
    }

    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#isInList(devplugin.Program)
     */
    public boolean isInList(Program program) {
        return mConfig.getMarkedPrograms().contains(program);
    }

    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#add(devplugin.Program)
     */
    public boolean add(Window parent, Program program) {
        
        if (isInList(program)) {
            return false;
        }
        
        
        if (mConfig.getOnlyFuturePrograms() && (program.isExpired() || program.isOnAir())) {
            
            JOptionPane.showMessageDialog(parent,
                    mLocalizer.msg("OnlyFuture", "Sorry, you are not able to record Programs that are on Air or are expired!\n(See the Settings to enable this)"),
                    mLocalizer.msg("Error","Error"),
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
        
        CaptureTimeDialog cdialog;
        
        if (parent instanceof JDialog) {
            cdialog = new CaptureTimeDialog((JDialog)parent, prgTime);
        } else {
            cdialog = new CaptureTimeDialog((JFrame)parent, prgTime);
        }
        
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
        
        CaptureExecute exec = new CaptureExecute(parent, mConfig);
        
        if (exec.addProgram(prgTime)) {
            mConfig.getMarkedPrograms().add(prgTime);
            return true;
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#remove(devplugin.Program)
     */
    public boolean remove(Window parent, Program program) {
        CaptureExecute exec = new CaptureExecute(parent, mConfig);
        
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
                mConfig.getMarkedPrograms(). remove(prgTime);
                return true;
            }
        }
        
        
        
        return false;

    }

    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#getProgramList()
     */
    public Program[] getProgramList() {
        ProgramTimeList marked = mConfig.getMarkedPrograms();
        
        return marked.getPrograms();
    }

    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#clone()
     */
    public Object clone() {
        return new DefaultDevice(this);
    }


    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#writeData(java.io.ObjectOutputStream)
     */
    public void writeData(ObjectOutputStream stream) throws IOException {
        mConfig.writeData(stream);
    }


    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#readData(java.io.ObjectInputStream)
     */
    public void readData(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        mConfig.readData(stream);
    }


    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#isAbleToAddPrograms()
     */
    public boolean isAbleToAddAndRemovePrograms() {
        if (mConfig.getParameterFormatAdd().trim().length()+mConfig.getParameterFormatRem().trim().length() == 0) {
            return false;
        }
        
        return true;
    }


    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#getAdditionalCommands(devplugin.Program)
     */
    public String[] getAdditionalCommands() {
        Collection commands = mConfig.getEnabledParamList();
        String[] values = new String[commands.size()];
        
        Iterator it = commands.iterator();

        int i = 0;
        while (it.hasNext()) {
            values[i] = it.next().toString();
            i++;
        }
        
        return values;
    }


    /* (non-Javadoc)
     * @see captureplugin.drivers.DeviceIf#executeAdditionalCommand(int, devplugin.Program)
     */
    public boolean executeAdditionalCommand(Window parent,int num, Program program) {
        
        CaptureExecute exec = new CaptureExecute(parent, mConfig);
        
        ArrayList list = new ArrayList(mConfig.getEnabledParamList());
        
        if (num <= list.size()) {
            
            ProgramTime time = new ProgramTime(program);
            
            return exec.execute(time, ((ParamEntry)list.get(num)).getParam());
        }
        
        return false;
    }
}