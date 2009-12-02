package calendarexportplugin.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;

import util.io.stream.PrintStreamProcessor;
import util.io.stream.StreamUtilities;

public class ICalFile extends AbstractCalFile {

  protected void print(File intothis, final PrintStreamProcessor processor)
      throws IOException {
    StreamUtilities.printStream(intothis, true, "UTF8", processor);
  }

  protected void printCreated(PrintStream out, String created, int sequence) {
    out.println("CREATED:" + created);
  }

  protected void printVersion(PrintStream out) {
    out.println("VERSION:2.0");
  }

  protected String opaque() {
    return "OPAQUE";
  }

  protected String transparent() {
    return "TRANSPARENT";
  }

  protected void printAlarm(final int minutes, PrintStream out,
      Calendar c) {
    out.println("BEGIN:VALARM");
    out.println("DESCRIPTION:");
    out.println("ACTION:DISPLAY");
    out.println("TRIGGER;VALUE=DURATION:-PT"
        + minutes
        + "M");
    out.println("END:VALARM");
  }

}
