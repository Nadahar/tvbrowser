package calendarexportplugin.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Properties;

import util.io.stream.PrintStreamProcessor;
import util.io.stream.StreamUtilities;
import calendarexportplugin.CalendarExportPlugin;

public class VCalFile extends AbstractCalFile {

  protected void print(File intothis, final PrintStreamProcessor processor)
      throws IOException {
    StreamUtilities.printStream(intothis, processor);
  }

  protected void printVersion(PrintStream out) {
    out.println("VERSION:1.0");
  }

  protected void printCreated(PrintStream out, String created, int sequence) {
    out.println("DCREATED:" + created);
    out.println("SEQUENCE:" + sequence);

  }

  protected String opaque() {
    return "0";
  }

  protected String transparent() {
    return "1";
  }

  protected void printAlarm(final Properties settings, PrintStream out,
      Calendar c) {
    try {
      int num = Integer.parseInt(settings.getProperty(
          CalendarExportPlugin.PROP_ALARMBEFORE, "0"));
      c.add(Calendar.MINUTE, num * -1);
    } catch (Exception e) {
    }

    out.println("DALARM:" + mDate.format(c.getTime()) + "T"
        + mTime.format(c.getTime()) + "Z;;1;beep!");
  }

}
