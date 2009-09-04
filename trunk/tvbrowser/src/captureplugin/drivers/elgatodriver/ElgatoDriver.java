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
package captureplugin.drivers.elgatodriver;

import util.ui.Localizer;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;
import captureplugin.drivers.simpledevice.SimpleDevice;

/**
 * The Driver for the Elgato EyeTV Software
 * 
 * @author bodum
 */
public class ElgatoDriver implements DriverIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ElgatoDriver.class);
  
  public DeviceIf createDevice(String name) {
    return new SimpleDevice(new ElgatoConnection(), this, name);
  }

  public String getDriverDesc() {
    return mLocalizer.msg("desc", "Description");
  }

  public String getDriverName() {
    return mLocalizer.msg("name", "Elgato EyeTV");
  }

}
