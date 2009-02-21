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
package bbcbackstagedataservice;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import bbc.rd.tvanytime.Duration;
import bbc.rd.tvanytime.Genre;
import bbc.rd.tvanytime.Synopsis;
import bbc.rd.tvanytime.TVAnytimeException;
import bbc.rd.tvanytime.creditsInformation.CreditsItem;
import bbc.rd.tvanytime.creditsInformation.CreditsList;
import bbc.rd.tvanytime.programInformation.AVAttributes;
import bbc.rd.tvanytime.programInformation.AudioAttributes;
import bbc.rd.tvanytime.programInformation.ProgramInformation;
import bbc.rd.tvanytime.programInformation.ProgramInformationTable;
import bbc.rd.tvanytime.programInformation.VideoAttributes;
import bbc.rd.tvanytime.programLocation.ProgramLocationTable;
import bbc.rd.tvanytime.programLocation.Schedule;
import bbc.rd.tvanytime.programLocation.ScheduleEvent;
import bbc.rd.tvanytime.xml.NonFatalXMLException;
import bbc.rd.tvanytime.xml.SAXXMLParser;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * This class parses the BBC Files
 * 
 * @author bodum
 */
public class BbcFileParser {
  /** Logger */
  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(BbcFileParser.class
      .getName());

  /** Stores the Data */
  private HashMap<Date, MutableChannelDayProgram> mCache;
  /** Channel */
  private Channel mChannel;
  /** Current Date */
  private Date mChannelDate;
  /** Current Time */
  private int mHour = 0;

  /**
   * 
   * @param cache
   * @param channel 
   * @param channeldate
   */
  public BbcFileParser(HashMap<Date, MutableChannelDayProgram> cache, Channel channel, Date channeldate) {
    mCache = cache;
    mChannelDate = channeldate;
    mChannel = channel;
  }

  /**
   * Checks if the Files exist and starts parsing
   * @param file
   * @throws IOException
   * @throws TVAnytimeException
   */
  public void parseFile(File file) throws IOException, TVAnytimeException {
    if (new File(file.getAbsoluteFile() + "_pi.xml").exists())
      analyseFile(file);
  }
  
  /**
   * Parses the BBC Files
   * @param file
   * @throws IOException
   * @throws TVAnytimeException
   */
  public void analyseFile(File file) throws IOException, TVAnytimeException {
    // Create parser
    SAXXMLParser parser = new SAXXMLParser();

    (parser).setParseProfile(SAXXMLParser.STANDARD);
    // Parse PI, PL and CR
    try {
      parser.parse(new File(file.getAbsoluteFile() + "_pi.xml"));
    } catch (NonFatalXMLException nfxe) {
    }
    try {
      parser.parse(new File(file.getAbsoluteFile() + "_pl.xml"));
    } catch (NonFatalXMLException nfxe) {
    }
    try {
      parser.parse(new File(file.getAbsoluteFile() + "_cr.xml"));
    } catch (NonFatalXMLException nfxe) {
    }
    
    ProgramLocationTable programLocationTable = parser.getProgramLocationTable();
    ProgramInformationTable programInformationTable = parser.getProgramInformationTable();

    MutableChannelDayProgram mutablechanneldayprogram = mCache.get(mChannelDate);
    if (mutablechanneldayprogram == null) {
      mutablechanneldayprogram = new MutableChannelDayProgram(mChannelDate, mChannel);
      mCache.put(mChannelDate, mutablechanneldayprogram);
    }
    
    // Search through all schedules in program location table
    for (int schedulect=0; schedulect<programLocationTable.getNumSchedules(); schedulect++) {
      Schedule schedule = programLocationTable.getSchedule(schedulect);
      if (schedule.getServiceID().equals(mChannel.getId())) {
        // Found schedule for a particular service, e.g. BBC 1
        for (int eventct=0; eventct<schedule.getNumScheduleEvents(); eventct++) {
          
          ScheduleEvent event = schedule.getScheduleEvent(eventct);

          @SuppressWarnings("unchecked")
          Vector<ProgramInformation> vector = programInformationTable.getProgramInformation(event.getCRID());
          
          if (vector.size() == 1) {
            ProgramInformation programInformation = vector.elementAt(0);
            
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            cal.setTime(event.getPublishedStartTime());

            // Workaround for Stupid Bug in TVAnytime API
            cal.add(Calendar.MILLISECOND, TimeZone.getDefault().getRawOffset());
            
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minutes = cal.get(Calendar.MINUTE);
            
            if (hour < mHour) {
              mChannelDate = mChannelDate.addDays(1);

              mutablechanneldayprogram = mCache.get(mChannelDate);
              if (mutablechanneldayprogram == null) {
                mutablechanneldayprogram = new MutableChannelDayProgram(mChannelDate, mChannel);
                mCache.put(mChannelDate, mutablechanneldayprogram);
              }
            }
            
            mHour = hour;
            MutableProgram prog = new MutableProgram(mChannel,mChannelDate,hour, minutes, true);
            
            prog.setTitle(programInformation.getBasicDescription().getTitle(0).getText());
            
            Duration duration = event.getPublishedDuration();
            if (duration != null) {
              minutes = (int) (duration.getDurationInMsec() / 1000 /60);
              if (minutes > 0) {
                prog.setLength(minutes);
              }
            }
            
            // Get Description
            
            StringBuffer shortBuffer = new StringBuffer();
            StringBuffer longBuffer = new StringBuffer();
            
            int maxSyn = programInformation.getBasicDescription().getNumSynopses();
            for (int syni=0;syni<maxSyn;syni++) {
              Synopsis synopsis = programInformation.getBasicDescription().getSynopsis(syni);
              
              if (synopsis.getLength() == Synopsis.SHORT) {
                if ((synopsis.getLanguage() == null) || (synopsis.getLanguage().equals("en"))) {
                  String text = synopsis.getText();
                  shortBuffer.append(text).append("\n\n");
                  if (text.length() > MutableProgram.MAX_SHORT_INFO_LENGTH) {
                    longBuffer.append(text).append("\n\n");
                  }
                } else {
                  mLog.warning("Unsupported Language: " + synopsis.getLanguage());
                }
              } else {
                if ((synopsis.getLanguage() == null) || (synopsis.getLanguage().equals("en"))) {
                  longBuffer.append(synopsis.getText()).append("\n\n");
                } else {
                  mLog.warning("Unsupported Language: " + synopsis.getLanguage());
                }
              }
              
            }
            
            if (shortBuffer.length() > 0)
              prog.setShortInfo(shortBuffer.toString().trim());
            if (longBuffer.length() > 0)
              prog.setDescription(longBuffer.toString().trim());
            
            // Get Credits
            
            CreditsList credits = programInformation.getBasicDescription().getCreditsList();
            
            int cmax = credits.getNumCreditsItems();
            
            for (int ci = 0; ci< cmax;ci++){
              CreditsItem credit = credits.getCreditsItem(ci);
              
              if (credit.getRole() != null && credit.getRole().endsWith("DIRECTOR")) {
                String director = getPersonNames(credit);
                prog.setTextField(ProgramFieldType.DIRECTOR_TYPE, director);
              }
              else if (credit.getRole() != null && credit.getRole().endsWith("ACTOR")) {
                
                StringBuffer creditList = new StringBuffer();
                if (credit.getNumPersonNames() > 0) {
                  StringBuffer person = new StringBuffer();
                  
                  for (int cv =0;cv<credit.getNumCharacters();cv++) {
                    person.append(credit.getCharacter(cv).getName().trim());
                    person.append('/');
                  }
  
                  person = new StringBuffer(person.substring(0, person.length()-1));
                  
                  person.append('\t');
                  
                  for (int cv =0;cv<credit.getNumPersonNames();cv++) {
                    String name = credit.getPersonName(cv).getName().trim();
                    if (name.startsWith("..")) {
                      name = name.substring(2).trim();
                    }
                    person.append(name);
                    person.append('/');
                  }
  
                  creditList.append(person.substring(0, person.length()-1));
                }
                if (creditList.length() > 0) {
                  prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE, creditList.toString().trim());
                }
              }
/*
              else {
                System.out.println(credit.getRole());
              }
*/
              }

            
            // Genre
            int maxg = programInformation.getBasicDescription().getNumGenres();
            
            StringBuffer genrebuffer = new StringBuffer();
            
            for (int gi=0;gi<maxg;gi++) {
              genrebuffer.append(programInformation.getBasicDescription().getGenre(gi).getMPEG7Name(Genre.MAIN).trim()).append(", ");
            }

            if (genrebuffer.length() > 0) {
              String genre = genrebuffer.substring(0, genrebuffer.length()-2);
              
              Pattern pattern = Pattern.compile("\\b[ÄÖÜA-Z]([ÖÄÜA-Z]*)\\b");
              Matcher matcher = pattern.matcher(genre);
              
              StringBuilder builder = new StringBuilder();
              
              int pos = 0;
              
              while (matcher.find()) {
                builder.append(genre.substring(pos, matcher.start(1)));
                builder.append(genre.substring(matcher.start(1), matcher.end(1)).toLowerCase());
                pos = matcher.end(1);
              }
              builder.append(genre.substring(pos));
              
              prog.setTextField(ProgramFieldType.GENRE_TYPE, builder.toString());
            }
            
            // Close Caption
            int bitset = 0;
            
            if (programInformation.getBasicDescription().getNumCaptionLanguages() > 0) {
              bitset = bitset | Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED;
            }
            
            // Audio Format
            AVAttributes avattributes = programInformation.getAVAttributes();
            
            AudioAttributes audio = avattributes.getAudioAttributes();

            if (audio != null) {
              if (audio.getNumOfChannels() == 1) {
                bitset = bitset | Program.INFO_AUDIO_MONO;
              } else if (audio.getNumOfChannels() == 2) {
                bitset = bitset | Program.INFO_AUDIO_STEREO;
              } else if (audio.getNumOfChannels() > 2) {
                bitset = bitset | Program.INFO_AUDIO_DOLBY_SURROUND;
              }
            }
            
            // Video Format
            VideoAttributes video = avattributes.getVideoAttributes();
            
            if (video != null) {
              boolean wide = false;
              for (int vi =0;vi< video.getNumAspectRatios();vi++) {
                if (video.getAspectRatio(vi).getAspectRatio().equals("16:9")) {
                  wide = true;
                }
              }

              if (wide) {
                bitset = bitset | Program.INFO_VISION_16_TO_9;
              } else {
                bitset = bitset | Program.INFO_VISION_4_TO_3;
              }
            }
            
            if (bitset != 0) {
              prog.setInfo(bitset);
            }
            
            prog.setProgramLoadingIsComplete();
            mutablechanneldayprogram.addProgram(prog);
          }
          
        }
      }
    }
  }

  private String getPersonNames(CreditsItem credit) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < credit.getNumPersonNames(); i++) {
      if (i > 0) {
        result.append(", ");
      }
      result.append(credit.getPersonName(i).getName().trim());
    }
    return result.toString();
  }

}
