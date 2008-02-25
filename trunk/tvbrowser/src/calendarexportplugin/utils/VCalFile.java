package calendarexportplugin.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import calendarexportplugin.CalendarExportPlugin;

import util.exc.ErrorHandler;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.ui.Localizer;
import devplugin.Program;

public class VCalFile {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(VCalFile.class);
  /** DateFormat */
  private SimpleDateFormat mDate = new SimpleDateFormat("yyyyMMdd");

  /** TimeFormat */
  private SimpleDateFormat mTime = new SimpleDateFormat("HHmmss");

  /**
   * Exports a list of Programs into a vCal-File
   * 
   * @param intothis into this File
   * @param list List to export
   * @param settings The settings to use
   * @param formating The formating value for the progam
   */
  public void exportVCal(File intothis, Program[] list, Properties settings, AbstractPluginProgramFormating formating) {
    try {
      ParamParser parser = new ParamParser();
      
      boolean nulltime = settings.getProperty(CalendarExportPlugin.PROP_NULLTIME, "false").equals("true");

      mTime.setTimeZone(TimeZone.getTimeZone("GMT"));
      mDate.setTimeZone(TimeZone.getTimeZone("GMT"));
      PrintStream out = new PrintStream(new FileOutputStream(intothis));

      out.println("BEGIN:VCALENDAR");
      out.println("PRODID:-//TV-Browser//Calendar Exporter");
      out.println("VERSION:1.0");

      for (int i = 0; i < list.length; i++) {
        Program p = list[i];

        out.println();
        out.println("BEGIN:VEVENT");

        Calendar c = Calendar.getInstance();

        out.println("DCREATED:" + mDate.format(c.getTime()) + "T" + mTime.format(c.getTime()));
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

        c = CalendarToolbox.getStartAsCalendar(p);

        out.println("UID:" + mDate.format(c.getTime()) + "-" + p.getID());
        
        String summary = parser.analyse(formating.getTitleValue(), list[i]);
        
        out.println("SUMMARY:" + CalendarToolbox.noBreaks(summary));

        out.println("DTSTART:" + mDate.format(c.getTime()) + "T" + mTime.format(c.getTime()) + "Z");

        String desc = parser.analyse(formating.getContentValue(), list[i]);
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

        }

        switch (showtime) {
        case 0:
          out.println("TRANSP:0");
          break;
        case 1:
          out.println("TRANSP:1");
          break;
        default:
          break;
        }

        out.println("DTEND:" + mDate.format(c.getTime()) + "T" + mTime.format(c.getTime()) + "Z");

        if (settings.getProperty(CalendarExportPlugin.PROP_ALARM, "false").equals("true")) {
          try {
            int num = Integer.parseInt(settings.getProperty(CalendarExportPlugin.PROP_ALARMBEFORE, "0"));
            c.add(Calendar.MINUTE, num * -1);
          } catch (Exception e) {
          }
          
          out.println("DALARM:"+ mDate.format(c.getTime()) + "T" + mTime.format(c.getTime())+"Z;;1;beep!");
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
