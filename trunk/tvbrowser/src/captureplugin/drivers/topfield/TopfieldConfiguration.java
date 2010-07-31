/**
 * Created on 20.06.2010
 */
package captureplugin.drivers.topfield;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import util.io.IOUtilities;
import captureplugin.drivers.utils.IDGenerator;
import captureplugin.utils.ConfigIf;
import captureplugin.utils.ExternalChannelIf;
import devplugin.Channel;
import devplugin.ProgramReceiveTarget;

/**
 * Container holding the configuration of Topfield recording device.
 * 
 * @author Wolfgang Reh
 */
public final class TopfieldConfiguration implements ConfigIf, Cloneable {
  private static final int CONFIGURATION_VERSION = 1;
  private static final String USER_NAME_PROPERTY = "user.name";

  private String configurationID;
  private long saveTimestamp;
  private String deviceAddress = "";
  private String username = "";
  private String password = "";
  private int defaultPreroll;
  private int defaultPostroll;
  private boolean useTuner4 = true;
  private boolean correctTime = true;
  private boolean recordingsLocal = true;
  private transient boolean deviceUnreachable = false;
  private int connectionTimeout = 3000;
  private HashMap<String, TopfieldServiceInfo> deviceChannels = new HashMap<String, TopfieldServiceInfo>();
  private HashMap<TopfieldServiceInfo, Channel> deviceChannelMap = new HashMap<TopfieldServiceInfo, Channel>();
  private HashMap<Channel, TopfieldServiceInfo> browserChannelMap = new HashMap<Channel, TopfieldServiceInfo>();
  private ProgramReceiveTarget[] receiveTargets = new ProgramReceiveTarget[0];
  private ArrayList<TopfieldTimerEntry> timerEntries = new ArrayList<TopfieldTimerEntry>();

  /**
   * Create an empty configuration.
   */
  public TopfieldConfiguration() {
    username = System.getProperty(USER_NAME_PROPERTY);
  }

  /**
   * Clone the configuration.
   * 
   * @param configuration
   *          The configuration to clone
   */
  public TopfieldConfiguration(TopfieldConfiguration configuration) {
    configurationID = configuration.configurationID;
    deviceAddress = configuration.deviceAddress;
    username = configuration.username;
    password = configuration.password;
    defaultPreroll = configuration.defaultPreroll;
    defaultPostroll = configuration.defaultPostroll;
    useTuner4 = configuration.useTuner4;
    correctTime = configuration.correctTime;
    recordingsLocal = configuration.recordingsLocal;
    deviceUnreachable = configuration.deviceUnreachable;
    connectionTimeout = configuration.connectionTimeout;
    deviceChannels = new HashMap<String, TopfieldServiceInfo>(configuration.deviceChannels);
    deviceChannelMap = new HashMap<TopfieldServiceInfo, Channel>(configuration.deviceChannelMap);
    browserChannelMap = new HashMap<Channel, TopfieldServiceInfo>(configuration.browserChannelMap);
    receiveTargets = Arrays.copyOf(configuration.receiveTargets, configuration.receiveTargets.length);
    timerEntries = new ArrayList<TopfieldTimerEntry>(configuration.timerEntries);
  }

  /**
   * Create a configuration object from an object stream.
   * 
   * @param stream
   *          The object stream
   * @throws IOException
   *           If the stream could not be read
   * @throws ClassNotFoundException
   *           If a class in the stream could not be instantiated
   */
  public TopfieldConfiguration(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    readFromStream(stream);
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.utils.ConfigIf#getExternalChannel(devplugin.Channel)
   */
  @Override
  public ExternalChannelIf getExternalChannel(Channel subscribedChannel) {
    return browserChannelMap.get(subscribedChannel);
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.utils.ConfigIf#getExternalChannels()
   */
  @Override
  public ExternalChannelIf[] getExternalChannels() {
    ExternalChannelIf[] channelArray = deviceChannels.values().toArray(new ExternalChannelIf[1]);
    Arrays.sort(channelArray, new Comparator<ExternalChannelIf>() {
      @Override
      public int compare(ExternalChannelIf channel1, ExternalChannelIf channel2) {
        return channel1.getName().compareToIgnoreCase(channel2.getName());
      }
    });
    return channelArray;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * captureplugin.utils.ConfigIf#setExternalChannel(devplugin.Channel,captureplugin
   * .utils.ExternalChannelIf)
   */
  @Override
  public void setExternalChannel(Channel subscribedChannel, ExternalChannelIf externalChannel) {
    deviceChannelMap.put((TopfieldServiceInfo) externalChannel, subscribedChannel);
    browserChannelMap.put(subscribedChannel, (TopfieldServiceInfo) externalChannel);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public TopfieldConfiguration clone() {
    return new TopfieldConfiguration(this);
  }

  /**
   * Read the configuration from a stream.
   * 
   * @param stream
   *          The stream to read
   * @throws IOException
   *           If reading the stream fails
   * @throws ClassNotFoundException
   *           If a saved class could not be instantiated
   */
  private void readFromStream(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    deviceChannels.clear();
    deviceChannelMap.clear();
    browserChannelMap.clear();
    timerEntries.clear();

    @SuppressWarnings("unused")
    int version = stream.readInt();
    configurationID = stream.readUTF();

    saveTimestamp = stream.readLong();
    deviceAddress = stream.readUTF();
    username = stream.readUTF();
    password = IOUtilities.xorDecode(stream.readUTF(), saveTimestamp);
    defaultPreroll = stream.readInt();
    defaultPostroll = stream.readInt();
    useTuner4 = stream.readBoolean();
    correctTime = stream.readBoolean();
    recordingsLocal = stream.readBoolean();
    connectionTimeout = stream.readInt();

    for (int channelCount = stream.readInt(); channelCount > 0; channelCount--) {
      TopfieldServiceInfo service = TopfieldServiceInfo.createFromStream(stream);
      deviceChannels.put(service.getKey(), service);
    }

    for (int assigmentCount = stream.readInt(); assigmentCount > 0; assigmentCount--) {
      Channel browserChannel = Channel.readData(stream, true);
      TopfieldServiceInfo service = deviceChannels.get(stream.readUTF());
      deviceChannelMap.put(service, browserChannel);
      browserChannelMap.put(browserChannel, service);
    }

    receiveTargets = new ProgramReceiveTarget[stream.readInt()];
    for (int targetIndex = 0; targetIndex < receiveTargets.length; targetIndex++) {
      receiveTargets[targetIndex] = new ProgramReceiveTarget(stream);
    }

    for (int timers = stream.readInt(); timers > 0; timers--) {
      timerEntries.add(new TopfieldTimerEntry(stream));
    }
  }

  /**
   * Write the configuration to a stream.
   * 
   * @param stream
   *          The stream to write to
   * @throws IOException
   *           If writing to the stream fails
   */
  public void writeToStream(ObjectOutputStream stream) throws IOException {
    saveTimestamp = System.currentTimeMillis();

    stream.writeInt(CONFIGURATION_VERSION);
    stream.writeUTF(getConfigurationID());

    stream.writeLong(saveTimestamp);
    stream.writeUTF(deviceAddress);
    stream.writeUTF(username);
    stream.writeUTF(IOUtilities.xorEncode(password, saveTimestamp));
    stream.writeInt(defaultPreroll);
    stream.writeInt(defaultPostroll);
    stream.writeBoolean(useTuner4);
    stream.writeBoolean(correctTime);
    stream.writeBoolean(recordingsLocal);
    stream.writeInt(connectionTimeout);

    stream.writeInt(deviceChannels.values().size());
    for (TopfieldServiceInfo service : deviceChannels.values()) {
      service.writeToStream(stream);
    }

    int assignmentCount = 0;
    for (Channel browserChannel : browserChannelMap.keySet()) {
      if (browserChannelMap.get(browserChannel) != null) {
        assignmentCount++;
      }
    }
    stream.writeInt(assignmentCount);
    for (Channel browserChannel : browserChannelMap.keySet()) {
      if ((browserChannel != null) && (browserChannelMap.get(browserChannel) != null)) {
        browserChannel.writeData(stream);
        stream.writeUTF(browserChannelMap.get(browserChannel).getKey());
      }
    }

    stream.writeInt(receiveTargets.length);
    for (ProgramReceiveTarget target : receiveTargets) {
      target.writeData(stream);
    }

    stream.writeInt(timerEntries.size());
    for (TopfieldTimerEntry entry : timerEntries) {
      entry.writeToStream(stream);
    }
  }

  /**
   * Get the channel specific preroll time.
   * 
   * @param subscribedChannel
   *          The channel for which to get the preroll time
   * @return The preroll time
   */
  public Integer getChannelPreroll(Channel subscribedChannel) {
    if (browserChannelMap.get(subscribedChannel) != null) {
      Integer preroll = browserChannelMap.get(subscribedChannel).getPreroll();
      return (preroll == null) ? defaultPreroll : preroll;
    } else {
      return null;
    }
  }

  /**
   * Get the channel specific postroll time.
   * 
   * @param subscribedChannel
   *          The channel for which to get the postroll time
   * @return The postroll time
   */
  public Integer getChannelPostroll(Channel subscribedChannel) {
    if (browserChannelMap.get(subscribedChannel) != null) {
      Integer postroll = browserChannelMap.get(subscribedChannel).getPostroll();
      return (postroll == null) ? defaultPostroll : postroll;
    } else {
      return null;
    }
  }

  /**
   * Set the channels currently tuned on the device.
   * 
   * @param services
   *          The list of services
   */
  protected void setDeviceChannels(List<TopfieldServiceInfo> services) {
    deviceChannels.clear();
    for (TopfieldServiceInfo service : services) {
      deviceChannels.put(service.getKey(), service);
    }
  }

  /**
   * @return the configurationID
   */
  public String getConfigurationID() {
    if (configurationID == null) {
      configurationID = IDGenerator.generateUniqueId();
    }
    return configurationID;
  }

  /**
   * @param deviceAddress
   *          the deviceAddress to set
   */
  public void setDeviceAddress(String deviceAddress) {
    this.deviceAddress = deviceAddress;
  }

  /**
   * @return the deviceAddress
   */
  public String getDeviceAddress() {
    return deviceAddress;
  }

  /**
   * @param username
   *          the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param password
   *          the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @return The password as character array
   */
  public char[] getPasswordChars() {
    return password.toCharArray();
  }

  /**
   * @return the defaultPreroll
   */
  public int getDefaultPreroll() {
    return defaultPreroll;
  }

  /**
   * @param defaultPreroll
   *          the defaultPreroll to set
   */
  public void setDefaultPreroll(int defaultPreroll) {
    this.defaultPreroll = defaultPreroll;
  }

  /**
   * @return the defaultPostroll
   */
  public int getDefaultPostroll() {
    return defaultPostroll;
  }

  /**
   * @param defaultPostroll
   *          the defaultPostroll to set
   */
  public void setDefaultPostroll(int defaultPostroll) {
    this.defaultPostroll = defaultPostroll;
  }

  /**
   * @param useTuner4
   *          the useTuner4 to set
   */
  public void setUseTuner4(boolean useTuner4) {
    this.useTuner4 = useTuner4;
  }

  /**
   * @return the useTuner4
   */
  public boolean isUseTuner4() {
    return useTuner4;
  }

  /**
   * @return the correctTime
   */
  public boolean isCorrectTime() {
    return (correctTime);
  }

  /**
   * @param correctTime
   *          the correctTime to set
   */
  public void setCorrectTime(boolean correctTime) {
    this.correctTime = correctTime;
  }

  /**
   * @param receiveTargets
   *          the receiveTargets to set
   */
  public void setReceiveTargets(ProgramReceiveTarget[] receiveTargets) {
    this.receiveTargets = receiveTargets;
  }

  /**
   * @return the receiveTargets
   */
  public ProgramReceiveTarget[] getReceiveTargets() {
    return receiveTargets;
  }

  /**
   * @param recordingsLocal
   *          the recordingsLocal to set
   */
  public void setRecordingsLocal(boolean recordingsLocal) {
    this.recordingsLocal = recordingsLocal;
  }

  /**
   * The user setting is overruled if the device is currently unreachable.
   * 
   * @return the recordingsLocal
   */
  public boolean isRecordingsLocal() {
    return !deviceUnreachable ? recordingsLocal : true;
  }

  /**
   * Don't take into account if the deice is reachable.
   * 
   * @return the recordingsLocal
   */
  protected boolean isRecordingsLocalUnchecked() {
    return recordingsLocal;
  }

  /**
   * @param deviceUnreachable
   *          the deviceUnreachable to set
   */
  public void setDeviceUnreachable(boolean deviceUnreachable) {
    this.deviceUnreachable = deviceUnreachable;
  }

  /**
   * @param connectionTimeout
   *          the connectionTimeout to set
   */
  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  /**
   * @return the connectionTimeout
   */
  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * Get a TVBrowser channel for device service parameters.
   * 
   * @param number
   *          The device service number
   * @param isTV
   *          Is this a TV service?
   * @return The TVBrowser channel
   */
  public Channel getChannelForService(int number, boolean isTV) {
    for (TopfieldServiceInfo service : deviceChannelMap.keySet()) {
      if ((service.isTV() == isTV) && (service.getChannelNumber() == number)) {
        return deviceChannelMap.get(service);
      }
    }

    return null;
  }

  /**
   * Add a timer entry to the list.
   * 
   * @param entry
   *          The entry to add
   */
  public void addTimerEntry(TopfieldTimerEntry entry) {
    timerEntries.add(entry);
  }

  /**
   * Remove an entry from the timer list.
   * 
   * @param entry
   *          The entry to remove
   */
  public void removeTimerEntry(TopfieldTimerEntry entry) {
    timerEntries.remove(entry);
  }

  /**
   * @return The list of timer entries
   */
  public List<TopfieldTimerEntry> getTimerEntries() {
    return new ArrayList<TopfieldTimerEntry>(timerEntries);
  }

  /**
   * @param entries
   *          The list of timers to set
   */
  public void setTimerEntries(List<TopfieldTimerEntry> entries) {
    timerEntries = new ArrayList<TopfieldTimerEntry>(entries);
  }
}
