/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2005-12-26 21:46:18 +0100 (Mo, 26 Dez 2005) $
 *   $Author: troggan $
 * $Revision: 1764 $
 */
package calendarexportplugin.exporter;

import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import util.exc.ErrorHandler;
import util.io.IOUtilities;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.ui.Localizer;
import util.ui.login.LoginDialog;
import calendarexportplugin.CalendarExportPlugin;
import calendarexportplugin.CalendarExportSettings;
import calendarexportplugin.utils.CalendarToolbox;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Query;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.extensions.BaseEventEntry;
import com.google.gdata.data.extensions.EventEntry;
import com.google.gdata.data.extensions.Reminder;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import devplugin.Program;

/**
 * Exporter for Google Calendar API
 *
 * @author bodum
 */
public class GoogleExporter extends AbstractExporter {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GoogleExporter.class);

  protected static final String USERNAME = "Google_Username";
  private static final String PASSWORD = "Google_Password";
  protected static final String STORE_PASSWORD = "Google_StorePassword";
  protected static final String STORE_SETTINGS = "Google_StoreSettgins";
  protected static final String SELECTED_CALENDAR = "Google_SelectedCalendar";
  protected static final String REMINDER = "Google_Reminder";
  protected static final String REMINDER_MINUTES = "Google_ReminderMinutes";
  protected static final String REMINDER_SMS = "Google_ReminderSMS";
  protected static final String REMINDER_EMAIL = "Google_ReminderEMAIL";
  protected static final String REMINDER_ALERT = "Google_ReminderAlert";

  private String mPassword = "";

  public String getName() {
    return mLocalizer.msg("name", "Google Calendar");
  }

  public boolean exportPrograms(Program[] programs, CalendarExportSettings settings, AbstractPluginProgramFormating formatting) {
    try {
      boolean uploadedItems = false;
      mPassword = IOUtilities.xorDecode(settings.getExporterProperty(PASSWORD), 345903).trim();

      if (!settings.getExporterProperty(STORE_PASSWORD, false)) {
        if (!showLoginDialog(settings)) {
          return false;
        }
      }

      if (!settings.getExporterProperty(STORE_SETTINGS, false)) {
        if (!showCalendarSettings(settings)) {
          return false;
        }
      }

      GoogleService myService = new GoogleService("cl", "tvbrowser-tvbrowsercalenderplugin-" + CalendarExportPlugin.getInstance().getInfo().getVersion().toString());
      myService.setUserCredentials(settings.getExporterProperty(USERNAME).trim(), mPassword);

      URL postUrl =
              new URL("http://www.google.com/calendar/feeds/" + settings.getExporterProperty(SELECTED_CALENDAR) + "/private/full");

      SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd");
      formatDay.setTimeZone(TimeZone.getTimeZone("GMT"));
      SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
      formatTime.setTimeZone(TimeZone.getTimeZone("GMT"));

      ParamParser parser = new ParamParser();
      for (Program program : programs) {
        final String title = parser.analyse(formatting.getTitleValue(), program);

        // First step: search for event in calendar
        boolean createEvent = true;

        CalendarEventEntry entry = findEntryForProgram(myService, postUrl, title, program);

        if (entry != null) {
          int ret = JOptionPane.showConfirmDialog(null, mLocalizer.msg("alreadyAvailable", "already available", program.getTitle()), mLocalizer.msg("title", "Add event?"),  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

          if (ret != JOptionPane.YES_OPTION) {
            createEvent = false;
          }
        }

        // add event to calendar
        if (createEvent) {
          EventEntry myEntry = new EventEntry();

          myEntry.setTitle(new PlainTextConstruct(title));

          String desc = parser.analyse(formatting.getContentValue(), program);
          myEntry.setContent(new PlainTextConstruct(desc));

          Calendar c = CalendarToolbox.getStartAsCalendar(program);

          DateTime startTime = new DateTime(c.getTime(), c.getTimeZone());

          c = CalendarToolbox.getEndAsCalendar(program);

          DateTime endTime = new DateTime(c.getTime(), c.getTimeZone());

          When eventTimes = new When();

          eventTimes.setStartTime(startTime);
          eventTimes.setEndTime(endTime);

          myEntry.addTime(eventTimes);

          if (settings.getExporterProperty(REMINDER, false)) {
            int reminderMinutes = 0;

            try {
              reminderMinutes = settings.getExporterProperty(REMINDER_MINUTES, 0);
            } catch (NumberFormatException e) {
              e.printStackTrace();
            }

            if (settings.getExporterProperty(REMINDER_ALERT, false)) {
              addReminder(myEntry, reminderMinutes, Reminder.Method.ALERT);
            }

            if (settings.getExporterProperty(REMINDER_EMAIL, false)) {
              addReminder(myEntry, reminderMinutes, Reminder.Method.EMAIL);
            }

            if (settings.getExporterProperty(REMINDER_SMS, false)) {
              addReminder(myEntry, reminderMinutes, Reminder.Method.SMS);
            }

          }

          if (settings.isShowBusy()) {
              myEntry.setTransparency(BaseEventEntry.Transparency.OPAQUE);
          }
          else {
              myEntry.setTransparency(BaseEventEntry.Transparency.TRANSPARENT);
          }
          // Send the request and receive the response:
          myService.insert(postUrl, myEntry);
          uploadedItems = true;
        }
      }

      if (uploadedItems) {
        JOptionPane.showMessageDialog(CalendarExportPlugin.getInstance().getBestParentFrame(), mLocalizer.msg("exportDone", "Google Export done."), mLocalizer.msg("export", "Export"), JOptionPane.INFORMATION_MESSAGE);
      }

      return true;
    } catch (AuthenticationException e) {
      ErrorHandler.handle(mLocalizer.msg("loginFailure", "Problems during login to Service.\nMaybe bad username or password?"), e);
      settings.setExporterProperty(STORE_PASSWORD, false);
    } catch (Exception e) {
      ErrorHandler.handle(mLocalizer.msg("commError", "Error while communicating with Google!"), e);
    }

    return false;
  }

  private CalendarEventEntry findEntryForProgram(GoogleService myService, URL postUrl, String title, Program program) throws IOException, ServiceException {
    Calendar c = CalendarToolbox.getStartAsCalendar(program);
    DateTime startTime = new DateTime(c.getTime(), c.getTimeZone());

    Query myQuery = new Query(postUrl);
    myQuery.setFullTextQuery(title);
    CalendarEventFeed myResultsFeed = myService.query(myQuery, CalendarEventFeed.class);

    for (CalendarEventEntry entry : myResultsFeed.getEntries()) {
      if ((entry.getTimes().size() > 0) && (entry.getTimes().get(0).getStartTime().getValue() == startTime.getValue())) {
        return entry;
      }
    }

    return null;
  }

  /**
   * Create new Reminder
   *
   * @param myEntry         add Reminder to this Entry
   * @param reminderMinutes Remind x Minutes before event
   * @param method          what method to use
   */
  private void addReminder(EventEntry myEntry, int reminderMinutes, Reminder.Method method) {
    Reminder reminder = new Reminder();
    reminder.setMinutes(reminderMinutes);
    reminder.setMethod(method);
    myEntry.getReminder().add(reminder);
  }

  /**
   * Show the Settings Dialog for the exporter
   *
   * @param settings Settings
   * @return true, if ok was pressed
   * @throws IOException      Exception during connection
   * @throws ServiceException Problems with the google service
   */
  private boolean showCalendarSettings(CalendarExportSettings settings) throws IOException, ServiceException {
    GoogleSettingsDialog settingsDialog;

    Window wnd = CalendarExportPlugin.getInstance().getBestParentFrame();
    settingsDialog = new GoogleSettingsDialog(wnd, settings, mPassword);

    return settingsDialog.showDialog() == JOptionPane.OK_OPTION;

  }

  /**
   * Show the Login-Dialog
   *
   * @param settings
   *          Settings to use for this Dialog
   * @return true, if successful
   */
  private boolean showLoginDialog(CalendarExportSettings settings) {
    LoginDialog login;

    Window parent = CalendarExportPlugin.getInstance().getBestParentFrame();

    login = new LoginDialog(parent, settings.getExporterProperty(USERNAME),
        IOUtilities.xorDecode(settings.getExporterProperty(PASSWORD), 345903),
        settings.getExporterProperty(STORE_PASSWORD, false));

    if (login.askLogin() != JOptionPane.OK_OPTION) {
      return false;
    }

    if ((StringUtils.isBlank(login.getUsername()) || (StringUtils.isBlank(login.getPassword())))) {
      JOptionPane.showMessageDialog(parent, mLocalizer.msg("noUserOrPassword",
          "No Username or Password entered!"), Localizer
          .getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
      return false;
    }

    settings.setExporterProperty(USERNAME, login.getUsername().trim());

    if (login.storePasswords()) {
      settings.setExporterProperty(PASSWORD, IOUtilities.xorEncode(login.getPassword().trim(), 345903));
      settings.setExporterProperty(STORE_PASSWORD, true);
    } else {
      settings.setExporterProperty(PASSWORD, "");
      settings.setExporterProperty(STORE_PASSWORD, false);
    }

    mPassword = login.getPassword().trim();
    return true;
  }

  @Override
  public boolean hasSettingsDialog() {
    return true;
  }

  @Override
  public void showSettingsDialog(CalendarExportSettings settings) {
    if (showLoginDialog(settings)) {
      try {
        showCalendarSettings(settings);
      } catch (AuthenticationException e) {
        ErrorHandler.handle(mLocalizer.msg("loginFailure", "Problems while Login to Service.\nMaybee bad Username/Password ?"), e);
        settings.setExporterProperty(STORE_PASSWORD, false);
      } catch (Exception e) {
        ErrorHandler.handle(mLocalizer.msg("commError", "Error while communicating with Google!"), e);
      }
    }
  }

  public String getIconName() {
    return "google.png";
  }
}