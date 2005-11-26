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

import devplugin.Plugin;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramItem;

/**
 * Configuration of the OnlineReminder
 * 
 * @author bodum
 */
public class Configuration {
  /** List of added Programs that must be stored on the Server */
  private ArrayList mAddedPrograms;
  /** List of removed Programs that must be deleted on the Server*/
  private ArrayList mRemovedPrograms;

  /** Root-Node */
  private PluginTreeNode mRoot;
  
  private int mDefaultTime = 5;
  
  /**
   * Creates a empty Configuration
   * @param root Root-Node of this Plugin
   */
  public Configuration(PluginTreeNode root) {
    mRoot = root;
    mAddedPrograms = new ArrayList();
    mRemovedPrograms = new ArrayList();
  }

  /**
   * Reads a Configuration from an InputStream
   * @param root Root-Node of this Plugin
   * @param in InputStream with Configuration
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public Configuration(PluginTreeNode root, ObjectInputStream in) throws IOException, ClassNotFoundException {
    mAddedPrograms = new ArrayList();
    mRemovedPrograms = new ArrayList();
    mRoot = root;
    readData(in);
  }

  /**
   * Add Program to the List
   * @param prog Program to add
   * @return true, if Program was added
   */
  public boolean addProgram(Program prog) {
    return addProgram(prog, mDefaultTime, true);
  }

  public boolean addProgram(Program prog, boolean updateTree) {
    return addProgram(prog, mDefaultTime, updateTree);
  }

  public boolean addProgram(Program prog, int reminderTime, boolean updateTree) {
    boolean val = false;
    if (!mRoot.contains(prog, true)) {
      ReminderProgramItem item = new ReminderProgramItem(prog, reminderTime);
      mRoot.addProgram(item);
      mAddedPrograms.add(prog);
      val = true;
    }
    
    if (updateTree) {
      mRoot.update();
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
      mRoot.removeProgram(prog);
    } else {
      mRoot.removeProgram(prog);
      mRemovedPrograms.add(prog);
    }
    mRoot.update();
  }
  
  /**
   * Write the Configuration to a OutputStream
   * @param out Write Data into this Stream
   * @throws IOException
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);

    Object[] obj = mRoot.getProgramItems();
    
    for (int i = 0;i<obj.length;i++) {
      System.out.println(obj[i].getClass().getName());
    }
    
    ProgramItem[] prgList = (ProgramItem[])mRoot.getProgramItems();
    
    out.writeInt(prgList.length);

    for (int i = 0; i < prgList.length; i++) {
      writeProgram(out, ((ReminderProgramItem) prgList[i]).getProgram());
      out.writeInt(((ReminderProgramItem)prgList[i]).getMinutes());
    }

    out.writeInt(mRemovedPrograms.size());

    for (int i = 0; i < mRemovedPrograms.size(); i++) {
      writeProgram(out, (Program) mRemovedPrograms.get(i));
    }

    out.writeInt(mAddedPrograms.size());

    for (int i = 0; i < mAddedPrograms.size(); i++) {
      writeProgram(out, (Program) mAddedPrograms.get(i));
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

      int minutes = in.readInt();
      
      if ((program != null) && (!program.isExpired()) && (!program.isOnAir())) {
        ReminderProgramItem item = new ReminderProgramItem(program, minutes);
        mRoot.addProgram(item);
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
    
    mRoot.update();
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

  public ProgramItem[] getProgramItems() {
    return mRoot.getProgramItems();
  }
  
  public Program[] getPrograms() {
    return mRoot.getPrograms();
  }
}