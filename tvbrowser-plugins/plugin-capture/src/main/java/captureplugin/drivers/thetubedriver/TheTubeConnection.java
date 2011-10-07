package captureplugin.drivers.thetubedriver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import util.misc.AppleScriptRunner;
import captureplugin.CapturePlugin;
import captureplugin.drivers.simpledevice.SimpleChannel;
import captureplugin.drivers.simpledevice.SimpleConfig;
import captureplugin.drivers.simpledevice.SimpleConnectionIf;
import captureplugin.drivers.utils.ProgramTime;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;

/**
 * Applescripts for The Tube
 */
public class TheTubeConnection implements SimpleConnectionIf {
  /**
    * Logger
    */
   private static final Logger mLog = Logger.getLogger(TheTubeConnection.class.getName());


  /**
   * AppleScript Runner
   */
  private final AppleScriptRunner mAppleScript = new AppleScriptRunner();

  private final static String CHANNELLIST = "set chList to {}\n"
      +
          "tell application \"TheTube\"\n" +
          "\trepeat with ch in channels\n" +
          "\t\tset end of chList to unique id of contents of ch\n" +
          "\t\tset end of chList to name of contents of ch\n" +
          "\tend repeat\n" +
          "end tell\n" +
          "\n" +
          "set text item delimiters to \"-TRENNER-\"\n" +
          "set outString to chList as text\n" +
          "outString";

  private final static String SWITCHCHANNEL = "tell application \"TheTube\"\n"
      +
          "\trepeat with ch in channels\n" +
          "\t\tif unique id of ch is {0} then\n" +
          "\t\t\tselect ch\n" +
          "\t\tend if\n" +
          "\tend repeat\n" +
          "end tell";

  private final static String LISTRECORDINGS = "script x\n"
      +
          "\ton getIsoDate(dateObj)\n" +
          "\t\ttell dateObj\n" +
          "\t\t\treturn (its year as string) & \"-\" & text 2 thru 3 of ((100 + (its month as integer)) as string) & \"-\" & text 2 thru 3 of ((100 + (its day)) as string)\n" +
          "\t\tend tell\n" +
          "\tend getIsoDate\n" +
          "\t\n" +
          "\ton getIsoTime(dateObj)\n" +
          "\t\ttell dateObj\n" +
          "\t\t\treturn text 2 thru 3 of ((100 + ((its time) div hours as integer)) as string) & \":\" & text 2 thru 3 of ((100 + ((its time) mod hours div minutes)) as string)\n" +
          "\t\tend tell\n" +
          "\tend getIsoTime\n" +
          "end script\n" +
          "\n" +
          "set chList to {}\n" +
          "tell application \"TheTube\"\n" +
          "\trepeat with r in scheduled recordings\n" +
          "\t\tset end of chList to channel id of contents of r\n" +
          "\t\tset end of chList to x's getIsoDate(rollIn date of contents of r)\n" +
          "\t\tset end of chList to x's getIsoTime(rollIn date of contents of r)\n" +
          "\t\tset end of chList to rollIn interval of contents of r\n" +
          "\t\tset end of chList to x's getIsoDate(rollOut date of contents of r)\n" +
          "\t\tset end of chList to x's getIsoTime(rollOut date of contents of r)\n" +
          "\t\tset end of chList to rollOut interval of contents of r\n" +
          "\tend repeat\n" +
          "end tell\n" +
          "set text item delimiters to \"-TRENNER-\"\n" +
          "set outString to chList as text\n" +
          "outString";

  private final static String CREATERECORDING = "on stringToList from theString for myDelimiters\n"
      +
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
          "\n" +
          "set a to getDateForISOdate(\"{1}\", \"{2}\")\n" +
          "set b to getDateForISOdate(\"{3}\", \"{4}\")\n" +
          "\n" +
          "tell application \"TheTube\"\n" +
          "\tshow scheduled recordings view\n" +
          "\tmake new scheduled recording with properties {name:\"{5}\", description:\"{6}\", rollIn interval:120.0, rollOut interval:300.0, startDate:a, endDate:b, channel id:{7}}\n" +
          "end tell";

  private final static String DELETERECORDING = "on stringToList from theString for myDelimiters\n"
      +
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
      "\tset monthConstants to {January, February, March, April, May, June, July, August, September, October, November, December}\n" +
      "\t\n" +
      "\tset theISODate to (stringToList from (theISODate) for \"-\")\n" +
      "\t\n" +
      "\tset myDate to date theISOTime\n" +
      "\t\n" +
      "\ttell theISODate\n" +
      "\t\tset year of myDate to item 1\n" +
      "\t\tset month of myDate to item (item 2) of monthConstants\n" +
      "\t\tset day of myDate to item 3\n" +
      "\tend tell\n" +
      "\t\n" +
      "\treturn myDate\n" +
      "end getDateForISOdate\n" +
      "\n" +
      "set mystartdate to getDateForISOdate(\"{1}\", \"{2}\")\n" +
      "\n" +
      "tell application \"TheTube\"\n" +
      "\tset counter to 0\n" +
      "\trepeat with r in scheduled recordings\n" +
      "\t\tset counter to counter + 1\n" +
      "\t\tif channel id of contents of r is {3} and startDate of contents of r is mystartdate then\n" +
      "\t\t\tdelete scheduled recording counter\n" +
      "\t\tend if\n" +
      "\tend repeat\n" +
      "end tell";


  public SimpleChannel[] getAvailableChannels() {
    ArrayList<SimpleChannel> lists = new ArrayList<SimpleChannel>();

    String res = null;
    try {
      res = mAppleScript.executeScript(CHANNELLIST);
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (res == null) {
      return new SimpleChannel[0];
    }

    String[] result = res.split("-TRENNER-");

    for (int i = 0; i < result.length; i += 2) {
      try {
        SimpleChannel channel = new SimpleChannel(Integer
                .parseInt(result[i]), result[i + 1]);
        lists.add(channel);
      } catch (NumberFormatException e) {
        mLog.warning("Could not parse channel data!");
        return null;
      }
    }

    return lists.toArray(new SimpleChannel[lists.size()]);
  }

  public Program[] getAllRecordings(SimpleConfig conf) {
    ArrayList<Program> programs = new ArrayList<Program>();

    String res = null;
    try {
        res = mAppleScript.executeScript(LISTRECORDINGS);
    } catch (IOException e) {
        e.printStackTrace();
    }

    if (StringUtils.isBlank(res)) {
        return new Program[0];
    }

    String[] result = res.split("-TRENNER-");

    for (int i = 0; i < result.length; i += 7) {
        try {
            int channel = Integer.parseInt(result[i]);

            String[] dateStr = result[i + 1].split("-");

            Date date = new Date(Integer.parseInt(dateStr[0]), Integer
                    .parseInt(dateStr[1]), Integer.parseInt(dateStr[2]));

            String[] hourStr = result[i + 2].split(":");

            int hour = Integer.parseInt(hourStr[0]);
            int min = Integer.parseInt(hourStr[1]);

            int offset = Integer.parseInt(result[i + 3].split(",")[0]) / 60;

            min += offset;

            if (min >= 60) {
              hour = hour + min / 60;
              min = min - (60 * (min / 60));
            }

            Channel chan = conf.getChannelForExternalId(channel);

            if (chan != null) {
              for (Iterator<Program> it = CapturePlugin.getPluginManager().getChannelDayProgram(date, chan); it.hasNext();) {
                Program prog = it.next();

                if ((prog.getHours() == hour) && (prog.getMinutes() == min)) {
                  programs.add(prog);
                }
              }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    return programs.toArray(new Program[programs.size()]);
  }

  public boolean addToRecording(SimpleConfig conf, ProgramTime prg) {
      SimpleDateFormat dateformater = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat timeformater = new SimpleDateFormat("HH:mm");

      String call = CREATERECORDING.replaceAll("\\{1\\}", dateformater.format(prg.getStart()));
      call = call.replaceAll("\\{2\\}", timeformater.format(prg.getStart()));
      call = call.replaceAll("\\{3\\}", dateformater.format(prg.getEnd()));
      call = call.replaceAll("\\{4\\}", timeformater.format(prg.getEnd()));

      call = call.replaceAll("\\{5\\}", mAppleScript.formatTextAsParam(prg.getProgram().getTitle()));

      if (prg.getProgram().getShortInfo() != null) {
          call = call.replaceAll("\\{6\\}", mAppleScript.formatTextAsParam(prg.getProgram().getShortInfo()));
      } else {
          call = call.replaceAll("\\{6\\}", "");
      }

      call = call.replaceAll("\\{7\\}", Integer.toString(((SimpleChannel)conf.getExternalChannel(prg.getProgram().getChannel())).getNumber()));

      String res = null;

      try {
          res = mAppleScript.executeScript(call);
      } catch (IOException e) {
          e.printStackTrace();
      }

      return res != null;
  }

  public void removeRecording(SimpleConfig conf, Program prg) {
    SimpleDateFormat dateformater = new SimpleDateFormat("yyyy-MM-dd");

    String call = DELETERECORDING.replaceAll("\\{1\\}", dateformater.format(prg.getDate().getCalendar().getTime()));

    call = call.replaceAll("\\{2\\}", prg.getHours() + ":" + prg.getMinutes());
    call = call.replaceAll("\\{3\\}", Integer.toString(((SimpleChannel)conf.getExternalChannel(prg.getChannel())).getNumber()));

    try {
        mAppleScript.executeScript(call);
    } catch (IOException e) {
        e.printStackTrace();
    }

  }

  public void switchToChannel(SimpleConfig conf, Channel channel) {
    try {
        mAppleScript.executeScript(SWITCHCHANNEL.replaceAll("\\{0\\}",
                Integer.toString(((SimpleChannel)conf.getExternalChannel(channel))
                        .getNumber())));
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}
