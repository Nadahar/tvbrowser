/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package devplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;


/**
 * A wrapper class for programs to add properties to the program.
 */
public class ProgramItem {
   
  private Program mProgram;
  private Properties mProperties;
    
  
  public ProgramItem(Program prog) {
    mProgram = prog;
    mProperties = null; // defer initialization until needed
  }
  
  public ProgramItem() {
    this(null);
  }

  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // version
    Date date = new Date(in);
    String progId = (String)in.readObject();
    mProgram = Plugin.getPluginManager().getProgram(date, progId);

    int keyCnt = in.readInt();
    if (keyCnt > 0) {
      mProperties = new Properties();
    }
    for (int i=0; i<keyCnt; i++) {
      String key = (String)in.readObject();
      String value = (String)in.readObject();
      mProperties.put(key, value);
    }
    
  }
  
  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    Date date = mProgram.getDate();
    date.writeData(out);
    String progId = mProgram.getID();
    out.writeObject(progId);
    
    if (mProperties == null) {
      out.writeInt(0);
    }
    else {
      Set<Object> keys = mProperties.keySet();
      out.writeInt(keys.size());
      Iterator<Object> it = keys.iterator();
      while (it.hasNext()) {
        String key = (String)it.next();
        String value = (String)mProperties.get(key);
        out.writeObject(key);
        out.writeObject(value);      
      }
    }
  }
  
  public void setProgram(Program prog) {
    mProgram = prog;
  }
  
  public Program getProgram() {
    return mProgram;
  }
  
  public void setProperty(String key, String value) {
    if (mProperties == null) {
      mProperties = new Properties();
    }
    mProperties.put(key, value);  
  }
  
  public String getProperty(String key) {
    if (mProperties == null) {
      return null;
    }
    return (String)mProperties.get(key);  
  }
  
  public String toString() {
    return mProgram.getTitle();
  }
    
}