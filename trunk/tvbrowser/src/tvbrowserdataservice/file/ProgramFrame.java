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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import devplugin.*;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramFrame implements Cloneable {

  private int mId;

  private ArrayList mProgramFieldList;
  
  
  
  public ProgramFrame(int id) {
    mId = id;
    
    mProgramFieldList = new ArrayList();
  }



  public ProgramFrame() {
    this(-1);
  }

  
  
  public Object clone() {
    try {
      ProgramFrame clone = (ProgramFrame) super.clone();
      
      // Make a deep copy of the field list
      clone.mProgramFieldList = (ArrayList) mProgramFieldList.clone();
      
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
    return (ProgramField) mProgramFieldList.get(index);
  }



  public void removeProgramFieldAt(int index) {
    mProgramFieldList.remove(index);
  }



  public boolean removeProgramFieldOfType(ProgramFieldType type) {
    int index = getProgramFieldIndexForTypeId(type.getTypeId());
    
    if (index == -1) {
      return false;
    } else {
      removeProgramFieldAt(index);
      return true;
    }
  }



  public void removeAllProgramFields() {
    mProgramFieldList.clear();
  }



  public void addProgramField(ProgramField field) {
    if (field==null) return;
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
  }



  public void writeToStream(OutputStream stream)
    throws IOException, FileFormatException
  {
    checkFormat();
    
    stream.write(mId);
    
    stream.write(getProgramFieldCount());
    for (int i = 0; i < getProgramFieldCount(); i++) {
      ProgramField field = getProgramFieldAt(i);
      field.writeToStream(stream);
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
