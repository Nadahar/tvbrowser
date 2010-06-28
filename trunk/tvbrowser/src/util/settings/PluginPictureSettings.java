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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tvbrowser.core.Settings;

/**
 * Contains the settings of the picture settings of a plugin.
 * 
 * @author René Mach
 * @since 2.6
 */
public class PluginPictureSettings {
  /** type to be used for showing pictures like the global plugin settings */
  public static final int ALL_PLUGINS_SETTINGS_TYPE = 0;
  /** type to be used for showing pictures and discription */
  public static final int PICTURE_AND_DISCRIPTION_TYPE = 1;
  /** type to be used for only showing pictures */
  public static final int ONLY_PICTURE_TYPE = 2;
  /** type to be used for not showing pictures */
  public static final int NO_PICTURE_TYPE = 3;
  
  private int mType;
  
  /**
   * Creates an instance of this settings with the given type.
   * 
   * @param type The type to be used for this settings.
   */
  public PluginPictureSettings(int type) {
    mType = type;
  }

  /**
   * Creates an instance of this settings from the given stream.
   * 
   * @param in The stream to read the values from.
   * @throws IOException Thrown if something went wrong.
   * @throws ClassNotFoundException Thrown if something went wrong.
   */
  public PluginPictureSettings(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readShort(); // version
    
    mType = in.readInt();
  }
  
  /**
   * Writes the data to the given output stream.
   * 
   * @param out The stream to store the data to.
   * @throws IOException Thrown if something went wrong.
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeShort(1); //version
    
    out.writeInt(mType);
  }
  
  /**
   * Gets if the pictures should be shown for this setting.
   * 
   * @return <code>True</code> if the picture is to be shown,
   * <code>false</code> otherwise.
   */
  public boolean isShowingPictures() {
    if(mType == ALL_PLUGINS_SETTINGS_TYPE) {
      return Settings.propPluginsPictureSetting.getInt() != NO_PICTURE_TYPE;
    }
    
    return mType != NO_PICTURE_TYPE;
  }
  
  /**
   * Gets if the picture description is to be shown for this setting.
   * 
   * @return <code>True</code> if the picture description is to be shown,
   * <code>false</code> otherwise.
   */
  public boolean isShowingDescription() {
    if(mType == ALL_PLUGINS_SETTINGS_TYPE) {
      return Settings.propPluginsPictureSetting.getInt() == PICTURE_AND_DISCRIPTION_TYPE;
    }
    
    return mType == PICTURE_AND_DISCRIPTION_TYPE;
  }
  
  /**
   * Gets the type of this settings.
   * 
   * @return The type of this settings.
   */
  public int getType() {
    return mType;
  }
}
