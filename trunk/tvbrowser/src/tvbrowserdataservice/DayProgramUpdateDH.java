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
public class DayProgramUpdateDH implements DownloadHandler {
  
  private File mDataDir;
  private TvDataBaseUpdater mUpdater;
  
    
  public DayProgramUpdateDH(File dataDir, TvDataBaseUpdater updater) {
    mDataDir = dataDir;
    mUpdater = updater;
  }
  
  
  public void handleDownload(String fileName, InputStream stream)
    throws TvBrowserException
  {
    System.out.println("Receiving file " + fileName);

    // Convert the file name to a complete file name
    // E.g. from '2003-10-04_de_premiere-1_base_update_15.prog.gz'
    //        to '2003-10-04_de_premiere-1_base_full.prog.gz'
    int updatePos = fileName.lastIndexOf("_update_");
    String completeFileName = fileName.substring(0, updatePos)
      + "_full.prog.gz";
    
    File completeFile = new File(mDataDir, completeFileName);
    try {
      // Read the update from the stream
      DayProgramFile updateProg = new DayProgramFile();
      updateProg.readFromStream(stream);
      
      // Load the complete file
      DayProgramFile completeProg = new DayProgramFile();
      completeProg.readFromFile(completeFile);
      
      // Update the complete file
      completeProg.update(updateProg);

      // Save the complete program
      completeProg.writeToFile(completeFile);
      
      // Tell the database updater that this file needs an update
      mUpdater.addUpdateJobForDayProgramFile(completeFileName);
    } catch (Exception exc) {
      throw new TvBrowserException(getClass(), "error.2",
        "Updating dayprogram file failed: {0}", completeFile.getAbsolutePath(),
        exc);
    }
  }
  
  
  public void handleFileNotFound(String fileName) throws TvBrowserException {
    // There is no update for this day -> Do nothing
  }

}
