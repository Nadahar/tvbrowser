/*
 * Created on 20.08.2004
 */
package captureplugin.drivers.pinnacle;

import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;


/**
 * @author bodum
 */
public class PinnacleDriver implements DriverIf {

    /* (non-Javadoc)
     * @see captureplugin.drivers.DriverIf#getDriverName()
     */
    public String getDriverName() {
        return "Pinnacle Old Interface";
    }

    /* (non-Javadoc)
     * @see captureplugin.drivers.DriverIf#getDriverDesc()
     */
    public String getDriverDesc() {
        return "Pinnacle Interface";
    }

    public String toString() {
        return getDriverName();
    }

    /* (non-Javadoc)
     * @see captureplugin.drivers.DriverIf#createDevice(java.lang.String)
     */
    public DeviceIf createDevice(String name) {
        // TODO Auto-generated method stub
        return null;
    }

}