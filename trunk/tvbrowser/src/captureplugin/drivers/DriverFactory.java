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
package captureplugin.drivers;

import captureplugin.drivers.defaultdriver.DefaultDriver;
import captureplugin.drivers.pinnacle.PinnacleDriver;


/**
 * This Factory returns all availabe Drivers and
 * creates a Device
 * 
 * @author bodum
 */
public class DriverFactory {
    /** Singleton */
    private static DriverFactory mFactory;
    
    /** Private */
    private DriverFactory() { }
    
    /**
     * Returns the DriverFactory
     * @return DriverFactory
     */
    public static DriverFactory getInstance() {
        
        if (mFactory == null) {
            mFactory = new DriverFactory();
        }
        
        return mFactory;
    }
    
    
    /**
     * Returns all available Drivers
     * @return All available Drivers
     */
    public DriverIf[] getDrivers() {
        
        DriverIf[] drivers = { 
                new DefaultDriver()//,       new PinnacleDriver()
        };
        
        return drivers;
    }
    
    /**
     * Creates a Device
     * @param classname Classname of Driver
     * @param devname Name of Device
     * @return created Device
     */
    public DeviceIf createDevice(String classname, String devname) {
        
        DriverIf[] drivers = getDrivers();
        
        for (int i = 0; i < drivers.length; i++) {
            if (drivers[i].getClass().getName().equals(classname)) {
                return drivers[i].createDevice(devname);
            }
        }
        
        return null;
    }
    
}