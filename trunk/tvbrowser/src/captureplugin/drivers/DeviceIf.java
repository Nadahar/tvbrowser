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

import devplugin.Program;

import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This Interfaces represents a Device.
 */
public interface DeviceIf extends Cloneable {
    /**
     * The Unique ID of this Device.
     *
     * Please use <code>IDGenerator.generateUniqueId()</code> to create the ID
     *
     * @return ID of this Device
     */
    public String getId();

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
     * @return LIst of programs
     */
    public Program[] getProgramList();

    /**
     * Get the List of additional Commands
     * @return List of additional Commands
     */
    public String[] getAdditionalCommands();
    
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
     * @return Clone
     */
    public Object clone();

    /**
     * Saves the Data into a Stream
     * @param stream write Data into this stream
     */
    public void writeData(ObjectOutputStream stream) throws IOException;

    /**
     * Reads the Data from a Stream
     * @param stream read data from this stream
     * @param <code>True</code> if the device should be imported.
     */
    public void readData(ObjectInputStream stream, boolean importDevice) throws IOException, ClassNotFoundException;
    
    /**
     * Checks the programs if there were updates or deletings
     * 
     * @return The deleted programs as array.
     * @since 2.11
     */
    public Program[] checkProgramsAfterDataUpdateAndGetDeleted();

    /**
     * Gets if programs that were removed during a data
     * update should be deleted automatically.
     * 
     * @return If the programs should be deleted.
     * @since 2.11
     */
    public boolean getDeleteRemovedProgramsAutomatically();
    
    /**
     * Removes programs that were deleted during a data update
     * 
     * @param p The program to remove from this device. 
     * @since 2.11
     */
    public void removeProgramWithoutExecution(Program p);
}