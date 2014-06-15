/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
package tvbrowser.core.icontheme;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tvbrowser.ui.mainframe.SoftwareUpdater;
import util.io.IOUtilities;

/**
 * Downloads list with available icon themes.
 * Modified from {@link SoftwareUpdater}
 * 
 * @author René Mach
 */
public class ThemeDownloader {
  private ArrayList<ThemeDownloadItem> mItemList;
  
  private ThemeDownloader() {
    mItemList = new ArrayList<ThemeDownloadItem>();
  }
  
  private void loadThemes(URL specs, File iconDir) {
    Pattern namePattern = Pattern.compile("\\[(.*)\\]");
    Pattern keyValuePattern = Pattern.compile("(.+?)=(.*)");
    Matcher matcher;
    
    try {
      BufferedReader read = new BufferedReader(new InputStreamReader(IOUtilities.getStream(specs)));
      
      String line = null;
      ThemeDownloadItem item = null;
      
      while((line = read.readLine()) != null) {
        matcher = namePattern.matcher(line);
        
        if(matcher.find()) {
          item = new ThemeDownloadItem(matcher.group(1));
          mItemList.add(item);
        }
        else {
          matcher=keyValuePattern.matcher(line);

          if (matcher.find()) { // new theme item
            String value = matcher.group(2);

            if(item != null) {
              item.addProperty(matcher.group(1), value);
            }
          }
        }
      }
      
      read.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private ThemeDownloadItem[] getLoadedThemes() {
    ThemeDownloadItem[] items = mItemList.toArray(new ThemeDownloadItem[mItemList.size()]);
    
    Arrays.sort(items);
    
    return items;
  }
  
  public static final ThemeDownloadItem[] downloadThemes(URL specs, File iconDir) {
    if(specs != null && iconDir != null) {
      ThemeDownloader load = new ThemeDownloader();
      load.loadThemes(specs, iconDir);
      
      return load.getLoadedThemes();
    }
    
    return null;
  }
}
