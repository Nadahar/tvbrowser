/**
 * Created on 27.06.2010
 */
package captureplugin.drivers.topfield.connector;

/**
 * Services changed on the device.
 * 
 * @author Wolfgang
 */
public class TopfieldServiceException extends Exception {

  /**
   * Default constructor.
   */
  public TopfieldServiceException() {
  }

  /**
   * @param message The error message
   */
  public TopfieldServiceException(String message) {
    super(message);
  }

  /**
   * @param cause The cause of the error
   */
  public TopfieldServiceException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message The error message
   * @param cause The cause of the error
   */
  public TopfieldServiceException(String message, Throwable cause) {
    super(message, cause);
  }

}
