/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
package tvbrowser.ui.programtable.background;

import java.awt.Image;

import tvbrowser.core.Settings;
import util.ui.ImageUtilities;
import devplugin.Program;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TimeOfDayBackPainter extends AbstractCellBasedBackPainter {
  
  private static final int mEarlyTime = 6 * 60;
  private static final int mMiddayTime    = 12 * 60;
  private static final int mAfternoonTime = 16 * 60;
  private static final int mEveningTime   = 20 * 60;

  private Image mBackgroundImageEdge, mBackgroundImageEarly,
    mBackgroundImageMidday, mBackgroundImageAfternoon, mBackgroundImageEvening;


  public TimeOfDayBackPainter() {
    mBackgroundImageEdge = ImageUtilities
        .createImageAsynchronous(Settings.propTimeOfDayBackgroundEdge
            .getString());
    mBackgroundImageEarly = ImageUtilities
        .createImageAsynchronous(Settings.propTimeOfDayBackgroundEarly
            .getString());
    mBackgroundImageMidday = ImageUtilities
        .createImageAsynchronous(Settings.propTimeOfDayBackgroundMidday
            .getString());
    mBackgroundImageAfternoon = ImageUtilities
        .createImageAsynchronous(Settings.propTimeOfDayBackgroundAfternoon
            .getString());
    mBackgroundImageEvening = ImageUtilities
        .createImageAsynchronous(Settings.propTimeOfDayBackgroundEvening
            .getString());
  }

  /**
   * Gets the background image for the outer areas, where no programs are.
   * 
   * @return The background image for the outer areas
   */
  protected Image getOuterBackgroundImage() {
    return mBackgroundImageEdge;
  }
  
  
  /**
   * Gets the background image for the given program.
   * 
   * @param prog The program.
   * @return The background image for the given program.
   */
  protected Image getBackgroundImageFor(Program prog) {
    int startTime = prog.getHours() * 60 + prog.getMinutes();
    if (startTime >= mEveningTime) {
      return mBackgroundImageEvening;
    } else if (startTime >= mAfternoonTime) {
      return mBackgroundImageAfternoon;
    } else if (startTime >= mMiddayTime) {
      return mBackgroundImageMidday;
    } else if (startTime >= mEarlyTime) {
      return mBackgroundImageEarly;
    } else {
      // It is before early -> it's still evening
      return mBackgroundImageEvening;
    }
  }

}
