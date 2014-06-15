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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import javax.swing.ImageIcon;

import tvbrowser.ui.update.SoftwareUpdateItem;
import util.io.IOUtilities;

/**
 * Download item for icon theme download
 * Modified from {@link SoftwareUpdateItem}
 * 
 * @author René Mach
 */
public class ThemeDownloadItem implements Comparable<ThemeDownloadItem> {
  public static final String NAME_EN = "name.en";
  public static final String NAME_DE = "name.de";
  public static final String DESCRIPTION_EN = "desc.en";
  public static final String DESCRIPTION_DE = "desc.de";
  public static final String PREVIEW_IMAGE = "preview";
  public static final String DOWNLOAD_URL = "download";
  public static final String AUTHOR = "author";
  
  private Properties mProperties;
  private String mID;
  
  public ThemeDownloadItem(String id) {
    mProperties = new Properties();
    mID = id;
  }
  
  public void addProperty(String key, String value) {
    mProperties.setProperty(key, value);
  }
  
  public String getName(Locale lang) {
    String name = null;
    
    if(lang != null) {
      if(lang.equals(Locale.GERMAN)) {
        name = mProperties.getProperty(NAME_DE, mProperties.getProperty(NAME_EN, mID));
      }
    }
    
    if(name == null) {
      name = mProperties.getProperty(NAME_EN, mID);
    }
    
    return name;
  }
  
  public String getDescription(Locale lang) {
    String description = null;
    
    if(lang != null) {
      if(lang.equals(Locale.GERMAN)) {
        description = mProperties.getProperty(DESCRIPTION_DE, mProperties.getProperty(DESCRIPTION_EN, null));
      }
    }
    
    if(description == null) {
      description = mProperties.getProperty(DESCRIPTION_EN, null);
    }
    
    return description;
  }
  
  public ImageIcon getPreviewImage() {
    String url = mProperties.getProperty(PREVIEW_IMAGE, null);
    
    if(url != null) {
      try {
        byte[] image = IOUtilities.loadFileFromHttpServer(new URL(url));
        return new ImageIcon(image);
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    return null;
  }
  
  public String getAuthor() {
    return mProperties.getProperty(AUTHOR, "Unknown");
  }
  
  public boolean download(File iconDir) {
    if(!iconDir.isDirectory()) {
      iconDir.mkdirs();
    }
    
    if(iconDir.isDirectory()) {
      String url = mProperties.getProperty(DOWNLOAD_URL,null);
      
      if(url != null) {
        File targetFile = new File(iconDir,url.substring(url.lastIndexOf("/")));
        
        try {
          IOUtilities.download(new URL(url), targetFile);
          return targetFile.isFile();
        } catch (MalformedURLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    
    return false;
  }
  
  @Override
  public String toString() {
    return mID;
  }

  @Override
  public int compareTo(ThemeDownloadItem o) {
    return getName(Locale.getDefault()).compareToIgnoreCase(o.getName(Locale.getDefault()));
  }
}
