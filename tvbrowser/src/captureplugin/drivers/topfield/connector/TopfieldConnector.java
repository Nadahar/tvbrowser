/**
 * Created on 20.06.2010
 */
package captureplugin.drivers.topfield.connector;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import captureplugin.drivers.topfield.TopfieldConfiguration;
import captureplugin.drivers.topfield.TopfieldServiceInfo;
import captureplugin.drivers.topfield.TopfieldTimerEntry;
import captureplugin.drivers.topfield.TopfieldTimerMode;
import captureplugin.drivers.utils.ProgramTime;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * Connector to the device.
 * 
 * @author Wolfgang Reh
 */
public class TopfieldConnector {
  /**
   * Authenticator for HTTP access authentication.
   * 
   * @author Wolfgang Reh
   */
  private static final class AccessAuthenticator extends Authenticator {
    private final String username;
    private final char[] password;

    public AccessAuthenticator(String username, char[] password) {
      this.username = username;
      this.password = password;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(username, password);
    }
  }

  /**
   * Container for information about one satellite.
   * 
   * @author Wolfgang Reh
   */
  private static final class SatelliteInfo {
    private final int index;
    private final int serviceCount;
    private final String name;

    /**
     * @param index
     *          The satellite index
     * @param count
     *          The number of services on this satellite
     * @param name
     *          The name of the satellite
     */
    public SatelliteInfo(int index, int count, String name) {
      this.index = index;
      this.serviceCount = count;
      this.name = name;
    }

    /**
     * @return the index
     */
    public int getIndex() {
      return (index);
    }

    /**
     * @return the serviceCount
     */
    public int getServiceCount() {
      return (serviceCount);
    }

    /**
     * @return the name
     */
    public String getName() {
      return (name);
    }
  }

  private static final String NEW_TIMER_FORM = "/TimerNewEntryForm.htm";
  private static final String INSERT_TIMER_PAGE = "/InsertTimerSchedule.cgi";
  private static final String DELETE_TIMER_PAGE = "/DeleteTimerSchedule.cgi";
  private static final String TIMER_LIST_PAGE = "/TimerSettings.htm";
  private static final String HTTP_PROTOCOL = "http://";
  private static final String INFO_CREATE_FORMAT = "satelliteInfo%s\\[(\\d+)\\]\\s*=\\s*new CreateServiceInfo\\((\\d+),\\s*(\\d+),\\s*\"([^\"]*)\"\\);";
  private static final String INFO_CHANNEL_FORMAT = "satelliteInfo%s\\[%d\\]\\.optionsValue\\[(\\d+)\\]\\s*=\\s*(\\d+);";
  private static final String INFO_SERVICE_FORMAT = "satelliteInfo%s\\[%d\\]\\.options\\[%d\\]\\s*=\\s*\"([^\"]*)\";";
  private static final String INFO_TUNER_FORMAT = "satelliteInfo%s\\[%d\\]\\.tunerNum\\[%d\\]\\s*=\\s*(\\d+);";
  private static final String RECORD_FORMAT = "RecordOnOff=1&Type=%d&Satellite=%d&Service=%d&Tuner=%d&Mode=%d&"
      + "DateDay=%d&DateMonth=%d&DateYear=%d&StartTimeHour=%d&StartTimeMinute=%d&DurationHour=%d&DurationMinute=%d&FileName=%s";
  private static final String SWITCH_FORMAT = "RecordOnOff=0&Type=%d&Satellite=%d&Service=%d&Tuner=%d&Mode=%d&"
      + "DateDay=%d&DateMonth=%d&DateYear=%d&StartTimeHour=%d&StartTimeMinute=%d&DurationHour=%d&DurationMinute=%d";
  private static final String DELETE_FORMAT = "EntryNum=%d";
  private static final String REPLY_ALERT = "alert(";
  private static final Pattern TIMER_ENTRY_PATTERN = Pattern
      .compile("new\\s*CreateTimerEntry\\((\\d+),\\s*(\\d+),\\s*(\\d+),\\s*\\\"([^\\\"]*)\\\",\\s*\\\"((\\d+)\\.\\s*([^\\\"]*))\\\",\\s*(\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(\\d+),\\s*\\\"([^\\\"]*)\\\"\\);");
  private static final Pattern RECEIVER_ADD_TIME_PATTERN = Pattern.compile("\\(-(\\d+)/\\+(\\d)\\)");
  private static final String ENCODING_UTF8 = "UTF-8";

  private final TopfieldConfiguration configuration;
  private final ArrayList<TopfieldServiceInfo> channels = new ArrayList<TopfieldServiceInfo>();

  /**
   * Create a connector.
   * 
   * @param configuration
   *          The configuration of the device
   */
  public TopfieldConnector(TopfieldConfiguration configuration) {
    this.configuration = configuration;
    Authenticator.setDefault(new AccessAuthenticator(configuration.getUsername(), configuration.getPasswordChars()));
  }

  /**
   * Read the contents of the given page.
   * 
   * @param page
   *          The page to read
   * @return A <code>StringBuffer</code> containing the page contents
   * @throws TopfieldConnectionException
   *           If the device could not be contacted
   */
  private StringBuffer readDeviceData(String page) throws TopfieldConnectionException {
    try {
      URL deviceURL = new URL(HTTP_PROTOCOL + configuration.getDeviceAddress() + page);
      URLConnection connection = deviceURL.openConnection();
      connection.setConnectTimeout(configuration.getConnectionTimeout());
      InputStream contentStream = (InputStream) connection.getContent();
      BufferedReader in = new BufferedReader(new InputStreamReader(contentStream));
      StringBuffer content = new StringBuffer();
      String line;
      while ((line = in.readLine()) != null) {
        content.append(line);
      }
      in.close();
      configuration.setDeviceUnreachable(false);
      return content;
    } catch (MalformedURLException e) {
      configuration.setDeviceUnreachable(true);
      throw new TopfieldConnectionException(e);
    } catch (IOException e) {
      configuration.setDeviceUnreachable(true);
      throw new TopfieldConnectionException(e);
    }
  }

  /**
   * Get the channels from the device.
   * 
   * @return The list of currently tuned channels
   * @throws TopfieldConnectionException
   *           If the device could not be contacted
   */
  public ArrayList<TopfieldServiceInfo> getDeviceChannels() throws TopfieldConnectionException {
    StringBuffer content = readDeviceData(NEW_TIMER_FORM);
    gatherChannels(content, TopfieldServiceType.TV);
    gatherChannels(content, TopfieldServiceType.RADIO);
    return channels;
  }

  /**
   * Collect the channels from the received content.
   * 
   * @param webPage
   *          The received content
   * @param type
   *          Collect TV or radio channel
   */
  private void gatherChannels(StringBuffer webPage, TopfieldServiceType type) {
    ArrayList<SatelliteInfo> satellites = new ArrayList<SatelliteInfo>();
    Matcher infoMatcher = Pattern.compile(String.format(INFO_CREATE_FORMAT, type)).matcher(webPage);
    while (infoMatcher.find()) {
      satellites.add(new SatelliteInfo(Integer.parseInt(infoMatcher.group(1)), Integer.parseInt(infoMatcher.group(2)),
          infoMatcher.group(4)));
    }
    for (SatelliteInfo info : satellites) {
      Matcher channelMatcher = Pattern.compile(String.format(INFO_CHANNEL_FORMAT, type, info.getIndex())).matcher(
          webPage);
      while (channelMatcher.find()) {
        int subIndex = Integer.parseInt(channelMatcher.group(1));
        int serviceIndex = Integer.parseInt(channelMatcher.group(2));
        Matcher serviceMatcher = Pattern.compile(String.format(INFO_SERVICE_FORMAT, type, info.getIndex(), subIndex))
            .matcher(webPage);
        Matcher tunerMatcher = Pattern.compile(String.format(INFO_TUNER_FORMAT, type, info.getIndex(), subIndex))
            .matcher(webPage);
        serviceMatcher.find();
        tunerMatcher.find();
        String serviceName = serviceMatcher.group(1);
        serviceName = serviceName.substring(serviceName.indexOf(" ") + 1);
        TopfieldServiceInfo serviceInfo = new TopfieldServiceInfo(serviceIndex, info.getName(), info.getIndex(),
            serviceName, Integer.parseInt(tunerMatcher.group(1)), type == TopfieldServiceType.TV);
        channels.add(serviceInfo);
      }
    }
  }

  /**
   * Add a R-timer.
   * 
   * @param parent
   *          The parent window calling this method
   * @param service
   *          The service to record
   * @param recordTime
   *          The time to record
   * @param repeat
   *          The repetition of the timer
   * @return <code>true</code> if the timer could be set
   * @throws TopfieldConnectionException
   *           If the device could not be contacted
   * @throws TopfieldServiceException
   *           If the service changed since last assignment
   * @throws TopfieldTunerException
   *           If no tuner could be found for the recording
   */
  public boolean addRecording(Window parent, TopfieldServiceInfo service, ProgramTime recordTime,
      TopfieldTimerMode repeat) throws TopfieldConnectionException, TopfieldServiceException, TopfieldTunerException {
    return addTimer(parent, service, recordTime, true, repeat);
  }

  /**
   * Add a P-timer.
   * 
   * @param parent
   *          The parent window calling this method
   * @param service
   *          The service to switch to
   * @param switchTime
   *          The time when to switch
   * @return <code>true</code> if the timer could be set
   * @throws TopfieldConnectionException
   *           If the device could not be contacted
   * @throws TopfieldServiceException
   *           If the service changed since last assignment
   * @throws TopfieldTunerException
   *           If no tuner could be found for the timer
   */
  public boolean addPTimer(Window parent, TopfieldServiceInfo service, ProgramTime switchTime)
      throws TopfieldConnectionException, TopfieldServiceException, TopfieldTunerException {
    return addTimer(parent, service, switchTime, false, TopfieldTimerMode.ONE_TIME);
  }

  /**
   * Add a timer.
   * 
   * @param parent
   *          The parent window calling this method
   * @param service
   *          The service to record
   * @param recordTime
   *          The time to record
   * @param record
   *          <code>true</code> creates a recording, <code>false</code> a
   *          P-timer
   * @param repeat
   *          The repetition of the timer
   * @return <code>true</code> if the timer could be set
   * @throws TopfieldConnectionException
   *           If the device could not be contacted
   * @throws TopfieldServiceException
   *           If the service changed since last assignment
   * @throws TopfieldTunerException
   *           If no tuner could be found for the recording
   */
  private boolean addTimer(Window parent, TopfieldServiceInfo service, ProgramTime recordTime, boolean record,
      TopfieldTimerMode repeat) throws TopfieldConnectionException, TopfieldServiceException, TopfieldTunerException {

    // RecordOnOff ...... 0 = Switch on only, 1 = Record
    // Type ............. 0 = TV, 1 = Radio
    // Satellite ........ Satellite index
    // Service .......... Service index
    // Tuner ............ Tuner number
    // Mode ............. 0 = One Time, 1 = Every Day, 2 = Every Weekend,
    // .. . . . . . . . . 3 = Weekly, 4 = Every Weekday
    // DateDay .......... Day
    // DateMonth ........ Month
    // DateYear ......... Year
    // StartTimeHour ... Start Hour
    // StartTimeMinute . Start Minute
    // DurationHour .... Duration Hour
    // DurationMinute .. Duration Minute
    // FileName ........ File name

    StringBuffer formContent = readDeviceData(NEW_TIMER_FORM);
    gatherChannels(formContent, (service.isTV() ? TopfieldServiceType.TV : TopfieldServiceType.RADIO));
    if (configuration.isCorrectTime()) {
      correctTime(formContent, recordTime);
    }
    if (channels.contains(service)) {
      Calendar recordStart = recordTime.getStartAsCalendar();
      Integer tuner = selectTuner(service, recordTime);
      if (tuner == null) {
        throw new TopfieldTunerException();
      }

      String request;
      try {
        if (record) {
          request = String.format(RECORD_FORMAT, (service.isTV() ? 0 : 1), service.getSatelliteNumber(), service
              .getChannelNumber(), tuner, repeat.toNumber(), recordStart.get(Calendar.DAY_OF_MONTH), recordStart
              .get(Calendar.MONTH) + 1, recordStart.get(Calendar.YEAR), recordStart.get(Calendar.HOUR_OF_DAY),
              recordStart.get(Calendar.MINUTE), recordTime.getLength() / 60, recordTime.getLength() % 60, URLEncoder
                  .encode(recordTime.getTitle(), ENCODING_UTF8));
        } else {
          request = String.format(SWITCH_FORMAT, (service.isTV() ? 0 : 1), service.getSatelliteNumber(), service
              .getChannelNumber(), tuner, repeat.toNumber(), recordStart.get(Calendar.DAY_OF_MONTH), recordStart
              .get(Calendar.MONTH) + 1, recordStart.get(Calendar.YEAR), recordStart.get(Calendar.HOUR_OF_DAY),
              recordStart.get(Calendar.MINUTE), recordTime.getLength() / 60, recordTime.getLength() % 60);
        }

        URL deviceURL = new URL(HTTP_PROTOCOL + configuration.getDeviceAddress() + INSERT_TIMER_PAGE);
        URLConnection connection = deviceURL.openConnection();
        connection.setConnectTimeout(configuration.getConnectionTimeout());
        connection.setDoOutput(true);
        OutputStreamWriter connectionWriter = new OutputStreamWriter(connection.getOutputStream());
        connectionWriter.write(request);
        connectionWriter.close();
        InputStream contentStream = connection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(contentStream));
        boolean insertOK = true;
        String line;
        while ((line = in.readLine()) != null) {
          if (line.contains(REPLY_ALERT)) {
            insertOK = false;
            break;
          }
        }
        in.close();
        getTimerList();
        configuration.setDeviceUnreachable(false);
        return insertOK;
      } catch (MalformedURLException e) {
        configuration.setDeviceUnreachable(true);
        throw new TopfieldConnectionException(e);
      } catch (IOException e) {
        configuration.setDeviceUnreachable(true);
        throw new TopfieldConnectionException(e);
      }
    } else {
      throw new TopfieldServiceException();
    }
  }

  /**
   * Correct the time according to the set pre- and postroll time on the
   * receiver.
   * 
   * @param formContent
   *          The content of the new timer form
   * @param recordTime
   *          The time to modify
   */
  private void correctTime(StringBuffer formContent, ProgramTime recordTime) {
    Matcher timeMatcher = RECEIVER_ADD_TIME_PATTERN.matcher(formContent);
    if (timeMatcher.find()) {
      int receiverPreroll = Integer.parseInt(timeMatcher.group(1));
      int receiverPostroll = Integer.parseInt(timeMatcher.group(2));

      recordTime.addMinutesToStart(receiverPreroll);
      recordTime.addMinutesToEnd(receiverPostroll * -1);
    }
  }

  /**
   * Select a tuner.
   * 
   * @param service
   *          The service to record
   * @param recordTime
   *          The time the recording should run
   * @return The selected tuner or <code>null</code> if no tuner found
   * @throws TopfieldConnectionException
   *           If the device could not be contacted
   */
  private Integer selectTuner(TopfieldServiceInfo service, ProgramTime recordTime) throws TopfieldConnectionException {
    if (configuration.isUseTuner4() && (service.getTuner() == 3)) {
      return 4;
    } else {
      getTimerList();
      HashSet<Integer> usedTuners = new HashSet<Integer>();
      for (TopfieldTimerEntry timerEntry : configuration.getTimerEntries()) {
        if (((timerEntry.getProgram().getStart().compareTo(recordTime.getStart()) >= 0) && (timerEntry.getProgram()
            .getStart().compareTo(recordTime.getEnd()) <= 0))
            || ((timerEntry.getProgram().getEnd().compareTo(recordTime.getStart()) >= 0) && (timerEntry.getProgram()
                .getEnd().compareTo(recordTime.getEnd()) <= 0))
            || ((timerEntry.getProgram().getStart().compareTo(recordTime.getStart()) <= 0) && (timerEntry.getProgram()
                .getEnd().compareTo(recordTime.getEnd()) >= 0))) {
          // This program overlaps with the new recording.
          usedTuners.add(timerEntry.getEntryTuner());
        }
      }
      switch (service.getTuner()) {
      case 1:
        if (!usedTuners.contains(1)) {
          return 1;
        }
        break;
      case 2:
        if (!usedTuners.contains(2)) {
          return 2;
        }
        break;
      case 3:
        if (usedTuners.isEmpty()) {
          return 1;
        } else if (usedTuners.contains(1) && !usedTuners.contains(2)) {
          return 2;
        } else if (!usedTuners.contains(1) && usedTuners.contains(2)) {
          return 1;
        }
        break;
      }
    }

    return null;
  }

  /**
   * Get the list of timers from the device.<br>
   * This method refreshes the list of currently set timer entries in the
   * configuration.
   * 
   * @throws TopfieldConnectionException
   *           If the device could not be contacted
   */
  public void getTimerList() throws TopfieldConnectionException {
    ArrayList<TopfieldTimerEntry> timerList = new ArrayList<TopfieldTimerEntry>();
    StringBuffer content = readDeviceData(TIMER_LIST_PAGE);
    Matcher timerEntryMatcher = TIMER_ENTRY_PATTERN.matcher(content);
    while (timerEntryMatcher.find()) {
      if (timerEntryMatcher.group(2).equals("0")) {
        // This is a P-timer => skip it
        continue;
      }

      int entryNumber = Integer.parseInt(timerEntryMatcher.group(1));
      TopfieldServiceType type = TopfieldServiceType.createFromNumber(Integer.parseInt(timerEntryMatcher.group(3)));
      int serviceNumber = Integer.parseInt(timerEntryMatcher.group(6)) - 1;
      int tuner = Integer.parseInt(timerEntryMatcher.group(8));
      TopfieldTimerMode mode = TopfieldTimerMode.createFromNumber(Integer.parseInt(timerEntryMatcher.group(9)));
      int startDay = Integer.parseInt(timerEntryMatcher.group(10));
      int startMonth = Integer.parseInt(timerEntryMatcher.group(11));
      int startYear = Integer.parseInt(timerEntryMatcher.group(12));
      int startHour = Integer.parseInt(timerEntryMatcher.group(13));
      int startMinute = Integer.parseInt(timerEntryMatcher.group(14));
      int length = Integer.parseInt(timerEntryMatcher.group(15));
      String fileName = timerEntryMatcher.group(16);

      Calendar recordingStart = Calendar.getInstance();
      recordingStart.set(Calendar.DAY_OF_MONTH, startDay);
      recordingStart.set(Calendar.MONTH, startMonth - 1);
      recordingStart.set(Calendar.YEAR, startYear);
      recordingStart.set(Calendar.HOUR_OF_DAY, startHour);
      recordingStart.set(Calendar.MINUTE, startMinute);
      recordingStart.set(Calendar.SECOND, 0);
      recordingStart.set(Calendar.MILLISECOND, 0);

      boolean channelHasData;
      do {
        channelHasData = false;
        Calendar recordingEnd = (Calendar) recordingStart.clone();
        recordingEnd.add(Calendar.MINUTE, length);

        int recordingStartMinutes = startHour * 60 + startMinute;
        int recordingEndMinutes = recordingStartMinutes + length;

        Channel tvBrowserChannel = configuration.getChannelForService(serviceNumber, type == TopfieldServiceType.TV);
        if (tvBrowserChannel != null) {
          Iterator<Program> programIterator = Plugin.getPluginManager().getChannelDayProgram(new Date(recordingStart),
              tvBrowserChannel);

          while (programIterator.hasNext()) {
            channelHasData = true;
            Program program = programIterator.next();
            int programStartMinutes = program.getStartTime();
            int programEndMinutes = programStartMinutes + program.getLength();

            if ((programStartMinutes >= recordingStartMinutes) && (programEndMinutes <= recordingEndMinutes)) {
              ProgramTime programTime = new ProgramTime(program, recordingStart.getTime(), recordingEnd.getTime());
              TopfieldTimerEntry timerEntry = new TopfieldTimerEntry(fileName, entryNumber, mode, tuner, programTime);
              timerList.add(timerEntry);
              break;
            }
          }
        }
      } while (channelHasData && nextRecordng(recordingStart, mode));
    }

    configuration.setTimerEntries(timerList);
  }

  private boolean nextRecordng(Calendar recordingTime, TopfieldTimerMode timerMode) {
    switch (timerMode) {
    case ONE_TIME:
      return false;
    case EVERY_DAY:
      recordingTime.add(Calendar.DAY_OF_MONTH, 1);
      return true;
    case WEEKLY:
      recordingTime.add(Calendar.DAY_OF_MONTH, 7);
      return true;
    case EVERY_WEEKDAY:
      switch (recordingTime.get(Calendar.DAY_OF_WEEK)) {
      case Calendar.SUNDAY:
      case Calendar.MONDAY:
      case Calendar.TUESDAY:
      case Calendar.WEDNESDAY:
      case Calendar.THURSDAY:
        recordingTime.add(Calendar.DAY_OF_MONTH, 1);
        break;
      case Calendar.FRIDAY:
        recordingTime.add(Calendar.DAY_OF_MONTH, 3);
        break;
      case Calendar.SATURDAY:
        recordingTime.add(Calendar.DAY_OF_MONTH, 2);
        break;
      }
      return true;
    case EVERY_WEEKEND:
      switch (recordingTime.get(Calendar.DAY_OF_WEEK)) {
      case Calendar.SUNDAY:
        recordingTime.add(Calendar.DAY_OF_MONTH, 6);
        break;
      case Calendar.MONDAY:
        recordingTime.add(Calendar.DAY_OF_MONTH, 5);
        break;
      case Calendar.TUESDAY:
        recordingTime.add(Calendar.DAY_OF_MONTH, 4);
        break;
      case Calendar.WEDNESDAY:
        recordingTime.add(Calendar.DAY_OF_MONTH, 3);
        break;
      case Calendar.THURSDAY:
        recordingTime.add(Calendar.DAY_OF_MONTH, 2);
        break;
      case Calendar.FRIDAY:
        recordingTime.add(Calendar.DAY_OF_MONTH, 1);
        break;
      case Calendar.SATURDAY:
        recordingTime.add(Calendar.DAY_OF_MONTH, 1);
        break;
      }
      return true;
    default:
      return false;
    }
  }

  /**
   * Delete a timer.
   * 
   * @param parent
   *          The parent window calling this method
   * @param entry
   *          The entry to delete
   * @return <code>true</code> if the entry was successfully deleted
   * @throws TopfieldConnectionException
   *           If the device could not be contacted
   */
  public boolean deleteRecording(Window parent, TopfieldTimerEntry entry) throws TopfieldConnectionException {
    String request = String.format(DELETE_FORMAT, entry.getEntryNumber());
    try {
      URL deviceURL = new URL(HTTP_PROTOCOL + configuration.getDeviceAddress() + DELETE_TIMER_PAGE);
      URLConnection connection = deviceURL.openConnection();
      connection.setConnectTimeout(configuration.getConnectionTimeout());
      connection.setDoOutput(true);
      OutputStreamWriter connectionWriter = new OutputStreamWriter(connection.getOutputStream());
      connectionWriter.write(request);
      connectionWriter.close();
      // No need to check the answer, it's always OK. Nevertheless we have to
      // read the result or the server on the device won't answer the next
      // request.
      InputStream contentStream = connection.getInputStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(contentStream));
      while (in.readLine() != null) {
        ;
      }
      in.close();
      getTimerList();
      boolean deleteOK = true;
      // Check the list of timers after deletion to determine if the deletion
      // was successful.
      for (TopfieldTimerEntry currentEntry : configuration.getTimerEntries()) {
        if (currentEntry.getProgram().getProgram().equals(entry.getProgram().getProgram())) {
          deleteOK = false;
          break;
        }
      }
      configuration.setDeviceUnreachable(false);
      return deleteOK;
    } catch (MalformedURLException e) {
      configuration.setDeviceUnreachable(true);
      throw new TopfieldConnectionException(e);
    } catch (IOException e) {
      configuration.setDeviceUnreachable(true);
      throw new TopfieldConnectionException(e);
    }
  }
}
