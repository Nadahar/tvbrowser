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
package tvbrowser.core.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tvbrowser.core.Settings;
import util.program.ProgramConfiguration;
import util.ui.Localizer;

/**
 * Handles the global program configurations.
 * 
 * @author René Mach
 */
public class ProgramConfigurationManager {
  private static Localizer mLocalizer = Localizer.getLocalizerFor(ProgramConfigurationManager.class);
  
  private static ProgramConfigurationManager mInstance;
  private ProgramConfiguration[] mAvailableProgramConfigurations;
  
  private ProgramConfigurationManager() {
    mInstance = this;
    ObjectInputStream in=null;
    
    try {
      File programConfigFile = new File(Settings.getUserSettingsDirName(),"programConfigurations.dat");
      
      in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(programConfigFile), 0x1000));
      
      in.readInt(); // read version
      
      mAvailableProgramConfigurations = new ProgramConfiguration[in.readInt()];
      
      for(int i = 0; i < mAvailableProgramConfigurations.length; i++)
        mAvailableProgramConfigurations[i] = ProgramConfiguration.readData(in);
      
    }catch(Exception e) {
      mAvailableProgramConfigurations = new ProgramConfiguration[] { getDefaultConfiguration() };
    } finally {
      if (in!=null) {
        try { in.close(); } catch(IOException exc) {}      
      }
    } 
  }
  
  /**
   * Gets the instance of this class.
   * 
   * @return The instance of this class.
   */
  public static ProgramConfigurationManager getInstance() {
    if(mInstance == null)
      new ProgramConfigurationManager();
    
    return mInstance;
  }
  
  /**
   * Gets the available program configurations.
   * 
   * @return The available program configurations.
   */  
  public ProgramConfiguration[] getAvailableProgramConfigurations() {
    return mAvailableProgramConfigurations;
  }
  
  /**
   * Sets the available program configurations.
   * 
   * @param configs The new program configuations array.
   */
  public void setAvailableProgramConfigurations(ProgramConfiguration[] configs) {
    mAvailableProgramConfigurations = configs;
  }
  
  /**
   * Saves the current available program configurations.
   */
  public void store() {
    File programConfigFile = new File(Settings.getUserSettingsDirName(),"programConfigurations.dat");
    try {
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(programConfigFile));
      
      // write version
      out.writeInt(1);
      //write number of configurations
      out.writeInt(mAvailableProgramConfigurations.length);
      
      for(ProgramConfiguration config : mAvailableProgramConfigurations)
        config.writeData(out);
      
    }catch(Exception e) {}
  }
  
  /**
   * Gets the default configuration.
   * 
   * @return The default configuration.
   */
  public static ProgramConfiguration getDefaultConfiguration() {
    return new ProgramConfiguration(mLocalizer.msg("default","Default"),"{title}","{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}","UTF-8");
  }
}
