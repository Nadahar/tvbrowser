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
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */
package captureplugin.drivers.dreambox.connector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import captureplugin.utils.ExternalChannelIf;

/**
 * A channel in the dreambox
 */
public class DreamboxChannel implements ExternalChannelIf {
    /** Service Reference */
    private String mReference;
    /** Name */
    private String mName;

    /** Name of the Bouqet */
    private String mBouqetName = "";

    /**
     * Create the DreamboxChannel
     * @param ref Service Reference
     * @param name Name
     * @param bouqetName Name of the bouqet
     */
    public DreamboxChannel(String ref, String name, String bouqetName) {
        mReference = ref;
        mName = name;
        mBouqetName = bouqetName;
    }

    /**
     * Read the Data from a stream
     * @param stream read from this stream
     * @throws IOException io errors
     */
    public DreamboxChannel(ObjectInputStream stream) throws IOException {
        readData(stream);
    }

    /**
     * @return Service Reference
     */
    public String getReference() {
        return mReference;
    }

    /**
     * @return Name
     */
    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return getName()  + "  ( " + mBouqetName +" )";
    }

    /**
     * Write Data to stream
     * @param stream write to this stream
     * @throws IOException io errors
     */
    public void writeData(ObjectOutputStream stream) throws IOException {
        stream.writeInt(2);
        stream.writeUTF(mReference);
        stream.writeUTF(mName);
        stream.writeUTF(mBouqetName);
    }

    /**
     * Read Data from stream
     * @param stream read from this stream
     * @throws IOException io errors
     */
    public void readData(ObjectInputStream stream) throws IOException {
        int version = stream.readInt();
        mReference = stream.readUTF();
        mName = stream.readUTF();

        if (version >= 2) {
            mBouqetName = stream.readUTF();
        }
    }

}
