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

package xmltvdataservice;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.net.URL;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.tvdataservice.*;

import tvdataloader.*;
import devplugin.*;

/**
 * A data service that reads TV-Data in the XMLTV-Format.
 * <p>
 * See <a href="http://membled.com/work/apps/xmltv/">the XMLTV site</a>
 * for more information about that format.
 * <p>
 * For the moment this source only reads from one source which covers the following
 * german channels:
 * <p>
 * TV 5, KABEL 1, ARD, PREMIERE, SAT.1, BAYERN, TRT, N-TV, SWR, ARTE, VIVA, MTV,
 * MTV2, MDR, Neunlive, EUROSPORT, 3SAT, RTL 2, ORF 2, KINDER KANAL, NBC, NORD 3,
 * HESSEN, PHOENIX, CNN, SF1, ORB, DSF, SUPER RTL, ZDF, VOX, EURONEWS, PRO 7, RTL,
 * ORF 1
 *
 * @author Til Schneider, www.murfman.de
 */
public class XmlTvDataService extends MultipleChannelTvDataService {

  /** The folder where to put the XML data. */  
  private static final String XMLTV_FOLDER = "xmldata";

  
  
  /**
   * Creates a new instance of XmlTvDataService.
   */
  public XmlTvDataService() {
  }
 
  
  
  /**
   * Called by the host-application before starting to download.
   */
  public void connect() throws TvBrowserException {
    super.connect();
    
    // ensure the xmltv.dtd is present
    File xmlTvDtdFile = new File(XMLTV_FOLDER + java.io.File.separator + "xmltv.dtd");
    
    if (! xmlTvDtdFile.exists()) {
      // create the xmldata directory
      xmlTvDtdFile.getParentFile().mkdirs();
      
      // save the xmltv.dtd
      InputStream stream = null;
      try {
        String xmlTvDtdResourceName = "xmltvdataservice/xmltv.dtd";
        ClassLoader classLoader = getClass().getClassLoader();
        stream = classLoader.getResourceAsStream(xmlTvDtdResourceName);

        IOUtilities.saveStream(stream, xmlTvDtdFile);
      }
      catch (IOException exc) {
        throw new TvBrowserException(getClass(), "error.1",
          "Error when preparing the XML TV data service directory!", exc);
      }
      finally {
        try {
          if (stream != null) stream.close();
        } catch (IOException exc) {}
      }
    }
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
    String fileName = getFileNameFor(date);
    String url = "http://www.szing.at/xmltv/" + fileName;
    try {
      IOUtilities.download(new URL(url), targetFile);
    }
    catch (Exception exc) {
      throw new TvBrowserException(getClass(), "error.2",
        "Error downloading '{0}' to '{1}'!", url, targetFile.getAbsolutePath(), exc);
    }
  }   
  
  

  /**
   * Gets the name of the directory where to download the data service specific
   * files.
   */
  protected String getDataDirectory() {
    return XMLTV_FOLDER;
  }

  
  
  /**
   * Gets the name of the file that contains the data of the specified date.
   */
  protected String getFileNameFor(devplugin.Date date) {
    java.util.Calendar cal = date.getCalendar();
    
    int month = cal.get(java.util.Calendar.MONTH) + 1;
    int day   = cal.get(java.util.Calendar.DAY_OF_MONTH);

    // The year is not saved in a devplugin.Date
    // so we've got to get the date from the current date
    java.util.Calendar now = new java.util.GregorianCalendar();
    int year = now.get(java.util.Calendar.YEAR);
    
    int nowMonth = now.get(java.util.Calendar.MONTH);
    if ((nowMonth > 11) && (month <= 2)) {
      // They want the given date of the following year
      year++;
    }
    
    // e.g. "tv_20030418.xml"
    StringBuffer fileNameBuf = new StringBuffer();
    fileNameBuf.append("tv_");
    IOUtilities.append(fileNameBuf, year, 4);
    IOUtilities.append(fileNameBuf, month, 2);
    IOUtilities.append(fileNameBuf, day, 2);
    fileNameBuf.append(".xml.gz");
    
    return fileNameBuf.toString();
  }

  
  
  /**
   * Parses the specified file.
   *
   * @param file The file to parse.
   * @param programDispatcher The ProgramDispatcher where to store the found
   *        programs.
   */
  protected void parseFile(File file, ProgramDispatcher programDispatcher)
    throws TvBrowserException
  {
    XmlTvHandler handler
      = new XmlTvHandler(programDispatcher, getChannels());

    SAXParserFactory factory = SAXParserFactory.newInstance();
    FileInputStream in = null;
    GZIPInputStream gzipIn = null;
    try {
      in = new FileInputStream(file);
      gzipIn = new GZIPInputStream(in);

      // The system id is the location where the parser searches the DTD.
      String systemId = file.getParentFile().toURI().toString();

      // parse the file
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(gzipIn, handler, systemId);
    }
    catch (Throwable thr) {
      throw new TvBrowserException(getClass(), "error.3",
        "XML TV file is corrupt!\n({0})", file.getAbsolutePath(), thr);
    }
    finally {
      try {
        if (in != null) in.close();
      } catch (IOException exc) {}
      try {
        if (gzipIn != null) gzipIn.close();
      } catch (IOException exc) {}
    }
  }
   
}
