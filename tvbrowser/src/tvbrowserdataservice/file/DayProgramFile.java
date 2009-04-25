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
package tvbrowserdataservice.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import util.exc.TvBrowserException;
import util.io.DownloadJob;
import util.io.FileFormatException;
import util.io.IOUtilities;
import devplugin.Date;

/**
 * @author Til Schneider, www.murfman.de
 */
public class DayProgramFile extends AbstractFile {

  private transient devplugin.Channel mChannel;
  private transient devplugin.Date mDate;

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(DayProgramFile.class);

  public static final TvDataLevel[] LEVEL_ARR = new TvDataLevel[] {
    new TvDataLevel("base", mLocalizer.msg("basicTVListings","Basic TV listings"),true),
    new TvDataLevel("more00-16",mLocalizer.msg("more00-16","")),
    new TvDataLevel("more16-00",mLocalizer.msg("more16-00","")),
    new TvDataLevel("picture00-16", mLocalizer.msg("picture00-16","")),
    new TvDataLevel("picture16-00", mLocalizer.msg("picture16-00","")),
  };

  private static final int FILE_VERSION = 1;

  private static final int ADDITONAL_FILE_VERSION = 1;

  private int mVersion;

  private ArrayList<ProgramFrame> mProgramFrameList;



  public DayProgramFile() {
    mVersion = 1;
    mProgramFrameList = new ArrayList<ProgramFrame>();
  }

  public DayProgramFile(devplugin.Date date, devplugin.Channel channel) {
    this();
    mDate=date;
    mChannel=channel;
  }


  public devplugin.Date getDate() {
    return mDate;
  }

  public devplugin.Channel getChannel() {
    return mChannel;
  }

  public int getVersion() {
    return mVersion;
  }



  /**
   * @param version
   */
  public void setVersion(int version) {
    mVersion = version;
  }



  public int getProgramFrameCount() {
    return mProgramFrameList.size();
  }



  public ProgramFrame getProgramFrameAt(int index) {
    return mProgramFrameList.get(index);
  }



  public void removeProgramFrameAt(int index) {
    mProgramFrameList.remove(index);
  }



  public void removeAllProgramFrames() {
    mProgramFrameList.clear();
  }



  public void addProgramFrame(ProgramFrame frame) {
    mProgramFrameList.add(frame);
  }



  public int getProgramFrameIndexForId(int id) {
    for (int i = 0; i < getProgramFrameCount(); i++) {
      if (getProgramFrameAt(i).getId() == id) {
        return i;
      }
    }

    // Nothing found
    return -1;
  }



  /**
   * Updates this complete file with an update file.
   *
   * @param updateFile The update to use to patch this day program file.
   * @throws FileFormatException If the update file does not have a higher
   *         version as this file.
   *
   * @see #updateUpdateFile(DayProgramFile)
   * @see #merge(DayProgramFile)
   */
  public void updateCompleteFile(DayProgramFile updateFile)
    throws FileFormatException
  {
    // Go through all frames in the update file
    merge(updateFile, false, true);
  }



  /**
   * Updates this update file with an update file.
   *
   * @param updateFile The update to use to patch this day program file.
   * @throws FileFormatException If the update file does not have a higher
   *         version as this file.
   *
   * @see #updateCompleteFile(DayProgramFile)
   * @see #merge(DayProgramFile)
   */
  public void updateUpdateFile(DayProgramFile updateFile)
    throws FileFormatException
  {
    // Go through all frames in the update file
    merge(updateFile, true, true);
  }



  /**
   * Merges the day program file with a day program file of another level.
   *
   * @param otherProg The day program file to merge with this file.
   * @throws FileFormatException If merging failed.
   *
   * @see #updateCompleteFile(DayProgramFile)
   * @see #updateUpdateFile(DayProgramFile)
   */
  public void merge(DayProgramFile otherProg) throws FileFormatException {
    merge(otherProg, false, false);
  }



  /**
   * Merges two day program files.
   *
   * @param otherFile The day program file to merge with this one.
   * @param thisIsUpdateFile Specifies whether this is an update file.
   *        When an update file is updated for each empty frame in the other
   *        file a empty frame in this frame is created.
   *        When a complete file is updated for each empty frame in the other
   *        file the frame in this file is deleted.
   * @param otherIsUpdateFile Specifies whether the other file is an update
   *        file.
   *        If this file is updated, an empty frame in the other file causes a
   *        deletion of the frame (or the creation of an empty frame).
   *        If this file is merged with another complete file, an empty frame in
   *        the other file is ignored.
   * @throws FileFormatException If the version of the update file is not higher
   *         than the version of this file
   */
  private void merge(DayProgramFile otherFile, boolean thisIsUpdateFile,
    boolean otherIsUpdateFile)
    throws FileFormatException
  {
    // Check the version
    if (otherIsUpdateFile && (otherFile.getVersion() <= getVersion())) {
      throw new FileFormatException("Update file must have a higher version ("
        + otherFile.getVersion() + " <= " + getVersion() + ")");
    }

    // Go through all frames in the merge the day programs
    for (int frameNr = 0; frameNr < otherFile.getProgramFrameCount(); frameNr++) {
      ProgramFrame frame = otherFile.getProgramFrameAt(frameNr);

      // Check whether this frame is obsolete
      // This is the case when the frame is empty (= has no fields)
      if (frame.getProgramFieldCount() == 0) {
        // Check whether this is an complete or an update file
        if (thisIsUpdateFile) {
          // The new update file says that this frame is obsolete
          // -> Add an empty frame to this update file, too

          // Remove an existing frame first
          int index = getProgramFrameIndexForId(frame.getId());
          if (index != -1) {
            removeProgramFrameAt(index);
          }

          // Add the empty frame (to mark the frame as obsolete)
          addProgramFrame(new ProgramFrame(frame.getId()));
        } else {
          if (otherIsUpdateFile) {
            int index = getProgramFrameIndexForId(frame.getId());
            if (index != -1) {
              // The update says, that this frame is obsolete -> delete it
              removeProgramFrameAt(index);
            }
          } else {
            // The other file is a complete file too (maybe from another level)
            // -> Ignore empty frames
          }
        }
      } else {
        // Insert or update the frame

        // Check whether we already have such a frame
        int index = getProgramFrameIndexForId(frame.getId());
        ProgramFrame targetFrame;
        if (index == -1) {
          // This is an insert
          targetFrame = new ProgramFrame(frame.getId());
          addProgramFrame(targetFrame);
        } else {
          // This is an update
          targetFrame = getProgramFrameAt(index);
        }

        // Replace all fields the update file provides
        for (int i = 0; i < frame.getProgramFieldCount(); i++) {
          ProgramField field = frame.getProgramFieldAt(i);

          // Remove the old field, if present
          int fieldIdx = targetFrame.getProgramFieldIndexForTypeId(field.getTypeId());
          if (fieldIdx != -1) {
            targetFrame.removeProgramFieldAt(fieldIdx);
          }

          // Check whether to update or delete the field
          if (field.getBinaryData() == null && !thisIsUpdateFile) {
            // This field should be deleted -> Ignore if this is no update file
          } else {
            /* This field should be updated or deleted in an update file ->
             * Add a copy of the field
             */
            ProgramField copy = (ProgramField) field.clone();
            targetFrame.addProgramField(copy);
          }
        }
        
        /* If the frame now is empty we have to remove it to prevent
         * it is misinterpreted as a to deleting program frame */
        if(frame.getProgramFieldCount() == 0) {
          index = getProgramFrameIndexForId(frame.getId());
          
          if(index != -1) {
            removeProgramFrameAt(index);
          }
        }
      }
    }

    if (otherIsUpdateFile) {
      // Upgrade to the new version
      setVersion(otherFile.getVersion());
    }
  }



  public void checkFormat() throws FileFormatException {
    // Check whether there are duplicate program IDs
    for (int i = 0; i < getProgramFrameCount(); i++) {
      ProgramFrame frame = getProgramFrameAt(i);
      // Check whether there is a program that has the same ID than this one
      for (int j = i + 1; j < getProgramFrameCount(); j++) {
        ProgramFrame cmp = getProgramFrameAt(j);

        if (frame.getId() == cmp.getId()) {
          throw new FileFormatException("Program #" + i + " and program #" + j
            + " have the same ID: " + frame.getId());
        }
      }
    }
  }



  /**
   * Reads only the version from a stream.
   *
   * @param stream The stream to read from
   * @return The version of the file
   * @throws IOException If reading failed
   * @throws FileFormatException If the file has a unknown file version
   */
  public static int readVersionFromStream(InputStream stream)
    throws IOException, FileFormatException
  {
    InputStream gIn = IOUtilities.openSaveGZipInputStream(stream);

    int fileVersion = gIn.read();
    if (fileVersion > FILE_VERSION) {
      throw new FileFormatException("Unknown file version: " + fileVersion);
    }

    int version = gIn.read();

    gIn.close();

    return version;
  }



  public static int readVersionFromFile(File file)
    throws IOException, FileFormatException
  {
    BufferedInputStream stream = null;
    try {
      stream = new BufferedInputStream(new FileInputStream(file), 0x4000);

      return readVersionFromStream(stream);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
  }



  public void readFromStream(InputStream stream, DownloadJob job)
    throws IOException, FileFormatException
  {
    InputStream gIn = IOUtilities.openSaveGZipInputStream(stream);

    int fileVersion = gIn.read();
    if (fileVersion > FILE_VERSION) {
      throw new FileFormatException("Unknown file version: " + fileVersion);
    }

    mVersion = gIn.read();

    int programCount = gIn.read();
    
    if(programCount == 254) {
      try {
        if(job.getServerUrl() != null) {
          String url = job.getServerUrl() + (job.getServerUrl().endsWith("/") ? "" : "/") + getAdditionalFileName(job.getFileName());
          programCount = readProgCountFromStream(IOUtilities.getStream(new URL(url)));
        }
        else
          programCount = readProgCountFromStream(new BufferedInputStream(new FileInputStream(getAdditionalFileName(job.getFileName())), 0x4000));
      }catch(Exception e) {}
    }

    mProgramFrameList.clear();
    mProgramFrameList.ensureCapacity(programCount);
    for (int i = 0; i < programCount; i++) {
      ProgramFrame frame = new ProgramFrame();
      frame.readFromStream(gIn);
      mProgramFrameList.add(frame);
    }

    gIn.close();
  }

  private int readProgCountFromStream(InputStream stream) throws IOException, FileFormatException {
    InputStream gIn = IOUtilities.openSaveGZipInputStream(stream);

    gIn.read(); //version

    int count = ((gIn.read() & 0xFF) << 8 ) | (gIn.read() & 0xFF);

    gIn.close();

    return count;
  }

  private void writeProgCountToStream(OutputStream stream) throws IOException, FileFormatException {
    GZIPOutputStream gOut = new GZIPOutputStream(stream);

    gOut.write(ADDITONAL_FILE_VERSION);

    int count = getProgramFrameCount();

    gOut.write((byte) (count >> 8));
    gOut.write((byte) (count & 0xFF));

    gOut.close();
  }

  public void writeToStream(OutputStream stream, File file)
    throws IOException, FileFormatException
  {
    checkFormat();

    GZIPOutputStream gOut = new GZIPOutputStream(stream);

    gOut.write(FILE_VERSION);

    gOut.write(mVersion);

    String fileName = getAdditionalFileName(file.toString());
    
    if(new File(fileName).isFile())
      new File(fileName).delete();
    
    if(getProgramFrameCount() >= 254) {
      if(file != null) {
        FileOutputStream write = null;

        try {
          write = new FileOutputStream(fileName);
          writeProgCountToStream(write);
        }catch(Exception e) {e.printStackTrace();
          try {
            write.close();
          }catch(Exception e2) {}

          (new File(fileName)).delete();
        }
      }

      gOut.write(254);
    }
    else
      gOut.write(getProgramFrameCount());

    for (int i = 0; i < getProgramFrameCount(); i++) {
      ProgramFrame frame = getProgramFrameAt(i);
      frame.writeToStream(gOut);
    }

    gOut.close();
  }
  private String getAdditionalFileName(String fileName) {
    int index = fileName.indexOf("_update_");
    
    if(index != -1)
      return fileName.substring(0,index) + fileName.substring(index+8,fileName.indexOf(".prog.gz")) + "_additional.prog.gz";
    else
      return fileName.substring(0,fileName.indexOf(".prog.gz")) + "_additional.prog.gz";
  }
  
  public String getProgramFileName() {
    if (mChannel==null || mDate==null) return null;
    return getProgramFileName(mDate,mChannel);
  }


  public static String getProgramFileName(Date date, devplugin.Channel channel) {
    return getProgramFileName(date,channel.getCountry(),channel.getId());
  }


  public static String getProgramFileName(Date date, String country,
    String channel)
  {
    return getProgramFileName(date, country, channel, "raw", -1);
  }



  public static String getProgramFileName(Date date, String country,
    String channel, String level)
  {
    return getProgramFileName(date, country, channel, level, -1);
  }



  public static String getProgramFileName(Date date, String country,
    String channel, String level, int updateVersion)
  {
    StringBuilder buf = new StringBuilder(50);

    buf.append(date.getYear());
    buf.append('-');
    buf.append(date.getMonth() < 10 ? "0" : "");
    buf.append(date.getMonth());
    buf.append('-');
    buf.append(date.getDayOfMonth() < 10 ? "0" : "");
    buf.append(date.getDayOfMonth());
    buf.append('_');
    buf.append(country);
    buf.append('_');
    buf.append(channel);
    buf.append('_');
    buf.append(level);
    if (updateVersion > 0) {
      buf.append("_update_");
      buf.append(updateVersion);
      buf.append(".prog.gz");
    } else {
      buf.append("_full.prog.gz");
    }

    return buf.toString();
  }


  public static Date getDateFromFileName(String fileName)
    throws TvBrowserException
  {
    try {
      // E.g. '2003-10-04_de_premiere-1_base_update_15.prog.gz'
      //   or '2003-10-04_de_premiere-1_base_full.prog.gz'
      int year = Integer.parseInt(fileName.substring(0, 4));
      int month = Integer.parseInt(fileName.substring(5, 7));
      int day = Integer.parseInt(fileName.substring(8, 10));
      return new Date(year, month, day);
    }
    catch (Exception exc) {
      throw new TvBrowserException(DayProgramFile.class, "error.1",
        "Program file name has wrong syntax: {0}", fileName, exc);
    }
  }


  public static String getCountryFromFileName(String fileName)
    throws TvBrowserException
  {
    try {
      // E.g. '2003-10-04_de_premiere-1_base_update_15.prog.gz'
      //   or '2003-10-04_de_premiere-1_base_full.prog.gz'
      return fileName.substring(11, 13);
    }
    catch (Exception exc) {
      throw new TvBrowserException(DayProgramFile.class, "error.1",
        "Program file name has wrong syntax: {0}", fileName, exc);
    }
  }


  public static String getChannelNameFromFileName(String fileName)
    throws TvBrowserException
  {
    try {
      // E.g. '2003-10-04_de_premiere-1_base_update_15.prog.gz'
      //   or '2003-10-04_de_premiere-1_base_full.prog.gz'
      int channelEnd = fileName.indexOf('_', 14);
      return fileName.substring(14, channelEnd);
    }
    catch (Exception exc) {
      throw new TvBrowserException(DayProgramFile.class, "error.1",
        "Program file name has wrong syntax: {0}", fileName, exc);
    }
  }


  public static String getLevelFromFileName(String fileName)
    throws TvBrowserException
  {
    try {
      // E.g. '2003-10-04_de_premiere-1_base_update_15.prog.gz'
      //   or '2003-10-04_de_premiere-1_base_full.prog.gz'
      int channelEnd = fileName.indexOf('_', 14);
      int levelEnd = fileName.indexOf('_', channelEnd + 1);
      return fileName.substring(channelEnd + 1, levelEnd);
    }
    catch (Exception exc) {
      throw new TvBrowserException(DayProgramFile.class, "error.1",
        "Program file name has wrong syntax: {0}", fileName, exc);
    }
  }


  public static int getLevelIndexForId(String levelId) {
    for (int i = 0; i < LEVEL_ARR.length; i++) {
      if (levelId.equals(LEVEL_ARR[i].getId())) {
        return i;
      }
    }

    return -1;
  }


  public boolean equals(Object obj) {
    if (obj instanceof DayProgramFile) {
      DayProgramFile file = (DayProgramFile) obj;

      if (getProgramFrameCount() != file.getProgramFrameCount()) {
        return false;
      }

      for (int i = 0; i < getProgramFrameCount(); i++) {
        ProgramFrame frame = getProgramFrameAt(i);

        int index = file.getProgramFrameIndexForId(frame.getId());
        if (index == -1) {
          return false;
        } else {
          if (! frame.equals(file.getProgramFrameAt(index))) {
            return false;
          }
        }
      }

      return true;
    } else {
      return false;
    }
  }

}