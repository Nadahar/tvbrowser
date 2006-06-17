package calendarexportplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import util.exc.ErrorHandler;
import util.paramhandler.ParamParser;
import util.ui.Localizer;
import devplugin.Program;

public class ICalExporter {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ICalExporter.class);
  /** DateFormat */
  private SimpleDateFormat mDate = new SimpleDateFormat("yyyyMMdd");

  /** TimeFormat */
  private SimpleDateFormat mTime = new SimpleDateFormat("HHmmss");

  /** TimeFormat */
  private SimpleDateFormat mExtTime = new SimpleDateFormat("HH:mm");


  /**
   * Exports a list of Programs into a iCal-File
   * 
   * @param intothis into this File
   * @param list List to export
   * @param nulltime Lenght of Programs = 0?
   */
  public void exportICal(File intothis, Program[] list, Properties settings) {
    try {
      ParamParser parser = new ParamParser();
      boolean nulltime = settings.getProperty(CalendarExportPlugin.PROP_NULLTIME, "false").equals("true");

      mTime.setTimeZone(TimeZone.getTimeZone("GMT"));
      mDate.setTimeZone(TimeZone.getTimeZone("GMT"));

      PrintStream out = new PrintStream(new FileOutputStream(intothis));

      out.println("BEGIN:VCALENDAR");
      out.println("PRODID:-//TV Browser//Calendar Exporter");
      out.println("VERSION:2.0");

      for (int i = 0; i < list.length; i++) {

        Program p = list[i];

        out.println();
        out.println("BEGIN:VEVENT");

        Calendar c = Calendar.getInstance();

        out.println("CREATED:" + mDate.format(c.getTime()) + "T" + mTime.format(c.getTime()));
        out.println("SEQUENZ:" + i);

        int classification = 0;

        try {
          classification = Integer.parseInt(settings.getProperty(CalendarExportPlugin.PROP_CLASSIFICATION, "0"));
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

        if (settings.getProperty(CalendarExportPlugin.PROP_CATEGORIE, "").trim().length() > 0) {
          out.println("CATEGORIES:" + settings.getProperty(CalendarExportPlugin.PROP_CATEGORIE, ""));
        }

        c = CalendarExporter.getStartAsCalendar(p);

        out.println("UID:" + mDate.format(c.getTime()) + "-" + p.getID());
        out.println("SUMMARY:" + p.getChannel().getName() + " - " + CalendarExporter.noBreaks(p.getTitle()));

        out.println("DTSTART:" + mDate.format(c.getTime()) + "T" + mTime.format(c.getTime()) + "Z");

        String desc = parser.analyse(settings.getProperty(CalendarExportPlugin.PROP_PARAM, CalendarExportPlugin.DEFAULT_PARAMETER), list[i]);
        if (parser.hasErrors()) {
          JOptionPane.showMessageDialog(null, parser.getErrorString(), "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }        
        out.println("DESCRIPTION:" + CalendarExporter.noBreaks(desc));
        
        if (!nulltime) {
          c = CalendarExporter.getEndAsCalendar(p);
        }

        int showtime = 0;

        try {
          showtime = Integer.parseInt(settings.getProperty(CalendarExportPlugin.PROP_SHOWTIME, "0"));
        } catch (Exception e) {

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
