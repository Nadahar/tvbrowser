/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mediathekplugin;

import java.io.File;
import java.util.Properties;

import util.settings.PropertyBasedSettings;

public class MediathekSettings extends PropertyBasedSettings {
  private static final String KEY_MEDIATHEK_PATH = "mediathekpath";

  public MediathekSettings(final Properties properties) {
    super(properties);
  }

  public String getMediathekPath() {
    String value = get(KEY_MEDIATHEK_PATH, "");
    
    if(value.trim().length() > 0) {
      if(!new File(value).isFile()) {
        value = "";
      }
    }
    
    return value;
  }

  public void setMediathekPath(final String path) {
    set(KEY_MEDIATHEK_PATH, path);
  }
  
  public String guessMediathekPath(boolean save) {
    String value = getMediathekPath();
    
    if(value.trim().length() == 0) {
      File test = new File(System.getProperty("user.home"),".mediathek");
      
      if(test.isDirectory()) {
        test = new File(test,".filme");
        
        if(test.isFile()) {
          value = test.getAbsolutePath();
          
          if(save) {
            setMediathekPath(value);
          }
        }
      }
    }
    
    return value;
  }
}
