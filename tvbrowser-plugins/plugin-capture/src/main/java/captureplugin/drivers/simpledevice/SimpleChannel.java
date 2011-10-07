/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.simpledevice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import captureplugin.utils.ExternalChannelIf;

/**
 * This Class represents an external channel
 * 
 * @author bodum
 */
public class SimpleChannel implements ExternalChannelIf {
    /** Number of the Channel */
    private int mNumber;
    /** Name of the Channel */
    private String mName;

    /**
     * Create the Channel
     * @param number Number of the Channel
     * @param name Name of the Channel
     */
    public SimpleChannel(int number, String name) {
        mNumber = number;
        mName = name;
    }

    /**
     * Create the Channel from a Stream
     * @param stream load Settings from this Stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public SimpleChannel(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      readData(stream);
    }

    /**
     * @return get the Name of the Channel
     */
    public String getName() {
        return mName;
    }

    /**
     * @param name Set the Name of the Channel
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * @return get the Number of the Channel
     */
    public int getNumber() {
        return mNumber;
    }

    /**
     * @param number Set the Number of the Channel
     */
    public void setNumber(int number) {
        mNumber = number;
    }

    public String toString() {
        return mName;
    }
    
    public int hashCode() {
      final int PRIME = 31;
      int result = 1;
      result = PRIME * result + ((mName == null) ? 0 : mName.hashCode());
      result = PRIME * result + mNumber;
      return result;
    }

    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final SimpleChannel other = (SimpleChannel) obj;
      if (mName == null) {
        if (other.mName != null) {
          return false;
        }
      } else if (!mName.equals(other.mName)) {
        return false;
      }
      if (mNumber != other.mNumber) {
        return false;
      }
      return true;
    }

    /**
     * Read Settings
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readData(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.readInt(); // version not yet used
      mNumber = stream.readInt();
      mName = stream.readUTF();
    }

    /**
     * Store Settings
     * @param stream
     * @throws IOException
     */
    public void writeData(ObjectOutputStream stream) throws IOException {
      stream.writeInt(1);
      stream.writeInt(mNumber);
      stream.writeUTF(mName);
    }
}