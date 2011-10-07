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
 *     $Date: 2010-07-17 14:17:27 +0200 (Sa, 17 Jul 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6681 $
 */
package captureplugin.drivers;

import java.util.ArrayList;

import util.misc.OperatingSystem;
import captureplugin.drivers.defaultdriver.DefaultDriver;
import captureplugin.drivers.dreambox.DreamboxDriver;
import captureplugin.drivers.topfield.TopfieldDriver;

/**
 * This Factory returns all availabe Drivers and creates a Device
 * 
 * @author bodum
 */
public class DriverFactory {
  /** Singleton */
  private static DriverFactory mFactory;

  /** Private */
  private DriverFactory() {
  }

  /**
   * Returns the DriverFactory
   * 
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
   * 
   * @return All available Drivers
   */
  public DriverIf[] getDrivers() {
    ArrayList<DriverIf> drivers = new ArrayList<DriverIf>();

    drivers.add(new DefaultDriver());
    drivers.add(new DreamboxDriver());
    drivers.add(new TopfieldDriver());

    if (OperatingSystem.isMacOs()) {
      try {
        DriverIf driver = (DriverIf) this.getClass().getClassLoader().loadClass(
            "captureplugin.drivers.elgatodriver.ElgatoDriver").newInstance();
        if (driver != null) {
          drivers.add(driver);
        }

        driver = (DriverIf) this.getClass().getClassLoader().loadClass(
            "captureplugin.drivers.thetubedriver.TheTubeDriver").newInstance();
        if (driver != null) {
          drivers.add(driver);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return drivers.toArray(new DriverIf[drivers.size()]);
  }

  /**
   * Creates a Device
   * 
   * @param classname Classname of Driver
   * @param devname Name of Device
   * @return created Device
   */
  public DeviceIf createDevice(String classname, String devname) {

    DriverIf[] drivers = getDrivers();

    for (DriverIf driver : drivers) {
      if (driver.getClass().getName().equals(classname)) {
        return driver.createDevice(devname);
      }
    }

    return null;
  }

}