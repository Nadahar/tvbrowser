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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import tvbrowser.core.Settings;
import util.ui.TimeFormatter;

/**
 * A utilities class for I/O stuff. It constists of serveral static
 * methods that perform some usefull things.
 *
 * @author Til Schneider, www.murfman.de
 */
public class IOUtilities {

  /** The logger for this class. */
  private static final Logger mLog
    = Logger.getLogger(IOUtilities.class.getName());

  /** A Calendar for time stuff */
  private static final Calendar CALENDAR = Calendar.getInstance();

  /** Formatting the Time */
  private static final TimeFormatter TIME_FORMATTER = new TimeFormatter();

  /**
   * Downloads a file from a HTTP server.
   *
   * @param url The URL of the file to download.
   * @param targetFile The file where to store the downloaded data.
   * @throws IOException When download or saving failed.
   * @see #loadFileFromHttpServer(URL)
   */
  public static void download(URL url, File targetFile) throws IOException {
    mLog.info("Downloading '" + url + "' to '"
      + targetFile.getAbsolutePath() + "'");

    InputStream stream = null;
    try {
      stream = new BufferedInputStream(getStream(url), 0x4000);
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
    BufferedOutputStream out = null;
    try {
      out = new BufferedOutputStream(new FileOutputStream(targetFile), 0x4000);

      pipeStreams(stream, out);
    }
    finally {
      try {
        if (out != null) out.close();
      } catch (IOException exc) {}
    }
  }

  /**
   * Gets an InputStream to the given URL.
   * <p>
   * The connection has the Settings.propDefaultNetworkConnectionTimeout
   * as connection timeout.
   *
   * @param page The page to get the stream to.
   * @param followRedirects If the stream should be also established if the page not exists
   *        at the location but contains a redirect to an other location.
   * @return The stream to the page.
   * @throws IOException Thrown if something goes wrong.
   */
  public static InputStream getStream(URL page, boolean followRedirects) throws IOException {
    return getStream(page, followRedirects, Settings.propDefaultNetworkConnectionTimeout.getInt());
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
   * @param followRedirects Follow redirects.
   * @param timeout The read timeout.
   * @throws IOException if something went wrong.
   * @return a stream reading data from the specified URL.
   */
  public static InputStream getStream(URL page, boolean followRedirects, int timeout) throws IOException {
    return getStream(page, followRedirects, timeout, null, null);
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
   * @param followRedirects Follow redirects.
   * @param timeout The read timeout.
   * @param userName The user name to use for the connection.
   * @param userPassword The password to use for the connection.
   * @throws IOException if something went wrong.
   * @return a stream reading data from the specified URL.
   */
  public static InputStream getStream(URL page, boolean followRedirects, int timeout, String userName, String userPassword)
    throws IOException
  {
    URLConnection conn = page.openConnection();

    if(userName != null && userPassword != null) {
      String password = userName + ":" + userPassword;
      String encodedPassword = new String(Base64.encodeBase64(password.getBytes()));
      conn.setRequestProperty  ("Authorization", "Basic " + encodedPassword);
    }

    if (timeout > 0) {
      conn.setReadTimeout(timeout);
    }

    if (followRedirects && (conn instanceof HttpURLConnection)) {
      HttpURLConnection hconn = (HttpURLConnection) conn;
      hconn.setInstanceFollowRedirects(false);

      int response = hconn.getResponseCode();
      boolean redirect = (response >= 300 && response <= 399);

      // In the case of a redirect, we want to actually change the URL
      // that was input to the new, redirected URL
      if (redirect) {
        String loc = conn.getHeaderField("Location");
        if (loc == null) {
          throw new FileNotFoundException("URL points to a redirect without "
            + "target location: " + page);
        }
        if (loc.startsWith("http")) {
          page = new URL(loc);
        } else {
          page = new URL(page, loc);
        }
        return getStream(page, followRedirects, timeout, userName, userPassword);
      }
    }

    InputStream in = conn.getInputStream();
    return in;
  }

  /**
   * Gets an InputStream to the given URL.
   * <p>
   * The connection has the Settings.propDefaultNetworkConnectionTimeout
   * as connection timeout.
   *
   * @param page The page to get the stream to.
   * @return The stream to the page.
   * @throws IOException Thrown if something goes wrong.
   */
  public static InputStream getStream(URL page)
    throws IOException
  {
    return getStream(page, true, Settings.propDefaultNetworkConnectionTimeout.getInt());
  }

  /**
   * Gets an InputStream to the given URL.
   * <p>
   * The connection has the given timeout
   * as connection timeout.
   *
   * @param page The page to get the stream to.
   * @param timeout The timeout for the connection, use 0 for no timeout.
   * @return The stream to the page.
   * @throws IOException Thrown if something goes wrong.
   */
  public static InputStream getStream(URL page, int timeout)
  throws IOException
  {
    return getStream(page, true, timeout);
  }

  /**
   * Loads a file from a Http server.
   * <p>
   * The connection has the Settings.propDefaultNetworkConnectionTimeout
   * as connection timeout.
   *
   * @param url The URL of the file
   * @return The content of the file
   * @throws IOException When the download failed
   * @see #download(URL, File)
   */
  public static byte[] loadFileFromHttpServer(URL url) throws IOException {
    return loadFileFromHttpServer(url, Settings.propDefaultNetworkConnectionTimeout.getInt());
  }

  /**
   * Loads a file from a Http server with the given
   * read timeout.
   *
   * @param url The URL of the file
   * @param timeout The read timeout for the connection.
   * @return The content of the file
   * @throws IOException When the download failed
   * @see #download(URL, File)
   */
  public static byte[] loadFileFromHttpServer(URL url, int timeout) throws IOException {
    InputStream in = null;
    try {
      in = IOUtilities.getStream(url, timeout);
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      pipeStreams(in, out);

      out.close();
      return out.toByteArray();
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
    }
  }



  /**
   * Pipes all data from the specified InputStream to the specified OutputStream,
   * until the InputStream has no more data.
   * <p>
   * Note: None of the streams is closed! You have to do that for yourself!
   *
   * @param from The stream to read the data from.
   * @param to The stream to write the data to.
   * @throws IOException Thrown if something goes wrong.
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
   * Pipes all data from the specified Reader to the specified Writer,
   * until the Reader has no more data.
   * <p>
   * Note: The Reader and the Writer are not closed! You have to do that for
   *       yourself!
   *
   * @param reader The Reader to read the data from.
   * @param writer The Writer to write the data to.
   * @throws IOException Thrown if something goes wrong.
   */
  public static void pipe(Reader reader, Writer writer) throws IOException {
    int len;
    char[] buffer = new char[10240];
    while ((len = (reader.read(buffer))) != -1) {
      writer.write(buffer, 0, len);
    }
  }


  private static void copyFile(File src, File targetDir, boolean onlyNew) throws IOException {
    File destFile = new File(targetDir, src.getName());
    copy(src, destFile, onlyNew);
  }

  private static File createDirectory(File targetDir, String dirName) throws IOException {
    File f = new File(targetDir.getAbsolutePath()+"/"+dirName);
		if (!f.exists()) {
			if (!f.mkdirs()) {
        throw new IOException("Could not create directory '"+f.getAbsolutePath()+"'");
      }
    }
    return f;
  }

  /**
   * Copies files given in source to the target directory.
   *
   * @param src The files to copy.
   * @param targetDir The target dir of the files.
   * @throws IOException Thrown if something goes wrong.
   */
  public static void copy(File[] src, File targetDir) throws IOException {
    copy(src, targetDir, false);
  }

  /**
   * Copies files given in source to the target directory.
   *
   * @param src The files to copy.
   * @param targetDir The target dir of the files.
   * @param onlyNew Overwrite only older files.
   * @throws IOException Thrown if something goes wrong.
   * @since 2.2.2/2.5.1
   */
  public static void copy(File[] src, File targetDir, boolean onlyNew) throws IOException {
    copy(targetDir,src,targetDir, onlyNew);
  }

  private static void copy(File firstTargetDir, File[] src, File targetDir, boolean onlyNew) throws IOException {
    // src might be null, if listFiles wasn't able to read the directory
    if (src == null) {
      return;
    }
		for (int i=0; i<src.length; i++) {
			if (src[i].isDirectory() && !src[i].equals(targetDir) && !src[i].equals(firstTargetDir)) {
        File newDir = createDirectory(targetDir, src[i].getName());
        copy(firstTargetDir,src[i].listFiles(), newDir, onlyNew);
      }
      else {
        copyFile(src[i], targetDir, onlyNew);
      }
    }
  }


  /**
   * Copies a file.
   *
   * @param src The file to read from
   * @param target The file to write to
   * @throws IOException If copying failed
   */
  public static void copy(File src, File target) throws IOException {
    copy(src, target, false);
  }

  /**
   * Copies a file.
   *
   * @param src The file to read from
   * @param target The file to write to
   * @param onlyNew Overwrite only older files.
   * @throws IOException If copying failed
   * @since 2.2.2/2.5.1
   */
  public static void copy(File src, File target, boolean onlyNew) throws IOException {
    BufferedInputStream in = null;
    BufferedOutputStream out = null;
    try {
      FileOutputStream outFile = new FileOutputStream(target);
      in = new BufferedInputStream(new FileInputStream(src), 0x4000);
      out = new BufferedOutputStream(outFile, 0x4000);

      if(!onlyNew || target.length() < 1 || (src.lastModified() > target.lastModified())) {
        outFile.getChannel().truncate(0);
        pipeStreams(in, out);
      }

      in.close();
      out.close();
    }
    finally {
      if (in != null) {
        try { in.close(); } catch (IOException exc) {}
      }
      if (out != null) {
        try { out.close(); } catch (IOException exc) {}
      }
    }
  }



  /**
   * Deletes a directory with all its files and subdirectories.
   *
   * @param dir The directory to delete.
   * @throws IOException Thrown if something goes wrong.
   */
  public static void deleteDirectory(File dir) throws IOException {
    if (! dir.exists()) {
      // Nothing to do
      return;
    }

    if (! dir.isDirectory()) {
      throw new IOException("File is not a directory: " + dir.getAbsolutePath());
    }

    // Delete all the files and subdirectories
    File[] fileArr = dir.listFiles();
    if (fileArr != null) {
      for (int i = 0; i < fileArr.length; i++) {
        if (fileArr[i].isDirectory()) {
          deleteDirectory(fileArr[i]);
        } else {
          if (! fileArr[i].delete()) {
            throw new IOException("Can't delete file: " + fileArr[i].getAbsolutePath());
          }
        }
      }
    }

    // Delete the directory
    if (! dir.delete()) {
      throw new IOException("Can't delete directory: " + dir.getAbsolutePath());
    }
  }



  /**
   * L�dt eine Datei aus einem Jar-File und gibt sie zur�ck.
   * <P>
   * Ist keine Datei mit diesem Namen im Jar-File, so wird versucht, sie vom
   * Dateisystem zu laden.
   *
   * @param fileName Der Name der Datei. (Ist case-sensitive!).
   * @param srcClass Eine Klasse, aus deren Jar-File das Image geladen werden soll.
   * @return The file loaded from the jar file as byte array.
   *
   * @throws IOException Wenn ein Fehler beim Laden der Datei auftrat.
   */
  public static byte[] loadFileFromJar(String fileName, Class srcClass)
    throws IOException
  {
    if (fileName == null) throw new IllegalArgumentException("fileName == null");
    if (StringUtils.isEmpty(fileName)) throw new IllegalArgumentException("fileName is empty");
//	if (srcClass == null) srcClass = IOUtilities.class;

    // Der Dateiname muss mit einem '/' anfangen, sonst wird er nicht gefunden.

    InputStream in;

    if (srcClass==null) {
    	in=new java.io.FileInputStream(fileName);
    }
    else {
		if ((fileName.charAt(0) != '/') && (fileName.charAt(0) != '\\')) {
			fileName = "/" + fileName;
		}

		in = srcClass.getResourceAsStream(fileName);


    }
	if (in == null) {
		throw new IOException("Resource not found: '" + fileName + "'");
	}


    byte[] buffer = new byte[10240];
    byte[] data = new byte[0];
    int len;
    while ((len = in.read(buffer)) != -1) {
      // data Array vergr��ern
      byte[] oldData = data;
      data = new byte[oldData.length + len];
      System.arraycopy(oldData, 0, data, 0, oldData.length);

      // Gerade gelesene Daten anh�ngen
      System.arraycopy(buffer, 0, data, oldData.length, len);
    }

    return data;
  }



  /**
   * Unzips a file from a ZIP-Archive (.zip, .jar).
   * <p>
   * Currently not used.
   *
   * @param srcFile The ZIP-File to read the data from.
   * @param entryName The name of the file in the ZIP-archive to unzip.
   * @param targetFile The file where to store the data.
   * @throws IOException Thrown if something goes wrong.
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
   *
   * @param srcFile The GZIP-File to unzip
   * @param targetFile The file where to store the data.
   * @throws IOException Thrown if something goes wrong.
   */
  public static void ungzip(File srcFile, File targetFile)
    throws IOException
  {
    mLog.info("Ungzipping '" + srcFile.getAbsolutePath() +
      "' to '" + targetFile.getAbsolutePath() + "'");

    InputStream stream = null;
    InputStream gzipStream = null;
    try {
      stream = new BufferedInputStream(new FileInputStream(srcFile), 0x4000);
      gzipStream = openSaveGZipInputStream(stream);

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



  /**
   * Clears an StringBuffer
   *
   * @param buffer The buffer to clear
   */
  public static void clear(StringBuffer buffer) {
    buffer.delete(0, buffer.length());
  }



  /**
   * Gets the number of minutes since midnight
   * <p>
   * This method does not create any objects.
   *
   * @return The number of minutes since midnight as integer
   */
  public static int getMinutesAfterMidnight() {
    synchronized(CALENDAR) {
      CALENDAR.setTimeInMillis(System.currentTimeMillis());
      return CALENDAR.get(Calendar.HOUR_OF_DAY) * 60 + CALENDAR.get(Calendar.MINUTE);
    }
  }


  /**
   * Gets a String representation in the format h:mm for a time in minutes after
   * midnight.
   *
   * @param minutesAfterMidnight The time to get the String for
   * @return A String for the time
   */
  public static String timeToString(int minutesAfterMidnight) {
	minutesAfterMidnight = minutesAfterMidnight % (24*60);
    int hours = minutesAfterMidnight / 60;
    int minutes = minutesAfterMidnight % 60;
    return TIME_FORMATTER.formatTime(hours, minutes);
  }


  /**
   * Encodes the specified String using a simple XOR encryption.
   *
   * @param text The text to encode
   * @param seed The seed of the Random object to use for getting the keys
   * @return The encoded String
   */
  public static String xorEncode(String text, long seed) {
    java.util.Random rnd = new java.util.Random(seed);

    char[] charArr = new char[text.length()];
    for (int i = 0; i < charArr.length; i++) {
      charArr[i] = (char)(text.charAt(i) ^ rnd.nextInt()); // XOR
    }
    return new String(charArr);
  }



  /**
   * Decodes the specified String using a simple XOR encryption.
   *
   * @param text The text to encode
   * @param seed The seed of the Random object to use for getting the keys
   * @return The decoded String
   */
  public static String xorDecode(String text, long seed) {
    // We use XOR encoding -> encoding and decoding are the same
    return xorEncode(text, seed);
  }


  /**
   * Reads a number of bytes into an array.
   *
   * @param stream The stream to read from
   * @param length The number of bytes to read
   * @return An array containing the read bytes
   * @throws IOException When the end of the stream has been reached or
   *                     reading failed
   */
  public static byte[] readBinaryData(InputStream stream, int length)
    throws IOException
  {
    byte[] data = new byte[length];
    int offset = 0;
    while (offset < length) {
      int len = stream.read(data, offset, length - offset);
      if (len == -1) {
        throw new IOException("Unexpected end of stream");
      }
      offset += len;
    }

    return data;
  }


    /**
     * Reads all Bytes from a File into an byte array
     *
     * @param file Read Bytes from this File
     * @return Byte-Array or NULL if too large
     * @throws IOException Read-Exception
     *
     * @since  2.5
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            return null;
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * Writes the given image icon to the given file in the given imageType.
     *
     * @param icon The icon to write.
     * @param imageType The image type.
     * @param targetFile The file to write the image to.
     *
     * @return <code>True</code> if the file could be written, <code>false</code> if something went wrong.
     * @since 2.6
     */
    public static boolean writeImageIconToFile(ImageIcon icon, String imageType, File targetFile) {
      try {
        BufferedImage iconimage = new BufferedImage(icon.getIconWidth(),icon.getIconWidth(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = iconimage.createGraphics();
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();

        ImageIO.write(iconimage, imageType , targetFile);
      }catch(Exception e) {
        return false;
      }

      return true;
    }

    /**
     * Read the image from the given file to an icon image.
     *
     * @param srcFile The file to read from.
     *
     * @return The read icon image or <code>null</code> if something went wrong.
     * @since 2.6
     */
    public static ImageIcon readImageIconFromFile(File srcFile) {
      try {
        return new ImageIcon(ImageIO.read(srcFile));
      }catch(Exception e) {}

      return null;
    }

    /**
     * This method tries to open an inputstream as gzip and uncompresses it. If it fails,
     * a normal inputstream is returned
     *
     * @param is Inputstream that could be compressed
     * @return uncompressed inputstream
     * @throws IOException Problems during opening of the Stream
     * @since 3.0
     */
    public static InputStream openSaveGZipInputStream(final InputStream is) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(is);
        bis.mark(64);
        try {
            final InputStream result = new GZIPInputStream(bis);
            return result;
        } catch (final IOException e) {
            e.printStackTrace();
            bis.reset();
            return bis;
        }
    }

}
