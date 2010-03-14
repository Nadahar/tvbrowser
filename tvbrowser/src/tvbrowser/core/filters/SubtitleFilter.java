/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.core.filters;

import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * This Filter filters Movies that have a Subtitle for Handicaped Persons or are
 * Original with Subtitle or sign language
 */
public class SubtitleFilter implements ProgramFilter {
  /**
   * Localizer
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SubtitleFilter.class);

  /**
   * Accept only programs with subtitle or sign language
   *
   * @param prog Program to check
   * @return true if prog is subtitled
   */
  public boolean accept(devplugin.Program prog) {
    int info = prog.getInfo();

    return info >= 1 && (bitSet(info, Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED) ||
            bitSet(info, Program.INFO_ORIGINAL_WITH_SUBTITLE) ||
            bitSet(info, Program.INFO_SIGN_LANGUAGE));
  }

  /**
   * Checks if bits are set
   *
   * @param num     check in here
   * @param pattern this pattern
   * @return Pattern set?
   */
  private boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }

  public String getName() {
    return toString();
  }

  /**
   * Name of Filter
   */
  public String toString() {
    return mLocalizer.msg("Subtitled", "Subtitled");
  }

  public boolean equals(Object o) {
    return o instanceof ProgramFilter && getClass().equals(o.getClass())
        && getName().equals(((ProgramFilter) o).getName());
  }
}