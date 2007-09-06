package captureplugin.drivers.simpledevice;

import devplugin.Channel;
import devplugin.Program;

/**
 * Small interface for applescript devices
 */
public interface SimpleConnectionIf {

    /**
     * Get all available Channels of this device
     * @return available channels
     */
    SimpleChannel[] getAvailableChannels();

    /**
     * Get all recordings of this device
     * @param conf configuration
     * @return all recordings
     */
    Program[] getAllRecordings(SimpleConfig conf);

    /**
     * Add a recording
     *
     * @param conf configuration of this device
     * @param prg add this Program
     * @param length lenght to record
     * @return <code>true</code> if successfully
     */
    boolean addToRecording(SimpleConfig conf, Program prg, int length);

    /**
     * Remove a recording
     *
     * @param prg Program to remove
     */
    void removeRecording(Program prg);

    /**
     * Switch to channel
     * @param conf Configuration of this device
     * @param channel Channel
     */
    void switchToChannel(SimpleConfig conf, Channel channel);
}
