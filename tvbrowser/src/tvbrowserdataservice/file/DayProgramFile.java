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

/**
 * @author Til Schneider, www.murfman.de
 */
public class DayProgramFile {
  
  private static final int FILE_VERSION = 1;

  private int mVersion;

  private ArrayList mProgramFrameList;
  
  
  
  public DayProgramFile() {
    mVersion = 1;
    mProgramFrameList = new ArrayList();
  }
  
  
  /**
   * @return
   */
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
    for (int frameNr = 0; frameNr < updateFile.getProgramFrameCount(); frameNr++) {
      ProgramFrame frame = updateFile.getProgramFrameAt(frameNr);
      
      // Check whether this frame should be deleted
      // This is the case when the frame has no fields
      if (frame.getProgramFieldCount() == 0) {
        // Delete the frame
        int index = getProgramFrameIndexForId(frame.getId());
        if (index == -1) {
          throw new FileFormatException("The update file says that the program"
            + " frame with the ID " + frame.getId() + " should be deleted, but "
            + " there is no such frame in this file.");
        }
        removeProgramFrameAt(index);
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
          ProgramField copy = (ProgramField) frame.getProgramFieldAt(i).clone();
          
          // Remove the old field, if present
          int fieldIdx = targetFrame.getProgramFieldIndexForTypeId(copy.getTypeId());
          if (fieldIdx != -1) {
            targetFrame.removeProgramFieldAt(fieldIdx);
          }
          
          // Add the copied field
          targetFrame.addProgramField(copy);
        }
      }
    }
    
    // Upgrade to the new version
    setVersion(updateFile.getVersion());
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
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(file);
      
      writeToStream(stream);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
  }

}
