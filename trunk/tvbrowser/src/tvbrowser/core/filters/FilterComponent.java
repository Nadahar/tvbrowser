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
package tvbrowser.core.filters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

/**
 * An interface for the program filter system of TV-Browser. 
 */
public interface FilterComponent {
  
  /**
   * Gets the version number of a FilterComponent.
   * 
   * @return The version number of a FilterComponent.
   */
  public int getVersion();
  
  /**
   * Checks a program if it is acceptable by the FilterComponent.
   * 
   * @param program The program to check.
   * @return <code>true</code> if the program is acceptable by the FilterComponent, <code>false</code> otherwise.
   */
  public boolean accept(devplugin.Program program);
  
  /**
   * Loads the settings of a FilterComponent from an ObjectInputStream.
   * 
   * @param in The stream to read from.
   * @param version The version of the data.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException;
  
  /**
   * Saves the settings of a FilterComponent to an ObjectOutputStream.
   * 
   * @param out The stream to write to.
   * @throws IOException
   */
  public void write(ObjectOutputStream out) throws IOException;
  
  /**
   * Gets the settings panel for a FilterComponent.
   * 
   * @return The settings panel the FilterComponent.
   */
  public JPanel getSettingsPanel();
  
  /**
   * Is called when the settings should be saved (i.e. the users pressed the OK button in the settings dialog)
   */
  public void saveSettings();
  
  /**
   * Gets the name of a FilterComponent.
   * 
   * @return The name of the FilterComponent.
   */
  public String getName();
  
  /**
   * Gets the description of a FilterComponent.
   * 
   * @return The description of the FilterComponent.
   */
  public String getDescription();
  
  /**
   * Sets the name of a FilterComponent.
   * 
   * @param name The new name of the FilterComponent.
   */
  public void setName(String name);

  /**
   * Sets the description of a FilterComponent.
   * 
   * @param desc The new description of the FilterComponent.
   */
  public void setDescription(String desc);  

  /**
   * comparator for filter components (comparing by name)
   * @author bananeweizen
   * @since 2.7
   */
  public static class NameComparator implements java.util.Comparator<FilterComponent> {
    public int compare(FilterComponent arg0, FilterComponent arg1) {
      return arg0.getName().compareToIgnoreCase(arg1.getName());
    }
  }

  /**
   * comparator for filter components (comparing by string representation)
   * @author bananeweizen
   * @since 2.7
   */
  public static class TypeComparator implements java.util.Comparator<FilterComponent> {
    public int compare(FilterComponent arg0, FilterComponent arg1) {
      return (arg0).toString().compareToIgnoreCase((arg1).toString());
    }
  }
}