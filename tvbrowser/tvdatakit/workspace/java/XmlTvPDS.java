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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import devplugin.*;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ChannelGroupImpl;
import devplugin.ProgramFieldType;

import primarydatamanager.primarydataservice.AbstractPrimaryDataService;
import primarydatamanager.primarydataservice.ProgramFrameDispatcher;
import tvbrowserdataservice.file.FileFormatException;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;

/**
 * Extracts TV data from a XMLTV file (see http://membled.com/work/apps/xmltv/).
 *
 * @author Til Schneider, www.murfman.de
 */
public class XmlTvPDS extends AbstractPrimaryDataService {
  
  /** The file where the channel data is stored. */
  private static final String CHANNEL_FILE_NAME = "TvChannels.xml";

  /** The file where the TV data is stored. */
  private static final String TV_DATA_FILE_NAME = "TvData.xml";


  /**
   * Gets the list of the channels that are available by this data service.
   *
   * @return The list of available channels
   */
  public Channel[] getAvailableChannels() {
    // Get the channel file
    File channelFile = new File(CHANNEL_FILE_NAME);
    if (! channelFile.exists()) {
      logException(new IOException("Channel data file not found: "
          + channelFile.getAbsolutePath()));
    }

    // parse the channel file
    ChannelHandler handler = new ChannelHandler();
    
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(channelFile);
      parse(stream, handler);
      stream.close();
    }
    catch (Exception exc) {
      logException(exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (Exception exc) {}
      }
    }
    
    return handler.getChannels();
  }


  /**
   * Gets the raw TV data and writes it to a directory
   *
   * @param dir The directory to write the raw TV data to.
   */
  protected void execute(String dir) {
    // Get the TV data file
    File channelFile = new File(TV_DATA_FILE_NAME);
    if (! channelFile.exists()) {
      logException(new IOException("Channel data file not found: "
          + channelFile.getAbsolutePath()));
    }
    
    // Get the channels
    Channel[] channelArr = getAvailableChannels();

    // parse the TV data file
    TvDataHandler handler = new TvDataHandler(channelArr);
    
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(channelFile);
      parse(stream, handler);
      stream.close();
    }
    catch (Exception exc) {
      logException(exc);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (Exception exc) {}
      }
    }
    
    // Store the extracted TV data
    try {
      handler.storeTvData(dir);
    }
    catch (Exception exc) {
      logException(exc);
    }
  }


  /**
   * Gets the number of bytes read (= downloaded) by this data service.
   *
   * @return The number of bytes read.
   */
  public int getReadBytesCount() {
    return 0;
  }


  /**
   * Parses a stream.
   * 
   * @param stream The stream to parse.
   * @param handler The handler to use for parsing.
   * @throws Exception When parsing failed.
   */
  private void parse(InputStream stream, ContentHandler handler)
    throws Exception
  {
    SAXParser parser = new SAXParser();
    parser.setContentHandler(handler);
    
    // Complete list of features of the xerces parser:
    // http://xml.apache.org/xerces2-j/features.html
    parser.setFeature("http://xml.org/sax/features/validation", false);
    parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    
    parser.parse(new InputSource(stream));
  }

  
  /**
   * Clears a StringBuffer
   * 
   * @param buffer The StringBuffer to clear.
   */
  private void clear(StringBuffer buffer) {    
    buffer.delete(0, buffer.length());
  }
  

  /**
   * A handler that parses channels from XML.
   */
  private class ChannelHandler extends DefaultHandler {
    
    /** The parsed channels. */
    private ArrayList mChannelList;
    
    /** Holds the text of the current tag. */
    private StringBuffer mText;
    
    /** The channel data */
    private String mName, mId, mGroupId, mTimeZoneName, mCountry,
      mCopyrightNotice, mWebpage;

    
    
    /**
     * Creates a new instance of ChannelHandler.
     */
    public ChannelHandler() {
      mChannelList = new ArrayList();
      mText = new StringBuffer();
    }

    
    /**
     * Handles the occurence of tag text.
     */
    public void characters (char ch[], int start, int length)
      throws SAXException
    {
      // There is some text -> Add it to the text buffer
      mText.append(ch, start, length);
    }


    /**
     * Handles the occurence of a start tag.
     */
    public void startElement (String uri, String localName, String qName,
      Attributes attributes)
      throws SAXException
    {
      // A new tag begins -> Clear the text buffer
      clear(mText);

      if (qName.equals("channel")) {
        mId = attributes.getValue("id");
      }
    }
    
    
    /**
     * Handles the occurence of an end tag.
     */
    public void endElement (String uri, String localName, String qName)
      throws SAXException
    {
      String text = mText.toString();

      if (qName.equals("name")) {
        mName = text;
      }
      else if (qName.equals("group")) {
        mGroupId = text;
      }
      else if (qName.equals("time-zone")) {
        mTimeZoneName = text;
      }
      else if (qName.equals("country")) {
        mCountry = text;
      }
      else if (qName.equals("copyright")) {
        mCopyrightNotice = text;
      }
      else if (qName.equals("url")) {
        mWebpage = text;
      }
      else if (qName.equals("channel")) {
        // Check the data
        if ((mName == null) || (mId == null) || (mGroupId == null)
          || (mTimeZoneName == null) || (mCountry == null)
          || (mCopyrightNotice == null) || (mWebpage == null))
        {
          logException(new IOException("Channel data of '" + mName + "' not complete!"));
        } else {
          // Create the channel
          ChannelGroup group = new ChannelGroupImpl(mGroupId, null, null);
          TimeZone timeZone = TimeZone.getTimeZone(mTimeZoneName);
          Channel channel = new Channel(null, mName, mId, timeZone, mCountry,
                                        mCopyrightNotice, mWebpage, group);
          mChannelList.add(channel);
        }

        // Reset fields for the next channel
        mName = mId = mGroupId = mTimeZoneName = mCountry = mCopyrightNotice
          = mWebpage = null;
      }
    }
    
    
    /**
     * Gets the parsed channels
     * 
     * @return
     */
    public Channel[] getChannels() {
      Channel[] arr = new Channel[mChannelList.size()];
      mChannelList.toArray(arr);
      return arr;
    }

  } // inner class ChannelHandler

  
  /**
   * A handler that parses TV data from XML.
   */
  private class TvDataHandler extends DefaultHandler {

    /** The program dispatchers. (key: channel id, value: ProgramFrameDispatcher) */
    private HashMap mDispatcherHash;

    /** Holds the text of the current tag. */
    private StringBuffer mText;
    
    /** The value of the attribute 'lang' of the current tag. */
    private String mLang;

    /** The country of the current ProgramFrame's channel. */
    private String mChannelCountry;
    
    /** The current ProgramFrame. */
    private ProgramFrame mFrame;
    
    /** The date of the current ProgramFrame */
    private Date mDate;
    
    /** The channel ID of the current ProgramFrame */
    private String mChannelId;
    
    
    /**
     * Creates a new instance of TvDataHandler.
     * 
     * @param channelArr The channels to extract the programs for.
     */
    public TvDataHandler(Channel[] channelArr) {
      mDispatcherHash = new HashMap();
      for (int i = 0; i < channelArr.length; i++) {
        String channelId = channelArr[i].getId();
        ProgramFrameDispatcher dis = new ProgramFrameDispatcher(channelArr[i]);
        mDispatcherHash.put(channelId, dis);
      }

      mText = new StringBuffer();
    }

    
    /**
     * Handles the occurence of tag text.
     */
    public void characters (char ch[], int start, int length)
      throws SAXException
    {
      // There is some text -> Add it to the text buffer
      mText.append(ch, start, length);
    }


    /**
     * Handles the occurence of a start tag.
     */
    public void startElement (String uri, String localName, String qName,
      Attributes attributes)
      throws SAXException
    {
      // A new tag begins -> Clear the text buffer
      clear(mText);
      
      // Set the lang
      mLang = attributes.getValue("lang");
      
      // Spezial tag treatment 
      if (qName.equals("programme")) {
        String start = attributes.getValue("start");
        mChannelId = attributes.getValue("channel");
        if (start == null) {
          logException(new IOException("Start time missing in programme tag"));
        }
        else if (mChannelId == null) {
          logException(new IOException("Channel missing in programme tag"));
        }
        else {
          ProgramFrameDispatcher dispatcher
            = (ProgramFrameDispatcher) mDispatcherHash.get(mChannelId);
          if (dispatcher == null) {
            logMessage("WARNING: programme tag uses unknown channel id: '"
                + mChannelId + "'. It will be ignored");
          } else {
            mChannelCountry = dispatcher.getChannel().getCountry();
            try {
              mDate = extractDate(start);
    
              mFrame = new ProgramFrame();
              mFrame.addProgramField(ProgramField.create(ProgramFieldType.START_TIME_TYPE,
                  extractTime(start)));
              mFrame.addProgramField(ProgramField.create(ProgramFieldType.SHOWVIEW_NR_TYPE,
                  attributes.getValue("showview")));
              
              String vps = attributes.getValue("vps-start");
              if (vps != null) {
                int time = extractTime(vps);
                mFrame.addProgramField(ProgramField.create(ProgramFieldType.VPS_TYPE, time));
              }
            }
            catch (IOException exc) {
              logException(exc);
              mFrame = null; // This frame is invalid
            }
          }
        }
      }
      else if (qName.equals("previously-shown")) {
        try {
          Date prevDate = extractDate(attributes.getValue("start"));
          mFrame.addProgramField(ProgramField.create(ProgramFieldType.REPETITION_OF_TYPE, prevDate.toString()));
        }
        catch (IOException exc) {
          logException(exc);
        }
      }
      else if (qName.equals("next-time-shown")) {
        try {
          Date nextDate = extractDate(attributes.getValue("start"));
          mFrame.addProgramField(ProgramField.create(ProgramFieldType.REPETITION_ON_TYPE, nextDate.toString()));
        }
        catch (IOException exc) {
          logException(exc);
        }
      }
      
    }
    
    
    /**
     * Handles the occurence of an end tag.
     */
    public void endElement (String uri, String localName, String qName)
      throws SAXException
    {
      if (mFrame != null) {
        String text = mText.toString().trim();

        if (qName.equals("title")) {
          if ((mLang == null) || mLang.equals(mChannelCountry)) {
            mFrame.addProgramField(ProgramField.create(ProgramFieldType.TITLE_TYPE, text));
          } else {
            mFrame.addProgramField(ProgramField.create(ProgramFieldType.ORIGINAL_TITLE_TYPE, text));
          }
        }
        else if (qName.equals("sub-title")) {
          if ((mLang == null) || mLang.equals(mChannelCountry)) {
            mFrame.addProgramField(ProgramField.create(ProgramFieldType.EPISODE_TYPE, text));
          } else {
            mFrame.addProgramField(ProgramField.create(ProgramFieldType.ORIGINAL_EPISODE_TYPE, text));
          }
        }
        else if (qName.equals("desc")) {
          mFrame.addProgramField(ProgramField.create(ProgramFieldType.DESCRIPTION_TYPE, text));
        }
        else if (qName.equals("date")) {
          if (text.length() < 4) {
            logMessage("WARNING: The date value must have at least 4 chars: '"
                       + text + "'");
          } else {
            int year = Integer.parseInt(text.substring(0, 4));
            mFrame.addProgramField(ProgramField.create(ProgramFieldType.PRODUCTION_YEAR_TYPE, year));
          }
        }
        else if (qName.equals("rating")) {
          try {
            int ageLimit = Integer.parseInt(text);
            mFrame.addProgramField(ProgramField.create(ProgramFieldType.AGE_LIMIT_TYPE, ageLimit));
          }
          catch (NumberFormatException exc) {
            logMessage("WARNING: rating is no number: '" + text + "' and will be ignored.");
          } 
        }
        else if (qName.equals("url")) {
          mFrame.addProgramField(ProgramField.create(ProgramFieldType.URL_TYPE, text));
        }
        else if (qName.equals("category")) {
          mFrame.addProgramField(ProgramField.create(ProgramFieldType.GENRE_TYPE, text));
        }
        else if (qName.equals("country")) {
          mFrame.addProgramField(ProgramField.create(ProgramFieldType.ORIGIN_TYPE, text));
        }
        else if (qName.equals("subtitles")) {
          if ((mLang == null) || mLang.equals(mChannelCountry)) {
            addInfoBit(Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED);
          } else {
            addInfoBit(Program.INFO_ORIGINAL_WITH_SUBTITLE);
          }
        }
        else if (qName.equals("live")) {
          addInfoBit(Program.INFO_LIVE);
        }
        else if (qName.equals("length")) {
          try {
            int length = Integer.parseInt(text);
            mFrame.addProgramField(ProgramField.create(ProgramFieldType.NET_PLAYING_TIME_TYPE, length));
          }
          catch (NumberFormatException exc) {
            logMessage("WARNING: length is no number: '" + text + "' and will be ignored.");
          } 
        }
        else if (qName.equals("actor")) {
          addToList(ProgramFieldType.ACTOR_LIST_TYPE, text);
        }
        else if (qName.equals("director")) {
          addToList(ProgramFieldType.DIRECTOR_TYPE, text);
        }
        else if (qName.equals("writer")) {
          addToList(ProgramFieldType.SCRIPT_TYPE, text);
        }
        else if (qName.equals("presenter")) {
          addToList(ProgramFieldType.MODERATION_TYPE, text);
        }
        else if (qName.equals("music")) {
          addToList(ProgramFieldType.MUSIC_TYPE, text);
        }
        else if (qName.equals("colour")) {
          if (text.equals("no")) {
            addInfoBit(Program.INFO_VISION_BLACK_AND_WHITE);
          }
          else if (! text.equals("yes")) {
            logMessage("WARNING: value of colour tag must be 'yes' or 'no',"
                + " but it is '" + text + "'");
          }
        }
        else if (qName.equals("aspect")) {
          if (text.equals("4:3")) {
            addInfoBit(Program.INFO_VISION_4_TO_3);
          }
          else if (text.equals("16:9")) {
            addInfoBit(Program.INFO_VISION_16_TO_9);
          }
          else {
            logMessage("WARNING: value of aspect tag must be '4:3' or '16:9',"
                + " but it is '" + text + "'");
          }
        }
        else if (qName.equals("stereo")) {
          if (text.equals("mono")) {
            addInfoBit(Program.INFO_AUDIO_MONO);
          }
          else if (text.equals("stereo")) {
            addInfoBit(Program.INFO_AUDIO_STEREO);
          }
          else if (text.equals("surround")) {
            addInfoBit(Program.INFO_AUDIO_DOLBY_SURROUND);
          }
          else if (text.equals("5.1")) {
            addInfoBit(Program.INFO_AUDIO_DOLBY_DIGITAL_5_1);
          }
          else if (text.equals("two channel tone")) {
            addInfoBit(Program.INFO_AUDIO_TWO_CHANNEL_TONE);
          }
          else {
            logMessage("WARNING: value of stereo tag must be one of 'mono', "
                + "'stereo', 'surround', '5.1' or 'two channel tone' but it is '"
                + text + "'");
          }
        }
        else if (qName.equals("programme")) {
          ProgramFrameDispatcher dis
                     = (ProgramFrameDispatcher) mDispatcherHash.get(mChannelId);
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
     * @throws IOException If the value has the wrong format.
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
     * Estracts the date from a XMLTV time value.
     * 
     * @param value The value to extract the date from.
     * @return The date.
     * @throws IOException If the value has the wrong format.
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
      mFrame.addProgramField(ProgramField.create(ProgramFieldType.INFO_TYPE, info));
    }

    
    private void addToList(ProgramFieldType type, String text) {
      // Try to prefix the old value
      ProgramField field = mFrame.removeProgramFieldOfType(type);
      if (field != null) {
        text = field.getTextData() + ", " + text;
      }
      
      // Set the text
      mFrame.addProgramField(ProgramField.create(type, text));
    }
    
    
    /**
     * Stores the extracted TV data in a directory.
     *
     * @param targetDir The directory where to write the raw TV data.
     * @throws IOException If writing the tv data failed.
     * @throws FileFormatException If the extracted TV data has an illegal
     *         format.
     */
    public void storeTvData(String targetDir)
      throws IOException, FileFormatException
    {
      Iterator iter = mDispatcherHash.values().iterator();
      while (iter.hasNext()) {
        ProgramFrameDispatcher dis = (ProgramFrameDispatcher) iter.next();
        dis.store(targetDir);
      }
    }
    
  } // inner class TvDataHandler
  
}