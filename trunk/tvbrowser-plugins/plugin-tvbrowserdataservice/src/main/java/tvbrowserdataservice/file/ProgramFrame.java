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
package tvbrowserdataservice.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import util.io.FileFormatException;
import devplugin.ProgramFieldType;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramFrame implements Cloneable {

  private int mId;

  private ArrayList<ProgramField> mProgramFieldList;
  
  
  
  public ProgramFrame(int id) {
    mId = id;
    
    mProgramFieldList = new ArrayList<ProgramField>();
  }



  public ProgramFrame() {
    this(-1);
  }

  
  
  public Object clone() {
    try {
      ProgramFrame clone = (ProgramFrame) super.clone();
      
      // Make a deep copy of the field list
      clone.mProgramFieldList = (ArrayList<ProgramField>) mProgramFieldList.clone();
      
      return clone;
    }
    catch (CloneNotSupportedException exc) {
      // This will never happen, since this class implements Cloneable
      return null;
    }
  }



  /**
   * @return id
   */
  public int getId() {
    return mId;
  }



  /**
   * @param id
   */
  public void setId(int id) {
    mId = id;
  }



  public int getProgramFieldCount() {
    return mProgramFieldList.size();
  }
  
  
  
  public ProgramField getProgramFieldAt(int index) {
    return mProgramFieldList.get(index);
  }



  public ProgramField removeProgramFieldAt(int index) {
    return mProgramFieldList.remove(index);
  }



  public ProgramField removeProgramFieldOfType(ProgramFieldType type) {
    int index = getProgramFieldIndexForTypeId(type.getTypeId());
    
    if (index == -1) {
      return null;
    } else {
      return removeProgramFieldAt(index);
    }
  }



  public void removeAllProgramFields() {
    mProgramFieldList.clear();
  }



  public void addProgramField(ProgramField field) {
    if (field==null) {
      return;
    }
    mProgramFieldList.add(field);
  }



  public int getProgramFieldIndexForTypeId(int typeId) {
    for (int i = 0; i < getProgramFieldCount(); i++) {
      if (getProgramFieldAt(i).getTypeId() == typeId) {
        return i;
      }
    }
    
    // Nothing found
    return -1;
  }



  public ProgramField getProgramFieldOfType(ProgramFieldType type) {
    int index = getProgramFieldIndexForTypeId(type.getTypeId());
    
    if (index == -1) {
      return null;
    } else {
      return getProgramFieldAt(index);
    }
  }



  private void checkFormat() throws FileFormatException {
    // Check whether the ID was set
    if (mId == -1) {
      throw new FileFormatException("The program frame's ID was not set");
    }
    
    // Check whether there are duplicate fields
    for (int i = 0; i < getProgramFieldCount(); i++) {
      ProgramField field = getProgramFieldAt(i);
      // Check whether there is a field that has the same type ID than this one
      for (int j = i + 1; j < getProgramFieldCount(); j++) {
        ProgramField cmp = getProgramFieldAt(j);
        
        if (field.getTypeId() == cmp.getTypeId()) {
          throw new FileFormatException("Program field #" + i + " and "
            + "program field #" + j + " have the same type: "
            + ProgramFieldType.getTypeForId(field.getTypeId()).getName());
        }
      }
    }
  }
  

  /**
   * Reads the data from a stream
   * 
   * @param stream The stream to read from.
   * @throws IOException Thrown if something goes wrong.
   * @throws FileFormatException Thrown if something goes wrong.
   */
  public void readFromStream(InputStream stream)
  throws IOException, FileFormatException
  {
    mId = stream.read();
  
    int fieldCount = stream.read();
    
    mProgramFieldList.clear();
    mProgramFieldList.ensureCapacity(fieldCount);
    for (int i = 0; i < fieldCount; i++) {
      ProgramField field = new ProgramField();
      field.readFromStream(stream);
      mProgramFieldList.add(field);
    }
  
    /*
     * If the id is 255 we have to read the additional
     * id from the additional ProgramField.
     */
    if(mId == 255) {
      ProgramField field = new ProgramField(null);
      try {
        field.readFromStream(stream);
        mId += field.getIntData();
      }catch(Exception e) {
          /*Ignore, maybe this is not a field that contains the id*/
        }
    }
  }



  public void writeToStream(OutputStream stream)
    throws IOException, FileFormatException
  {
    checkFormat();

    /*
     * Check if the id of this ProgramFrame is greater
     * than or equal to 255. If this is so we only write
     * 255 to stream as track value that we have an
     * additional id in a ProgramField.
     */
    if(mId >= 255) {
      stream.write(255);
    } else {
      stream.write(mId);
    }
  
    stream.write(getProgramFieldCount());
    
    for (int i = 0; i < getProgramFieldCount(); i++) {
      ProgramField field = getProgramFieldAt(i);
      field.writeToStream(stream);
    }
  
    /*
     * Write the additional ProgramField, that contains
     * the additional id value.
     */
    if(mId >= 255) {
      ProgramField additional = new ProgramField(null);
      additional.setIntData(mId-255);
      additional.writeToStream(stream, false);
    }
  }



  public boolean equals(Object obj) {
    if (obj instanceof ProgramFrame) {
      ProgramFrame frame = (ProgramFrame) obj;
      
      if (getProgramFieldCount() != frame.getProgramFieldCount()) {
        return false;
      }
      
      for (int i = 0; i < getProgramFieldCount(); i++) {
        ProgramField field = getProgramFieldAt(i);
        
        int index = frame.getProgramFieldIndexForTypeId(field.getTypeId());
        if (index == -1) {
          return false;
        } else {
          if (! field.equals(frame.getProgramFieldAt(index))) {
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
