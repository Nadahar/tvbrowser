/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.elgatodriver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
 * This Class represents the Connection to the Elgato EyeTV.
 *
 * @author bodum
 */
public class ElgatoConnection implements SimpleConnectionIf {
    private static final String SEPARATOR = "-TRENNER-";

    /**
     * Logger
     */
    private static final Logger mLog = Logger.getLogger(ElgatoConnection.class.getName());

    /** AppleScript Runner */
    private final AppleScriptRunner mAppleScript = new AppleScriptRunner();

    /** Script to get the Channelllist */
    private static final String CHANNEL_LIST = "set chList to {}\n"
            + "tell application \"EyeTV\"\n" + "  repeat with ch in channels\n"
            + "    set end of chList to channel number of contents of ch\n"
            + "    set end of chList to name of contents of ch\n"
            + "  end repeat\n" + "end tell\n"
            + "set text item delimiters to \"" + SEPARATOR + "\"\n"
            + "set outString to chList as text\n" + "outString";

    /** Script for switching of Channels */
    private static final String SWITCH_CHANNEL = "tell application \"EyeTV\"\n"
            + "set current channel to \"{0}\"\n" + "end tell";

    /** Script for creation of recordings */
    private static final String CREATE_RECORDING = "on stringToList from theString for myDelimiters\n"
            + "  tell AppleScript\n"
            + "    set theSavedDelimiters to AppleScript's text item delimiters\n"
            + "    set text item delimiters to myDelimiters\n"
            + "    \n"
            + "    set outList to text items of theString\n"
            + "    set text item delimiters to theSavedDelimiters\n"
            + "    \n"
            + "    return outList\n"
            + "  end tell\n"
            + "end stringToList\n"
            + "\n"
            + "\n" +
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
            "\n"
            + "set dateob to getdateForISOdate(\"{0}\", \"{1}\")\n"
            + "\n"
            + "tell application \"EyeTV\"\n"
            + "  make new program with properties {start time:dateob, duration:{2}, title:\"{3}\", channel number:{4}, description: \"{5}\"}\n"
            + "end tell";

    /** List all Recordings */
    private final static String LIST_RECORDINGS = "set recList to {}\n"
            + "\n"
            + "script x\n"
            + "  on getIsoDate(dateObj)\n"
            + "    tell dateObj\n"
            + "     return (its year as string) & \"-\" & text 2 thru 3 of ((100 + (its month as integer)) as string) & \"-\" & text 2 thru 3 of ((100 + (its day)) as string)\n"
            + "    end tell\n"
            + "  end getIsoDate\n"
            + "  \n"
            + "  on getIsoTime(dateObj)\n"
            + "    tell dateObj\n"
            + "      return text 2 thru 3 of ((100 + ((its time) div hours as integer)) as string) & \":\" & text 2 thru 3 of ((100 + ((its time) mod hours div minutes)) as string)\n"
            + "    end tell\n" + "  end getIsoTime\n" + "end script\n" + "\n"
            + "tell application \"EyeTV\"\n"
            + "  repeat with prog in programs\n"
            + "    set end of recList to channel number of prog\n"
            + "    set end of recList to x's getIsoDate(start time of prog)\n"
            + "    set end of recList to x's getIsoTime(start time of prog)\n"
            + "    set end of recList to duration of prog\n"
            + "    set end of recList to unique ID of prog\n"
            + "    set end of recList to title of prog\n" + "  end repeat\n"
            + "end tell\n" + "\n" + "set text item delimiters to \"" + SEPARATOR + "\"\n"
            + "set outString to reclist as text\n" + "outString";

    /** Remove a specific Recording */
    private final static String REMOVE_RECORDING = "tell application \"EyeTV\"\n"
            + "  repeat with transmission in programs\n"
            + "    if unique ID of transmission is {0} then\n"
            + "      delete transmission\n" + "    end if\n" + "  end repeat\n"
            + "end tell";

    /** Map with Program - Elgato ID Mappings */
    private HashMap<Program, String> mProgramMapping = new HashMap<Program, String>();

    /**
     * Get the List of all available Channels
     *
     * @return All available Channels, <code>null</code> if error while loading
     */
    public SimpleChannel[] getAvailableChannels() {
        ArrayList<SimpleChannel> lists = new ArrayList<SimpleChannel>();

        String res = null;
        try {
            res = mAppleScript.executeScript(CHANNEL_LIST);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (res == null) {
            return new SimpleChannel[0];
        }

        String[] result = res.split(SEPARATOR);

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

    /**
     * @return List of all current Recordings
     * @param conf Config
     */
    public Program[] getAllRecordings(SimpleConfig conf) {
        ArrayList<Program> programs = new ArrayList<Program>();

        mProgramMapping = new HashMap<Program, String>();

        String res = null;
        try {
            res = mAppleScript.executeScript(LIST_RECORDINGS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (StringUtils.isBlank(res)) {
            return new Program[0];
        }

        String[] result = res.split(SEPARATOR);

        for (int i = 0; i < result.length; i += 6) {
            try {
                int channel = Integer.parseInt(result[i]);
                String id = result[i + 4].replace(',', '.');
                String title = result[i + 5].trim();

                String[] dateStr = result[i + 1].split("-");

                Date date = new Date(Integer.parseInt(dateStr[0]), Integer
                        .parseInt(dateStr[1]), Integer.parseInt(dateStr[2]));

                String[] hourStr = result[i + 2].split(":");

                int hour = Integer.parseInt(hourStr[0]);
                int min = Integer.parseInt(hourStr[1]);

                Channel chan = conf.getChannelForExternalId(channel);

                if (chan != null) {
                  for (Iterator<Program> it = CapturePlugin.getPluginManager().getChannelDayProgram(date, chan); it.hasNext();) {
                    Program prog = it.next();

                    if ((prog.getHours() == hour)
                            && (prog.getMinutes() == min)
                            && (prog.getTitle().trim().toLowerCase()
                                    .equals(title.trim().toLowerCase()))) {
                        programs.add(prog);
                        mProgramMapping.put(prog, id);
                    }
                  }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return programs.toArray(new Program[programs.size()]);
    }

    private String replaceParams(final String command, String ... arguments) {
      String result = command;
      int count = 0;
      for (String argument : arguments) {
        result = result.replaceAll("\\{" + String.valueOf(count) + "\\}", mAppleScript.formatTextAsParam(argument));
        count++;
      }
      return result;
    }

    /**
     * Record Program
     *
     * @param conf Config
     * @param prg Program to record
     * @return true if successful
     */
    public boolean addToRecording(SimpleConfig conf, ProgramTime prg) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(prg.getStart());
        String time = new SimpleDateFormat("HH:mm").format(prg.getStart());
        String text = "";
        if (prg.getProgram().getShortInfo() != null) {
          text = prg.getProgram().getShortInfo();
        }

        String call = replaceParams(CREATE_RECORDING,
            date,
            time,
            String.valueOf(prg.getLength()*60),
            prg.getTitle(),
            String.valueOf(((SimpleChannel)conf
                .getExternalChannel(prg.getProgram().getChannel())).getNumber()),
            text);

        String res = null;
        try {
            res = mAppleScript.executeScript(call);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res != null && res.startsWith("program id");
   }

    /**
     * Remove Recording
     *
     * @param conf
     * @param prg Remove recording of this Program
     */
    public void removeRecording(SimpleConfig conf, Program prg) {
        String id = mProgramMapping.get(prg);
        try {
            mAppleScript.executeScript(replaceParams(REMOVE_RECORDING, id));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switch to Channel of Program
     *
     * @param conf Config to use
     * @param channel Switch to Channel
     */
    public void switchToChannel(SimpleConfig conf, Channel channel) {
        try {
            mAppleScript.executeScript(replaceParams(SWITCH_CHANNEL,
                    String.valueOf(((SimpleChannel)conf.getExternalChannel(channel))
                            .getNumber())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}