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

import tvbrowserdataservice.file.DayProgramFile;
import util.exc.TvBrowserException;
import util.io.DownloadHandler;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class DayProgramReceiveDH implements DownloadHandler {

  private TvBrowserDataService mDataService;
  private TvDataBaseUpdater mUpdater;


  public DayProgramReceiveDH(TvBrowserDataService dataService,
    TvDataBaseUpdater updater)
  {
    mDataService = dataService;
    mUpdater = updater;
  }


  public void handleDownload(String fileName, InputStream stream)
    throws TvBrowserException
  {
    System.out.println("Receiving file " + fileName);
    File completeFile = new File(mDataService.getDataDir(), fileName);
    try {
      DayProgramFile prog = new DayProgramFile();
      prog.readFromStream(stream);
      
      // When we are here then the loading suceed -> The file is OK.
      // It is not a corrupt because it is currently being updated.

      // Save the day program
      prog.writeToFile(completeFile);
      
      // Tell the database updater that this file needs an update
      mUpdater.addUpdateJobForDayProgramFile(fileName);
    } catch (Exception exc) {
      throw new TvBrowserException(getClass(), "error.1",
        "Saving dayprogram file failed: {0}", completeFile.getAbsolutePath(),
        exc);
    }
    
    mDataService.checkCancelDownload();
  }


  public void handleFileNotFound(String fileName) throws TvBrowserException {
    // There is no data for this day
    
    mDataService.checkCancelDownload();
  }

}
