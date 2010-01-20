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
package primarydatamanager.mirrorupdater.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.logging.Logger;

import primarydatamanager.mirrorupdater.UpdateException;
import util.io.IOUtilities;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class HttpDataSource implements DataSource {

  private static final Logger mLog
    = Logger.getLogger(HttpDataSource.class.getName());

  private String mBaseUrl;
  
  private int mBytesRead;
  private int mFilesChecked;
  


  public HttpDataSource(String url) {
    mBaseUrl = url;
    if (! mBaseUrl.endsWith("/")) {
      mBaseUrl += "/";
    }
  }



  public boolean fileExists(String fileName) throws UpdateException {
    mFilesChecked++;
    
    InputStream in = null;
    try {
      in = IOUtilities.getStream(new URL(mBaseUrl + fileName), 60000);
      
      return true;
    }
    catch (IOException exc) {
      // Check whether there is a FileNotFoundException
      Throwable nested = exc;
      while (nested != null) {
        if (nested instanceof FileNotFoundException) {
          return false;
        }
        nested = nested.getCause();
      }

      // There is no FileNotFoundException -> This is a real error      
      throw new UpdateException("Checking file existence failed: "
        + mBaseUrl + fileName, exc);
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
    }
  }



  public byte[] loadFile(String fileName) throws UpdateException {
    try {
      byte[] data = IOUtilities.loadFileFromHttpServer(new URL(mBaseUrl + fileName), 60000);
      mBytesRead += data.length;
      return data;
    }
    catch (IOException exc) {
      throw new UpdateException("Loading file failed: " + mBaseUrl + fileName, exc);
    }
  }



  public void close() throws UpdateException {
    mLog.info("In total there were "
      + NumberFormat.getInstance().format(mFilesChecked) + " files checked and "
      + NumberFormat.getInstance().format(mBytesRead) + " bytes read.");
  }

}
