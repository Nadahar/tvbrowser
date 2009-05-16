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
package tvbrowser.core;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import util.io.stream.ObjectInputStreamProcessor;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import devplugin.Channel;
import devplugin.Date;

/**
 * Remembers the day programs that are known to the user (or the user's plugins).
 * <p>
 * This way the TV data directory may be changed by other users, other
 * clients or by hand and events of these changes will be sent anyway. This
 * ensures that all plugins can react on data changed by third parties. 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TvDataInventory {

  public static final int UNKNOWN = 1;
  public static final int OTHER_VERSION = 2;
  public static final int KNOWN = 3;
  
  private HashMap<String, Integer> mInventoryHash;
  
  
  public TvDataInventory() {
    mInventoryHash = new HashMap<String, Integer>();
  }
  
  

  /**
   * Gets whether the given day program is already known to the user.
   * <p>
   * Returns
   * <ul>
   * <li><code>UNKNOWN</code> if the day program is totally unknown.</li>
   * <li><code>OTHER_VERSION</code> if the day program is known but in another
   *     version.</li>
   * <li><code>KNOWN</code> if the day program is known in the given version.</li>
   * </ul>
   * 
   * @param date The day program's date
   * @param channel The day program's channel
   * @param version The day program's version (e.g. file size)
   * @return whether the given day program is already known to the user.
   */
  public synchronized int getKnownStatus(Date date, Channel channel, int version) {
    String key = TvDataBase.getDayProgramKey(date, channel);
    Integer ver = mInventoryHash.get(key);
    
    if (ver == null) {
      return UNKNOWN;
    }
    else if (ver.intValue() != version) {
      return OTHER_VERSION;
    }
    else {
      return KNOWN;
    }
  }
  

  /**
   * Sets the day program to "known to the user".
   * 
   * @param date The day program's date
   * @param channel The day program's channel
   * @param version The day program's version (e.g. file size)
   */  
  public synchronized void setKnown(Date date, Channel channel, int version) {
    String key = TvDataBase.getDayProgramKey(date, channel);
    mInventoryHash.put(key, version);
  }


  /**
   * Sets the day program to "NOT known to the user".
   * 
   * @param date The day program's date
   * @param channel The day program's channel
   */  
  public synchronized void setUnknown(Date date, Channel channel) {
    String key = TvDataBase.getDayProgramKey(date, channel);
    mInventoryHash.remove(key);
  }

  /**
   * Sets the day program to "NOT known to the user".
   * 
   */
  public synchronized void setUnknown(final String key) {
    mInventoryHash.remove(key);
  }
  
  /**
   * Gets the keys of all known day programs.
   * <p>
   * The keys of the day programs are equal to their file name.
   * 
   * @return The keys of all known day programs.
   * @see TvDataBase#getDayProgramKey(Date, Channel)
   */
  public synchronized String[] getKnownDayPrograms() {
    String[] keyArr = new String[mInventoryHash.size()];
    mInventoryHash.keySet().toArray(keyArr);
    return keyArr;
  }

  
  /**
   * Loads the inventory list.
   * @throws IOException 
   * @throws ClassNotFoundException 
   *
   * @see #writeData(File)
   */
  public synchronized void readData(File file)
    throws IOException, ClassNotFoundException
  {
      StreamUtilities.objectInputStream(file, 0x10000,
        new ObjectInputStreamProcessor() {

          @Override
          public void process(final ObjectInputStream in) throws IOException {
            try {
              in.readInt(); // version

              mInventoryHash.clear();
              int count = in.readInt();
              for (int i = 0; i < count; i++) {
                String key = (String) in.readObject();
                Integer ver = in.readInt();
                mInventoryHash.put(key, ver);
              }
            } catch (ClassNotFoundException e) {
              e.printStackTrace();
            }
          }
        });
  }

  
  /**
   * Saves the inventory list.
   * <p>
   * It is essential that this list is saved at the shut down of TV-Browser!
   * If TV-Browser failes to save the plugin data then the inventory list should
   * NOT be saved too. Doing so the plugins will update themselves automatically
   * on the next startup, because new TV data will be treated as unknown. 
   * @throws IOException 
   *
   * @see #readData(File)
   */
  public synchronized void writeData(File file) throws IOException {
    StreamUtilities.objectOutputStream(file, new ObjectOutputStreamProcessor() {
      public void process(ObjectOutputStream out) throws IOException {
        out.writeInt(1); // version

        out.writeInt(mInventoryHash.size());
        Iterator<String> iter = mInventoryHash.keySet().iterator();
        while (iter.hasNext()) {
          String key = (String) iter.next();
          Integer ver = mInventoryHash.get(key);
          out.writeObject(key);
          out.writeInt(ver.intValue());
        }

        out.close();
      }
    });
  }

}
