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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.TvDataUpdateManager;
import util.io.stream.InputStreamProcessor;
import util.io.stream.StreamUtilities;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * Parses the RadioTimes Data
 *
 * @author bodum
 */
public class RadioTimesFileParser {

  /** The logger for this class. */
  private static final Logger mLog
    = Logger.getLogger(RadioTimesFileParser.class.getName());

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
  private static final int RT_SUBTITLED = 9;
  private static final int RT_16_TO_9 = 10;
  private static final int RT_NEW_SERIES = 11;
  private static final int RT_DEAF_SIGNED = 12;
  private static final int RT_BLACK_WHITE = 13;
  private static final int RT_STAR_RATING = 14; // 1 to 5 stars
  private static final int RT_AGE_LIMIT = 15;
  private static final int RT_GENRE = 16;
  private static final int RT_DESCRIPTION = 17;
  @SuppressWarnings("unused")
  private static final int RT_CHOICE = 18; // This means that the Radio Times editorial team have marked it as a choice
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
  public void parse(final TvDataUpdateManager updateManager, final Date endDate) throws Exception {
    StringBuilder urlString = new StringBuilder(RadioTimesDataService.BASEURL);
    urlString.append(mChannel.getId().substring(RadioTimesDataService.RADIOTIMES.length()));
    urlString.append(".dat");

    StreamUtilities.inputStream(new URL(urlString.toString()), new InputStreamProcessor() {

      @Override
      public void process(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));

        int countProgram = 0;
        String line;
        String lastLine = "";
        while ((line = reader.readLine()) != null) {
          if (lastLine.length() > 0) { // re-append updated descriptions.
            line = lastLine + " " + line;
          }
          String[] items = line.trim().split("~");

          if (items.length == 23) {
            try {
              for (int i = 0; i < items.length; i++) {
                items[i] = items[i].trim();
              }
              Date date = parseDate(items[RT_DATE]);
              MutableChannelDayProgram mutDayProg = getMutableDayProgram(date);

              int[] time = parseTime(items[RT_START_TIME]);

              MutableProgram prog = new MutableProgram(mChannel,date, time[0], time[1], true);

              prog.setTitle(items[RT_TITLE]);

              StringBuilder desc = new StringBuilder(items[RT_SUBTITLE].trim()).append("\n\n");

              if (items[RT_DESCRIPTION].indexOf(0x0D) > 0) {
                items[RT_DESCRIPTION] = items[RT_DESCRIPTION].replace((char)0x0D, '\n');
              }

              if (items[RT_STAR_RATING].length() != 0) {
                  prog.setIntField(ProgramFieldType.RATING_TYPE, translateRating(items[RT_STAR_RATING]));
              }

              desc.append(items[RT_DESCRIPTION]);

              prog.setTextField(ProgramFieldType.DESCRIPTION_TYPE, desc.toString().trim());

              prog.setTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE, MutableProgram.generateShortInfoFromDescription(items[RT_DESCRIPTION].trim()));

              String field = items[RT_EPISODE];
              if (field.length() > 0) {
                prog.setTextField(ProgramFieldType.EPISODE_TYPE, field);
              }

              field = items[RT_YEAR];
              if (field.length() > 0) {
                try {
                  prog.setIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE, Integer.parseInt(field));
                } catch (Exception e) {
                }
              }

              field = items[RT_DURATION_MINUTES];
              if (field.length() > 0) {
                try {
                  prog.setIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE, Integer.parseInt(field));
                } catch (Exception e) {
                }
              }

              field = items[RT_DIRECTOR];
              if (field.length() > 0) {
                prog.setTextField(ProgramFieldType.DIRECTOR_TYPE, field);
              }

              field = items[RT_ACTORS];
              if (field.length() > 0) {
                prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE, createCast(field));
              }

              int bitset = 0;

              field = items[RT_GENRE];
              if ((field.length() > 0)&& (!field.equalsIgnoreCase("No Genre"))) {
                prog.setTextField(ProgramFieldType.GENRE_TYPE, field);

                if (field.equalsIgnoreCase("Business")) {
                  bitset |= Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
                } else if (field.equalsIgnoreCase("Children")) {
                  bitset |= Program.INFO_CATEGORIE_CHILDRENS;
                } else if (field.equalsIgnoreCase("Comedy")) {
                  bitset |= Program.INFO_CATEGORIE_SHOW;
                } else if (field.equalsIgnoreCase("Consumer")) {
                  bitset |= Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
                } else if (field.equalsIgnoreCase("Cookery")) {
                  bitset |= Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
                } else if (field.equalsIgnoreCase("Current affairs")) {
                  bitset |= Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
                } else if (field.equalsIgnoreCase("Documentary")) {
                  bitset |= Program.INFO_CATEGORIE_DOCUMENTARY;
                } else if (field.equalsIgnoreCase("Education")) {
                  bitset |= Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
                } else if (field.equalsIgnoreCase("Entertainment")) {
                  bitset |= Program.INFO_CATEGORIE_SHOW;
                } else if (field.equalsIgnoreCase("Film")) {
                  bitset |= Program.INFO_CATEGORIE_MOVIE;
                } else if (field.equalsIgnoreCase("Game show")) {
                  bitset |= Program.INFO_CATEGORIE_SHOW;
                } else if (field.equalsIgnoreCase("Gardening")) {
                  bitset |= Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
                } else if (field.equalsIgnoreCase("Health")) {
                  bitset |= Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
                } else if (field.equalsIgnoreCase("Interests")) {
                  bitset |= Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
                } else if (field.equalsIgnoreCase("Music and Arts")) {
                  bitset |= Program.INFO_CATEGORIE_ARTS;
                } else if (field.equalsIgnoreCase("News and Current Affairs")) {
                  bitset |= Program.INFO_CATEGORIE_NEWS;
                } else if (field.equalsIgnoreCase("Science")) {
                  bitset |= Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
                } else if (field.equalsIgnoreCase("Sitcom")) {
                  bitset |= Program.INFO_CATEGORIE_SERIES;
                } else if (field.equalsIgnoreCase("Soap")) {
                  bitset |= Program.INFO_CATEGORIE_SERIES;
                } else if (field.equalsIgnoreCase("Sport")) {
                  bitset |= Program.INFO_CATEGORIE_SPORTS;
                } else if (field.equalsIgnoreCase("Talk show")) {
                  bitset |= Program.INFO_CATEGORIE_SHOW;
                } else if (field.equalsIgnoreCase("Travel")) {
                  bitset |= Program.INFO_CATEGORIE_DOCUMENTARY;
                } else if (field.equalsIgnoreCase("Environment")) {
                  bitset |= Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
                }
              }

              if (Boolean.parseBoolean(items[RT_SUBTITLED])) {
                bitset |= Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED;
              }

              if (Boolean.parseBoolean(items[RT_DEAF_SIGNED])) {
                bitset |= Program.INFO_SIGN_LANGUAGE;
              }

              if (Boolean.parseBoolean(items[RT_16_TO_9])) {
                bitset |= Program.INFO_VISION_16_TO_9;
              }

              if (Boolean.parseBoolean(items[RT_BLACK_WHITE])) {
                bitset |= Program.INFO_VISION_BLACK_AND_WHITE;
              }

              if (Boolean.parseBoolean(items[RT_MOVIE])) {
                bitset |= Program.INFO_CATEGORIE_MOVIE;
              }

              if (Boolean.parseBoolean(items[RT_MOVIE_PREMIERE])) {
                bitset |= Program.INFO_NEW;
              }

              if (Boolean.parseBoolean(items[RT_NEW_SERIES])) {
                bitset |= Program.INFO_NEW;
                bitset |= Program.INFO_CATEGORIE_MOVIE;
              }

              prog.setInfo(bitset);

              try {
                int age = Integer.parseInt(items[RT_AGE_LIMIT]);
                prog.setIntField(ProgramFieldType.AGE_LIMIT_TYPE, age);
              } catch (Exception e) {
                // do nothing if field is non numeric
              }


              int[] endtime = parseTime(items[RT_END_TIME]);
              prog.setTimeField(ProgramFieldType.END_TIME_TYPE, endtime[0] * 60 + endtime[1]);

              prog.setProgramLoadingIsComplete();
              mutDayProg.addProgram(prog);
              countProgram ++;
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            lastLine = "";
          }
          else {
            if (items.length > 1) { // empty lines and the first line with the usage agreement are expected
              if (items.length != 18) {
                mLog.warning("Non matching line in Radiotimes: " + line);
              }
              lastLine = line;
            }
          }

        }
        if (countProgram == 0) {
          mLog.warning("Found no matching entry in RadioTimes file");
        }
        else {
          mLog.info("Found " + countProgram + " programs.");
        }
      }
    });

    storeDayPrograms(updateManager);
  }

  private static int translateRating(final String rating) {
    if (rating.equals("1")) {
      return 0;
    } else if (rating.equals("2")) {
      return 25;
    } else if (rating.equals("3")) {
      return 50;
    } else if (rating.equals("4")) {
      return 75;
    } else if (rating.equals("5")) {
      return 100;
    }

    if (rating.length() > 0) {
      mLog.warning("Unknown rating: " + rating);
    }

    return 0;
  }

  private void storeDayPrograms(TvDataUpdateManager updateManager) {
    for (MutableChannelDayProgram newDayProg : getAllMutableDayPrograms()) {
      // compare new and existing programs to avoid unnecessary updates
      boolean update = true;

      Iterator<Program> itCurrProg = AbstractTvDataService.getPluginManager().getChannelDayProgram(newDayProg.getDate(), mChannel);
      Iterator<Program> itNewProg = newDayProg.getPrograms();
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
      if (update) {
        updateManager.updateDayProgram(newDayProg);
      }
    }
  }

  /**
   * @param string Analyze this String
   * @return Create Actor List from String
   */
  private static String createCast(String string) {
    if (string.contains("|")) {
      StringBuilder actors = new StringBuilder();

      String[] actorlist = string.split("\\|");

      for (String actor : actorlist) {
        String[] names = actor.split("\\*");

        if (names.length == 2) {
          actors.append(names[1]).append('\t').append(names[0]).append('\n');
        } else {
          actors.append(names[0]).append('\n');
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
  private static int[] parseTime(String string) {
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
  private static Date parseDate(String string) {
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