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
import tvdataloader.*;

/**
 *
 * @author  Til
 */
public abstract class MultipleChannelTvDataService implements TVDataServiceInterface {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(MultipleChannelTvDataService.class.getName());
  
  /** Specifies whether file that have been parsed should be deleted. */
  private static final boolean DELETE_PARSED_FILES = false;
  
  private Channel[] mSubscribedChannelArr;
  
  private Properties mSettings;
  
  private ProgramDispatcher mProgramDispatcher;
  
  /**
   * A set of the files, we downloaded or we tried to download. We need this
   * list, so we don't attempt to download a file where the download failed.
   */
  private HashSet mAlreadyDownloadedFiles;
  
  
  
  /**
   * Creates a new instance of MultipleChannelTvDataService.
   */
  public MultipleChannelTvDataService() {
  }
  
  
  
  /**
   * Gets the name of the directory where to download the data service specific
   * files.
   */
  protected abstract String getDataDirectory();

  
  
  /**
   * Gets the name of the file that contains the data of the specified date.
   */
  protected abstract String getFileNameFor(devplugin.Date date);

  
  
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
   * @param programDispatcher The ProgramDispatcher where to store the found
   *        programs.
   */
  protected abstract void parseFile(File file, ProgramDispatcher programDispatcher)
    throws TvBrowserException;
  
  
  
  /**
   * Gets the settings.
   */
  protected Properties getSettings() {
    return mSettings;
  }
  
  
  
  /**
   * Gets the subscribed channels.
   */
  protected Channel[] getChannels() {
    return mSubscribedChannelArr;
  }
  
  
  
  /**
   * Called by the host-application before starting to download.
   */
  public void connect() throws TvBrowserException {
    mProgramDispatcher = new ProgramDispatcher();
    mSubscribedChannelArr = Plugin.getPluginManager().getSubscribedChannels();
    mAlreadyDownloadedFiles = new HashSet();
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
   * Returns the whole program of the channel on the specified date.
   */
  public AbstractChannelDayProgram downloadDayProgram(devplugin.Date date,
    devplugin.Channel channel) throws TvBrowserException
  {
    MutableChannelDayProgram channelDayProgram
      = mProgramDispatcher.getChannelDayProgram(date, channel);
    
    // If the wanted AbstractChannelDayProgram isn't already in the cache
    // load the apropriate XMl file
    if (channelDayProgram == null) {
      loadFileFor(date, channel);
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
    String fileName = getFileNameFor(date);
    
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
        parseFile(localFile, mProgramDispatcher);
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
  
  
  
  public javax.swing.JPanel getSettingsPanel() {
    return null;
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
   * Called by the host-application to read the day-program of a channel from
   * the file system.
   * Enter code like "return (AbstractChannelDayProgram)in.readObject();" here.
   */
  public AbstractChannelDayProgram readChannelDayProgram(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
  {
    return (AbstractChannelDayProgram)in.readObject();
  }
  
}
