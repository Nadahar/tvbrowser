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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import util.exc.ErrorHandler;
import util.io.IOUtilities;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.ui.Localizer;
import calendarexportplugin.CalendarExportPlugin;
import calendarexportplugin.utils.CalendarToolbox;

import com.google.gdata.client.GoogleService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.extensions.EventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;

import devplugin.Program;

/**
 * Exporter for Google Calendar API
 *  
 * @author bodum
 */
public class GoogleExporter extends AbstractExporter {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GoogleExporter.class);
  
  private static final String USERNAME = "Google_Username";
  private static final String PASSWORD = "Google_Password";
  private static final String STOREPASSWORD = "Google_StorePassword";
  
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
      GoogleService myService = new GoogleService("cl", "tvbrowser-tvbrowsercalenderplugin-" + CalendarExportPlugin.getInstance().getInfo().getVersion().toString());
      
      GoogleLoginDialog login;
      
      Window wnd = CalendarExportPlugin.getInstance().getBestParentFrame();
      
      if (wnd instanceof JDialog) {
        login = new GoogleLoginDialog((JDialog)wnd,
            settings.getProperty(USERNAME, ""), 
            IOUtilities.xorDecode(settings.getProperty(PASSWORD, ""), 345903), 
            settings.getProperty(STOREPASSWORD, "false").equals("true"));
      } else {
        login = new GoogleLoginDialog((JFrame)wnd,
            settings.getProperty(USERNAME, ""), 
            IOUtilities.xorDecode(settings.getProperty(PASSWORD, ""), 345903), 
            settings.getProperty(STOREPASSWORD, "false").equals("true"));
      }
      
      if (login.askLogin() != JOptionPane.OK_OPTION) {
        return false;
      }
      
      if ((login.getUsername().trim().length() == 0) ||(login.getPassword().trim().length() == 0)) {
        JOptionPane.showMessageDialog(wnd, mLocalizer.msg("noUserOrPassword","No Username or Password entered!"), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
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
      
      myService.setUserCredentials(login.getUsername().trim(), login.getPassword().trim());
      
      URL postUrl =
        new URL("http://www.google.com/calendar/feeds/"+login.getUsername().trim()+"/private/full");
      
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
        
        DateTime startTime = DateTime.parseDateTime(formatDay.format(c.getTime())+"T"+formatTime.format(c.getTime()));

        c = CalendarToolbox.getEndAsCalendar(program);

        DateTime endTime = DateTime.parseDateTime(formatDay.format(c.getTime())+"T"+formatTime.format(c.getTime()));
        
        When eventTimes = new When();
        
        eventTimes.setStartTime(startTime);
        eventTimes.setEndTime(endTime);
        
        myEntry.addTime(eventTimes);
        
        // Send the request and receive the response:
        EventEntry insertedEntry = myService.insert(postUrl, myEntry);
      }
      
      JOptionPane.showMessageDialog(CalendarExportPlugin.getInstance().getBestParentFrame(), mLocalizer.msg("exportDone", "Google Export done."), mLocalizer.msg("export","Export"), JOptionPane.INFORMATION_MESSAGE);
      return true;
    } catch (AuthenticationException e) {
      ErrorHandler.handle(mLocalizer.msg("loginFailure", "Problems while Login to Service.\nMaybee bad Username/Password ?"), e);
    } catch (Exception e) {
      ErrorHandler.handle(mLocalizer.msg("commError", "Error while communicating with Google!"), e);
    }
    
    return false;
  }
}