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
 *     $Date: 2007-01-09 18:37:06 +0100 (Di, 09 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2997 $
 */
package tvbrowser.core.plugin.programformating;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.program.AbstractPluginProgramFormating;

/**
 * A class for global program configuration settings.
 * 
 * @author René Mach
 * @since 2.5.1
 */
public class GlobalPluginProgramFormating extends AbstractPluginProgramFormating {  
  private String mId;
  private String mName;
  private String mTitleValue;
  private String mContentValue;
  private String mEncodingValue;  

  /**
   * Creates an empty instance of this class.
   */
  public GlobalPluginProgramFormating() {
    mName = null;
    mTitleValue = null;
    mContentValue = null;
    mEncodingValue = null;
  }
  
  protected GlobalPluginProgramFormating(String name, String titleValue, String contentValue, String encodingValue) {
    mName = name;
    mTitleValue = titleValue;
    mContentValue = contentValue;
    mEncodingValue = encodingValue;
    mId = "#id_" + System.currentTimeMillis();
  }  

  protected void setName(String name) {
    mName = name;
  }
  
  protected void setTitleValue(String value) {
    mTitleValue = value;
  }
  
  protected void setContentValue(String value) {
    mContentValue = value;
  }
  
  protected void setEncodingValue(String value) {
    mEncodingValue = value;
  }
  
  public String getContentValue() {
    GlobalPluginProgramFormating config = GlobalPluginProgramFormatingManager.getInstance().getConfigurationForId(mId);
    
    if(config != null)
      return config.mContentValue;
    else if(mContentValue != null)
      return mContentValue;
    
    return null;
  }

  public String getEncodingValue() {
    GlobalPluginProgramFormating config = GlobalPluginProgramFormatingManager.getInstance().getConfigurationForId(mId);
    
    if(config != null)
      return config.mEncodingValue;
    else if(mEncodingValue != null)
      return mEncodingValue;
    
    return null;
  }

  public String getName() {
    GlobalPluginProgramFormating config = GlobalPluginProgramFormatingManager.getInstance().getConfigurationForId(mId);
    
    if(config != null)
      return config.mName;
    else if(mName != null)
      return mName;
    
    return null;
  }

  public String getTitleValue() {
    GlobalPluginProgramFormating config = GlobalPluginProgramFormatingManager.getInstance().getConfigurationForId(mId);
    
    if(config != null)
      return config.mTitleValue;
    else if(mTitleValue != null)
      return mTitleValue;
    
    return null;
  }
  
  protected static GlobalPluginProgramFormating load(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); //read version
    
    GlobalPluginProgramFormating config = new GlobalPluginProgramFormating();
    
    config.mId = (String)in.readObject();
    config.mName = (String)in.readObject();
    config.mTitleValue = (String)in.readObject();
    config.mContentValue = (String)in.readObject();
    config.mEncodingValue = (String)in.readObject();
    
    return config;
  }

  protected void store(ObjectOutputStream out) throws IOException {
    out.writeInt(1); //write version
    out.writeObject(mId);
    out.writeObject(mName);
    out.writeObject(mTitleValue);
    out.writeObject(mContentValue);
    out.writeObject(mEncodingValue);
  }
  
  protected void loadData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); //read version
    mId = (String)in.readObject();
  }
  
  protected void storeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); //write version
    out.writeObject(mId);
  }
  
  /**
   * Checks if this global program configuration has the given id.
   * 
   * @param id The id to check.
   * @return <code>true</code> if the ids are the same, <code>false</code> otherwise;
   */
  public boolean hasId(String id) {
    if(id != null)
      return id.compareTo(mId) == 0;
    
    return false;
  }
  
  public String toString() {
    return getName();
  }
  
  public boolean equals(Object o) {
    if(o != null && o instanceof GlobalPluginProgramFormating)
      return ((GlobalPluginProgramFormating)o).mId.compareTo(mId) == 0;
    
    return false;
  }

  @Override
  public boolean isValid() {
    return GlobalPluginProgramFormatingManager.getInstance().getConfigurationForId(mId) != null;
  }

  @Override
  public String getId() {
    return mId;
  }
}
