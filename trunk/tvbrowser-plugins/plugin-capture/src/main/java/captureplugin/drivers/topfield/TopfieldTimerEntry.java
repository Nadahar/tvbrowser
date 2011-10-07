/**
 * Created on 27.06.2010
 */
package captureplugin.drivers.topfield;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import captureplugin.drivers.utils.ProgramTime;

/**
 * Data for an entry on the device and the matching TVBrowser program.
 * 
 * @author Wolfgang Reh
 */
public class TopfieldTimerEntry {
  private String deviceFilename;
  private int entryNumber;
  private TopfieldTimerMode entryRepetition;
  private int entryTuner;
  private ProgramTime program;

  /**
   * Create a timer entry.
   * 
   * @param filename The file name on the device
   * @param entryNr The entry number on the device
   * @param repeat The repetition mode of the entry
   * @param tuner The tuner this recoding is assigned to
   * @param prg The program (in TVBrowser)
   */
  public TopfieldTimerEntry(String filename, int entryNr, TopfieldTimerMode repeat, int tuner, ProgramTime prg) {
    deviceFilename = filename;
    entryNumber = entryNr;
    entryRepetition = repeat;
    entryTuner = tuner;
    program = prg;
  }

  /**
   * Create a timer entry from a stream.
   * 
   * @param stream The stream to read from
   * @throws IOException If reading from the stream failed
   * @throws ClassNotFoundException If a saved class could not be found
   */
  public TopfieldTimerEntry(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    readFromStream(stream);
  }

  /**
   * Write the timer entry to a stream.
   * 
   * @param stream The stream to write to
   * @throws IOException If writing to the stream failed
   */
  public void writeToStream(ObjectOutputStream stream) throws IOException {
    stream.writeUTF(deviceFilename);
    stream.writeInt(entryNumber);
    stream.writeInt(entryRepetition.toNumber());
    stream.writeInt(entryTuner);
    program.writeData(stream);
  }

  /**
   * Read the timer entry from a stream.
   * 
   * @param stream The stream to read from
   * @throws IOException If reading from the stream failed
   * @throws ClassNotFoundException If a saved class could not be found
   */
  private void readFromStream(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    deviceFilename = stream.readUTF();
    entryNumber = stream.readInt();
    entryRepetition = TopfieldTimerMode.createFromNumber(stream.readInt());
    entryTuner = stream.readInt();
    program = new ProgramTime();
    program.readData(stream);
  }

  /**
   * @return the deviceFilename
   */
  public String getDeviceFilename() {
    return deviceFilename;
  }

  /**
   * @return the entryNumber
   */
  public int getEntryNumber() {
    return (entryNumber);
  }

  /**
   * @return the entryRepetition
   */
  public TopfieldTimerMode getEntryRepetition() {
    return (entryRepetition);
  }

  /**
   * @return the entryTuner
   */
  public int getEntryTuner() {
    return (entryTuner);
  }

  /**
   * @return the program
   */
  public ProgramTime getProgram() {
    return (program);
  }
}
