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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

/**
 * Parses the RadioTimes Data
 * 
 * @author bodum
 */
public class RadioTimesFileParser {
  /** Channel */
  private Channel mChannel;
  private HashMap<Date, MutableChannelDayProgram> mMutMap = new HashMap<Date, MutableChannelDayProgram>();
  
  /**
   * 
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
    
    // Do the parsing...
    BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(builder.toString()).openStream(), "UTF8"));

    String line;

    while ((line = reader.readLine()) != null) {
      String[] items = line.split("~");

      if (items.length == 23) {
        Date date = parseDate(items[19]);

        if (date.compareTo(endDate) >  0) {
          for (MutableChannelDayProgram mutDayProg : getAllMutableDayPrograms()) {
            updateManager.updateDayProgram(mutDayProg);
          }

          reader.close();
          return;
        }
        
        MutableChannelDayProgram mutDayProg = getMutableDayProgram(date);
        
        int[] time = parseTime(items[20]);
        
        MutableProgram prog = new MutableProgram(mChannel,date, time[0], time[1], true);
        
        prog.setTitle(items[0]);

        StringBuilder desc = new StringBuilder(items[1].trim()).append("\n\n");
        desc.append(items[17]);
        prog.setTextField(ProgramFieldType.DESCRIPTION_TYPE, desc.toString().trim());

        prog.setTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE, items[17].trim());
        
        prog.setTextField(ProgramFieldType.EPISODE_TYPE, items[2].trim());
        
        if (items[3].trim().length() > 0) {
          try {
            prog.setIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE, Integer.parseInt(items[3]));
          } catch (Exception e) {
          }
        }

        if (items[4].trim().length() > 0) {
          prog.setTextField(ProgramFieldType.DIRECTOR_TYPE, items[4].trim());
        }
        
        if (items[5].trim().length() > 0)
          prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE, createCast(items[5]));
        
        if (items[16].trim().length() > 0) {
          prog.setTextField(ProgramFieldType.GENRE_TYPE, items[16].trim());
        }

        int bitset = 0;
        
        if (items[9].trim().equalsIgnoreCase("true")) {
          bitset = bitset | Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED;
        }
        
        if (items[10].trim().equalsIgnoreCase("true")) {
          bitset = bitset | Program.INFO_VISION_16_TO_9;
        }
        
        if (items[13].trim().equalsIgnoreCase("true")) {
          bitset = bitset | Program.INFO_VISION_BLACK_AND_WHITE;
        }
       
        prog.setInfo(bitset);
        
        try {
          int age = Integer.parseInt(items[15]);
          prog.setIntField(ProgramFieldType.AGE_LIMIT_TYPE, age);
        } catch (Exception e) {
        }
        
        
        int[] endtime = parseTime(items[20]);
        prog.setTimeField(ProgramFieldType.END_TIME_TYPE, endtime[0] * 60 + endtime[1]);
        
        prog.setProgramLoadingIsComplete();
        mutDayProg.addProgram(prog);
      }

    }

    for (MutableChannelDayProgram mutDayProg : getAllMutableDayPrograms()) {
      updateManager.updateDayProgram(mutDayProg);
    }
    
    reader.close();
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
          actors.append(names[1]).append(" ... ").append(names[0]).append("\n");
        } else {
          actors.append(names).append("\n");
        }
      }
      
      return actors.toString();
    } else 
      return string.trim();
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
    MutableChannelDayProgram mut = mMutMap.get(date);
    
    if (mut == null) {
      mut = new MutableChannelDayProgram(date, mChannel);
      mMutMap.put(date, mut);
    }
   
    return mut;
  }

}