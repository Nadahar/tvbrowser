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
import java.io.UnsupportedEncodingException;

import java.util.logging.Level;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramField implements Cloneable {
  
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(ProgramField.class.getName());
  
  private static final String TEXT_CHARSET = "UTF-8";

  private int mTypeId;

  private ProgramFieldType mType;
  
  private byte[] mData;
  
  private int mDataFormat;
  
  
  
  public ProgramField() {
    mDataFormat = ProgramFieldType.UNKOWN_FORMAT;
    mType = null;
  }
  
  public ProgramField(ProgramFieldType type, String text) {
    setType(type);
    setTextData(text);
   // System.out.println(text);
  }
  
  public ProgramField(ProgramFieldType type, byte[] data) {
    setType(type);
    setBinaryData(data);
  }
  
  public ProgramField(ProgramFieldType type, int value) {
    setType(type);
    setIntData(value);
   // System.out.println(value);
  }
  
  
  
  
  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException exc) {
      // This will never happen, since this class implements Cloneable
      return null;
    }
  }



  /**
   * @return
   */
  public int getTypeId() {
    return mTypeId;
  }



  /**
   * @param type
   */
  public ProgramFieldType getType() {
    if (mType == null) {
      mType = ProgramFieldType.getTypeForId(mTypeId);
    }
    
    return mType;
  }



  /**
   * @param type
   */
  public void setType(ProgramFieldType type) {
    mType = type;
    mTypeId = type.getTypeId();
  }



  /**
   * @return
   */
  public byte[] getBinaryData() {
    return mData;
  }



  /**
   * @param data
   */
  public void setBinaryData(byte[] data) {
    mDataFormat = ProgramFieldType.BINARY_FORMAT;
    
    mData = data;
  }
  
  
  
  public String getTextData() {
    try {
      return new String(mData, TEXT_CHARSET);
    }
    catch (UnsupportedEncodingException exc) {
      // This will never happen, because UTF-8 is always supported
      mLog.log(Level.SEVERE, "Charset " + TEXT_CHARSET + " is not supported", exc);
      
      return null;
    }
  }



  public void setTextData(String text) {
    mDataFormat = ProgramFieldType.TEXT_FORMAT;

    try {
      mData = text.getBytes(TEXT_CHARSET);
    }
    catch (UnsupportedEncodingException exc) {
      // This will never happen, because UTF-8 is always supported
      mLog.log(Level.SEVERE, "Charset " + TEXT_CHARSET + " is not supported", exc);
    }
  }
  
  
  
  public int getIntData() {
    return dataToInt(mData);
  }
  
  
  
  public void setIntData(int value) {
    mDataFormat = ProgramFieldType.INT_FORMAT;
    
    mData = intToData(value);
  }



  public int getTimeData() {
    return dataToInt(mData);
  }
  
  
  
  public void setTimeData(int minutesAfter1970) {
    mDataFormat = ProgramFieldType.TIME_FORMAT;
    
    mData = intToData(minutesAfter1970);
  }



  private static int dataToInt(byte[] data) {
    return (((int) data[0] & 0xFF) << (3 * 8))
         | (((int) data[1] & 0xFF) << (2 * 8))
         | (((int) data[2] & 0xFF) << (1 * 8))
         | (((int) data[3] & 0xFF) << (0 * 8));
  }



  private static byte[] intToData(int value) {
    byte[] data = new byte[4];
    
    data[0] = (byte) (value >> (3 * 8));
    data[1] = (byte) (value >> (2 * 8));
    data[2] = (byte) (value >> (1 * 8));
    data[3] = (byte) (value >> (0 * 8));
    
    return data;
  }



  private void checkFormat() throws FileFormatException {
    // Check whether the field data has the right format
    if (! getType().isRightFormat(mDataFormat)) {
      throw new FileFormatException("The field " + getType().getName()
        + " must have the " + ProgramFieldType.getFormatName(getType().getFormat())
        + " but it has the " + ProgramFieldType.getFormatName(mDataFormat));
    }
  }



  public void readFromStream(InputStream stream)
    throws IOException, FileFormatException
  {
    mTypeId = stream.read();
    mType = null;

    mDataFormat = ProgramFieldType.UNKOWN_FORMAT;
    
    int dataLength = ((stream.read() & 0xFF) << 16)
                   | ((stream.read() & 0xFF) << 8)
                   |  (stream.read() & 0xFF);
    
    mData = new byte[dataLength];
    int offset = 0;
    while (offset < dataLength) {
      int len = stream.read(mData, offset, dataLength - offset);
      if (len == -1) {
        throw new FileFormatException("Unexpected end of file");
      }
      offset += len;
    }
  }



  public void writeToStream(OutputStream stream)
    throws IOException, FileFormatException
  {
    // Check whether the field has the right format
    checkFormat();

    stream.write(mTypeId);
    
    if (mData == null) {
      stream.write(0); // Length highest byte
      stream.write(0); // Length middle byte
      stream.write(0); // Length lowest byte
    } else {
      // Write the data length
      stream.write((byte) (mData.length >> 16));
      stream.write((byte) (mData.length >> 8));
      stream.write((byte) (mData.length));
      
      // Write the data
      stream.write(mData);
    }
  }

}
