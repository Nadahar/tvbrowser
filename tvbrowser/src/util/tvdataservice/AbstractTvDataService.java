/*
 * MultipleChannelTvDataService.java
 *
 * Created on 7. Mai 2003, 15:12
 */

package util.tvdataservice;

import java.util.*;

import java.io.File;

import util.exc.TvBrowserException;

import devplugin.*;
import tvdataservice.*;

/**
 *
 * @author  Til
 */
public abstract class AbstractTvDataService implements TvDataService {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(AbstractTvDataService.class.getName());

  /** Specifies whether file that have been parsed should be deleted. */
  private static final boolean DELETE_PARSED_FILES = false;

  private Channel[] mAvailableChannelArr;

  private Properties mSettings;

  private ProgramDispatcher mProgramDispatcher;

  /**
   * A set of the files, we downloaded or we tried to download. We need this
   * list, so we don't attempt to download a file where the download failed.
   */
  private HashSet mAlreadyDownloadedFiles;



  /**
   * Creates a new instance of AbstractTvDataService.
   */
  public AbstractTvDataService() {
  }



  /**
   * Gets the default list of the channels that are available by this data
   * service.
   */
  protected abstract Channel[] getDefaultAvailableChannels();



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
   */
  public void connect() throws TvBrowserException {
    if (mProgramDispatcher != null) {
      throw new IllegalArgumentException("We are already connected!");
    }

    mProgramDispatcher = new ProgramDispatcher();
    mAlreadyDownloadedFiles = new HashSet();
  }



  /**
   * After the download is done, this method is called. Use this method for
   * clean-up.
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
   */
  public ChannelDayProgram downloadDayProgram(devplugin.Date date,
    devplugin.Channel channel) throws TvBrowserException
  {
    if (mProgramDispatcher == null) {
      throw new IllegalArgumentException("We are not connected!");
    }

    MutableChannelDayProgram channelDayProgram
      = mProgramDispatcher.getChannelDayProgram(date, channel);

    // If the wanted AbstractChannelDayProgram isn't already in the cache
    // load the apropriate XMl file
    if (channelDayProgram == null) {
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
   * @param programDispatcher The dispatcher where to put the programs.
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
      mLog.info("" + localFile.getAbsolutePath() + " does not exist!");

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
