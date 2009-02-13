/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package calendarexportplugin.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import util.exc.ErrorHandler;
import util.io.stream.PrintStreamProcessor;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.ui.Localizer;
import calendarexportplugin.CalendarExportPlugin;
import devplugin.Program;

public abstract class AbstractCalFile {
  /** Translator */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(AbstractCalFile.class);

  /** DateFormat */
  protected SimpleDateFormat mDate = new SimpleDateFormat("yyyyMMdd");

  /** TimeFormat */
  protected SimpleDateFormat mTime = new SimpleDateFormat("HHmmss");

  /**
   * Exports a list of Programs into a iCal/vCal file
   * 
   * @param intothis
   *          into this File
   * @param list
   *          List to export
   * @param settings
   *          The settings to use
   * @param formatting
   *          The formatting value for the program
   */
  public void export(File intothis, final Program[] list,
      final Properties settings, final AbstractPluginProgramFormating formatting) {
    try {
      final ParamParser parser = new ParamParser();

      final boolean nulltime = settings.getProperty(
          CalendarExportPlugin.PROP_NULLTIME, "false").equals("true");

      mTime.setTimeZone(TimeZone.getTimeZone("GMT"));
      mDate.setTimeZone(TimeZone.getTimeZone("GMT"));

      final PrintStreamProcessor processor = new PrintStreamProcessor() {
        public void process(PrintStream out) throws IOException {
          out.println("BEGIN:VCALENDAR");
          out.println("PRODID:-//TV-Browser//Calendar Exporter");
          printVersion(out);

          for (int i = 0; i < list.length; i++) {
            Program p = list[i];

            out.println();
            out.println("BEGIN:VEVENT");

            Calendar c = Calendar.getInstance();

            printCreated(out, mDate.format(c.getTime()) + "T"
                + mTime.format(c.getTime()), i);

            int classification = 0;

            try {
              classification = Integer.parseInt(settings.getProperty(
                  CalendarExportPlugin.PROP_CLASSIFICATION, "0"));
            } catch (Exception e) {

            }

            switch (classification) {
            case 0:
              out.println("CLASS:PUBLIC");
              break;
            case 1:
              out.println("CLASS:PRIVATE");
              break;
            case 2:
              out.println("CLASS:CONFIDENTIAL");
              break;

            default:
              break;
            }

            out.println("PRIORITY:3");

            if (settings.getProperty(CalendarExportPlugin.PROP_CATEGORY, "")
                .trim().length() > 0) {
              out.println("CATEGORIES:"
                  + settings
                      .getProperty(CalendarExportPlugin.PROP_CATEGORY, ""));
            }

            c = CalendarToolbox.getStartAsCalendar(p);

            out.println("UID:" + mDate.format(c.getTime()) + "-" + p.getID());

            String summary = parser.analyse(formatting.getTitleValue(), p);

            out.println("SUMMARY:" + CalendarToolbox.noBreaks(summary));

            out.println("DTSTART:" + mDate.format(c.getTime()) + "T"
                + mTime.format(c.getTime()) + "Z");

            String desc = parser.analyse(formatting.getContentValue(), p);
            if (parser.hasErrors()) {
              JOptionPane.showMessageDialog(null, parser.getErrorString(),
                  "Error", JOptionPane.ERROR_MESSAGE);
              return;
            }
            out.println("DESCRIPTION:" + CalendarToolbox.noBreaks(desc));

            if (!nulltime) {
              c = CalendarToolbox.getEndAsCalendar(p);
            }

            int showtime = 0;

            try {
              showtime = Integer.parseInt(settings.getProperty(
                  CalendarExportPlugin.PROP_SHOWTIME, "0"));
            } catch (Exception e) {

            }

            switch (showtime) {
            case 0:
              out.println("TRANSP:" + opaque());
              break;
            case 1:
              out.println("TRANSP:" + transparent());
              break;
            default:
              break;
            }

            out.println("DTEND:" + mDate.format(c.getTime()) + "T"
                + mTime.format(c.getTime()) + "Z");

            if (settings.getProperty(CalendarExportPlugin.PROP_ALARM, "false")
                .equals("true")) {
              printAlarm(settings, out, c);
            }

            out.println("END:VEVENT\n");
          }

          out.println();
          out.println("END:VCALENDAR");
          out.close();
        }
      };
      print(intothis, processor);

    } catch (Exception e) {
      ErrorHandler.handle(mLocalizer.msg("saveError",
          "An error occured while saving the Calendar-File"), e);
      e.printStackTrace();
    }
  }

  protected abstract void print(File intothis, PrintStreamProcessor processor)
      throws IOException;

  protected abstract void printAlarm(Properties settings, PrintStream out,
      Calendar c);

  protected abstract String transparent();

  protected abstract String opaque();

  protected abstract void printCreated(PrintStream out, String created,
      int sequence);

  protected abstract void printVersion(PrintStream out);
}