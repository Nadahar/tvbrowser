/**
 * Created on 19.06.2010
 */
package captureplugin.drivers.topfield;

import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;
import captureplugin.drivers.topfield.connector.TopfieldConnectionException;
import captureplugin.drivers.topfield.connector.TopfieldConnector;
import captureplugin.drivers.topfield.connector.TopfieldServiceException;
import captureplugin.drivers.topfield.connector.TopfieldTunerException;
import captureplugin.drivers.utils.ProgramTime;
import captureplugin.drivers.utils.ProgramTimeDialog;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;

/**
 * A recoding device for a Topfield SRP-2410.
 * 
 * @author Wolfgang Reh
 */
public final class TopfieldDevice implements DeviceIf {
  private static final String EXPIRED_TITLE = "expiredTitle";
  private static final String DEFAULT_EXPIRED_TITLE = "Recording in the past";
  private static final String EXPIRED_TEXT = "expiredText";
  private static final String DEFAULT_EXPIRED_TEXT = "Cannot record a program broadcasted in the past!";
  private static final String NOT_CONFIGURED_TITLE = "notConfiguredTitle";
  private static final String DEFAULT_NOT_CONFIGURED_TITLE = "Channel not configured";
  private static final String NOT_CONFIGURED_TEXT = "notConfiguredText";
  private static final String DEFAULT_NOT_CONFIGURED_TEXT = "Channel not configured.\nDo you want to configure the channel now?";
  private static final String DEVICE_UNREACHABLE_TITLE = "unreachableTitle";
  private static final String DEFAULT_DEVICE_UNREACHABLE_TITLE = "Error contacting device";
  private static final String DEVICE_UNREACHABLE_TEXT = "notReachable";
  private static final String DEFAULT_DEVICE_UNREACHABLE_TEXT = "The device %s could not be contacted.\nMake sure it's switched on.";
  private static final String SERVICE_CHANGED_TITLE = "serviceChangedTitle";
  private static final String DEFAULT_SERVICE_CHANGED_TITLE = "Service changed";
  private static final String SERVICE_CHANGED_TEXT = "serviceChangedText";
  private static final String DEFAULT_SERVICE_CHANGED_TEXT = "The service on the device differs from the service in the plugin.\nPlease reconfigure the plugin.";
  private static final String NO_TUNER_TITLE = "noTunerTitle";
  private static final String DEFAULT_NO_TUNER_TITLE = "Tuner assignment";
  private static final String NO_TUNER_TEXT = "noTunerText";
  private static final String DEFAULT_NO_TUNER_TEXT = "No tuner available for the timer";
  private static final String SWITCH_PROGRAM = "switchProgram";
  private static final String DEFAULT_SWITCH_PROGRAM = "Switch to";
  private static final String EXPIRED_P_TITLE = "expiredPTitle";
  private static final String DEFAULT_EXPIRED_P_TITLE = "Switch in the past";
  private static final String EXPIRED_P_TEXT = "expiredPText";
  private static final String DEFAULT_EXPIRED_P_TEXT = "Cannot switch to a program broadcasted in the past!";
  private static final String REPEAT_LABEL = "recordRepeat";
  private static final String DEFAULT_REPEAT_LABEL = "Repeat:";

  private static final Localizer localizer = Localizer.getLocalizerFor(TopfieldDevice.class);

  private final TopfieldDriver driver;
  private String name;
  private TopfieldConfiguration configuration;

  /**
   * Create a new device.
   * 
   * @param driver Driver for the device
   * @param name Name of the device
   */
  public TopfieldDevice(TopfieldDriver driver, String name) {
    this.driver = driver;
    this.name = name;
    configuration = new TopfieldConfiguration();
  }

  /**
   * Clone this device.
   * 
   * @param device The device to clone
   */
  public TopfieldDevice(TopfieldDevice device) {
    this.driver = device.driver;
    this.name = device.name;
    this.configuration = device.configuration.clone();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() {
    return new TopfieldDevice(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#add(java.awt.Window, devplugin.Program)
   */
  @Override
  public boolean add(Window parent, Program program) {
    if (program.isExpired()) {
      JOptionPane.showMessageDialog(parent, localizer.msg(EXPIRED_TEXT, DEFAULT_EXPIRED_TEXT), localizer.msg(
          EXPIRED_TITLE, DEFAULT_EXPIRED_TITLE), JOptionPane.INFORMATION_MESSAGE);
      return false;
    }

    TopfieldServiceInfo service = (TopfieldServiceInfo) configuration.getExternalChannel(program.getChannel());
    if (service == null) {
      if (JOptionPane.showConfirmDialog(parent, localizer.msg(NOT_CONFIGURED_TEXT, DEFAULT_NOT_CONFIGURED_TEXT),
          localizer.msg(NOT_CONFIGURED_TITLE, DEFAULT_NOT_CONFIGURED_TITLE), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        configDevice(parent);
      }
    } else {
      ProgramTime time = new ProgramTime(program);

      Calendar start = time.getStartAsCalendar();
      start.add(Calendar.MINUTE, configuration.getChannelPreroll(program.getChannel()) * -1);
      time.setStart(start.getTime());

      Calendar end = time.getEndAsCalendar();
      end.add(Calendar.MINUTE, configuration.getChannelPostroll(program.getChannel()));
      time.setEnd(end.getTime());

      JComboBox repeatSelector = new JComboBox();
      for (TopfieldTimerMode mode : TopfieldTimerMode.values()) {
        repeatSelector.addItem(mode);
      }
      ProgramTimeDialog recordDialog = new ProgramTimeDialog(parent, time, true, localizer.msg(REPEAT_LABEL,
          DEFAULT_REPEAT_LABEL), repeatSelector);
      UiUtilities.centerAndShow(recordDialog);
      ProgramTime programToRecord = recordDialog.getPrgTime();
      if (programToRecord != null) {
        TopfieldConnector connector = new TopfieldConnector(configuration);
        boolean recordingAdded = false;
        try {
          recordingAdded = connector.addRecording(parent, service, programToRecord, (TopfieldTimerMode) repeatSelector
              .getSelectedItem());
        } catch (TopfieldConnectionException e) {
          String message = String.format(localizer.msg(DEVICE_UNREACHABLE_TEXT, DEFAULT_DEVICE_UNREACHABLE_TEXT), name);
          JOptionPane.showMessageDialog(parent, message, localizer.msg(DEVICE_UNREACHABLE_TITLE,
              DEFAULT_DEVICE_UNREACHABLE_TITLE), JOptionPane.ERROR_MESSAGE);
        } catch (TopfieldServiceException e) {
          JOptionPane.showMessageDialog(parent, localizer.msg(SERVICE_CHANGED_TEXT, DEFAULT_SERVICE_CHANGED_TEXT),
              localizer.msg(SERVICE_CHANGED_TITLE, DEFAULT_SERVICE_CHANGED_TITLE), JOptionPane.ERROR_MESSAGE);
        } catch (TopfieldTunerException e) {
          JOptionPane.showMessageDialog(parent, localizer.msg(NO_TUNER_TEXT, DEFAULT_NO_TUNER_TEXT), localizer.msg(
              NO_TUNER_TITLE, DEFAULT_NO_TUNER_TITLE), JOptionPane.ERROR_MESSAGE);
        }
        return recordingAdded;
      }
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * captureplugin.drivers.DeviceIf#checkProgramsAfterDataUpdateAndGetDeleted()
   */
  @Override
  public Program[] checkProgramsAfterDataUpdateAndGetDeleted() {
    if (!configuration.isRecordingsLocal()) {
      TopfieldConnector connector = new TopfieldConnector(configuration);
      try {
        connector.getTimerList();
      } catch (TopfieldConnectionException e) {
        String message = String.format(localizer.msg(DEVICE_UNREACHABLE_TEXT, DEFAULT_DEVICE_UNREACHABLE_TEXT), name);
        JOptionPane.showMessageDialog(null, message, localizer.msg(DEVICE_UNREACHABLE_TITLE,
            DEFAULT_DEVICE_UNREACHABLE_TITLE), JOptionPane.ERROR_MESSAGE);
      }
    }

    ArrayList<Program> deleted = new ArrayList<Program>();
    for (TopfieldTimerEntry entry : configuration.getTimerEntries()) {
      if (entry.getProgram().checkIfRemovedOrUpdateInstead()) {
        deleted.add(entry.getProgram().getProgram());
      }
    }

    return deleted.toArray(new Program[deleted.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#configDevice(java.awt.Window)
   */
  @Override
  public void configDevice(Window parent) {
    TopfieldConfigurationDialog configurationDialog = new TopfieldConfigurationDialog(parent, this, configuration);

    UiUtilities.centerAndShow(configurationDialog);

    if (configurationDialog.configurationOK()) {
      name = configurationDialog.getDeviceName();
      configuration = configurationDialog.getConfiguration();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * captureplugin.drivers.DeviceIf#executeAdditionalCommand(java.awt.Window,
   * int, devplugin.Program)
   */
  @Override
  public boolean executeAdditionalCommand(Window parent, int num, Program program) {
    switch (num) {
    case 0:
      if (program.isExpired()) {
        JOptionPane.showMessageDialog(parent, localizer.msg(EXPIRED_P_TEXT, DEFAULT_EXPIRED_P_TEXT), localizer.msg(
            EXPIRED_P_TITLE, DEFAULT_EXPIRED_P_TITLE), JOptionPane.INFORMATION_MESSAGE);
        return false;
      }

      TopfieldServiceInfo service = (TopfieldServiceInfo) configuration.getExternalChannel(program.getChannel());
      if (service == null) {
        if (JOptionPane.showConfirmDialog(parent, localizer.msg(NOT_CONFIGURED_TEXT, DEFAULT_NOT_CONFIGURED_TEXT),
            localizer.msg(NOT_CONFIGURED_TITLE, DEFAULT_NOT_CONFIGURED_TITLE), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
          configDevice(parent);
        }
      } else {
        ProgramTime time = new ProgramTime(program);

        Calendar start = time.getStartAsCalendar();
        start.add(Calendar.MINUTE, configuration.getChannelPreroll(program.getChannel()) * -1);
        time.setStart(start.getTime());

        Calendar end = time.getEndAsCalendar();
        end.add(Calendar.MINUTE, configuration.getChannelPostroll(program.getChannel()));
        time.setEnd(end.getTime());

        TopfieldConnector connector = new TopfieldConnector(configuration);
        boolean timerAdded = false;
        try {
          timerAdded = connector.addPTimer(parent, service, time);
        } catch (TopfieldConnectionException e) {
          String message = String.format(localizer.msg(DEVICE_UNREACHABLE_TEXT, DEFAULT_DEVICE_UNREACHABLE_TEXT), name);
          JOptionPane.showMessageDialog(parent, message, localizer.msg(DEVICE_UNREACHABLE_TITLE,
              DEFAULT_DEVICE_UNREACHABLE_TITLE), JOptionPane.ERROR_MESSAGE);
        } catch (TopfieldServiceException e) {
          JOptionPane.showMessageDialog(parent, localizer.msg(SERVICE_CHANGED_TEXT, DEFAULT_SERVICE_CHANGED_TEXT),
              localizer.msg(SERVICE_CHANGED_TITLE, DEFAULT_SERVICE_CHANGED_TITLE), JOptionPane.ERROR_MESSAGE);
        } catch (TopfieldTunerException e) {
          JOptionPane.showMessageDialog(parent, localizer.msg(NO_TUNER_TEXT, DEFAULT_NO_TUNER_TEXT), localizer.msg(
              NO_TUNER_TITLE, DEFAULT_NO_TUNER_TITLE), JOptionPane.ERROR_MESSAGE);
        }
        return timerAdded;
      }
      break;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#getAdditionalCommands()
   */
  @Override
  public String[] getAdditionalCommands() {
    return new String[] { localizer.msg(SWITCH_PROGRAM, DEFAULT_SWITCH_PROGRAM) };
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#getDeleteRemovedProgramsAutomatically()
   */
  @Override
  public boolean getDeleteRemovedProgramsAutomatically() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#getDriver()
   */
  @Override
  public DriverIf getDriver() {
    return driver;
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#getId()
   */
  @Override
  public String getId() {
    return configuration.getConfigurationID();
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * captureplugin.drivers.DeviceIf#getProgramForProgramInList(devplugin.Program
   * )
   */
  @Override
  public Program getProgramForProgramInList(Program p) {
    if (!configuration.isRecordingsLocal()) {
      TopfieldConnector connector = new TopfieldConnector(configuration);
      try {
        connector.getTimerList();
      } catch (TopfieldConnectionException e) {
        String message = String.format(localizer.msg(DEVICE_UNREACHABLE_TEXT, DEFAULT_DEVICE_UNREACHABLE_TEXT), name);
        JOptionPane.showMessageDialog(null, message, localizer.msg(DEVICE_UNREACHABLE_TITLE,
            DEFAULT_DEVICE_UNREACHABLE_TITLE), JOptionPane.ERROR_MESSAGE);
      }
    }

    for (TopfieldTimerEntry entry : configuration.getTimerEntries()) {
      for (Program program : entry.getProgram().getAllPrograms()) {
        if (program.equals(p)) {
          return entry.getProgram().getProgram();
        }
      }
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#getProgramList()
   */
  @Override
  public Program[] getProgramList() {
    if (!configuration.isRecordingsLocal()) {
      TopfieldConnector connector = new TopfieldConnector(configuration);
      try {
        connector.getTimerList();
      } catch (TopfieldConnectionException e) {
        String message = String.format(localizer.msg(DEVICE_UNREACHABLE_TEXT, DEFAULT_DEVICE_UNREACHABLE_TEXT), name);
        JOptionPane.showMessageDialog(null, message, localizer.msg(DEVICE_UNREACHABLE_TITLE,
            DEFAULT_DEVICE_UNREACHABLE_TITLE), JOptionPane.ERROR_MESSAGE);
      }
    }
    List<TopfieldTimerEntry> timerEntries = configuration.getTimerEntries();
    Program[] programList = new Program[timerEntries.size()];
    int index = 0;
    for (TopfieldTimerEntry entry : timerEntries) {
      programList[index++] = entry.getProgram().getProgram();
    }
    return programList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#isAbleToAddAndRemovePrograms()
   */
  @Override
  public boolean isAbleToAddAndRemovePrograms() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#isInList(devplugin.Program)
   */
  @Override
  public boolean isInList(Program program) {
    if (!configuration.isRecordingsLocal()) {
      TopfieldConnector connector = new TopfieldConnector(configuration);
      try {
        connector.getTimerList();
      } catch (TopfieldConnectionException e) {
        String message = String.format(localizer.msg(DEVICE_UNREACHABLE_TEXT, DEFAULT_DEVICE_UNREACHABLE_TEXT), name);
        JOptionPane.showMessageDialog(null, message, localizer.msg(DEVICE_UNREACHABLE_TITLE,
            DEFAULT_DEVICE_UNREACHABLE_TITLE), JOptionPane.ERROR_MESSAGE);
      }
    }

    for (TopfieldTimerEntry entry : configuration.getTimerEntries()) {
      if (entry.getProgram().getProgram().equals(program)) {
        return true;
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#readData(java.io.ObjectInputStream,
   * boolean)
   */
  @Override
  public void readData(ObjectInputStream stream, boolean importDevice) throws IOException, ClassNotFoundException {
    configuration = new TopfieldConfiguration(stream);
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#remove(java.awt.Window,
   * devplugin.Program)
   */
  @Override
  public boolean remove(Window parent, Program program) {
    TopfieldConnector connector = new TopfieldConnector(configuration);
    if (!configuration.isRecordingsLocal()) {
      try {
        connector.getTimerList();
      } catch (TopfieldConnectionException e) {
        String message = String.format(localizer.msg(DEVICE_UNREACHABLE_TEXT, DEFAULT_DEVICE_UNREACHABLE_TEXT), name);
        JOptionPane.showMessageDialog(parent, message, localizer.msg(DEVICE_UNREACHABLE_TITLE,
            DEFAULT_DEVICE_UNREACHABLE_TITLE), JOptionPane.ERROR_MESSAGE);
      }
    }

    for (TopfieldTimerEntry entry : configuration.getTimerEntries()) {
      if (entry.getProgram().getProgram().equals(program)) {
        if (!configuration.isRecordingsLocal()) {
          try {
            return connector.deleteRecording(parent, entry);
          } catch (TopfieldConnectionException e) {
            String message = String.format(localizer.msg(DEVICE_UNREACHABLE_TEXT, DEFAULT_DEVICE_UNREACHABLE_TEXT),
                name);
            JOptionPane.showMessageDialog(parent, message, localizer.msg(DEVICE_UNREACHABLE_TITLE,
                DEFAULT_DEVICE_UNREACHABLE_TITLE), JOptionPane.ERROR_MESSAGE);
          }
        } else {
          configuration.removeTimerEntry(entry);
          return true;
        }
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * captureplugin.drivers.DeviceIf#removeProgramWithoutExecution(devplugin.
   * Program)
   */
  @Override
  public void removeProgramWithoutExecution(Program p) {
    remove(null, p);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * captureplugin.drivers.DeviceIf#sendProgramsToReceiveTargets(devplugin.Program
   * [])
   */
  @Override
  public void sendProgramsToReceiveTargets(Program[] progs) {
    for (ProgramReceiveTarget target : configuration.getReceiveTargets()) {
      target.receivePrograms(progs);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#setName(java.lang.String)
   */
  @Override
  public String setName(String name) {
    this.name = name;
    return this.name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see captureplugin.drivers.DeviceIf#writeData(java.io.ObjectOutputStream)
   */
  @Override
  public void writeData(ObjectOutputStream stream) throws IOException {
    configuration.writeToStream(stream);
  }
}
