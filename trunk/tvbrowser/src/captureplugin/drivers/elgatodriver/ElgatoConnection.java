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

import java.awt.Window;
import java.util.ArrayList;

import devplugin.Program;

/**
 * This Class represents the Connection to the Elgato EyeTV.
 * 
 * @author bodum
 */
public class ElgatoConnection {
    /** AppleScript Runner */
    private AppleScriptRunner mAppleScript = new AppleScriptRunner();
    
    /** Script to get the Channelllist */
    private static final String CHANNELLIST = "set chList to {}\n"+
    "tell application \"EyeTV\"\n"+
    "  repeat with ch in channels\n"+
    "    set end of chList to channel number of contents of ch\n"+
    "    set end of chList to name of contents of ch\n"+
    "  end repeat\n"+
    "end tell\n" +
    "set text item delimiters to \"¥\"\n" +
    "set outString to chList as text\n"+
    "outString";
    
    /**
     * Get the List of all available Channels
     * @return All available Channels
     */
    public ElgatoChannel[] getAvailableChannels() {
        ArrayList list = new ArrayList();
        
        String[] result = mAppleScript.executeScript(CHANNELLIST).split("¥");
        
        for (int i =0;i<result.length;i+=2) {
            ElgatoChannel channel = new ElgatoChannel(Integer.parseInt(result[i]), result[i+1]);
            list.add(channel);
        }
        
        return (ElgatoChannel[]) list.toArray(new ElgatoChannel[list.size()]);
    }
  
    /**
     * @return List of all current Recordings
     */
    public Program[] getAllRecordings() {
        System.out.println("Get all Recordings");
        return new Program[0];
    }
    
    /**
     * Record Program
     * @param parent Parent-window
     * @param prg Program to record
     * @return true if successfull
     */
    public boolean addToRecording(Window parent, Program prg) {
        System.out.println("Add to Recording");
        return false;
    }

    /**
     * Remove Recording
     * @param prg Remove recording of this Program
     */
    public void removeRecording(Program prg) {
        System.out.println("Remove Recording");
    }
    
    /**
     * Switch to Channel of Program
     * @param prg Switch to Channel of Program
     */
    public void switchToChannel(Program prg) {
       System.out.println("Switch to Channel"); 
    }
}