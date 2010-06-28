/*
 * Created on 18.06.2004
 */
package calendarexportplugin.utils;

import java.util.Calendar;

import devplugin.Program;

/**
 * This Class is a ToolBox for the iCal/vCal exporter
 *
 * @author bodo
 */
public class CalendarToolbox {
  /**
   * Replaces Newline-Characters with ' '
   *
   * @param b replace here
   * @return String without Newline
   */
  public static String noBreaks(String b) {
    b = b.trim().replaceAll("\\\\", "\\\\\\\\");
    b = b.replaceAll(",", "\\\\,");
    b = b.replaceAll("\\n", "\\\\n");
    return b;
  }

  /**
   * Gets the Start-Time as Calendar
   *
   * @param p Program
   * @return Start-Time
   */
  public static Calendar getStartAsCalendar(Program p) {
    Calendar cal = p.getDate().getCalendar();

    int min = p.getStartTime();

    int hour = min % 60;

    min -= hour * 60;

    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, min);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    return cal;
  }

  /**
   * Gets the End-Time as Calendar
   *
   * @param p Program
   * @return End-Time
   */
  public static Calendar getEndAsCalendar(Program p) {
    Calendar cal = getStartAsCalendar(p);

    int leng = p.getLength();

    if (leng <= 0) {
      leng = 0;
    }

    cal.add(Calendar.MINUTE, leng);

    return cal;
  }

  /**
   * Remove special Chars from Filename
   *
   * @param fileName Filename to celan
   * @return cleaned Filename
   * @since 2.62
   *
   */
  public static String cleanFilename(String fileName) {
    return fileName.replaceAll("[^A-z0-9ŠšŸ\\s]*", "");
  }
}