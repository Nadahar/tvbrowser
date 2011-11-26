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
 *     $Date: 2008-06-01 12:08:18 +0200 (So, 01 Jun 2008) $
 *   $Author: troggan $
 * $Revision: 4787 $
 */
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import primarydatamanager.primarydataservice.AbstractPrimaryDataService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Extracts TV data from a XMLTV file (see http://membled.com/work/apps/xmltv/).
 *
 * @author Til Schneider, www.murfman.de
 */
public class XmlTvPDS extends AbstractPrimaryDataService {

  /**
   * The file where the TV data is stored.
   */
  private static final String TV_DATA_FILE_NAME = "input/TvData.xml";

  /**
   * Gets the list of the channels that are available by this data service.
   *
   * @return The list of available channels
   */

  /**
   * Gets the raw TV data and writes it to a directory
   *
   * @param dir The directory to write the raw TV data to.
   */
  protected void execute(String dir) {
    // Get the TV data file
    File channelFile = new File(TV_DATA_FILE_NAME);
    if (!channelFile.exists()) {
      logMessage("Channel data file not found: " + channelFile.getAbsolutePath());
    }

    // parse the TV data file
    XmlTvDataHandler handler = new XmlTvDataHandler(this);

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
        try {
          stream.close();
        } catch (Exception exc) {
          // Emtpy Catch
        }
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
   * @param stream  The stream to parse.
   * @param handler The handler to use for parsing.
   * @throws Exception When parsing failed.
   */
  private void parse(InputStream stream, ContentHandler handler)
      throws Exception {
    SAXParser parser = new SAXParser();
    parser.setContentHandler(handler);

    // Complete list of features of the xerces parser:
    // http://xml.apache.org/xerces2-j/features.html
    parser.setFeature("http://xml.org/sax/features/validation", false);
    parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    parser.parse(new InputSource(stream));
  }

}