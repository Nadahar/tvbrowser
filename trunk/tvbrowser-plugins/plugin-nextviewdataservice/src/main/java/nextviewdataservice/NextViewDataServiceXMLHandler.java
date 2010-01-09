/*
 * NextViewDataService Plugin by Andreas Hessel (Vidrec@gmx.de)
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
 */
package nextviewdataservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FileInputStream;

import java.util.Calendar;
import java.util.Properties;
import java.util.Scanner;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import tvdataservice.MutableProgram;

import devplugin.ProgramFieldType;
import devplugin.Channel;
import devplugin.Date;

/**
 * XML Handler to parse nxtvepg's program data 
 * @author jb
 */
public class NextViewDataServiceXMLHandler extends DefaultHandler {

  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(NextViewDataServiceXMLHandler.class.getName());
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(NextViewDataServiceXMLHandler.class);

  private StringBuffer characters;
  private NextViewDataServiceData data;
  private String currentChannelId;
  private MutableProgram currentProgram;
  private String currentSubtitles;
  private Properties cniMappings = new Properties();
  private String globalTimeZone = "+0100";
  private int version = (tvbrowser.TVBrowser.VERSION.getMajor()*100) +  tvbrowser.TVBrowser.VERSION.getMinor();
  private Date yesterday;
  private boolean isUpdateProgram;



  /**
   * Initializes a new instance of NextViewDataServiceXMLHandler
   * @param data ; the channel&program container of thi data service
   */
  NextViewDataServiceXMLHandler(NextViewDataServiceData data) {
    this.data = data;
    this.characters = new StringBuffer();
    try {
      cniMappings.load(new FileInputStream(NextViewDataService.getInstance().mDataDir.toString() + "/cni.map.properties"));
    } catch (IOException e) {
    }
    this.yesterday = Date.getCurrentDate().addDays(-1);

  }

  /*
   * (non-Javadoc)
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   * 
   * The characters method reports each chunk of character 
   * data and appends it to the characters buffer.
   */
  @Override
  public void characters(char[] buf, int offset, int len) throws SAXException {
    characters.append(buf, offset, len);
  }

  /*
   * (non-Javadoc)
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   * 
   * if start tag is
   * channel:   keep channel id
   * programme: add new program to program list of the data service
   *            setting start time, length, VPS,
   * subtitles: keep subtitles
   * tv:		keep timezone of file creation           
   * 
   */
  @Override
  public void startElement(String namespaceURI, String simpleName,
      String qualifiedName, Attributes attrs) throws SAXException {
    String currentElement = simpleName;
    if (currentElement.equals("")) {
      currentElement = qualifiedName;
    }

    try {
      if (currentElement.equals("channel")) {
        // read channel information
        currentChannelId = attrs.getValue("id");
        String newChannelId = cniMappings.getProperty(currentChannelId);
        if (newChannelId != null) {
          currentChannelId = newChannelId.split(",", 2)[0];
        }

      } else if (currentElement.equals("programme")) {
        // read information for a single program
        String start = attrs.getValue("start");
        String stop = attrs.getValue("stop");
        String channelId = attrs.getValue("channel");
        String newChannelId = cniMappings.getProperty(channelId);
        if (newChannelId != null) {
          channelId = newChannelId.split(",", 2)[0];
        }
        Channel channel = data.getChannel(channelId);

        if ((start.length() >= 12) && (channel != null)) {
          Date mDate = extractDate(start);

          int length = 0;

          if (stop!=null && (stop.length() >= 12) ) {
            if (extractTime(stop) > extractTime(start)) {
              length = extractTime(stop) - extractTime(start);
            } else {
              length = extractTime(stop) + 1440 - extractTime(start);
            }
          }

          // customize program start time at the beginning and the end of DST
          if (!globalTimeZone.equals(start.split(" ",2)[1])){
            start = getTransTime (mDate, start);
            mDate = extractDate(start);
          }

          if (mDate.compareTo(yesterday)>=0 ) {
            isUpdateProgram = true;
            currentProgram = new MutableProgram(channel, mDate, extractHour(start), extractMinutes(start), true);
            currentProgram.setLength(length);

            // customize VPS at the beginning and the end of DST
            String pdc = attrs.getValue("pdc-start");
            currentSubtitles = "";
            if (pdc != null && pdc.length() >= 12) {
              if (!globalTimeZone.equals(pdc.split(" ",2)[1])){
                pdc = getTransTime (mDate, pdc);
              }
              int vpsTime = extractTime(pdc);
              currentProgram.setTimeField(ProgramFieldType.VPS_TYPE, vpsTime);
            }
          } else {
            isUpdateProgram = false;
          }
        }
      } else {
          if (currentElement.equals("subtitles")&& isUpdateProgram) {
            currentSubtitles = attrs.getValue("type");
          } else
            if (currentElement.equals("tv")) {
              // Get timezone during data export to compare with 
              // timezone of program information at DST change.
              String nxtvTime = attrs.getValue("date");
              if (nxtvTime != null) {
                String nxtvZone = nxtvTime.split(" ", 2)[1];
                if (nxtvZone.length() == 5) {
                  globalTimeZone = nxtvZone;
              }
            }
        }
      }


    } catch (Exception e) {
      mLog.warning(e.toString());

    }

    characters.delete(0, characters.length());
  }

  /*
   * (non-Javadoc)
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   * 
   * if end tag is
   * channel:	  add channel to channel list of this program
   * programme:   add info bits depending on the genre info
   *              dispatch program in the AbstractChannelDayProgram
   * title:		  add title to program
   * category:    add genre information to program
   * desc:		  add program description to program
   * aspect:      if given, set INFO_VISION_16_TO_9 bit of program
   * stereo:	  if given, set stereo or surround bit of program
   * star-rating: add rating to the program
   * rating:      add agelimit to the programm
   * subtitles:   if telelext, set INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED bit of program          	
   */
  @Override
  public void endElement(String namespaceURI, String simpleName,
      String qualifiedName) throws SAXException {
    String currentElement = simpleName;
    if (currentElement.equals("")) {
      currentElement = qualifiedName;
    }

    try {
      if (currentElement.equals("channel")) {
        String channelName = characters.toString();
        if ((channelName != null) && (currentChannelId != null)) {
          data.addChannel(currentChannelId, channelName.trim());

        }
        currentChannelId = null;
      } else {
        if (isUpdateProgram) {
          if (currentElement.equals("programme")) {

            String categories = currentProgram.getTextField(ProgramFieldType.GENRE_TYPE);


            if (categories == null || categories.equals("")) {
              categories = "n.n.";
            }


            if (version >= 260) {
              if (categories.contains("Show") || categories.contains("show")) {
                currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_CATEGORIE_SHOW, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
              }
              if (categories.contains("magazin") || categories.contains("Magazin")) {
                currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
              }
              if (categories.equals("Nachrichten")||categories.contains("Nachrichten,")) {
                currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_CATEGORIE_NEWS, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
              }
              if (categories.contains("Serie") || categories.contains("serie")) {
                currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_CATEGORIE_SERIES, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
              }
              if (categories.contains("Kinder")) {
                currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_CATEGORIE_CHILDRENS, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
              }
              if (categories.contains("Live")) {
                currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_LIVE, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
              }
              if (categories.contains("Spielfilm") || categories.contains("spielfilm")) {
                currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_CATEGORIE_MOVIE, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
              }
              if (version >= 270) {
                if (categories.contains("Doku") || categories.contains("doku")||categories.contains("Reportage") || categories.contains("reportage")) {
                  currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_CATEGORIE_MOVIE, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
                }
                if (categories.contains("Oper") || categories.contains("Ballet")|| categories.contains("Theater")||categories.contains("Konzert")) {
                  currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_CATEGORIE_ARTS, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
                }
                if (categories.contains("Sport") || categories.contains("sport")) {
                  currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_CATEGORIE_SPORTS, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
                }
                if (categories.contains("Kinder")) {
                  currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_CATEGORIE_CHILDRENS, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
                }
              }
            }
            currentProgram.setProgramLoadingIsComplete();
            data.getDispatcher().dispatch(currentProgram);

          } else if (currentElement.equals("title")) {
            String title = characters.toString();
            if (currentProgram != null) {
              // special arte HD treatment
              if (currentProgram.getChannel().getId().equals("CNI0D85")&&title.endsWith(" (hd)")&& version >= 270){
                title = title.substring(0, title.length()-5);
                currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_VISION_HD, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
              }
              currentProgram.setTitle(title);
            }
          } else if (currentElement.equals("category")) {
            String category = characters.toString();
            if (category.contains("/")){
              String []cats = category.split("/");
              StringBuffer catBuffer = new StringBuffer(cats[0]);
              for (int i=1; i<cats.length; i++){
                catBuffer.append(", " + cats[i]);
              }
              category = catBuffer.toString();
            }
            if (currentProgram != null) {
              if (currentProgram.getTextField(ProgramFieldType.GENRE_TYPE) != null) {
                category = currentProgram.getTextField(ProgramFieldType.GENRE_TYPE) + ", " + category;
              }

              category = HelperMethods.cleanUpCategories (category);

              currentProgram.setTextField(ProgramFieldType.GENRE_TYPE, category);

            }

          } else if (currentElement.equals("desc")) {
            if (currentProgram != null) {
              String desc = characters.toString();
              // special arte HD treatment
              if (currentProgram.getChannel().getId().equals("CNI0D85")&& desc.contains(" (HD)") && version >= 270){
                desc = desc.replace(" (HD)", "");
                currentProgram.setInfo(HelperMethods.mixInfoBits(MutableProgram.INFO_VISION_HD, currentProgram.getIntField(ProgramFieldType.INFO_TYPE)));
              }
              Scanner descElement = new Scanner(desc);
              String testLine = "";
              String description = "";
              String shortDesc = "";

              // Read 3 Lines
              shortDesc = descElement.nextLine();

              if (descElement.hasNextLine()) {
                testLine = descElement.nextLine();
                if (descElement.hasNextLine()) {
                  description = descElement.nextLine();
                }
              }

              // read all the rest
              while (descElement.hasNextLine()) {
                if (description.equals("")) {
                  description = descElement.nextLine();
                } else {
                  description = description + System.getProperty("line.separator") + descElement.nextLine();
                }
              }

              // if we have at least 3 lines, and line 2 is empty, we have a short description in line 1
              if (testLine.equals("") && !description.equals("") && shortDesc.length() <= MutableProgram.MAX_SHORT_INFO_LENGTH) {
                currentProgram.setTextField(ProgramFieldType.SHORT_DESCRIPTION_TYPE, shortDesc);
              } else {
                if (!testLine.equals("")) {
                  description = testLine + System.getProperty("line.separator") + description;
                }
                if (!shortDesc.equals("")) {
                  description = shortDesc + System.getProperty("line.separator") + description;
                }
              }


              currentProgram.setTextField(ProgramFieldType.DESCRIPTION_TYPE, description);
            }
          } else if (currentElement.equals("aspect")) {
            String aspect = characters.toString();
            if (currentProgram != null) {
              if (aspect.equals("16:9")) {
                addInfoBit(MutableProgram.INFO_VISION_16_TO_9);
              }
            }
          } else if (currentElement.equals("stereo")) {
            String stereo = characters.toString();
            if (currentProgram != null) {
              if (stereo.equals("stereo")) {
                addInfoBit(MutableProgram.INFO_AUDIO_STEREO);
              }
              if (stereo.equals("surround")) {
                addInfoBit(MutableProgram.INFO_AUDIO_DOLBY_SURROUND);
              }
            }
          } else if (currentElement.equals("star-rating")) {
            String starRating = characters.toString().trim();
            starRating = "Bewertung: " + starRating;

            if (currentProgram != null) {
              if (currentProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE) != null) {
                starRating = currentProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE) + System.getProperty("line.separator") + System.getProperty("line.separator") + starRating;
              }
              currentProgram.setTextField(ProgramFieldType.DESCRIPTION_TYPE, starRating);
            }
          } else if (currentElement.equals("rating")) {
            String rating = characters.toString().trim();
            int ageLimit = 0;
            boolean isInteger = false;

            if (currentProgram != null) {

              try {
                ageLimit = Integer.parseInt(rating);
                isInteger = true;
              } catch (NumberFormatException nfe) {
              }
            }
            if (isInteger) {
              currentProgram.setIntField(ProgramFieldType.AGE_LIMIT_TYPE, ageLimit);
            } else {
              if (rating.equals("general")) {
                rating = mLocalizer.msg("noAgeLimit", "no age limit");
              } else {
                rating = mLocalizer.msg("ageLimit", "age limit")+ ": " + rating;
              }
              if (currentProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE) != null) {
                rating = currentProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE) + System.getProperty("line.separator") + System.getProperty("line.separator") + rating;
              }
              currentProgram.setTextField(ProgramFieldType.DESCRIPTION_TYPE, rating);
            }

          } else if (currentElement.equals("subtitles")) {
            if (currentProgram != null) {
              if (currentSubtitles.equals("teletext")) {
                addInfoBit(MutableProgram.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED);
              } else {
                if (currentProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE) == null) {
                  currentProgram.setTextField(ProgramFieldType.DESCRIPTION_TYPE, "Untertitel: " + currentSubtitles);
                } else {
                  currentProgram.setTextField(ProgramFieldType.DESCRIPTION_TYPE, currentProgram.getTextField(ProgramFieldType.DESCRIPTION_TYPE) + System.getProperty("line.separator") + System.getProperty("line.separator") + "Untertitel: " + currentSubtitles);
                }
              }
            }
          }

        }
      }

    } catch (Exception e) {
      mLog.warning(e.toString());
    }
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId) {
    return new InputSource(new ByteArrayInputStream("<!-- -->".getBytes()));
  }


  /** 
   * Extracts the date information from a XMLTV time string.
   *
   * @param value ; the string to extract the date from.
   * @return the date (as Date object)
   * @throws IOException If the value has the wrong format.
   */
  private Date extractDate(String value) throws IOException {
    // E.g. "200407101030"
    try {
      int year = Integer.parseInt(value.substring(0, 4));
      int month = Integer.parseInt(value.substring(4, 6));
      int day = Integer.parseInt(value.substring(6, 8));
      return new Date(year, month, day);
    } catch (Throwable thr) {
      throw new IOException("Illegal time value: '" + value + "'");
    }
  }

  /**
   *  Extracts the time from a XMLTV time string.
   *
   * @param value ; the string to extract the time from.
   * @return the time of the day as int
   * @throws IOException if the time string has the wrong format.
   */
  private int extractTime(String value) throws IOException {
    // E.g. "200407101030"
    try {
      int hour = Integer.parseInt(value.substring(8, 10));
      int minute = Integer.parseInt(value.substring(10, 12));
      return hour * 60 + minute;
    } catch (Throwable thr) {
      throw new IOException("Illegal time value: '" + value + "'");
    }
  }

  /**
   * Extracts the hour information from a XMLTV time value.
   * @param value ; the calue 
   * @return the hour of the day as int
   * @throws IOException
   */
  private int extractHour(String value) throws IOException {
    // E.g. "200407101030"
    try {
      int hour = Integer.parseInt(value.substring(8, 10));
      return hour;
    } catch (Throwable thr) {
      throw new IOException("Illegal time value: '" + value + "'");
    }
  }

  /**
   * Extracts the minutes information from a XMLTV time string.
   * @param value ; the time string given
   * @return the minutes of the hour as int
   * @throws IOException
   */
  private int extractMinutes(String value) throws IOException {
    // E.g. "200407101030"
    try {
      int minute = Integer.parseInt(value.substring(10, 12));
      return minute;
    } catch (Throwable thr) {
      throw new IOException("Illegal time value: '" + value + "'");
    }
  }

  /**
   * Customizes a time string if timezone of file creation and program info differ
   * @param mDate ; the date of the program info
   * @param timeString ; the XMLTV time string from the program info;
   * @return the customized time as XMLTV time string
   */
  private String getTransTime (Date mDate, String timeString){
    try {
      Calendar cal = mDate.getCalendar();
      cal.set(Calendar.HOUR_OF_DAY, extractHour(timeString));
      cal.set(Calendar.MINUTE, extractMinutes(timeString));
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);

      String localTimeZone = timeString.split(" ",2)[1];
      int timeDiff = getZoneInt(localTimeZone) - getZoneInt(globalTimeZone);
      int diffHours = timeDiff /60;
      cal.add(Calendar.HOUR_OF_DAY, diffHours);
      cal.add(Calendar.MINUTE, timeDiff - (diffHours*60));

      timeString = Integer.toString(cal.get(Calendar.YEAR))+normalize(Integer.toString(cal.get(Calendar.MONTH)+1))+normalize(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)))+normalize(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)))+normalize(Integer.toString(cal.get(Calendar.MINUTE)))+"00";

    } catch (IOException e) {
      mLog.warning("Illegal time value: '" + timeString + "'; " + e.getMessage());	}
    return timeString;
  }

  /**
   * Expands hour and time to 2 digits
   * @param value ; the input string
   * @return normalized string
   */
  private String normalize (String value){
    if (value.length()<2)
      value = "0"+value;
    return value;
  }

  /**
   * Transform XMLTV timezone string to integer
   * @param timeZone ; as XMLTV string
   * @return timeZone as int
   */
  private int getZoneInt (String timeZone){
    int hour = Integer.parseInt(timeZone.substring(1, 3));
    int minute = Integer.parseInt(timeZone.substring(3, 5));
    int sign = 1;
    if (timeZone.substring(0, 1).equals("-")){
      sign = -1;
    }
    return sign * hour * 60 + minute;
  }

  /**
   * helper method to collect program info bits
   * @param bit
   */
  private void addInfoBit(int bit) {
    if (currentProgram.getInfo() == -1) {
      currentProgram.setInfo(bit);
    } else {
      currentProgram.setInfo(bit + currentProgram.getInfo());
    }
  }


  @Override
  public void fatalError(SAXParseException e) {
    mLog.warning(e.toString());
  }

  @Override
  public void error(SAXParseException e) {
    mLog.warning(e.toString());
  }

  @Override
  public void warning(SAXParseException e) {
    mLog.warning(e.toString());
  }
}
