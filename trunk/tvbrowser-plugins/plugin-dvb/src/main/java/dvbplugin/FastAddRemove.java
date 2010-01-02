/*
 * FastRemove.java
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

import java.util.List;

import javax.swing.JOptionPane;

import util.ui.Localizer;
import devplugin.Program;
import dvbplugin.Settings.TvbDvbVChannel;
import dvbplugin.dvbviewer.ProcessHandler;
import dvbplugin.dvbviewer.ScheduledRecording;
import dvbplugin.dvbviewer.DvbViewerTimers;

/**
 * @author Probum
 */
final class FastAddRemove {
  private static final Localizer localizer = Localizer.getLocalizerFor(FastAddRemove.class);


  /**
   * Stops others from creating instances
   */
  private FastAddRemove() {
  // no instance wanted
  }


  /**
   * Removes the <code>program</code> from the DVBViewer recordings list
   *
   * @param program the program to be removes
   */
  static void remove(Program program) {
    Settings set = Settings.getSettings();
    if (set.getChannelCount() == 0) {
      HelperClass.error(localizer.msg("err_no_channelassignment",
              "No channels!\nPlease add some channels first!"));
      return;
    }

    List<ScheduledRecording> entries = DvbViewerTimers.getEntries(set.getViewerTimersPath());

    TvbDvbVChannel assignedChannel = set.getChannelByTVBrowserName(program.getChannel().getName());
    if (!assignedChannel.isValid()) {
      HelperClass.error(localizer.msg("err_missing_assignment",
              "Channel not available, please add it!"));
      return;
    }

    // find the entry in the list
    for (int i = 0; i < entries.size(); i++) {
      ScheduledRecording rec = entries.get(i);

      Marker marker = set.getMarker();
      if (Marker.match(program, rec.getProgramTitle(), rec.getStartDate(), rec.getStartTime())) {
        entries.remove(i);
        marker.unmark(i);
      }
    }

    DvbViewerTimers.setEntries(set.getViewerTimersPath(), entries);

    if (ProcessHandler.isDVBViewerActive(set)) {
      // force DVBViewer to re-read the recordings list
      ProcessHandler.updateDvbViewer(set);
    } else {
      // update the scheduler if it exists
      ProcessHandler.runDvbSchedulerUpdate(set);
    }
  }


  /**
   * Add the selected program to the timers.xml using default values
   *
   * @param program
   */
  static void add(Program program) {
    Settings set = Settings.getSettings();
    if (set.getChannelCount() == 0) {
      HelperClass.error(localizer.msg("err_no_channelassignment",
              "No channels!\nPlease add some channels first!"));
      return;
    }
    TvbDvbVChannel channel = set.getChannelByTVBrowserName(program.getChannel().getName());
    if (!channel.isValid()) {
      HelperClass.error(localizer.msg("err_missing_assignment",
              "Channel not available, please add it!"));
      return;
    }

    List<ScheduledRecording> entries = DvbViewerTimers.getEntries(set.getViewerTimersPath());

    // create the output
    ScheduledRecording rec = new ScheduledRecording(program, channel,
                                                    set.getRecordBefore(), set.getRecordAfter(),
                                                    set.getDefRecAction(), set.getDefAfterAction(),
                                                    set.isDefAvDisabled());

    if (!entries.contains(rec)) {
      // entry missing, add it
      entries.add(rec);
      // write the file for DVBViewer
      DvbViewerTimers.setEntries(set.getViewerTimersPath(), entries);
    }

    if (set.isMarkRecordings()) {
      program.mark(set.getPlugin());
      set.getMarker().addProgram(program);
    }

    if (ProcessHandler.isDVBViewerActive(set)) {
      // force DVBViewer to re-read the recordings list
      ProcessHandler.updateDvbViewer(set);
    } else if (program.isOnAir()) {
      // program is running ask user for starting DVBviewer
      int requestRunningProgramReply = HelperClass.confirm(localizer.msg("request_startviewer",
              "Program is running, start DVBViewer?"));
      if (requestRunningProgramReply == JOptionPane.YES_OPTION) {
        ProcessHandler.runDvbViewer(set, channel);
      }
    } else {
      // update the scheduler if it exists
      ProcessHandler.runDvbSchedulerUpdate(set);
    }
  }
}
