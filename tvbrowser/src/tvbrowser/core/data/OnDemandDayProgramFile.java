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
package tvbrowser.core.data;

import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import tvdataservice.MutableChannelDayProgram;

import devplugin.*;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;

/**
 * An encapsulated access on a file containing TV data that allows an access
 * of single data fields on demand.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class OnDemandDayProgramFile {

  /**
   * The size field data must have to count as large field.
   */
  private static final int LARGE_FIELD_SIZE_LIMIT = 50;

  /** The file to load the data from. */
  private File mFile;
  
  /** The day program that holds the already loaded TV data. */
  private MutableChannelDayProgram mDayProgram;
  
  /**
   * Holds, whether this file is valid. This is not the case, when it was
   * replaced by another one
   */
  private boolean mValid;


  public OnDemandDayProgramFile(File file, Date date, Channel channel) {
    this(file, new MutableChannelDayProgram(date, channel));
  }


  public OnDemandDayProgramFile(File file, MutableChannelDayProgram dayProgram) {
    mFile = file;
    mDayProgram = dayProgram;
    
    mValid = true;
  }
  
  
  public synchronized void setValid(boolean valid) {
    mValid = valid;
  }
  
  
  public MutableChannelDayProgram getDayProgram() {
    return mDayProgram;
  }


  public synchronized void loadDayProgram()
    throws IOException, ClassNotFoundException
  {
    checkValid();
    
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(mFile);
      CountingInputStream countIn = new CountingInputStream(stream);
      ObjectInputStream objIn = new ObjectInputStream(countIn);
      
      countIn.resetOffset();

      int version = objIn.readInt();

      Date date = new Date(objIn);
      Channel channel = Channel.readData(objIn, false);

      int size = objIn.readInt();
      mDayProgram.removeAllPrograms();
      for (int i = 0; i < size; i++) {
        Program prog = loadProgram(countIn, objIn, date, channel);
        mDayProgram.addProgram(prog);
      }

      stream.close();
    }
    finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
      }
    }
  }


  public synchronized void saveDayProgram() throws IOException {
    checkValid();
    
    Date date = mDayProgram.getDate();
    Channel channel = mDayProgram.getChannel();

    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(mFile);
      ObjectOutputStream objOut = new ObjectOutputStream(stream);

      objOut.writeInt(1); // version

      date.writeData(objOut);
      channel.writeData(objOut);

      int programCount = mDayProgram.getProgramCount();
      objOut.writeInt(programCount);
      for (int i = 0; i < programCount; i++) {
        Program program = mDayProgram.getProgramAt(i);
        saveProgram(program, objOut, date, channel);
      }

      objOut.close();
      stream.close();
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
      }
    }
  }


  private Program loadProgram(CountingInputStream countIn,
    ObjectInputStream objIn, Date date, Channel channel)
    throws IOException, ClassNotFoundException
  {
    int version = objIn.readInt();

    OnDemandProgram prog = new OnDemandProgram(channel, date, this);

    if (version == 1) {
      prog.setTitle((String) objIn.readObject());
      prog.setShortInfo((String) objIn.readObject());
      prog.setDescription((String) objIn.readObject());
      prog.setTextField(
        ProgramFieldType.ACTOR_LIST_TYPE,
        (String) objIn.readObject());
      prog.setTextField(ProgramFieldType.URL_TYPE, (String) objIn.readObject());

      int minutes = objIn.readInt();
      int localHours = objIn.readInt();
      int localStartTime = localHours * 60 + minutes;
      prog.setTimeField(ProgramFieldType.START_TIME_TYPE, localStartTime);

      prog.setLength(objIn.readInt());
      prog.setInfo(objIn.readInt());

      Channel unusedChannel = Channel.readData(objIn, false);
      Date unusedDate = new devplugin.Date(objIn);

      prog.setBinaryField(
        ProgramFieldType.IMAGE_TYPE,
        (byte[]) objIn.readObject());
    } else {
      Channel unusedChannel = Channel.readData(objIn, false);
      Date unusedDate = new devplugin.Date(objIn);

      int fieldCount = objIn.readInt();
      for (int i = 0; i < fieldCount; i++) {
        int typeId = objIn.readInt();
        ProgramFieldType type = ProgramFieldType.getTypeForId(typeId);
        if (type.getFormat() == ProgramFieldType.BINARY_FORMAT) {
          int offset = countIn.getOffset();
          byte[] value = (byte[]) objIn.readObject();
          if ((value != null) && (value.length >= LARGE_FIELD_SIZE_LIMIT)) {
            prog.setLargeField(type, offset);
          } else {
            prog.setBinaryField(type, value);
          }
        } else if (type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
          int offset = countIn.getOffset();
          String value = (String) objIn.readObject();
          if ((value != null) && (value.length() >= LARGE_FIELD_SIZE_LIMIT)) {
            prog.setLargeField(type, offset);
          } else {
            prog.setTextField(type, value);
          }
        } else if (type.getFormat() == ProgramFieldType.INT_FORMAT) {
          prog.setIntField(type, objIn.readInt());
        } else if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
          prog.setTimeField(type, objIn.readInt());
        }
      }
    }

    return prog;
  }


  private void saveProgram(Program program, ObjectOutputStream objOut,
    Date localDate, Channel channel)
    throws IOException
  {
    objOut.writeInt(2); // file version

    channel.writeData(objOut);
    localDate.writeData(objOut);

    int fieldCount = program.getFieldCount();
    objOut.writeInt(fieldCount);
    Iterator iter = program.getFieldIterator();
    for (int i = 0; i < fieldCount; i++) {
      ProgramFieldType type = (ProgramFieldType) iter.next();
      objOut.writeInt(type.getTypeId());

      if (type.getFormat() == ProgramFieldType.BINARY_FORMAT) {
        objOut.writeObject(program.getBinaryField(type));
      } else if (type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
        objOut.writeObject(program.getTextField(type));
      } else if (type.getFormat() == ProgramFieldType.INT_FORMAT) {
        objOut.writeInt(program.getIntField(type));
      } else if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
        objOut.writeInt(program.getTimeField(type));
      }
    }
  }


  synchronized Object loadFieldValue(int offset) throws IOException, ClassNotFoundException {
    checkValid();
    
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(mFile);
      ObjectInputStream objIn = new ObjectInputStream(stream);

      stream.skip(offset);

      Object value = objIn.readObject();
      
      stream.close();

      return value;
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
      }
    }
  }


  /**
   * Checks whether this day program is still valid.
   * <p>
   * This is not the case if it was replaced by another one.
   * 
   * @throws IOException When the day program is not valid any more.
   */
  private void checkValid() throws IOException {
    if (! mValid) {
      throw new IOException("The day program file is invalid. Maybe it was " +
        "replaced.");
    }
  }
}
