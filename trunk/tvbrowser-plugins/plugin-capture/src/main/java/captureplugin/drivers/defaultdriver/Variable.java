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
 *     $Date: 2009-09-04 11:15:55 +0200 (Fr, 04 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5953 $
 */
package captureplugin.drivers.defaultdriver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Variables
 * 
 * @author bodum
 */
public class Variable {
  /** Value */
  private String mValue = "";
  /** Description */
  private String mDescription = "";
  
  /**
   * Read the Data from a Stream
   * @param stream Read from this Stream
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void readData(ObjectInputStream stream) throws IOException, ClassNotFoundException{
    stream.readInt(); // version not yet used
    mDescription = (String) stream.readObject();
    mValue = (String) stream.readObject();
  }

  /**
   * Write the Data to this Stream
   * @param stream Write to this Stream
   * @throws IOException
   */
  public void writeData(ObjectOutputStream stream) throws IOException {
    stream.writeInt(1);
    
    stream.writeObject(mDescription);
    stream.writeObject(mValue);
  }

  /**
   * Set the Description
   * @param description Description
   */
  public void setDescription(String description) {
    mDescription = description;
  }

  /**
   * Set the Value
   * @param value Value
   */
  public void setValue(String value) {
    mValue = value;
  }

  /**
   * Get the Description
   * @return description
   */
  public String getDescription() {
    return mDescription;
  }

  /**
   * Get the Value
   * @return Value
   */
  public String getValue() {
    return mValue;
  }

}