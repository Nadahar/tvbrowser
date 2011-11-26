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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */
package primarydatamanager.mirrorupdater.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import primarydatamanager.mirrorupdater.UpdateException;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class FtpDataTarget implements DataTarget {

  private static final Logger mLog
    = Logger.getLogger(FtpDataTarget.class.getName());

  private FTPClient mFTPClient;
  private String mServerUrl;
  private String mPath;
  private int mPort;
  private String mUser, mPassword;
  private long mBytesWritten;
  
  

  
  public FtpDataTarget(String serverUrl, String path, int port, String user,
    String password)
    throws UpdateException
  {
    
    // Cut off the protocol (we know, it's FTP)
    if (serverUrl.startsWith("ftp://")) {
      serverUrl = serverUrl.substring(6);
    }

    mServerUrl = serverUrl;
    mPath = path;
    mPort = port;
    mUser = user;
    mPassword = password;
    
    mFTPClient=null;
    reset();
   
  }


  private void reset() throws UpdateException {
    if (mFTPClient!=null && mFTPClient.isConnected()) {
      try {
        mFTPClient.disconnect();
      } catch (IOException e) {
        // ignore
      }
    }
    
    // Connect to the server
       mFTPClient = new FTPClient();
       try {
         mFTPClient.connect(mServerUrl, mPort);
         mLog.fine("Connected to " + mServerUrl + ":" + mPort);
         mFTPClient.setSoTimeout(30000);
         checkReplyCode();
       }
       catch(Exception exc) {
         if (mFTPClient.isConnected()) {
           try {
             mFTPClient.disconnect();
           } catch(IOException exc2) {
             // do nothing
           }
         }
         throw new UpdateException("Could not connect to server '" + mServerUrl
                                   + "'", exc);
       }
    
       // Log in
       try {
         boolean success = mFTPClient.login(mUser, mPassword);
         checkReplyCode();
         if (! success) {
           throw new UpdateException("Login failed");
         }
       } catch (Exception exc) {
         throw new UpdateException("Login using user='" + mUser
             + "' and password=(" + mPassword.length() + " characters) failed", exc);
       }
    
       // Set the file type to binary
       try {
         boolean success = mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
         checkReplyCode();
         if (! success) {
           throw new UpdateException("Setting file type to binary failed");
         }
       } catch (Exception exc) {
         throw new UpdateException("Setting file type to binary failed", exc);
       }
    
       // Change directory
       try {
         boolean success = mFTPClient.changeWorkingDirectory(mPath);
         checkReplyCode();
         if (! success) {
           throw new UpdateException("Could not change to directory '" + mPath + "'");
         }
       } catch (Exception exc) {
         throw new UpdateException("Could not change to directory '" + mPath + "'",
                                   exc);
       }
       mLog.fine("Changed to directory " + mPath);
    
  }

  private void checkReplyCode() throws UpdateException {
    // Check the reply code to verify success.
    int reply = mFTPClient.getReplyCode();
      
    if (! FTPReply.isPositiveCompletion(reply)) {
      throw new UpdateException("FTP server '" + mServerUrl
        + "' sent negative completion. Reply: " + reply + ": "
        + mFTPClient.getReplyString());
    }
  }



  public String[] listFiles() throws UpdateException {
    FTPFile[] fileArr;
    try {
      fileArr = mFTPClient.listFiles();
      checkReplyCode();
    } catch (Exception exc) {
      throw new UpdateException("Getting file list failed", exc);
    }
    
    if (fileArr == null) {
      return new String[0];
    } else {
      String[] fileNameArr = new String[fileArr.length];
      for (int i = 0; i < fileArr.length; i++) {
        fileNameArr[i] = fileArr[i].getName();
      }
      
      return fileNameArr;
    }
  }



  public void deleteFile(String fileName) throws UpdateException {
    try {
      boolean success = mFTPClient.deleteFile(fileName);
      checkReplyCode();
      if (! success) {
        throw new UpdateException("Could not delete file '" + fileName + "'");
      }
    } catch (Exception exc) {
      throw new UpdateException("Could not delete file '" + fileName + "'", exc);
    }
  }



  public void writeFile(String fileName, byte[] data) throws UpdateException {
    
    boolean success=false;
    int tryCount=3;
    while (!success && tryCount>0) {
      try {
        
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        success = mFTPClient.storeFile(fileName, stream);
        checkReplyCode();
        if (!success) {
          throw new UpdateException("Could not write file '" + fileName + "'");
        }
      } catch (Exception exc) {
        // Try to delete the corrupt file
        try {
          deleteFile(fileName);
        }
        catch (Exception exc2) {
          // Do nothing
        }
        tryCount--;
        success=false;
        System.err.println("Could not write file '"+fileName+"'. Reason: "+exc.getMessage());
        if (tryCount>0) {
          System.err.println("trying again...");
          reset();
        }
      }
    }
    
    if (success) {
      mBytesWritten += data.length;
    } else {
      throw new UpdateException("Could not write file '" + fileName + "'");
    }
  }



  public void close() throws UpdateException {
    if (mFTPClient.isConnected()) {
      try {
        mFTPClient.disconnect();
        checkReplyCode();
      } catch(Exception exc) {
        throw new UpdateException("Could not disconnect from server '"
          + mServerUrl + "'", exc);
      }
    }
    
    mLog.fine("Disconnected from " + mServerUrl);
    mLog.info("In total there were "
      + NumberFormat.getInstance().format(mBytesWritten) + " bytes written.");
  }

}
