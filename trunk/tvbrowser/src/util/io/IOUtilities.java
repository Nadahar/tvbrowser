/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package util.io;

import java.io.*;
import java.net.*;
import java.util.zip.*;

/**
 * A utilities class for I/O stuff. It constists of serveral static
 * methods that perform some usefull things.
 *
 * @author Til Schneider, www.murfman.de
 */
public class IOUtilities {

  /** The logger for this class. */  
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(IOUtilities.class.getName());

  
  
  /**
   * Downloads a file from a HTTP server.
   *
   * @param url The URL of the file to download.
   * @param targetFile The file where to store the downloaded data.
   * @throws IOException When download or saving failed.
   */  
  public static void download(URL url, File targetFile) throws IOException {
    mLog.info("Downloading '" + url + "' to '"
      + targetFile.getAbsolutePath() + "'");
    
    InputStream stream = null;
    try {
      stream = getStream(url);
      if (stream == null) {
        throw new IOException("Can't connect to '" + url + "'!");
      }
      saveStream(stream, targetFile);
    }
    finally {
      try {
        if (stream != null) stream.close();
      } catch (IOException exc) {}
    }
  }

  
  
  /**
   * Saves the data from an InputStream into a file.
   *
   * @param stream The stream to read the data from.
   * @param targetFile The file where to store the data.
   * @throws IOException When saving failed or when the InputStream throws an
   *         IOException.
   */  
  public static void saveStream(InputStream stream, File targetFile)
    throws IOException
  {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(targetFile);
      
      pipeStreams(stream, out);
    }
    finally {
      try {
        if (out != null) out.close();
      } catch (IOException exc) {}
    }
  }
  
  
  
  /**
   * Originally copied from javax.swing.JEditorPane.
   * <p>
   * Fetches a stream for the given URL, which is about to
   * be loaded by the <code>setPage</code> method.  By
   * default, this simply opens the URL and returns the
   * stream.  This can be reimplemented to do useful things
   * like fetch the stream from a cache, monitor the progress
   * of the stream, etc.
   * <p>
   * This method is expected to have the the side effect of
   * establishing the content type, and therefore setting the
   * appropriate <code>EditorKit</code> to use for loading the stream.
   * <p>
   * If this the stream was an http connection, redirects
   * will be followed and the resulting URL will be set as
   * the <code>Document.StreamDescriptionProperty</code> so that relative
   * URL's can be properly resolved.
   *
   * @param page the URL of the page
   * @throws IOException if something went wrong.
   * @return a stream reading data from the specified URL.
   */
  public static InputStream getStream(URL page) throws IOException {
    URLConnection conn = page.openConnection();
    if (conn instanceof HttpURLConnection) {
      HttpURLConnection hconn = (HttpURLConnection) conn;
      hconn.setInstanceFollowRedirects(false);
      int response = hconn.getResponseCode();
      boolean redirect = (response >= 300 && response <= 399);

      // In the case of a redirect, we want to actually change the URL
      // that was input to the new, redirected URL
      if (redirect) {
        String loc = conn.getHeaderField("Location");
        if (loc.startsWith("http", 0)) {
          page = new URL(loc);
        } else {
          page = new URL(page, loc);
        }
        return getStream(page);
      }
    }
    
    InputStream in = conn.getInputStream();
    return in;
  }
  
  

  /**
   * Pipes all data from the specified InputStream to the specified OutputStream,
   * until the InputStream has no more data.
   * <p>
   * Note: None of the streams is closed! You have to do that for yourself!
   *
   * @param from The stream to read the data from.
   * @param to The stream to write the data to.
   */
  public static void pipeStreams(InputStream from, OutputStream to)
    throws IOException
  {
    int len;
    byte[] buffer = new byte[10240];
    while ((len = (from.read(buffer))) != -1) {
      to.write(buffer, 0, len);
    }
  }

  
  
  /**
   * Unzips a file from a ZIP-Archive (.zip, .jar).
   * <p>
   * Currently not used.
   *
   * @param srcFile The ZIP-File to read the data from.
   * @param entryName The name of the file in the ZIP-archive to unzip.
   * @param targetFile The file where to store the data.
   */  
  public static void unzip(File srcFile, String entryName, File targetFile)
    throws IOException
  {
    mLog.info("Unzipping '" + entryName + "' from '"
      + srcFile.getAbsolutePath() + "' to '" + targetFile.getAbsolutePath() + "'");
    
    InputStream stream = null;
    try {
      ZipFile zipFile = new ZipFile(srcFile);
      ZipEntry entry = new ZipEntry(entryName);

      stream = zipFile.getInputStream(entry);
      if (stream == null) {
        throw new IOException("Can't unzip '" + entryName + "' from '"
          + srcFile.getAbsolutePath() + "'!");
      }
      saveStream(stream, targetFile);
    }
    finally {
      try {
        if (stream != null) stream.close();
      } catch (IOException exc) {}
    }
  }

  
  
  /**
   * Unzips a GZIP-file (.gz).
   * <p>
   * Currently not used.
   *
   * @param srcFile The GZIP-File to unzip
   * @param targetFile The file where to store the data.
   */  
  public static void ungzip(File srcFile, File targetFile)
    throws IOException
  {
    mLog.info("Ungzipping '" + srcFile.getAbsolutePath() +
      "' to '" + targetFile.getAbsolutePath() + "'");
    
    InputStream stream = null;
    GZIPInputStream gzipStream = null;
    try {
      stream = new FileInputStream(srcFile);
      gzipStream = new GZIPInputStream(stream);
      
      saveStream(gzipStream, targetFile);
    }
    finally {
      try {
        if (gzipStream != null) gzipStream.close();
        if (stream != null) stream.close();
      } catch (IOException exc) {}
    }
  }
  
  
  
  /**
   * Appends an integer to a StringBuffer. If the length of the integer's
   * String representation is smaller than minChars, the missing chars will be
   * filled as nulls ('0') as postfix.
   *
   * @param buffer The buffer where to append the integer.
   * @param number The integer to append.
   * @param minChars The minimum number of chars.
   */
  public static void append(StringBuffer buffer, int number, int minChars) {
    String asString = Integer.toString(number);
    for (int i = asString.length(); i < minChars; i++) {
      buffer.append('0');
    }    
    buffer.append(asString);
  }

  
  
  /**
   * Replaces a substring in the specified String.
   *
   * @param original The String to replace the substring in.
   * @param pattern The pattern to replace.
   * @param str The String to replace. This string may contain the pattern.
   * @return The result.
   */  
  public static String replace(String original, String pattern, String str) {
    StringBuffer buffer = new StringBuffer(original);
    replace(buffer, pattern, str);
    return buffer.toString();
  }
  

  
  /**
   * Replaces in <code>buffer</code> the <code>pattern</code> by <code>str</code>.
   *
   * @param buffer The buffer to replace in.
   * @param pattern The pattern to replace.
   * @param str The str that should replace the pattern.
   */
  public static void replace(StringBuffer buffer, String pattern, String str) {
    int offset = 0;
    int patternIdx;
    do {
      patternIdx = buffer.indexOf(pattern, offset);
      
      if (patternIdx != -1) {
        buffer.replace(patternIdx, patternIdx + pattern.length(), str);
      }
      
      offset = patternIdx + str.length();
    } while (patternIdx != -1);
  }
  
}
