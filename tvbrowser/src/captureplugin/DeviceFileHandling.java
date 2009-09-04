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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.io.stream.ObjectInputStreamProcessor;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
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
     * @throws java.io.IOException Problems in delete operation
     */
    public void clearDirectory() throws IOException{
        
        File dirname = new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome() + File.separator + "CaptureDevices");
        
        if (!dirname.exists()) {
            dirname.mkdir();
        }
        
        File[] fileList = dirname.listFiles();
        if ((fileList != null) && (fileList.length > 0)) {
            for (File file : fileList) {
                file.delete();
            }
        }
        
        mCount = 0;
    }

    /**
     * Saves a Device
     * @param dev Device to Save
     * @return Filename
     * @throws java.io.IOException Problems reading the Device
     */
    public String writeDevice(final DeviceIf dev) throws IOException {
        
        if (mCount == -1) {
            return null;
        }
        mCount++;
        
        File data = new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome()  + File.separator +
                "CaptureDevices" + File.separator + mCount + ".dat");
        StreamUtilities.objectOutputStream(data, new ObjectOutputStreamProcessor() {
      public void process(ObjectOutputStream stream) throws IOException {
        dev.writeData(stream);
        stream.close();
      }
    });
        
        return data.getName();
    }

    /**
     * Loads a Device
     * @param classname Driver-Classname
     * @param filename Filename
     * @param devname Name of the Device
     * @return DeviceIf
     * @throws java.io.IOException  Problems while reading the device
     * @throws ClassNotFoundException Class creation problems
     */
    public DeviceIf readDevice(String classname, String filename, String devname) throws IOException, ClassNotFoundException {
        final DeviceIf dev = DriverFactory.getInstance().createDevice(classname, devname);
        
        if (dev == null) {
            return null;
        }
        
        File data = new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome()  + File.separator +
                "CaptureDevices" + File.separator + filename);
        StreamUtilities.objectInputStreamIgnoringExceptions(data, 0x2000,
        new ObjectInputStreamProcessor() {

          @Override
          public void process(final ObjectInputStream inputStream) throws IOException {
            try {
              dev.readData(inputStream, false);
            } catch (ClassNotFoundException e) {
              e.printStackTrace();
            }
          }});
        
        return dev;
    }

}