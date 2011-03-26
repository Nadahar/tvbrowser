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

import util.program.ProgramTextCreator;
import devplugin.ProgramFieldType;

/**
 * The settings for the printing of the program info.
 * 
 * @author René Mach
 *
 */
public class ProgramInfoPrintSettings {

  private Font mFont;
  private Object[] mAllFields;
  private Object[] mFieldTypes;
  private static ProgramInfoPrintSettings mInstance;
  private boolean mPrintImage;
  private boolean mPrintPluginIcons;
  
  private ProgramInfoPrintSettings() {
    mInstance = this;
    mFont = new Font("Dialog",Font.PLAIN,12);
    mPrintImage = true;
    mPrintPluginIcons = true;
    
    mAllFields = mFieldTypes = ProgramTextCreator.getDefaultOrder();
  }
  
  /**
   * If no instance exisits create one.
   * 
   * @return The instance of this class.
   */
  public static ProgramInfoPrintSettings getInstance() {
    if(mInstance == null) {
      new ProgramInfoPrintSettings();
    }
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
    
    if(version >= 1) {
      mFont = (Font) in.readObject();
      
      int n = in.readInt();
      mFieldTypes = new Object[n];
      
      for(int i = 0; i < n; i++) {
        mFieldTypes[i] = ProgramFieldType.getTypeForId(in.readInt());
        
        if(((ProgramFieldType)mFieldTypes[i]).getTypeId() == ProgramFieldType.UNKNOWN_FORMAT) {
          mFieldTypes[i] = ProgramTextCreator.getDurationTypeString();
        }
      }
      
      mPrintImage = in.readBoolean();
      
      if(version == 2) {
        mPrintPluginIcons = in.readBoolean();
      }
    }
  }
  
  /**
   * Write the settings to the data file.
   * 
   * @param out The output stream.
   * @throws IOException If something went wrong with the file io.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(2);
    out.writeObject(mFont);
    out.writeInt(mFieldTypes.length);
    
    for (Object mFieldType : mFieldTypes) {
      if(mFieldType instanceof ProgramFieldType) {
        out.writeInt(((ProgramFieldType)mFieldType).getTypeId());
      } else {
        out.writeInt(ProgramFieldType.UNKNOWN_FORMAT);
      }
    }
    
    out.writeBoolean(mPrintImage);
    out.writeBoolean(mPrintPluginIcons);
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
  public Object[] getFieldTypes() {
    return mFieldTypes;
  }
  
  /**
   * @return All possible field types.
   */
  public Object[] getAllFieldTypes() {
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
   * Set the printImage state of this setting
   * 
   * @param printImage If the image should be printed.
   */
  public void setPrintImage(boolean printImage) {
    mPrintImage = printImage;
  }
  
  /**
   * @return If the image should be printed.
   */
  public boolean isPrintImage() {
    return mPrintImage;
  }

  /**
   * Set the printPluginIcons state of this setting
   * 
   * @param printIcons If the plugin icons should be printed.
   */
  public void setPrintPluginIcons(boolean printIcons) {
    mPrintPluginIcons = printIcons;
  }
  
  /**
   * @return If the plugin icons should be printed.
   */
  public boolean isPrintPluginIcons() {
    return mPrintPluginIcons;
  }

  
  /**
   * Set the program field types of this settings.
   * 
   * @param fieldTypes The field types to set.
   */
  public void setFieldTypes(Object[] fieldTypes) {
    mFieldTypes = fieldTypes;
  }
}
