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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package util.program;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Settings for program formating.
 * 
 * @author René Mach
 */
public class ProgramConfiguration {
  private String mName;
  private String mTitleValue;
  private String mContentValue;
  private String mEncodingValue;
  
  private ProgramConfiguration() {}
  
  /**
   * Creates an instance of a program configuration.
   * 
   * @param name The name of the configuration.
   * @param titleValue The value for the title configuration.
   * @param contentValue The value for the content.
   * @param encodingValue The encoding value.
   */
  public ProgramConfiguration(String name, String titleValue, String contentValue, String encodingValue) {
    mName = name;
    mTitleValue = titleValue;
    mContentValue = contentValue;
    mEncodingValue = encodingValue;
  }
  
  /**
   * Gets the name of this configuration
   * 
   * @return The name of this configuration
   */
  public String getName() {
    return mName;
  }
  
  /**
   * Gets the value for title formating
   * 
   * @return The value for title formating
   */
  public String getTitleValue() {
    return mTitleValue;
  }
  
  /**
   * Gets the value for the content formating
   * 
   * @return The value for the content formating
   */
  public String getContentValue() {
    return mContentValue;
  }
  
  /**
   * Gets the value for the formating
   * 
   * @return The value for the formating
   */
  public String getEncodingValue() {
    return mEncodingValue;
  }
  
  /**
   * Sets the name of this configuration
   * 
   * @param value The new name
   */
  public void setName(String value) {
    mName = value;
  }
  
  /**
   * Sets the title formating value
   * 
   * @param value The new title value
   */
  public void setTitleValue(String value) {
    mTitleValue = value;
  }

  /**
   * Sets the content formating value
   * 
   * @param value The new content value
   */
  public void setContentValue(String value) {
    mContentValue = value;
  }
  
  /**
   * Sets the encoding value of this configuraion
   * 
   * @param value The new encoding
   */
  public void setEncodingValue(String value) {
    mEncodingValue = value;
  }
  
  /**
   * Creates an instance of a ProgramConfiguration from an ObjetcInputStream-
   * 
   * @param in The stream to read the values from
   * @return The created ProgramConfiguration
   * @throws IOException Thrown if something goes wrong
   * @throws ClassNotFoundException Thrown if something goes wrong
   */
  public static ProgramConfiguration readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    ProgramConfiguration config = new ProgramConfiguration(); 
    
    in.readInt(); //read version
    
    config.mName = (String)in.readObject();
    config.mTitleValue = (String)in.readObject();
    config.mContentValue = (String)in.readObject();
    config.mEncodingValue = (String)in.readObject();
    
    return config;
  }
  
  /**
   * Saves this configuration in an ObjectOutputStream
   * 
   * @param out The stream to save the values in
   * @throws IOException Thrown if something goes wrong
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); //write version
    out.writeObject(mName);
    out.writeObject(mTitleValue);
    out.writeObject(mContentValue);
    out.writeObject(mEncodingValue);
  }
  
  public String toString() {
    return mName;
  }
}
