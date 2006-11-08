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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 MÃ¤r 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.elgatodriver;

import captureplugin.CapturePlugin;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import util.misc.AppleScriptRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This Class represents the Connection to the Elgato EyeTV.
 * 
 * @author bodum
 */
public class ElgatoConnection {
    /** AppleScript Runner */
    private AppleScriptRunner mAppleScript = new AppleScriptRunner();

    /** Script to get the Channelllist */
    private static final String CHANNELLIST = "set chList to {}\n"
            + "tell application \"EyeTV\"\n" + "  repeat with ch in channels\n"
            + "    set end of chList to channel number of contents of ch\n"
            + "    set end of chList to name of contents of ch\n"
            + "  end repeat\n" + "end tell\n"
            + "set text item delimiters to \"¥\"\n"
            + "set outString to chList as text\n" + "outString";

    /** Script for switching of Channels */
    private static final String SWITCHCHANNEL = "tell application \"EyeTV\"\n"
            + "set current channel to \"{0}\"\n" + "end tell";

    /** Script for creation of recordings */
    private static final String CREATERECORDING = "on stringToList from theString for myDelimiters\n"
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
            + "\n"
            + "on getdateForISOdate(theISODate, theISOTime)\n"
            + "  local myDate\n"
            + "  -- converts an ISO format (YYYY-MM-DD) and time to a date object\n"
            + "  set monthConstants to {January, February, March, April, May, June, July, August, September, October, November, December} \n"
            + " \n"
            + "  set theISODate to (stringToList from (theISODate) for \"-\")\n"
            + "  \n"
            + "  set myDate to date theISOTime\n"
            + "  \n"
            + "  tell theISODate\n"
            + "    set year of myDate to item 1\n"
            + "    set month of myDate to item (item 2) of monthConstants\n"
            + "    set day of myDate to item 3\n"
            + "  end tell\n"
            + " \n"
            + "  return myDate\n"
            + "end getdateForISOdate\n"
            + "\n"
            + "set dateob to getdateForISOdate(\"{0}\", \"{1}\")\n"
            + "\n"
            + "tell application \"EyeTV\"\n"
            + "  make new program with properties {start time:dateob, duration:{2}, title:\"{3}\", channel number:{4}, description : \"{5}\"}\n"
            + "end tell";

    /** List all Recordings */
    private final String LISTRECORDINGS = "set recList to {}\n"
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
            + "end tell\n" + "\n" + "set text item delimiters to \"¥\"\n"
            + "set outString to reclist as text\n" + "outString";

    /** Remove a specific Recording */
    private final String REMOVERECORDING = "tell application \"EyeTV\"\n"
            + "  repeat with transmission in programs\n"
            + "    if unique ID of transmission is {0} then\n"
            + "      delete transmission\n" + "    end if\n" + "  end repeat\n"
            + "end tell";

    /** Map with Program - Elgato ID Mappings */
    private HashMap mProgramMapping = new HashMap();

    /**
     * Get the List of all available Channels
     * 
     * @return All available Channels
     */
    public ElgatoChannel[] getAvailableChannels() {
        ArrayList list = new ArrayList();

        String res = null;
        try {
            res = mAppleScript.executeScript(CHANNELLIST);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (res == null) {
            return new ElgatoChannel[0];
        }

        String[] result = res.split("¥");

        for (int i = 0; i < result.length; i += 2) {
            ElgatoChannel channel = new ElgatoChannel(Integer
                    .parseInt(result[i]), result[i + 1]);
            list.add(channel);
        }

        return (ElgatoChannel[]) list.toArray(new ElgatoChannel[list.size()]);
    }

    /**
     * @return List of all current Recordings
     */
    public Program[] getAllRecordings(ElgatoConfig conf) {
        ArrayList programs = new ArrayList();

        mProgramMapping = new HashMap();

        String res = null;
        try {
            res = mAppleScript.executeScript(LISTRECORDINGS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (res == null) {
            return new Program[0];
        }

        String[] result = res.split("¥");

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

                Channel chan = conf.getChannelForElgatoId(channel);

                if (chan != null) {
                    Iterator it = CapturePlugin.getPluginManager()
                            .getChannelDayProgram(date, chan);

                    if (it != null)
                        while (it.hasNext()) {
                            Program prog = (Program) it.next();

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

        return (Program[]) programs.toArray(new Program[programs.size()]);
    }

    /**
     * Record Program
     * 
     * @param conf Config
     * @param prg Program to record
     * @param length Length of Program
     * @return true if successfull
     */
    public boolean addToRecording(ElgatoConfig conf, Program prg, int length) {
        String date = prg.getDate().getYear() + "-" + prg.getDate().getMonth()
                + "-" + prg.getDate().getDayOfMonth();

        String time = prg.getHours() + ":" + prg.getMinutes();

        String call = CREATERECORDING.replaceAll("\\{0\\}", date);
        call = call.replaceAll("\\{1\\}", time);
        call = call.replaceAll("\\{2\\}", Integer.toString(length));
        call = call.replaceAll("\\{3\\}", prg.getTitle());
        call = call.replaceAll("\\{4\\}", Integer.toString(conf
                .getElgatoChannel(prg.getChannel()).getNumber()));

        if (prg.getShortInfo() != null)
            call = call.replaceAll("\\{5\\}", prg.getShortInfo().replaceAll("\"",
                "\\\\\\\\\"").replace('\n', ' '));

        String res = null;
        try {
            res = mAppleScript.executeScript(call);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (res == null)
            return false;

        if (res.startsWith("program id"))
            return true;

        return false;
    }

    /**
     * Remove Recording
     * 
     * @param prg Remove recording of this Program
     */
    public void removeRecording(Program prg) {
        String id = (String) mProgramMapping.get(prg);
        try {
            mAppleScript.executeScript(REMOVERECORDING
                    .replaceAll("\\{0\\}", id));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switch to Channel of Program
     * 
     * @param conf Config to use
     * @param prg Switch to Channel of Program
     */
    public void switchToChannel(ElgatoConfig conf, Program prg) {
        try {
            mAppleScript.executeScript(SWITCHCHANNEL.replaceAll("\\{0\\}",
                    Integer.toString(conf.getElgatoChannel(prg.getChannel())
                            .getNumber())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}