/*
 * Marker.java
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import dvbplugin.Settings.TvbDvbVChannel;
import dvbplugin.dvbviewer.ScheduledRecording;
import dvbplugin.dvbviewer.DvbViewerTimers;

/**
 * @author Probum (Tobias Bürner)
 */
class Marker {

  private ArrayList<Program> programs;


  /** Creates a new instance of Marker */
  Marker() {
    programs = new ArrayList<Program>();
    getPrograms();
  }


  void addProgram(Program p) {
    if (!programs.contains(p)) programs.add(p);
  }


  void mark() {
    unmarkAll();
    Plugin plugin = Settings.getSettings().getPlugin();
    for (int i = 0; i < programs.size(); i++) {
      Program p = programs.get(i);
      p.mark(plugin);
    }
  }


  void unmark(int index) {
    try {
      Program p = programs.get(index);
      programs.remove(index);
      p.unmark(Settings.getSettings().getPlugin());
    } catch (Exception e) {
      // nichts tun
    }
  }


  void unmarkAll() {
    Program[] p = Plugin.getPluginManager().getMarkedPrograms();
    Plugin plugin = Settings.getSettings().getPlugin();
    for (int i = 0; i < p.length; i++) {
      p[i].unmark(plugin);
    }
  }


  private final void getPrograms() {
    Settings set = Settings.getSettings();
    if (!set.isValid() || 0 == set.getChannelCount()) { return; }

    Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
    List<ScheduledRecording> entries = DvbViewerTimers.getEntries(set.getViewerTimersPath());
    devplugin.Date date = new devplugin.Date();

    for (int tage = 0; tage < 31; tage++) {
      for (Iterator<ScheduledRecording> it = entries.iterator(); it.hasNext();) {
        ScheduledRecording rec = it.next();

        Channel channel = null;

        TvbDvbVChannel chnl = set.getChannelByDVBViewerName(rec.getDvbViewerChannel());
        if (chnl.isValid()) {
          for (int i = 0; i < channels.length; ++i) {
            if (chnl.getTvBrowserName().equals(channels[i].getName())) {
              channel = channels[i];
              break;
            }
          }

          if (null != channel) {
            Iterator<Program> pit = Plugin.getPluginManager().getChannelDayProgram(date, channel);
            while (null != pit && pit.hasNext()) {
              Program p = pit.next();
              if (match(p, rec.getProgramTitle(), rec.getStartDate(), rec.getStartTime())) {
                rec.setTvbID(p.getID());
                addProgram(p);
              }
            }
          }
        }
      }
      date = date.addDays(1);
    }
  }


  static final boolean match(Program program, String title, String datum, String start) {
    String programTitle = program.getTitle().replace('\n', ' ');
    if (-1 == title.indexOf(programTitle)) {
      return false;
    }

    // Ein datum zusammenbauen
    int year = Integer.parseInt(datum.substring(6));
    int month = Integer.parseInt(datum.substring(3, 5));
    int day = Integer.parseInt(datum.substring(0, 2));

    Date date = new Date(year, month, day);
    if (!program.getDate().equals(date)) {
      return false;
    }

    int h = 15;
    int pStart = program.getStartTime();
    int sStart = getStartTime(start);
    if (pStart - h <= sStart && sStart <= pStart) {
      return true;
    }

    return false;
  }


  static final int getStartTime(String s) {
    int idx = s.indexOf(':');
    int hour = Integer.parseInt(s.substring(0, idx));
    int min = Integer.parseInt(s.substring(idx + 1, idx + 3));

    return hour * 60 + min;
  }
}
