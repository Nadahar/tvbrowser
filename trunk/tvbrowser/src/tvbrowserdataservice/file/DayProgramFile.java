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

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import devplugin.Date;

/**
 * @author Til Schneider, www.murfman.de
 */
public class DayProgramFile {
  
  private transient devplugin.Channel mChannel;
  private transient devplugin.Date mDate;
  
  public static final String[] LEVEL_ARR = {
    "base", "more16-00", "more00-16" // , "image16-00", "image00-16"
  };

  private static final int FILE_VERSION = 1;

  private int mVersion;

  private ArrayList mProgramFrameList;
  
  
  
  public DayProgramFile() {
    mVersion = 1;
    mProgramFrameList = new ArrayList();
  }
  
  public DayProgramFile(devplugin.Date date, devplugin.Channel channel) {
    this();
    mDate=date;
    mChannel=channel;
  }
  /**
   * @return
   */
  
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
    return (ProgramFrame) mProgramFrameList.get(index);
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



  public void update(DayProgramFile updateFile) throws FileFormatException {
    // Check the version
    if (updateFile.getVersion() <= getVersion()) {
      throw new FileFormatException("Update file must have a higher version ("
        + updateFile.getVersion() + " <= " + getVersion() + ")");
    }
    
    // Go through all frames in the update file
    merge(updateFile, false);
    
    // Upgrade to the new version
    setVersion(updateFile.getVersion());
  }



  public void merge(DayProgramFile otherProg) throws FileFormatException {
    merge(otherProg, false);
  }
  
  
  
  private void merge(DayProgramFile otherProg, boolean allowDeleting)
    throws FileFormatException
  {
    // Go through all frames in the merge the day programs
    for (int frameNr = 0; frameNr < otherProg.getProgramFrameCount(); frameNr++) {
      ProgramFrame frame = otherProg.getProgramFrameAt(frameNr);
      
      // Check whether this frame should be deleted
      // This is the case when the frame has no fields
      if (frame.getProgramFieldCount() == 0) {
        // Delete the frame if allowed
        if (allowDeleting) {
          int index = getProgramFrameIndexForId(frame.getId());
          if (index == -1) {
            throw new FileFormatException("The other file says that the program"
              + " frame with the ID " + frame.getId() + " should be deleted, but "
              + " there is no such frame in this file.");
          }
          removeProgramFrameAt(index);
        }
      } else {
        // Insert or update the frame
        
        // Check whether we already have such a frame
        int index = getProgramFrameIndexForId(frame.getId());
        ProgramFrame targetFrame;
        if (index == -1) {
          // This is a insert
          targetFrame = new ProgramFrame(frame.getId());
          addProgramFrame(targetFrame);
        } else {
          // This is a update
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
          if (field.getBinaryData() == null) {
            // This field should be deleted -> Do nothing
          } else {
            // This field should be updated -> Add a copy of the field
            ProgramField copy = (ProgramField) field.clone();
            targetFrame.addProgramField(copy);
          }
        }
      }
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
    GZIPInputStream gIn = new GZIPInputStream(stream);
      
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
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(file);
      
      return readVersionFromStream(stream);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
  }



  public void readFromStream(InputStream stream)
    throws IOException, FileFormatException
  {
    GZIPInputStream gIn = new GZIPInputStream(stream);
    
    int fileVersion = gIn.read();
    if (fileVersion > FILE_VERSION) {
      throw new FileFormatException("Unknown file version: " + fileVersion);
    }
    
    mVersion = gIn.read();
    
    int programCount = gIn.read();
    mProgramFrameList.clear();
    mProgramFrameList.ensureCapacity(programCount);
    for (int i = 0; i < programCount; i++) {
      ProgramFrame frame = new ProgramFrame();
      frame.readFromStream(gIn);
      mProgramFrameList.add(frame);
    }
    
    gIn.close();
  }



  public void readFromFile(File file) throws IOException, FileFormatException {
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(file);
      
      readFromStream(stream);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
  }



  public void writeToStream(OutputStream stream)
    throws IOException, FileFormatException
  {
    checkFormat();
    
    GZIPOutputStream gOut = new GZIPOutputStream(stream);

    gOut.write(FILE_VERSION);
    
    gOut.write(mVersion);
    
    gOut.write(getProgramFrameCount());
    for (int i = 0; i < getProgramFrameCount(); i++) {
      ProgramFrame frame = getProgramFrameAt(i);
      frame.writeToStream(gOut);
    }
    
    gOut.close();
  }



  public void writeToFile(File file) throws IOException, FileFormatException {
    // NOTE: We need two try blocks to ensure that the file is closed in the
    //       outer block.
    
    try {
      FileOutputStream stream = null;
      try {
        stream = new FileOutputStream(file);
        
        writeToStream(stream);
      }
      finally {
        // Close the file in every case
        if (stream != null) {
          try { stream.close(); } catch (IOException exc) {}
        }
      }
    }
    catch (IOException exc) {
      file.delete();
      throw exc;
    }
    catch (FileFormatException exc) {
      file.delete();
      throw new FileFormatException("Writing file failed "
        + file.getAbsolutePath(), exc);
    }
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
    StringBuffer buf = new StringBuffer();
    
    buf.append(date.getYear());
    buf.append("-");
    buf.append(date.getMonth() < 10 ? "0" : "");
    buf.append(date.getMonth());
    buf.append("-");
    buf.append(date.getDayOfMonth() < 10 ? "0" : "");
    buf.append(date.getDayOfMonth());
    buf.append("_");
    buf.append(country);
    buf.append("_");
    buf.append(channel);
    buf.append("_");
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
