package util.program;

import util.io.IOUtilities;
import devplugin.Date;
import devplugin.Program;

import java.util.Comparator;

import tvbrowser.core.ChannelList;

/**
 * Provides utilities for program stuff.
 * 
 * @author Ren� Mach
 *
 */
public class ProgramUtilities {

  /**
   * Helper method to check if a program runs.
   * 
   * @param p
   *          The program to check.
   * @return True if the program runs.
   */
  public static boolean isOnAir(Program p) {
    int time = IOUtilities.getMinutesAfterMidnight();

    if (Date.getCurrentDate().addDays(-1).compareTo(p.getDate()) == 0)
      time += 24 * 60;
    if (Date.getCurrentDate().compareTo(p.getDate()) < 0)
      return false;

    if (p.getStartTime() <= time && (p.getStartTime() + p.getLength()) > time)
      return true;
    return false;
  }

  public static Comparator<Program> getProgramComparator() {
    return sProgramComparator;
  }

  /**
   * Comparator to sort programs by date, time and channel 
   */
  private static Comparator<Program> sProgramComparator = new Comparator<Program>(){
    public int compare(Program p1, Program p2) {
      int res=p1.getDate().compareTo(p2.getDate());
      if (res!=0) return res;

      int minThis=p1.getHours()*60+p1.getMinutes();
      int minOther=p2.getHours()*60+p2.getMinutes();

      if (minThis<minOther) {
        return -1;
      }else if (minThis>minOther) {
        return 1;
      }

      int pos1 = ChannelList.getPos(p1.getChannel());
      int pos2 = ChannelList.getPos(p2.getChannel());
      if (pos1 < pos2) {
        return -1;
      }
      else if (pos1 > pos2) {
        return 1;
      }

      return 0;

    }
  };

}
