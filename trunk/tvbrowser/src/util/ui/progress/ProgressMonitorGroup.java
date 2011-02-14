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
package util.ui.progress;

import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import devplugin.ProgressMonitor;

/**
 *
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProgressMonitorGroup {

  private static final Logger mLog = Logger.getLogger(ProgressMonitorGroup.class.getName());

  private int MAXIMUM = 1000;

  private ProgressMonitor mDelegate;
  private int mTotalWeight;
  private int mUsedWeight;

  private int mCurrentMonitorWeight;
  private int mCurrentMonitorMaximum;


  public ProgressMonitorGroup(ProgressMonitor delegate, int totalWeight) {
    mDelegate = delegate;
    mTotalWeight = totalWeight;

    mDelegate.setMaximum(MAXIMUM);
  }


  public ProgressMonitorGroup(JProgressBar progressBar, JLabel label, int totalWeight) {
    this (new ProgressBarProgressMonitor(progressBar, label), totalWeight);
  }


  public ProgressMonitor getNextProgressMonitor(int weight) {
    // Add the weight of the last monitor
    mUsedWeight += mCurrentMonitorWeight;

    // Remember the weight of the current monitor
    mCurrentMonitorWeight = weight;

    return new ProgressMonitor() {
      public void setMaximum(int maximum) {
        mCurrentMonitorMaximum = maximum;
      }

      public void setValue(int value) {
        setCurrentMonitorValue(value);
      }

      public void setMessage(String msg) {
        mDelegate.setMessage(msg);
      }
    };
  }


  private void setCurrentMonitorValue(int value) {
    // Ensure that the value is in the range
    if ((value < 0) || (value > mCurrentMonitorMaximum)) {
      mLog.severe("Progress value " + value
        + " is out of range [0.." + mCurrentMonitorMaximum + "]");
      return;
    }

    int groupStartValue = MAXIMUM * mUsedWeight / mTotalWeight;
    int groupValue = MAXIMUM * mCurrentMonitorWeight
                   * value / mCurrentMonitorMaximum / mTotalWeight;

    int result = groupStartValue + groupValue;
    if (result >= 0) {
      mDelegate.setValue(result);
    }
  }

}
