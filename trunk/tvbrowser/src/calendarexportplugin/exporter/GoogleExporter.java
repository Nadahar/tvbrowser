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

import calendarexportplugin.CalendarExportPlugin;
import calendarexportplugin.utils.CalendarToolbox;
import com.google.gdata.client.GoogleService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.extensions.EventEntry;
import com.google.gdata.data.extensions.Reminder;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.BaseEventEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import devplugin.Program;
import util.exc.ErrorHandler;
import util.io.IOUtilities;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.ui.Localizer;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Window;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;
import java.io.IOException;

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

  public static final String USERNAME = "Google_Username";
  public static final String PASSWORD = "Google_Password";
  public static final String STOREPASSWORD = "Google_StorePassword";
  public static final String STORESETTINGS = "Google_StoreSettgins";
  public static final String SELECTEDCALENDAR = "Google_SelectedCalendar";
  public static final String REMINDER = "Google_Reminder";
  public static final String REMINDERMINUTES = "Google_ReminderMinutes";
  public static final String REMINDERSMS = "Google_ReminderSMS";
  public static final String REMINDEREMAIL = "Google_ReminderEMAIL";
  public static final String REMINDERALERT = "Google_ReminderAlert";

  private String mPassword = "";

  /*
   * (non-Javadoc)
   * @see calendarexportplugin.exporter.ExporterIf#getName()
   */
  public String getName() {
    return mLocalizer.msg("name", "Google Calendar");
  }

  /*
   * (non-Javadoc)
   * @see calendarexportplugin.exporter.ExporterIf#exportPrograms(devplugin.Program[], java.util.Properties)
   */
  public boolean exportPrograms(Program[] programs, Properties settings, AbstractPluginProgramFormating formating) {
    try {
      mPassword = IOUtilities.xorDecode(settings.getProperty(PASSWORD, ""), 345903).trim();

      if (!settings.getProperty(STOREPASSWORD, "false").equals("true")) {
        if (!showLoginDialog(settings)) {
          return false;
        }
      }

      if (!settings.getProperty(STORESETTINGS, "false").equals("true")) {
        if (!showCalendarSettings(settings)) {
          return false;
        }
      }

      GoogleService myService = new GoogleService("cl", "tvbrowser-tvbrowsercalenderplugin-" + CalendarExportPlugin.getInstance().getInfo().getVersion().toString());
      myService.setUserCredentials(settings.getProperty(USERNAME, "").trim(), mPassword);

      URL postUrl =
              new URL("http://www.google.com/calendar/feeds/" + settings.getProperty(SELECTEDCALENDAR) + "/private/full");

      SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd");
      formatDay.setTimeZone(TimeZone.getTimeZone("GMT"));
      SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
      formatTime.setTimeZone(TimeZone.getTimeZone("GMT"));

      for (Program program : programs) {
        EventEntry myEntry = new EventEntry();

        ParamParser parser = new ParamParser();
        String title = parser.analyse(formating.getTitleValue(), program);

        myEntry.setTitle(new PlainTextConstruct(title));

        String desc = parser.analyse(formating.getContentValue(), program);
        myEntry.setContent(new PlainTextConstruct(desc));

        Calendar c = CalendarToolbox.getStartAsCalendar(program);

        DateTime startTime = new DateTime(c.getTime(), c.getTimeZone());

        c = CalendarToolbox.getEndAsCalendar(program);

        DateTime endTime = new DateTime(c.getTime(), c.getTimeZone());

        When eventTimes = new When();

        eventTimes.setStartTime(startTime);
        eventTimes.setEndTime(endTime);

        myEntry.addTime(eventTimes);

        if (settings.getProperty(REMINDER, "false").equals("true")) {
          int reminderMinutes = 0;

          try {
            reminderMinutes = Integer.parseInt(settings.getProperty(REMINDERMINUTES, "0"));
          } catch (NumberFormatException e) {
            e.printStackTrace();
          }

          if (settings.getProperty(REMINDERALERT, "false").equals("true")) {
            addReminder(myEntry, reminderMinutes, Reminder.Method.ALERT);
          }

          if (settings.getProperty(REMINDEREMAIL, "false").equals("true")) {
            addReminder(myEntry, reminderMinutes, Reminder.Method.EMAIL);
          }

          if (settings.getProperty(REMINDERSMS, "false").equals("true")) {
            addReminder(myEntry, reminderMinutes, Reminder.Method.SMS);
          }

        }

        int showtime = 0;

        try {
          showtime = Integer.parseInt(settings.getProperty(CalendarExportPlugin.PROP_SHOWTIME, "0"));
        } catch (Exception e) {
          // Empty
        }

        switch (showtime) {
          case 0:
            myEntry.setTransparency(BaseEventEntry.Transparency.OPAQUE);
            break;
          case 1:
            myEntry.setTransparency(BaseEventEntry.Transparency.TRANSPARENT);
            break;
          default:
            break;
        }
        // Send the request and receive the response:
        myService.insert(postUrl, myEntry);
      }

      JOptionPane.showMessageDialog(CalendarExportPlugin.getInstance().getBestParentFrame(), mLocalizer.msg("exportDone", "Google Export done."), mLocalizer.msg("export", "Export"), JOptionPane.INFORMATION_MESSAGE);
      return true;
    } catch (AuthenticationException e) {
      ErrorHandler.handle(mLocalizer.msg("loginFailure", "Problems during login to Service.\nMaybe bad username or password?"), e);
      settings.setProperty(GoogleExporter.STOREPASSWORD, "false");
    } catch (Exception e) {
      ErrorHandler.handle(mLocalizer.msg("commError", "Error while communicating with Google!"), e);
    }

    return false;
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
  private boolean showCalendarSettings(Properties settings) throws IOException, ServiceException {
    GoogleSettingsDialog settingsDialog;

    Window wnd = CalendarExportPlugin.getInstance().getBestParentFrame();

    if (wnd instanceof JDialog) {
      settingsDialog = new GoogleSettingsDialog((JDialog) wnd, settings, mPassword);
    } else {
      settingsDialog = new GoogleSettingsDialog((JFrame) wnd, settings, mPassword);
    }

    return settingsDialog.showDialog() == JOptionPane.OK_OPTION;

  }

  /**
   * Show the Login-Dialog
   *
   * @param settings Settings to use for this Dialog
   * @return true, if successfull
   */
  private boolean showLoginDialog(Properties settings) {
    GoogleLoginDialog login;

    Window wnd = CalendarExportPlugin.getInstance().getBestParentFrame();

    if (wnd instanceof JDialog) {
      login = new GoogleLoginDialog((JDialog) wnd,
              settings.getProperty(USERNAME, ""),
              IOUtilities.xorDecode(settings.getProperty(PASSWORD, ""), 345903),
              settings.getProperty(STOREPASSWORD, "false").equals("true"));
    } else {
      login = new GoogleLoginDialog((JFrame) wnd,
              settings.getProperty(USERNAME, ""),
              IOUtilities.xorDecode(settings.getProperty(PASSWORD, ""), 345903),
              settings.getProperty(STOREPASSWORD, "false").equals("true"));
    }

    if (login.askLogin() != JOptionPane.OK_OPTION) {
      return false;
    }

    if ((login.getUsername().trim().length() == 0) || (login.getPassword().trim().length() == 0)) {
      JOptionPane.showMessageDialog(wnd, mLocalizer.msg("noUserOrPassword", "No Username or Password entered!"), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
      return false;
    }

    settings.setProperty(USERNAME, login.getUsername().trim());

    if (login.storePasswords()) {
      settings.setProperty(PASSWORD, IOUtilities.xorEncode(login.getPassword().trim(), 345903));
      settings.setProperty(STOREPASSWORD, "true");
    } else {
      settings.setProperty(PASSWORD, "");
      settings.setProperty(STOREPASSWORD, "false");
    }

    mPassword = login.getPassword().trim();
    return true;
  }

  /*
   * (non-Javadoc)
   * @see calendarexportplugin.exporter.ExporterIf#hasSettingsDialog()
   */
  @Override
  public boolean hasSettingsDialog() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see calendarexportplugin.exporter.ExporterIf#showSettingsDialog(java.util.Properties)
   */
  @Override
  public void showSettingsDialog(Properties settings) {
    if (showLoginDialog(settings)) {
      try {
        showCalendarSettings(settings);
      } catch (AuthenticationException e) {
        ErrorHandler.handle(mLocalizer.msg("loginFailure", "Problems while Login to Service.\nMaybee bad Username/Password ?"), e);
        settings.setProperty(GoogleExporter.STOREPASSWORD, "false");
      } catch (Exception e) {
        ErrorHandler.handle(mLocalizer.msg("commError", "Error while communicating with Google!"), e);
      }
    }
  }
}