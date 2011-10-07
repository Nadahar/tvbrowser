/**
 * Created on 20.06.2010
 */
package captureplugin.drivers.topfield;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import captureplugin.utils.ExternalChannelIf;

/**
 * Container holding information about a service on the device.
 * 
 * @author Wolfgang Reh
 */
public class TopfieldServiceInfo implements ExternalChannelIf {
  private static final String DESCRIPTION_FORMAT = "%s (%d) %s %s";
  private static final String TV_SERVICE = "TV";
  private static final String RADIO_SERVICE = "Radio";
  private static final String TV_PREFIX = "T";
  private static final String RADIO_PREFIX = "R";
  private static final String KEY_FORMAT = "%s%04d";

  private final int channelNumber;
  private final String satelliteName;
  private final int satelliteNumber;
  private final String serviceName;
  private final int tuner;
  private final boolean isTV;
  private Integer preroll;
  private Integer postroll;

  /**
   * Create a service info.
   * 
   * @param channel The channel used on the device
   * @param satellite The satellite name the service is broadcasted on
   * @param satIndex The index of the satellite
   * @param service The service name
   * @param tuner The tuner this service is tuned on
   * @param tv Is this a TV service?
   */
  public TopfieldServiceInfo(int channel, String satellite, int satIndex, String service, int tuner, boolean tv) {
    channelNumber = channel;
    satelliteName = satellite;
    satelliteNumber = satIndex;
    serviceName = service;
    this.tuner = tuner;
    isTV = tv;
  }

  /**
   * Write the data to a stream.
   * 
   * @param stream The stream to write to
   * @throws IOException If the stream could not be written
   */
  public void writeToStream(ObjectOutputStream stream) throws IOException {
    stream.writeInt(channelNumber);
    stream.writeUTF(satelliteName);
    stream.writeInt(satelliteNumber);
    stream.writeUTF(serviceName);
    stream.writeInt(tuner);
    stream.writeBoolean(isTV);
    stream.writeInt((preroll == null) ? -1 : preroll);
    stream.writeInt((postroll == null) ? -1 : postroll);
  }

  /**
   * Create a service info object from a stream.
   * 
   * @param stream The stream to read from
   * @return The <code>TopfieldServiceInfo</code> object
   * @throws IOException If the stream could not be read
   */
  public static TopfieldServiceInfo createFromStream(ObjectInputStream stream) throws IOException {
    int chNr = stream.readInt();
    String satNm = stream.readUTF();
    int satNr = stream.readInt();
    String svNm = stream.readUTF();
    int tu = stream.readInt();
    boolean tv = stream.readBoolean();
    int pre = stream.readInt();
    int post = stream.readInt();
    TopfieldServiceInfo serviceInfo = new TopfieldServiceInfo(chNr, satNm, satNr, svNm, tu, tv);
    serviceInfo.setPreroll((pre < 0) ? null : pre);
    serviceInfo.setPostroll((post < 0) ? null : post);
    return serviceInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return String.format(DESCRIPTION_FORMAT, serviceName, channelNumber + 1, satelliteName, isTV() ? TV_SERVICE
        : RADIO_SERVICE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (satelliteName + serviceName).hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    } else if (other instanceof TopfieldServiceInfo) {
      TopfieldServiceInfo otherService = (TopfieldServiceInfo) other;
      return (channelNumber == otherService.channelNumber) && (satelliteNumber == otherService.satelliteNumber) &&
          satelliteName.equals(otherService.satelliteName) && serviceName.equals(otherService.serviceName) &&
          (tuner == otherService.tuner) && (isTV == otherService.isTV);
    }

    return false;
  }

  /**
   * Get the unique key for this service.
   * 
   * @return The key
   */
  public String getKey() {
    return String.format(KEY_FORMAT, (isTV ? TV_PREFIX : RADIO_PREFIX), channelNumber);
  }

  /**
   * @return the channelNumber
   */
  public int getChannelNumber() {
    return channelNumber;
  }

  /**
   * @return the satelliteName
   */
  public String getSatelliteName() {
    return satelliteName;
  }

  /**
   * @return the satelliteNumber
   */
  public int getSatelliteNumber() {
    return satelliteNumber;
  }

  /**
   * @return the serviceName
   */
  public String getName() {
    return serviceName;
  }

  /**
   * @return the tuner
   */
  public int getTuner() {
    return (tuner);
  }

  /**
   * @return the isTV
   */
  public boolean isTV() {
    return isTV;
  }

  /**
   * @param preroll the preroll to set
   */
  public void setPreroll(Integer preroll) {
    this.preroll = preroll;
  }

  /**
   * @return the preroll
   */
  public Integer getPreroll() {
    return (preroll);
  }

  /**
   * @param postroll the postroll to set
   */
  public void setPostroll(Integer postroll) {
    this.postroll = postroll;
  }

  /**
   * @return the postroll
   */
  public Integer getPostroll() {
    return (postroll);
  }
}
