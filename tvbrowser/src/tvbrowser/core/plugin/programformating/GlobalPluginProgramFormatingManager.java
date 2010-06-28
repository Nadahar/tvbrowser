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
package tvbrowser.core.plugin.programformating;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tvbrowser.core.Settings;
import util.io.stream.ObjectOutputStreamProcessor;
import util.io.stream.StreamUtilities;
import util.ui.Localizer;

/**
 * Handles the global program configurations.
 * 
 * @author René Mach
 * @since 2.5.1
 */
public class GlobalPluginProgramFormatingManager {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GlobalPluginProgramFormatingManager.class);
  
  private static GlobalPluginProgramFormatingManager mInstance;
  private GlobalPluginProgramFormating[] mAvailableProgramConfigurations;
  
  private GlobalPluginProgramFormatingManager() {
    mInstance = this;
    ObjectInputStream in=null;
    
    try {
      File programConfigFile = new File(Settings.getUserSettingsDirName(),"programConfigurations.dat");
      
      in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(programConfigFile), 0x1000));
      
      in.readInt(); // read version
      
      mAvailableProgramConfigurations = new GlobalPluginProgramFormating[in.readInt()];
      
      for(int i = 0; i < mAvailableProgramConfigurations.length; i++) {
        mAvailableProgramConfigurations[i] = GlobalPluginProgramFormating.load(in);
      }
      
    }catch(Exception e) {
      mAvailableProgramConfigurations = new GlobalPluginProgramFormating[2];
      mAvailableProgramConfigurations[0] = getDefaultConfiguration();
      new Thread("Plugin formating creation") {
        public void run() {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {}
          
          mAvailableProgramConfigurations[1] = getTvPearlFormating();
          store();
        }
      }.start();
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
  public static GlobalPluginProgramFormatingManager getInstance() {
    if(mInstance == null) {
      new GlobalPluginProgramFormatingManager();
    }
    
    return mInstance;
  }
  
  /**
   * Gets the available program configurations.
   * 
   * @return The available program configurations.
   */
  public GlobalPluginProgramFormating[] getAvailableGlobalPluginProgramFormatings() {
    return mAvailableProgramConfigurations;
  }

  /**
   * Sets the available program configurations.
   * 
   * @param configs
   *          The new program configurations array.
   */
  public void setAvailableProgramConfigurations(GlobalPluginProgramFormating[] configs) {
    mAvailableProgramConfigurations = configs;
  }
  
  /**
   * Saves the current available program configurations.
   */
  public void store() {
    File programConfigFile = new File(Settings.getUserSettingsDirName(),"programConfigurations.dat");
    StreamUtilities.objectOutputStreamIgnoringExceptions(programConfigFile,
        new ObjectOutputStreamProcessor() {
          public void process(ObjectOutputStream out) throws IOException {
            // write version
            out.writeInt(1);
            // write number of configurations
            out.writeInt(mAvailableProgramConfigurations.length);

            for (GlobalPluginProgramFormating config : mAvailableProgramConfigurations) {
              config.store(out);
            }
            out.close();
          }
        });
  }
  
  /**
   * Gets the default configuration.
   * 
   * @return The default configuration.
   */
  public static GlobalPluginProgramFormating getDefaultConfiguration() {
    return new GlobalPluginProgramFormating(mLocalizer.msg("default","Default"),"{title}","{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n","UTF-8");
  }
  
  /**
   * Gets the TV pearl formating.
   * 
   * @return The TV pearl formating.
   * @since 2.6
   */
  public static GlobalPluginProgramFormating getTvPearlFormating() {
    return new GlobalPluginProgramFormating(mLocalizer.msg("tvPearl","TV Pearl"),"{title}","{start_day_of_week}, {start_day}. {start_month_name}, {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}, {channel_name}\n{title}\n\n{genre}","UTF-8");
  }
  
  protected GlobalPluginProgramFormating getConfigurationForId(String id) {
    for(GlobalPluginProgramFormating config : mAvailableProgramConfigurations) {
      if(config.hasId(id)) {
        return config;
      }
    }
    
    return null;
  }
  
  /**
   * Gets the instance of the given formating.
   * 
   * @param formating The formating to get the instance for.
   * @return The instance of the given formating.
   */
  public GlobalPluginProgramFormating getFormatingInstanceForInstance(GlobalPluginProgramFormating formating) {
    for(GlobalPluginProgramFormating config : mAvailableProgramConfigurations) {
      if(config.equals(formating)) {
        return config;
      }
    }
    
    return null;
  }
}
