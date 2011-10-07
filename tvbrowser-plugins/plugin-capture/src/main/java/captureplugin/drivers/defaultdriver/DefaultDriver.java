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
 *     $Date: 2009-07-18 18:51:13 +0200 (Sa, 18 Jul 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5821 $
 */
package captureplugin.drivers.defaultdriver;

import util.ui.Localizer;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;


/**
 * The Default-Driver
 */
public class DefaultDriver implements DriverIf {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DefaultDriver.class);
 
    public String getDriverName() {
        return mLocalizer.msg("Name", "Default Driver");
    }

    public String getDriverDesc() {
        return mLocalizer.msg("Description", "The Default Driver. A commandline/web-interface");
    }
    
    /**
     * Returns the Driver-Name
     * @return Name of Driver
     */
    public String toString() {
        return getDriverName();
    }

    public DeviceIf createDevice(String name) {
        return new DefaultDevice(this, name);
    }

}