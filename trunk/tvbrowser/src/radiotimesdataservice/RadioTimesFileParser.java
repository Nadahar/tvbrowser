/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 *     $Date: 2006-06-03 00:23:19 +0200 (Sa, 03 Jun 2006) $
 *   $Author: ds10 $
 * $Revision: 2452 $
 */
package radiotimesdataservice;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFieldType;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.TvDataUpdateManager;
import util.io.IOUtilities;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Parses the RadioTimes Data
 * 
 * @author bodum
 */
/**
 * @author bananeweizen
 *
 */
public class RadioTimesFileParser {

  /** The logger for this class. */
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(RadioTimesFileParser.class.getName());
  
  /** Channel */
  private Channel mChannel;
  
  /**
   * map of lazily created update channel day programs 
   */
  private HashMap<Date, MutableChannelDayProgram> mMutMap = new HashMap<Date, MutableChannelDayProgram>();

  // field numbers used in the downloaded text files
  private static final int RT_TITLE = 0;
  private static final int RT_SUBTITLE = 1;
  private static final int RT_EPISODE = 2;
  private static final int RT_YEAR = 3;
  private static final int RT_DIRECTOR = 4;
  private static final int RT_ACTORS = 5;
  private static final int RT_MOVIE_PREMIERE = 6;
  private static final int RT_MOVIE = 7;
  @SuppressWarnings("unused")
  private static final int RT_REPETITION = 8; // is a repetition
  @SuppressWarnings("unused")
  private static final int RT_SUBTITLED = 9; // this does not mean ORIGINAL_WITH_SUBTITLE
  private static final int RT_16_TO_9 = 10;
  private static final int RT_NEW_SERIES = 11;
  private static final int RT_SUBTITLES_FOR_AURALLY_HANDICAPPED = 12;
  private static final int RT_BLACK_WHITE = 13;
  @SuppressWarnings("unused")
  private static final int RT_STAR_RATING = 14; // 1 to 5 stars
  private static final int RT_AGE_LIMIT = 15;
  private static final int RT_GENRE = 16;
  private static final int RT_DESCRIPTION = 17;
  @SuppressWarnings("unused")
  private static final int RT_CHOICE = 18; // ???
  private static final int RT_DATE = 19;
  private static final int RT_START_TIME = 20;
  private static final int RT_END_TIME = 21;
  private static final int RT_DURATION_MINUTES = 22;

  /**
   * @param ch Parse this Channel
   */
  public RadioTimesFileParser(Channel ch) {
    mChannel = ch;
  }

  /**
   * Parse the Data
   * 
   * @param updateManager
   * @param endDate 
   * @throws Exception
   */
  public void parse(TvDataUpdateManager updateManager, Date endDate) throws Exception {
    StringBuilder builder = new StringBuilder(RadioTimesDataService.BASEURL);
    builder.append(mChannel.getId().substring(RadioTimesDataService.RADIOTIMES.length()));
    builder.append(".dat");

    String file = new String(IOUtilities.loadFileFromHttpServer(new URL(builder.toString())), "UTF8");

    for (String line:file.split("\n")) {
      String[] items = line.split("~");

      if (items.length == 23) {
        Date date = parseDate(items[RT_DATE]);
        if (date.compareTo(endDate) >  0) {
          storeDayPrograms(updateManager);
          return;
        }
        
        MutableChannelDayProgram mutDayProg = getMutableDayProgram(date);
        
        int[] time = parseTime(items[RT_START_TIME]);
        
        MutableProgram prog = new MutableProgram(mChannel,date, time[0], time[1], true);
        
        prog.setTitle(items[RT_TITLE]);

        StringBuilder desc = new StringBuilder(items[RT_SUBTITLE].trim()).append("\n\n");

        if (items[RT_DESCRIPTION].indexOf(0x0D) > 0) {
          items[RT_DESCRIPTION] = items[RT_DESCRIPTION].replace((char)0x0D, '\n');
        }

        if (items[RT_STAR_RATING].length() != 0) {
            items[RT_DESCRIPTION] = "[" + items[RT_STAR_RATING] + "/5] " + items[RT_DESCRIPTION];
        }

        desc.append(items[RT_DESCRIPTION]);
        desc.append("\n\n");

        prog.setTextField(ProgramFieldType.DESCRIPTION_TYPE, desc.toString().trim());

        prog.setTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE, MutableProgram.generateShortInfoFromDescription(items[RT_DESCRIPTION].trim()));

        String field = items[RT_EPISODE].trim();
        if (field.length() > 0) {
          prog.setTextField(ProgramFieldType.EPISODE_TYPE, field);
        }
        
        field = items[RT_YEAR].trim();
        if (field.length() > 0) {
          try {
            prog.setIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE, Integer.parseInt(field));
          } catch (Exception e) {
          }
        }

        field = items[RT_DURATION_MINUTES].trim();
        if (field.length() > 0) {
          try {
            prog.setIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE, Integer.parseInt(field));
          } catch (Exception e) {
          }
        }

        field = items[RT_DIRECTOR].trim();
        if (field.length() > 0) {
          prog.setTextField(ProgramFieldType.DIRECTOR_TYPE, field);
        }
        
        field = items[RT_ACTORS].trim();
        if (field.length() > 0) {
          prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE, createCast(field));
        }
        
        field = items[RT_GENRE].trim();
        if (field.length() > 0) {
          prog.setTextField(ProgramFieldType.GENRE_TYPE, field);
        }

        int bitset = 0;
        
        if (items[RT_SUBTITLES_FOR_AURALLY_HANDICAPPED].trim().equalsIgnoreCase("true")) {
          bitset |= Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED;
        }
        
        if (items[RT_16_TO_9].trim().equalsIgnoreCase("true")) {
          bitset |= Program.INFO_VISION_16_TO_9;
        }
        
        if (items[RT_BLACK_WHITE].trim().equalsIgnoreCase("true")) {
          bitset |= Program.INFO_VISION_BLACK_AND_WHITE;
        }
        
        if (items[RT_MOVIE].trim().equalsIgnoreCase("true")) {
          bitset |= Program.INFO_CATEGORIE_MOVIE;
        }
        
        if (items[RT_MOVIE_PREMIERE].trim().equalsIgnoreCase("true")) {
          bitset |= Program.INFO_NEW; 
        }

        if (items[RT_NEW_SERIES].trim().equalsIgnoreCase("true")) {
          bitset |= Program.INFO_NEW;
          bitset |= Program.INFO_CATEGORIE_MOVIE;
        }
       
        prog.setInfo(bitset);
        
        try {
          int age = Integer.parseInt(items[RT_AGE_LIMIT]);
          prog.setIntField(ProgramFieldType.AGE_LIMIT_TYPE, age);
        } catch (Exception e) {
        }
        
        
        int[] endtime = parseTime(items[RT_END_TIME]);
        prog.setTimeField(ProgramFieldType.END_TIME_TYPE, endtime[0] * 60 + endtime[1]);
        
        prog.setProgramLoadingIsComplete();
        mutDayProg.addProgram(prog);
      }
      else {
        mLog.warning("Non matching line in Radiotimes: " + line);
      }

    }

    storeDayPrograms(updateManager);
  }

  private void storeDayPrograms(TvDataUpdateManager updateManager) {
    for (MutableChannelDayProgram newDayProg : getAllMutableDayPrograms()) {
      // compare new and existing programs to avoid unnecessary updates
      boolean update = true;
      
      Iterator<Program> itCurrProg = RadioTimesDataService.getPluginManager().getChannelDayProgram(newDayProg.getDate(), mChannel);
      Iterator<Program> itNewProg = newDayProg.getPrograms();
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
        updateManager.updateDayProgram(newDayProg);
      }
    }
  }

  /**
   * @param string Analyse this String
   * @return Create Actor List from String
   */
  private String createCast(String string) {
    if (string.contains("|")) {
      StringBuilder actors = new StringBuilder();
      
      String[] actorlist = string.split("\\|");
      
      for (String actor : actorlist) {
        String[] names = actor.split("\\*");
        
        if (names.length == 2) {
          actors.append(names[1]).append("\t").append(names[0]).append("\n");
        } else {
          actors.append(names[0]).append("\n");
        }
      }
      
      return actors.toString();
    } else {
      return string.trim();
    }
  }

  /**
   * @param string Parse this Time
   * @return {hour, minute}
   */
  private int[] parseTime(String string) {
    int[] times = new int[2];
    times[0] = Integer.parseInt(string.substring(0, 2));
    times[1] = Integer.parseInt(string.substring(3, 5));
    return times;
  }

  /**
   * Parse Date
   * 
   * @param string Date to parse
   * @return Date
   * @throws Exception
   */
  private Date parseDate(String string) throws Exception {
    int day   = Integer.parseInt(string.substring(0, 2));
    int month = Integer.parseInt(string.substring(3, 5));
    int year  = Integer.parseInt(string.substring(6));
    
    if (year < 2000) {
      year += 2000;
    }
    
    return new Date(year, month, day);
  }

  /**
   * @return all MutableChannelDayPrograms
   */
  private Collection<MutableChannelDayProgram> getAllMutableDayPrograms() {
    return mMutMap.values();
  }

  /**
   * @param date Date to search for
   * @return MutableChannelDayProgram that fits to the Date, a new one is created if needed
   */
  private MutableChannelDayProgram getMutableDayProgram(Date date) {
    MutableChannelDayProgram dayProgram = mMutMap.get(date);
    
    if (dayProgram == null) {
      dayProgram = new MutableChannelDayProgram(date, mChannel);
      mMutMap.put(date, dayProgram);
    }
   
    return dayProgram;
  }

}