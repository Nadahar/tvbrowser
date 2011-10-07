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
 *     $Date: 2011-03-22 18:58:35 +0100 (Di, 22 Mrz 2011) $
 *   $Author: bananeweizen $
 * $Revision: 6962 $
 */
package captureplugin.drivers.defaultdriver;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.lang.StringUtils;


/**
 * A parameter for the additional parameter list
 */
public class ParamEntry {

    /** The name */
    private String mName;
    /** parameter string for the device itself */
    private String mParam;
    /** Is the parameter enabled ? */
    private boolean mEnabled;

    /**
     * Create an empty parameter
     */
    public ParamEntry() {
        mName = "";
        mParam = "";
        mEnabled = true;
    }

    /**
     * Creates a Parameter
     * @param name Name
     * @param param Parameter
     * @param enabled True if Parameter is enabled
     */
    public ParamEntry(String name, String param, boolean enabled) {
        mName = name;
        mParam = param;
        mEnabled = enabled;
    }

    /**
     * Save data to Stream
     * @param out save to this stream
     * @throws IOException during save operation
     */
    public void writeData(ObjectOutputStream out) throws IOException {
        out.writeInt(2);
        out.writeObject(mName);
        out.writeObject(mParam);
        out.writeBoolean(mEnabled);
    }

    /**
     * Read Data from Stream
     * @param in read data from this stream
     * @throws IOException read errors
     * @throws ClassNotFoundException problems while creating classes
     */
    public void readData(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        mName = (String)in.readObject();
        mParam = (String)in.readObject();

        mEnabled = version < 2 || in.readBoolean();
    }

    /**
     * Get Name
     * @return Name of this parameter
     */
    public String getName() {
        return mName;
    }

    /**
     * Set Name
     * @param name new name of this parameter
     */
    public void setName(String name) {
        this.mName = name;
    }

    /**
     * Get device parameter
     * @return Parameter
     */
    public String getParam() {
        return mParam;
    }

    /**
     * Set device parameter
     * @param param new parameter
     */
    public void setParam(String param) {
        this.mParam = param;
    }

    /**
     * Returns Name
     */
    public String toString() {
        if (StringUtils.isEmpty(mName)) {
            return " ";
        }
        return mName;
    }

    /**
     * @return is Enabled ?
     */
    public boolean isEnabled() {
      return mEnabled;
    }

    /**
     * Enable/Disable Param
     * @param enabled true, if this param should be enabled
     */
    public void setEnabled(boolean enabled) {
      mEnabled = enabled;
    }
}
