/**
 * Created on 27.06.2010
 */
package captureplugin.drivers.topfield.connector;

/**
 * Type of service.
 * 
 * @author Wolfgang Reh
 */
public enum TopfieldServiceType {
  /**
   * Service is a TV station.
   */
  TV(0, "Tv"),
  /**
   * Service is a radio station.
   */
  RADIO(1, "Radio");

  private int serviceNumber;
  private String serviceName;

  /**
   * Create a <code>TopfieldServiceType</code>.
   * 
   * @param number The service number on the device
   * @param name the name of the service
   */
  private TopfieldServiceType(int number, String name) {
    serviceNumber = number;
    serviceName = name;
  }

  /**
   * Create a <code>TopfieldServiceType</code> from the number on the device.
   * 
   * @param number The number on the device
   * @return The <code>TopfieldServiceType</code>
   */
  public static TopfieldServiceType createFromNumber(int number) {
    switch (number) {
    case 0:
      return TV;
    case 1:
      return RADIO;
    default:
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return serviceName;
  }

  /**
   * @return The service number on the device
   */
  public int toNumber() {
    return serviceNumber;
  }
}
