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
public class XmlTvDataService implements TVDataServiceInterface {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(XmlTvDataService.class.getName());
  
  /** The folder where to put the XML data. */  
  private static final String XMLTV_FOLDER = "xmldata" + java.io.File.separator;

  /** Specifies whether file that have been parsed should be deleted. */
  private static final boolean DELETE_PARSED_FILES = true;

  /**
   * A list of the files, we downloaded or we tried to download. We need this
   * list, so we don't attempt to download a file where the download failed.
   */
  private ArrayList mAlreadyDownloadedFiles;
  
  private ProgramDispatcher mProgramDispatcher;
  
  private Channel[] mSubscribedChannelArr;
  
  
  
  /**
   * Creates a new instance of XmlTvDataService.
   */
  public XmlTvDataService() {
  }
 
  
  
  /**
   * Called by the host-application before starting to download.
   */
  public void connect() throws TvBrowserException {
    mProgramDispatcher = new ProgramDispatcher();
    
    mSubscribedChannelArr = Plugin.getPluginManager().getSubscribedChannels();
    
    // ensure the xmltv.dtd is present
    File xmlTvDtdFile = new File(XMLTV_FOLDER + "xmltv.dtd");
    
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
    
    // empty the list of the already downloaded files
    mAlreadyDownloadedFiles = new ArrayList();
  }

  
  
  /**
   * After the download is done, this method is called. Use this method for
   * clean-up.
   */
  public void disconnect() throws TvBrowserException {
    mProgramDispatcher = null;
    mSubscribedChannelArr = null;
    mAlreadyDownloadedFiles = null;
  }

  
  
  /**
   * Called by the host-application to read the day-program of a channel from
   * the file system.
   * <p>
   * Enter code like "return (AbstractChannelDayProgram)in.readObject();" here.
   *
   * @param in The stream to read the ChannelDayProgram from.
   * @return The AbstractChannelDayProgram read from the stream.
   */
  public AbstractChannelDayProgram readChannelDayProgram(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
  {
    return (AbstractChannelDayProgram)in.readObject();
  }

  
  
  /**
   * Returns the whole program of the channel on the specified date.
   *
   * @param date The date to get the programs for.
   * @param channel The channel to get the programs for.
   * @return the whole program of the channel on the specified date.
   */
  public AbstractChannelDayProgram downloadDayProgram(devplugin.Date date,
    Channel channel) throws TvBrowserException
  {
    MutableChannelDayProgram channelDayProgram
      = mProgramDispatcher.getChannelDayProgram(date, channel);
    
    // If the wanted AbstractChannelDayProgram isn't already in the cache
    // load the apropriate XMl file
    if (channelDayProgram == null) {
      loadXmlFileFor(date, channel);
      channelDayProgram = mProgramDispatcher.getChannelDayProgram(date, channel);
    }

    // Check whether the AbstractChannelDayProgram is complete
    if ((channelDayProgram != null)) {
      if (! channelDayProgram.isComplete()) {
        channelDayProgram = null;
      }
    }
    
    return channelDayProgram;
  }

  
  
  
  /**
   * Downloads and parses the XML file for the given date and channel.
   * <p>
   * If at any time there is chance to get the list of channels, this method will
   * only be called once per XML file. This method will parse the XML file and
   * extract the programs for all channels at once.
   *
   * @param date The date to get the programs for.
   * @param channel The channel to get the programs for. When the channel list
   *        is available, this parameter will get obsolete.
   */
  private void loadXmlFileFor(devplugin.Date date, Channel channel)
    throws TvBrowserException
  {
    // Get the name of the XML file to load
    String xmlTvFileName = getXmlFileName(date);
    String gzFileName = getXmlFileName(date) + ".gz";
    File gzFile = new File(XMLTV_FOLDER + gzFileName);
    
    // Check whether the files was already downloaded
    boolean alreadyDownloaded = false;
    Iterator iter = mAlreadyDownloadedFiles.iterator();
    while (iter.hasNext()) {
      String fileName = (String) iter.next();
      if (fileName.equals(gzFileName)) {
        alreadyDownloaded = true;
      }
    }
    
    // Download the file if nessesary
    if ((! alreadyDownloaded) && (! gzFile.exists())) {
      mAlreadyDownloadedFiles.add(gzFileName);
      String gzUrl = "http://www.szing.at/xmltv/" + gzFileName;
      try {
        IOUtilities.download(new URL(gzUrl), gzFile);
      }
      catch (Exception exc) {
        gzFile.delete();
        throw new TvBrowserException(getClass(), "error.2",
          "Error downloading '{0}' to '{1}'!", gzUrl, gzFile.getAbsolutePath(), exc);
      }
    }
      
    if (! gzFile.exists()) {
      // Download must have failed
      mLog.info("File '" + gzFile.getAbsolutePath() + "' does not exist!");
    } else {
      // parse the XML file
      XmlTvHandler handler
        = new XmlTvHandler(mProgramDispatcher, mSubscribedChannelArr);

      // Use the default (non-validating) parser
      SAXParserFactory factory = SAXParserFactory.newInstance();
      FileInputStream in = null;
      GZIPInputStream gzipIn = null;
      boolean fileIsCorrupt = false;
      try {
        mLog.info("Parsing '" + gzFile.getAbsolutePath() + "'...");

        in = new FileInputStream(gzFile);
        gzipIn = new GZIPInputStream(in);

        // The system id is the location where the parser searches the DTD.
        String systemId = gzFile.getParentFile().toURI().toString();

        // parse the file
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(gzipIn, handler, systemId);
      }
      catch (Throwable thr) {
        fileIsCorrupt = true;
        
        throw new TvBrowserException(getClass(), "error.3",
          "XML TV file is corrupt!\n({0})", gzFile.getAbsolutePath(), thr);
      }
      finally {
        try {
          if (in != null) in.close();
        } catch (IOException exc) {}
        try {
          if (gzipIn != null) gzipIn.close();
        } catch (IOException exc) {}

        // If the file was parsed or currupt:
        // Delete the file, we don't need it any more
        if (DELETE_PARSED_FILES || fileIsCorrupt) {
          gzFile.delete();
        }
      }
    }
  }
  
  
  
  /**
   * Gets the name of the XML file that contains the program for the given day.
   *
   * @param date The date to get the XML file name for.
   * @return the name of the XML file.
   */
  protected String getXmlFileName(devplugin.Date date) {
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
    fileNameBuf.append(".xml");
    
    return fileNameBuf.toString();
  }

  
  
  /**
   * Gets a key for hash tables for a channel.
   *
   * @param channel The channel to get the key for.
   * @return The key for the given channel.
   */
  protected Object getKeyForChannel(Channel channel) {
    return new Integer(channel.getId());
  }
  
  
  
  /**
   * Gets a channel for the given name.
   * <p>
   * Doesn't work yet.
   *
   * @param channelName The name of the channel to get.
   * @return The channel for the specified name.
   */
  protected Channel getChannelForName(String channelName) {
    // ToDo
    return null;
  }
   
   
  public void loadSettings(java.util.Properties settings) {
  	// TODO: implement this method
  }
  
   public java.util.Properties storeSettings() {
   	// TODO: implement this method
   	return null;
   }
  
   public javax.swing.JPanel getSettingsPanel() {
   	// TODO: implement this method
   	return null;
   }
    
   
}
