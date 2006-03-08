package util.program;

import util.io.IOUtilities;
import devplugin.Date;
import devplugin.Program;

/**
 * Provides utilities for program stuff.
 * 
 * @author René Mach
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
}
