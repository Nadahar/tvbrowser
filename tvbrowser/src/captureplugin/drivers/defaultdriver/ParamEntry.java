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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin.drivers.defaultdriver;

import java.io.IOException;
import java.io.ObjectOutputStream;


/**
 * An for the additional Param-List
 */
public class ParamEntry {

    /** The name */
    private String mName;
    /** The Param */
    private String mParam;
    
    /**
     * Createa a empty Param
     */
    public ParamEntry() {
        mName = "";
        mParam = "";
    }
    
    /**
     * Creates a Param
     * @param name Name
     * @param param Param
     */
    public ParamEntry(String name, String param) {
        mName = name;
        mParam = param;
    }
    
    /**
     * Save data to Stream
     * @param out
     * @throws IOException
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeInt(1);
        out.writeObject(mName);
        out.writeObject(mParam);
    }
    
    /**
     * Read Data from Stream
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readData(java.io.ObjectInputStream in)throws IOException, ClassNotFoundException {
        int version = in.readInt();
        mName = (String)in.readObject();
        mParam = (String)in.readObject();
    }
    
    /**
     * Get Name
     * @return
     */
    public String getName() {
        return mName;
    }
    
    /**
     * Set Name
     * @param name
     */
    public void setName(String name) {
        this.mName = name;
    }
    
    /**
     * Get Param
     * @return
     */
    public String getParam() {
        return mParam;
    }
    
    /**
     * Set Param
     * @param param
     */
    public void setParam(String param) {
        this.mParam = param;
    }
    
    /**
     * Returns Name
     */
    public String toString() {
        if (mName.length() == 0) {
            return " ";
        }
        return mName;
    }
}
