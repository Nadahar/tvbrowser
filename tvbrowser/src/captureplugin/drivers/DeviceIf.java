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
package captureplugin.drivers;

import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import devplugin.Program;

/**
 * This Interfaces represents a Device.
 */
public interface DeviceIf extends Cloneable {
    /**
     * Name of the Device
     * @return Name of Device
     */
    public String getName();

    /**
     * Sets the Name of the Device
     * @param name Name of Device
     * @return new Name of Device
     */
    public String setName(String name);

    /**
     * Returns the Driver for this Device
     * @return Driver for this Device
     */
    public DriverIf getDriver();

    /**
     * Creates a Config-Dialog for this Device
     * @param parent Parent-Frame
     */
    public void configDevice(Window parent);

    /**
     * Is this Program in the List of selected Recordings?
     * @param program Program
     * @return is Progam in List?
     */
    public boolean isInList(Program program);

    /**
     * Is this Device able to add Programs?
     * @return True if able to add Programs
     */
    public boolean isAbleToAddAndRemovePrograms();
    
    /**
     * Adds a Program to the List
     * @param parent Parent-Frame
     * @param program Program to add
     * @return true if successfull
     */
    public boolean add(Window parent, Program program);

    /**
     * Removes a Program from the List
     * @param parent Parent-Frame
     * @param program Program to remove
     * @return true if successfull
     */
    public boolean remove(Window parent, Program program);

    /**
     * Get the List of Programs selected by this Device
     * @return
     */
    public Program[] getProgramList();

    /**
     * Get the List of additional Commands
     * @param program Program
     * @return List of additional Commands
     */
    public String[] getAdditionalCommands(Program program);
    
    /**
     * Execute a additional Command. The Number must corospond to the
     * List of getAdditionalCommands()
     * @param parent Parent-Frame
     * @param num Number of Command
     * @param program Program
     * @return true if successfull
     */
    public boolean executeAdditionalCommand(Window parent,int num, Program program);
    
    /**
     * Clones this Device
     * @return
     */
    public Object clone();

    /**
     * Saves the Data into a Stream
     * @param stream
     */
    public void writeData(ObjectOutputStream stream) throws IOException;

    /**
     * Reads the Data from a Stream
     * @param stream
     */
    public void readData(ObjectInputStream stream) throws IOException, ClassNotFoundException;
}