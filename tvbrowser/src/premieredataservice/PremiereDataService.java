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

package premieredataservice;

import java.io.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.regex.*;
import java.net.URL;

import util.exc.*;
import util.io.IOUtilities;
import util.tvdataservice.*;

import devplugin.*;
import tvdataservice.*;

/**
 * A data service for the german pay TV channels from Premiere.
 * <p>
 * Premiere offers its program for the next month
 * <a href="http://www.premiere.de/content/Programm_Film_Movieguide.jsp">here</a>
 * in a text format.
 * <p>
 * Currently the source deliveres the following programs:<br>
 * 13th Street, Beate-Uhse.TV, Classica, Disney Channel, Fox Kids, Heimatkanal,
 * Junior, MGM, Premiere 1, Premiere 2, Premiere 3, Premiere 4, Premiere 5,
 * Premiere 6, Premiere 7, Premiere Krimi, Premiere Nostalgie, Premiere Serie,
 * Premiere Start, Studio Universal
 *
 * @author Til Schneider, www.murfman.de
 */
public class PremiereDataService extends AbstractTvDataService {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(PremiereDataService.class);

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(PremiereDataService.class.getName());

  private static devplugin.PluginInfo INFO=new devplugin.PluginInfo(
        mLocalizer.msg("name", "Premiere data service"),
        "Die Sender von premiere (13th Street, Beate-Uhse.TV, Classica, Disney Channel, " +
        "Fox Kids, Heimatkanal, Junior, MGM, Premiere 1, Premiere 2, Premiere 3, " +
        "Premiere 4, Premiere 5, Premiere 6, Premiere 7, Premiere Krimi, " +
        "Premiere Nostalgie, Premiere Serie, Premiere Start, Studio Universal)",
        "Til Schneider",
        new Version(2,0)
  );

  /**
   * Creates a new instance of PremiereDataService.
   */
  public PremiereDataService() {
  }


  public devplugin.PluginInfo getInfo() {
    return INFO;
  }


  /**
   * Gets the default list of the channels that are available by this data
   * service.
   */
  protected Channel[] getDefaultAvailableChannels() {
    return new Channel[] {
      // main channels
      new Channel(this, "13th Street"),
      new Channel(this, "Beate-Uhse.TV"),
      new Channel(this, "Classica"),
      new Channel(this, "Disney Channel"),
      new Channel(this, "Fox Kids"),
      new Channel(this, "Heimatkanal"),
      new Channel(this, "Junior"),
      new Channel(this, "MGM"),
      new Channel(this, "Premiere 1"),
      new Channel(this, "Premiere 2"),
      new Channel(this, "Premiere 3"),
      new Channel(this, "Premiere 4"),
      new Channel(this, "Premiere 5"),
      new Channel(this, "Premiere 6"),
      new Channel(this, "Premiere 7"),
      new Channel(this, "Premiere Krimi"),
      new Channel(this, "Premiere Nostalgie"),
      new Channel(this, "Premiere Serie"),
      new Channel(this, "Premiere Start"),
      new Channel(this, "Studio Universal")
    };
  }



  /**
   * Gets the name of the directory where to download the data service specific
   * files.
   */
  protected String getDataDirectory() {
    return "premieredata";
  }



  /**
   * Gets the name of the file that contains the data of the specified date.
   */
  protected String getFileNameFor(Date date, Channel channel) {
    // Movie guide:  http://www.premiere.de/content/download/mguide_d_s_05_03.txt
    // Erotic guide: http://www.premiere.de/content/download/eguide_d_s_05_03.txt
    Calendar cal = date.getCalendar();
    int month = cal.get(Calendar.MONTH) + 1;
    int year = cal.get(Calendar.YEAR) % 100;

    // Check whether the channel is a channel of the erotic guide
    // TODO: a smarter implementation that detects the channels dynamically
    boolean isEroticGuide = false;
    if (channel.getName().equalsIgnoreCase("BEATE-UHSE.TV")) {
      isEroticGuide = true;
    }

    // Take the right prefix
    String fileNamePrefix;
    if (isEroticGuide) {
      fileNamePrefix = "eguide_d_s_";
    } else {
      fileNamePrefix = "mguide_d_s_";
    }

    return fileNamePrefix
      + ((month < 10) ? ("0" + month) : ("" + month))
      + "_"
      + ((year < 10) ? ("0" + year) : ("" + year))
      + ".txt";
  }



  /**
   * Downloads the file containing the data for the specified dat and channel.
   *
   * @param date The date to load the data for.
   * @param channel The channel to load the data for.
   * @param targetFile The file where to store the file.
   */
  protected void downloadFileFor(devplugin.Date date, Channel channel,
    File targetFile) throws TvBrowserException
  {
    String fileName = getFileNameFor(date, channel);
    String url = "http://www.premiere.de/content/download/" + fileName;
    try {
      IOUtilities.download(new URL(url), targetFile);
    }
    catch (Exception exc) {
     // ignore error
     // throw new TvBrowserException(getClass(), "error.1",
     //   "Error downloading '{0}' to '{1}'!", url, targetFile.getAbsolutePath(), exc);
    }
  }



  private String getString(String s) {
      try {
        return new String(s.getBytes(),"ISO-8859-1");
      }catch(UnsupportedEncodingException e) {
        return s;
      } 
    
    }



  /**
   * Parses the specified file.
   *
   * @param file The file to parse.
   * @param programDispatcher The ProgramDispatcher where to store the found
   *        programs.
   */
  protected void parseFile(File file, devplugin.Date date,
    Channel channel, ProgramDispatcher programDispatcher)
    throws TvBrowserException
  {
    HashSet knownChannelNameSet = new HashSet();

    FileReader fileReader = null;
    BufferedReader reader = null;
    int lineNr = -1;
    try {
      fileReader = new FileReader(file);      
      reader = new BufferedReader(fileReader);
    

      Pattern[] regexPatternArr = new Pattern[] {
        // Example: "STUDIO UNIVERSAL: 01.05./06:00"
        Pattern.compile("(.*): (\\d*)\\.(\\d*)\\./(\\d*):(\\d*)"),
        // Example: "Titel: Lieben Sie Brahms?"
        Pattern.compile("Titel: (.*)"),
        // Example: "Episode:  - Genre:  - Länge: 01:55 Stunden"
        Pattern.compile("Episode: (.*) - Genre: (.*) - L.nge: (\\d*):(\\d*) Stunden"),
        // Example: "Produktionsland: USA - Produktionsjahr: 1960 - Regie: Anatole Litvak"
        Pattern.compile("Produktionsland: (.*) - Produktionsjahr: (.*) - Regie: (.*)"),
        // Example: "Bild- und Tonformate: 4:3/Mono"
        Pattern.compile("Bild- und Tonformate: (.*)"),
        // Example: "Darsteller: Ingrid Bergman, Anthony Perkins , Clarke Jean , Pierre Dux"
        Pattern.compile("Darsteller: (.*)"),
        // Example: "Obwohl ihr Mann Roger (Yves Montand) immer wieder fremdgeht ..."
        Pattern.compile("(.*)")
      };

      Calendar cal = Calendar.getInstance();

      // Parse the file line by line
      String line;
      int progLine = 0; // The line within the current program
      lineNr = 1;
      MutableProgram currProgram = null;
      StringBuffer descriptionBuffer = null;
      StringBuffer additionalInfoBuffer = null;
      while ((line = reader.readLine()) != null) {
        // mLog.info("checking line " + lineNr + ", progLine: " + progLine
        //   + ": '" + line + "'");

        if (line.length() == 0) {
          // The current program is finished
          if (currProgram != null) {
            currProgram.setShortInfo(descriptionBuffer.toString());

            // Append the additional information
            descriptionBuffer.append("\n\n");
            descriptionBuffer.append(additionalInfoBuffer);
            currProgram.setDescription(descriptionBuffer.toString());

            programDispatcher.dispatch(currProgram);

            currProgram = null;
          }

          // Start a new program
          progLine = -1;
        } else {
          // This is one line of the current program
          if ((progLine != 0) && (currProgram == null)) {
            // This one line (but not the first) of a program that doesn't
            // interest us, because the channel is not wanted
            // -> stay in progLine 1 until there comes an empty line
            progLine = 1;
          } else {
            // This is either the first line of a new program
            // or some other line of a program that interest us

            // Get the right regex matcher
            Matcher matcher = regexPatternArr[progLine].matcher(line);
            if (!matcher.find()) {  // must find!
            	return;
            }

            if (progLine == 0) {
              currProgram = extractProgram(matcher, date, knownChannelNameSet, cal);
              if (currProgram != null) {
                descriptionBuffer = new StringBuffer();
                additionalInfoBuffer = new StringBuffer();
              }
            } else {
              computeLine(matcher, progLine, currProgram, descriptionBuffer,
                additionalInfoBuffer);

            }
          } // else block of if ((progLine != 0) && (currProgram == null))
        } // else block of if (line.length() == 0)

        if (progLine < regexPatternArr.length - 1) {
          // The next line may be another description line
          progLine++;
        }
        lineNr++;
      }

      // Copy the known channels into an array
      String[] knownChannelNameArr = new String[knownChannelNameSet.size()];
      knownChannelNameSet.toArray(knownChannelNameArr);

      // Sort the array
      Arrays.sort(knownChannelNameArr);

      // Print out the array
      String asString = "";
      for (int i = 0; i < knownChannelNameArr.length; i++) {
        if (i != 0) {
          asString += ", ";
        }
        asString += knownChannelNameArr[i];
      }
      mLog.info("Known channels: " + asString);
    }
    catch (Exception exc) {
      throw new TvBrowserException(getClass(), "error.2",
        "Error parsing premiere tv data file line {0}!\n('{1}')",
        new Integer(lineNr), file.getAbsolutePath(), exc);
    }
    finally {
      if (reader != null) {
        try { reader.close(); } catch (IOException exc) {}
      }
      if (fileReader != null) {
        try { fileReader.close(); } catch (IOException exc) {}
      }
    }
  }



  private MutableProgram extractProgram(Matcher matcher,
    devplugin.Date startDate, HashSet knownChannelNameSet, Calendar cal)
  {
    // regex: "([^:]*): (\\d*)\\.(\\d*)\\./(\\d*):(\\d*)"
    String channelName = matcher.group(1);
    String dayStr = matcher.group(2);
    String monthStr = matcher.group(3);
    String hoursStr = matcher.group(4);
    String minutesStr = matcher.group(5);

    knownChannelNameSet.add(channelName);
    Channel channel = getChannelForName(channelName);
    if (channel == null) {
      return null;
    }

    int day = Integer.parseInt(dayStr);
    int month = Integer.parseInt(monthStr);
    cal.set(Calendar.DAY_OF_MONTH, day);
    cal.set(Calendar.MONTH, month - 1);

/*
	int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
	int daylight = cal.get(Calendar.DST_OFFSET);
  
    int daysSince1970 = (int)((cal.getTimeInMillis()+zoneOffset+daylight) / 1000L / 60L / 60L / 24L);
  */  
    // TODO:
    /*
    // Only get the next few days (to avoid out of memory)
    int minDaysSince1970 = startDate.getDaysSince1970();
    int maxDaysSince1970 = minDaysSince1970;
    if ((daysSince1970 < minDaysSince1970) || (daysSince1970 > maxDaysSince1970)) {
      // This program doesn't interest us
      return null;
    }
    
    devplugin.Date progDate = new devplugin.Date(daysSince1970);
    */
    
    devplugin.Date progDate=new devplugin.Date(cal);
    
    int hours = Integer.parseInt(hoursStr);
    int minutes = Integer.parseInt(minutesStr);

    return new MutableProgram(channel, progDate, hours, minutes);
  }



  private Channel getChannelForName(String channelName) {
    Channel[] channelArr = getAvailableChannels();
    for (int i = 0; i < channelArr.length; i++) {
      if (channelArr[i].getName().equalsIgnoreCase(channelName)) {
        return channelArr[i];
      }
    }

    return null;
  }



  private void computeLine(Matcher matcher, int progLine,
    MutableProgram currProgram, StringBuffer descriptionBuffer,
    StringBuffer additionalInfoBuffer)
  {
    switch(progLine) {
      case 1: {
        // regex: "Titel: (.*)"
        currProgram.setTitle(getString(matcher.group(1)));
      } break;

      case 2: {
        // regex: "Episode: (.*) - Genre: (.*) - Länge: (\\d*):(\\d*) Stunden"
        String episode = getString(matcher.group(1));
        String genre = getString(matcher.group(2));
        String lengthHoursStr = matcher.group(3);
        String lengthMinutesStr = matcher.group(4);

        if (episode.length() > 0) {
          additionalInfoBuffer.append(mLocalizer.msg("episode", "Episode:") + " ");
          additionalInfoBuffer.append(episode + "\n");
        }
        if (genre.length() > 0) {
          additionalInfoBuffer.append(mLocalizer.msg("genre", "Genre:") + " ");
          additionalInfoBuffer.append(genre + "\n");
        }

        int lengthHours = Integer.parseInt(lengthHoursStr);
        int lengthMinutes = Integer.parseInt(lengthMinutesStr);
        currProgram.setLength(lengthHours * 60 + lengthMinutes);
      } break;

      case 3: {
        // regex: "Produktionsland: (.*) - Produktionsjahr: (.*) - Regie: (.*)"
        String country = getString(matcher.group(1));
        String year = getString(matcher.group(2));
        String direction = getString(matcher.group(3));

        if (country.length() > 0) {
          additionalInfoBuffer.append(mLocalizer.msg("country", "Production country:") + " ");
          additionalInfoBuffer.append(country + "\n");
        }
        if (year.length() > 0) {
          additionalInfoBuffer.append(mLocalizer.msg("year", "Production year:") + " ");
          additionalInfoBuffer.append(year + "\n");
        }
        if (direction.length() > 0) {
          additionalInfoBuffer.append(mLocalizer.msg("direction", "Direction:") + " ");
          additionalInfoBuffer.append(direction + "\n");
        }
      } break;

      case 4: {
        // regex: "Bild- und Tonformate: (.*)"
        String formats = getString(matcher.group(1));
        int info = extractInfo(formats);
        currProgram.setInfo(info);
      } break;

      case 5: {
        // regex: "Darsteller: (.*)"
        String actors = getString(matcher.group(1));

        currProgram.setActors(actors);
      } break;

      case 6: {
        // regex: ""

        // Add the line to the description until there is an empty line
        descriptionBuffer.append(getString(matcher.group(1)));
      } break;
    } // switch(progLine)
  }

  
  
  private int extractInfo(String formats) {
    int info = 0;
    
    StringTokenizer tokenizer = new StringTokenizer(formats, "/");
    while (tokenizer.hasMoreTokens()) {
      String format = tokenizer.nextToken();
      
      if (format.equalsIgnoreCase("k.A.")) {
        // noop
      }
      else if (format.equalsIgnoreCase("4:3")) {
        info |= Program.INFO_VISION_4_TO_3;
      }
      else if (format.equalsIgnoreCase("16:9")) {
        info |= Program.INFO_VISION_16_TO_9;
      }
      else if (format.equalsIgnoreCase("Mono")) {
        info |= Program.INFO_AUDIO_MONO;
      }
      else if (format.equalsIgnoreCase("Stereo")) {
        info |= Program.INFO_AUDIO_STEREO;
      }
      else if (format.equalsIgnoreCase("Dolby Surround")) {
        info |= Program.INFO_AUDIO_DOLBY_SURROUND;
      }
      else if (format.equalsIgnoreCase("Dolby Digital 5.1")) {
        info |= Program.INFO_AUDIO_DOLBY_DIGITAL_5_1;
      }
      else if (format.equalsIgnoreCase("Zweikanalton")) {
        info |= Program.INFO_AUDIO_TWO_CHANNEL_TONE;
      }
      else {
        mLog.info("Unknown vision or audio format: '" + format + "'");
      }
    }
    
    return info;
  }
  
}
