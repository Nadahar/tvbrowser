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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverFactory;
import devplugin.Plugin;


/**
 * Loads/Saves the Devices
 * 
 * @author bodum
 */
public class DeviceFileHandling {

    /** File-Counter */
    private int mCount = -1;
    
    /**
     * Creates a empty Directory
     */
    public void clearDirectory() throws IOException{
        
        File dirname = new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome() + File.separator + "CaptureDevices");
        
        if (!dirname.exists()) {
            dirname.mkdir();
        }
        
        if (dirname.listFiles().length > 0) {
            File[] fileList = dirname.listFiles();
            
            for (int i = 0; i < fileList.length; i++) {
                fileList[i].delete();
            }
        }
        
        mCount = 0;
    }

    /**
     * Saves a Device
     * @param dev Device to Save
     * @return Filename
     */
    public String writeDevice(DeviceIf dev) throws IOException {
        
        if (mCount == -1) {
            return null;
        }
        mCount++;
        
        File data = new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome()  + File.separator + 
                "CaptureDevices" + File.separator + mCount + ".dat");
        
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(data));
        
        dev.writeData(stream);
        
        stream.close();
        
        return data.getName();
    }

    /**
     * Loads a Device
     * @param classname Driver-Classname
     * @param filename Filename
     * @param devname Name of the Device
     * @return DeviceIf
     */
    public DeviceIf readDevice(String classname, String filename, String devname) throws IOException, ClassNotFoundException {
        DeviceIf dev = DriverFactory.getInstance().createDevice(classname, devname);
        
        if (dev == null) {
            return null;
        }
        
        File data = new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome()  + File.separator + 
                "CaptureDevices" + File.separator + filename);
        
        ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(data), 0x2000));
        
        dev.readData(stream);
        
        stream.close();
        return dev;
    }

}