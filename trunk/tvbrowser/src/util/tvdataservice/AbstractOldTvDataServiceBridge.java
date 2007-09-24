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
package util.tvdataservice;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataService;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import devplugin.Channel;
import devplugin.ProgressMonitor;
import devplugin.Version;


/**
 * Works as a bridge between the old and the new TvDataService style.
 * <p>
 * If you have used the AbstractTvDataService class you can extend from this
 * class and your TvDataService will work again. 
 * 
 * @deprecated Use the new TvDataService style.
 * 
 * @author Til Schneider, www.murfman.de
 */
public abstract class AbstractOldTvDataServiceBridge implements TvDataService {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(AbstractOldTvDataServiceBridge.class.getName());

  /** Specifies whether file that have been parsed should be deleted. */
  private static final boolean DELETE_PARSED_FILES = false;

  private Channel[] mAvailableChannelArr;

  private Properties mSettings;

  private ProgramDispatcher mProgramDispatcher;

  /**
   * A set of the files, we downloaded or we tried to download. We need this
   * list, so we don't attempt to download a file where the download failed.
   */
  private HashSet<String> mAlreadyDownloadedFiles;

  public devplugin.Version getAPIVersion() {
    return new Version(1,0);
  }


  /**
   * Creates a new instance of AbstractTvDataService.
   */
  public AbstractOldTvDataServiceBridge() {
  }



  /**
   * Updates the TV listings provided by this data service.
   * 
   * @throws TvBrowserException
   */  
  public void updateTvData(TvDataUpdateManager dataBase, Channel[] channelArr,
    devplugin.Date startDate, int dateCount, ProgressMonitor monitor)
    throws TvBrowserException
  {
    connect();

    monitor.setMaximum(dateCount * channelArr.length);
    try {
      devplugin.Date date = startDate;
      for (int day = 0; day < dateCount; day++) {
        boolean anyDataFound = false;
        for (int i = 0; i < channelArr.length; i++) {
          monitor.setValue(day * channelArr.length + i);
          
          if (dataBase.isDayProgramAvailable(date, channelArr[i])) {
            anyDataFound = true;
          } else {
            MutableChannelDayProgram prog = downloadDayProgram(date, channelArr[i]);
            if (prog != null) {
              dataBase.updateDayProgram(prog);
              anyDataFound = true;
            }
          }
          
          // Check whether the download should be canceled
          if (dataBase.cancelDownload()) {
            break;
          }
        }
        
        // If we can't find any data on one day (for all channels) or the
        // download should be canceled, we stop
        if (dataBase.cancelDownload() || (! anyDataFound)) {
          break;
        }
        
        date = date.addDays(1);
      }
    }
    finally {
      // Disconnect in every case
      disconnect();
    }
  }



  /**
   * Gets the default list of the channels that are available by this data
   * service.
   */
  protected abstract Channel[] getDefaultAvailableChannels();


  public Channel[] checkForAvailableChannels() throws TvBrowserException {
    return null;
  }
  
  public boolean supportsDynamicChannelList() {
    return false;
  }


  /**
   * Gets the name of the directory where to download the data service specific
   * files.
   */
  protected abstract String getDataDirectory();



  /**
   * Gets the name of the file that contains the data of the specified date.
   */
  protected abstract String getFileNameFor(devplugin.Date date,
    devplugin.Channel channel);



  /**
   * Downloads the file containing the data for the specified dat and channel.
   *
   * @param date The date to load the data for.
   * @param channel The channel to load the data for.
   * @param targetFile The file where to store the file.
   */
  protected abstract void downloadFileFor(devplugin.Date date, Channel channel,
    File targetFile) throws TvBrowserException;



  /**
   * Parses the specified file.
   *
   * @param file The file to parse.
   * @param date The date to load the data for
   * @param channel The channel to load the data for
   * @param programDispatcher The ProgramDispatcher where to store the found
   *        programs.
   */
  protected abstract void parseFile(File file, devplugin.Date date,
    Channel channel, ProgramDispatcher programDispatcher)
    throws TvBrowserException;



  /**
   * Gets the settings.
   */
  protected Properties getSettings() {
    return mSettings;
  }



  /**
   * Called by the host-application before starting to download.
   * @throws TvBrowserException 
   */
  public void connect() throws TvBrowserException {
    if (mProgramDispatcher != null) {
      throw new IllegalArgumentException("We are already connected!");
    }

    mProgramDispatcher = new ProgramDispatcher();
    mAlreadyDownloadedFiles = new HashSet<String>();
  }



  /**
   * After the download is done, this method is called. Use this method for
   * clean-up.
   * @throws TvBrowserException 
   */
  public void disconnect() throws TvBrowserException {
    if (mProgramDispatcher == null) {
      throw new IllegalArgumentException("We are already disconnected!");
    }

    mProgramDispatcher = null;
    mAlreadyDownloadedFiles = null;
  }



  /**
   * Returns the whole program of the channel on the specified date.
   * @throws TvBrowserException 
   */
  public MutableChannelDayProgram downloadDayProgram(devplugin.Date date,
    devplugin.Channel channel) throws TvBrowserException
  {
    if (mProgramDispatcher == null) {
      throw new IllegalArgumentException("We are not connected!");
    }

    MutableChannelDayProgram channelDayProgram
      = mProgramDispatcher.getChannelDayProgram(date, channel);
    
    if (channelDayProgram == null || !channelDayProgram.isComplete()) {
      loadFileFor(date, channel);
      channelDayProgram = mProgramDispatcher.getChannelDayProgram(date, channel);
    }

    return channelDayProgram;
  }


  
  

  /**
   * Downloads and parses the file for the given date and channel.
   * <p>
   * This method parses the file and extracts the programs for all channels at
   * once. The file is downloaded if nessasry.
   *
   * @param date The date to get the programs for.
   * @param channel The channel to get the programs for. When the channel list
   *        is available, this parameter will get obsolete.
   */
  private void loadFileFor(devplugin.Date date, Channel channel)
    throws TvBrowserException
  {
    String dataDirectory = getDataDirectory() + File.separator;
    String fileName = getFileNameFor(date, channel);

    // Check whether the file is already present
    File localFile = new File(dataDirectory + fileName);
    if (! localFile.exists()) {
    //  mLog.info("" + localFile.getAbsolutePath() + " does not exist!");

      // The file is not present -> try to download it
      if (! mAlreadyDownloadedFiles.contains(fileName)) {
        // Create the data directory
        localFile.getParentFile().mkdir();

        // download the file
        mAlreadyDownloadedFiles.add(fileName);
        try {
          downloadFileFor(date, channel, localFile);
        }
        catch (TvBrowserException exc) {
          // File is incomplete -> delete it
          mLog.info("download failed");
          localFile.delete();
          // rethrow the exception
          throw exc;
        }
      }
    }

    // parse the file
    if (localFile.exists()) {
      try {
        parseFile(localFile, date, channel, mProgramDispatcher);
      }
      catch (TvBrowserException exc) {
        // The file is corrupt -> delete it
        localFile.delete();
        // rethrow the exception
        throw exc;
      }

      // If parsed files should be deleted -> do it
      if (DELETE_PARSED_FILES) {
        localFile.delete();
      }
    }
  }



  public SettingsPanel getSettingsPanel() {
    return null;
  }



  public boolean hasSettingsPanel() {
    return false;
  }



  /**
   * Called by the host-application during start-up. Implements this method to
   * load your dataservices settings from the file system.
   */
  public void loadSettings(Properties settings) {
    mSettings = settings;
  }



  /**
   * Called by the host-application during shut-down. Implements this method to
   * store your dataservices settings to the file system.
   */
  public java.util.Properties storeSettings() {
    return mSettings;
  }



  /**
   * Gets the list of the channels that are available by this data service.
   */
  public Channel[] getAvailableChannels() {
    if (mAvailableChannelArr == null) {
      mAvailableChannelArr = getDefaultAvailableChannels();
    }
    return mAvailableChannelArr;
  }

}
