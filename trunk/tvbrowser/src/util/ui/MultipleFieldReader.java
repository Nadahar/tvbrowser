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
package util.ui;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * Reads the values of multiple fields of a program.
 * <p>
 * The clue is, that when the Reader doesn't reach a field, this field won't be
 * requested from the program.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class MultipleFieldReader extends Reader {
  
  /** The default String to use for separating fields. */
  private static final String DEFAULT_FIELD_SEPARATOR = " - ";

  /** The program to read the fields from. */
  private Program mProgram;
  
  /** The String to use for separating fields. */
  private String mFieldSeparator;

  /** The fields that should be read. */
  private ProgramFieldType[] mInfoFieldArr;
  
  /** The currently reading field. */
  private int mCurrentField;

  /** The number of fields that were already read (Includes the current one). */
  private int mReadFieldCount;
  
  /** The reader that reads the current field value. */
  private StringReader mCurrentFieldReader;


  /**
   * Creates a new instance of MultipleFieldReader.
   * 
   * @param program The program to read the fields from.
   * @param infoFieldArr The fields that should be read.
   */
  public MultipleFieldReader(Program program, ProgramFieldType[] infoFieldArr) {
    this (program, infoFieldArr, DEFAULT_FIELD_SEPARATOR);
  }
  
  
  /**
   * Creates a new instance of MultipleFieldReader.
   * 
   * @param program The program to read the fields from.
   * @param infoFieldArr The fields that should be read.
   * @param fieldSeparator The String to use for separating fields.
   */
  public MultipleFieldReader(Program program, ProgramFieldType[] infoFieldArr,
    String fieldSeparator)
  {
    mProgram = program;
    mInfoFieldArr = infoFieldArr;
    mFieldSeparator = fieldSeparator;
    
    mCurrentField = -1;
    mReadFieldCount = 0;
  }
  
  
  public int read(char cbuf[], int off, int len) throws IOException {
    while (true) {
      checkFieldReader();
      
      if (mCurrentFieldReader == null) {
        // There are no more fields
        return -1;
      } else {
        int readCount = mCurrentFieldReader.read(cbuf, off, len);
        if (readCount == -1) {
          // This field is read completely -> Go on with the next one
          mCurrentFieldReader = null;
        } else {
          return readCount;
        }
      }
    }
  }
  
  
  public int read() throws IOException {
    while (true) {
      checkFieldReader();
      
      if (mCurrentFieldReader == null) {
        // There are no more fields
        return -1;
      } else {
        int readChar = mCurrentFieldReader.read();
        if (readChar == -1) {
          // This field is read completely -> Go on with the next one
          mCurrentFieldReader = null;
        } else {
          return readChar;
        }
      }
    }
  }


  /**
   * Ensures that <code>mCurrentFieldReader</code> points to a valid reader for
   * reading field data or sets it to <code>null</code>, if there are no more
   * fields to read.
   */
  private void checkFieldReader() {
    if (mCurrentFieldReader == null) {
      mCurrentField++;

      if (mCurrentField < mInfoFieldArr.length) {
        ProgramFieldType fieldType = mInfoFieldArr[mCurrentField];
        
        String fieldValue = null;
        if (fieldType.getFormat() == ProgramFieldType.TEXT_FORMAT) {
          fieldValue = mProgram.getTextField(fieldType);
          
          if (fieldType == ProgramFieldType.SHORT_DESCRIPTION_TYPE) {
            // Special treatment for the short description:
            // If the short and the long description are shown
            // and the short description is just the cutted version of the
            // long description then don't show the short description
            
            if (containsDescription(mInfoFieldArr)) {
              String description = mProgram.getDescription();
              if (isShortVersion(fieldValue, description)) {
                fieldValue = null;
              }
            }
          }
        }
        else if (fieldType.getFormat() == ProgramFieldType.TIME_FORMAT) {
          fieldValue = mProgram.getTimeFieldAsString(fieldType);
        }
        else if (fieldType.getFormat() == ProgramFieldType.INT_FORMAT) {
          fieldValue = mProgram.getIntFieldAsString(fieldType);
        }
        
        if (fieldValue == null) {
          // This field has no value -> Go on with the next one
          checkFieldReader();
        } else {
          if (mReadFieldCount > 0) {
            fieldValue = mFieldSeparator + fieldValue;
          }
          
          mCurrentFieldReader = new StringReader(fieldValue);
          
          mReadFieldCount++;
        }
      } else {
        // There are no more fields
        mCurrentFieldReader = null;
      }
    }
  }


  /**
   * Checks whether the array of ProgramFieldTypes contains the description type.
   * 
   * @param infoFieldArr The array to check.
   * @return Whether the array contains the description type.
   */
  private boolean containsDescription(ProgramFieldType[] infoFieldArr) {
    for (ProgramFieldType element : infoFieldArr) {
      if (element == ProgramFieldType.DESCRIPTION_TYPE) {
        return true;
      }
    }
    
    return false;
  }


  /**
   * Checks whether the short description is only a short version of the long
   * description.
   * 
   * @param shortDesc The short description to check.
   * @param description The long description to check.
   * @return Whether the short description is only a short version of the long
   *         description.
   */
  private boolean isShortVersion(String shortDesc, String description) {
    if ((shortDesc == null) || (description == null)) {
      return false;
    }

    StringBuilder shortInfo = new StringBuilder(shortDesc.trim());
    
    while(shortInfo.toString().endsWith(".")) {
      shortInfo.deleteCharAt(shortInfo.length() - 1);
    }
    
    return description.trim().startsWith(shortInfo.toString());
  }

  
  public void close() {
  }

}
