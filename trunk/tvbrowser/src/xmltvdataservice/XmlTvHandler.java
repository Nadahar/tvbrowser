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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package xmltvdataservice;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import util.io.IOUtilities;

import tvdataloader.*;
import devplugin.*;

/**
 * Extracts the program information from a XMLTV file.
 * <p>
 * This class is used by the XML-SAX-Parser to handle the parsing events.
 *
 * @author Til Schneider, www.murfman.de
 */
public class XmlTvHandler extends DefaultHandler {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(XmlTvHandler.class.getName());
  
  /**
   * The maximum length of a short info. Used for generating a short info out of a
   * (long) description.
   */  
  private static final int MAX_SHORT_INFO_LENGTH = 100;

  /** The program dipatcher used to store the programs. */  
  private ProgramDispatcher mProgramDispatcher;
  
  /** The list of channels to search for. */
  private Channel[] mSubscribedChannelArr;

  /** The StringBuffer that collects the text of a tag. */  
  private StringBuffer mCurrTextBuffer = new StringBuffer();
  
  /** The currently parsed program. */  
  private MutableProgram mCurrProgram;
  
  /**
   * The XML locator. It can be used to ask for the location of the parser
   * within the XML file.
   */  
  private Locator mLocator;
  
  /** The set where the found channel names are stored. */  
  private HashSet mChannelSet = new HashSet();
  
  private GregorianCalendar mCalendar;



  /**
   * Creates a new instance of XmlTvHandler.
   *
   * @param date The date to look for.
   * @param channel The channel to look for.
   */  
  public XmlTvHandler(ProgramDispatcher programDispatcher,
    Channel[] subscribedChannelArr)
  {
    mProgramDispatcher = programDispatcher;
    mSubscribedChannelArr = subscribedChannelArr;
    
    mCalendar = new GregorianCalendar();
  }



  /**
   * Called by the XML-SAX-parser when he starts parsing the file.
   */
  public void startDocument() throws SAXException {
    // no op
  }

  
  
  /**
   * Receive notification of the end of the document.
   *
   * @see org.xml.sax.ContentHandler#endDocument
   */
  public void endDocument() throws SAXException {
    /* // dump all channels found
    String msg = "Channels found: ";
    Iterator iter = mChannelSet.iterator();
    while (iter.hasNext()) {
      msg += ", " + iter.next();
    }
    mLog.info(msg);
    */
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
          mLog.info("In line " + mLocator.getLineNumber());
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
        
        // Get the numbers from the start String
        int year = Integer.parseInt(start.substring(0, 4));
        int month = Integer.parseInt(start.substring(4, 6));
        int day = Integer.parseInt(start.substring(6, 8));
        int hours = Integer.parseInt(start.substring(8, 10));
        int minutes = Integer.parseInt(start.substring(10, 12));

        // Get the date
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month - 1);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        java.util.Date utilDate = mCalendar.getTime();
        long daysSince1970 = utilDate.getTime() / (24 * 60 * 60 * 1000);
        devplugin.Date date = new devplugin.Date((int) daysSince1970);
        
        // Get the time
        int time = hours * 60 + minutes;
        
        // extract the channel
        Channel channel = getChannelForName(channelName);
        
        if (channel == null) {
          mCurrProgram = null;
        } else {
          mCurrProgram = new MutableProgram(channel, date, hours, minutes);
        }
      }
    }
    catch (RuntimeException exc) {
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
      // mLog.info("Program found: " + mCurrProgram);
      
      mProgramDispatcher.dispatch(mCurrProgram);
      mCurrProgram = null;
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
    IOUtilities.replace(mCurrTextBuffer, " und auml;", "\u00e4");
    IOUtilities.replace(mCurrTextBuffer, " und Auml;", "\u00c4");
    IOUtilities.replace(mCurrTextBuffer, " und ouml;", "\u00f6");
    IOUtilities.replace(mCurrTextBuffer, " und Ouml;", "\u00d6");
    IOUtilities.replace(mCurrTextBuffer, " und uuml;", "\u00fc");
    IOUtilities.replace(mCurrTextBuffer, " und Uuml;", "\u00dc");
    IOUtilities.replace(mCurrTextBuffer, " und szlig;", "\u00df");
    IOUtilities.replace(mCurrTextBuffer, " und middot;", "\u00b7");
    IOUtilities.replace(mCurrTextBuffer, " und aacute;", "\u00e1");
    IOUtilities.replace(mCurrTextBuffer, " und eacute;", "\u00e9");
    IOUtilities.replace(mCurrTextBuffer, " und oacute;", "\u00f3");
    IOUtilities.replace(mCurrTextBuffer, " und agrave;", "\u00e0");
    IOUtilities.replace(mCurrTextBuffer, " und egrave;", "\u00e8");
    IOUtilities.replace(mCurrTextBuffer, " und ograve;", "\u00f2");
    IOUtilities.replace(mCurrTextBuffer, " und amp;", "\u0026");
    IOUtilities.replace(mCurrTextBuffer, " und quot;", "\"");
    IOUtilities.replace(mCurrTextBuffer, " und deg;", "\u00b0");
    
    // mLog.info("deg: " + Integer.toHexString('°'));

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
  
  
  
  protected Channel getChannelForName(String channelName) {
    // Add this channel name to the set of channels known by this XML file.
    mChannelSet.add(channelName);

    for (int i = 0; i < mSubscribedChannelArr.length; i++) {
      Channel channel = mSubscribedChannelArr[i];
      if (channel.getName().equalsIgnoreCase(channelName)) {
        return channel;
      }
    }
    
    return null;
  }
  
}
