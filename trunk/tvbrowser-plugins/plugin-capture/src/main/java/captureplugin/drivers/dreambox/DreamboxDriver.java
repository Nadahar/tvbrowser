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

import util.ui.Localizer;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;

/**
 * The Driver for the Dreambox
 *
 * @author bodum
 */
public class DreamboxDriver implements DriverIf {
    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DreamboxDriver.class);

    /**
     * @param name Name for the Device
     * @return a new Dreambox-Device
     */
    public DeviceIf createDevice(String name) {
        return new DreamboxDevice(this, name);
    }

    /**
     * @return Description for this Driver
     */
    public String getDriverDesc() {
        return mLocalizer.msg("desc", "Description");
    }

    /**
     * @return Name for this Driver
     */
    public String getDriverName() {
        return mLocalizer.msg("name", "DreamboxConnector Driver");
    }

}
