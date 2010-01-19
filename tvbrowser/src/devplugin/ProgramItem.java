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
public class ProgramItem implements Comparable<ProgramItem> {

  private Program mProgram;
  private Properties mProperties;
  private transient String mProgId;
  private transient Date mDate;


  public ProgramItem(Program prog) {
    mProgram = prog;
    mProperties = null; // defer initialization until needed
  }

  public ProgramItem() {
    this(null);
  }

  /**
   * read the object from the input stream.
   *
   * @param in the stream to read from
   * @throws IOException if something went wrong reading the stream
   * @throws ClassNotFoundException if the object could not be deserialized
   */
  public void read(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // version
    mDate = (Date) in.readObject();
    mProgId = (String) in.readObject();

    int keyCnt = in.readInt();
    if (keyCnt > 0) {
      mProperties = new Properties();
    }
    for (int i = 0; i < keyCnt; i++) {
      String key = (String) in.readObject();
      String value = (String) in.readObject();
      mProperties.put(key, value);
    }

  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    if (mDate != null) {
      out.writeObject(mDate);
    } else {
      out.writeObject(mProgram.getDate());
    }
    if (mProgId != null) {
      out.writeObject(mProgId);
    } else {
      out.writeObject(mProgram.getID());
    }

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
    if (mProgram == null) {
      mProgram = Plugin.getPluginManager().getProgram(mDate, mProgId);
    }
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

  @Override
  public String toString() {
    return mProgram.getTitle();
  }

  /**
   * Get the date of the program. Prefer this method over
   * getProgram().getDate().
   *
   * @return date of the program
   * @since 3.0
   */
  public Date getDate() {
    if (mDate != null) {
      return mDate;
    }
    return getProgram().getDate();
  }

  /**
   * get the start time of the program in minutes after midnight
   *
   * @return start time of the program
   * @since 3.0
   */
  public int getStartTime() {
    if (mProgram != null) {
      return getProgram().getStartTime();
    }
    // all the following lines could be written using split() and parseInt(),
    // but this code is 2 orders of magnitude faster
    int underscore = mProgId.lastIndexOf('_');
    String hours = mProgId.substring(underscore + 1, underscore + 3);
    int hoursNum;
    String minutes;
    int minutesNum;
    if (hours.charAt(1) == ':') {
      hoursNum = hours.charAt(0) - '0';
      minutes = mProgId.substring(underscore + 3, underscore + 5);
    } else {
      hoursNum = Integer.parseInt(hours);
      minutes = mProgId.substring(underscore + 4, underscore + 6);
    }
    if (minutes.charAt(1) == ':') {
      minutesNum = minutes.charAt(0) - '0';
    } else {
      minutesNum = Integer.parseInt(minutes);
    }
    return hoursNum * 60 + minutesNum;
  }

  @Override
  public int compareTo(final ProgramItem other) {
    final int result = getDate().compareTo(other.getDate());
    if (result != 0) {
      return result;
    }
    int t1 = getStartTime();
    int t2 = other.getStartTime();
    if (t1 < t2) {
      return -1;
    } else if (t1 > t2) {
      return 1;
    }
    return 0;
  }

}