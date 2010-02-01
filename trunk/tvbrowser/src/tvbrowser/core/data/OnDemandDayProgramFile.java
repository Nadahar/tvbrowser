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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

import tvdataservice.MutableChannelDayProgram;
import util.io.BufferedRandomAccessFile;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * An encapsulated access on a file containing TV data that allows an access of
 * single data fields on demand.
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

  private boolean mTimeLimitationlData;

  public OnDemandDayProgramFile(File file, Date date, Channel channel) {
    this(file, new MutableChannelDayProgram(date, channel));
  }

  public OnDemandDayProgramFile(File file, MutableChannelDayProgram dayProgram) {
    mFile = file;
    mDayProgram = dayProgram;

    mValid = true;
    mTimeLimitationlData = false;
  }

  public synchronized void setValid(boolean valid) {
    mValid = valid;
  }

  public MutableChannelDayProgram getDayProgram() {
    return mDayProgram;
  }

  /**
   * Loads the day program for on demand access.
   *
   * @param update <code>True</code> if this is called from TV data update,
   *        <code>false</code> otherwise.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public synchronized void loadDayProgram(boolean update) throws IOException,
      ClassNotFoundException {
    checkValid();
//    System.out.println(mDayProgram.getDate().toString() + " " + mDayProgram.getChannel().getName());
    mTimeLimitationlData = !update;

    BufferedRandomAccessFile dataFile = null;
    try {
      dataFile = new BufferedRandomAccessFile(mFile, "rw");

      int version = dataFile.readInt();

      if (version < 2) {
        dataFile.close();
        updateToVersion2();
        dataFile = new BufferedRandomAccessFile(mFile, "rw");
        int ver2 = dataFile.readInt();

        if (ver2 < 2) {
          mValid = false;
          checkValid();
        }
      }

      mDayProgram.setLastProgramHadEndOnUpdate(dataFile.readBoolean());

      Date date = Date.readData(dataFile);
      Channel channel = Channel.readData(dataFile, false);

      boolean timeLimited = channel.isTimeLimited();
      int startTimeLimit = channel.getStartTimeLimit();
      int endTimeLimit = channel.getEndTimeLimit();

      int size = dataFile.readInt();
      mDayProgram.removeAllPrograms();
      for (int i = 0; i < size; i++) {
        Program prog = loadProgram(dataFile, date, channel);

        if (prog != null) {
          int time = prog.getHours() * 60 + prog.getMinutes();
          if (timeLimited && !update) {
            if ((startTimeLimit < endTimeLimit && time >= startTimeLimit && time < endTimeLimit)
                || (endTimeLimit < startTimeLimit && (time < endTimeLimit || time >= startTimeLimit))) {
              mDayProgram.addProgram(prog);
            }
          }
          else {
            mDayProgram.addProgram(prog);
          }
        }else {
          break;
        }
      }

      dataFile.close();
    } finally {
      if (dataFile != null) {
        try {
          dataFile.close();
        } catch (IOException exc) {}
      }
    }
  }

  /**
   * Does an update of the version 1 on demand data file to version 2.
   *
   * @throws IOException
   * @throws ClassNotFoundException
   *
   * @since 2.2
   */
  private void updateToVersion2() throws IOException, ClassNotFoundException {
    BufferedInputStream stream = null;
    try {
      stream = new BufferedInputStream(new FileInputStream(mFile), 0x10000);
      ObjectInputStream objIn = new ObjectInputStream(stream);

      objIn.readInt();

      Date date = Date.readData(objIn);
      Channel channel = Channel.readData(objIn, false);

      int size = objIn.readInt();
      mDayProgram.removeAllPrograms();
      for (int i = 0; i < size; i++) {
        Program prog = loadProgram(objIn, date, channel);
        mDayProgram.addProgram(prog);
      }

      stream.close();
      saveDayProgram(true);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {}
      }
      File newFile = new File(mFile.getPath() + "_to_ver_2_update");
      mFile.delete();
      newFile.renameTo(mFile);
      mValid = true;
    }
  }

  /**
   * Loads the data of the old on demand data file version.
   *
   * @param objIn
   * @param date
   * @param channel
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private Program loadProgram(ObjectInputStream objIn, Date date,
      Channel channel) throws IOException, ClassNotFoundException {
    int version = objIn.readInt();

    OnDemandProgram prog = new OnDemandProgram(channel, date, this);

    if (version == 1) {
      prog.setTitle((String) objIn.readObject());
      prog.setShortInfo((String) objIn.readObject());
      prog.setDescription((String) objIn.readObject());
      prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE, (String) objIn
          .readObject());
      prog.setTextField(ProgramFieldType.URL_TYPE, (String) objIn.readObject());

      int minutes = objIn.readInt();
      int localHours = objIn.readInt();
      int localStartTime = localHours * 60 + minutes;
      prog.setTimeField(ProgramFieldType.START_TIME_TYPE, localStartTime);

      prog.setLength(objIn.readInt());
      prog.setInfo(objIn.readInt());

      Channel.readData(objIn, false); // unused channel
      devplugin.Date.readData(objIn); // unused date

      prog.setBinaryField(ProgramFieldType.PICTURE_TYPE, (byte[]) objIn
          .readObject());
    } else {
      Channel.readData(objIn, false); // unused channel
      devplugin.Date.readData(objIn); // unused date

      int fieldCount = objIn.readInt();
      for (int i = 0; i < fieldCount; i++) {
        int typeId = objIn.readInt();
        ProgramFieldType type = ProgramFieldType.getTypeForId(typeId);
        if (type.getFormat() == ProgramFieldType.BINARY_FORMAT) {
          byte[] value = (byte[]) objIn.readObject();
          prog.setBinaryField(type, value);
        } else if (type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
          String value = (String) objIn.readObject();
          prog.setTextField(type, value);
        } else if (type.getFormat() == ProgramFieldType.INT_FORMAT) {
          prog.setIntField(type, objIn.readInt());
        } else if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
          prog.setTimeField(type, objIn.readInt());
        }
      }
    }

    prog.setProgramLoadingIsComplete();

    return prog;
  }

  /**
   * Saves the day program to the on demand data file.
   *
   * @throws IOException
   */
  public synchronized void saveDayProgram() throws IOException {
    saveDayProgram(false);
  }

  private void saveDayProgram(final boolean update) throws IOException {
    checkValid();

    Date date = mDayProgram.getDate();
    Channel channel = mDayProgram.getChannel();

    RandomAccessFile dataFile = null;
    try {
      if (update)
      {
        dataFile = new RandomAccessFile(mFile + "_to_ver_2_update", "rw");
      }
      else
      {
        dataFile = new RandomAccessFile(mFile, "rw");
      }

      dataFile.writeInt(2); // version

      dataFile.writeBoolean(mDayProgram.getLastProgramHadEndOnUpdate());

      date.writeData(dataFile);
      channel.writeToDataFile(dataFile);

      int programCount = mDayProgram.getProgramCount();
      dataFile.writeInt(programCount);
      for (int i = 0; i < programCount; i++) {
        Program program = mDayProgram.getProgramAt(i);
        saveProgram(program, dataFile);
      }

      dataFile.close();
    } finally {
      if (dataFile != null) {
        try {
          dataFile.close();
        } catch (final IOException exc) {}
      }
    }
  }

  /**
   * Loads the data from a RandomAccessFile.
   *
   * @since 2.2
   */
  private Program loadProgram(RandomAccessFile dataFile, Date date,
      Channel channel) throws IOException, ClassNotFoundException {
    int version = dataFile.readInt();

    OnDemandProgram prog = new OnDemandProgram(channel, date, this);

    if (version == 3) {
      int fieldCount = dataFile.readInt();

      for (int i = 0; i < fieldCount; i++) {
        int typeId = dataFile.readInt();
        ProgramFieldType type = ProgramFieldType.getTypeForId(typeId);

        if (type.getFormat() == ProgramFieldType.UNKNOWN_FORMAT)
          return null;
        else if (type.getFormat() == ProgramFieldType.BINARY_FORMAT) {
          long position = dataFile.getFilePointer();

          int n = dataFile.readInt();

          byte[] value = new byte[n];
          dataFile.readFully(value);

          if ((value != null) && (n >= LARGE_FIELD_SIZE_LIMIT)) {
            prog.setLargeField(type, position);
          } else {
            prog.setBinaryField(type, value);
          }
        } else if (type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
          long position = dataFile.getFilePointer();
          String value = dataFile.readUTF();
          if (value != null) {
            if (value.length() >= LARGE_FIELD_SIZE_LIMIT) {
              prog.setLargeField(type, position);
            }
            else {
              if (value.length() > 0) {
                prog.setTextField(type, value);
              }
            }
          }
        } else if (type.getFormat() == ProgramFieldType.INT_FORMAT) {
          prog.setIntField(type, dataFile.readInt());
        } else if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
          prog.setTimeField(type, dataFile.readInt());
        }
      }
    }

    prog.setProgramLoadingIsComplete();

    return prog;
  }

  private void saveProgram(Program program, RandomAccessFile dataFile) throws IOException {
    dataFile.writeInt(3); // file version

    int fieldCount = program.getFieldCount();
    dataFile.writeInt(fieldCount);
    Iterator<ProgramFieldType> iter = program.getFieldIterator();
    for (int i = 0; i < fieldCount; i++) {
      ProgramFieldType type = iter.next();
      dataFile.writeInt(type.getTypeId());

      if (type.getFormat() == ProgramFieldType.BINARY_FORMAT) {
        byte[] b = program.getBinaryField(type);
        dataFile.writeInt(b.length);
        dataFile.write(b);
      } else if (type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
        dataFile.writeUTF(program.getTextField(type));
      } else if (type.getFormat() == ProgramFieldType.INT_FORMAT) {
        dataFile.writeInt(program.getIntField(type));
      } else if (type.getFormat() == ProgramFieldType.TIME_FORMAT) {
        dataFile.writeInt(program.getTimeField(type));
      }
    }
  }

  synchronized Object loadFieldValue(long position, ProgramFieldType type)
      throws IOException, ClassNotFoundException {
    checkValid();

    RandomAccessFile dataFile = null;
    try {
      dataFile = new RandomAccessFile(mFile, "r");
      dataFile.seek(position);

      Object value;

      if (type.getFormat() == ProgramFieldType.TEXT_FORMAT) {
        value = dataFile.readUTF();
      } else {
        int n = dataFile.readInt();

        byte[] b = new byte[n];
        dataFile.readFully(b);

        value = b;
      }

      dataFile.close();

      return value;
    } finally {
      if (dataFile != null) {
        try {
          dataFile.close();
        } catch (IOException exc) {}
      }
    }
  }

  /**
   * Checks whether this day program is still valid.
   * <p>
   * This is not the case if it was replaced by another one.
   *
   * @throws IOException
   *           When the day program is not valid any more.
   */
  private void checkValid() throws IOException {
    if (!mValid) {
      throw new IOException("The day program file is invalid. Maybe it was "
          + "replaced.");
    }
  }

  /**
   * Gets if this file data file is loaded for data base.
   *
   * @return <code>True</code> if this data is used in program table,
   *         <code>false</code> if this data is used for data update.
   * @since 2.2.4/2.6
   */
  public boolean isTimeLimitationData() {
    return mTimeLimitationlData;
  }

  /**
   * Calculates the time limits of this file.
   *
   * @since 2.2.4/2.6
   */
  public void calculateTimeLimits() {
    if(mDayProgram.getChannel().isTimeLimited()) {
      ArrayList<Program> programs = new ArrayList<Program>();

      for(int i = 0; i < mDayProgram.getProgramCount(); i++) {
        programs.add(mDayProgram.getProgramAt(i));
      }

      mDayProgram.removeAllPrograms();

      Channel channel = mDayProgram.getChannel();

      for(Program prog : programs) {
        int time = prog.getHours() * 60 + prog.getMinutes();

        int startTimeLimit = channel.getStartTimeLimit();
        int endTimeLimit = channel.getEndTimeLimit();

        if((startTimeLimit < endTimeLimit && time >= startTimeLimit && time < endTimeLimit) ||
            (endTimeLimit < startTimeLimit && (time < endTimeLimit || time >= startTimeLimit))) {
          mDayProgram.addProgram(prog);
        }
      }
    }

    mTimeLimitationlData = true;
  }
}
