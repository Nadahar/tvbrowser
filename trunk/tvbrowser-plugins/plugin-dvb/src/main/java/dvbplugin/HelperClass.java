/*
 * HelperClass.java
 * Copyright (C) 2006 Probum
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
 *     $Date: $
 *   $Author: $
 * $Revision: $
 */

package dvbplugin;

import java.awt.Component;

import javax.swing.JOptionPane;

import util.ui.Localizer;
import devplugin.Program;

/**
 * @author Probum
 */
public final class HelperClass {

  /** Translator */
  private static final Localizer localizer = Localizer.getLocalizerFor(HelperClass.class);


  /** Creates a new instance of HelperClass */
  private HelperClass() {
  // no, we do not want to have instances of this class
  }


  /**
   * creates a Confirm Dialog
   */
  static int confirm(String msg, Component parent) {
    return JOptionPane.showConfirmDialog(parent, msg, localizer.msg("question_title", "Question"),
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
  }


  /**
   * creates a Confirm Dialog
   */
  static int confirm(String msg) {
    return confirm(msg, null);
  }


  /**
   * Creates a Error Dialog
   */
  static void error(String msg, Component parent) {
    JOptionPane.showMessageDialog(parent, msg, localizer.msg("error_title", "Error"),
            JOptionPane.ERROR_MESSAGE);
  }


  /**
   * Creates a Error Dialog
   */
  public static void error(String msg) {
    error(msg, null);
  }


  /**
   * Calculates from given <code>program</code> and <code>offset</code> the
   * end time including the offset and returns it formatted as string of format
   * <code>hh:mm:ss</code>
   *
   * @param program the program to calculate the end time for
   * @param offset the offset to add to the end time
   * @return String end time including offset (if any) format: hh:mm:ss
   */
  public static String calcEndTime(Program program, int offset) {
    int start = program.getStartTime();
    int length = program.getLength();
    int ende = start + length + offset;
    if (ende > 1440) {
      // program stops after midnight subtract a day
      ende -= 1440;
    }

    int h = ende / 60;
    int m = ende % 60;

    if (m >= 10) { return String.valueOf(h) + ':' + m + ":00"; }

    return h + ":0" + m + ":00";
  }


  /**
   * Calculates start time from given start time and offset and returns it
   * formatted as string of format <code>hh:mm:ss</code>
   *
   * @param program the program to calculate the start time for
   * @param offset the offset to subtract from the start time
   * @return String starttime including offset (if any) format: hh:mm:ss
   */
  public static String calcStartTime(Program program, int offset) {
    int start = program.getStartTime();
    start = start - offset;
    if (start < 0) {
      // program starts after midnight so add a day
      start += 1440;
    }
    int h = start / 60;
    int m = start % 60;

    if (m >= 10) { return String.valueOf(h) + ':' + m + ":00"; }

    return h + ":0" + m + ":00";
  }


  /**
   * Fomats the date given by the Program
   */
  public static String date(Program mProgram) {
    String s = mProgram.getDate().getDateString();
    String jahr = s.substring(0, 4);
    String monat = s.substring(4, 6);
    String tag = s.substring(6, 8);
    return tag + '.' + monat + '.' + jahr;
  }
}
