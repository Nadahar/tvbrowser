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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JOptionPane;

import util.ui.Localizer;
import captureplugin.drivers.DeviceIf;


/**
 * This Class contains all needed Data
 */
public class CapturePluginData implements Cloneable {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePluginData.class);

    /**
     * All Devices
     */
    private Vector mDevices = new Vector();
    
    /**
     * Default Constructor
     */
    public CapturePluginData() {
        
    }

    /**
     * Copies Settings from another Data-Object
     * @param data Data object to copy
     */
    public CapturePluginData(CapturePluginData data) {
        
      mDevices = new Vector();
      
      Vector old = new Vector(data.getDevices());
      
      for (int i = 0; i < old.size(); i++) {
          DeviceIf dev = (DeviceIf) old.get(i);
          mDevices.add(dev.clone());
      }
    }
    
    
    /**
     * Writes the Data to an OuputStream
     * 
     * @param out write here
     * @throws Exception
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeInt(2);
        
        out.writeInt(mDevices.size());
        
        DeviceFileHandling writer = new DeviceFileHandling();
        
        writer.clearDirectory();
        
        for (int i = 0; i < mDevices.size(); i++) {
            
            DeviceIf dev = (DeviceIf) mDevices.get(i);
            
            out.writeObject(dev.getDriver().getClass().getName());
            out.writeObject(dev.getName());
            
            out.writeObject(writer.writeDevice(dev));
        }
    }

    /**
     * Loads the Data from an InputStream
     * 
     * @param in InputStream
     * @param p Plugin
     * @throws Exception
     */
    public void readData(ObjectInputStream in, CapturePlugin p) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        
        if (version < 2) {
            return;
        }
        
        int num = in.readInt();
        
        mDevices = new Vector();
        
        DeviceFileHandling reader = new DeviceFileHandling();

        for (int i = 0; i < num; i++) {
            String classname = (String) in.readObject();
            String devname = (String)in.readObject();
            String filename = (String)in.readObject();
            try {
                DeviceIf dev = reader.readDevice(classname, filename, devname);
                
                if (dev != null) {
                    mDevices.add(dev);
                }
            } catch (Throwable e) {
                JOptionPane.showMessageDialog(null, 
                        mLocalizer.msg("ProblemDevice", "Problems while loading Device {0}.", devname),
                        mLocalizer.msg("Problems","Problems"), JOptionPane.ERROR_MESSAGE);
            }
            
        }
        
    }

    /**
     * Returns all Devices
     * @return Devices
     */
    public Collection getDevices() {
        return mDevices;
    }
    
    /**
     * Returns all Devices as Array
     * @return Devices
     */
    public DeviceIf[] getDeviceArray() {
        DeviceIf[] dev = new DeviceIf[mDevices.size()];
        
        for (int i = 0; i < mDevices.size(); i++) {
            dev[i] = (DeviceIf) mDevices.get(i);
        }
        
        return dev;
    }

    /**
     * Clones this Object
     */
    public Object clone() {
        return new CapturePluginData(this);
    }


}