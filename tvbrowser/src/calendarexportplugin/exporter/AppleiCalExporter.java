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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

import util.exc.ErrorHandler;
import util.misc.AppleScriptRunner;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import calendarexportplugin.CalendarExportPlugin;
import calendarexportplugin.CalendarExportSettings;
import calendarexportplugin.utils.CalendarToolbox;
import devplugin.Program;

/**
 * Export for Apple iCal
 *
 * @author bodum
 */
public class AppleiCalExporter extends AbstractExporter {

    static final String PROPERTY_CALENDAR_NAME = "calendar name";

    public String getName() {
        return "Apple iCal";
    }

    public boolean exportPrograms(Program[] programs, CalendarExportSettings settings, AbstractPluginProgramFormating formatting) {
        AppleScriptRunner runner = new AppleScriptRunner();

        StringBuilder script = new StringBuilder();

        SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatHour = new SimpleDateFormat("HH:mm");

        String calTitle = settings.getExporterProperty(AppleiCalExporter.PROPERTY_CALENDAR_NAME);
        if (StringUtils.isBlank(calTitle)) {
          calTitle = "TV-Browser";
        }
        script.append("property myTVCalendar : \"" + calTitle + "\"\n");

        script.append("on stringToList from theString for myDelimiters\n" +
          "\ttell AppleScript\n" +
          "\t\tset theSavedDelimiters to AppleScript's text item delimiters\n" +
          "\t\tset text item delimiters to myDelimiters\n" +
          "\t\t\n" +
          "\t\tset outList to text items of theString\n" +
          "\t\tset text item delimiters to theSavedDelimiters\n" +
          "\t\t\n" +
          "\t\treturn outList\n" +
          "\tend tell\n" +
          "end stringToList\n" +
          "\n" +
          "\n" +
          "on getDateForISOdate(theISODate, theISOTime)\n" +
          "\tlocal myDate\n" +
          "\t-- converts an ISO format (YYYY-MM-DD) and time to a date object\n" +
          "\tset monthConstants to {January, February, March, April, May, June, July, August, September, October, November, December}\n" +
          "\t\n" +
          "\tset theISODate to (stringToList from (theISODate) for \"-\")\n" +
          "\tset theISOTime to (stringToList from (theISOTime) for \":\")\n" +
          "\t\n" +
          "\tset myDate to current date\n" +
          "\tset month of myDate to 1\n" +
          "\t\n" +
          "\ttell theISODate\n" +
          "\t\tset year of myDate to item 1\n" +
          "\t\tset day of myDate to item 3\n" +
          "\t\tset month of myDate to item (item 2) of monthConstants\n" +
          "\tend tell\n" +
          "\ttell theISOTime\n" +
          "\t\tset hours of myDate to item 1\n" +
          "\t\tset minutes of myDate to item 2\n" +
          "\t\tset seconds of myDate to 0\n" +
          "\tend tell\n" +
          "\t\n" +
          "\treturn myDate\n" +
          "end getDateForISOdate\n" +
          "\n");

        script.append("\n");
        script.append("tell application \"iCal\"\n");
        script.append("  if (exists (calendars whose title is myTVCalendar)) then\n");
        script.append("    set TVBrowserCalendar to first item of (calendars whose title is myTVCalendar)\n");
        script.append("  else\n");
        script.append("    set TVBrowserCalendar to make new calendar with properties {title:myTVCalendar}\n");
        script.append("  end if\n");
        script.append("\n");

        for (Program program : programs) {
            final Calendar start = CalendarToolbox.getStartAsCalendar(program);
            final Calendar end   = CalendarToolbox.getEndAsCalendar(program);

            script.append("  set startDate to my getDateForISOdate(\"").append(formatDay.format(start.getTime())).append("\", \"").append(formatHour.format(start.getTime())).append("\")\n");
            script.append("  set endDate to my getDateForISOdate(\"").append(formatDay.format(end.getTime())).append("\", \"").append(formatHour.format(end.getTime())).append("\")\n");
            script.append("\n");
            script.append("  set props to {start date:startDate, end date:endDate, summary:\"");

            ParamParser parser = new ParamParser();

            String title = parser.analyse(formatting.getTitleValue(), program);
            script.append(title);

            script.append("\", description:\"");

            String desc = parser.analyse(formatting.getContentValue(), program);
            script.append(desc.replaceAll("\"", "\\\\\"").replace('\n', ' '));

            script.append("\"}\n");
            script.append("  set theEvent to make new event at end of (events of TVBrowserCalendar) with properties props\n");

            if (settings.getUseAlarm()) {
                script.append("  make new display alarm at beginning of theEvent with properties {trigger interval:-");
                script.append(settings.getAlarmMinutes());
                script.append("}\n");
            }

        }

        script.append("end tell\n");

        try {
            runner.executeScript(script.toString());
        } catch (IOException e) {
            e.printStackTrace();
            ErrorHandler.handle("Error during execution of the applescript", e);
        }

        return true;
    }

  public String getIconName() {
    return "apple_ical.png";
  }

  @Override
  public boolean hasSettingsDialog() {
    return true;
  }

  @Override
  public void showSettingsDialog(CalendarExportSettings settings) {
    Window wnd = CalendarExportPlugin.getInstance().getBestParentFrame();
    AppleSettingsDialog settingsDialog = new AppleSettingsDialog(wnd, settings);
    settingsDialog.showDialog();
  }

}