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
import java.util.TimeZone;

import util.exc.ErrorHandler;
import util.io.stream.PrintStreamProcessor;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.ui.Localizer;
import calendarexportplugin.CalendarExportSettings;
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
      final CalendarExportSettings settings, final AbstractPluginProgramFormating formatting) {
    try {
      final ParamParser parser = new ParamParser();

      final boolean nulltime = settings.getNullTime();

      mTime.setTimeZone(TimeZone.getTimeZone("GMT"));
      mDate.setTimeZone(TimeZone.getTimeZone("GMT"));

      final PrintStreamProcessor processor = new PrintStreamProcessor() {
        public void process(PrintStream out) throws IOException {
          out.println("BEGIN:VCALENDAR");
          out.println("PRODID:-//TV-Browser//Calendar Export Plugin");
          printVersion(out);

          for (int i = 0; i < list.length; i++) {
            Program p = list[i];

            out.println();
            out.println("BEGIN:VEVENT");

            Calendar c = Calendar.getInstance();

            printCreated(out, mDate.format(c.getTime()) + "T"
                + mTime.format(c.getTime()), i);

            if (settings.isClassificationPrivate()) {
              out.println("CLASS:PRIVATE");
            }
            else if (settings.isClassificationConfidential()) {
              out.println("CLASS:CONFIDENTIAL");
            }
            else if (settings.isClassificationPublic()) {
              out.println("CLASS:PUBLIC");
            }

            out.println("PRIORITY:3");

            String category = settings.getCategory().trim();
            if (category.length() > 0) {
              out.println("CATEGORIES:" + category);
            }

            c = CalendarToolbox.getStartAsCalendar(p);

            out.println("UID:" + mDate.format(c.getTime()) + "-" + p.getID());

            String summary = parser.analyse(formatting.getTitleValue(), p);

            out.println("SUMMARY:" + CalendarToolbox.noBreaks(summary));

            out.println("DTSTART:" + mDate.format(c.getTime()) + "T"
                + mTime.format(c.getTime()) + "Z");

            String desc = parser.analyse(formatting.getContentValue(), p);
            if (parser.showErrors()) {
              return;
            }
            out.println("DESCRIPTION:" + CalendarToolbox.noBreaks(desc));

            if (!nulltime) {
              c = CalendarToolbox.getEndAsCalendar(p);
            }

            if (settings.isShowBusy()) {
              out.println("TRANSP:" + opaque());
            }
            else if (settings.isShowFree()) {
              out.println("TRANSP:" + transparent());
            }

            out.println("DTEND:" + mDate.format(c.getTime()) + "T"
                + mTime.format(c.getTime()) + "Z");

            if (settings.getUseAlarm()) {
              printAlarm(settings.getAlarmMinutes(), out, c);
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
          "An error occured while saving the calendar file"), e);
      e.printStackTrace();
    }
  }

  protected abstract void print(File intothis, PrintStreamProcessor processor)
      throws IOException;

  protected abstract void printAlarm(final int minutes, PrintStream out,
      Calendar c);

  protected abstract String transparent();

  protected abstract String opaque();

  protected abstract void printCreated(PrintStream out, String created,
      int sequence);

  protected abstract void printVersion(PrintStream out);
}