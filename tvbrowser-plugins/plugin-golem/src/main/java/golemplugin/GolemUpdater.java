/*
 * Golem.de guckt - Plugin for TV-Browser
 * Copyright (C) 2010 Bodo Tasche
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
 * SVN information:
 *     $Date: 2010-02-20 13:09:24 +0100 (Sa, 20. Feb 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6530 $
 */
package golemplugin;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import edu.emory.mathcs.backport.java.util.Collections;

public class GolemUpdater {
  private static final String GOLEM_ICS_URL = "http://www.golem.de/guckt/GoGu.ics";

  private static final Logger logger = Logger.getLogger(GolemUpdater.class.getName());

  private static GolemUpdater instance;

  private boolean updateRunning = false;

  private HashMap<String, String> channelMap;

  private GolemUpdater() {
    channelMap = new HashMap<String, String>();
    channelMap.put("RTL II", "RTL2");
    channelMap.put("RTL 2", "RTL2");
    channelMap.put("Dradio Kultur", "Deutschlandradio Kultur");
    channelMap.put("DLF", "Deutschlandfunk");
    channelMap.put("Pro7", "ProSieben");
    channelMap.put("RBB", "RBB Berlin");
    channelMap.put("Eins Festival", "einsfestival");
    channelMap.put("SuperRTL", "Super RTL");
    channelMap.put("Tele5", "Tele 5");
    channelMap.put("NDR", "NDR Niedersachsen");
  }

  public static GolemUpdater getInstance() {
    if (instance == null) {
      instance = new GolemUpdater();
    }

    return instance;
  }

  public void update() {
    synchronized (this) {
      if (updateRunning) {
        return;
      }
      updateRunning = true;
    }

    try {
      GolemPlugin.getInstance().getSettings().resetPrograms();
      URL ical = new URL(GOLEM_ICS_URL);

      CalendarBuilder builder = new CalendarBuilder();
      Calendar calendar = builder.build(ical.openStream());

      Channel[] channels = GolemPlugin.getPluginManager().getSubscribedChannels();

      for (Object o : calendar.getComponents(Component.VEVENT)) {
        VEvent event = (VEvent) o;

        java.util.Calendar start = java.util.Calendar.getInstance();
        start.setTime(event.getStartDate().getDate());
        final int startTime = start.get(java.util.Calendar.HOUR_OF_DAY) * 60 + start.get(java.util.Calendar.MINUTE);

        String summary = event.getSummary().getValue();
        String[] desc = event.getDescription().getValue().split("\n");

        if (desc.length >= 2) {
          Date date = new Date(start);
          String chname = desc[1].trim();
          Channel ch = findChannel(channels, chname);

          // sometimes the genre and channel name are mixed up
          if (ch == null) {
            ch = findChannel(channels, desc[0].trim());
          }

          if (ch != null) {
            Iterator<Program> iterator = GolemPlugin.getPluginManager().getChannelDayProgram(date, ch);
            ArrayList<Program> programs = new ArrayList<Program>();
            if (null != iterator) {
              boolean found = false;
              while (iterator.hasNext() && !found) {
                Program p = iterator.next();
                programs.add(p);
                if (p.getStartTime() == startTime) {
                  GolemPlugin.getInstance().getSettings().addProgram(p);
                  found = true;
                }
              }
              if (!found) {
                Collections.sort(programs, new Comparator<Program>() {

                  @Override
                  public int compare(Program first, Program second) {
                    int firstDelta = Math.abs(first.getStartTime() - startTime);
                    int secondDelta = Math.abs(second.getStartTime() - startTime);
                    return firstDelta - secondDelta;
                  }
                });
                for (Program program : programs) {
                  if (program.getTitle().equalsIgnoreCase(summary)) {
                    GolemPlugin.getInstance().getSettings().addProgram(program);
                    break;
                  }
                }
              }
            }

          } else {
            logger.fine("Missed channel name: " + chname);
          }
        }

      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Problems during data download: ", e);
    }
    GolemPlugin.getInstance().getRootNode().update();

    synchronized (this) {
      updateRunning = false;
    }
  }

  private Channel findChannel(Channel[] channels, String chname) {
    // first try original channel name
    for (Channel ch : channels) {
      if (ch.getName().equalsIgnoreCase(chname) || ch.getDefaultName().equalsIgnoreCase(chname)) {
        return ch;
      }
    }

    // now try alternate version
    if (channelMap.containsKey(chname)) {
      chname = channelMap.get(chname);
      for (Channel ch : channels) {
        if (ch.getName().equalsIgnoreCase(chname) || ch.getDefaultName().equalsIgnoreCase(chname)) {
          return ch;
        }
      }
    }

    return null;
  }

}