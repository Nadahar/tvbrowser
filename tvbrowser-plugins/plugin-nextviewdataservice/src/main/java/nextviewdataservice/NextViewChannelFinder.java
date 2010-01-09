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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML Handler to check for available channels 
 * @author jb
 */

public class NextViewChannelFinder extends DefaultHandler {

  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(NextViewChannelFinder.class.getName());
  private StringBuffer characters;
  private NextViewDataServiceData mData;
  private NextViewDataService mService;
  private String currentChannelId;
  private Properties channelList = new Properties();
  private Properties cniMappings = new Properties();
  private boolean isFinished = false;

  /**
   * Initializie new NextViewChannelFinder instance
   * @param data ; a container for channel an program data
   */

  NextViewChannelFinder(NextViewDataServiceData data) {
    this.mData = data;
    this.mService = NextViewDataService.getInstance();
    this.characters = new StringBuffer();
    try {
      cniMappings.load(new FileInputStream(mService.mDataDir.toString() + "/cni.map.properties"));
    } catch (IOException e) {
    }
  }

  /*
   * (non-Javadoc)
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   * 
   * The characters method reports each chunk of character 
   * data and appends it to the characters buffer;
   * here: the channel name
   */
  @Override
  public void characters(char[] buf, int offset, int len) throws SAXException {
    characters.append(buf, offset, len);
  }

  /*
   * (non-Javadoc)
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   * 
   * Collects all the channel ID's, stops with the first appearnce of a 'programme' tag
   */
  @Override
  public void startElement(String namespaceURI, String simpleName,
      String qualifiedName, Attributes attrs) throws SAXException {

    if (isFinished) {
      // isFinished = true, if the channel information in the beginning of the xml-file is read
      return;
    }

    String currentElement = simpleName;
    if (currentElement.equals("")) {
      currentElement = qualifiedName;
    }

    try {
      if (currentElement.equals("channel")) {
        // parse channel information
        currentChannelId = attrs.getValue("id");
        String newChannelId = cniMappings.getProperty(currentChannelId);
        if (newChannelId != null) {
          currentChannelId = newChannelId.split(",", 2)[0];
        }
      } else if (currentElement.equals("programme")) {
        // as channel informations are always in front of the programme tags, no further parsing is necessary.
        isFinished = true;
        storeChannels();
        return;
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
   * Adds channel id & name to the channel list of the data service.
   */
  @Override
  public void endElement(String namespaceURI, String simpleName,
      String qualifiedName) throws SAXException {

    if (isFinished) {
      return;
    }

    String currentElement = simpleName;
    if (currentElement.equals("")) {
      currentElement = qualifiedName;
    }

    try {
      if (currentElement.equals("channel")) {
        // add channel to the channel list of this data service
        String channelName = characters.toString();
        if ((channelName != null) && (currentChannelId != null)) {
          channelList.setProperty(currentChannelId, channelName.trim());
          mData.addChannel(currentChannelId, channelName.trim());

        }
        currentChannelId = null;
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
   * Saves channel data to file for future use on file system
   */
  public void storeChannels() {
    OutputStream writer = null;
    try {
      writer = new FileOutputStream((mService.mDataDir + "/channels.properties"));

      channelList.store(writer, "Nxtvepg Channellist");
    } catch (IOException e) {
      mLog.info(e.toString());
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        mLog.info(e.toString());
      }
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
