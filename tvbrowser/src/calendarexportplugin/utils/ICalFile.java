package calendarexportplugin.utils;

import calendarexportplugin.CalendarExportPlugin;
import devplugin.Program;
import util.exc.ErrorHandler;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.ui.Localizer;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

public class ICalFile {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ICalFile.class);
  /** DateFormat */
  private SimpleDateFormat mDate = new SimpleDateFormat("yyyyMMdd");

  /** TimeFormat */
  private SimpleDateFormat mTime = new SimpleDateFormat("HHmmss");

  /**
   * Exports a list of Programs into a iCal-File
   * 
   * @param intothis into this File
   * @param list List to export
   * @param settings Settings for the Export
   * @param formating The formating for the program.
   */
  public void exportICal(File intothis, Program[] list, Properties settings, AbstractPluginProgramFormating formating) {
    try {
      ParamParser parser = new ParamParser();
      boolean nulltime = settings.getProperty(CalendarExportPlugin.PROP_NULLTIME, "false").equals("true");

      mTime.setTimeZone(TimeZone.getTimeZone("GMT"));
      mDate.setTimeZone(TimeZone.getTimeZone("GMT"));

      PrintStream out = new PrintStream(new FileOutputStream(intothis),true,"UTF8");

      out.println("BEGIN:VCALENDAR");
      out.println("PRODID:-//TV Browser//Calendar Exporter");
      out.println("VERSION:2.0");

        for (Program p : list) {

            out.println();
            out.println("BEGIN:VEVENT");

            Calendar c = Calendar.getInstance();

            out.println("CREATED:" + mDate.format(c.getTime()) + "T" + mTime.format(c.getTime()));

            int classification = 0;

            try {
                classification = Integer.parseInt(settings.getProperty(CalendarExportPlugin.PROP_CLASSIFICATION, "0"));
            } catch (Exception e) {
                // Empty
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

            if (settings.getProperty(CalendarExportPlugin.PROP_CATEGORIE, "").trim().length() > 0) {
                out.println("CATEGORIES:" + settings.getProperty(CalendarExportPlugin.PROP_CATEGORIE, ""));
            }

            c = CalendarToolbox.getStartAsCalendar(p);

            out.println("UID:" + mDate.format(c.getTime()) + "-" + p.getID());
            
            String summary = parser.analyse(formating.getTitleValue(), p);
            
            out.println("SUMMARY:" + CalendarToolbox.noBreaks(summary));

            out.println("DTSTART:" + mDate.format(c.getTime()) + "T" + mTime.format(c.getTime()) + "Z");

            String desc = parser.analyse(formating.getContentValue(), p);
            if (parser.hasErrors()) {
                JOptionPane.showMessageDialog(null, parser.getErrorString(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            out.println("DESCRIPTION:" + CalendarToolbox.noBreaks(desc));

            if (!nulltime) {
                c = CalendarToolbox.getEndAsCalendar(p);
            }

            int showtime = 0;

            try {
                showtime = Integer.parseInt(settings.getProperty(CalendarExportPlugin.PROP_SHOWTIME, "0"));
            } catch (Exception e) {
                // Empty
            }

            switch (showtime) {
                case 0:
                    out.println("TRANSP:OPAQUE");
                    break;
                case 1:
                    out.println("TRANSP:TRANSPARENT");
                    break;
                default:
                    break;
            }

            out.println("DTEND:" + mDate.format(c.getTime()) + "T" + mTime.format(c.getTime()) + "Z");

            if (settings.getProperty(CalendarExportPlugin.PROP_ALARM, "false").equals("true")) {
                out.println("BEGIN:VALARM");
                out.println("DESCRIPTION:");
                out.println("ACTION:DISPLAY");
                out.println("TRIGGER;VALUE=DURATION:-PT" + settings.getProperty(CalendarExportPlugin.PROP_ALARMBEFORE, "0") + "M");
                out.println("END:VALARM");
            }

            out.println("END:VEVENT\n");
        }

        out.println();
      out.println("END:VCALENDAR");
      out.close();
    } catch (Exception e) {
      ErrorHandler.handle(mLocalizer.msg("saveError", "An error occured while saving the Calendar-File"), e);
      e.printStackTrace();
    }

  }


}
