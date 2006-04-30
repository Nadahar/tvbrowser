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
package captureplugin.drivers.elgatodriver;

import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import util.ui.Localizer;
import captureplugin.drivers.DeviceIf;
import captureplugin.drivers.DriverIf;
import devplugin.Program;

/**
 * The Elgato-Device
 * 
 * @author bodum
 */
public class ElgatoDevice implements DeviceIf {
    /** Translator */
    private static final Localizer mLocalizer = Localizer
            .getLocalizerFor(ElgatoDevice.class);

    /** Driver */
    private ElgatoDriver mDriver;

    /** Connection */
    private ElgatoConnection mConnection = new ElgatoConnection();
    
    /** Name of Device */
    private String mName;

    /** List of Recordings */
    private Program[] mListOfRecordings;
    
    public ElgatoDevice(ElgatoDriver driver, String name) {
        mDriver = driver;
        mName = name;
    }

    public ElgatoDevice(ElgatoDevice device) {
        mDriver = (ElgatoDriver) device.getDriver();
        mName = device.getName();
    }

    public DriverIf getDriver() {
        return mDriver;
    }

    public String getName() {
        return mName;
    }

    public String setName(String name) {
        mName = name;
        return mName;
    }

    public void configDevice(Window parent) {
        // TODO Auto-generated method stub

    }

    public String[] getAdditionalCommands() {
        return new String[] { mLocalizer.msg("switchChannel",
                "Switch to Channel"), };
    }

    public boolean executeAdditionalCommand(Window parent, int num,
            Program program) {
        if (num == 0) {
            mConnection.switchToChannel(program);
        }

        return false;
    }

    public boolean isAbleToAddAndRemovePrograms() {
        return true;
    }

    public Program[] getProgramList() {
        mListOfRecordings = mConnection.getAllRecordings();
        return mListOfRecordings;
    }

    public boolean isInList(Program program) {
        if (mListOfRecordings == null) {
            mListOfRecordings = mConnection.getAllRecordings();
        }
        return Arrays.asList(mListOfRecordings).contains(program);
    }

    public boolean add(Window parent, Program program) {
        return mConnection.addToRecording(parent, program);
    }

    public boolean remove(Window parent, Program program) {
        mConnection.removeRecording(program);
        return true;
    }

    public void readData(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
    }

    public void writeData(ObjectOutputStream stream) throws IOException {
    }

    public Object clone() {
        return new ElgatoDevice(this);
    }

}