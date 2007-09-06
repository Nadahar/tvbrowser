package captureplugin.drivers.thetubedriver;

import captureplugin.drivers.elgatodriver.ElgatoConnection;
import captureplugin.drivers.simpledevice.SimpleChannel;
import captureplugin.drivers.simpledevice.SimpleConfig;
import captureplugin.drivers.simpledevice.SimpleConnectionIf;
import captureplugin.CapturePlugin;
import devplugin.Channel;
import devplugin.Program;
import devplugin.Date;
import util.misc.AppleScriptRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Applescripts for The Tube
 */
public class TheTubeConnection implements SimpleConnectionIf {
  /**
    * Logger
    */
   private static Logger mLog = Logger.getLogger(TheTubeConnection.class.getName());


  /**
   * AppleScript Runner
   */
  private AppleScriptRunner mAppleScript = new AppleScriptRunner();

  private final String CHANNELLIST = "set chList to {}\n" +
          "tell application \"TheTube\"\n" +
          "\trepeat with ch in channels of active device\n" +
          "\t\tset end of chList to unique id of contents of ch\n" +
          "\t\tset end of chList to name of contents of ch\n" +
          "\tend repeat\n" +
          "end tell\n" +
          "\n" +
          "set text item delimiters to \"¥\"\n" +
          "set outString to chList as text\n" +
          "outString";

  private final String SWITCHCHANNEL = "tell application \"TheTube\"\n" +
          "\trepeat with ch in channels of active device\n" +
          "\t\tif unique id of ch is {0} then\n" +
          "\t\t\tselect ch\n" +
          "\t\tend if\n" +
          "\tend repeat\n" +
          "end tell";

  private final String LISTRECORDINGS = "script x\n" +
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
          "set text item delimiters to \"¥\"\n" +
          "set outString to chList as text\n" +
          "outString";

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

    String[] result = res.split("¥");

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

    if (res == null || res.trim().length() == 0) {
        return new Program[0];
    }

    String[] result = res.split("¥");

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
                Iterator<Program> it = CapturePlugin.getPluginManager()
                        .getChannelDayProgram(date, chan);

                if (it != null)
                    while (it.hasNext()) {
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

  public boolean addToRecording(SimpleConfig conf, Program prg, int length) {


    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void removeRecording(Program prg) {
    //To change body of implemented methods use File | Settings | File Templates.
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
