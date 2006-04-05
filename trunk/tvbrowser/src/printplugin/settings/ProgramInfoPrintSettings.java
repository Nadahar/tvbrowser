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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package printplugin.settings;

import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import devplugin.ProgramFieldType;

/**
 * The settings for the printing of the program info.
 *  
 * @author René Mach
 *
 */
public class ProgramInfoPrintSettings {

  private Font mFont;
  private ProgramFieldType[] mAllFields;
  private ProgramFieldType[] mFieldTypes;
  private static ProgramInfoPrintSettings mInstance;
  
  private ProgramInfoPrintSettings() {
    mInstance = this;
    mFont = new Font("Dialog",Font.PLAIN,12);
    
    Iterator it = ProgramFieldType.getTypeIterator();
    
    ArrayList programFields = new ArrayList();
    
    while(it.hasNext())
      programFields.add(it.next());
    
    mFieldTypes = new ProgramFieldType[programFields.size()];
    programFields.toArray(mFieldTypes);
    
    mAllFields = new ProgramFieldType[programFields.size()];
    programFields.toArray(mAllFields);
  }
  
  /**
   * If no instance exisits create one.
   * 
   * @return The instance of this class.
   */
  public static ProgramInfoPrintSettings getInstance() {
    if(mInstance == null)
      new ProgramInfoPrintSettings();
    return mInstance;
  }
  
  /**
   * Read the settings from the data file.
   * 
   * @param in The input stream.
   * @throws IOException If something went wrong with the file io.
   * @throws ClassNotFoundException If a class could not be found.
   */
  public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = 0;
    
    try {
      version = in.readInt();
    }catch(Exception e) {}
    
    if(version == 1) {
      mFont = (Font) in.readObject();
      
      int n = in.readInt();
      mFieldTypes = new ProgramFieldType[n];
      
      for(int i = 0; i < n; i++)
        mFieldTypes[i] = ProgramFieldType.getTypeForId(in.readInt());
    }
  }
  
  /**
   * Write the settings to the data file.
   * 
   * @param out The output stream.
   * @throws IOException If something went wrong with the file io.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1);
    out.writeObject(mFont);
    out.writeInt(mFieldTypes.length);
    
    for(int i = 0; i < mFieldTypes.length; i++)
      out.writeInt(mFieldTypes[i].getTypeId());
  }

  /**
   * @return The font for the printing.
   */
  public Font getFont() {
    return mFont;
  }
  
  /**
   * @return The field types to print.
   */
  public ProgramFieldType[] getFieldTypes() {
    return mFieldTypes;
  }
  
  /**
   * @return All possible field types.
   */
  public ProgramFieldType[] getAllFieldTypes() {
    return mAllFields;
  }
  
  /**
   * Set the font of this setting.
   * 
   * @param font The font to set.
   */
  public void setFont(Font font) {
    mFont = font;
  }
    
  /**
   * Set the program field types of this settings.
   * 
   * @param fieldTypes The field types to set.
   */
  public void setFieldTypes(Object[] fieldTypes) {
    mFieldTypes = new ProgramFieldType[fieldTypes.length];
    
    for(int i = 0; i < fieldTypes.length; i++)
      mFieldTypes[i] = (ProgramFieldType)fieldTypes[i];
  }
}
