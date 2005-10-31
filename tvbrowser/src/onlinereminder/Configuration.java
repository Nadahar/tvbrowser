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
package onlinereminder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import devplugin.Plugin;
import devplugin.Program;

/**
 * Configuration of the OnlineReminder
 * 
 * @author bodum
 */
public class Configuration {
  /** List of Programs to remind */
  private ArrayList mProgramList;
  /** List of added Programs that must be stored on the Server */
  private ArrayList mAddedPrograms;
  /** List of removed Programs that must be deleted on the Server*/
  private ArrayList mRemovedPrograms;

  /**
   * Creates a empty Configuration
   */
  public Configuration() {
    mProgramList = new ArrayList();
    mAddedPrograms = new ArrayList();
    mRemovedPrograms = new ArrayList();
  }

  /**
   * Reads a Configuration from an InputStream
   * @param in InputStream with Configuration
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public Configuration(ObjectInputStream in) throws IOException, ClassNotFoundException {
    mProgramList = new ArrayList();
    mAddedPrograms = new ArrayList();
    mRemovedPrograms = new ArrayList();
    readData(in);
  }

  /**
   * Get List of Programs
   * @return List of Programs
   */
  public List getProgramList() {
    return mProgramList;
  }

  /**
   * Add Program to the List
   * @param prog Program to add
   * @return true, if Program was added
   */
  public boolean addProgram(Program prog) {
    boolean val = false;
    if (!mProgramList.contains(prog)) {
      mAddedPrograms.add(prog);
      mProgramList.add(prog);
      val = true;
    }
    return val;
  }

  /**
   * Remove Programe from List and add it to the 
   * Remove-List
   * @param prog Program to remove
   */
  public void removeProgram(Program prog) {
    
    if (mAddedPrograms.contains(prog)) {
      mAddedPrograms.remove(prog);
      mProgramList.remove(prog);
    } else {
      mProgramList.remove(prog);
      mRemovedPrograms.add(prog);
    }
    
  }
  
  /**
   * Write the Configuration to a OutputStream
   * @param out Write Data into this Stream
   * @throws IOException
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);

    out.writeInt(mProgramList.size());

    for (int i = 0; i < mProgramList.size(); i++) {
      writeProgram(out, (Program) mProgramList.get(i));
    }

    out.writeInt(mRemovedPrograms.size());

    for (int i = 0; i < mRemovedPrograms.size(); i++) {
      writeProgram(out, (Program) mProgramList.get(i));
    }

    out.writeInt(mAddedPrograms.size());

    for (int i = 0; i < mAddedPrograms.size(); i++) {
      writeProgram(out, (Program) mProgramList.get(i));
    }

  }
  
  /**
   * Read Data from this InputStream
   * @param in Read Data from this Stream
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();

    int size = in.readInt();

    for (int i = 0; i < size; i++) {
      Program program = readProgram(in);

      if ((program != null) && (!program.isExpired()) && (!program.isOnAir())) {
        mProgramList.add(program);
      }
    }

    size = in.readInt();

    for (int i = 0; i < size; i++) {
      Program program = readProgram(in);

      if ((program != null) && (!program.isExpired()) && (!program.isOnAir())) {
        mRemovedPrograms.add(program);
      }
    }

    size = in.readInt();

    for (int i = 0; i < size; i++) {
      Program program = readProgram(in);

      if ((program != null) && (!program.isExpired()) && (!program.isOnAir())) {
        mAddedPrograms.add(program);
      }
    }
  }

  /**
   * Write a single Program to a OutputStream
   * @param out Write into this Program
   * @param program Write this Program
   * @throws IOException
   */
  private void writeProgram(ObjectOutputStream out, Program program) throws IOException {
    program.getDate().writeData(out);
    out.writeObject(program.getID());
  }

  /**
   * Read a Program from a OutputStream
   * @param in Read from this Stream
   * @return Program that was loaded, null if Program was not found
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private Program readProgram(ObjectInputStream in) throws IOException, ClassNotFoundException {
    devplugin.Date progDate = new devplugin.Date(in);
    String progId = (String) in.readObject();
    return Plugin.getPluginManager().getProgram(progDate, progId);
  }

}