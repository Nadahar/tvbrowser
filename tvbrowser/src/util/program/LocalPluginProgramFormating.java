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
 *     $Date: 2007-01-20 23:10:59 +0100 (Sa, 20 Jan 2007) $
 *   $Author: ds10 $
 * $Revision: 3037 $
 */

package util.program;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.misc.HashCodeUtilities;

/**
 * Settings for program formating. This is the class for
 * using for creating configuration used only by the
 * plugin which creates the instance of this class.
 *
 * @author René Mach
 * @since 2.5.1
 */
public class LocalPluginProgramFormating extends AbstractPluginProgramFormating {

  /**
   * Creates an empty instance of this class,
   * for example for loading the values from
   * an ObjectInputStream.
   */
  public LocalPluginProgramFormating() {
    super(null, null, null, null, null);
  }

  /**
   * Creates an instance of a program configuration.
   *
   * @param id The id of this formating.
   * @param name The name of the configuration.
   * @param titleValue The value for the title configuration.
   * @param contentValue The value for the content.
   * @param encodingValue The encoding value.
   */
  public LocalPluginProgramFormating(String id, String name, String titleValue, String contentValue, String encodingValue) {
    super("#id_local_" + id, name, titleValue, contentValue, encodingValue);
  }

  /**
   * Creates an instance of a program configuration.
   *
   * @param name The name of the configuration.
   * @param titleValue The value for the title configuration.
   * @param contentValue The value for the content.
   * @param encodingValue The encoding value.
   */
  public LocalPluginProgramFormating(String name, String titleValue, String contentValue, String encodingValue) {
    super("#id_local_" + System.currentTimeMillis(), name, titleValue, contentValue, encodingValue);
  }

  /**
   * Creates an instance of a ProgramConfiguration from an ObjetcInputStream-
   *
   * @param in The stream to read the values from
   * @throws IOException Thrown if something goes wrong
   * @throws ClassNotFoundException Thrown if something goes wrong
   */
  protected void loadData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); //read version

    setId((String)in.readObject());
    setName((String)in.readObject());
    setTitleValue((String)in.readObject());
    setContentValue((String)in.readObject());
    setEncodingValue((String)in.readObject());
  }

  /**
   * Saves this configuration in an ObjectOutputStream
   *
   * @param out The stream to save the values in
   * @throws IOException Thrown if something goes wrong
   */
  protected void storeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); //write version
    out.writeObject(getId());
    out.writeObject(getName());
    out.writeObject(getTitleValue());
    out.writeObject(getContentValue());
    out.writeObject(getEncodingValue());
  }

  public boolean equals(Object o) {
    if(o != null && o instanceof LocalPluginProgramFormating) {
      return ((LocalPluginProgramFormating)o).getId().equals(getId());
    }

    return false;
  }

  @Override
  public int hashCode() {
    return HashCodeUtilities.hash(getId());
  }

  @Override
  public boolean isValid() {
    return true;
  }
}
