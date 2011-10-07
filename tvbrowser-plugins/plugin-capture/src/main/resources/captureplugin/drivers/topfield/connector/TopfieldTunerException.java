/**
 * Created on 27.06.2010
 */
package captureplugin.drivers.topfield.connector;

/**
 * Error selecting a tuner.
 * 
 * @author Wolfgang
 */
public class TopfieldTunerException extends Exception {

  /**
   * Default constructor.
   */
  public TopfieldTunerException() {
  }

  /**
   * @param message The error message
   */
  public TopfieldTunerException(String message) {
    super(message);
  }

  /**
   * @param cause The cause of the error
   */
  public TopfieldTunerException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message The error message
   * @param cause The cause of the error
   */
  public TopfieldTunerException(String message, Throwable cause) {
    super(message, cause);
  }

}
