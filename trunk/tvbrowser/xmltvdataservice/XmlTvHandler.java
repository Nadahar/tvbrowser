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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package xmltvdataservice;

import java.util.*;

import tvdataloader.*;
import devplugin.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Extracts the program information from a XMLTV file.
 * <p>
 * This class is used by the XML-SAX-Parser to handle the parsing events.
 *
 * @author Til Schneider, www.murfman.de
 */
public class XmlTvHandler extends DefaultHandler {
  
  /**
   * The maximum length of a short info. Used for generating a short info out of a
   * (long) description.
   */  
  private static final int MAX_SHORT_INFO_LENGTH = 100;

  /** The date to look for. */  
  private devplugin.Date mDate;
  
  /** The channel to look for. */  
  private Channel mChannel;

  /** The StringBuffer that collects the text of a tag. */  
  private StringBuffer mCurrTextBuffer = new StringBuffer();
  
  /** The currently parsed program. */  
  private MutableProgram mCurrProgram;
  
  /** The program list where to store the programs found. */  
  private MutableChannelDayProgram mChannelDayProgram;
  
  /**
   * The XML locator. It can be used to ask for the location of the parser
   * within the XML file.
   */  
  private Locator mLocator;
  
  /** The set where the found channel names are stored. */  
  private HashSet mChannelSet = new HashSet();
  
  /**
   * The maximum start time in the XML file. In minutes after midnight
   * (hours * 60 + minutes).
   * <p>
   * Used to determine whether the file contains the program of the whole day.
   */
  private int mMaxStartTime;

  

  /**
   * Creates a new instance of XmlTvHandler.
   *
   * @param date The date to look for.
   * @param channel The channel to look for.
   */  
  public XmlTvHandler(devplugin.Date date, Channel channel) {
    mDate = date;
    mChannel = channel;
  }



  /**
   * Gets the list of the found programs.
   *
   * @return the list of the found programs.
   */  
  public AbstractChannelDayProgram getChannelDayProgram() {
    /* // dump all channels found
    System.out.print("Channels found: ");
    Iterator iter = mChannelSet.iterator();
    while (iter.hasNext()) {
      System.out.print(", " + iter.next());
    }
    System.out.println();
    */
    
    System.out.println("Found " + mChannelDayProgram.getProgramCount()
      + " programs for " + mChannel.getName() + " on " + mDate);
    
    return mChannelDayProgram;
  }
  
  
  
  /**
   * Gets the maximum start time in the XML file. In minutes after midnight
   * (hours * 60 + minutes).
   * <p>
   * Used to determine whether the file contains the program of the whole day.
   *
   * @return maximum start time in the XML file.
   */
  public int getMaxStartTime() {
    return mMaxStartTime;
  }

  
  
  /**
   * Called by the XML-SAX-parser when he starts parsing the file.
   */
  public void startDocument() throws SAXException {
    mMaxStartTime = -1;
  }
  


  /**
   * Called by the XML-SAX-parser when a tag starts.
   *
   * @param namespaceURI The namespace.
   * @param simpleName The simple name.
   * @param qualifiedName The qualified name.
   * @param attrs The attributes of the tag.
   */  
  public void startElement(String namespaceURI, String simpleName,
    String qualifiedName, Attributes attrs) throws SAXException
  {
    // Empty the current text buffer
    mCurrTextBuffer.delete(0, mCurrTextBuffer.length());

    // Get the name of the starting element
    String currElement = simpleName;
    if ("".equals(currElement)) {
      // not namespaceAware
      currElement = qualifiedName;
    }

    try {
      // element specific behaviour
      if (currElement.equals("programme")) {
        /*
        if (mLocator != null) {
          System.out.println("In line " + mLocator.getLineNumber());
        }
        */
        
        String start       = attrs.getValue("start");
        String stop        = attrs.getValue("stop");
        String channelName = attrs.getValue("channel");
        String clumpidx    = attrs.getValue("clumpidx");

        // extract the start time, e.g. "200304182145 +0100"
        if (start.length() < 12) {
          return;
        }
        
        int hours = Integer.parseInt(start.substring(8, 10));
        int minutes = Integer.parseInt(start.substring(10, 12));
        
        // update mMaxStartTime
        int time = hours * 60 + minutes;
        mMaxStartTime = Math.max(mMaxStartTime, time);
        
        // Add this channel name to the set of known channels.
        mChannelSet.add(channelName);

        // BEGIN: No caching
        if (channelName.equalsIgnoreCase(mChannel.getName())) {
          mCurrProgram = new MutableProgram(mChannel, mDate, hours, minutes);
        } else {
          mCurrProgram = null;
        }
        // END: No caching

        /*
        // extract the channel
        Channel channel = getChannelForName(channelName);
        mCurrProgram.setChannel(channel);

        // get the ChannelDayProgram for this channel from the hash
        Object channelKey = getKeyForChannel(channel);
        MutableChannelDayProgram channelDayProgram
          = (MutableChannelDayProgram) mTargetHash.get(channelKey);

        if (channelDayProgram == null) {
          // There is no ChannelDayProgram in the cache
          // -> create one and put it in the cache
          channelDayProgram = new MutableChannelDayProgram(mDate, channel);
          mTargetHash.put(channelKey, channelDayProgram);
        }

        // Add the current program to the ChannelDayProgram
        mChannelDayProgram.addProgram(mCurrProgram);
        */
      }
    }
    catch (RuntimeException exc) {
      System.err.println("Exception: " + exc);
      exc.printStackTrace();
      throw new SAXException("Exception handling start tag '" + currElement + "'!", exc);
    }
  }



  /**
   * Called by the XML-SAX-parser when a tag ends.
   *
   * @param namespaceURI The namspace.
   * @param simpleName The simple name.
   * @param qualifiedName The qualified name.
   */  
  public void endElement(String namespaceURI, String simpleName,
    String qualifiedName) throws SAXException
  {
    if (mCurrProgram == null) {
      // This program doesn't interest us
      return;
    }

    // Get the name of the starting element
    String currElement = simpleName;
    if ("".equals(currElement)) {
      // not namespaceAware
      currElement = qualifiedName;
    }

    // element specific behaviour
    if (currElement.equals("title")) {
      String title = getText();
      mCurrProgram.setTitle(title);
    }
    else if (currElement.equals("desc")) {
      String desc = getText();
      
      // Create a shortinfo
      String shortInfo = createShortInfo(desc);
      
      mCurrProgram.setShortInfo(shortInfo);
      mCurrProgram.setDescription(desc);
    }
    else if (currElement.equals("length")) {
      String lengthAsString = getText();
      // We assume the length is in minutes...
      int lengthInMinutes = -1;
      try {
        lengthInMinutes = Integer.parseInt(lengthAsString);
      }
      catch (NumberFormatException exc) {}
      mCurrProgram.setLength(lengthInMinutes);
    }
    else if (currElement.equals("programme")) {
      if (mChannelDayProgram == null) {
        mChannelDayProgram = new MutableChannelDayProgram(mDate, mChannel);
      }

      // Add the current program to the ChannelDayProgram
      mChannelDayProgram.addProgram(mCurrProgram);
      
      // System.out.println("Program found: " + mCurrProgram);
    }
  }



  /**
   * Called by the XML-SAX-parser when he found text within a tag.
   *
   * @param buf The buffer where the text is stored.
   * @param offset The offset whithin the buffer
   * @param len The length of the text.
   */  
  public void characters(char[] buf, int offset, int len) throws SAXException {
    mCurrTextBuffer.append(buf, offset, len);
  }

  
  
  /**
   * Called by the XML-SAX-parser to set the Locator object.
   *
   * @param locator A locator for all SAX document events.
   * @see org.xml.sax.ContentHandler#setDocumentLocator
   * @see org.xml.sax.Locator
   */
  public void setDocumentLocator (Locator locator) {
    mLocator = locator;
  }
  
  
  
  /**
   * Gets the text of the current tag.
   * <p>
   * This text will be post-processed to correct errors.
   *
   * @return the text of the current tag.
   */
  protected String getText() {
    // Workaround: There is an error in the german XMLTV files:
    // All '&' characters are replaced by ' und ',
    // so '&auml;' becomes ' und auml;' so the XML parser can't replace it.
    XmlTvUtilities.replace(mCurrTextBuffer, " und auml;", "\u00e4");
    XmlTvUtilities.replace(mCurrTextBuffer, " und Auml;", "\u00c4");
    XmlTvUtilities.replace(mCurrTextBuffer, " und ouml;", "\u00f6");
    XmlTvUtilities.replace(mCurrTextBuffer, " und Ouml;", "\u00d6");
    XmlTvUtilities.replace(mCurrTextBuffer, " und uuml;", "\u00fc");
    XmlTvUtilities.replace(mCurrTextBuffer, " und Uuml;", "\u00dc");
    XmlTvUtilities.replace(mCurrTextBuffer, " und szlig;", "\u00df");
    XmlTvUtilities.replace(mCurrTextBuffer, " und middot;", "\u00b7");
    XmlTvUtilities.replace(mCurrTextBuffer, " und eacute;", "\u00e9");
    XmlTvUtilities.replace(mCurrTextBuffer, " und oacute;", "\u00f3");
    XmlTvUtilities.replace(mCurrTextBuffer, " und amp;", "\u0026");
    XmlTvUtilities.replace(mCurrTextBuffer, " und quot;", "\"");
    
    System.out.println("deg: " + Integer.toHexString('°'));
    System.out.println("aacute: " + Integer.toHexString('á'));
    System.out.println("agrave: " + Integer.toHexString('à'));
    System.out.println("egrave: " + Integer.toHexString('è'));
    System.out.println("ograve: " + Integer.toHexString('ò'));

    return mCurrTextBuffer.toString().trim();
  }


  
  /**
   * Creates a short info out of a long description.
   *
   * @param description The description to create the short info from.
   * @return The created short info.
   */
  protected String createShortInfo(String description) {
    if (description.length() < MAX_SHORT_INFO_LENGTH) {
      return description;
    } else {
      // Get the end of the last fitting sentense
      int lastDot = description.lastIndexOf('.', MAX_SHORT_INFO_LENGTH);
      int lastMidDot = description.lastIndexOf('*', MAX_SHORT_INFO_LENGTH);
      
      int cutIdx = Math.max(lastDot, lastMidDot);
      
      // But show at least half the maximum length
      if (cutIdx < (MAX_SHORT_INFO_LENGTH / 2)) {
        cutIdx = description.lastIndexOf(' ', MAX_SHORT_INFO_LENGTH);
      }
      
      return description.substring(0, cutIdx + 1) + "...";
    }
  }
  
}
