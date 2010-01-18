/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *     $Date: 2008-05-27 21:09:30 +0200 (Di, 27 Mai 2008) $
 *   $Author: troggan $
 * $Revision: 4740 $
 */
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import primarydatamanager.primarydataservice.ProgramFrameDispatcher;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.File;

import tvbrowserdataservice.file.ProgramFrame;
import tvbrowserdataservice.file.ProgramField;
import devplugin.Date;
import devplugin.Channel;
import devplugin.ProgramFieldType;
import devplugin.Program;
import util.io.IOUtilities;
import util.io.FileFormatException;

/**
 * A handler that parses TV data from XML.
 *
 * ToDo: The following Elements must be implemented:
 *  - EPISODE_NUMBER_TYPE
 *  - EPISODE_TOTAL_NUMBER_TYPE
 *  - SEASON_NUMBER_TYPE
 *  - CAMERA_TYPE
 *  - CUTTER_TYPE
 *  - ADDITIONAL_PERSONS_TYPE
 *  - PROCUCTION_COMPANY_TYPE
 *
 *  - INFO_CATEGORIE_MOVIE
 *  - INFO_CATEGORIE_SERIES
 *  - INFO_CATEGORIE_NEWS
 *  - INFO_CATEGORIE_SHOW
 *  - INFO_CATEGORIE_MAGAZINE_INFOTAINMENT
 *  - INFO_CATEGORIE_DOCUMENTARY
 *  - INFO_CATEGORIE_ARTS
 *  - INFO_CATEGORIE_SPORTS
 *  - INFO_CATEGORIE_CHILDRENS
 *  - INFO_CATEGORIE_OTHERS
 */
class XmlTvDataHandler extends DefaultHandler {

  /**
   * The program dispatchers. (key: channel id, value: ProgramFrameDispatcher)
   */
  private HashMap<String, ProgramFrameDispatcher> mDispatcherHash;

  /**
   * Holds the text of the current tag.
   */
  private StringBuffer mText;

  /**
   * The value of the attribute 'lang' of the current tag.
   */
  private String mLang;

  /**
   * The country of the current ProgramFrame's channel.
   */
  private String mChannelCountry;

  /**
   * The current ProgramFrame.
   */
  private ProgramFrame mFrame;

  /**
   * The date of the current ProgramFrame
   */
  private Date mDate;

  /**
   * The channel ID of the current ProgramFrame
   */
  private String mChannelId;

  /**
   * The PDS that is used in this handler
   */
  private XmlTvPDS mXmlTvPDS;

  /**
   * RegEx-Pattern for the Actor
   */
  private Pattern mActorPattern = Pattern.compile("(.*)\\((.*)\\)");
  /**
   * Value for some elements (e.g. star-rating)
   */
  private String mValue = null;

  /**
   * Creates a new instance of TvDataHandler.
   * @param xmlTvPDS The PDS that is used for the handler
   */
  public XmlTvDataHandler(XmlTvPDS xmlTvPDS) {
    this.mXmlTvPDS = xmlTvPDS;
    mDispatcherHash = new HashMap<String, ProgramFrameDispatcher>();
    mText = new StringBuffer();
  }


  /**
   * Handles the occurence of tag text.
   */
  public void characters(char ch[], int start, int length)
      throws SAXException {
    // There is some text -> Add it to the text buffer
    mText.append(ch, start, length);
  }

  private ProgramFrameDispatcher getProgramDispatcher(String channelId) {
    ProgramFrameDispatcher dispatcher = mDispatcherHash.get(channelId);
    if (dispatcher == null) {
      String[] s = channelId.split("_");
      Channel channel;
      if (s.length == 2) {
        channel = new Channel(s[0], s[1]);
      } else {
        channel = new Channel(s[0]);
      }
      dispatcher = new ProgramFrameDispatcher(channel);
      mDispatcherHash.put(channelId, dispatcher);
    }
    return dispatcher;
  }


  /**
   * Handles the occurence of a start tag.
   */
  public void startElement(String uri, String localName, String qName,
                           Attributes attributes)
      throws SAXException {
    // A new tag begins -> Clear the text buffer
    clear(mText);

    // Set the lang
    mLang = attributes.getValue("lang");

    // Spezial tag treatment
    if (qName.equals("programme")) {
      String start = attributes.getValue("start");
      mChannelId = attributes.getValue("channel");
      if (start == null) {
        mXmlTvPDS.logMessage("Start time missing in programme tag");
      } else if (mChannelId == null) {
        mXmlTvPDS.logMessage("Channel missing in programme tag");
      } else {
        ProgramFrameDispatcher dispatcher = getProgramDispatcher(mChannelId);
        mChannelCountry = dispatcher.getChannel().getCountry();
        try {
          mDate = extractDate(start);

          mFrame = new ProgramFrame();
          addField(ProgramField.create(ProgramFieldType.START_TIME_TYPE,extractTime(start)));
          
          String vps = attributes.getValue("vps-start");
          if (vps != null) {
            int time = extractTime(vps);
            addField(ProgramField.create(ProgramFieldType.VPS_TYPE, time));
          }

          String stop = attributes.getValue("stop");
          if (stop != null) {
            mFrame.addProgramField(ProgramField.create(ProgramFieldType.END_TIME_TYPE,
                extractTime(stop)));
          }
        }
        catch (IOException exc) {
          mXmlTvPDS.logException(exc);
          mFrame = null; // This frame is invalid
        }
      }
    } else if (qName.equals("previously-shown")) {
      try {
        Date prevDate = extractDate(attributes.getValue("start"));
        addField(ProgramField.create(ProgramFieldType.REPETITION_OF_TYPE, prevDate.toString()));
      }
      catch (IOException exc) {
        mXmlTvPDS.logException(exc);
      }
    } else if (qName.equals("next-time-shown")) {
      try {
        Date nextDate = extractDate(attributes.getValue("start"));
        addField(ProgramField.create(ProgramFieldType.REPETITION_ON_TYPE, nextDate.toString()));
      }
      catch (IOException exc) {
        mXmlTvPDS.logException(exc);
      }
    }

  }


  /**
   * Handles the occurence of an end tag.
   */
  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    if (mFrame != null) {
      String text = mText.toString().trim();

      if (qName.equals("title")) {
        if ((mLang == null) || mLang.equals(mChannelCountry)) {
          addField(ProgramField.create(ProgramFieldType.TITLE_TYPE, text));
        } else {
          addField(ProgramField.create(ProgramFieldType.ORIGINAL_TITLE_TYPE, text));
        }
      } else if (qName.equals("sub-title")) {
        if ((mLang == null) || mLang.equals(mChannelCountry)) {
          addField(ProgramField.create(ProgramFieldType.EPISODE_TYPE, text));
        } else {
          addField(ProgramField.create(ProgramFieldType.ORIGINAL_EPISODE_TYPE, text));
        }
      } else if (qName.equals("desc")) {
        addField(ProgramField.create(ProgramFieldType.DESCRIPTION_TYPE, text));
      } else if (qName.equals("date")) {
        if (text.length() < 4) {
          mXmlTvPDS.logMessage("WARNING: The date value must have at least 4 chars: '"
              + text + "'");
        } else {
          int year = Integer.parseInt(text.substring(0, 4));
          addField(ProgramField.create(ProgramFieldType.PRODUCTION_YEAR_TYPE, year));
        }
      } else if (qName.equals("rating")) {
        try {
          int ageLimit = Integer.parseInt(text);
          addField(ProgramField.create(ProgramFieldType.AGE_LIMIT_TYPE, ageLimit));
        }
        catch (NumberFormatException exc) {
          mXmlTvPDS.logMessage("WARNING: rating is no number: '" + text + "' and will be ignored.");
        }
      } else if (qName.equals("url")) {
        addField(ProgramField.create(ProgramFieldType.URL_TYPE, text));
      } else if (qName.equals("category")) {
        addField(ProgramField.create(ProgramFieldType.GENRE_TYPE, text));
      } else if (qName.equals("country")) {
        addField(ProgramField.create(ProgramFieldType.ORIGIN_TYPE, text));
      } else if (qName.equals("subtitles")) {
        if ((mLang == null) || mLang.equals(mChannelCountry)) {
          addInfoBit(Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED);
        } else {
          addInfoBit(Program.INFO_ORIGINAL_WITH_SUBTITLE);
        }
      } else if (qName.equals("live")) {
        addInfoBit(Program.INFO_LIVE);
      } else if (qName.equals("length")) {
        try {
          int length = Integer.parseInt(text);
          addField(ProgramField.create(ProgramFieldType.NET_PLAYING_TIME_TYPE, length));
        }
        catch (NumberFormatException exc) {
          mXmlTvPDS.logMessage("WARNING: length is no number: '" + text + "' and will be ignored.");
        }
      } else if (qName.equals("actor")) {
        Matcher m = mActorPattern.matcher(text);
        if (m.matches()) {
          text = m.group(2).trim() + "\t\t-\t\t" + m.group(1).trim();
        }

        addToList(ProgramFieldType.ACTOR_LIST_TYPE, text, true);
      } else if (qName.equals("director")) {
        addToList(ProgramFieldType.DIRECTOR_TYPE, text, false);
      } else if (qName.equals("writer")) {
        addToList(ProgramFieldType.SCRIPT_TYPE, text, false);
      } else if (qName.equals("presenter")) {
        addToList(ProgramFieldType.MODERATION_TYPE, text, false);
      } else if (qName.equals("music")) {
        addToList(ProgramFieldType.MUSIC_TYPE, text, false);
      } else if (qName.equals("producer")) {
        addToList(ProgramFieldType.PRODUCER_TYPE, text, false);
      } else if (qName.equals("colour")) {
        if (text.equals("no")) {
          addInfoBit(Program.INFO_VISION_BLACK_AND_WHITE);
        } else if (!text.equals("yes")) {
          mXmlTvPDS.logMessage("WARNING: value of colour tag must be 'yes' or 'no',"
              + " but it is '" + text + "'");
        }
      } else if (qName.equals("quality")) {
        if (text.equals("HDTV")) {
          addInfoBit(Program.INFO_VISION_HD);
        } else {
          mXmlTvPDS.logMessage("WARNING: value of quality tag must be 'HDTV' but it is '" + text + "'");
        }
      } else if (qName.equals("aspect")) {
        if (text.equals("4:3")) {
          addInfoBit(Program.INFO_VISION_4_TO_3);
        } else if (text.equals("16:9")) {
          addInfoBit(Program.INFO_VISION_16_TO_9);
        } else {
          mXmlTvPDS.logMessage("WARNING: value of aspect tag must be '4:3' or '16:9',"
              + " but it is '" + text + "'");
        }
      } else if (qName.equals("stereo")) {
        if (text.equals("mono")) {
          addInfoBit(Program.INFO_AUDIO_MONO);
        } else if (text.equals("stereo")) {
          addInfoBit(Program.INFO_AUDIO_STEREO);
        } else if (text.equals("surround")) {
          addInfoBit(Program.INFO_AUDIO_DOLBY_SURROUND);
        } else if (text.equals("5.1")) {
          addInfoBit(Program.INFO_AUDIO_DOLBY_DIGITAL_5_1);
        } else if (text.equals("two channel tone")) {
          addInfoBit(Program.INFO_AUDIO_TWO_CHANNEL_TONE);
        } else if (text.equals("audio description")) {
          addInfoBit(Program.INFO_AUDIO_DESCRIPTION);
        } else {
          mXmlTvPDS.logMessage("WARNING: value of stereo tag must be one of 'mono', "
              + "'stereo', 'surround', '5.1' or 'two channel tone' but it is '"
              + text + "'");
        }
      } else if (qName.equals("picture")) {
        File file = new File(text);
        if (file.exists() && file.isFile()) {
          try {
            addField(ProgramField.create(ProgramFieldType.PICTURE_TYPE, IOUtilities.getBytesFromFile(file)));
          } catch (IOException e) {
            mXmlTvPDS.logException(e);
          }
        } else
          mXmlTvPDS.logMessage("Warning: File does not exist: " + text);
      } else if (qName.equals("picture-copyright")) {
        addField(ProgramField.create(ProgramFieldType.PICTURE_COPYRIGHT_TYPE, text));
      } else if (qName.equals("picture-description")) {
        addField(ProgramField.create(ProgramFieldType.PICTURE_DESCRIPTION_TYPE, text));
      } else if (qName.equals("value")) {
        mValue = text;
      } else if (qName.equals("star-rating")) {
        if (mValue != null) {

          if (mValue.contains("/")) {
            try {
              int num = Integer.parseInt(mValue.substring(0, mValue.indexOf('/')).trim());
              int max = Integer.parseInt(mValue.substring(mValue.indexOf('/')+1).trim());
              addField(ProgramField.create(ProgramFieldType.RATING_TYPE, (int) (num / (float) max * 100)));
            } catch (NumberFormatException ex) {
              mXmlTvPDS.logException(ex);
            }
          } else {
            mXmlTvPDS.logMessage("Star-rating must be in form 8/10");
          }

        }

        mValue = null;
      } else if (qName.equals("new")) {
        addInfoBit(Program.INFO_NEW);
      } else if (qName.equals("programme")) {
        ProgramFrameDispatcher dis
            = mDispatcherHash.get(mChannelId);
        if (dis != null) {
          dis.dispatchProgramFrame(mFrame, mDate);
        }

        mFrame = null;
      }
    }

    // Clear lang
    mLang = null;
  }


  /**
   * Extracts the time from a XMLTV time value.
   *
   * @param value The value to extract the time from.
   * @return The time.
   * @throws java.io.IOException If the value has the wrong format.
   */
  private int extractTime(String value) throws IOException {
    // E.g. "200407101030"
    try {
      int hour = Integer.parseInt(value.substring(8, 10));
      int minute = Integer.parseInt(value.substring(10, 12));
      return hour * 60 + minute;
    }
    catch (Throwable thr) {
      throw new IOException("Illegal time value: '" + value + "'");
    }
  }


  /**
   * Extracts the date from a XMLTV time value.
   *
   * @param value The value to extract the date from.
   * @return The date.
   * @throws java.io.IOException If the value has the wrong format.
   */
  private Date extractDate(String value) throws IOException {
    // E.g. "200407101030"
    try {
      int year = Integer.parseInt(value.substring(0, 4));
      int month = Integer.parseInt(value.substring(4, 6));
      int day = Integer.parseInt(value.substring(6, 8));
      return new Date(year, month, day);
    }
    catch (Throwable thr) {
      throw new IOException("Illegal time value: '" + value + "'");
    }
  }


  /**
   * Adds a bit to the info field
   *
   * @param bit The bit to add
   */
  private void addInfoBit(int bit) {
    int info = 0;

    // Try to get the already set info bits
    ProgramField infoField = mFrame.removeProgramFieldOfType(ProgramFieldType.INFO_TYPE);
    if (infoField != null) {
      info = infoField.getIntData();
    }

    // Add the bit
    info |= bit;

    // Set the changed info bits
    addField(ProgramField.create(ProgramFieldType.INFO_TYPE, info));
  }


  /**
   * Adds a text to a field that builds a comma separated value (e.g. the
   * actor list).
   *
   * @param type The type of the field to add the text to.
   * @param text The text to add.
   * @param linebreak <code>true</code> if a linebreak should be added after each line
   */
  private void addToList(ProgramFieldType type, String text, boolean linebreak) {
    // Try to prefix the old value
    ProgramField field = mFrame.removeProgramFieldOfType(type);
    if (field != null) {
      if (linebreak) {
        text = field.getTextData() + ",\n" + text;
      } else {
        text = field.getTextData() + ", " + text;
      }
    }

    // Set the text
    addField(ProgramField.create(type, text));
  }


  /**
   * Adds a field to the current frame.
   * <p/>
   * If there is already a field of this type in the frame, a warning is
   * generated (and not an error which will happen when adding two times a
   * field of the same type).
   *
   * @param field The field to add.
   */
  private void addField(ProgramField field) {
    if (field == null) {
      return;
    }

    ProgramField existingField = mFrame.getProgramFieldOfType(field.getType());
    if (existingField == null) {
      // There is no such field -> Add the new one
      mFrame.addProgramField(field);
    } else {
      // We already have this kind of field -> log a warning
      mXmlTvPDS.logMessage("WARNING: There is already a field of the type '"
          + field.getType().getName() + "': existing value: "
          + existingField.getDataAsString() + ", ignored value: "
          + field.getDataAsString());
    }
  }


  /**
   * Stores the extracted TV data in a directory.
   *
   * @param targetDir The directory where to write the raw TV data.
   * @throws java.io.IOException         If writing the tv data failed.
   * @throws util.io.FileFormatException If the extracted TV data has an illegal
   *                                     format.
   */
  public void storeTvData(String targetDir)
      throws IOException, FileFormatException {
    for (ProgramFrameDispatcher dis : mDispatcherHash.values()) {
      dis.store(targetDir);
    }
  }

  /**
   * Clears a StringBuffer
   *
   * @param buffer The StringBuffer to clear.
   */
  private void clear(StringBuffer buffer) {
    buffer.delete(0, buffer.length());
  }

}
