/*
 * DVBViewerEPGParser.java
 * Copyright (C) 2008 Ullrich Pollaehne (pollaehne@users.sourceforge.net)
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
package dvbviewerdataservice;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.TvDataUpdateManager;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import dvbviewer.DVBViewerCOM;
import dvbviewer.DVBViewerChannel;
import dvbviewer.com4j.IEPGItem;


/**
 * Read the DVBViewer EPG data
 * <p>
 * Reads and converts the DVBViewer EPG data to something
 * that TV-Browser can handle
 *
 * @author pollaehne
 * @version $Revision: $
 */
public class DVBViewerEPGParser {

  /** Channel */
  private Channel channel;

  /** start of day display in minutes from midnight */
  private int startOfDay;

  /** end of day display in minutes from midnight */
  private int endOfDay;

  /** reference to DVBViewer */
  private DVBViewerCOM dvbviewer;

  /** the DVBViewer channel */
  private DVBViewerChannel dvbChannel;

  /**
   * map of lazily created update channel day programs
   */
  private HashMap<Date, MutableChannelDayProgram> dayprograms = new HashMap<Date, MutableChannelDayProgram>();


  /**
   * @param ch the TV-Browser channel to get EPG for
   * @param viewer a reference to the DVBViewer
   * @param viewerChannel the DVBViewer channel
   * @param start the start of day display (in minutes from midnight)
   * @param end the end of day display (in minutes from midnight)
   */
  public DVBViewerEPGParser(Channel ch, DVBViewerCOM viewer, DVBViewerChannel viewerChannel, int start, int end) {
    channel = ch;
    dvbviewer = viewer;
    dvbChannel = viewerChannel;
    startOfDay = start;
    endOfDay = end;
  }


  /**
   * Parse the DVBViewer EPG infos into DayPrograms
   *
   * @param updateManager
   * @param startDate
   * @param dayCount
   */
  public void parse(TvDataUpdateManager updateManager, Date startDate, int dayCount) {
      // get start and end date/time
      Calendar startDateTime = getCalendar(startDate, startOfDay, 0);
      Calendar endDateTime = getCalendar(startDate, endOfDay, dayCount);

      // process all EPG items
      for (IEPGItem item : dvbviewer.getEPG(dvbChannel, startDateTime, endDateTime)) {
        java.util.Date start = item.time();
        Date sdate = new Date(start.getYear()+1900, start.getMonth()+1, start.getDate());

        MutableChannelDayProgram dayProg = getMutableDayProgram(sdate);

        MutableProgram prog = new MutableProgram(channel, sdate, start.getHours(), start.getMinutes(), true);
        prog.setTitle(item.title());

        int duration = item.duration().getHours() * 60 + item.duration().getMinutes();
        prog.setLength(duration);

        int bitset = processDescription(item.description(), prog);
        bitset = processContent(item.content(), bitset);
        prog.setInfo(bitset);

        prog.setProgramLoadingIsComplete();
        dayProg.addProgram(prog);
      }

      storeDayPrograms(updateManager);
  }


  /**
   * Process the description lines of the EPG info
   * This info can contain description lines and
   * Audio/Video format informations
   *
   * The short and long description are stored in <code>prog</code> if there are any
   *
   * @param description the description lines
   * @param prog the program to be updated
   * @return the info bitset of Audio/Video information
   */
  private int processDescription(final String description, MutableProgram prog) {
    if (null == description) {
      return 0;
    }

    int bitset = 0;
    String shortDescription = null;
    String longDescription = null;

    for (String descLine : description.split("\n")) {
      if (descLine.contains("Video,")) {
        // video format infos
        if (descLine.contains("16:9")) {
          bitset |= Program.INFO_VISION_16_TO_9;
        } else if (descLine.contains("4:3")) {
          bitset |= Program.INFO_VISION_4_TO_3;
        }
      } else if (descLine.contains("Audio,")) {
        // audio format infos
        if (descLine.contains("stereo")) {
          bitset |= Program.INFO_AUDIO_STEREO;
        }
        if (descLine.contains("surround")) {
          bitset |= Program.INFO_AUDIO_DOLBY_SURROUND;
        }
      } else {
        // description
        if (null == shortDescription) {
          // only take the first line as short description
          longDescription = shortDescription = descLine;
        } else {
          longDescription += "\n" + descLine;
        }
      }
    }

    if (null != shortDescription) {
      shortDescription = validateShortInfo(shortDescription);
      prog.setShortInfo(shortDescription);
    }
    if (null != longDescription) {
      prog.setDescription(longDescription);
    }

    return bitset;
  }


  private String validateShortInfo(String shortInfo) {
    int MAX_SHORT_INFO_LENGTH = 196;
    if ((shortInfo != null) && (shortInfo.length() > MAX_SHORT_INFO_LENGTH)) {
      // Get the end of the last fitting sentense
      int lastDot = shortInfo.lastIndexOf('.', MAX_SHORT_INFO_LENGTH);

      int n = shortInfo.lastIndexOf('!', MAX_SHORT_INFO_LENGTH);
      if (n > lastDot) {
        lastDot = n;
      }
      n = shortInfo.lastIndexOf('?', MAX_SHORT_INFO_LENGTH);
      if (n > lastDot) {
        lastDot = n;
      }
      n = shortInfo.lastIndexOf(" - ", MAX_SHORT_INFO_LENGTH);
      if (n > lastDot) {
        lastDot = n;
      }

      int lastMidDot = shortInfo.lastIndexOf('\u00b7', MAX_SHORT_INFO_LENGTH);

      int cutIdx = Math.max(lastDot, lastMidDot);

      // But show at least half the maximum length
      if (cutIdx < (MAX_SHORT_INFO_LENGTH / 2)) {
        cutIdx = shortInfo.lastIndexOf(' ', MAX_SHORT_INFO_LENGTH);
      }

      shortInfo = shortInfo.substring(0, cutIdx + 1) + "...";
    }

    return shortInfo;
  }


  /**
   * Process the content type of the EPG infos
   * If there are know content types they are 'OR'ed to the <code>bitset</code> and returned
   * @param content the content type
   * @param bitset the initial info bitset
   * @return bitset 'OR'ed with content type
   */
  private int processContent(int content, int bitset) {
    switch (content) {
      case 21: //soap/melodrama/folkloric
        bitset |= Program.INFO_CATEGORIE_SERIES;
        break;
      case 32: //news/current affairs (general)
      case 33: //news/weather report
      case 34: //news/magazine
        bitset |= Program.INFO_CATEGORIE_NEWS;
        break;
      case 48: //show/game show (general)
      case 49: //game show/quiz/contest
      case 50: //variety show
      case 51: //talk show
        bitset |= Program.INFO_CATEGORIE_SHOW;
        break;
      //case 16: //movie/drama (general) | misused by some channels for series
      case 22: //romance
      case 23: //serious/classical/religious/historical movie/drama
      case 24: //adult movie/drama
      case 118: //film/cinema
      case 119: //experimental film/video
        bitset |= Program.INFO_CATEGORIE_MOVIE;
        break;
      default:
        // nothing to do
    }

    return bitset;
  }


  /**
   * Create a Calendar containing date and time with the timezone of the channel
   * The time is set to midnight, the <code>minutesFromMidnight</code> are added
   * The date is set to <code>startDate</code>, the <code>addDays</code> days are added
   *
   * @param startDate
   * @param minutesFromMidnight
   * @param addDays
   * @return
   */
  private Calendar getCalendar(Date startDate, int minutesFromMidnight, int addDays) {
    GregorianCalendar cal = new GregorianCalendar(channel.getTimeZone());
    cal.set(Calendar.YEAR, startDate.getYear());
    cal.set(Calendar.MONTH, startDate.getMonth()-1);
    cal.set(Calendar.DAY_OF_MONTH, startDate.getDayOfMonth());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);

    cal.add(Calendar.DAY_OF_MONTH, addDays);
    cal.add(Calendar.MINUTE, minutesFromMidnight);
    return cal;
  }


  /**
   * Process all ChannelDayPrograms in <code>dayprograms</code> and
   * update them in TV-Browser if there are new or changed data
   *
   * @param updateManager
   */
  private void storeDayPrograms(TvDataUpdateManager updateManager) {
    for (MutableChannelDayProgram dayProg : dayprograms.values()) {
      // compare new and existing programs to avoid unnecessary updates
      boolean update = true;

      Iterator<Program> itCurrProg = AbstractTvDataService.getPluginManager().getChannelDayProgram(dayProg.getDate(), channel);
      Iterator<Program> itNewProg = dayProg.getPrograms();
      if (itCurrProg != null && itNewProg != null) {
        update = false;
        while (itCurrProg.hasNext() && itNewProg.hasNext()) {
          MutableProgram currProg = (MutableProgram) itCurrProg.next();
          MutableProgram newProg = (MutableProgram) itNewProg.next();
          if (!currProg.equalsAllFields(newProg)) {
            update = true;
          }
        }
        // not the same number of programs ?
        if (itCurrProg.hasNext() != itNewProg.hasNext()) {
          update = true;
        }
      }
      if (update) {
        updateManager.updateDayProgram(dayProg);
      }
    }
  }


  /**
   * @param date Date to search for
   * @return MutableChannelDayProgram that fits to the Date, a new one is created if needed
   */
  private MutableChannelDayProgram getMutableDayProgram(Date date) {
    MutableChannelDayProgram dayProgram = dayprograms.get(date);
    if (dayProgram == null) {
      dayProgram = new MutableChannelDayProgram(date, channel);
      dayprograms.put(date, dayProgram);
    }

    return dayProgram;
  }
}



