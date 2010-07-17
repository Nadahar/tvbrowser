/**
 * Created on 20.06.2010
 */
package captureplugin.drivers.topfield.connector;

/**
 * Exception during communication with the device.
 * 
 * @author Wolfgang Reh
 */
public class TopfieldConnectionException extends Exception {

  /**
   * Default constructor.
   */
  public TopfieldConnectionException() {
  }

  /**
   * @param message The error message
   */
  public TopfieldConnectionException(String message) {
    super(message);
  }

  /**
   * @param cause The error cause
   */
  public TopfieldConnectionException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message The error message
   * @param cause The error cause
   */
  public TopfieldConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
