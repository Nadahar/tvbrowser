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
package tvbrowserdataservice;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

import tvbrowserdataservice.file.DayProgramFile;
import util.exc.TvBrowserException;
import util.io.DownloadHandler;
import util.io.DownloadJob;
import devplugin.Channel;
import devplugin.Date;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class DayProgramUpdateDH implements DownloadHandler {

  private static final Logger mLog
    = Logger.getLogger(DayProgramReceiveDH.class.getName());
  
  private TvBrowserDataService mDataService;
  private TvDataBaseUpdater mUpdater;
  
  /**
   * Creates a new DayProgramUpdateDH.
   * 
   * @param dataService The dataservice of the data to update.
   * @param updater The TvDataBaseUpdater.
   */
  public DayProgramUpdateDH(TvBrowserDataService dataService,
    TvDataBaseUpdater updater)
  {
    mDataService = dataService;
    mUpdater = updater;
  }
  
  
  public void handleDownload(DownloadJob job, InputStream stream)
    throws TvBrowserException
  {
    mLog.fine("Receiving file " + job.getFileName());

    // Convert the file name to a complete file name
    String completeFileName = updateFileNameToCompleteFileName(job.getFileName());
    
    File completeFile = new File(mDataService.getDataDir(), completeFileName);
    try {
      // Read the update from the stream
      DayProgramFile updateProg = new DayProgramFile();
      updateProg.readFromStream(stream, job);

      // When we are here then the loading succeed -> The file is OK.
      // It is not a corrupt because it is currently being updated.
      
      // Load the complete file
      DayProgramFile completeProg = new DayProgramFile();
      completeProg.readFromFile(completeFile);
      
      // Update the complete file
      completeProg.updateCompleteFile(updateProg);

      // Save the complete program
      completeProg.writeToFile(completeFile);
      
      // Tell the database updater that this file needs an update
      mUpdater.addUpdateJobForDayProgramFile(completeFileName);
    }
    catch (Exception exc) {
      throw new TvBrowserException(getClass(), "error.1",
        "Updating dayprogram file failed: {0}", completeFile.getAbsolutePath(),
        exc);
    }
    finally {
      mDataService.checkCancelDownload();
    }
  }
  
  
  public void handleFileNotFound(String fileName) throws TvBrowserException {
    // There is no update for this day
    mDataService.checkCancelDownload();
    
    // We have a up-to-date version of the day program in the
    // TvBrowserDataService directory. But maybe the program was deleted from
    // the TvDataBase directory -> Check this.
    Date date = DayProgramFile.getDateFromFileName(fileName);
    String country = DayProgramFile.getCountryFromFileName(fileName);
    String channelName = DayProgramFile.getChannelNameFromFileName(fileName);
    Channel channel = mDataService.getChannel(country, channelName);
    if ((channel != null)
      && (! mDataService.isDayProgramInDataBase(date, channel)))
    {
      // This day program is NOT in the database -> update it
      mUpdater.addUpdateJob(date, channel);
    }
  }


  private String updateFileNameToCompleteFileName(String updateFileName) {
    // E.g. from '2003-10-04_de_premiere-1_base_update_15.prog.gz'
    //        to '2003-10-04_de_premiere-1_base_full.prog.gz'
    
    int updatePos = updateFileName.lastIndexOf("_update_");
    String completeFileName = updateFileName.substring(0, updatePos)
      + "_full.prog.gz";

    return completeFileName;
  }

}
