/**
 * Created on 19.06.2010
 */
package captureplugin.drivers.topfield;

import util.ui.Localizer;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;

/**
 * Driver for Topfield SRP-2410
 * 
 * @author Wolfgang Reh
 */
public class TopfieldDriver implements DriverIf {
  private static final Localizer localizer = Localizer.getLocalizerFor(TopfieldDriver.class);

  private static final String DRIVER_DESCRIPTION = "desc";
  private static final String DEFAULT_DRIVER_DESCRIPTION = "Driver for the Web-Interface of the Topfield SRP-2410.";
  private static final String DRIVER_NAME = "name";
  private static final String DEFAULT_DRIVER_NAME = "Topfield SRP-2410";

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DriverIf#createDevice(java.lang.String)
   */
  @Override
  public DeviceIf createDevice(String name) {
    return new TopfieldDevice(this, name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DriverIf#getDriverDesc()
   */
  @Override
  public String getDriverDesc() {
    return localizer.msg(DRIVER_DESCRIPTION, DEFAULT_DRIVER_DESCRIPTION);
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DriverIf#getDriverName()
   */
  @Override
  public String getDriverName() {
    return localizer.msg(DRIVER_NAME, DEFAULT_DRIVER_NAME);
  }
}
